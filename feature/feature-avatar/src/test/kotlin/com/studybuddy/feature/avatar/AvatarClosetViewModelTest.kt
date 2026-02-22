package com.studybuddy.feature.avatar

import app.cash.turbine.test
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.usecase.avatar.GetAvatarConfigUseCase
import com.studybuddy.core.domain.usecase.avatar.PurchaseItemUseCase
import com.studybuddy.core.domain.usecase.avatar.PurchaseResult
import com.studybuddy.core.domain.usecase.avatar.UpdateAvatarUseCase
import com.studybuddy.core.domain.usecase.points.GetTotalPointsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarClosetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getAvatarConfig: GetAvatarConfigUseCase = mockk()
    private val updateAvatar: UpdateAvatarUseCase = mockk(relaxed = true)
    private val purchaseItem: PurchaseItemUseCase = mockk()
    private val getTotalPoints: GetTotalPointsUseCase = mockk()
    private val rewardsRepository: RewardsRepository = mockk()

    private val defaultConfig = AvatarConfig(
        bodyId = "fox",
        hatId = "hat_none",
        faceId = "face_none",
        outfitId = "outfit_none",
        petId = "pet_none",
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getAvatarConfig(any()) } returns flowOf(defaultConfig)
        every { rewardsRepository.getOwnedRewards(any()) } returns flowOf(emptyList())
        every { getTotalPoints(any()) } returns flowOf(200L)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AvatarClosetViewModel(
        getAvatarConfigUseCase = getAvatarConfig,
        updateAvatarUseCase = updateAvatar,
        purchaseItemUseCase = purchaseItem,
        getTotalPointsUseCase = getTotalPoints,
        rewardsRepository = rewardsRepository,
    )

    @Test
    fun `init loads avatar config and star balance`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(defaultConfig, state.avatarConfig)
        assertEquals(200L, state.starBalance)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `init includes starter items as owned`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.ownedItemIds.containsAll(RewardCatalog.starterItemIds))
    }

    @Test
    fun `select character updates avatar config bodyId`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(AvatarClosetIntent.SelectCharacter("unicorn"))
        advanceUntilIdle()

        assertEquals("unicorn", viewModel.state.value.avatarConfig?.bodyId)
        coVerify {
            updateAvatar(
                profileId = any(),
                config = match { it.bodyId == "unicorn" },
            )
        }
    }

    @Test
    fun `select tab updates selected tab`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(AvatarClosetIntent.SelectTab(AccessoryTab.PETS))
        advanceUntilIdle()

        assertEquals(AccessoryTab.PETS, viewModel.state.value.selectedTab)
    }

    @Test
    fun `equip owned hat updates avatar config hatId`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // hat_tophat is a starter item (owned)
        viewModel.onIntent(AvatarClosetIntent.EquipItem("hat_tophat"))
        advanceUntilIdle()

        assertEquals("hat_tophat", viewModel.state.value.avatarConfig?.hatId)
        coVerify {
            updateAvatar(
                profileId = any(),
                config = match { it.hatId == "hat_tophat" },
            )
        }
    }

    @Test
    fun `equip owned pet updates avatar config petId`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(AvatarClosetIntent.EquipItem("pet_chick"))
        advanceUntilIdle()

        assertEquals("pet_chick", viewModel.state.value.avatarConfig?.petId)
    }

    @Test
    fun `request purchase shows purchase dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val crown = RewardCatalog.getItemById("hat_crown")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(crown))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.showPurchaseDialog)
        assertEquals("hat_crown", viewModel.state.value.showPurchaseDialog?.id)
    }

    @Test
    fun `dismiss purchase dialog clears dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val crown = RewardCatalog.getItemById("hat_crown")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(crown))
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.showPurchaseDialog)

        viewModel.onIntent(AvatarClosetIntent.DismissPurchaseDialog)
        advanceUntilIdle()

        assertNull(viewModel.state.value.showPurchaseDialog)
    }

    @Test
    fun `confirm purchase success equips item and emits effect`() = runTest {
        coEvery { purchaseItem(any(), any()) } returns PurchaseResult.Success

        val viewModel = createViewModel()
        advanceUntilIdle()

        val crown = RewardCatalog.getItemById("hat_crown")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(crown))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(AvatarClosetIntent.ConfirmPurchase)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AvatarClosetEffect.PurchaseSuccess)
            assertEquals("Crown", (effect as AvatarClosetEffect.PurchaseSuccess).itemName)
        }

        // Item should be equipped
        assertEquals("hat_crown", viewModel.state.value.avatarConfig?.hatId)
        // Dialog should be dismissed
        assertNull(viewModel.state.value.showPurchaseDialog)
    }

    @Test
    fun `confirm purchase insufficient points shows error`() = runTest {
        coEvery { purchaseItem(any(), any()) } returns PurchaseResult.InsufficientPoints(needed = 30)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val crown = RewardCatalog.getItemById("hat_crown")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(crown))
        advanceUntilIdle()

        viewModel.onIntent(AvatarClosetIntent.ConfirmPurchase)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.purchaseError)
        assertTrue(viewModel.state.value.purchaseError!!.contains("30"))
        // Dialog should still be showing
        assertNotNull(viewModel.state.value.showPurchaseDialog)
    }
}
