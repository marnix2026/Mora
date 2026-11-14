package com.mora.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.mora.app.ui.Lang

class SpeechInput(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    fun available(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    private fun tag(lang: Lang) = when (lang) {
        Lang.FR -> "fr-FR"
        Lang.EN -> "en-US"
        Lang.MG -> "mg-MG"
    }

    fun start(
        lang: Lang,
        onListening: () -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()
        val r = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) { onListening() }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(t: Int, p: Bundle?) {}
            override fun onPartialResults(p: Bundle?) {}
            override fun onError(error: Int) {
                onError(
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "nomatch"
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "network"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "permission"
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
                        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "language"
                        else -> "error$error"
                    }
                )
            }
            override fun onResults(results: Bundle?) {
                val t = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (t.isNullOrBlank()) onError("nomatch") else onResult(t)
            }
        })
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag(lang))
        }
        r.startListening(i)
    }

    fun stop() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }
}
