package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.ConjugationDao
import com.studybuddy.core.data.db.entity.ConjugationProgressEntity
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class LocalConjugationRepositoryTest {

    private val dao: ConjugationDao = mockk(relaxed = true)
    private val repository = LocalConjugationRepository(dao)

    private fun existingRow(
        bestCorrect: Int,
        bestTotal: Int,
        completedAt: Long? = 1_000L,
    ) = ConjugationProgressEntity(
        id = "row-1",
        profileId = "default",
        stageId = "etre",
        step = ConjugationStep.WRITE.name,
        bestCorrect = bestCorrect,
        bestTotal = bestTotal,
        completedAt = completedAt,
        updatedAt = 1_000L,
    )

    @Test
    fun `first run inserts a completed record`() = runTest {
        coEvery { dao.getStepProgress("default", "etre", "WRITE") } returns null
        val inserted = slot<ConjugationProgressEntity>()
        coEvery { dao.insert(capture(inserted)) } returns Unit

        repository.recordStepResult("default", "etre", ConjugationStep.WRITE, correct = 4, total = 6)

        assertEquals(4, inserted.captured.bestCorrect)
        assertEquals(6, inserted.captured.bestTotal)
        assertNotNull(inserted.captured.completedAt)
    }

    @Test
    fun `a better run replaces the best score`() = runTest {
        coEvery { dao.getStepProgress("default", "etre", "WRITE") } returns existingRow(4, 6)
        val updated = slot<ConjugationProgressEntity>()
        coEvery { dao.update(capture(updated)) } returns Unit

        repository.recordStepResult("default", "etre", ConjugationStep.WRITE, correct = 6, total = 6)

        assertEquals(6, updated.captured.bestCorrect)
        assertEquals(6, updated.captured.bestTotal)
    }

    @Test
    fun `a worse run keeps the previous best score`() = runTest {
        coEvery { dao.getStepProgress("default", "etre", "WRITE") } returns existingRow(6, 6)
        val updated = slot<ConjugationProgressEntity>()
        coEvery { dao.update(capture(updated)) } returns Unit

        repository.recordStepResult("default", "etre", ConjugationStep.WRITE, correct = 2, total = 6)

        assertEquals(6, updated.captured.bestCorrect)
        assertEquals(6, updated.captured.bestTotal)
    }

    @Test
    fun `original completion timestamp is preserved`() = runTest {
        coEvery { dao.getStepProgress("default", "etre", "WRITE") } returns existingRow(4, 6, completedAt = 777L)
        val updated = slot<ConjugationProgressEntity>()
        coEvery { dao.update(capture(updated)) } returns Unit

        repository.recordStepResult("default", "etre", ConjugationStep.WRITE, correct = 6, total = 6)

        assertEquals(777L, updated.captured.completedAt)
        coVerify(exactly = 0) { dao.insert(any()) }
    }
}
