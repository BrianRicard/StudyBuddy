package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationMilestone
import com.studybuddy.core.domain.model.conjugation.ConjugationPathStage
import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetConjugationMilestonesUseCaseTest {

    private val useCase = GetConjugationMilestonesUseCase()

    /**
     * Builds a stage where step N completes at `completedAtMs + N * 100`,
     * so tests can tell "first step" from "last step" derivations apart.
     */
    private fun stage(
        order: Int,
        completedSteps: Int,
        completedAtMs: Long = order * 1_000L,
        perfect: Boolean = true,
        updatedAtMs: Long? = null,
    ): ConjugationPathStage {
        val stage = ConjugationStages.all[order - 1]
        val steps = ConjugationStep.entries.take(completedSteps).withIndex().associate { (index, step) ->
            val stepTime = completedAtMs + index * 100L
            step to ConjugationProgress(
                id = "${stage.id}-$step",
                profileId = "default",
                stageId = stage.id,
                step = step,
                bestCorrect = if (perfect) 6 else 4,
                bestTotal = 6,
                completedAt = Instant.fromEpochMilliseconds(stepTime),
                updatedAt = Instant.fromEpochMilliseconds(updatedAtMs ?: stepTime),
            )
        }
        return ConjugationPathStage(stage = stage, stepProgress = steps, isUnlocked = true)
    }

    private fun status(
        milestone: ConjugationMilestone,
        stages: List<ConjugationPathStage>,
    ) = useCase(stages).first { it.milestone == milestone }

    @Test
    fun `nothing is achieved with no progress`() {
        val stages = ConjugationStages.all.indices.map { stage(it + 1, completedSteps = 0) }

        useCase(stages).forEach {
            assertFalse(it.isAchieved)
            assertEquals(0, it.current)
        }
    }

    @Test
    fun `first step milestone uses the earliest completion time`() {
        val stages = listOf(stage(1, completedSteps = 2, completedAtMs = 500)) +
            (2..6).map { stage(it, completedSteps = 0) }

        val first = status(ConjugationMilestone.FIRST_STEP, stages)

        assertTrue(first.isAchieved)
        assertEquals(Instant.fromEpochMilliseconds(500), first.achievedAt)
    }

    @Test
    fun `verb milestones track completed stage counts`() {
        val stages = (1..3).map { stage(it, completedSteps = ConjugationStep.entries.size) } +
            (4..6).map { stage(it, completedSteps = 1) }

        assertTrue(status(ConjugationMilestone.FIRST_VERB, stages).isAchieved)
        assertTrue(status(ConjugationMilestone.THREE_VERBS, stages).isAchieved)
        val allVerbs = status(ConjugationMilestone.ALL_VERBS, stages)
        assertFalse(allVerbs.isAchieved)
        assertEquals(3, allVerbs.current)
        assertEquals(6, allVerbs.target)
    }

    @Test
    fun `three verbs milestone is stamped when the third stage's LAST step finished`() {
        val stages = (1..6).map { stage(it, completedSteps = ConjugationStep.entries.size) }

        // Stage 3's steps finish at 3000..3400; completion is the last one.
        assertEquals(
            Instant.fromEpochMilliseconds(3_400),
            status(ConjugationMilestone.THREE_VERBS, stages).achievedAt,
        )
    }

    @Test
    fun `perfect quest achieved on replay is stamped when the winning score happened`() {
        val stages = (1..5).map { stage(it, completedSteps = ConjugationStep.entries.size) } +
            stage(
                6,
                completedSteps = ConjugationStep.entries.size,
                updatedAtMs = 99_999L,
            )

        assertEquals(
            Instant.fromEpochMilliseconds(99_999L),
            status(ConjugationMilestone.PERFECT_QUEST, stages).achievedAt,
        )
    }

    @Test
    fun `perfect quest requires every stage completed perfectly`() {
        val perfect = (1..6).map { stage(it, completedSteps = ConjugationStep.entries.size) }
        assertTrue(status(ConjugationMilestone.PERFECT_QUEST, perfect).isAchieved)

        val flawed = perfect.dropLast(1) +
            stage(6, completedSteps = ConjugationStep.entries.size, perfect = false)
        val flawedStatus = status(ConjugationMilestone.PERFECT_QUEST, flawed)
        assertFalse(flawedStatus.isAchieved)
        assertNull(flawedStatus.achievedAt)
        assertEquals(5, flawedStatus.current)
    }
}
