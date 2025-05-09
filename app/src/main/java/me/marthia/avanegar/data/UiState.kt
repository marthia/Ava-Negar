package me.marthia.avanegar.data

data class UiState(
    val recognitionState: RecognitionState = RecognitionState.Starting,
    val resultText: String = "",
    val error: String? = null,
    val isPaused: Boolean = false,
    val fileButtonEnabled: Boolean = false,
    val micButtonEnabled: Boolean = false,
    val pauseEnabled: Boolean = false
) {
    override fun toString(): String {
        return "UiState(recognitionState=$recognitionState," +
                " resultText='$resultText'," +
                " error=$error," +
                " isPaused=$isPaused," +
                " fileButtonEnabled=$fileButtonEnabled," +
                " micButtonEnabled=$micButtonEnabled," +
                " pauseEnabled=$pauseEnabled)"
    }
}