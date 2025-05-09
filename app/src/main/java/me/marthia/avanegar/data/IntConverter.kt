package me.marthia.avanegar.data

import androidx.room.TypeConverter
import me.marthia.avanegar.presentation.utils.fromJson
import me.marthia.avanegar.presentation.utils.toJson

class IntConverter {
    @TypeConverter
    fun toListOfStrings(value: String): List<Int>? {
        return value.fromJson()
    }

    @TypeConverter
    fun fromListOfStrings(listOfInt: List<Int>?): String {
        return listOfInt?.toString()?.toJson() ?: ""
    }
}