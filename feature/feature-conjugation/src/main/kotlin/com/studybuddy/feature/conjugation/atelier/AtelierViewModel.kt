package com.studybuddy.feature.conjugation.atelier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.conjugation.AtelierVerbGarden
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.usecase.conjugation.GetAtelierGardenUseCase
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
import kotlinx.datetime.Clock

data class AtelierState(
    val dueCardCount: Int = 0,
    val dueVerbCount: Int = 0,
    val verbs: List<AtelierVerbGarden> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface AtelierIntent {
    data object StartRevision : AtelierIntent
    data object StartSurprise : AtelierIntent
    data class OpenCell(val verbId: String, val tense: ConjugationTense) : AtelierIntent
}

sealed interface AtelierEffect {
    /** The drill screen ships in the next update; until then taps sprout a gentle note. */
    data object ShowComingSoon : AtelierEffect
}

@HiltViewModel
class AtelierViewModel @Inject constructor(
    getAtelierGarden: GetAtelierGardenUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AtelierState())
    val state: StateFlow<AtelierState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AtelierEffect>()
    val effects: SharedFlow<AtelierEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getAtelierGarden(AppConstants.DEFAULT_PROFILE_ID, Clock.System.now()).collect { garden ->
                _state.update {
                    it.copy(
                        dueCardCount = garden.dueCardCount,
                        dueVerbCount = garden.dueVerbCount,
                        verbs = garden.verbs,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onIntent(intent: AtelierIntent) {
        when (intent) {
            AtelierIntent.StartRevision,
            AtelierIntent.StartSurprise,
            is AtelierIntent.OpenCell,
            -> viewModelScope.launch { _effects.emit(AtelierEffect.ShowComingSoon) }
        }
    }
}
