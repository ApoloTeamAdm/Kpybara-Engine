/**
 * @file kpybara_watchdog.h
 * @brief Thread de Watchdog (< 300 ms) com interrupção assíncrona segura.
 *
 * Monitora o tempo de execução da avaliação e sinaliza a interrupção caso
 * o script ou rotina nativa exceda o SLA rigoroso de 300 milissegundos.
 */

#ifndef KPYBARA_WATCHDOG_H
#define KPYBARA_WATCHDOG_H

#include <chrono>
#include <thread>
#include <atomic>
#include <condition_variable>
#include <mutex>
#include <functional>

namespace kpybara {

class ExecutionWatchdog {
public:
    using TimeoutCallback = std::function<void()>;

    explicit ExecutionWatchdog(uint32_t timeout_ms = 300) noexcept
        : m_timeoutMs(timeout_ms), m_running(false), m_aborted(false) {}

    ~ExecutionWatchdog() noexcept {
        stop();
    }

    // Não-copiável
    ExecutionWatchdog(const ExecutionWatchdog&) = delete;
    ExecutionWatchdog& operator=(const ExecutionWatchdog&) = delete;

    /**
     * @brief Inicia o monitoramento de timeout em thread isolada.
     * @param onTimeout Callback executado caso o tempo expire.
     */
    void start(TimeoutCallback onTimeout = nullptr) noexcept {
        stop(); // Garante estado limpo anterior

        m_aborted.store(false, std::memory_order_relaxed);
        m_running.store(true, std::memory_order_release);

        m_worker = std::thread([this, cb = std::move(onTimeout)]() {
            std::unique_lock<std::mutex> lock(m_mutex);
            const bool completedNormally = m_cv.wait_for(
                lock,
                std::chrono::milliseconds(m_timeoutMs),
                [this]() { return !m_running.load(std::memory_order_acquire); }
            );

            if (!completedNormally && m_running.load(std::memory_order_acquire)) {
                m_aborted.store(true, std::memory_order_release);
                if (cb) {
                    cb();
                }
            }
        });
    }

    /**
     * @brief Para o watchdog de forma síncrona e segura.
     */
    void stop() noexcept {
        if (m_running.exchange(false, std::memory_order_acq_rel)) {
            {
                std::lock_guard<std::mutex> lock(m_mutex);
                m_cv.notify_all();
            }
            if (m_worker.joinable()) {
                m_worker.join();
            }
        }
    }

    [[nodiscard]] bool has_timed_out() const noexcept {
        return m_aborted.load(std::memory_order_acquire);
    }

    void set_timeout_ms(uint32_t ms) noexcept {
        m_timeoutMs = ms;
    }

private:
    uint32_t m_timeoutMs;
    std::atomic<bool> m_running{false};
    std::atomic<bool> m_aborted{false};
    std::mutex m_mutex;
    std::condition_variable m_cv;
    std::thread m_worker;
};

} // namespace kpybara

#endif // KPYBARA_WATCHDOG_H
