package com.studybuddy.core.domain.usecase.avatar

import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.domain.repository.RewardsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GrantCharacterUseCaseTest {

    private val rewardsRepository: RewardsRepository = mockk(relaxed = true)
    private val useCase = GrantCharacterUseCase(rewardsRepository)

    @Test
    fun `grants an unowned character and reports a new unlock`() = runTest {
        coEvery { rewardsRepository.isRewardOwned("default", "char_ladybug") } returns flowOf(false)
        val granted = slot<RewardItem>()
        coEvery { rewardsRepository.purchaseReward(any(), capture(granted)) } returns Unit

        val result = useCase("default", "ladybug")

        assertTrue(result)
        assertEquals("char_ladybug", granted.captured.id)
    }

    @Test
    fun `an already-owned character is not re-granted`() = runTest {
        coEvery { rewardsRepository.isRewardOwned("default", "char_ladybug") } returns flowOf(true)

        val result = useCase("default", "ladybug")

        assertFalse(result)
        coVerify(exactly = 0) { rewardsRepository.purchaseReward(any(), any()) }
    }

    @Test
    fun `an unknown body id grants nothing`() = runTest {
        val result = useCase("default", "not_a_character")

        assertFalse(result)
        coVerify(exactly = 0) { rewardsRepository.purchaseReward(any(), any()) }
    }
}
