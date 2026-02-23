package com.studybuddy.feature.dictee.list

import app.cash.turbine.test
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.usecase.dictee.GetDicteeListsUseCase
import com.studybuddy.core.domain.usecase.dictee.ImportWordListUseCase
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
    private val getDicteeListsUseCase: GetDicteeListsUseCase = mockk()
    private val importWordListUseCase: ImportWordListUseCase = mockk()

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

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getDicteeListsUseCase(any()) } returns flowOf(testLists)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DicteeListViewModel = DicteeListViewModel(
        getDicteeListsUseCase = getDicteeListsUseCase,
        dicteeRepository = dicteeRepository,
        importWordListUseCase = importWordListUseCase,
    )

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = createViewModel()
        // Before idle, state should have isLoading true by default
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `load lists populates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.lists.size)
        assertEquals("Animals", state.lists[0].title)
    }

    @Test
    fun `show create dialog updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ShowCreateDialog)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showCreateDialog)
    }

    @Test
    fun `dismiss create dialog updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ShowCreateDialog)
        advanceUntilIdle()
        viewModel.onIntent(DicteeListIntent.DismissCreateDialog)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showCreateDialog)
    }

    @Test
    fun `create list calls repository and closes dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ShowCreateDialog)
        viewModel.onIntent(DicteeListIntent.UpdateNewListTitle("My New List"))
        viewModel.onIntent(DicteeListIntent.UpdateNewListLanguage("de"))
        viewModel.onIntent(DicteeListIntent.CreateList)
        advanceUntilIdle()

        coVerify {
            dicteeRepository.createList(
                match {
                    it.title == "My New List" && it.language == "de"
                },
            )
        }
        assertFalse(viewModel.state.value.showCreateDialog)
    }

    @Test
    fun `create list with blank title does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ShowCreateDialog)
        viewModel.onIntent(DicteeListIntent.UpdateNewListTitle("  "))
        viewModel.onIntent(DicteeListIntent.CreateList)
        advanceUntilIdle()

        coVerify(exactly = 0) { dicteeRepository.createList(any()) }
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
    fun `open list emits navigate effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.OpenList("list1"))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.NavigateToWords)
            assertEquals("list1", (effect as DicteeListEffect.NavigateToWords).listId)
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
    fun `start challenge emits NavigateToChallenge with selected ids and exits select mode`() = runTest {
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

    @Test
    fun `open list in select mode toggles selection instead of navigating`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeListIntent.ToggleSelectMode)

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.OpenList("list1"))
            advanceUntilIdle()

            // Should NOT emit NavigateToWords
            expectNoEvents()
        }

        assertTrue("list1" in viewModel.state.value.selectedListIds)
    }

    // ── Import CSV tests ─────────────────────────────────────────────────────

    @Test
    fun `import csv calls use case and emits success toast`() = runTest {
        val csv = "List,Language,Word\nAnimals,fr,chat\nAnimals,fr,chien"
        coEvery { importWordListUseCase(csv, any()) } returns 2
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.ImportCsv(csv))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.ShowToast)
            assertEquals("Imported 2 words", (effect as DicteeListEffect.ShowToast).message)
        }

        assertFalse(viewModel.state.value.isImporting)
    }

    @Test
    fun `import csv with zero words emits no words found toast`() = runTest {
        val csv = "List,Language,Word"
        coEvery { importWordListUseCase(csv, any()) } returns 0
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.ImportCsv(csv))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.ShowToast)
            assertEquals("No words found in file", (effect as DicteeListEffect.ShowToast).message)
        }
    }

    @Test
    fun `import csv error emits failure toast`() = runTest {
        val csv = "bad data"
        coEvery { importWordListUseCase(csv, any()) } throws RuntimeException("parse error")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeListIntent.ImportCsv(csv))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeListEffect.ShowToast)
            assertTrue(
                (effect as DicteeListEffect.ShowToast).message.contains("Import failed"),
            )
        }

        assertFalse(viewModel.state.value.isImporting)
    }
}
