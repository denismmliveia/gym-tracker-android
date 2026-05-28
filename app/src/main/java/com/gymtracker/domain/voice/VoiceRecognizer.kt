package com.gymtracker.domain.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.MainThread
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class VoiceResult {
    data class Success(val text: String) : VoiceResult()
    data class Error(val message: String) : VoiceResult()
}

class VoiceRecognizer(private val context: Context) {

    @MainThread
    fun listen(): Flow<VoiceResult> = callbackFlow {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                trySend(VoiceResult.Success(text))
                close()
            }

            override fun onError(error: Int) {
                trySend(VoiceResult.Error("Error de reconocimiento: código $error"))
                close()
            }

            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partial: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })

        recognizer.startListening(intent)
        awaitClose { recognizer.destroy() }
    }
}
