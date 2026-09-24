package com.example.domain.model

enum class ExerciseType {
    WORD_ORDERING,
    MULTIPLE_CHOICE,
    PAIR_MATCHING,
    FILL_IN_BLANK,
    LISTENING_COMPREHENSION,
    PRONUNCIATION_SPEECH
}

data class PairItem(
    val left: String,
    val right: String
)

data class ExercisePayload(
    val target_sentence: String? = null,
    val tokens: List<String> = emptyList(),
    val distractors: List<String> = emptyList(),
    val options: List<String> = emptyList(),
    val correct_option_index: Int? = null,
    val pairs: List<PairItem> = emptyList(),
    val audio_text: String? = null,
    val explanation: String? = null
)

data class SrsMetadata(
    val keywords: List<String> = emptyList(),
    val difficulty_rating: Double = 2.5
)

data class Exercise(
    val exercise_id: String,
    val type: ExerciseType,
    val prompt: String,
    val payload: ExercisePayload,
    val srs_metadata: SrsMetadata? = null
)

data class Lesson(
    val lesson_id: String,
    val lesson_title: String,
    val exercises: List<Exercise>,
    val is_completed: Boolean = false,
    val score: Double = 0.0
)

data class CourseModule(
    val module_id: String,
    val module_title: String,
    val lessons: List<Lesson>
)

data class CoursePackage(
    val course_id: String,
    val title: String,
    val description: String = "",
    val source_language: String = "pt-BR",
    val target_language: String = "eo",
    val cefr_level: String = "A1",
    val version: String = "1.0.0",
    val aqsi_score: Double = 94.5,
    val download_url: String = "",
    val author_wallet_lightning: String? = null,
    val is_downloaded: Boolean = true,
    val modules: List<CourseModule> = emptyList()
)

data class FederatedRepository(
    val repository_name: String,
    val maintainer: String,
    val version: String,
    val repository_url: String,
    val courses: List<CoursePackage> = emptyList()
)

data class AQSIScore(
    val cefr_alignment: Double,
    val distractor_quality: Double,
    val readability: Double,
    val accessibility: Double,
    val overall: Double,
    val status: String,
    val audit_notes: List<String>
)
