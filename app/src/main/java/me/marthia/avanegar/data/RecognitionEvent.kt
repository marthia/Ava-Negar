package me.marthia.avanegar.data

import org.vosk.Model

// Define a sealed class to represent the events
sealed class RecognitionEvent {
    data class ModelReady(val model: Model) : RecognitionEvent()
    data class Result(val hypothesis: String) : RecognitionEvent()
    data class FinalResult(val hypothesis: String) : RecognitionEvent()
    data class PartialResult(val hypothesis: String) : RecognitionEvent()
    data class Error(val errorMessage: String) : RecognitionEvent()
    data object Timeout : RecognitionEvent()
}