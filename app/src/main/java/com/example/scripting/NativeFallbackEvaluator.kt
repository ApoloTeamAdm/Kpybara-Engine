package com.example.scripting

import com.example.domain.model.Exercise
import com.example.domain.model.ExerciseType
import kotlin.math.max

/**
 * Avaliador Nativo Resiliente com Fallback Automático.
 * Garante execução ultraleve (< 1ms) sem dependências externas,
 * fornecendo compatibilidade e alta performance para dispositivos de 2 GB de RAM.
 */
object NativeFallbackEvaluator {

    fun evaluate(
        exercise: Exercise,
        studentAnswer: String,
        telemetryError: String? = null
    ): EvaluationResult {
        val payload = exercise.payload
        val cleanInput = normalize(studentAnswer)

        when (exercise.type) {
            ExerciseType.MULTIPLE_CHOICE -> {
                val correctIndex = payload.correct_option_index ?: 0
                val correctOption = payload.options.getOrNull(correctIndex) ?: ""
                val isCorrect = cleanInput == normalize(correctOption) ||
                        cleanInput == correctIndex.toString()

                val srsInterval = if (isCorrect) 3 else 1
                val feedback = if (isCorrect) {
                    payload.explanation ?: "Correto! Muito bem."
                } else {
                    "A resposta correta é: '$correctOption'. ${payload.explanation ?: ""}".trim()
                }

                return EvaluationResult(
                    is_correct = isCorrect,
                    score_ratio = if (isCorrect) 1.0 else 0.0,
                    feedback_message = feedback,
                    next_recommended_lesson_id = if (!isCorrect) "reinforcement_${exercise.exercise_id}" else null,
                    srs_interval_days = srsInterval,
                    used_fallback = true,
                    telemetry_error = telemetryError
                )
            }

            ExerciseType.WORD_ORDERING, ExerciseType.FILL_IN_BLANK -> {
                val targetSentence = payload.target_sentence ?: ""
                val cleanTarget = normalize(targetSentence)

                val distance = levenshtein(cleanInput, cleanTarget)
                val maxLen = max(cleanInput.length, cleanTarget.length)
                val similarity = if (maxLen == 0) 1.0 else 1.0 - (distance.toDouble() / maxLen.toDouble())
                val isCorrect = similarity >= 0.88

                val feedback = if (isCorrect) {
                    if (similarity == 1.0) "Perfeito! Ordem e sintaxe exatas."
                    else "Muito bom! Quase exato: '$targetSentence'."
                } else {
                    "Incorreto. A frase correta é: '$targetSentence'."
                }

                return EvaluationResult(
                    is_correct = isCorrect,
                    score_ratio = similarity.coerceIn(0.0, 1.0),
                    feedback_message = feedback,
                    next_recommended_lesson_id = if (!isCorrect) "reinforcement_${exercise.exercise_id}" else null,
                    srs_interval_days = if (isCorrect) (if (similarity > 0.95) 4 else 2) else 1,
                    used_fallback = true,
                    telemetry_error = telemetryError
                )
            }

            ExerciseType.PAIR_MATCHING -> {
                // Para pares, studentAnswer pode ser "pairs_matched:total"
                val isAllMatched = studentAnswer.contains("completed") || studentAnswer == "true"
                return EvaluationResult(
                    is_correct = isAllMatched,
                    score_ratio = if (isAllMatched) 1.0 else 0.0,
                    feedback_message = if (isAllMatched) "Excelente! Todos os pares correspondidos." else "Alguns pares ficaram incorretos.",
                    next_recommended_lesson_id = if (!isAllMatched) "reinforcement_${exercise.exercise_id}" else null,
                    srs_interval_days = if (isAllMatched) 3 else 1,
                    used_fallback = true,
                    telemetry_error = telemetryError
                )
            }

            ExerciseType.LISTENING_COMPREHENSION -> {
                val targetAudioText = payload.audio_text ?: payload.target_sentence ?: ""
                val cleanTarget = normalize(targetAudioText)
                val isCorrect = cleanInput == cleanTarget || levenshtein(cleanInput, cleanTarget) <= 2

                return EvaluationResult(
                    is_correct = isCorrect,
                    score_ratio = if (isCorrect) 1.0 else 0.2,
                    feedback_message = if (isCorrect) "Excelente compreensão auditiva!" else "O áudio dizia: '$targetAudioText'.",
                    next_recommended_lesson_id = if (!isCorrect) "reinforcement_${exercise.exercise_id}" else null,
                    srs_interval_days = if (isCorrect) 3 else 1,
                    used_fallback = true,
                    telemetry_error = telemetryError
                )
            }

            ExerciseType.PRONUNCIATION_SPEECH -> {
                val target = payload.target_sentence ?: payload.audio_text ?: ""
                val similarity = calculateSimilarity(cleanInput, normalize(target))
                val isCorrect = similarity >= 0.75

                return EvaluationResult(
                    is_correct = isCorrect,
                    score_ratio = similarity,
                    feedback_message = if (isCorrect) "Pronúncia clara e correta!" else "Tente articular com clareza: '$target'.",
                    next_recommended_lesson_id = if (!isCorrect) "reinforcement_${exercise.exercise_id}" else null,
                    srs_interval_days = if (isCorrect) 3 else 1,
                    used_fallback = true,
                    telemetry_error = telemetryError
                )
            }
        }
    }

    private fun normalize(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), "")
            .replace(Regex("\\s+"), " ")
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        val dist = levenshtein(s1, s2)
        val maxLen = max(s1.length, s2.length)
        return if (maxLen == 0) 1.0 else (1.0 - (dist.toDouble() / maxLen.toDouble())).coerceIn(0.0, 1.0)
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = Array(lhsLength + 1) { it }
        var newCost = Array(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
}
