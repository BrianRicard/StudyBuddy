package com.studybuddy.feature.home

import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.compose.ui.graphics.Color
import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.TodayPlan
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.theme.SubjectArcade
import com.studybuddy.core.ui.theme.SubjectDictee
import com.studybuddy.core.ui.theme.SubjectMath
import com.studybuddy.core.ui.theme.SubjectPoems
import com.studybuddy.core.ui.theme.SubjectReading
import com.studybuddy.core.ui.theme.SubjectVerbs

/** One line of today's quest: something the child is being asked to do. */
data class QuestTask(
    @PluralsRes val titleRes: Int,
    val count: Int,
    @DrawableRes val iconRes: Int,
    /** The subject hue; the screen resolves it against the live theme. */
    val hue: Color,
    val intent: HomeIntent,
    /** Already satisfied today. Shown as done rather than hidden, so the child sees the win. */
    val isDone: Boolean = false,
)

/**
 * What the quest card shows.
 *
 * [Free] is not an error state — a clear day is a legitimate outcome, and the card
 * offers rather than nags. [Complete] is the celebration when a parent's plan is
 * finished, and carries the bonus so the child sees what it earned.
 */
sealed interface HomeQuest {
    data object Free : HomeQuest

    data class Ready(
        val tasks: List<QuestTask>,
        /** True when a parent set these, false when they came from the SRS schedules. */
        val fromParent: Boolean,
    ) : HomeQuest

    data class Complete(val bonusPoints: Int) : HomeQuest
}

/**
 * Builds today's quest.
 *
 * A parent's plan wins when there is one: it is an explicit instruction for today,
 * whereas the SRS due-counts are the app's own suggestion. A weekday with no plan
 * set is a rest day from the parent's point of view, not a blank screen, so the
 * suggestion takes over rather than showing nothing.
 */
fun buildHomeQuest(
    plan: TodayPlan?,
    tablesDue: Int,
    atelierDueVerbs: Int,
    planBonusPoints: Int,
): HomeQuest {
    if (plan != null && !plan.isEmpty) {
        if (plan.isComplete) return HomeQuest.Complete(planBonusPoints)
        return HomeQuest.Ready(
            tasks = plan.tasks.map { it.toQuestTask() },
            fromParent = true,
        )
    }
    return buildSuggestedQuest(tablesDue, atelierDueVerbs)
}

/**
 * The app's own suggestion, from what the spaced-repetition schedules say is due.
 *
 * Ordering is deliberate: tables before verbs, because a maths fact left unwatered
 * decays faster than a conjugation the child has already met in class.
 */
private fun buildSuggestedQuest(
    tablesDue: Int,
    atelierDueVerbs: Int,
): HomeQuest {
    val tasks = buildList {
        if (tablesDue > 0) {
            add(
                QuestTask(
                    titleRes = CoreUiR.plurals.tables_to_water_plural,
                    count = tablesDue,
                    iconRes = CoreUiR.drawable.ic_subject_math,
                    hue = SubjectMath,
                    intent = HomeIntent.NavigateToMath,
                ),
            )
        }
        if (atelierDueVerbs > 0) {
            add(
                QuestTask(
                    titleRes = CoreUiR.plurals.atelier_verbs_to_water_plural,
                    count = atelierDueVerbs,
                    iconRes = CoreUiR.drawable.ic_subject_verbs,
                    hue = SubjectVerbs,
                    intent = HomeIntent.NavigateToConjugation,
                ),
            )
        }
    }
    return if (tasks.isEmpty()) HomeQuest.Free else HomeQuest.Ready(tasks, fromParent = false)
}

private fun com.studybuddy.core.domain.model.PlanTaskProgress.toQuestTask(): QuestTask {
    val visuals = task.mode.visuals()
    return QuestTask(
        // The count shown is what is *left* to do, so the row reads as an instruction
        // rather than a score. A finished row shows its full target instead of zero.
        titleRes = visuals.titleRes,
        count = if (isDone) task.targetCount else task.targetCount - completedCount,
        iconRes = visuals.iconRes,
        hue = visuals.hue,
        intent = visuals.intent,
        isDone = isDone,
    )
}

private data class ModeVisuals(
    @PluralsRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val hue: Color,
    val intent: HomeIntent,
)

private fun LearningMode.visuals(): ModeVisuals = when (this) {
    LearningMode.DICTEE -> ModeVisuals(
        CoreUiR.plurals.plan_task_dictee,
        CoreUiR.drawable.ic_subject_dictee,
        SubjectDictee,
        HomeIntent.NavigateToDictee,
    )
    LearningMode.SPEED_MATH -> ModeVisuals(
        CoreUiR.plurals.plan_task_speed_math,
        CoreUiR.drawable.ic_subject_math,
        SubjectMath,
        HomeIntent.NavigateToMath,
    )
    LearningMode.VERB_QUEST -> ModeVisuals(
        CoreUiR.plurals.plan_task_verb_quest,
        CoreUiR.drawable.ic_subject_verbs,
        SubjectVerbs,
        HomeIntent.NavigateToConjugation,
    )
    LearningMode.POEMS -> ModeVisuals(
        CoreUiR.plurals.plan_task_poems,
        CoreUiR.drawable.ic_subject_poems,
        SubjectPoems,
        HomeIntent.NavigateToPoems,
    )
    LearningMode.READING -> ModeVisuals(
        CoreUiR.plurals.plan_task_reading,
        CoreUiR.drawable.ic_subject_reading,
        SubjectReading,
        HomeIntent.NavigateToReading,
    )
    LearningMode.MATH_CHALLENGE -> ModeVisuals(
        CoreUiR.plurals.plan_task_math_challenge,
        CoreUiR.drawable.ic_subject_arcade,
        SubjectArcade,
        HomeIntent.NavigateToMathChallenge,
    )
}
