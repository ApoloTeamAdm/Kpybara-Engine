package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.patch.JsonPatchEngine
import com.example.domain.model.*
import com.example.sync.SmartSyncManager
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class CourseRepository(
    private val db: AppDatabase,
    private val syncManager: SmartSyncManager? = null
) {
    private val courseDao = db.courseDao()
    private val srsDao = db.srsDao()
    private val progressDao = db.progressDao()
    private val repoDao = db.federatedRepoDao()
    private val patchEngine = JsonPatchEngine(courseDao)

    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun getModulesForCourse(courseId: String): Flow<List<ModuleEntity>> =
        courseDao.getModulesForCourse(courseId)

    fun getLessonsForModule(moduleId: String): Flow<List<LessonEntity>> =
        courseDao.getLessonsForModule(moduleId)

    fun getProgress(courseId: String): Flow<StudentProgressEntity?> =
        progressDao.getProgress(courseId)

    fun getCardsDueForReview(): Flow<List<SrsCardEntity>> =
        srsDao.getAllCards()

    fun getFederatedRepos(): Flow<List<FederatedRepoEntity>> =
        repoDao.getAllRepos()

    suspend fun getExercisesForLesson(lessonId: String): List<Exercise> {
        val entities = courseDao.getExercisesForLesson(lessonId)
        return entities.map { parseExercise(it) }
    }

    suspend fun markLessonCompleted(courseId: String, lessonId: String, score: Double) {
        val earnedXp = (score * 20).toInt().coerceAtLeast(10)
        courseDao.markLessonCompleted(lessonId, score)
        progressDao.addXp(courseId, earnedXp)

        // Gera Delta Patch RFC 6902 para sincronização offline inteligente
        val patchArr = JSONArray().apply {
            put(JSONObject().apply {
                put("op", "replace")
                put("path", "/lessons/$lessonId/is_completed")
                put("value", true)
            })
            put(JSONObject().apply {
                put("op", "replace")
                put("path", "/lessons/$lessonId/score")
                put("value", score)
            })
            put(JSONObject().apply {
                put("op", "add")
                put("path", "/progress/$courseId/xp")
                put("value", earnedXp)
            })
        }

        syncManager?.recordProgressDelta(
            courseId = courseId,
            action = "LESSON_COMPLETED: $lessonId",
            patchOperations = patchArr
        )
    }

    suspend fun recordSrsCardSyncDelta(card: SrsCardEntity) {
        val patchArr = JSONArray().apply {
            put(JSONObject().apply {
                put("op", "replace")
                put("path", "/srs_cards/${card.card_id}/repetitions")
                put("value", card.repetitions)
            })
            put(JSONObject().apply {
                put("op", "replace")
                put("path", "/srs_cards/${card.card_id}/interval_days")
                put("value", card.interval_days)
            })
            put(JSONObject().apply {
                put("op", "replace")
                put("path", "/srs_cards/${card.card_id}/ease_factor")
                put("value", card.ease_factor)
            })
            put(JSONObject().apply {
                put("op", "replace")
                put("path", "/srs_cards/${card.card_id}/next_review_epoch")
                put("value", card.next_review_epoch)
            })
        }

        syncManager?.recordProgressDelta(
            courseId = card.course_id,
            action = "SRS_REVIEWED: ${card.card_id}",
            patchOperations = patchArr
        )
    }

    suspend fun applyDeltaPatch(courseId: String, patchJson: String): Result<Int> {
        return patchEngine.applyCoursePatch(courseId, patchJson)
    }

    suspend fun installFederatedCourse(pkg: CoursePackage) {
        val entity = CourseEntity(
            course_id = pkg.course_id,
            title = pkg.title,
            description = pkg.description,
            source_language = pkg.source_language,
            target_language = pkg.target_language,
            cefr_level = pkg.cefr_level,
            version = pkg.version,
            aqsi_score = pkg.aqsi_score,
            download_url = pkg.download_url,
            author_wallet_lightning = pkg.author_wallet_lightning,
            is_downloaded = true,
            download_status = "DOWNLOADED",
            download_progress = 1.0f,
            downloaded_bytes = 1_800_000L
        )
        courseDao.insertCourse(entity)

        progressDao.saveProgress(
            StudentProgressEntity(
                course_id = pkg.course_id,
                current_lesson_id = pkg.modules.firstOrNull()?.lessons?.firstOrNull()?.lesson_id ?: "",
                streak_days = 1,
                total_xp = 50,
                lives = 5
            )
        )

        val moduleEntities = pkg.modules.mapIndexed { idx, m ->
            ModuleEntity(
                module_id = m.module_id,
                course_id = pkg.course_id,
                module_title = m.module_title,
                order_index = idx,
                is_downloaded = true,
                download_status = "DOWNLOADED",
                audio_assets_cached = 4,
                total_assets = 4
            )
        }
        courseDao.insertModules(moduleEntities)

        val lessonEntities = mutableListOf<LessonEntity>()
        val exerciseEntities = mutableListOf<ExerciseEntity>()

        pkg.modules.forEach { mod ->
            mod.lessons.forEachIndexed { lIdx, l ->
                lessonEntities.add(
                    LessonEntity(l.lesson_id, mod.module_id, l.lesson_title, lIdx, false, 0.0)
                )
                l.exercises.forEach { ex ->
                    exerciseEntities.add(
                        ExerciseEntity(
                            exercise_id = ex.exercise_id,
                            lesson_id = l.lesson_id,
                            type = ex.type.name,
                            prompt = ex.prompt,
                            payload_json = serializePayload(ex.payload),
                            srs_keywords = ex.srs_metadata?.keywords?.joinToString(",") ?: "",
                            difficulty_rating = ex.srs_metadata?.difficulty_rating ?: 2.5
                        )
                    )
                }
            }
        }

        courseDao.insertLessons(lessonEntities)
        courseDao.insertExercises(exerciseEntities)
    }

    private fun serializePayload(p: ExercisePayload): String {
        return JSONObject().apply {
            p.target_sentence?.let { put("target_sentence", it) }
            put("tokens", JSONArray(p.tokens))
            put("distractors", JSONArray(p.distractors))
            put("options", JSONArray(p.options))
            p.correct_option_index?.let { put("correct_option_index", it) }
            val pairArr = JSONArray()
            p.pairs.forEach {
                pairArr.put(JSONObject().apply {
                    put("left", it.left)
                    put("right", it.right)
                })
            }
            put("pairs", pairArr)
            p.audio_text?.let { put("audio_text", it) }
            p.explanation?.let { put("explanation", it) }
        }.toString()
    }

    private fun parseExercise(entity: ExerciseEntity): Exercise {
        val payloadObj = try {
            JSONObject(entity.payload_json)
        } catch (e: Exception) {
            JSONObject()
        }

        val tokensList = mutableListOf<String>()
        val tokensArr = payloadObj.optJSONArray("tokens")
        if (tokensArr != null) {
            for (i in 0 until tokensArr.length()) tokensList.add(tokensArr.getString(i))
        }

        val distractorsList = mutableListOf<String>()
        val distractorsArr = payloadObj.optJSONArray("distractors")
        if (distractorsArr != null) {
            for (i in 0 until distractorsArr.length()) distractorsList.add(distractorsArr.getString(i))
        }

        val optionsList = mutableListOf<String>()
        val optionsArr = payloadObj.optJSONArray("options")
        if (optionsArr != null) {
            for (i in 0 until optionsArr.length()) optionsList.add(optionsArr.getString(i))
        }

        val pairsList = mutableListOf<PairItem>()
        val pairsArr = payloadObj.optJSONArray("pairs")
        if (pairsArr != null) {
            for (i in 0 until pairsArr.length()) {
                val p = pairsArr.getJSONObject(i)
                pairsList.add(PairItem(p.optString("left"), p.optString("right")))
            }
        }

        val typeEnum = try {
            ExerciseType.valueOf(entity.type)
        } catch (e: Exception) {
            ExerciseType.MULTIPLE_CHOICE
        }

        val payload = ExercisePayload(
            target_sentence = payloadObj.optString("target_sentence").takeIf { it.isNotBlank() },
            tokens = tokensList,
            distractors = distractorsList,
            options = optionsList,
            correct_option_index = if (payloadObj.has("correct_option_index")) payloadObj.getInt("correct_option_index") else null,
            pairs = pairsList,
            audio_text = payloadObj.optString("audio_text").takeIf { it.isNotBlank() },
            explanation = payloadObj.optString("explanation").takeIf { it.isNotBlank() }
        )

        return Exercise(
            exercise_id = entity.exercise_id,
            type = typeEnum,
            prompt = entity.prompt,
            payload = payload,
            srs_metadata = SrsMetadata(
                keywords = entity.srs_keywords.split(",").filter { it.isNotBlank() },
                difficulty_rating = entity.difficulty_rating
            )
        )
    }
}
