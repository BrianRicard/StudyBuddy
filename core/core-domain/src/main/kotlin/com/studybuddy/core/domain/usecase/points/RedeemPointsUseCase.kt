package com.studybuddy.core.domain.usecase.points

import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.PointsRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Takes stars back in exchange for a real-world reward the parent hands over.
 *
 * The counterpart to a [PointSource.GIFT] grant, and deliberately not the same
 * source: one is the parent adding, the other the parent spending, and the Latest
 * list has to be able to tell them apart.
 *
 * Clamped at the current balance. `PointsRepository.deductPoints` is not — it just
 * writes a negative event and lets `getTotalPoints` sum it — so without this a
 * mistyped redemption leaves the child owing stars, which is exactly the punishing
 * feedback the app avoids.
 */
class RedeemPointsUseCase @Inject constructor(
    private val repository: PointsRepository,
    private val clock: Clock = Clock.System,
) {
    /** Returns the number of stars actually spent, which may be less than [amount]. */
    suspend operator fun invoke(
        profileId: String,
        amount: Int,
        reason: String,
    ): Int {
        if (amount <= 0) return 0

        val balance = repository.getTotalPoints(profileId).first()
        val spend = minOf(amount.toLong(), maxOf(balance, 0L)).toInt()
        if (spend == 0) return 0

        repository.addPointEvent(
            PointEvent(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                source = PointSource.REDEMPTION,
                points = -spend,
                reason = reason,
                timestamp = clock.now(),
            ),
        )
        return spend
    }
}
