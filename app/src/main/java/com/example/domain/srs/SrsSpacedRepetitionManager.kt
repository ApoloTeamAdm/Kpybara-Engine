package com.example.domain.srs

import com.example.data.local.dao.SrsDao
import com.example.data.local.entity.SrsCardEntity
import kotlin.math.roundToInt

class SrsSpacedRepetitionManager(
    private val srsDao: SrsDao
) {
    /**
     * Aplica o algoritmo SuperMemo-2 (SM-2) a um cartão.
     * @param card O cartão atual
     * @param quality Nota de 0 a 5:
     *   5 - Resposta perfeita e imediata
     *   4 - Resposta correta com leve hesitação
     *   3 - Resposta correta com dificuldade considerável
     *   2 - Resposta incorreta mas onde a correta parecia fácil
     *   1 - Resposta incorreta; lembrava da resposta após vê-la
     *   0 - Apagão completo
     */
    suspend fun reviewCard(card: SrsCardEntity, quality: Int): SrsCardEntity {
        val q = quality.coerceIn(0, 5)

        val nextRepetitions: Int
        val nextInterval: Int
        val nextEaseFactor: Double

        if (q >= 3) {
            nextRepetitions = card.repetitions + 1
            nextInterval = when (card.repetitions) {
                0 -> 1
                1 -> 6
                else -> (card.interval_days * card.ease_factor).roundToInt().coerceAtLeast(1)
            }
        } else {
            nextRepetitions = 0
            nextInterval = 1
        }

        // Fórmula SM-2: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val delta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)
        nextEaseFactor = (card.ease_factor + delta).coerceAtLeast(1.3)

        val nextReviewEpoch = System.currentTimeMillis() + (nextInterval * 86_400_000L)

        val updatedCard = card.copy(
            repetitions = nextRepetitions,
            interval_days = nextInterval,
            ease_factor = nextEaseFactor,
            next_review_epoch = nextReviewEpoch,
            last_review_epoch = System.currentTimeMillis()
        )

        srsDao.updateCard(updatedCard)
        return updatedCard
    }
}
