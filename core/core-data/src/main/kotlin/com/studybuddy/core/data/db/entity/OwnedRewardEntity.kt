package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "owned_rewards",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId"), Index(value = ["profileId", "rewardId"], unique = true)],
)
data class OwnedRewardEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val rewardId: String,
    val category: String,
    val purchasedAt: Long,
)
