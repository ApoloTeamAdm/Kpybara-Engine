package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE course_id = :courseId")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Query("SELECT * FROM modules WHERE course_id = :courseId ORDER BY order_index ASC")
    fun getModulesForCourse(courseId: String): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE module_id = :moduleId")
    suspend fun getModuleById(moduleId: String): ModuleEntity?

    @Query("SELECT * FROM lessons WHERE module_id = :moduleId ORDER BY order_index ASC")
    fun getLessonsForModule(moduleId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM exercises WHERE lesson_id = :lessonId")
    suspend fun getExercisesForLesson(lessonId: String): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("UPDATE lessons SET is_completed = 1, score = :score WHERE lesson_id = :lessonId")
    suspend fun markLessonCompleted(lessonId: String, score: Double)

    @Query("UPDATE courses SET title = :newTitle WHERE course_id = :courseId")
    suspend fun updateCourseTitle(courseId: String, newTitle: String)

    @Query("UPDATE exercises SET prompt = :newPrompt WHERE exercise_id = :exerciseId")
    suspend fun updateExercisePrompt(exerciseId: String, newPrompt: String)

    @Query("UPDATE courses SET is_downloaded = :isDownloaded, download_status = :status, download_progress = :progress, downloaded_bytes = :bytes WHERE course_id = :courseId")
    suspend fun updateCourseDownloadStatus(courseId: String, isDownloaded: Boolean, status: String, progress: Float, bytes: Long)

    @Query("UPDATE modules SET is_downloaded = :isDownloaded, download_status = :status, audio_assets_cached = :cachedAssets WHERE module_id = :moduleId")
    suspend fun updateModuleDownloadStatus(moduleId: String, isDownloaded: Boolean, status: String, cachedAssets: Int)
}

@Dao
interface SrsDao {
    @Query("SELECT * FROM srs_cards ORDER BY next_review_epoch ASC")
    fun getAllCards(): Flow<List<SrsCardEntity>>

    @Query("SELECT * FROM srs_cards WHERE next_review_epoch <= :currentEpoch")
    fun getCardsDueForReview(currentEpoch: Long): Flow<List<SrsCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SrsCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<SrsCardEntity>)

    @Update
    suspend fun updateCard(card: SrsCardEntity)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM student_progress WHERE course_id = :courseId")
    fun getProgress(courseId: String): Flow<StudentProgressEntity?>

    @Query("SELECT * FROM student_progress WHERE course_id = :courseId")
    suspend fun getProgressSnapshot(courseId: String): StudentProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: StudentProgressEntity)

    @Query("UPDATE student_progress SET total_xp = total_xp + :xp, last_activity_epoch = :now WHERE course_id = :courseId")
    suspend fun addXp(courseId: String, xp: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE student_progress SET lives = :lives WHERE course_id = :courseId")
    suspend fun updateLives(courseId: String, lives: Int)
}

@Dao
interface FederatedRepoDao {
    @Query("SELECT * FROM federated_repos")
    fun getAllRepos(): Flow<List<FederatedRepoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepo(repo: FederatedRepoEntity)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY created_at_epoch ASC")
    fun getAllSyncItems(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at_epoch ASC")
    fun getPendingSyncItemsFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at_epoch ASC")
    suspend fun getPendingSyncItemsList(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueuePatch(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status WHERE patch_id = :patchId")
    suspend fun updateStatus(patchId: String, status: String)

    @Query("UPDATE sync_queue SET status = 'SYNCED', synced_at_epoch = :syncedEpoch WHERE patch_id = :patchId")
    suspend fun markSynced(patchId: String, syncedEpoch: Long = System.currentTimeMillis())

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun clearSynced()
}

@Dao
interface OfflineAssetDao {
    @Query("SELECT * FROM offline_assets WHERE course_id = :courseId")
    fun getAssetsForCourse(courseId: String): Flow<List<OfflineAssetEntity>>

    @Query("SELECT * FROM offline_assets WHERE module_id = :moduleId")
    suspend fun getAssetsForModule(moduleId: String): List<OfflineAssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<OfflineAssetEntity>)

    @Query("DELETE FROM offline_assets WHERE module_id = :moduleId")
    suspend fun deleteAssetsForModule(moduleId: String)

    @Query("DELETE FROM offline_assets WHERE course_id = :courseId")
    suspend fun deleteAssetsForCourse(courseId: String)

    @Query("SELECT SUM(byte_size) FROM offline_assets WHERE is_cached = 1")
    fun getTotalCacheBytes(): Flow<Long?>
}
