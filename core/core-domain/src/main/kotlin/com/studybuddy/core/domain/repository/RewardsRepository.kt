package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.RewardItem
import kotlinx.coroutines.flow.Flow

interface RewardsRepository {
    fun getOwnedRewards(profileId: String): Flow<List<RewardItem>>
    fun isRewardOwned(profileId: String, rewardId: String): Flow<Boolean>
    suspend fun purchaseReward(profileId: String, reward: RewardItem)
    suspend fun sync()
}
