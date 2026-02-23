package com.studybuddy.feature.dictee.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.usecase.dictee.GetDicteeListsUseCase
import com.studybuddy.core.domain.usecase.dictee.ImportWordListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
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

@HiltViewModel
class DicteeListViewModel @Inject constructor(
    private val getDicteeListsUseCase: GetDicteeListsUseCase,
    private val dicteeRepository: DicteeRepository,
    private val importWordListUseCase: ImportWordListUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DicteeListState())
    val state: StateFlow<DicteeListState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<DicteeListEffect>()
    val effects: SharedFlow<DicteeListEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    init {
        onIntent(DicteeListIntent.LoadLists)
    }

    fun onIntent(intent: DicteeListIntent) {
        when (intent) {
            is DicteeListIntent.LoadLists -> loadLists()
            is DicteeListIntent.ShowCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true, newListTitle = "", newListLanguage = "fr") }
            }
            is DicteeListIntent.DismissCreateDialog -> {
                _state.update { it.copy(showCreateDialog = false) }
            }
            is DicteeListIntent.UpdateNewListTitle -> {
                _state.update { it.copy(newListTitle = intent.title) }
            }
            is DicteeListIntent.UpdateNewListLanguage -> {
                _state.update { it.copy(newListLanguage = intent.language) }
            }
            is DicteeListIntent.CreateList -> createList()
            is DicteeListIntent.DeleteList -> deleteList(intent.listId)
            is DicteeListIntent.UndoDelete -> undoDelete(intent.list)
            is DicteeListIntent.OpenList -> {
                if (_state.value.isSelectMode) {
                    onIntent(DicteeListIntent.ToggleListSelection(intent.listId))
                } else {
                    viewModelScope.launch {
                        _effects.emit(DicteeListEffect.NavigateToWords(intent.listId))
                    }
                }
            }
            is DicteeListIntent.ToggleSelectMode -> {
                _state.update {
                    it.copy(
                        isSelectMode = !it.isSelectMode,
                        selectedListIds = emptySet(),
                    )
                }
            }
            is DicteeListIntent.ToggleListSelection -> {
                _state.update { current ->
                    val updated = if (intent.listId in current.selectedListIds) {
                        current.selectedListIds - intent.listId
                    } else {
                        current.selectedListIds + intent.listId
                    }
                    current.copy(selectedListIds = updated)
                }
            }
            is DicteeListIntent.ImportCsv -> importCsv(intent.csvContent)
            is DicteeListIntent.StartChallenge -> startChallenge()
        }
    }

    private fun loadLists() {
        viewModelScope.launch {
            getDicteeListsUseCase(profileId).collect { lists ->
                _state.update { it.copy(lists = lists, isLoading = false) }
            }
        }
    }

    private fun createList() {
        val currentState = _state.value
        if (currentState.newListTitle.isBlank()) return

        viewModelScope.launch {
            val now = Clock.System.now()
            val list = DicteeList(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                title = currentState.newListTitle.trim(),
                language = currentState.newListLanguage,
                createdAt = now,
                updatedAt = now,
            )
            dicteeRepository.createList(list)
            _state.update { it.copy(showCreateDialog = false, newListTitle = "", newListLanguage = "fr") }
        }
    }

    private fun deleteList(listId: String) {
        val list = _state.value.lists.find { it.id == listId } ?: return
        viewModelScope.launch {
            dicteeRepository.deleteList(listId)
            _effects.emit(DicteeListEffect.ShowUndoSnackbar(list))
        }
    }

    private fun undoDelete(list: DicteeList) {
        viewModelScope.launch {
            dicteeRepository.createList(list)
        }
    }

    private fun importCsv(csvContent: String) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true) }
            try {
                val count = importWordListUseCase(csvContent, profileId)
                _state.update { it.copy(isImporting = false) }
                if (count > 0) {
                    _effects.emit(DicteeListEffect.ShowToast("Imported $count words"))
                } else {
                    _effects.emit(DicteeListEffect.ShowToast("No words found in file"))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isImporting = false) }
                _effects.emit(
                    DicteeListEffect.ShowToast("Import failed: ${e.message ?: "Unknown error"}"),
                )
            }
        }
    }

    private fun startChallenge() {
        val ids = _state.value.selectedListIds.toList()
        if (ids.size < 2) return
        viewModelScope.launch {
            _state.update { it.copy(isSelectMode = false, selectedListIds = emptySet()) }
            _effects.emit(DicteeListEffect.NavigateToChallenge(ids))
        }
    }
}
