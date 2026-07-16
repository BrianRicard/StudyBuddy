package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetConjugationPathUseCaseTest {

    private val repository: ConjugationRepository = mockk()
    private val useCase = GetConjugationPathUseCase(repository)

    private fun completedStep(
        stageId: String,
        step: ConjugationStep,
        correct: Int = 6,
        total: Int = 6,
    ) = ConjugationProgress(
        id = "$stageId-$step",
        profileId = "default",
        stageId = stageId,
        step = step,
        bestCorrect = correct,
        bestTotal = total,
        completedAt = Instant.fromEpochMilliseconds(1_000),
        updatedAt = Instant.fromEpochMilliseconds(1_000),
    )

    private fun givenProgress(progress: List<ConjugationProgress>) {
        every { repository.getProgressForProfile("default") } returns flowOf(progress)
    }

    @Test
    fun `with no progress only stage 1 is unlocked`() = runTest {
        givenProgress(emptyList())

        val path = useCase("default").first()

        assertEquals(6, path.size)
        assertTrue(path.first().isUnlocked)
        assertTrue(path.drop(1).none { it.isUnlocked })
        assertEquals(ConjugationStep.LEARN, path.first().nextStep)
    }

    @Test
    fun `completing every step of stage 1 unlocks stage 2 only`() = runTest {
        givenProgress(ConjugationStep.entries.map { completedStep("etre", it) })

        val path = useCase("default").first()

        assertTrue(path[0].isCompleted)
        assertNull(path[0].nextStep)
        assertTrue(path[1].isUnlocked)
        assertFalse(path[2].isUnlocked)
    }

    @Test
    fun `a partially completed stage does not unlock the next one`() = runTest {
        givenProgress(
            listOf(
                completedStep("etre", ConjugationStep.LEARN),
                completedStep("etre", ConjugationStep.WRITE),
            ),
        )

        val path = useCase("default").first()

        assertFalse(path[0].isCompleted)
        assertEquals(ConjugationStep.SPEAK, path[0].nextStep)
        assertFalse(path[1].isUnlocked)
    }

    @Test
    fun `steps unlock in order within a stage`() = runTest {
        givenProgress(listOf(completedStep("etre", ConjugationStep.LEARN)))

        val stage1 = useCase("default").first().first()

        assertTrue(stage1.isStepUnlocked(ConjugationStep.LEARN))
        assertTrue(stage1.isStepUnlocked(ConjugationStep.WRITE))
        assertFalse(stage1.isStepUnlocked(ConjugationStep.SPEAK))
        assertFalse(stage1.isStepUnlocked(ConjugationStep.BOSS))
    }
}
