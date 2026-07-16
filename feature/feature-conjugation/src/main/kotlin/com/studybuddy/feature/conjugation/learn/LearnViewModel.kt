package com.studybuddy.feature.conjugation.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
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

data class LearnState(
    val stage: ConjugationStage? = null,
    val heard: Set<ConjugationPerson> = emptySet(),
    val playingPerson: ConjugationPerson? = null,
    val isSaving: Boolean = false,
) {
    val allHeard: Boolean get() = heard.size == ConjugationPerson.entries.size
}

sealed interface LearnIntent {
    data class PlayForm(val person: ConjugationPerson) : LearnIntent
    data object Finish : LearnIntent
}

sealed interface LearnEffect {
    data object Completed : LearnEffect
}

@HiltViewModel
class LearnViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ttsManager: TtsManager,
    private val conjugationRepository: ConjugationRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
) : ViewModel() {

    private val stageId: String = checkNotNull(savedStateHandle["stageId"])

    private val _state = MutableStateFlow(LearnState(stage = ConjugationStages.byId(stageId)))
    val state: StateFlow<LearnState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LearnEffect>()
    val effects: SharedFlow<LearnEffect> = _effects.asSharedFlow()

    init {
        ttsManager.initialize()
    }

    fun onIntent(intent: LearnIntent) {
        when (intent) {
            is LearnIntent.PlayForm -> playForm(intent.person)
            is LearnIntent.Finish -> finish()
        }
    }

    private fun playForm(person: ConjugationPerson) {
        val stage = _state.value.stage ?: return
        ttsManager.speak(stage.verb.display(person), Locale.FRENCH)
        _state.update { it.copy(heard = it.heard + person, playingPerson = person) }
    }

    private fun finish() {
        val current = _state.value
        if (!current.allHeard || current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val total = ConjugationPerson.entries.size
            conjugationRepository.recordStepResult(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                stageId = stageId,
                step = ConjugationStep.LEARN,
                correct = total,
                total = total,
            )
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = PointValues.CONJUGATION_LISTEN_COMPLETE,
                streak = 0,
                source = PointSource.CONJUGATION,
                reason = "Conjugation listen: $stageId",
            )
            _effects.emit(LearnEffect.Completed)
        }
    }

    override fun onCleared() {
        ttsManager.stop()
        super.onCleared()
    }
}
