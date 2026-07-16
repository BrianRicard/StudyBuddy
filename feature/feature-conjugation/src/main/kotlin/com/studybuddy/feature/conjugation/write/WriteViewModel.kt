package com.studybuddy.feature.conjugation.write

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.usecase.conjugation.CheckConjugationAnswerUseCase
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** How the last submission went; drives the feedback banner. */
sealed interface WriteFeedback {
    data object Idle : WriteFeedback
    data class Correct(val praiseSeed: Int) : WriteFeedback
    data object TryAgain : WriteFeedback
    data class Revealed(val correctForm: String) : WriteFeedback
}

data class WriteState(
    val stage: ConjugationStage? = null,
    val index: Int = 0,
    val input: String = "",
    val feedback: WriteFeedback = WriteFeedback.Idle,
    val attemptsOnCurrent: Int = 0,
    val firstTryCorrect: Int = 0,
    val isFinished: Boolean = false,
    val isSaving: Boolean = false,
) {
    val person: ConjugationPerson get() = ConjugationPerson.entries[index]
    val total: Int get() = ConjugationPerson.entries.size
    val isLast: Boolean get() = index == total - 1
    val canRevealHint: Boolean get() = attemptsOnCurrent >= HINT_ATTEMPT_THRESHOLD

    companion object {
        const val HINT_ATTEMPT_THRESHOLD = 2
    }
}

sealed interface WriteIntent {
    data class InputChanged(val value: String) : WriteIntent
    data object Submit : WriteIntent
    data object RevealHint : WriteIntent
    data object Next : WriteIntent
}

sealed interface WriteEffect {
    data object Completed : WriteEffect
}

@HiltViewModel
class WriteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkAnswer: CheckConjugationAnswerUseCase,
    private val conjugationRepository: ConjugationRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val stageId: String = checkNotNull(savedStateHandle["stageId"])

    private val _state = MutableStateFlow(WriteState(stage = ConjugationStages.byId(stageId)))
    val state: StateFlow<WriteState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<WriteEffect>()
    val effects: SharedFlow<WriteEffect> = _effects.asSharedFlow()

    init {
        ttsManager.initialize()
    }

    fun onIntent(intent: WriteIntent) {
        when (intent) {
            is WriteIntent.InputChanged -> _state.update { it.copy(input = intent.value) }
            is WriteIntent.Submit -> submit()
            is WriteIntent.RevealHint -> revealHint()
            is WriteIntent.Next -> next()
        }
    }

    private fun submit() {
        val current = _state.value
        val stage = current.stage ?: return
        if (current.feedback is WriteFeedback.Correct || current.input.isBlank()) return

        val correctForm = stage.verb.form(current.person)
        when (checkAnswer(current.input, correctForm)) {
            is Feedback.Correct -> onCorrect(stage, correctForm)
            else -> _state.update {
                it.copy(
                    feedback = WriteFeedback.TryAgain,
                    attemptsOnCurrent = it.attemptsOnCurrent + 1,
                )
            }
        }
    }

    private fun onCorrect(
        stage: ConjugationStage,
        correctForm: String,
    ) {
        val firstTry = _state.value.attemptsOnCurrent == 0
        ttsManager.speak(stage.verb.display(_state.value.person), Locale.FRENCH)
        _state.update {
            it.copy(
                feedback = WriteFeedback.Correct(praiseSeed = it.index),
                input = correctForm,
                firstTryCorrect = it.firstTryCorrect + if (firstTry) 1 else 0,
            )
        }
        viewModelScope.launch {
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = PointValues.CONJUGATION_FORM_WRITTEN,
                streak = 0,
                source = PointSource.CONJUGATION,
                reason = "Conjugation written: $stageId/$correctForm",
            )
        }
    }

    private fun revealHint() {
        val current = _state.value
        val stage = current.stage ?: return
        if (!current.canRevealHint) return
        _state.update {
            it.copy(feedback = WriteFeedback.Revealed(stage.verb.form(current.person)))
        }
    }

    private fun next() {
        val current = _state.value
        if (current.feedback !is WriteFeedback.Correct) return
        if (!current.isLast) {
            _state.update {
                it.copy(
                    index = it.index + 1,
                    input = "",
                    feedback = WriteFeedback.Idle,
                    attemptsOnCurrent = 0,
                )
            }
            return
        }
        finish()
    }

    private fun finish() {
        val current = _state.value
        if (current.isSaving) return
        _state.update { it.copy(isSaving = true, isFinished = true) }
        viewModelScope.launch {
            conjugationRepository.recordStepResult(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                stageId = stageId,
                step = ConjugationStep.WRITE,
                correct = current.firstTryCorrect,
                total = current.total,
            )
            if (current.firstTryCorrect == current.total) {
                awardPointsUseCase(
                    profileId = AppConstants.DEFAULT_PROFILE_ID,
                    basePoints = PointValues.CONJUGATION_PERFECT_BONUS,
                    streak = 0,
                    source = PointSource.CONJUGATION,
                    reason = "Conjugation perfect writing: $stageId",
                )
            }
            _effects.emit(WriteEffect.Completed)
        }
    }

    override fun onCleared() {
        ttsManager.stop()
        super.onCleared()
    }
}
