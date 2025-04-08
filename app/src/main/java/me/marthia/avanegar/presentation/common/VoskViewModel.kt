package me.marthia.avanegar.presentation.common

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import me.marthia.avanegar.domain.RecognitionState
import me.marthia.avanegar.domain.mapVoskResultToTranscription
import javax.inject.Inject

@HiltViewModel
class VoskViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    var recognitionState by mutableStateOf(RecognitionState.Starting)
        private set


    var fileButtonEnabled by mutableStateOf(false)
        private set


    var micButtonEnabled by mutableStateOf(false)
        private set


    var pauseEnabled by mutableStateOf(false)
        private set


    var isPaused by mutableStateOf(false)
        private set

    var transcription by mutableStateOf("")
        private set

    var errorMsg by mutableStateOf("")
        private set


    private var speechManager: SpeechRecognitionManager = SpeechRecognitionManager(context).apply {

        onModelReady {
            Log.i("RecognitionState", "onModelReady")
            onReadyUi()
        }

        onResult { hypothesis ->
            Log.i("RecognitionState", "onResult")
            appendResult(hypothesis)
        }

        onFinalResult { hypothesis ->
            Log.i("RecognitionState", "onFinalResult")
            appendResult(hypothesis)
            onDoneUi()
        }

        onPartialResult { hypothesis ->
            Log.i("RecognitionState", "onPartialResult")
            appendResult(hypothesis)
        }

        onError { errorMessage ->
            setError(errorMessage)
        }

        onTimeout {
            onDoneUi()
        }
    }

    private fun onReadyUi() {
        recognitionState = RecognitionState.Ready
        pauseEnabled = false
        isPaused = false
    }

    private fun onDoneUi() {
        recognitionState = RecognitionState.Done
        fileButtonEnabled = true
        micButtonEnabled = true
    }

    private fun onFileUi() {
        recognitionState = RecognitionState.File
        fileButtonEnabled = true
    }

    private fun onMicrophoneUi() {
        recognitionState = RecognitionState.Microphone
        micButtonEnabled = true
        pauseEnabled = true
    }


    /*fun updateState(state: RecognitionState) {
        _uiState.update { currentState ->
            Log.i("RecognitionState", "Current State--> $currentState")
            when (state) {
                RecognitionState.Starting -> currentState.copy(
                    recognitionState = state,
                    fileButtonEnabled = false,
                    micButtonEnabled = false,
                    pauseEnabled = false,
                    isPaused = false
                )

                RecognitionState.Ready -> currentState.copy(
                    recognitionState = state,
                    fileButtonEnabled = true,
                    micButtonEnabled = true,
                    pauseEnabled = false,
                    isPaused = false
                )

                RecognitionState.File -> currentState.copy(
                    recognitionState = state,
                    fileButtonEnabled = true,
                    micButtonEnabled = false,
                    pauseEnabled = false,
                    isPaused = false
                )

                RecognitionState.Microphone -> currentState.copy(
                    recognitionState = state,
                    fileButtonEnabled = false,
                    micButtonEnabled = true,
                    pauseEnabled = true,
                    isPaused = false
                )

                RecognitionState.Done -> currentState.copy(
                    recognitionState = state,
                    fileButtonEnabled = true,
                    micButtonEnabled = true,
                    pauseEnabled = false,
                    isPaused = false
                )

                RecognitionState.Error -> currentState.copy(
                    recognitionState = state,
                    fileButtonEnabled = false,
                    micButtonEnabled = false,
                    pauseEnabled = false,
                    isPaused = false
                )
            }
        }
    }*/

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
        fileButtonEnabled = false
        micButtonEnabled = false
        pauseEnabled = false
    }

    fun toggleFileRecognition() {
        if (speechManager.isFileRecognitionActive()) {
            speechManager.stopFileRecognition()
            onDoneUi()
        } else {
            onFileUi()
            speechManager.startFileRecognition()
        }
    }

    fun toggleMicrophoneRecognition() {
        if (speechManager.isMicrophoneRecognitionActive()) {
            speechManager.stopMicrophoneRecognition()
            onDoneUi()
        } else {
            onMicrophoneUi()
            speechManager.startMicrophoneRecognition()
        }
    }

    fun setPause(isPaused: Boolean) {
        speechManager.setPause(isPaused)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}