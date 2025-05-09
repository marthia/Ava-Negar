package me.marthia.avanegar.presentation.common

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.marthia.avanegar.data.TranscriptionHistoryRepository
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: TranscriptionHistoryRepository
) : ViewModel() {


}