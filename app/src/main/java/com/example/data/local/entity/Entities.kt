package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val course_id: String,
    val title: String,
    val description: String,
    val source_language: String,
    val target_language: String,
    val cefr_level: String,
    val version: String,
    val aqsi_score: Double,
    val download_url: String,
    val author_wallet_lightning: String?,
    val is_downloaded: Boolean = true,
    val download_status: String = "DOWNLOADED", // NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED
    val download_progress: Float = 1.0f,
    val downloaded_bytes: Long = 2_450_000L
)

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val module_id: String,
    val course_id: String,
    val module_title: String,
    val order_index: Int,
    val is_downloaded: Boolean = true,
    val download_status: String = "DOWNLOADED", // NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED
    val audio_assets_cached: Int = 6,
    val total_assets: Int = 6
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val lesson_id: String,
    val module_id: String,
    val lesson_title: String,
    val order_index: Int,
    val is_completed: Boolean = false,
    val score: Double = 0.0
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val exercise_id: String,
    val lesson_id: String,
    val type: String,
    val prompt: String,
    val payload_json: String,
    val srs_keywords: String = "",
    val difficulty_rating: Double = 2.5
)

@Entity(tableName = "srs_cards")
data class SrsCardEntity(
    @PrimaryKey val card_id: String,
    val exercise_id: String,
    val course_id: String,
    val prompt: String,
    val target_answer: String,
    val keywords: String,
    val repetitions: Int = 0,
    val interval_days: Int = 1,
    val ease_factor: Double = 2.5,
    val next_review_epoch: Long = System.currentTimeMillis(),
    val last_review_epoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_progress")
data class StudentProgressEntity(
    @PrimaryKey val course_id: String,
    val current_lesson_id: String,
    val streak_days: Int = 1,
    val total_xp: Int = 120,
    val lives: Int = 5,
    val last_activity_epoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "federated_repos")
data class FederatedRepoEntity(
    @PrimaryKey val repo_url: String,
    val repo_name: String,
    val maintainer: String,
    val version: String,
    val last_synced_epoch: Long = System.currentTimeMillis()
)

/**
 * Fila de Sincronização Inteligente Offline (Outbox Pattern).
 * Registra cada progresso efetuado offline como um Delta Patch RFC 6902
 * para transmissão otimizada quando a conectividade for restabelecida.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val patch_id: String,
    val course_id: String,
    val action: String,
    val delta_patch_json: String,
    val status: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    val retry_count: Int = 0,
    val created_at_epoch: Long = System.currentTimeMillis(),
    val synced_at_epoch: Long? = null
)

/**
 * Registro de Assets Offline (Áudios TTS pré-gerados, payloads e imagens).
 */
@Entity(tableName = "offline_assets")
data class OfflineAssetEntity(
    @PrimaryKey val asset_id: String,
    val course_id: String,
    val module_id: String,
    val asset_type: String, // AUDIO, TEXT_PAYLOAD, IMAGE
    val local_path: String,
    val byte_size: Long,
    val is_cached: Boolean = true
)
