package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiDiagnosticsService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getPedagogicalDiagnostic(
        exerciseType: String,
        prompt: String,
        expectedAnswer: String,
        studentAnswer: String,
        sourceLanguage: String = "pt-BR",
        targetLanguage: String = "eo"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalPedagogicalExplanation(exerciseType, expectedAnswer, studentAnswer, targetLanguage)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val promptText = """
                Você é o motor pedagógico CourseEngine.
                Um estudante que fala $sourceLanguage errou um exercício de $targetLanguage.
                Tipo: $exerciseType
                Enunciado: '$prompt'
                Resposta esperada: '$expectedAnswer'
                Resposta dada pelo aluno: '$studentAnswer'

                Gere exatamente UMA frase pedagógica empática e direta em $sourceLanguage explicando o motivo do erro (concordância, acusativo, falso amigo ou ordem dos termos) e como corrigir.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", promptText) })
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 120)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val responseJson = JSONObject(body)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            } else {
                Log.w("GeminiDiagnostics", "HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Log.w("GeminiDiagnostics", "Erro ao contatar Gemini API: ${e.message}")
        }

        // Fallback local se a API falhar ou estiver offline
        getLocalPedagogicalExplanation(exerciseType, expectedAnswer, studentAnswer, targetLanguage)
    }

    private fun getLocalPedagogicalExplanation(
        exerciseType: String,
        expectedAnswer: String,
        studentAnswer: String,
        targetLanguage: String
    ): String {
        val cleanExpected = expectedAnswer.trim().lowercase()
        val cleanStudent = studentAnswer.trim().lowercase()

        return when {
            cleanExpected.endsWith("n") && !cleanStudent.endsWith("n") ->
                "Lembre-se: em Esperanto o objeto direto (quem recebe a ação) deve obrigatoriamente receber a terminação '-n' do caso acusativo."

            cleanExpected.endsWith("j") && !cleanStudent.endsWith("j") ->
                "Atenção ao plural: em Esperanto os substantivos e adjetivos no plural recebem o sufixo '-j'."

            exerciseType == "WORD_ORDERING" ->
                "Verifique a ordem dos constituintes da frase: a estrutura padrão correta é '$expectedAnswer'."

            cleanStudent.isBlank() ->
                "Não deixe em branco: tente formular a frase canônica esperada: '$expectedAnswer'."

            else ->
                "Atenção à terminação: a forma padrão esperada é '$expectedAnswer' e não '$studentAnswer'."
        }
    }
}
