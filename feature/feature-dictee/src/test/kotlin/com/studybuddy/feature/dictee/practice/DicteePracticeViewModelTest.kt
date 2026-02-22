package com.studybuddy.feature.dictee.practice

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.dictee.CheckSpellingUseCase
import com.studybuddy.core.domain.usecase.dictee.GetPracticeWordsUseCase
import com.studybuddy.shared.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import com.studybuddy.shared.tts.TtsState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DicteePracticeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dicteeRepository: DicteeRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val ttsManager: TtsManager = mockk(relaxed = true)
    private val checkSpellingUseCase = CheckSpellingUseCase()
    private val getPracticeWordsUseCase: GetPracticeWordsUseCase = mockk()
    private val awardPointsUseCase: AwardPointsUseCase = mockk(relaxed = true)

    private val testWords = listOf(
        DicteeWord(id = "w1", listId = "list1", word = "maison"),
        DicteeWord(id = "w2", listId = "list1", word = "chat"),
        DicteeWord(id = "w3", listId = "list1", word = "école"),
    )

    private val testList = DicteeList(
        id = "list1",
        profileId = "p1",
        title = "Test List",
        language = "fr",
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { ttsManager.state } returns MutableStateFlow(TtsState.Ready)
        every { dicteeRepository.getList("list1") } returns flowOf(testList)
        every { getPracticeWordsUseCase("list1") } returns flowOf(testWords)
        coEvery { settingsRepository.isAccentStrict() } returns flowOf(false)
        coEvery { awardPointsUseCase(any(), any(), any(), any(), any()) } returns 10
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DicteePracticeViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("listId" to "list1"))
        return DicteePracticeViewModel(
            savedStateHandle = savedStateHandle,
            getPracticeWordsUseCase = getPracticeWordsUseCase,
            checkSpellingUseCase = checkSpellingUseCase,
            dicteeRepository = dicteeRepository,
            settingsRepository = settingsRepository,
            awardPointsUseCase = awardPointsUseCase,
            ttsManager = ttsManager,
        )
    }

    @Test
    fun `load words populates state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(3, state.words.size)
            assertEquals("Test List", state.listTitle)
            assertEquals(0, state.currentIndex)
        }

    @Test
    fun `submit correct answer sets feedback to Correct and increments streak`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.UpdateInput("maison"))
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state.feedback is Feedback.Correct)
            assertEquals(1, state.streak)
            assertTrue(state.sessionScore > 0)
        }

    @Test
    fun `submit incorrect answer sets feedback to Incorrect and resets streak`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            // First get a correct answer to build streak
            viewModel.onIntent(DicteePracticeIntent.UpdateInput("maison"))
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
            advanceUntilIdle()
            assertEquals(1, viewModel.state.value.streak)

            // Move to next word
            viewModel.onIntent(DicteePracticeIntent.NextWord)
            advanceUntilIdle()

            // Submit wrong answer
            viewModel.onIntent(DicteePracticeIntent.UpdateInput("wrong"))
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state.feedback is Feedback.Incorrect)
            assertEquals(0, state.streak)
        }

    @Test
    fun `toggle input mode switches between keyboard and handwriting`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(InputMode.KEYBOARD, viewModel.state.value.inputMode)

            viewModel.onIntent(DicteePracticeIntent.ToggleInputMode)
            advanceUntilIdle()
            assertEquals(InputMode.HANDWRITING, viewModel.state.value.inputMode)

            viewModel.onIntent(DicteePracticeIntent.ToggleInputMode)
            advanceUntilIdle()
            assertEquals(InputMode.KEYBOARD, viewModel.state.value.inputMode)
        }

    @Test
    fun `next word advances index and clears state`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.UpdateInput("maison"))
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.NextWord)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(1, state.currentIndex)
            assertEquals("", state.userInput)
            assertNull(state.feedback)
        }

    @Test
    fun `complete all words emits NavigateToResults`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                // Answer all 3 words
                repeat(3) {
                    viewModel.onIntent(DicteePracticeIntent.UpdateInput(testWords[it].word))
                    advanceUntilIdle()
                    viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
                    advanceUntilIdle()
                    viewModel.onIntent(DicteePracticeIntent.NextWord)
                    advanceUntilIdle()
                }

                // Collect effects - should include ShowPoints and NavigateToResults
                val effects = cancelAndConsumeRemainingEvents()
                assertTrue(
                    effects.filterIsInstance<app.cash.turbine.Event.Item<DicteePracticeEffect>>().any {
                        it.value is DicteePracticeEffect.NavigateToResults
                    },
                )
            }
        }

    @Test
    fun `show hint makes hint visible`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.hintVisible)

            viewModel.onIntent(DicteePracticeIntent.ShowHint)
            advanceUntilIdle()

            assertEquals(true, viewModel.state.value.hintVisible)
        }

    @Test
    fun `retry word clears input and feedback`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.UpdateInput("wrong"))
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.RetryWord)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("", state.userInput)
            assertNull(state.feedback)
        }

    @Test
    fun `correct answer awards points via use case`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.UpdateInput("maison"))
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
            advanceUntilIdle()

            coVerify {
                awardPointsUseCase(
                    profileId = any(),
                    // DICTEE_CORRECT_TYPED
                    basePoints = 10,
                    streak = 1,
                    source = PointSource.DICTEE,
                    reason = any(),
                )
            }
        }

    @Test
    fun `correct answer updates word mastery in repository`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.UpdateInput("maison"))
            advanceUntilIdle()
            viewModel.onIntent(DicteePracticeIntent.CheckAnswer)
            advanceUntilIdle()

            coVerify {
                dicteeRepository.updateWord(
                    match { it.id == "w1" && it.attempts == 1 && it.correctCount == 1 },
                )
            }
        }

    @Test
    fun `handwriting recognized updates user input`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.HandwritingRecognized("maison"))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("maison", state.recognizedText)
            assertEquals("maison", state.userInput)
        }

    @Test
    fun `play word calls TTS manager`() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onIntent(DicteePracticeIntent.PlayWord)
            advanceUntilIdle()

            verify { ttsManager.speak("maison", any(), any()) }
        }
}
