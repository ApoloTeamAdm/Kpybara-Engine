package com.example.scripting

/**
 * Contrato estrito de retorno do motor de exercícios (EvaluationResult).
 */
data class EvaluationResult(
    val is_correct: Boolean,
    val score_ratio: Double,
    val feedback_message: String,
    val next_recommended_lesson_id: String? = null,
    val srs_interval_days: Int = 1,
    val used_fallback: Boolean = false,
    val telemetry_error: String? = null
)
