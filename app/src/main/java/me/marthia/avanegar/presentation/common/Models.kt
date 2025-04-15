package me.marthia.avanegar.presentation.common


enum class Models(val title: String, val path: String) {
    EN(
        title = "vosk-model-small-en-us-0.15",
        path = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"
    ),
    FA(
        title = "vosk-model-small-fa-0.42",
        path = "https://alphacephei.com/vosk/models/vosk-model-small-fa-0.42.zip"
    ),
    FA_ALT(
        title = "vosk-model-small-fa-0.5",
        path = "https://alphacephei.com/vosk/models/vosk-model-small-fa-0.5.zip"
    ),
    RU(
        title = "vosk-model-small-ru-0.22",
        path = "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip"
    )
}