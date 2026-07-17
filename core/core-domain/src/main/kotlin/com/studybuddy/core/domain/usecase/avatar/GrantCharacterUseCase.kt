package com.studybuddy.core.domain.usecase.avatar

import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.repository.RewardsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Unlocks a character body for free (e.g. as a quest reward), without spending
 * stars. Idempotent — granting an already-owned character is a no-op.
 *
 * @return true when the character was newly unlocked, false if already owned
 * or the body id is unknown.
 */
class GrantCharacterUseCase @Inject constructor(
    private val rewardsRepository: RewardsRepository,
) {
    suspend operator fun invoke(
        profileId: String,
        bodyId: String,
    ): Boolean {
        val item = RewardCatalog.getCharacterItem(bodyId) ?: return false
        val alreadyOwned = rewardsRepository.isRewardOwned(profileId, item.id).first()
        if (alreadyOwned) return false
        rewardsRepository.purchaseReward(profileId, item)
        return true
    }
}
