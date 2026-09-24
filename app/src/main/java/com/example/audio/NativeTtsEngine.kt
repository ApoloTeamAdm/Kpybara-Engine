package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class NativeTtsEngine(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.ENGLISH
            tts?.setSpeechRate(0.9f)
            tts?.setPitch(1.0f)
        } else {
            Log.e("NativeTtsEngine", "TTS initialization failed: $status")
        }
    }

    fun speak(text: String, langCode: String = "eo") {
        if (!isInitialized || text.isBlank()) return

        val locale = when (langCode.lowercase()) {
            "eo" -> Locale.forLanguageTag("eo")
            "pt", "pt-br" -> Locale.forLanguageTag("pt-BR")
            "es" -> Locale.forLanguageTag("es-ES")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            else -> Locale.ENGLISH
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback para inglês ou padrão do dispositivo
                tts?.language = Locale.ENGLISH
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CourseEngine_TTS_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.w("NativeTtsEngine", "Erro ao executar speak: ${e.message}")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
