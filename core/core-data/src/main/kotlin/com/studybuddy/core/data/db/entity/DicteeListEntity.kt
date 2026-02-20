package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dictee_lists",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId")],
)
data class DicteeListEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val title: String,
    val language: String,
    val createdAt: Long,
    val updatedAt: Long,
)
