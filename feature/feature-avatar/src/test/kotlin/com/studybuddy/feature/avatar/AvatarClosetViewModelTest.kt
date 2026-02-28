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
        bodyId = "bunny",
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
    fun `select owned character updates avatar config bodyId`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // squirrel is a free starter character
        viewModel.onIntent(AvatarClosetIntent.SelectCharacter("squirrel"))
        advanceUntilIdle()

        assertEquals("squirrel", viewModel.state.value.avatarConfig?.bodyId)
        coVerify {
            updateAvatar(
                profileId = any(),
                config = match { it.bodyId == "squirrel" },
            )
        }
    }

    @Test
    fun `select unowned character shows purchase dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // unicorn is not a starter character
        viewModel.onIntent(AvatarClosetIntent.SelectCharacter("unicorn"))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.showPurchaseDialog)
        assertEquals("char_unicorn", viewModel.state.value.showPurchaseDialog?.id)
    }

    @Test
    fun `select unowned character does not change bodyId`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(AvatarClosetIntent.SelectCharacter("dragon"))
        advanceUntilIdle()

        assertEquals("bunny", viewModel.state.value.avatarConfig?.bodyId)
    }

    @Test
    fun `request purchase shows purchase dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val unicorn = RewardCatalog.getCharacterItem("unicorn")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(unicorn))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.showPurchaseDialog)
        assertEquals("char_unicorn", viewModel.state.value.showPurchaseDialog?.id)
    }

    @Test
    fun `dismiss purchase dialog clears dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val unicorn = RewardCatalog.getCharacterItem("unicorn")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(unicorn))
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.showPurchaseDialog)

        viewModel.onIntent(AvatarClosetIntent.DismissPurchaseDialog)
        advanceUntilIdle()

        assertNull(viewModel.state.value.showPurchaseDialog)
    }

    @Test
    fun `confirm purchase success auto-equips character and emits effect`() = runTest {
        coEvery { purchaseItem(any(), any()) } returns PurchaseResult.Success

        val viewModel = createViewModel()
        advanceUntilIdle()

        val dragon = RewardCatalog.getCharacterItem("dragon")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(dragon))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(AvatarClosetIntent.ConfirmPurchase)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AvatarClosetEffect.PurchaseSuccess)
            assertEquals("Dragon", (effect as AvatarClosetEffect.PurchaseSuccess).itemName)
        }

        // Character should be auto-equipped
        assertEquals("dragon", viewModel.state.value.avatarConfig?.bodyId)
        // Dialog should be dismissed
        assertNull(viewModel.state.value.showPurchaseDialog)
    }

    @Test
    fun `confirm purchase insufficient points shows error`() = runTest {
        coEvery { purchaseItem(any(), any()) } returns PurchaseResult.InsufficientPoints(needed = 30)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val dragon = RewardCatalog.getCharacterItem("dragon")!!
        viewModel.onIntent(AvatarClosetIntent.RequestPurchase(dragon))
        advanceUntilIdle()

        viewModel.onIntent(AvatarClosetIntent.ConfirmPurchase)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.purchaseError)
        assertTrue(viewModel.state.value.purchaseError!!.contains("30"))
        // Dialog should still be showing
        assertNotNull(viewModel.state.value.showPurchaseDialog)
    }

    @Test
    fun `init with null avatar config uses default and finishes loading`() = runTest {
        every { getAvatarConfig(any()) } returns flowOf(null)
        every { rewardsRepository.getOwnedRewards(any()) } returns flowOf(emptyList())
        every { getTotalPoints(any()) } returns flowOf(100L)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(
            false,
            state.isLoading,
            "isLoading must become false even when avatar config is null",
        )
        assertNotNull(
            state.avatarConfig,
            "avatarConfig must be non-null (default) when use case returns null",
        )
        assertEquals(
            AvatarConfig.default(),
            state.avatarConfig,
            "avatarConfig must be the default config when use case returns null",
        )
        assertEquals(100L, state.starBalance)
    }

    @Test
    fun `free starter characters are selectable without purchase`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        listOf("bunny", "squirrel", "dog").forEach { bodyId ->
            viewModel.onIntent(AvatarClosetIntent.SelectCharacter(bodyId))
            advanceUntilIdle()

            assertEquals(
                bodyId,
                viewModel.state.value.avatarConfig?.bodyId,
                "Starter character $bodyId should be directly selectable",
            )
            assertNull(
                viewModel.state.value.showPurchaseDialog,
                "No purchase dialog for free character $bodyId",
            )
        }
    }
}
