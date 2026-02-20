package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "avatar_configs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId", unique = true)],
)
data class AvatarConfigEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val bodyId: String,
    val hatId: String,
    val faceId: String,
    val outfitId: String,
    val petId: String,
    val equippedTitle: String? = null,
    val updatedAt: Long,
)
