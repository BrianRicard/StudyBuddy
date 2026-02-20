package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.RewardsDao
import com.studybuddy.core.data.db.entity.OwnedRewardEntity
import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.domain.repository.RewardsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRewardsRepository @Inject constructor(
    private val dao: RewardsDao,
) : RewardsRepository {

    override fun getOwnedRewards(profileId: String): Flow<List<RewardItem>> =
        dao.getOwnedRewards(profileId).map { entities ->
            entities.map { entity ->
                RewardItem(
                    id = entity.rewardId,
                    category = runCatching {
                        RewardCategory.valueOf(entity.category)
                    }.getOrDefault(RewardCategory.HAT),
                    name = entity.rewardId,
                    icon = "",
                    cost = 0,
                )
            }
        }

    override fun isRewardOwned(profileId: String, rewardId: String): Flow<Boolean> =
        dao.isRewardOwned(profileId, rewardId)

    override suspend fun purchaseReward(profileId: String, reward: RewardItem) {
        dao.insert(
            OwnedRewardEntity(
                id = java.util.UUID.randomUUID().toString(),
                profileId = profileId,
                rewardId = reward.id,
                category = reward.category.name,
                purchasedAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
