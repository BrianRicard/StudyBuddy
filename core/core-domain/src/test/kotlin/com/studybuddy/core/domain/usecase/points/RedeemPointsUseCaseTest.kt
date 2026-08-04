package com.studybuddy.core.domain.usecase.points

import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RedeemPointsUseCaseTest {

    private val written = mutableListOf<PointEvent>()

    private fun useCase(balance: Long) = RedeemPointsUseCase(
        object : PointsRepository {
            override fun getPointsForProfile(profileId: String): Flow<List<PointEvent>> = flowOf(emptyList())

            override fun getTotalPoints(profileId: String): Flow<Long> = flowOf(balance)

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
        },
    )

    @Test
    fun `spends the requested amount when the child can afford it`() = runTest {
        val spent = useCase(balance = 500).invoke(PROFILE, amount = 200, reason = "Lego set")

        assertEquals(200, spent)
        assertEquals(-200, written.single().points)
        assertEquals(PointSource.REDEMPTION, written.single().source)
    }

    @Test
    fun `clamps to the balance rather than going negative`() = runTest {
        // The parent mistypes 5000 for a child with 120 stars.
        val spent = useCase(balance = 120).invoke(PROFILE, amount = 5000, reason = "Bike")

        assertEquals(120, spent)
        assertEquals(-120, written.single().points)
    }

    @Test
    fun `an already-negative balance cannot be driven further down`() = runTest {
        val spent = useCase(balance = -50).invoke(PROFILE, amount = 100, reason = "Book")

        assertEquals(0, spent)
        assertTrue(written.isEmpty(), "nothing should be written when there is nothing to spend")
    }

    @Test
    fun `a zero or negative request is ignored`() = runTest {
        assertEquals(0, useCase(balance = 500).invoke(PROFILE, amount = 0, reason = "x"))
        assertEquals(0, useCase(balance = 500).invoke(PROFILE, amount = -30, reason = "x"))
        assertTrue(written.isEmpty())
    }

    @Test
    fun `redemption is distinguishable from a shop purchase and from a gift`() = runTest {
        useCase(balance = 500).invoke(PROFILE, amount = 100, reason = "Lego set")

        val source = written.single().source
        assertTrue(source != PointSource.PURCHASE, "a parent trade is not a shop purchase")
        assertTrue(source != PointSource.GIFT, "a parent trade is not a parent grant")
    }

    private companion object {
        const val PROFILE = "test-profile"
    }
}
