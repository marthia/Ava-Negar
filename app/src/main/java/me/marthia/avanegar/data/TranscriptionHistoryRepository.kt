package me.marthia.avanegar.data

import androidx.paging.PagingSource

interface TranscriptionHistoryRepository {
    fun get(): PagingSource<Int, TranscriptionHistory>
}