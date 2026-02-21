package com.studybuddy.core.domain.usecase.avatar

import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.repository.RewardsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PurchaseItemUseCaseTest {

    private val rewardsRepository: RewardsRepository = mockk(relaxed = true)
    private val pointsRepository: PointsRepository = mockk(relaxed = true)
    private lateinit var useCase: PurchaseItemUseCase

    private val testItem = RewardItem(
        id = "hat_crown",
        category = RewardCategory.HAT,
        name = "Crown",
        icon = "\uD83D\uDC51",
        cost = 50,
    )

    @BeforeEach
    fun setup() {
        useCase = PurchaseItemUseCase(
            rewardsRepository = rewardsRepository,
            pointsRepository = pointsRepository,
        )
    }

    @Test
    fun `purchase succeeds when balance is sufficient`() = runTest {
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(100L)

        val result = useCase(profileId = "profile1", item = testItem)

        assertTrue(result is PurchaseResult.Success)
    }

    @Test
    fun `purchase deducts points on success`() = runTest {
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(100L)

        useCase(profileId = "profile1", item = testItem)

        coVerify {
            pointsRepository.deductPoints(
                profileId = "profile1",
                amount = 50,
                reason = "Purchased: Crown",
            )
        }
    }

    @Test
    fun `purchase adds item to owned rewards on success`() = runTest {
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(100L)

        useCase(profileId = "profile1", item = testItem)

        coVerify {
            rewardsRepository.purchaseReward(profileId = "profile1", reward = testItem)
        }
    }

    @Test
    fun `purchase fails with insufficient points when balance is too low`() = runTest {
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(30L)

        val result = useCase(profileId = "profile1", item = testItem)

        assertTrue(result is PurchaseResult.InsufficientPoints)
        assertEquals(20L, (result as PurchaseResult.InsufficientPoints).needed)
    }

    @Test
    fun `purchase does not deduct points on insufficient balance`() = runTest {
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(10L)

        useCase(profileId = "profile1", item = testItem)

        coVerify(exactly = 0) {
            pointsRepository.deductPoints(any(), any(), any())
        }
    }

    @Test
    fun `purchase does not add reward on insufficient balance`() = runTest {
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(10L)

        useCase(profileId = "profile1", item = testItem)

        coVerify(exactly = 0) {
            rewardsRepository.purchaseReward(any(), any())
        }
    }

    @Test
    fun `purchase succeeds with exact balance`() = runTest {
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(50L)

        val result = useCase(profileId = "profile1", item = testItem)

        assertTrue(result is PurchaseResult.Success)
    }

    @Test
    fun `purchase free item always succeeds`() = runTest {
        val freeItem = testItem.copy(id = "hat_none", cost = 0)
        coEvery { pointsRepository.getTotalPoints("profile1") } returns flowOf(0L)

        val result = useCase(profileId = "profile1", item = freeItem)

        assertTrue(result is PurchaseResult.Success)
    }
}
