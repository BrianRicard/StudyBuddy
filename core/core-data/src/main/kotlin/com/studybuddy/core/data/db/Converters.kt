package com.studybuddy.core.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringSet(value: Set<String>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toStringSet(value: String): Set<String> =
        json.decodeFromString(value)
}
