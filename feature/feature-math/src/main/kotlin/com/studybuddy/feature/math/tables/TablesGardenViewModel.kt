package com.studybuddy.feature.math.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.mathfacts.TableGarden
import com.studybuddy.core.domain.usecase.mathfacts.GetTablesGardenUseCase
import com.studybuddy.core.domain.usecase.mathfacts.TablesMode
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
    data class NavigateToDrill(
        val mode: TablesMode,
        val table: Int? = null,
    ) : TablesGardenEffect
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
        val effect = when (intent) {
            TablesGardenIntent.StartRevision -> TablesGardenEffect.NavigateToDrill(TablesMode.REVISION)
            TablesGardenIntent.StartSurprise -> TablesGardenEffect.NavigateToDrill(TablesMode.SURPRISE)
            is TablesGardenIntent.OpenTable ->
                TablesGardenEffect.NavigateToDrill(mode = TablesMode.TABLE, table = intent.table)
        }
        viewModelScope.launch { _effects.emit(effect) }
    }
}
