package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studybuddy.core.data.db.entity.OwnedRewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardsDao {
    @Query("SELECT * FROM owned_rewards WHERE profileId = :profileId")
    fun getOwnedRewards(profileId: String): Flow<List<OwnedRewardEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM owned_rewards WHERE profileId = :profileId AND rewardId = :rewardId)")
    fun isRewardOwned(
        profileId: String,
        rewardId: String,
    ): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reward: OwnedRewardEntity)

    @Query("SELECT * FROM owned_rewards")
    suspend fun getAllOwnedRewards(): List<OwnedRewardEntity>
}
