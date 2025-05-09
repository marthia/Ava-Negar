package me.marthia.avanegar.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface TranscriptionService {

    @Query("SELECT * FROM t_transcription_history LIMIT 3")
    fun getTop3() : List<TranscriptionHistoryEntity>


    @Query("SELECT * FROM t_transcription_history")
    fun getAll() : PagingSource<Int, TranscriptionHistoryEntity>
}