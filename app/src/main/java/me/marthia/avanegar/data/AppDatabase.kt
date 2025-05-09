package me.marthia.avanegar.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * To manage data items that can be accessed, updated
 * & maintain relationships between them
 *
 * @Created by MARTHIA
 */
@Database(
    entities = [
        TranscriptionHistoryEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringConverter::class, IntConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val surahDao: TranscriptionService

    companion object {
        const val DB_NAME = "App.db"

    }
}
