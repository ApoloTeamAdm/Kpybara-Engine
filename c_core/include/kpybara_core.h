/**
 * @file kpybara_core.h
 * @brief Kpybara Universal Core C++20 - Zero-Copy C-ABI FFI Header
 * @version 2.1.0
 * 
 * Este arquivo define o contrato de interface de alta performance (FFI)
 * entre a UI Flutter/Dart e o motor nativo Kpybara escrito em C++20.
 * Utiliza alinhamento compacto (#pragma pack), sem alocações de heap
 * no caminho crítico e tempo de execução garantido < 300 ms com teto de 10 MB de RAM.
 *
 * Licença: AGPLv3
 */

#ifndef KPYBARA_CORE_H
#define KPYBARA_CORE_H

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

#define KPY_MAGIC_HEADER 0x4B505932 // 'KPY2'
#define KPY_MAX_FEEDBACK_LEN 256
#define KPY_MAX_BRANCH_LEN 64
#define KPY_DEFAULT_TIMEOUT_MS 300
#define KPY_MAX_MEMORY_CEILING_KB 10240 // 10 MB

#pragma pack(push, 1)

/**
 * @struct KpyStringViewC
 * @brief Representação leve de ponteiro e comprimento de string, evitando cópias (Zero-Copy).
 */
typedef struct {
    const char* data;      /**< Ponteiro para os caracteres UTF-8 não-mutáveis */
    uint32_t length;       /**< Comprimento em bytes */
} KpyStringViewC;

/**
 * @struct KpyEvalRequest
 * @brief Struct compacta transmitida diretamente por ponteiro (FFI Zero-Copy).
 */
typedef struct {
    uint32_t magic;                /**< Assinatura do protocolo KPY_MAGIC_HEADER */
    KpyStringViewC student_answer; /**< Resposta submetida pelo aluno */
    KpyStringViewC target_sentence;/**< Gabarito / sentença alvo */
    KpyStringViewC prompt;         /**< Enunciado do exercício */
    uint32_t timeout_ms;           /**< Timeout máximo para o Watchdog (< 300 ms) */
    uint32_t max_memory_kb;        /**< Teto de alocação de memória (<= 10240 KB) */
} KpyEvalRequest;

/**
 * @struct KpyEvalResponse
 * @brief Resposta com alinhamento de 1 byte, transmitida por referência direta.
 */
typedef struct {
    uint8_t is_correct;                    /**< 1 se correto, 0 se incorreto */
    double score;                          /**< Nota normalizada entre 0.0 e 1.0 */
    char feedback[KPY_MAX_FEEDBACK_LEN];   /**< Mensagem pedagógica amigável em PT-BR */
    char next_lesson[KPY_MAX_BRANCH_LEN];  /**< Próximo nó de lição ou reforço */
    uint32_t execution_time_us;            /**< Latência de execução em microssegundos */
    uint32_t memory_used_kb;               /**< Memória utilizada pelo Arena Allocator */
    uint8_t timed_out;                     /**< 1 se o Watchdog abortou por timeout */
    uint8_t error_code;                    /**< 0 se OK, >0 se código de erro */
} KpyEvalResponse;

#pragma pack(pop)

/**
 * @brief Inicializa o subsistema Kpybara Core e a Arena de Memória global.
 * @return 0 em caso de sucesso, código de erro caso contrário.
 */
int32_t kpy_init(void);

/**
 * @brief Avalia uma submissão de exercício com tempo < 300 ms e isolamento de memória.
 * @param request Ponteiro para a requisição de avaliação.
 * @param response Ponteiro para a struct de resposta preenchida pelo motor.
 * @return 0 em caso de sucesso, código de erro caso contrário.
 */
int32_t kpy_evaluate(const KpyEvalRequest* request, KpyEvalResponse* response);

/**
 * @brief Reseta o Arena Allocator em tempo O(1), liberando a memória sem churn de heap.
 */
void kpy_reset_arena(void);

/**
 * @brief Encerra o subsistema do Kpybara Core e desaloca os recursos reservados.
 */
void kpy_shutdown(void);

/**
 * @brief Retorna a versão semântica do motor nativo C++20.
 */
const char* kpy_version(void);

#ifdef __cplusplus
}
#endif

#endif // KPYBARA_CORE_H
