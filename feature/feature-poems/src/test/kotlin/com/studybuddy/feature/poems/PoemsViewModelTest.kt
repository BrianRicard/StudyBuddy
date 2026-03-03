package com.studybuddy.feature.poems

import app.cash.turbine.test
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.domain.usecase.poem.GetFavouritePoemsUseCase
import com.studybuddy.core.domain.usecase.poem.GetPoemsUseCase
import com.studybuddy.core.domain.usecase.poem.GetUserPoemsUseCase
import com.studybuddy.core.domain.usecase.poem.RefreshPoemsUseCase
import com.studybuddy.core.domain.usecase.poem.ToggleFavouriteUseCase
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PoemsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getPoemsUseCase: GetPoemsUseCase = mockk()
    private val getFavouritePoemsUseCase: GetFavouritePoemsUseCase = mockk()
    private val getUserPoemsUseCase: GetUserPoemsUseCase = mockk()
    private val refreshPoemsUseCase: RefreshPoemsUseCase = mockk()
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase = mockk()

    private val testPoems = listOf(
        Poem(
            id = "1",
            title = "Test Poem",
            author = "Author",
            lines = listOf("Line 1"),
            language = "en",
            source = PoemSource.API,
        ),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getPoemsUseCase("en") } returns flowOf(testPoems)
        every { getFavouritePoemsUseCase(any()) } returns flowOf(emptyList())
        every { getUserPoemsUseCase(any()) } returns flowOf(emptyList())
        coEvery { refreshPoemsUseCase(any()) } returns Unit
        coEvery { toggleFavouriteUseCase(any(), any(), any()) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = PoemsViewModel(
        getPoemsUseCase = getPoemsUseCase,
        getFavouritePoemsUseCase = getFavouritePoemsUseCase,
        getUserPoemsUseCase = getUserPoemsUseCase,
        refreshPoemsUseCase = refreshPoemsUseCase,
        toggleFavouriteUseCase = toggleFavouriteUseCase,
    )

    @Test
    fun `init loads poems and sets loading to false`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(testPoems, state.poems)
    }

    @Test
    fun `select language reloads poems`() = runTest {
        val frenchPoems = listOf(
            Poem(
                id = "2",
                title = "Poème",
                author = "Auteur",
                lines = listOf("Ligne 1"),
                language = "fr",
                source = PoemSource.BUNDLED,
            ),
        )
        every { getPoemsUseCase("fr") } returns flowOf(frenchPoems)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PoemsIntent.SelectLanguage("fr"))
        advanceUntilIdle()

        assertEquals("fr", viewModel.state.value.selectedLanguage)
        assertEquals(frenchPoems, viewModel.state.value.poems)
    }

    @Test
    fun `select tab updates selectedTab`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PoemsIntent.SelectTab(PoemsTab.FAVOURITES))
        assertEquals(PoemsTab.FAVOURITES, viewModel.state.value.selectedTab)
    }

    @Test
    fun `open poem emits NavigateToDetail effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(PoemsIntent.OpenPoem("poem-123"))
            val effect = awaitItem()
            assertEquals(PoemsEffect.NavigateToDetail("poem-123"), effect)
        }
    }

    @Test
    fun `toggle favourite calls use case`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PoemsIntent.ToggleFavourite(testPoems[0]))
        advanceUntilIdle()

        coVerify { toggleFavouriteUseCase("1", "API", any()) }
    }

    @Test
    fun `displayPoems returns poems on BROWSE tab`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(testPoems, viewModel.state.value.displayPoems)
    }

    @Test
    fun `displayPoems returns favourites on FAVOURITES tab`() = runTest {
        val favPoems = listOf(
            Poem(
                id = "fav-1",
                title = "Fav Poem",
                author = "Author",
                lines = listOf("Fav line"),
                language = "en",
                source = PoemSource.API,
                isFavourite = true,
            ),
        )
        every { getFavouritePoemsUseCase(any()) } returns flowOf(favPoems)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PoemsIntent.SelectTab(PoemsTab.FAVOURITES))
        assertEquals(favPoems, viewModel.state.value.displayPoems)
    }
}
