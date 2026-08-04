package com.studybuddy.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * One line of a parent's plan: "on Tuesdays, two Dictée sessions".
 *
 * [dayOfWeek] is ISO-8601 — Monday is 1, Sunday is 7 — matching
 * `kotlinx.datetime.DayOfWeek.isoDayNumber` so no lookup table is needed.
 */
data class PlanTask(
    val id: String,
    val profileId: String,
    val dayOfWeek: Int,
    val mode: LearningMode,
    val targetCount: Int,
    val updatedAt: Instant,
)

/** A [PlanTask] with the child's progress against it today. */
data class PlanTaskProgress(
    val task: PlanTask,
    val completedCount: Int,
) {
    val isDone: Boolean get() = completedCount >= task.targetCount
}

/**
 * Everything the Home screen needs about today's plan.
 *
 * An empty [tasks] list means the parent set nothing for this weekday — a legitimate
 * rest day, not an error, so Home falls back to what the SRS schedules say is due.
 */
data class TodayPlan(
    val tasks: List<PlanTaskProgress>,
    val bonusAlreadyAwarded: Boolean,
) {
    val isEmpty: Boolean get() = tasks.isEmpty()
    val isComplete: Boolean get() = tasks.isNotEmpty() && tasks.all { it.isDone }
}

/**
 * One day of the parent's history view.
 *
 * The child never sees this: an unfinished day expires quietly overnight, because
 * waking up to a debt is the punishing feedback this app does not do.
 */
data class PlanDayResult(
    val date: LocalDate,
    val plannedCount: Int,
    val completedCount: Int,
) {
    val isComplete: Boolean get() = plannedCount > 0 && completedCount >= plannedCount

    /** No plan set for that weekday, so there was nothing to miss. */
    val isRestDay: Boolean get() = plannedCount == 0
}
