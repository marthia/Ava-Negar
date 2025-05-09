package me.marthia.avanegar.data

import androidx.room.TypeConverter
import me.marthia.avanegar.presentation.utils.fromJson
import me.marthia.avanegar.presentation.utils.toJson

class StringConverter {
    @TypeConverter
    fun toListOfStrings(stringValue: String): List<String>? {
        return stringValue.fromJson()
    }

    @TypeConverter
    fun fromListOfStrings(listOfString: List<String>?): String {
        return listOfString?.toJson() ?: ""
    }
}