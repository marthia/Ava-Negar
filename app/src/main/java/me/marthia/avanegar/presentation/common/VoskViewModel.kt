package me.marthia.avanegar.presentation.common

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.marthia.avanegar.domain.RecognitionEvent
import me.marthia.avanegar.domain.RecognitionState
import me.marthia.avanegar.domain.mapVoskResultToTranscription
import javax.inject.Inject

@HiltViewModel
class VoskViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    var recognitionState by mutableStateOf(RecognitionState.Starting)
        private set

    var transcription by mutableStateOf("")
        private set


    var errorMsg by mutableStateOf("")
        private set


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
        speechManager.startFileRecognition(audio)
    }

    fun setPause(isPaused: Boolean) {
        speechManager.setPause(isPaused)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }

    fun initModel(model: Models) {
        speechManager.initModel(model)
    }
}