/**
 * @file kpybara_arena.h
 * @brief Arena Allocator em C++20 com teto rígido de 10 MB e reset O(1).
 *
 * Localização: motor/include/kpybara_arena.h
 * Garante alocação linear contígua sem fragmentação de memória ou chamadas
 * de sistema (brk/mmap) durante a execução dos scripts e avaliações.
 */

#ifndef KPYBARA_ARENA_H
#define KPYBARA_ARENA_H

#include <cstddef>
#include <cstdint>
#include <new>
#include <utility>
#include <cstring>
#include <atomic>

namespace kpybara {

class ArenaAllocator {
public:
    static constexpr size_t DEFAULT_CAPACITY = 10 * 1024 * 1024; // 10 MB Estritos

    explicit ArenaAllocator(size_t capacity = DEFAULT_CAPACITY) noexcept
        : m_capacity(capacity), m_offset(0), m_peakUsage(0) {
        // Aloca o bloco contíguo na inicialização do subsistema
        m_buffer = static_cast<uint8_t*>(::operator new(capacity, std::nothrow));
    }

    ~ArenaAllocator() noexcept {
        if (m_buffer != nullptr) {
            ::operator delete(m_buffer);
            m_buffer = nullptr;
        }
    }

    // Não-copiável e não-atribuível (Singleton ou gerenciado explicitamente)
    ArenaAllocator(const ArenaAllocator&) = delete;
    ArenaAllocator& operator=(const ArenaAllocator&) = delete;

    ArenaAllocator(ArenaAllocator&& other) noexcept
        : m_buffer(std::exchange(other.m_buffer, nullptr)),
          m_capacity(other.m_capacity),
          m_offset(other.m_offset),
          m_peakUsage(other.m_peakUsage) {}

    ArenaAllocator& operator=(ArenaAllocator&& other) noexcept {
        if (this != &other) {
            if (m_buffer != nullptr) {
                ::operator delete(m_buffer);
            }
            m_buffer = std::exchange(other.m_buffer, nullptr);
            m_capacity = other.m_capacity;
            m_offset = other.m_offset;
            m_peakUsage = other.m_peakUsage;
        }
        return *this;
    }

    /**
     * @brief Aloca um bloco linear com alinhamento correto em tempo O(1).
     * @param bytes Quantidade de bytes solicitada.
     * @param alignment Alinhamento requerido (padrão: alinhamento máximo escalar).
     * @return Ponteiro para a memória alocada ou nullptr se exceder o teto de 10 MB.
     */
    [[nodiscard]] void* allocate(size_t bytes, size_t alignment = alignof(std::max_align_t)) noexcept {
        if (m_buffer == nullptr || bytes == 0) [[unlikely]] {
            return nullptr;
        }

        const size_t currentAddr = reinterpret_cast<size_t>(m_buffer + m_offset);
        const size_t alignedAddr = (currentAddr + (alignment - 1)) & ~(alignment - 1);
        const size_t padding = alignedAddr - currentAddr;

        if (m_offset + padding + bytes > m_capacity) [[unlikely]] {
            return nullptr; // Teto rígido de 10 MB excedido com segurança
        }

        m_offset += padding;
        void* const ptr = m_buffer + m_offset;
        m_offset += bytes;

        if (m_offset > m_peakUsage) {
            m_peakUsage = m_offset;
        }

        return ptr;
    }

    /**
     * @brief Cria um objeto tipado no espaço da Arena.
     */
    template <typename T, typename... Args>
    [[nodiscard]] T* create(Args&&... args) noexcept {
        void* const mem = allocate(sizeof(T), alignof(T));
        if (mem == nullptr) [[unlikely]] {
            return nullptr;
        }
        return ::new (mem) T(std::forward<Args>(args)...);
    }

    /**
     * @brief Reseta o ponteiro da Arena em tempo O(1).
     */
    void reset() noexcept {
        m_offset = 0;
    }

    [[nodiscard]] size_t used_bytes() const noexcept {
        return m_offset;
    }

    [[nodiscard]] size_t peak_bytes() const noexcept {
        return m_peakUsage;
    }

    [[nodiscard]] size_t capacity() const noexcept {
        return m_capacity;
    }

    [[nodiscard]] bool is_valid() const noexcept {
        return m_buffer != nullptr;
    }

private:
    uint8_t* m_buffer{nullptr};
    size_t m_capacity{0};
    size_t m_offset{0};
    size_t m_peakUsage{0};
};

} // namespace kpybara

#endif // KPYBARA_ARENA_H
