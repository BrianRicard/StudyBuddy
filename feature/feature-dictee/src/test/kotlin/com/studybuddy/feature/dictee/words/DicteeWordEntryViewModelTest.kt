package com.studybuddy.feature.dictee.words

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.usecase.dictee.AddWordUseCase
import com.studybuddy.shared.tts.TtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class DicteeWordEntryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dicteeRepository: DicteeRepository = mockk(relaxed = true)
    private val addWordUseCase: AddWordUseCase = mockk(relaxed = true)
    private val ttsManager: TtsManager = mockk(relaxed = true)

    private val testList = DicteeList(
        id = "list1",
        profileId = "p1",
        title = "Animals",
        language = "fr",
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private val testWords = listOf(
        DicteeWord(id = "w1", listId = "list1", word = "chat"),
        DicteeWord(id = "w2", listId = "list1", word = "chien"),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { dicteeRepository.getList("list1") } returns flowOf(testList)
        every { dicteeRepository.getWordsForList("list1") } returns flowOf(testWords)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DicteeWordEntryViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("listId" to "list1"))
        return DicteeWordEntryViewModel(
            savedStateHandle = savedStateHandle,
            dicteeRepository = dicteeRepository,
            addWordUseCase = addWordUseCase,
            ttsManager = ttsManager,
        )
    }

    @Test
    fun `load words populates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.words.size)
        assertEquals("Animals", state.list?.title)
    }

    @Test
    fun `add word calls use case and clears input`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeWordEntryIntent.UpdateNewWordText("oiseau"))
        advanceUntilIdle()
        viewModel.onIntent(DicteeWordEntryIntent.AddWord)
        advanceUntilIdle()

        coVerify { addWordUseCase(match { it.word == "oiseau" && it.listId == "list1" }) }
        assertEquals("", viewModel.state.value.newWordText)
    }

    @Test
    fun `add blank word does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeWordEntryIntent.UpdateNewWordText("  "))
        viewModel.onIntent(DicteeWordEntryIntent.AddWord)
        advanceUntilIdle()

        coVerify(exactly = 0) { addWordUseCase(any()) }
    }

    @Test
    fun `delete word calls repository and emits undo snackbar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeWordEntryIntent.DeleteWord("w1"))
            advanceUntilIdle()

            coVerify { dicteeRepository.deleteWord("w1") }

            val effect = awaitItem()
            assertTrue(effect is DicteeWordEntryEffect.ShowUndoSnackbar)
            assertEquals("chat", (effect as DicteeWordEntryEffect.ShowUndoSnackbar).word.word)
        }
    }

    @Test
    fun `toggle edit mode flips state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isEditMode)

        viewModel.onIntent(DicteeWordEntryIntent.ToggleEditMode)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isEditMode)

        viewModel.onIntent(DicteeWordEntryIntent.ToggleEditMode)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isEditMode)
    }

    @Test
    fun `play word calls TTS with correct locale`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeWordEntryIntent.PlayWord("chat"))
        advanceUntilIdle()

        verify { ttsManager.speak("chat", java.util.Locale.FRENCH) }
    }

    @Test
    fun `start practice emits navigate effect when words exist`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeWordEntryIntent.StartPractice)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeWordEntryEffect.NavigateToPractice)
            assertEquals("list1", (effect as DicteeWordEntryEffect.NavigateToPractice).listId)
        }
    }

    @Test
    fun `start practice with no words does not emit effect`() = runTest {
        every { dicteeRepository.getWordsForList("list1") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeWordEntryIntent.StartPractice)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `add word failure sets error state`() = runTest {
        coEvery { addWordUseCase(any()) } throws RuntimeException("DB error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeWordEntryIntent.UpdateNewWordText("oiseau"))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(DicteeWordEntryIntent.AddWord)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DicteeWordEntryEffect.ShowError)

            val state = viewModel.state.value
            assertEquals("Could not add word. Please try again.", state.errorMessage)
        }
    }

    @Test
    fun `dismiss error clears error message`() = runTest {
        coEvery { addWordUseCase(any()) } throws RuntimeException("DB error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DicteeWordEntryIntent.UpdateNewWordText("test"))
        viewModel.onIntent(DicteeWordEntryIntent.AddWord)
        advanceUntilIdle()

        assertEquals("Could not add word. Please try again.", viewModel.state.value.errorMessage)

        viewModel.onIntent(DicteeWordEntryIntent.DismissError)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.errorMessage)
    }
}
