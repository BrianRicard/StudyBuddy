package com.studybuddy.feature.poems

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.domain.usecase.poem.GetPoemByIdUseCase
import com.studybuddy.core.domain.usecase.poem.SaveReadingSessionUseCase
import com.studybuddy.core.domain.usecase.poem.ToggleFavouriteUseCase
import com.studybuddy.shared.whisper.AudioRecorder
import com.studybuddy.shared.whisper.ModelDownloadManager
import com.studybuddy.shared.whisper.WhisperEngine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PoemDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getPoemByIdUseCase: GetPoemByIdUseCase = mockk()
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase = mockk()
    private val saveReadingSessionUseCase: SaveReadingSessionUseCase = mockk()
    private val whisperEngine: WhisperEngine = mockk(relaxed = true)
    private val audioRecorder: AudioRecorder = mockk(relaxed = true)
    private val modelDownloadManager: ModelDownloadManager = mockk(relaxed = true)

    private val testPoem = Poem(
        id = "poem-1",
        title = "Test Poem",
        author = "Test Author",
        lines = listOf("The cat sat", "on the mat"),
        language = "en",
        source = PoemSource.BUNDLED,
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getPoemByIdUseCase(any()) } returns testPoem
        coEvery { toggleFavouriteUseCase(any(), any(), any()) } returns Unit
        coEvery { saveReadingSessionUseCase(any()) } returns Unit
        every { modelDownloadManager.getModelPath(any()) } returns null
        every { whisperEngine.isInitialized } returns false
        coEvery { modelDownloadManager.downloadModel(any(), any()) } returns Result.failure(Exception("test"))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PoemDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("poemId" to "poem-1"))
        return PoemDetailViewModel(
            savedStateHandle = savedStateHandle,
            getPoemByIdUseCase = getPoemByIdUseCase,
            toggleFavouriteUseCase = toggleFavouriteUseCase,
            saveReadingSessionUseCase = saveReadingSessionUseCase,
            whisperEngine = whisperEngine,
            audioRecorder = audioRecorder,
            modelDownloadManager = modelDownloadManager,
        )
    }

    @Test
    fun `init loads poem and flattens words`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.poem)
        assertEquals("Test Poem", state.poem?.title)
        // "The cat sat" + "on the mat" = 6 words
        assertEquals(6, state.words.size)
        assertEquals("The", state.words[0].text)
        assertEquals("mat", state.words[5].text)
    }

    @Test
    fun `words have correct line and word indices`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val words = viewModel.state.value.words
        // Line 0: "The" "cat" "sat"
        assertEquals(0, words[0].lineIndex)
        assertEquals(0, words[1].lineIndex)
        assertEquals(0, words[2].lineIndex)
        // Line 1: "on" "the" "mat"
        assertEquals(1, words[3].lineIndex)
        assertEquals(1, words[4].lineIndex)
        assertEquals(1, words[5].lineIndex)
    }

    @Test
    fun `toggle favourite updates poem state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.poem!!.isFavourite)

        viewModel.onIntent(PoemDetailIntent.ToggleFavourite)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.poem!!.isFavourite)
    }

    @Test
    fun `start read aloud sets state and emits SpeakLine`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(PoemDetailIntent.StartReadAloud)
            val state = viewModel.state.value
            assertTrue(state.isReadingAloud)
            assertEquals(0, state.currentReadLine)

            val effect = awaitItem()
            assertTrue(effect is PoemDetailEffect.SpeakLine)
            assertEquals("The cat sat", (effect as PoemDetailEffect.SpeakLine).text)
        }
    }

    @Test
    fun `stop read aloud resets state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(PoemDetailIntent.StartReadAloud)
        advanceUntilIdle()

        viewModel.onIntent(PoemDetailIntent.StopReadAloud)
        val state = viewModel.state.value
        assertFalse(state.isReadingAloud)
        assertEquals(-1, state.currentReadLine)
    }

    @Test
    fun `start recording without model triggers download attempt`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PoemDetailIntent.StartRecording)
        advanceUntilIdle()

        // Download fails (mocked to return failure), so model is not loaded
        // but the download was attempted
        assertFalse(viewModel.state.value.isModelLoaded)
        coVerify { modelDownloadManager.downloadModel(any(), any()) }
    }

    @Test
    fun `start recording without permission emits RequestAudioPermission`() = runTest {
        every { modelDownloadManager.getModelPath(any()) } returns "/path/to/model"
        coEvery { whisperEngine.initialize(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()
        // Model loaded but no permission
        assertTrue(viewModel.state.value.isModelLoaded)
        assertFalse(viewModel.state.value.hasAudioPermission)

        viewModel.effects.test {
            viewModel.onIntent(PoemDetailIntent.StartRecording)
            val effect = awaitItem()
            assertTrue(effect is PoemDetailEffect.RequestAudioPermission)
        }
    }

    @Test
    fun `tap word while not recording emits SpeakWord not snackbar`() = runTest {
        // When not recording (default state), tapping a word should emit SpeakWord
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(RecordingState.IDLE, viewModel.state.value.recordingState)

        viewModel.effects.test {
            viewModel.onIntent(PoemDetailIntent.TapWord(2))
            val effect = awaitItem()
            assertTrue(effect is PoemDetailEffect.SpeakWord)
            assertEquals("sat", (effect as PoemDetailEffect.SpeakWord).text)
            assertEquals("en", effect.language)
        }
    }

    @Test
    fun `dismiss result sheet updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PoemDetailIntent.DismissResultSheet)
        assertFalse(viewModel.state.value.showResultSheet)
    }

    @Test
    fun `reset clears recording state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PoemDetailIntent.Reset)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(RecordingState.IDLE, state.recordingState)
        assertFalse(state.showResultSheet)
        state.words.forEach {
            assertEquals(
                com.studybuddy.feature.poems.detail.WordState.UNREAD,
                it.state,
            )
        }
        verify { audioRecorder.release() }
    }
}
