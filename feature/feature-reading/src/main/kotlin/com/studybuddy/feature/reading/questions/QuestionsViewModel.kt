package com.studybuddy.feature.reading.questions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.model.ReadingQuestion
import com.studybuddy.core.domain.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestionsState(
    val passage: ReadingPassage? = null,
    val isLoading: Boolean = true,
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerRevealed: Boolean = false,
    val answers: List<AnswerRecord> = emptyList(),
    val showPassageExpanded: Boolean = false,
    val questionsStartTime: Long = 0L,
) {
    val currentQuestion: ReadingQuestion?
        get() = passage?.questions?.getOrNull(currentQuestionIndex)

    val totalQuestions: Int
        get() = passage?.questions?.size ?: 0

    val isLastQuestion: Boolean
        get() = currentQuestionIndex >= totalQuestions - 1
}

data class AnswerRecord(
    val questionIndex: Int,
    val selectedAnswer: String,
    val isCorrect: Boolean,
    val changedAnswer: Boolean,
)

sealed interface QuestionsIntent {
    data class SelectAnswer(val answer: String) : QuestionsIntent
    data object ConfirmAnswer : QuestionsIntent
    data object NextQuestion : QuestionsIntent
    data object TogglePassage : QuestionsIntent
}

sealed interface QuestionsEffect {
    data class NavigateToResults(
        val passageId: String,
        val score: Int,
        val totalQuestions: Int,
        val readingTimeMs: Long,
        val questionsTimeMs: Long,
        val allCorrectFirstTry: Boolean,
        val tier: Int,
    ) : QuestionsEffect
}

private const val FEEDBACK_DELAY_MS = 2000L

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val readingRepository: ReadingRepository,
) : ViewModel() {

    private val passageId: String = checkNotNull(savedStateHandle["passageId"])
    val readingTimeMs: Long = savedStateHandle["readingTimeMs"] ?: 0L

    private val _state = MutableStateFlow(QuestionsState())
    val state: StateFlow<QuestionsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<QuestionsEffect>()
    val effects: SharedFlow<QuestionsEffect> = _effects.asSharedFlow()

    private var firstAnswerPerQuestion = mutableMapOf<Int, String>()

    init {
        loadPassage()
    }

    fun onIntent(intent: QuestionsIntent) {
        when (intent) {
            is QuestionsIntent.SelectAnswer -> selectAnswer(intent.answer)
            is QuestionsIntent.ConfirmAnswer -> confirmAnswer()
            is QuestionsIntent.NextQuestion -> nextQuestion()
            is QuestionsIntent.TogglePassage -> {
                _state.update { it.copy(showPassageExpanded = !it.showPassageExpanded) }
            }
        }
    }

    private fun loadPassage() {
        viewModelScope.launch {
            val passage = readingRepository.getPassageById(passageId)
            _state.update {
                it.copy(
                    passage = passage,
                    isLoading = false,
                    questionsStartTime = System.currentTimeMillis(),
                )
            }
        }
    }

    private fun selectAnswer(answer: String) {
        if (_state.value.isAnswerRevealed) return
        val qIndex = _state.value.currentQuestionIndex
        if (qIndex !in firstAnswerPerQuestion) {
            firstAnswerPerQuestion[qIndex] = answer
        }
        _state.update { it.copy(selectedAnswer = answer) }
    }

    private fun confirmAnswer() {
        val state = _state.value
        val question = state.currentQuestion ?: return
        val selected = state.selectedAnswer ?: return
        if (state.isAnswerRevealed) return

        val isCorrect = selected == question.correctAnswer
        val qIndex = state.currentQuestionIndex
        val changedAnswer = firstAnswerPerQuestion[qIndex] != selected

        val record = AnswerRecord(
            questionIndex = qIndex,
            selectedAnswer = selected,
            isCorrect = isCorrect,
            changedAnswer = changedAnswer,
        )

        _state.update {
            it.copy(
                isAnswerRevealed = true,
                answers = it.answers + record,
            )
        }

        if (!state.isLastQuestion) {
            viewModelScope.launch {
                delay(FEEDBACK_DELAY_MS)
                if (_state.value.isAnswerRevealed) {
                    nextQuestion()
                }
            }
        } else {
            viewModelScope.launch {
                delay(FEEDBACK_DELAY_MS)
                finishQuestions()
            }
        }
    }

    private fun nextQuestion() {
        _state.update {
            it.copy(
                currentQuestionIndex = it.currentQuestionIndex + 1,
                selectedAnswer = null,
                isAnswerRevealed = false,
            )
        }
    }

    private fun finishQuestions() {
        val state = _state.value
        val passage = state.passage ?: return
        val score = state.answers.count { it.isCorrect }
        val allCorrectFirstTry = state.answers.all { it.isCorrect && !it.changedAnswer }
        val questionsTime = System.currentTimeMillis() - state.questionsStartTime

        viewModelScope.launch {
            _effects.emit(
                QuestionsEffect.NavigateToResults(
                    passageId = passageId,
                    score = score,
                    totalQuestions = state.totalQuestions,
                    readingTimeMs = readingTimeMs,
                    questionsTimeMs = questionsTime,
                    allCorrectFirstTry = allCorrectFirstTry,
                    tier = passage.tier,
                ),
            )
        }
    }
}
