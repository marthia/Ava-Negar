package me.marthia.avanegar.data

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.IOException

@Serializable
data class Transcription(val text: String, val partial: String = "") //partial is optional

fun mapVoskResultToTranscription(jsonString: String): Transcription {
    try {
        // Use kotlinx.serialization to parse the JSON string
        val jsonObject = Json.parseToJsonElement(jsonString).jsonObject

        val text = jsonObject["text"]?.toString()?.replace("\"", "")?.trim() ?: "" // Handle null values gracefully
        val partial = jsonObject["partial"]?.toString()?.replace("\"", "")?.trim() ?: ""

        return Transcription(text, partial)

    } catch (e: IOException) {
        // Handle JSON parsing errors.  Log the error and return a default transcription or throw an exception.
        println("Error parsing Vosk result: $e")
        return Transcription("") // Or throw an exception if you prefer
    }
}


// Example Usage (assuming 'rec.Result()' returns a JSON string)
fun processVoskResult(jsonString: String) {
    val transcription = mapVoskResultToTranscription(jsonString)
    println("Recognized Text: ${transcription.text}")
    if (transcription.partial.isNotEmpty()) {
        println("Partial Result: ${transcription.partial}")
    }
}

