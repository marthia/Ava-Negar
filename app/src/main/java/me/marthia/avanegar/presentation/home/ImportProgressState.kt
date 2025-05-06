package me.marthia.avanegar.presentation.home

sealed class ImportProgressState {
    data object Error : ImportProgressState()
    data class Progress(val percent: Int) : ImportProgressState()
    data object Done : ImportProgressState()
    data object Idle : ImportProgressState()
}
