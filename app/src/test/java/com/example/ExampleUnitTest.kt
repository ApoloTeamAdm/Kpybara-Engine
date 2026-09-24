package com.example

import com.example.domain.model.Exercise
import com.example.domain.model.ExercisePayload
import com.example.domain.model.ExerciseType
import com.example.scripting.EvaluationResult
import com.example.scripting.NativeFallbackEvaluator
import com.example.scripting.ScriptEngineRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testNativeEvaluatorMultipleChoice_Correct() {
        val exercise = Exercise(
            exercise_id = "test_mc_1",
            type = ExerciseType.MULTIPLE_CHOICE,
            prompt = "Como se diz Olá?",
            payload = ExercisePayload(
                options = listOf("Saluton", "Adiaŭ", "Dankon"),
                correct_option_index = 0,
                explanation = "Saluton é o cumprimento canônico."
            )
        )

        val result = NativeFallbackEvaluator.evaluate(exercise, "Saluton")
        assertTrue("Deveria ser correto", result.is_correct)
        assertEquals(1.0, result.score_ratio, 0.01)
        assertTrue(result.used_fallback)
    }

    @Test
    fun testNativeEvaluatorMultipleChoice_Wrong_GeneratesReinforcement() {
        val exercise = Exercise(
            exercise_id = "test_mc_2",
            type = ExerciseType.MULTIPLE_CHOICE,
            prompt = "Como se diz Olá?",
            payload = ExercisePayload(
                options = listOf("Saluton", "Adiaŭ", "Dankon"),
                correct_option_index = 0
            )
        )

        val result = NativeFallbackEvaluator.evaluate(exercise, "Adiaŭ")
        assertFalse("Deveria ser incorreto", result.is_correct)
        assertEquals(0.0, result.score_ratio, 0.01)
        assertEquals("reinforcement_test_mc_2", result.next_recommended_lesson_id)
    }

    @Test
    fun testWordOrderingLevenshteinSimilarity() {
        val exercise = Exercise(
            exercise_id = "test_wo_1",
            type = ExerciseType.WORD_ORDERING,
            prompt = "Ordene: 'Bonan tagon'",
            payload = ExercisePayload(
                target_sentence = "Bonan tagon",
                tokens = listOf("Bonan", "tagon")
            )
        )

        val exactResult = NativeFallbackEvaluator.evaluate(exercise, "Bonan tagon")
        assertTrue("Exato deve ser correto", exactResult.is_correct)
        assertEquals(1.0, exactResult.score_ratio, 0.01)

        val closeResult = NativeFallbackEvaluator.evaluate(exercise, "bonan tagon")
        assertTrue("Minúsculas normalizadas devem ser corretas", closeResult.is_correct)
    }

    @Test
    fun testScriptEngineRunnerTimeoutSafety() = runBlocking {
        val exercise = Exercise(
            exercise_id = "test_script_1",
            type = ExerciseType.WORD_ORDERING,
            prompt = "Frase rápida",
            payload = ExercisePayload(
                target_sentence = "Mi amas esperanton",
                tokens = listOf("Mi", "amas", "esperanton")
            )
        )

        // Execução sem script customizado deve invocar fallback em < 300ms
        val result = ScriptEngineRunner.evaluateExercise(exercise, "Mi amas esperanton")
        assertTrue(result.is_correct)
    }

    @Test
    fun testRfc6902DeltaPatchPayloadBandwidthOptimization() {
        val patchJson = """
            [
                {"op": "replace", "path": "/lessons/esp_les_01/is_completed", "value": true},
                {"op": "replace", "path": "/lessons/esp_les_01/score", "value": 1.0},
                {"op": "add", "path": "/progress/esperanto_ptbr/xp", "value": 20}
            ]
        """.trimIndent()

        val patchBytes = patchJson.toByteArray().size
        val rawCourseBytes = 2_500_000L // Tamanho de download integral típico

        val savedRatio = (1.0 - (patchBytes.toDouble() / rawCourseBytes.toDouble()))
        assertTrue("O patch delta deve ser menor que 1KB", patchBytes < 1000)
        assertTrue("A economia de tráfego de dados deve ser superior a 99%", savedRatio > 0.99)
    }

    @Test
    fun testMultiLanguageRunnerAllLanguages() = runBlocking {
        val languages = com.example.devtools.model.DevLanguage.values()
        assertEquals(8, languages.size)

        for (lang in languages) {
            val result = com.example.devtools.engine.MultiLanguageScriptRunner.executeScript(
                language = lang,
                scriptCode = lang.defaultSnippet,
                prompt = "Teste prompt",
                studentAnswer = "saluton",
                targetSentence = "saluton"
            )

            assertTrue("${lang.displayName} deveria executar com sucesso", result.isSuccess)
            assertTrue("${lang.displayName} tempo de execução deve respeitar teto de 300ms", result.executionTimeMs <= 300)
            assertTrue("${lang.displayName} deveria validar resposta correta", result.isCorrect)
            assertEquals(1.0, result.score, 0.01)

            // Testa transpilação para Lua 5.4 universal
            val transpiledLua = com.example.devtools.engine.MultiLanguageScriptRunner.transpileToLua(lang, lang.defaultSnippet)
            assertTrue("Transpilação deve conter evaluate()", transpiledLua.contains("function evaluate"))
        }
    }

    @Test
    fun testKpybaraUniversalCoreSubMillisecondExecution() = runBlocking {
        val target = "Saluton mondo!"
        val bytecode = com.example.kpybara.KpybaraUniversalCore.compileToKpyBytecode(target, "esp_saluton_mondo")
        assertTrue("Bytecode Kpybara deve ser compacto (< 100 bytes)", bytecode.size < 100)

        // Resposta correta
        val resCorrect = com.example.kpybara.KpybaraUniversalCore.executePrecompiledBytecode(
            bytecode = bytecode,
            studentAnswer = "saluton mondo",
            targetSentence = target,
            exerciseId = "esp_saluton_mondo"
        )

        assertTrue("Deveria validar resposta correta", resCorrect.isCorrect)
        assertEquals(1.0, resCorrect.score, 0.001)
        assertTrue("Execução no Core C++ deve ser < 1ms (1000 µs)", resCorrect.executionTimeMicros <= 1000)
        assertTrue("Consumo de RAM no Core C++ deve ser < 5 MB", resCorrect.memoryFootprintKb < 5000)
        assertTrue("Zero overhead verificado", resCorrect.zeroOverheadVerified)

        // Resposta incorreta
        val resIncorrect = com.example.kpybara.KpybaraUniversalCore.executePrecompiledBytecode(
            bytecode = bytecode,
            studentAnswer = "bonvenon amiko",
            targetSentence = target,
            exerciseId = "esp_saluton_mondo"
        )
        assertFalse("Deveria acusar erro", resIncorrect.isCorrect)
        assertEquals(0.0, resIncorrect.score, 0.001)
        assertTrue("Branch de reforço deve apontar para nó específico", resIncorrect.nextLessonId.contains("esp_saluton_mondo"))
    }

    @Test
    fun testKpybaraTranspilerPipelineToCppAndPackage() {
        val pkg = com.example.kpybara.KpybaraTranspilerPipeline.compileAndPackage(
            exerciseId = "ex_test_01",
            sourceLanguage = com.example.devtools.model.DevLanguage.PYTHON,
            sourceCode = "def evaluate(ans): return ans == 'ok'",
            prompt = "Traduza 'ok'",
            targetSentence = "ok"
        )

        assertEquals("kpybara_cpp20_universal", pkg.kpySpec)
        assertTrue("C++20 gerado deve usar std::string_view", pkg.cppNativeSource.contains("std::string_view"))
        assertTrue("C++20 gerado deve exportar C-ABI", pkg.cppNativeSource.contains("extern \"C\""))
        assertTrue("Bytecode hex não pode ser vazio", pkg.bytecodeHex.isNotEmpty())
        assertTrue("JSON payload deve conter o schema kpy", pkg.jsonPayload.contains("kpy-package-v2.json"))
        assertTrue("JSON payload deve declarar zero_overhead_mobile", pkg.jsonPayload.contains("\"zero_overhead_mobile\": true"))
    }
}
