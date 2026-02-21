package com.studybuddy.core.domain.usecase.avatar

import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.repository.RewardsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PurchaseItemUseCase @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val pointsRepository: PointsRepository,
) {
    suspend operator fun invoke(profileId: String, item: RewardItem): PurchaseResult {
        val totalPoints = pointsRepository.getTotalPoints(profileId).first()
        if (totalPoints < item.cost) {
            return PurchaseResult.InsufficientPoints(
                needed = item.cost.toLong() - totalPoints
            )
        }
        pointsRepository.deductPoints(profileId, item.cost, "Purchased: ${item.name}")
        rewardsRepository.purchaseReward(profileId, item)
        return PurchaseResult.Success
    }
}

sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data class InsufficientPoints(val needed: Long) : PurchaseResult
}
