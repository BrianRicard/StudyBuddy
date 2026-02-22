package com.studybuddy.feature.dictee.list

import app.cash.turbine.test
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.repository.DicteeRepository
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
}
