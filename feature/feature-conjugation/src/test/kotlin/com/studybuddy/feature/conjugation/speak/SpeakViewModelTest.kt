package com.studybuddy.feature.conjugation.speak

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.repository.StepResultOutcome
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import com.studybuddy.shared.whisper.AudioRecorder
import com.studybuddy.shared.whisper.ModelDownloadManager
import com.studybuddy.shared.whisper.WhisperEngine
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
class SpeakViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val conjugationRepository: ConjugationRepository = mockk()
    private val awardPointsUseCase: AwardPointsUseCase = mockk(relaxed = true)
    private val ttsManager: TtsManager = mockk(relaxed = true)
    private val whisperEngine: WhisperEngine = mockk(relaxed = true)
    private val audioRecorder: AudioRecorder = mockk(relaxed = true)
    private val modelDownloadManager: ModelDownloadManager = mockk()
    private val settingsRepository: SettingsRepository = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            conjugationRepository.recordStepResult(any(), any(), any(), any(), any())
        } returns StepResultOutcome(firstCompletion = true, newBest = true)
        every { settingsRepository.getWhisperModel() } returns flowOf("ggml-tiny.bin")
        // No downloaded model: the screen falls back to echo mode.
        every { modelDownloadManager.bestAvailableModel(any()) } returns null
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SpeakViewModel(
        savedStateHandle = SavedStateHandle(mapOf("stageId" to "etre")),
        ttsManager = ttsManager,
        whisperEngine = whisperEngine,
        audioRecorder = audioRecorder,
        modelDownloadManager = modelDownloadManager,
        settingsRepository = settingsRepository,
        conjugationRepository = conjugationRepository,
        awardPointsUseCase = awardPointsUseCase,
    )

    @Test
    fun `without a whisper model the screen stays in echo mode`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isMicMode)
    }

    @Test
    fun `echo confirmation praises and awards the echo points`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SpeakIntent.ConfirmEcho)
        advanceUntilIdle()

        assertEquals(SpeakPhase.HEARD, viewModel.state.value.phase)
        assertEquals(1, viewModel.state.value.spokenCount)
        coVerify {
            awardPointsUseCase(
                profileId = "default",
                basePoints = PointValues.CONJUGATION_FORM_ECHOED,
                streak = 0,
                source = any(),
                reason = any(),
            )
        }
    }

    @Test
    fun `echoing all six forms completes the step`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            repeat(6) {
                viewModel.onIntent(SpeakIntent.ConfirmEcho)
                advanceUntilIdle()
                viewModel.onIntent(SpeakIntent.Next)
                advanceUntilIdle()
            }
            assertEquals(SpeakEffect.Completed, awaitItem())
        }

        coVerify {
            conjugationRepository.recordStepResult(
                profileId = "default",
                stageId = "etre",
                step = ConjugationStep.SPEAK,
                correct = 6,
                total = 6,
            )
        }
    }

    @Test
    fun `recording without permission asks for it in mic mode`() = runTest {
        every { modelDownloadManager.bestAvailableModel(any()) } returns
            com.studybuddy.shared.whisper.WhisperModel.TINY
        every { modelDownloadManager.getModelPath(any()) } returns "/models/tiny.bin"
        coEvery { whisperEngine.initialize(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(SpeakIntent.StartRecording)
            advanceUntilIdle()
            assertEquals(SpeakEffect.RequestAudioPermission, awaitItem())
        }
    }
}
