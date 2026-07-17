package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.ConjugationDao
import com.studybuddy.core.data.db.entity.ConjugationProgressEntity
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * In-memory fake so the DAO's default [ConjugationDao.recordResult] merge
 * logic runs for real instead of being mocked away.
 */
private class FakeConjugationDao : ConjugationDao {
    private val rows = MutableStateFlow<Map<String, ConjugationProgressEntity>>(emptyMap())

    private fun key(entity: ConjugationProgressEntity) = "${entity.profileId}|${entity.stageId}|${entity.step}"

    override fun getProgressForProfile(profileId: String): Flow<List<ConjugationProgressEntity>> =
        rows.map { all -> all.values.filter { it.profileId == profileId } }

    override suspend fun getStepProgress(
        profileId: String,
        stageId: String,
        step: String,
    ): ConjugationProgressEntity? = rows.value["$profileId|$stageId|$step"]

    override suspend fun insert(progress: ConjugationProgressEntity) {
        check(key(progress) !in rows.value) { "unique index violation" }
        rows.value += (key(progress) to progress)
    }

    override suspend fun update(progress: ConjugationProgressEntity) {
        rows.value += (key(progress) to progress)
    }
}

class LocalConjugationRepositoryTest {

    private val dao = FakeConjugationDao()
    private val repository = LocalConjugationRepository(dao)

    private suspend fun record(
        correct: Int,
        total: Int = 6,
    ) = repository.recordStepResult(
        profileId = "default",
        stageId = "etre",
        step = ConjugationStep.WRITE,
        correct = correct,
        total = total,
    )

    private suspend fun storedRow() = dao.getStepProgress("default", "etre", "WRITE")

    @Test
    fun `first run inserts a completed record and reports first completion`() = runTest {
        val outcome = record(correct = 4)

        assertTrue(outcome.firstCompletion)
        assertTrue(outcome.newBest)
        val row = storedRow()
        assertNotNull(row)
        assertEquals(4, row?.bestCorrect)
        assertEquals(6, row?.bestTotal)
        assertNotNull(row?.completedAt)
    }

    @Test
    fun `a better run replaces the best score`() = runTest {
        record(correct = 4)
        val outcome = record(correct = 6)

        assertFalse(outcome.firstCompletion)
        assertTrue(outcome.newBest)
        assertEquals(6, storedRow()?.bestCorrect)
    }

    @Test
    fun `a worse run leaves the stored record untouched`() = runTest {
        record(correct = 6)
        val before = storedRow()

        val outcome = record(correct = 2)

        assertFalse(outcome.newBest)
        assertEquals(before, storedRow())
    }

    @Test
    fun `equal ratio with larger total wins the tie`() = runTest {
        record(correct = 3, total = 6)
        val outcome = record(correct = 4, total = 8)

        assertTrue(outcome.newBest)
        assertEquals(4, storedRow()?.bestCorrect)
        assertEquals(8, storedRow()?.bestTotal)
    }

    @Test
    fun `ratios are compared across different totals`() = runTest {
        record(correct = 5, total = 6)
        val outcome = record(correct = 6, total = 8)

        // 6/8 < 5/6: not a new best.
        assertFalse(outcome.newBest)
        assertEquals(5, storedRow()?.bestCorrect)
    }

    @Test
    fun `a scored run beats a stored zero-total record`() = runTest {
        record(correct = 0, total = 0)
        val outcome = record(correct = 3, total = 6)

        assertTrue(outcome.newBest)
        assertEquals(3, storedRow()?.bestCorrect)
        assertEquals(6, storedRow()?.bestTotal)
    }

    @Test
    fun `original completion timestamp is preserved across improvements`() = runTest {
        record(correct = 4)
        val firstCompletedAt = storedRow()?.completedAt

        record(correct = 6)

        assertEquals(firstCompletedAt, storedRow()?.completedAt)
    }

    @Test
    fun `progress flow drops rows with unknown step names`() = runTest {
        record(correct = 4)
        dao.insert(
            ConjugationProgressEntity(
                id = "legacy",
                profileId = "default",
                stageId = "etre",
                step = "FUTURE_STEP",
                bestCorrect = 1,
                bestTotal = 1,
                completedAt = 1L,
                updatedAt = 1L,
            ),
        )

        val progress = repository.getProgressForProfile("default").first()

        assertEquals(listOf(ConjugationStep.WRITE), progress.map { it.step })
    }

    @Test
    fun `invalid scores are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { record(correct = 7, total = 6) }
        }
    }
}
