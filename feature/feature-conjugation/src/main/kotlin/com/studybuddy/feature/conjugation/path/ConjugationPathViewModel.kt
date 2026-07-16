package com.studybuddy.feature.conjugation.path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.conjugation.ConjugationPathStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.usecase.conjugation.GetConjugationPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConjugationPathState(
    val stages: List<ConjugationPathStage> = emptyList(),
    val expandedStageId: String? = null,
    val isLoading: Boolean = true,
) {
    val isQuestComplete: Boolean
        get() = stages.isNotEmpty() && stages.all { it.isCompleted }
}

sealed interface ConjugationPathIntent {
    data class ToggleStage(val stageId: String) : ConjugationPathIntent
    data class OpenStep(val stageId: String, val step: ConjugationStep) : ConjugationPathIntent
}

sealed interface ConjugationPathEffect {
    data class NavigateToStep(val stageId: String, val step: ConjugationStep) : ConjugationPathEffect
}

@HiltViewModel
class ConjugationPathViewModel @Inject constructor(
    getConjugationPath: GetConjugationPathUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ConjugationPathState())
    val state: StateFlow<ConjugationPathState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ConjugationPathEffect>()
    val effects: SharedFlow<ConjugationPathEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getConjugationPath(AppConstants.DEFAULT_PROFILE_ID).collect { stages ->
                _state.update { current ->
                    current.copy(
                        stages = stages,
                        isLoading = false,
                        // Auto-open the stage the child should play next.
                        expandedStageId = current.expandedStageId
                            ?: stages.firstOrNull { it.isUnlocked && !it.isCompleted }?.stage?.id,
                    )
                }
            }
        }
    }

    fun onIntent(intent: ConjugationPathIntent) {
        when (intent) {
            is ConjugationPathIntent.ToggleStage -> _state.update {
                it.copy(expandedStageId = if (it.expandedStageId == intent.stageId) null else intent.stageId)
            }

            is ConjugationPathIntent.OpenStep -> {
                val stage = _state.value.stages.firstOrNull { it.stage.id == intent.stageId } ?: return
                if (!stage.isStepUnlocked(intent.step)) return
                viewModelScope.launch {
                    _effects.emit(ConjugationPathEffect.NavigateToStep(intent.stageId, intent.step))
                }
            }
        }
    }
}
