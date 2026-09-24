/**
 * @file kpybara_core.cpp
 * @brief Implementação C++20 do Kpybara Universal Core Runtime
 *
 * Cumpre as diretrizes do C++ Core Guidelines:
 * - Const-correctness estrita
 * - Sem alocação dinâmica no heap dentro do ciclo de avaliação (Zero Heap Churn)
 * - Watchdog assíncrono com interrupção em < 300 ms
 * - Comunicação Zero-Copy via C-ABI
 */

#include "kpybara_core.h"
#include "kpybara_arena.h"
#include "kpybara_watchdog.h"

#include <string_view>
#include <algorithm>
#include <chrono>
#include <cstdio>
#include <cctype>
#include <memory>
#include <vector>

namespace {

// Instância global protegida da Arena de 10 MB
static std::unique_ptr<kpybara::ArenaAllocator> g_arena = nullptr;

// Helper: Normaliza caracteres removendo pontuações básicas e convertendo para minúsculas
inline char normalize_char(char c) noexcept {
    return static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
}

// Algoritmo de Distância Levenshtein otimizado usando memória contígua da Arena
size_t compute_levenshtein(
    std::string_view s1,
    std::string_view s2,
    kpybara::ArenaAllocator& arena,
    const std::atomic<bool>& abortFlag
) noexcept {
    const size_t m = s1.size();
    const size_t n = s2.size();

    if (m == 0) return n;
    if (n == 0) return m;

    // Buffer de 2 linhas na Arena para complexidade de espaço O(min(m, n))
    const size_t rowSize = (n + 1) * sizeof(size_t);
    auto* prev = static_cast<size_t*>(arena.allocate(rowSize, alignof(size_t)));
    auto* curr = static_cast<size_t*>(arena.allocate(rowSize, alignof(size_t)));

    if (!prev || !curr) [[unlikely]] {
        return (m > n) ? (m - n) : (n - m); // Fallback conservador se esgotar arena
    }

    for (size_t j = 0; j <= n; ++j) {
        prev[j] = j;
    }

    for (size_t i = 1; i <= m; ++i) {
        if (abortFlag.load(std::memory_order_relaxed)) [[unlikely]] {
            return 9999; // Interrompido pelo Watchdog
        }

        curr[0] = i;
        const char c1 = normalize_char(s1[i - 1]);

        for (size_t j = 1; j <= n; ++j) {
            const char c2 = normalize_char(s2[j - 1]);
            const size_t cost = (c1 == c2) ? 0 : 1;

            curr[j] = std::min({
                prev[j] + 1,        // Deleção
                curr[j - 1] + 1,    // Inserção
                prev[j - 1] + cost  // Substituição
            });
        }

        std::memcpy(prev, curr, rowSize);
    }

    return curr[n];
}

} // namespace anonymous

extern "C" {

int32_t kpy_init(void) {
    if (!g_arena) {
        g_arena = std::make_unique<kpybara::ArenaAllocator>(kpybara::ArenaAllocator::DEFAULT_CAPACITY);
    }
    return (g_arena && g_arena->is_valid()) ? 0 : -1;
}

int32_t kpy_evaluate(const KpyEvalRequest* request, KpyEvalResponse* response) {
    if (!request || !response) [[unlikely]] {
        return -1;
    }

    if (request->magic != KPY_MAGIC_HEADER) [[unlikely]] {
        response->error_code = 1; // Assinatura mágica inválida
        return -2;
    }

    if (!g_arena || !g_arena->is_valid()) [[unlikely]] {
        kpy_init();
        if (!g_arena || !g_arena->is_valid()) {
            response->error_code = 2; // Falha na Arena
            return -3;
        }
    }

    const auto startTime = std::chrono::steady_clock::now();

    // Reset O(1) do ponteiro de alocação da Arena para cada avaliação
    g_arena->reset();

    // Limpeza da struct de resposta
    std::memset(response, 0, sizeof(KpyEvalResponse));

    // Zero-Copy views sobre os dados da struct C
    const std::string_view student(
        request->student_answer.data ? request->student_answer.data : "",
        request->student_answer.length
    );
    const std::string_view target(
        request->target_sentence.data ? request->target_sentence.data : "",
        request->target_sentence.length
    );

    // Configuração do Watchdog com limite de 300 ms (ou customizado na request)
    const uint32_t timeoutMs = (request->timeout_ms > 0 && request->timeout_ms <= 1000)
        ? request->timeout_ms
        : KPY_DEFAULT_TIMEOUT_MS;

    kpybara::ExecutionWatchdog watchdog(timeoutMs);
    std::atomic<bool> isAborted{false};

    watchdog.start([&isAborted]() {
        isAborted.store(true, std::memory_order_relaxed);
    });

    // 1. Caminho Ultrarrápido: Comparação exata O(1)
    bool isExactMatch = false;
    if (student.size() == target.size()) {
        isExactMatch = std::equal(
            student.begin(), student.end(),
            target.begin(),
            [](char a, char b) noexcept {
                return normalize_char(a) == normalize_char(b);
            }
        );
    }

    if (isExactMatch) {
        watchdog.stop();
        response->is_correct = 1;
        response->score = 1.0;
        std::snprintf(response->feedback, sizeof(response->feedback),
            "Excelente! Resposta perfeitamente exata. A capivara Kpy comemora!");
        std::snprintf(response->next_lesson, sizeof(response->next_lesson), "next_node");
    } else {
        // 2. Caminho Heurístico via Levenshtein na Arena com verificação do Watchdog
        const size_t maxLen = std::max(student.size(), target.size());
        const size_t distance = compute_levenshtein(student, target, *g_arena, isAborted);

        watchdog.stop();

        if (watchdog.has_timed_out() || isAborted.load(std::memory_order_relaxed)) {
            response->timed_out = 1;
            response->error_code = 3;
            response->is_correct = 0;
            response->score = 0.0;
            std::snprintf(response->feedback, sizeof(response->feedback),
                "O tempo de execução excedeu o limite de segurança (%u ms). Tente novamente!", timeoutMs);
        } else {
            const double calculatedScore = (maxLen > 0)
                ? (1.0 - (static_cast<double>(distance) / static_cast<double>(maxLen)))
                : 0.0;
            const double normalizedScore = std::max(0.0, std::min(1.0, calculatedScore));

            response->score = normalizedScore;

            if (normalizedScore >= 0.85) {
                response->is_correct = 1;
                std::snprintf(response->feedback, sizeof(response->feedback),
                    "Mandou bem! Quase perfeito, apenas pequenos desvios tipográficos.");
                std::snprintf(response->next_lesson, sizeof(response->next_lesson), "next_node");
            } else if (normalizedScore >= 0.50) {
                response->is_correct = 0;
                std::snprintf(response->feedback, sizeof(response->feedback),
                    "Foi por pouco! Você está no caminho certo. Revise a sintaxe.");
                std::snprintf(response->next_lesson, sizeof(response->next_lesson), "reinforce_node");
            } else {
                response->is_correct = 0;
                std::snprintf(response->feedback, sizeof(response->feedback),
                    "Não desanime! Kpy está aqui para te ajudar a tentar outra vez.");
                std::snprintf(response->next_lesson, sizeof(response->next_lesson), "retry_node");
            }
        }
    }

    const auto endTime = std::chrono::steady_clock::now();
    const auto elapsedUs = std::chrono::duration_cast<std::chrono::microseconds>(endTime - startTime).count();

    response->execution_time_us = static_cast<uint32_t>(elapsedUs);
    response->memory_used_kb = static_cast<uint32_t>(g_arena->used_bytes() / 1024);

    return 0;
}

void kpy_reset_arena(void) {
    if (g_arena) {
        g_arena->reset();
    }
}

void kpy_shutdown(void) {
    g_arena.reset();
}

const char* kpy_version(void) {
    return "2.1.0-c20-arena-fastpath";
}

} // extern "C"
