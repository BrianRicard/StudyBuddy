package com.studybuddy.feature.dictee.list

import app.cash.turbine.test
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.dictee.GetDicteeListsUseCase
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
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DicteeListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dicteeRepository: DicteeRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val getDicteeListsUseCase: GetDicteeListsUseCase = mockk()

    private val testLists = listOf(
        DicteeList(
            id = "list1",
            profileId = "p1",
            title = "Animals",
            language = "fr",
            wordCount = 5,
            masteredCount = 3,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        ),
        DicteeList(
            id = "list2",
            profileId = "p1",
            title = "Colors",
            language = "en",
            wordCount = 8,
            masteredCount = 8,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        ),
    )

    private val testWords = listOf(
        DicteeWord(id = "w1", listId = "list1", word = "chat"),
        DicteeWord(id = "w2", listId = "list1", word = "chien"),
        DicteeWord(id = "w3", listId = "list1", word = "oiseau"),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.isDicteeSeeded() } returns flowOf(true)
        every { getDicteeListsUseCase(any()) } returns flowOf(testLists)
        every { dicteeRepository.getWordsForList("list1") } returns flowOf(testWords)
        every { dicteeRepository.getWordsForList("list2") } returns flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DicteeListViewModel = DicteeListViewModel(
        getDicteeListsUseCase = getDicteeListsUseCase,
        dicteeRepository = dicteeRepository,
        settingsRepository = settingsRepository,
    )

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `load lists populates state with word previews`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.items.size)
        assertEquals("Animals", state.items[0].list.title)
        assertEquals(listOf("chat", "chien", "oiseau"), state.items[0].wordPreview)
        assertTrue(state.items[1].wordPreview.isEmpty())
    }

    @Test
    fun `search filters items by title`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.UpdateSearch("anim"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("anim", state.searchQuery)
        assertEquals(1, state.filteredItems.size)
        assertEquals("Animals", state.filteredItems[0].list.title)
    }

    @Test
    fun `search filters items by word preview`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.UpdateSearch("chat"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.filteredItems.size)
        assertEquals("Animals", state.filteredItems[0].list.title)
    }

    @Test
    fun `delete list calls repository and emits undo snackbar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.DeleteList("list1"))
            advanceUntilIdle()

            coVerify { dicteeRepository.deleteList("list1") }

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.ShowUndoSnackbar)
            assertEquals("Animals", (effect as DicteeListEffect.ShowUndoSnackbar).list.title)
        }
    }

    @Test
    fun `undo delete calls createList`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.UndoDelete(testLists[0]))
        advanceUntilIdle()

        coVerify { dicteeRepository.createList(testLists[0]) }
    }

    @Test
    fun `open list emits navigate to practice effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.OpenList("list1"))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.NavigateToPractice)
            assertEquals("list1", (effect as DicteeListEffect.NavigateToPractice).listId)
        }
    }

    @Test
    fun `open empty list shows toast instead of navigating`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            // list2 has wordCount = 8 but the mock returns empty words list.
            // However wordCount is set on the DicteeList model itself (8).
            // We need a list with wordCount = 0 to trigger the guard.
            // Let's use a fresh list with wordCount = 0.
        }

        // Create a ViewModel with a list that has 0 words
        val emptyList = DicteeList(
            id = "empty",
            profileId = "p1",
            title = "Empty List",
            language = "fr",
            wordCount = 0,
            masteredCount = 0,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        every { getDicteeListsUseCase(any()) } returns flowOf(listOf(emptyList))
        every { dicteeRepository.getWordsForList("empty") } returns flowOf(emptyList())

        val viewModel2 = createViewModel()
        advanceUntilIdle()

        viewModel2.effects.test {
            viewModel2.onIntent(DicteeListIntent.OpenList("empty"))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.ShowToast)
        }
    }

    @Test
    fun `edit list emits navigate to edit effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.EditList("list1"))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.NavigateToEdit)
            assertEquals("list1", (effect as DicteeListEffect.NavigateToEdit).listId)
        }
    }

    // ── Challenge / multi-select mode ─────────────────────────────────────────

    @Test
    fun `toggle select mode enables selection mode`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSelectMode)
        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSelectMode)
        assertTrue(viewModel.state.value.selectedListIds.isEmpty())
    }

    @Test
    fun `toggle select mode off clears selection`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)
        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list1"))
        viewModel.onIntent(DicteeListIntent.ToggleSelectMode) // cancel
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSelectMode)
        assertTrue(viewModel.state.value.selectedListIds.isEmpty())
    }

    @Test
    fun `toggle list selection adds and removes list from selection`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)
        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list1"))
        advanceUntilIdle()

        assertTrue("list1" in viewModel.state.value.selectedListIds)

        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list1"))
        advanceUntilIdle()

        assertFalse("list1" in viewModel.state.value.selectedListIds)
    }

    @Test
    fun `canStartChallenge is true only when 2 or more lists selected`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canStartChallenge)

        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list1"))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canStartChallenge)

        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list2"))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.canStartChallenge)
    }

    @Test
    fun `start challenge emits NavigateToChallenge with selected ids`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)
        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list1"))
        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list2"))

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.StartChallenge)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.NavigateToChallenge)
            val ids = (effect as DicteeListEffect.NavigateToChallenge).listIds
            assertEquals(2, ids.size)
            assertTrue("list1" in ids)
            assertTrue("list2" in ids)
        }

        assertFalse(viewModel.state.value.isSelectMode)
        assertTrue(viewModel.state.value.selectedListIds.isEmpty())
    }

    @Test
    fun `start challenge with fewer than 2 lists does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)
        viewModel.onIntent(DicteeListIntent.ToggleListSelection("list1"))

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.StartChallenge)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    // ── Default list seeding ──────────────────────────────────────────────────

    @Test
    fun `seeds default lists on first launch`() = runTest {
        every { settingsRepository.isDicteeSeeded() } returns flowOf(false)

        createViewModel()
        advanceUntilIdle()

        coVerify { dicteeRepository.seedDefaultLists(any()) }
        coVerify { settingsRepository.setDicteeSeeded(true) }
    }

    @Test
    fun `skips seeding when already seeded`() = runTest {
        every { settingsRepository.isDicteeSeeded() } returns flowOf(true)

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { dicteeRepository.seedDefaultLists(any()) }
    }

    @Test
    fun `open list in select mode toggles selection instead of navigating`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.OpenList("list1"))
            advanceUntilIdle()

            // Should NOT emit NavigateToPractice
            expectNoEvents()
        }

        assertTrue("list1" in viewModel.state.value.selectedListIds)
    }
}
