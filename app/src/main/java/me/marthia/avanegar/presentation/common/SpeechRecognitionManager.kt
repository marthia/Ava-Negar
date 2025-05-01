package me.marthia.avanegar.presentation.common

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import me.marthia.avanegar.domain.RecognitionEvent
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class SpeechRecognitionManager(
    private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : RecognitionListener {

    private val defaultPath = "${context.getExternalFilesDir(null)}/model/"
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
    private var currentLanguageModel: LanguageModel? = null

    private val managerScope = CoroutineScope(defaultDispatcher + SupervisorJob())

    private val _recognitionEventFlow =
        MutableSharedFlow<RecognitionEvent>(extraBufferCapacity = 64)
    val recognitionEventFlow = _recognitionEventFlow.asSharedFlow()

    // Initialize with default callbacks (silent/empty)
    init {
        LibVosk.setLogLevel(LogLevel.INFO)
    }

    // Update RecognitionListener overrides to call lambdas
    override fun onResult(hypothesis: String) {
        managerScope.launch {
            _recognitionEventFlow.emit(RecognitionEvent.Result(hypothesis))
        }
    }

    override fun onFinalResult(hypothesis: String) {
        speechStreamService = null
        managerScope.launch {
            _recognitionEventFlow.emit(RecognitionEvent.FinalResult(hypothesis))
        }
    }

    override fun onPartialResult(hypothesis: String) {
        managerScope.launch {
            _recognitionEventFlow.emit(RecognitionEvent.PartialResult(hypothesis))
        }
    }

    override fun onError(e: Exception) {
        managerScope.launch {
            _recognitionEventFlow.emit(RecognitionEvent.Error(e.message ?: "Unknown error"))
        }
    }

    override fun onTimeout() {
        managerScope.launch {
            _recognitionEventFlow.emit(RecognitionEvent.Timeout)
        }
    }

    fun initModel(speechModel: LanguageModel) {

//        if (this.model != null || currentLanguageModel != speechModel) {
//            Log.d("SpeechManager", "Model Already Active")
//            return
//        }

        kotlin.runCatching {
            val model = Model("$defaultPath${speechModel.title}")
            this.model = model
        }.onSuccess {
            managerScope.launch {
                _recognitionEventFlow.emit(RecognitionEvent.ModelReady(model!!))
            }
        }.onFailure { exception ->
            managerScope.launch {
                _recognitionEventFlow.emit(RecognitionEvent.Error("Failed to unpack the model: ${exception.message}"))
            }
        }
    }

    fun isFileRecognitionActive(): Boolean = speechStreamService != null

    fun startFileRecognition(audioPath: String) {
        val currentModel = model ?: run {
            managerScope.launch {
                _recognitionEventFlow.emit(RecognitionEvent.Error("Model not initialized"))
            }
            return
        }

        try {
            val rec = Recognizer(currentModel, 16000f)

//            val ais = context.contentResolver.openInputStream(audioPath.toUri())
            val audiFile = File(audioPath)
            val ais = FileInputStream(audiFile)
            if (ais?.skip(44) != 44L) throw IOException("File too short")

            speechStreamService = SpeechStreamService(rec, ais, 16000f)
            speechStreamService?.start(this)
        } catch (e: IOException) {
            managerScope.launch {
                _recognitionEventFlow.emit(
                    RecognitionEvent.Error(
                        e.message ?: "Unknown error during file recognition"
                    )
                )
            }
        }
    }

    fun stopFileRecognition() {
        speechStreamService?.stop()
        speechStreamService = null
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
        managerScope.cancel()
    }

    fun isModelAvailable(name: String): Boolean {
        val isDirectory = File("$defaultPath$name").exists()
        Log.i("Manager", if (isDirectory) "file is already downloaded" else "directory not found")
        return isDirectory
    }
}
