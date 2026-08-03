package com.studybuddy.feature.home

import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.compose.ui.graphics.Color
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.theme.SubjectMath
import com.studybuddy.core.ui.theme.SubjectVerbs

/**
 * One line of today's quest: something the child is being asked to do.
 *
 * Today these are derived from what the spaced-repetition schedules say is due.
 * When a parent can set the day's tasks, the same rows will come from that plan
 * instead — [HomeQuest] is the seam, so the screen does not change again.
 */
data class QuestTask(
    @PluralsRes val titleRes: Int,
    val count: Int,
    @DrawableRes val iconRes: Int,
    /** The subject hue; the screen resolves it against the live theme. */
    val hue: Color,
    val intent: HomeIntent,
)

/**
 * What the quest card shows. [Free] is not an error state — a clear day is a
 * legitimate outcome, and the card offers rather than nags.
 */
sealed interface HomeQuest {
    data object Free : HomeQuest

    data class Ready(val tasks: List<QuestTask>) : HomeQuest
}

/**
 * Builds today's quest from the due counts the home state already carries.
 *
 * Ordering is deliberate: tables before verbs, because a maths fact left unwatered
 * decays faster than a conjugation the child has already met in class.
 */
fun buildHomeQuest(
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
    return if (tasks.isEmpty()) HomeQuest.Free else HomeQuest.Ready(tasks)
}
