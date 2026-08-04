package com.studybuddy.core.domain.usecase.plan

import com.studybuddy.core.domain.model.PlanTaskProgress
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.TodayPlan
import com.studybuddy.core.domain.repository.ParentPlanRepository
import com.studybuddy.core.domain.repository.PointsRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.isoDayNumber

/**
 * Today's plan with the child's progress against it.
 *
 * Re-derives the day from [dayTicker], which fires again at each local midnight, so
 * a tablet left switched on overnight rolls over to the new day's plan. Capturing
 * the boundary once would leave it scoring the morning's sessions against
 * yesterday's window — where they count as zero — until the process restarted.
 */
class GetTodayPlanUseCase @Inject constructor(
    private val planRepository: ParentPlanRepository,
    private val pointsRepository: PointsRepository,
    private val clock: Clock = Clock.System,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(profileId: String): Flow<TodayPlan> = dayTicker(clock).flatMapLatest { day ->
        planRepository.getPlanForDay(profileId, day.date.dayOfWeek.isoDayNumber)
            .flatMapLatest { tasks ->
                combine(
                    planRepository.getSessionCounts(profileId, day.start, day.end),
                    bonusAwardedToday(profileId, day.start),
                ) { counts, bonusAwarded ->
                    TodayPlan(
                        tasks = tasks.map { task ->
                            PlanTaskProgress(task = task, completedCount = counts[task.mode] ?: 0)
                        },
                        bonusAlreadyAwarded = bonusAwarded,
                    )
                }
            }
    }

    private fun bonusAwardedToday(
        profileId: String,
        dayStart: Instant,
    ): Flow<Boolean> = pointsRepository.getPointsForProfile(profileId).map { events ->
        events.any { it.source == PointSource.PLAN_BONUS && it.timestamp >= dayStart }
    }
}
