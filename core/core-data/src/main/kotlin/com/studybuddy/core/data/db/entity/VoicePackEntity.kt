package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_packs")
data class VoicePackEntity(
    @PrimaryKey val id: String,
    val locale: String,
    val displayName: String,
    val sizeBytes: Long,
    val status: String,
)
