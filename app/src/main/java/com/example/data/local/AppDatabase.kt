package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Database(
    entities = [
        CourseEntity::class,
        ModuleEntity::class,
        LessonEntity::class,
        ExerciseEntity::class,
        SrsCardEntity::class,
        StudentProgressEntity::class,
        FederatedRepoEntity::class,
        SyncQueueEntity::class,
        OfflineAssetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun srsDao(): SrsDao
    abstract fun progressDao(): ProgressDao
    abstract fun federatedRepoDao(): FederatedRepoDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun offlineAssetDao(): OfflineAssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "course_engine.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialCourses(database)
                    }
                }
            }
        }

        suspend fun populateInitialCourses(database: AppDatabase) {
            val courseDao = database.courseDao()
            val srsDao = database.srsDao()
            val progressDao = database.progressDao()
            val repoDao = database.federatedRepoDao()
            val assetDao = database.offlineAssetDao()

            // 1. Repositório Federado Inicial
            repoDao.insertRepo(
                FederatedRepoEntity(
                    repo_url = "https://raw.githubusercontent.com/federated-courses/index.json",
                    repo_name = "Cursos Comunitários de Idiomas Raros",
                    maintainer = "Comunidade OpenSource",
                    version = "1.2.0"
                )
            )

            // 2. Curso de Esperanto Prático (Inicialmente Baixado para uso 100% Offline)
            val espCourse = CourseEntity(
                course_id = "esperanto_ptbr",
                title = "Esperanto Prático para Brasileiros",
                description = "Aprenda a língua internacional neutra com metodologia moderna e acelerada.",
                source_language = "pt-BR",
                target_language = "eo",
                cefr_level = "A1",
                version = "2.1.0",
                aqsi_score = 94.5,
                download_url = "https://raw.githubusercontent.com/user/repo/main/esperanto_ptbr.json",
                author_wallet_lightning = "author@getalby.com",
                is_downloaded = true,
                download_status = "DOWNLOADED",
                download_progress = 1.0f,
                downloaded_bytes = 2_450_000L
            )
            courseDao.insertCourse(espCourse)

            // Progresso do estudante
            progressDao.saveProgress(
                StudentProgressEntity(
                    course_id = "esperanto_ptbr",
                    current_lesson_id = "esp_les_01",
                    streak_days = 7,
                    total_xp = 340,
                    lives = 5
                )
            )

            // Módulos
            val espModules = listOf(
                ModuleEntity(
                    module_id = "esp_mod_01",
                    course_id = "esperanto_ptbr",
                    module_title = "Módulo 1: Fundamentos & Saudações",
                    order_index = 0,
                    is_downloaded = true,
                    download_status = "DOWNLOADED",
                    audio_assets_cached = 6,
                    total_assets = 6
                ),
                ModuleEntity(
                    module_id = "esp_mod_02",
                    course_id = "esperanto_ptbr",
                    module_title = "Módulo 2: O Acusativo & Construção Frasal",
                    order_index = 1,
                    is_downloaded = true,
                    download_status = "DOWNLOADED",
                    audio_assets_cached = 4,
                    total_assets = 4
                )
            )
            courseDao.insertModules(espModules)

            // Lições
            val espLessons = listOf(
                LessonEntity(
                    lesson_id = "esp_les_01",
                    module_id = "esp_mod_01",
                    lesson_title = "Primeiras Palavras e Saudações",
                    order_index = 0,
                    is_completed = false
                ),
                LessonEntity(
                    lesson_id = "esp_les_02",
                    module_id = "esp_mod_01",
                    lesson_title = "Apresentando-se no Mundo",
                    order_index = 1,
                    is_completed = false
                ),
                LessonEntity(
                    lesson_id = "esp_acusativo_reforco_02",
                    module_id = "esp_mod_02",
                    lesson_title = "Nó de Reforço: Acusativo (-n)",
                    order_index = 0,
                    is_completed = false
                )
            )
            courseDao.insertLessons(espLessons)

            // Exercícios da Lição 1
            val ex1Payload = JSONObject().apply {
                put("options", JSONArray(listOf("Saluton", "Bonan tagon", "Dankon")))
                put("correct_option_index", 0)
                put("explanation", "Saluton é o cumprimento universal em Esperanto (Olá/Oi).")
                put("audio_text", "Saluton")
            }

            val ex2Payload = JSONObject().apply {
                put("target_sentence", "Bonan tagon")
                put("tokens", JSONArray(listOf("Bonan", "tagon")))
                put("distractors", JSONArray(listOf("nokton", "dankon")))
                put("explanation", "Bonan tagon significa Bom dia e leva -n acusativo.")
                put("audio_text", "Bonan tagon")
            }

            val ex3Payload = JSONObject().apply {
                val pairsArr = JSONArray().apply {
                    put(JSONObject().apply { put("left", "Saluton"); put("right", "Olá") })
                    put(JSONObject().apply { put("left", "Dankon"); put("right", "Obrigado") })
                    put(JSONObject().apply { put("left", "Jes"); put("right", "Sim") })
                    put(JSONObject().apply { put("left", "Ne"); put("right", "Não") })
                }
                put("pairs", pairsArr)
                put("explanation", "Correspondências léxicas fundamentais de Esperanto.")
            }

            val ex4Payload = JSONObject().apply {
                put("target_sentence", "Bonan")
                put("tokens", JSONArray(listOf("Bonan", "Malbonan", "Nokton")))
                put("distractors", JSONArray(listOf("Grandan")))
                put("explanation", "Complete: [___] tagon! -> Bonan tagon!")
                put("audio_text", "Bonan tagon")
            }

            val ex5Payload = JSONObject().apply {
                put("target_sentence", "Mi estas feliĉa")
                put("audio_text", "Mi estas feliĉa")
                put("options", JSONArray(listOf("Mi estas feliĉa", "Li estas granda", "Ni estas pretaj")))
                put("correct_option_index", 0)
                put("explanation", "Escute atentamente a pronúncia: 'Mi estas feliĉa' (Eu sou feliz).")
            }

            val ex6Payload = JSONObject().apply {
                put("target_sentence", "Dankon multe")
                put("audio_text", "Dankon multe")
                put("explanation", "Pronuncie claramente: Dankon multe (Muito obrigado).")
            }

            // Exercício de Reforço do Acusativo
            val exReforcoPayload = JSONObject().apply {
                put("target_sentence", "La birdo trinkas akvon")
                put("tokens", JSONArray(listOf("La", "birdo", "trinkas", "akvon")))
                put("distractors", JSONArray(listOf("akvo", "hundo")))
                put("explanation", "Em esperanto o objeto direto (o que é bebido) exige o acusativo '-n': akvon.")
                put("audio_text", "La birdo trinkas akvon")
            }

            val exercises = listOf(
                ExerciseEntity("esp_ex_1", "esp_les_01", "MULTIPLE_CHOICE", "Como se diz 'Olá' em Esperanto?", ex1Payload.toString(), "saudação,básico", 1.5),
                ExerciseEntity("esp_ex_2", "esp_les_01", "WORD_ORDERING", "Ordene as palavras para: 'Bom dia'", ex2Payload.toString(), "saudação,acusativo", 2.0),
                ExerciseEntity("esp_ex_3", "esp_les_01", "PAIR_MATCHING", "Conecte os pares corretos de vocabulário:", ex3Payload.toString(), "pares,básico", 1.8),
                ExerciseEntity("esp_ex_4", "esp_les_01", "FILL_IN_BLANK", "Complete a saudação: '___ tagon!'", ex4Payload.toString(), "saudação,adjetivo", 2.0),
                ExerciseEntity("esp_ex_5", "esp_les_01", "LISTENING_COMPREHENSION", "Ouça o áudio e selecione a frase ouvida:", ex5Payload.toString(), "audição,compreensão", 2.5),
                ExerciseEntity("esp_ex_6", "esp_les_01", "PRONUNCIATION_SPEECH", "Fale a frase em voz alta: 'Dankon multe!'", ex6Payload.toString(), "pronúncia,fala", 2.2),
                ExerciseEntity("esp_ex_ref_1", "esp_acusativo_reforco_02", "WORD_ORDERING", "Monte a frase com o acusativo: 'O pássaro bebe água'", exReforcoPayload.toString(), "acusativo,reforço", 2.8)
            )
            courseDao.insertExercises(exercises)

            // Assets Offline de Áudio e Payload Pré-cacheados
            val offlineAssets = listOf(
                OfflineAssetEntity("asset_saluton", "esperanto_ptbr", "esp_mod_01", "AUDIO", "assets/audio/saluton.mp3", 45000L, true),
                OfflineAssetEntity("asset_bonan_tagon", "esperanto_ptbr", "esp_mod_01", "AUDIO", "assets/audio/bonan_tagon.mp3", 62000L, true),
                OfflineAssetEntity("asset_dankon", "esperanto_ptbr", "esp_mod_01", "AUDIO", "assets/audio/dankon.mp3", 38000L, true),
                OfflineAssetEntity("asset_akvon", "esperanto_ptbr", "esp_mod_02", "AUDIO", "assets/audio/akvon.mp3", 55000L, true),
                OfflineAssetEntity("asset_text_corpus", "esperanto_ptbr", "esp_mod_01", "TEXT_PAYLOAD", "assets/corpus/vocab_a1.json", 120000L, true)
            )
            assetDao.insertAssets(offlineAssets)

            // Cartões de SRS Iniciais
            val srsCards = listOf(
                SrsCardEntity(
                    card_id = "srs_1",
                    exercise_id = "esp_ex_1",
                    course_id = "esperanto_ptbr",
                    prompt = "Como se diz 'Olá' em Esperanto?",
                    target_answer = "Saluton",
                    keywords = "saudação,eo",
                    repetitions = 1,
                    interval_days = 1,
                    ease_factor = 2.5,
                    next_review_epoch = System.currentTimeMillis() - 1000L
                ),
                SrsCardEntity(
                    card_id = "srs_2",
                    exercise_id = "esp_ex_2",
                    course_id = "esperanto_ptbr",
                    prompt = "Traduza 'Bom dia' em Esperanto:",
                    target_answer = "Bonan tagon",
                    keywords = "acusativo,saudação",
                    repetitions = 2,
                    interval_days = 2,
                    ease_factor = 2.4,
                    next_review_epoch = System.currentTimeMillis() - 500L
                ),
                SrsCardEntity(
                    card_id = "srs_3",
                    exercise_id = "esp_ex_3",
                    course_id = "esperanto_ptbr",
                    prompt = "Como se diz 'Obrigado' em Esperanto?",
                    target_answer = "Dankon",
                    keywords = "cortesia,eo",
                    repetitions = 0,
                    interval_days = 1,
                    ease_factor = 2.5,
                    next_review_epoch = System.currentTimeMillis() + 86400000L
                )
            )
            srsDao.insertCards(srsCards)
        }
    }
}
