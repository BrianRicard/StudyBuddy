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
import kotlin.random.Random
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
import org.junit.jupiter.api.Assertions.assertTrue
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

    private val loudSamples = FloatArray(AudioRecorder.SAMPLE_RATE) {
        // ~0.3 RMS: clearly "the child spoke".
        if (Random(it).nextBoolean()) 0.3f else -0.3f
    }
    private val silentSamples = FloatArray(AudioRecorder.SAMPLE_RATE) { 0.0001f }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            conjugationRepository.recordStepResult(any(), any(), any(), any(), any())
        } returns StepResultOutcome(firstCompletion = true, newBest = true)
        every { settingsRepository.getWhisperModel() } returns flowOf("ggml-tiny.bin")
        // Default: no downloaded model, so the mic path verifies loudness only.
        every { modelDownloadManager.bestAvailableModel(any()) } returns null
        every { audioRecorder.stopRecording() } returns loudSamples
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

    private fun grantedViewModel() = createViewModel().also {
        it.onIntent(SpeakIntent.PermissionSeeded(granted = true))
    }

    @Test
    fun `without a whisper model spoken words are not scored`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.whisperScoring)
    }

    @Test
    fun `recording that contains speech is accepted and awards points`() = runTest {
        val viewModel = grantedViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SpeakIntent.StartRecording)
        advanceUntilIdle()
        viewModel.onIntent(SpeakIntent.StopRecording)
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
    fun `a silent recording is not accepted — the free pass is closed`() = runTest {
        every { audioRecorder.stopRecording() } returns silentSamples
        val viewModel = grantedViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SpeakIntent.StartRecording)
        advanceUntilIdle()
        viewModel.onIntent(SpeakIntent.StopRecording)
        advanceUntilIdle()

        assertEquals(SpeakPhase.ENCOURAGE, viewModel.state.value.phase)
        assertEquals(0, viewModel.state.value.spokenCount)
        coVerify(exactly = 0) { awardPointsUseCase(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `an instant tap with no audio is rejected`() = runTest {
        every { audioRecorder.stopRecording() } returns FloatArray(0)
        val viewModel = grantedViewModel()
        advanceUntilIdle()

        viewModel.onIntent(SpeakIntent.StartRecording)
        advanceUntilIdle()
        viewModel.onIntent(SpeakIntent.StopRecording)
        advanceUntilIdle()

        assertEquals(SpeakPhase.ENCOURAGE, viewModel.state.value.phase)
    }

    @Test
    fun `speaking all six forms completes the step`() = runTest {
        val viewModel = grantedViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            repeat(6) {
                viewModel.onIntent(SpeakIntent.StartRecording)
                advanceUntilIdle()
                viewModel.onIntent(SpeakIntent.StopRecording)
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
    fun `the whisper path scores the transcription and awards spoken points`() = runTest {
        every { modelDownloadManager.bestAvailableModel(any()) } returns
            com.studybuddy.shared.whisper.WhisperModel.TINY
        every { modelDownloadManager.getModelPath(any()) } returns "/models/tiny.bin"
        coEvery { whisperEngine.initialize(any()) } returns Result.success(Unit)
        coEvery { whisperEngine.transcribe(any(), any(), any()) } returns
            Result.success(com.studybuddy.shared.whisper.TranscriptionResult("je suis", emptyList()))

        val viewModel = grantedViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.whisperScoring)

        viewModel.onIntent(SpeakIntent.StartRecording)
        advanceUntilIdle()
        viewModel.onIntent(SpeakIntent.StopRecording)
        advanceUntilIdle()

        assertEquals(SpeakPhase.HEARD, viewModel.state.value.phase)
        coVerify {
            awardPointsUseCase(
                profileId = "default",
                basePoints = PointValues.CONJUGATION_FORM_SPOKEN,
                streak = 0,
                source = any(),
                reason = any(),
            )
        }
    }

    @Test
    fun `recording without permission asks for it`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(SpeakIntent.StartRecording)
            advanceUntilIdle()
            assertEquals(SpeakEffect.RequestAudioPermission, awaitItem())
        }
    }

    @Test
    fun `self-report is only allowed after mic permission is denied`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // With permission not denied, a self-report tap does nothing.
        viewModel.onIntent(SpeakIntent.ConfirmWithoutMic)
        advanceUntilIdle()
        assertEquals(SpeakPhase.IDLE, viewModel.state.value.phase)

        viewModel.onIntent(SpeakIntent.AudioPermissionResult(granted = false))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showPermissionHint)

        viewModel.onIntent(SpeakIntent.ConfirmWithoutMic)
        advanceUntilIdle()
        assertEquals(SpeakPhase.HEARD, viewModel.state.value.phase)
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
    fun `a whisper transcription failure encourages instead of crashing`() = runTest {
        every { modelDownloadManager.bestAvailableModel(any()) } returns
            com.studybuddy.shared.whisper.WhisperModel.TINY
        every { modelDownloadManager.getModelPath(any()) } returns "/models/tiny.bin"
        coEvery { whisperEngine.initialize(any()) } returns Result.success(Unit)
        coEvery { whisperEngine.transcribe(any(), any(), any()) } returns
            Result.failure(RuntimeException("transcription failed"))

        val viewModel = grantedViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.whisperScoring)

        viewModel.onIntent(SpeakIntent.StartRecording)
        advanceUntilIdle()
        viewModel.onIntent(SpeakIntent.StopRecording)
        advanceUntilIdle()

        assertEquals(SpeakPhase.ENCOURAGE, viewModel.state.value.phase)
        coVerify(exactly = 0) { awardPointsUseCase(any(), any(), any(), any(), any()) }
    }
}
