package com.studybuddy.feature.home

import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.PlanTask
import com.studybuddy.core.domain.model.PlanTaskProgress
import com.studybuddy.core.domain.model.TodayPlan
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeQuestTest {

    @Test
    fun `a parent's plan wins over what the SRS suggests`() {
        val quest = buildHomeQuest(
            plan = plan(LearningMode.DICTEE to (0 to 2)),
            tablesDue = 3,
            atelierDueVerbs = 5,
            planBonusPoints = 40,
        )

        val ready = assertInstanceOf(HomeQuest.Ready::class.java, quest)
        assertTrue(ready.fromParent)
        assertEquals(1, ready.tasks.size, "the SRS rows must not be mixed into a parent's plan")
    }

    @Test
    fun `a weekday with no plan falls back to the SRS suggestion`() {
        val quest = buildHomeQuest(
            plan = TodayPlan(tasks = emptyList(), bonusAlreadyAwarded = false),
            tablesDue = 3,
            atelierDueVerbs = 0,
            planBonusPoints = 40,
        )

        val ready = assertInstanceOf(HomeQuest.Ready::class.java, quest)
        assertFalse(ready.fromParent, "a rest day should suggest, not show a blank card")
        assertEquals(1, ready.tasks.size)
    }

    @Test
    fun `a plan not yet read falls back rather than flashing an empty card`() {
        val quest = buildHomeQuest(plan = null, tablesDue = 2, atelierDueVerbs = 0, planBonusPoints = 40)

        assertInstanceOf(HomeQuest.Ready::class.java, quest)
    }

    @Test
    fun `nothing planned and nothing due is free choice`() {
        val quest = buildHomeQuest(plan = null, tablesDue = 0, atelierDueVerbs = 0, planBonusPoints = 40)

        assertInstanceOf(HomeQuest.Free::class.java, quest)
    }

    @Test
    fun `a finished plan celebrates and carries the bonus`() {
        val quest = buildHomeQuest(
            plan = plan(LearningMode.DICTEE to (2 to 2)),
            tablesDue = 9,
            atelierDueVerbs = 9,
            planBonusPoints = 40,
        )

        val complete = assertInstanceOf(HomeQuest.Complete::class.java, quest)
        assertEquals(40, complete.bonusPoints)
    }

    @Test
    fun `a part-done row counts down what is left, and a done row shows its target`() {
        val quest = buildHomeQuest(
            plan = plan(
                LearningMode.DICTEE to (1 to 3),
                LearningMode.READING to (1 to 1),
            ),
            tablesDue = 0,
            atelierDueVerbs = 0,
            planBonusPoints = 40,
        )

        val tasks = assertInstanceOf(HomeQuest.Ready::class.java, quest).tasks
        assertEquals(2, tasks[0].count, "one of three done leaves two to go")
        assertFalse(tasks[0].isDone)
        assertEquals(1, tasks[1].count, "a finished row shows its target, not zero")
        assertTrue(tasks[1].isDone)
    }

    @Test
    fun `overshooting a target does not produce a negative count`() {
        val quest = buildHomeQuest(
            plan = plan(LearningMode.DICTEE to (5 to 2), LearningMode.POEMS to (0 to 1)),
            tablesDue = 0,
            atelierDueVerbs = 0,
            planBonusPoints = 40,
        )

        val tasks = assertInstanceOf(HomeQuest.Ready::class.java, quest).tasks
        assertTrue(tasks.all { it.count >= 0 }, "counts were ${tasks.map { it.count }}")
    }

    @Test
    fun `every mode maps to a distinct row, so a plan can name any of them`() {
        val quest = buildHomeQuest(
            plan = plan(*LearningMode.entries.map { it to (0 to 1) }.toTypedArray()),
            tablesDue = 0,
            atelierDueVerbs = 0,
            planBonusPoints = 40,
        )

        val tasks = assertInstanceOf(HomeQuest.Ready::class.java, quest).tasks
        assertEquals(LearningMode.entries.size, tasks.size)
        assertEquals(tasks.size, tasks.map { it.titleRes }.distinct().size, "two modes share a label")
        assertEquals(tasks.size, tasks.map { it.intent }.distinct().size, "two modes open the same screen")
    }

    /** Each pair is `mode to (completed to target)`. */
    private fun plan(vararg entries: Pair<LearningMode, Pair<Int, Int>>) = TodayPlan(
        tasks = entries.mapIndexed { index, (mode, progress) ->
            PlanTaskProgress(
                task = PlanTask(
                    id = "task-$index",
                    profileId = "p",
                    dayOfWeek = 1,
                    mode = mode,
                    targetCount = progress.second,
                    updatedAt = Instant.fromEpochMilliseconds(0),
                ),
                completedCount = progress.first,
            )
        },
        bonusAlreadyAwarded = false,
    )
}
