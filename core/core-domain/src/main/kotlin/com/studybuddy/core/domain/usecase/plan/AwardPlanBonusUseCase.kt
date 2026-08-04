package com.studybuddy.core.domain.usecase.plan

import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.TodayPlan
import com.studybuddy.core.domain.repository.PointsRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Pays the bonus for finishing every task the parent set for today.
 *
 * Idempotent per day, and it has to be: the Home screen re-collects on every points
 * change, and awarding the bonus is itself a points change. The caller's [TodayPlan]
 * is only used to decide *whether the work is done* — whether the bonus was already
 * paid is re-read from the ledger under a lock, because the snapshot the caller
 * observed may predate a payment already in flight.
 *
 * Singleton so the lock is shared; a plain `@Inject` class would give each collector
 * its own mutex and no mutual exclusion at all.
 *
 * Returns the points awarded, or 0 if nothing was due.
 */
@Singleton
class AwardPlanBonusUseCase @Inject constructor(
    private val pointsRepository: PointsRepository,
    private val clock: Clock = Clock.System,
) {
    private val lock = Mutex()

    suspend operator fun invoke(
        profileId: String,
        plan: TodayPlan,
        bonusPoints: Int,
    ): Int {
        if (!plan.isComplete || bonusPoints <= 0) return 0

        return lock.withLock {
            val timeZone = TimeZone.currentSystemDefault()
            val dayStart = clock.now().toLocalDateTime(timeZone).date.atStartOfDayIn(timeZone)
            val alreadyPaid = pointsRepository.getPointsForProfile(profileId).first()
                .any { it.source == PointSource.PLAN_BONUS && it.timestamp >= dayStart }
            if (alreadyPaid) return@withLock 0

            pointsRepository.addPointEvent(
                PointEvent(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    source = PointSource.PLAN_BONUS,
                    points = bonusPoints,
                    reason = PLAN_BONUS_REASON,
                    timestamp = clock.now(),
                ),
            )
            bonusPoints
        }
    }

    private companion object {
        /**
         * Never shown to the child — the UI labels this row from [PointSource]. It is
         * here for the parent's export, which is read by an adult.
         */
        const val PLAN_BONUS_REASON = "All of today's tasks complete"
    }
}
