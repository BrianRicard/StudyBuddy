package com.studybuddy.core.domain.usecase.plan

import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.PlanDayResult
import com.studybuddy.core.domain.model.PlanTask
import com.studybuddy.core.domain.repository.ParentPlanRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * The last [DAYS] days, scored against whatever plan applies to each weekday.
 *
 * For the Parent Zone only. The child's own screens never show a past shortfall.
 *
 * A caveat worth knowing: history is scored against the plan as it stands *now*,
 * not as it stood on the day. Storing a snapshot per day would be more faithful,
 * but it would also mean a parent tightening the plan retroactively breaks a run of
 * green days, which reads worse than the small inaccuracy.
 */
class GetPlanHistoryUseCase @Inject constructor(
    private val repository: ParentPlanRepository,
    private val clock: Clock = Clock.System,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(profileId: String): Flow<List<PlanDayResult>> = dayTicker(clock).flatMapLatest { day ->
        val timeZone = TimeZone.currentSystemDefault()
        val dates = (0 until DAYS).map { day.date.minus(DatePeriod(days = it)) }.reversed()

        // The plan is combined in as one more source rather than wrapped around the
        // rest: under flatMapLatest, every "+" tap in the parent's editor would tear
        // down and re-subscribe all seven day queries.
        val sources = dates.map { date ->
            repository.getSessionCounts(
                profileId = profileId,
                from = date.atStartOfDayIn(timeZone),
                to = date.plus(DatePeriod(days = 1)).atStartOfDayIn(timeZone),
            )
        } + repository.getPlan(profileId)

        combine(sources) { values ->
            @Suppress("UNCHECKED_CAST")
            val plan = values.last() as List<PlanTask>
            dates.mapIndexed { index, date ->
                @Suppress("UNCHECKED_CAST")
                scoreDay(date, plan, values[index] as Map<LearningMode, Int>)
            }
        }
    }

    private fun scoreDay(
        date: LocalDate,
        plan: List<PlanTask>,
        counts: Map<LearningMode, Int>,
    ): PlanDayResult {
        val tasks = plan.filter { it.dayOfWeek == date.dayOfWeek.isoDayNumber }
        return PlanDayResult(
            date = date,
            plannedCount = tasks.size,
            completedCount = tasks.count { (counts[it.mode] ?: 0) >= it.targetCount },
        )
    }

    private companion object {
        const val DAYS = 7
    }
}
