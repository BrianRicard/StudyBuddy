package com.studybuddy.feature.rewards

import app.cash.turbine.test
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.avatar.PurchaseItemUseCase
import com.studybuddy.core.domain.usecase.avatar.PurchaseResult
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
class RewardsShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val rewardsRepository: RewardsRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val purchaseItem: PurchaseItemUseCase = mockk()
    private val getTotalPoints: GetTotalPointsUseCase = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { rewardsRepository.getOwnedRewards(any()) } returns flowOf(emptyList())
        every { getTotalPoints(any()) } returns flowOf(300L)
        every { settingsRepository.getSelectedTheme() } returns flowOf("sunset")
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = RewardsShopViewModel(
        rewardsRepository = rewardsRepository,
        settingsRepository = settingsRepository,
        purchaseItemUseCase = purchaseItem,
        getTotalPointsUseCase = getTotalPoints,
    )

    @Test
    fun `init loads star balance and owned items`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(300L, state.starBalance)
        assertTrue(state.ownedItemIds.containsAll(RewardCatalog.starterItemIds))
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `init loads active theme`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("sunset", viewModel.state.value.activeTheme)
    }

    @Test
    fun `select tab updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(RewardsShopIntent.SelectTab(RewardsTab.THEMES))
        advanceUntilIdle()

        assertEquals(RewardsTab.THEMES, viewModel.state.value.selectedTab)
    }

    @Test
    fun `request purchase shows dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val item = RewardCatalog.getItemById("theme_ocean")!!
        viewModel.onIntent(RewardsShopIntent.RequestPurchase(item))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.showPurchaseDialog)
        assertEquals("theme_ocean", viewModel.state.value.showPurchaseDialog?.id)
    }

    @Test
    fun `dismiss dialog clears purchase dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val item = RewardCatalog.getItemById("theme_ocean")!!
        viewModel.onIntent(RewardsShopIntent.RequestPurchase(item))
        advanceUntilIdle()

        viewModel.onIntent(RewardsShopIntent.DismissDialog)
        advanceUntilIdle()

        assertNull(viewModel.state.value.showPurchaseDialog)
        assertNull(viewModel.state.value.purchaseError)
    }

    @Test
    fun `confirm purchase success clears dialog and emits effect`() = runTest {
        coEvery { purchaseItem(any(), any()) } returns PurchaseResult.Success

        val viewModel = createViewModel()
        advanceUntilIdle()

        val item = RewardCatalog.getItemById("theme_ocean")!!
        viewModel.onIntent(RewardsShopIntent.RequestPurchase(item))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(RewardsShopIntent.ConfirmPurchase)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is RewardsShopEffect.PurchaseSuccess)
            assertEquals("Ocean", (effect as RewardsShopEffect.PurchaseSuccess).itemName)
        }

        assertNull(viewModel.state.value.showPurchaseDialog)
    }

    @Test
    fun `confirm purchase insufficient points shows error`() = runTest {
        coEvery { purchaseItem(any(), any()) } returns PurchaseResult.InsufficientPoints(needed = 50)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val item = RewardCatalog.getItemById("theme_galaxy")!!
        viewModel.onIntent(RewardsShopIntent.RequestPurchase(item))
        advanceUntilIdle()

        viewModel.onIntent(RewardsShopIntent.ConfirmPurchase)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.purchaseError)
        assertTrue(viewModel.state.value.purchaseError!!.contains("50"))
    }

    @Test
    fun `activate theme calls settings repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(RewardsShopIntent.ActivateTheme("ocean"))
        advanceUntilIdle()

        coVerify { settingsRepository.setSelectedTheme("ocean") }
    }

    @Test
    fun `activate theme emits ThemeChanged effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(RewardsShopIntent.ActivateTheme("forest"))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is RewardsShopEffect.ThemeChanged)
            assertEquals("forest", (effect as RewardsShopEffect.ThemeChanged).themeId)
        }
    }

    @Test
    fun `equip title updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(RewardsShopIntent.EquipTitle("title_rising_star"))
        advanceUntilIdle()

        assertEquals("title_rising_star", viewModel.state.value.equippedTitle)
    }

    @Test
    fun `default tab is AVATAR`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(RewardsTab.AVATAR, viewModel.state.value.selectedTab)
    }
}
