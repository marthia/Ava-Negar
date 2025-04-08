package me.marthia.avanegar.presentation.common

import android.content.Context
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import java.io.IOException

class SpeechRecognitionManager(private val context: Context) : RecognitionListener {

    // 🎯 Define callback properties for each listener method
    private var onModelReady: () -> Unit = {}
    private var onResult: (hypothesis: String) -> Unit = { _ -> }
    private var onFinalResult: (hypothesis: String) -> Unit = { _ -> }
    private var onPartialResult: (hypothesis: String) -> Unit = { _ -> }
    private var onError: (errorMessage: String) -> Unit = { _ -> }
    private var onTimeout: () -> Unit = {}

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null

    // Initialize with default callbacks (silent/empty)
    init {
        LibVosk.setLogLevel(LogLevel.INFO)
    }

    // 🔥 New methods to set callbacks (replaces `setListener`)
    fun onModelReady(callback: () -> Unit) {
        onModelReady = callback
    }

    fun onResult(callback: (String) -> Unit) {
        onResult = callback
    }

    fun onFinalResult(callback: (String) -> Unit) {
        onFinalResult = callback
    }

    fun onPartialResult(callback: (String) -> Unit) {
        onPartialResult = callback
    }

    fun onError(callback: (String) -> Unit) {
        onError = callback
    }

    fun onTimeout(callback: () -> Unit) {
        onTimeout = callback
    }

    // Update RecognitionListener overrides to call lambdas
    override fun onResult(hypothesis: String) {
        onResult(hypothesis)
    }

    override fun onFinalResult(hypothesis: String) {
        speechStreamService = null
        onFinalResult(hypothesis)
    }

    override fun onPartialResult(hypothesis: String) {
        onPartialResult(hypothesis)
    }

    override fun onError(e: Exception) {
        onError(e.message ?: "Unknown error")
    }

    override fun onTimeout() {
        onTimeout()
    }

    fun initModel() {
//        StorageService.unpack(context, "model-fa-ir", "model",
//            { model ->
//                this.model = model
//                listener?.onModelReady()
//            },
//            { exception ->
//                listener?.onError("Failed to unpack the model: ${exception.message}")
//            }
//        )
    }

    fun isFileRecognitionActive(): Boolean = speechStreamService != null

    fun isMicrophoneRecognitionActive(): Boolean = speechService != null

    fun startFileRecognition() {
        val currentModel = model ?: run {
            onError("Model not initialized")
            return
        }

        try {
            val rec = Recognizer(currentModel, 44100f)

            val ais = context.assets.open("test.wav")
            if (ais.skip(44) != 44L) throw IOException("File too short")

            speechStreamService = SpeechStreamService(rec, ais, 44100f)
            speechStreamService?.start(this)
        } catch (e: IOException) {
            onError(e.message ?: "Unknown error during file recognition")
        }
    }

    fun stopFileRecognition() {
        speechStreamService?.stop()
        speechStreamService = null
    }

    fun startMicrophoneRecognition() {
        val currentModel = model ?: run {
            onError("Model not initialized")
            return
        }

        try {
            val rec = Recognizer(currentModel, 44100.0f)
            speechService = SpeechService(rec, 44100.0f)
            speechService?.startListening(this)
        } catch (e: IOException) {
            onError(e.message ?: "Unknown error during microphone recognition")
        }
    }

    fun stopMicrophoneRecognition() {
        speechService?.stop()
        speechService = null
    }

    fun setPause(paused: Boolean) {
        speechService?.setPause(paused)
    }

    fun shutdown() {
        speechService?.run {
            stop()
            shutdown()
        }
        speechService = null

        speechStreamService?.stop()
        speechStreamService = null
    }
}
