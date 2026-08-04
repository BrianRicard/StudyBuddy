package com.studybuddy.core.domain.usecase.plan

import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.PlanTask
import com.studybuddy.core.domain.model.PlanTaskProgress
import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.TodayPlan
import com.studybuddy.core.domain.repository.PointsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AwardPlanBonusUseCaseTest {

    private val written = mutableListOf<PointEvent>()

    private val repository = object : PointsRepository {
        // Reads back what was written, so the use case's own idempotence check is
        // exercised rather than a canned empty ledger.
        override fun getPointsForProfile(profileId: String): Flow<List<PointEvent>> = flowOf(written.toList())

        override fun getTotalPoints(profileId: String): Flow<Long> = flowOf(0L)

        override fun getPointsToday(profileId: String): Flow<Int> = flowOf(0)

        override fun getSessionsToday(profileId: String): Flow<Int> = flowOf(0)

        override suspend fun addPointEvent(event: PointEvent) {
            written += event
        }

        override suspend fun deductPoints(
            profileId: String,
            amount: Int,
            reason: String,
        ) = Unit

        override suspend fun sync() = Unit
    }

    private val useCase = AwardPlanBonusUseCase(repository)

    @Test
    fun `pays the bonus once the last task is done`() = runTest {
        val awarded = useCase(PROFILE, plan(done = 2, of = 2), bonusPoints = 40)

        assertEquals(40, awarded)
        assertEquals(PointSource.PLAN_BONUS, written.single().source)
        assertEquals(40, written.single().points)
    }

    @Test
    fun `pays nothing while a task is outstanding`() = runTest {
        assertEquals(0, useCase(PROFILE, plan(done = 1, of = 2), bonusPoints = 40))
        assertTrue(written.isEmpty())
    }

    @Test
    fun `does not pay twice in one day`() = runTest {
        // Home re-collects on every points change, and paying the bonus IS a points
        // change — without this guard it would pay in a loop.
        val plan = plan(done = 2, of = 2)

        assertEquals(40, useCase(PROFILE, plan, bonusPoints = 40))
        assertEquals(0, useCase(PROFILE, plan, bonusPoints = 40))
        assertEquals(1, written.size)
    }

    @Test
    fun `a stale snapshot cannot buy a second payment`() = runTest {
        // The caller passes the plan it observed, which may predate a payment already
        // in flight. Whether the bonus was paid must come from the ledger, not the
        // snapshot, or a queued emission pays again.
        val stale = plan(done = 2, of = 2)
        useCase(PROFILE, stale, bonusPoints = 40)

        assertEquals(0, useCase(PROFILE, stale, bonusPoints = 40), "the stale snapshot still says unpaid")
        assertEquals(1, written.size)
    }

    @Test
    fun `concurrent completions pay exactly once`() = runTest {
        val plan = plan(done = 2, of = 2)

        val results = (1..8).map { async { useCase(PROFILE, plan, bonusPoints = 40) } }.awaitAll()

        assertEquals(1, written.size, "paid ${written.size} times")
        assertEquals(40, results.sum(), "exactly one caller should be told it awarded the bonus")
    }

    @Test
    fun `an empty plan is not a completed plan`() = runTest {
        val restDay = TodayPlan(tasks = emptyList(), bonusAlreadyAwarded = false)

        assertEquals(0, useCase(PROFILE, restDay, bonusPoints = 40))
        assertTrue(written.isEmpty(), "a day with no tasks set must not pay a completion bonus")
    }

    @Test
    fun `a parent who sets the bonus to zero is honoured`() = runTest {
        assertEquals(0, useCase(PROFILE, plan(done = 2, of = 2), bonusPoints = 0))
        assertTrue(written.isEmpty())
    }

    @Test
    fun `overshooting the target still counts as done`() = runTest {
        assertEquals(40, useCase(PROFILE, plan(done = 5, of = 2), bonusPoints = 40))
    }

    private fun plan(
        done: Int,
        of: Int,
    ) = TodayPlan(
        tasks = listOf(
            PlanTaskProgress(
                task = PlanTask(
                    id = "t1",
                    profileId = PROFILE,
                    dayOfWeek = 1,
                    mode = LearningMode.DICTEE,
                    targetCount = of,
                    updatedAt = Instant.fromEpochMilliseconds(0),
                ),
                completedCount = done,
            ),
        ),
        bonusAlreadyAwarded = false,
    )

    private companion object {
        const val PROFILE = "test-profile"
    }
}
