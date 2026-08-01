package com.studybuddy.feature.math.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.mathfacts.TableGarden
import com.studybuddy.core.domain.usecase.mathfacts.GetTablesGardenUseCase
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

data class TablesGardenState(
    val dueCardCount: Int = 0,
    val dueTableCount: Int = 0,
    val newCardCount: Int = 0,
    val tables: List<TableGarden> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface TablesGardenIntent {
    data object StartRevision : TablesGardenIntent
    data object StartSurprise : TablesGardenIntent
    data class OpenTable(val table: Int) : TablesGardenIntent
}

sealed interface TablesGardenEffect {
    /** The drill screen ships in the next update; until then taps sprout a gentle note. */
    data object ShowComingSoon : TablesGardenEffect
}

@HiltViewModel
class TablesGardenViewModel @Inject constructor(
    getTablesGarden: GetTablesGardenUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TablesGardenState())
    val state: StateFlow<TablesGardenState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TablesGardenEffect>()
    val effects: SharedFlow<TablesGardenEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getTablesGarden(AppConstants.DEFAULT_PROFILE_ID).collect { garden ->
                _state.update {
                    it.copy(
                        dueCardCount = garden.dueCardCount,
                        dueTableCount = garden.dueTableCount,
                        newCardCount = garden.newCardCount,
                        tables = garden.tables,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onIntent(intent: TablesGardenIntent) {
        when (intent) {
            TablesGardenIntent.StartRevision,
            TablesGardenIntent.StartSurprise,
            is TablesGardenIntent.OpenTable,
            -> viewModelScope.launch { _effects.emit(TablesGardenEffect.ShowComingSoon) }
        }
    }
}
