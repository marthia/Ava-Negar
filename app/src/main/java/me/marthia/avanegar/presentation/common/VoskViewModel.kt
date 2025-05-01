package me.marthia.avanegar.presentation.common

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import me.marthia.avanegar.domain.RecognitionEvent
import me.marthia.avanegar.domain.RecognitionState
import me.marthia.avanegar.domain.mapVoskResultToTranscription
import me.marthia.avanegar.presentation.common.DownloadModelWorker.Companion.DATA_INFO_KEY
import me.marthia.avanegar.presentation.utils.toJson
import javax.inject.Inject

@HiltViewModel
class VoskViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val workManager: WorkManager = WorkManager.getInstance(context)

    var recognitionState by mutableStateOf(RecognitionState.Starting)
        private set

    var transcription by mutableStateOf("")
        private set


    var errorMsg by mutableStateOf("")
        private set

    var downloadState by mutableStateOf(false)
        private set

    private val fileUtils = FileUtils(context)

    private var speechManager: SpeechRecognitionManager = SpeechRecognitionManager(context).apply {
        viewModelScope.launch {
            recognitionEventFlow.collect { event ->
                when (event) {
                    is RecognitionEvent.ModelReady -> {
                        Log.i("RecognitionState", "onModelReady")
                        // onReadyUi()
                    }

                    is RecognitionEvent.Result -> {
                        Log.i("RecognitionState", "onResult")
                        appendResult(event.hypothesis)
                    }

                    is RecognitionEvent.PartialResult -> {
                        Log.i("RecognitionState", "onPartialResult")
                        appendResult(event.hypothesis)
                    }

                    is RecognitionEvent.FinalResult -> {

                        Log.i("RecognitionState", "onFinalResult")
                        appendResult(event.hypothesis)
                        // onDoneUi()
                    }

                    is RecognitionEvent.Error -> {
                        setError(event.errorMessage)
                    }

                    is RecognitionEvent.Timeout -> {
//                        onDoneUi()
                    }
                }
            }
        }
    }


    init {
        initModel(Pref.currentModel)
    }


    private fun appendResult(text: String) {
        Log.i("RecognitionState", "appendResult--> $text")
        val rs = mapVoskResultToTranscription(text)
        val currentState = transcription

        val finalResult = buildString {
            if (currentState.isNotBlank()) {
                append(currentState)
                append(" ")
            }

            append(rs.text)
        }

        transcription = finalResult.trim()
    }

    private fun setError(errorMessage: String) {
        Log.e("RecognitionState", "setError--> $errorMessage")

        recognitionState = RecognitionState.Error
        errorMsg = errorMessage
    }

    fun toggleFileRecognition(audio: String) {
        viewModelScope.launch {

            val audioUri = audio.toUri()
            val wavFilePath = AudioConverter.getTempWavFilePath(context, audioUri)

            // Convert the audio
            if (AudioConverter.convertToWav(
                    context = context,
                    inputPath = audioUri,
                    outputPath = wavFilePath
                )
            ) {
                speechManager.startFileRecognition(wavFilePath)
            }
        }
    }

    fun setPause(isPaused: Boolean) {
        speechManager.setPause(isPaused)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }

    fun initModel(model: LanguageModel) {
        // check if model is already downloaded
        val isPresent = speechManager.isModelAvailable(model.name)

        // download model
        if (!isPresent)
            onDownload(model.path) {
                speechManager.initModel(model)
            }
        else {
            // init model
            speechManager.initModel(model)
        }
    }

    private fun onDownload(media: String, predicate: () -> Unit) {
        downloadState = true
        viewModelScope.launch {
            val data = workDataOf(DATA_INFO_KEY to media.toJson())

            val id = DownloadModelWorker.enqueue(context, data)

            workManager.getWorkInfoByIdFlow(id).collect { info ->
                viewModelScope.launch {
                    if (info?.state == WorkInfo.State.SUCCEEDED) {
                        downloadState = false
                        predicate()
                    }
                }
            }
        }
    }
}