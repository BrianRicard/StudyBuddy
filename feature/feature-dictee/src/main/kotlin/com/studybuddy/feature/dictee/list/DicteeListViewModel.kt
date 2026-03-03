package com.studybuddy.feature.dictee.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.usecase.dictee.GetDicteeListsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DicteeListViewModel @Inject constructor(
    private val getDicteeListsUseCase: GetDicteeListsUseCase,
    private val dicteeRepository: DicteeRepository,
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
            is DicteeListIntent.OpenList -> {
                if (_state.value.isSelectMode) {
                    onIntent(DicteeListIntent.ToggleListSelection(intent.listId))
                } else {
                    viewModelScope.launch {
                        _effects.emit(DicteeListEffect.NavigateToWords(intent.listId))
                    }
                }
            }
            is DicteeListIntent.EditList -> {
                viewModelScope.launch {
                    _effects.emit(DicteeListEffect.NavigateToEdit(intent.listId))
                }
            }
            is DicteeListIntent.DeleteList -> deleteList(intent.listId)
            is DicteeListIntent.UndoDelete -> undoDelete(intent.list)
            is DicteeListIntent.UpdateSearch -> {
                _state.update { it.copy(searchQuery = intent.query) }
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
            is DicteeListIntent.StartChallenge -> startChallenge()
        }
    }

    private fun loadLists() {
        viewModelScope.launch {
            getDicteeListsUseCase(profileId).collect { lists ->
                val items = lists.map { list ->
                    val words = dicteeRepository.getWordsForList(list.id).first()
                    DicteeListItem(
                        list = list,
                        wordPreview = words.take(WORD_PREVIEW_COUNT).map { it.word },
                    )
                }
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    private fun deleteList(listId: String) {
        val item = _state.value.items.find { it.list.id == listId } ?: return
        viewModelScope.launch {
            dicteeRepository.deleteList(listId)
            _effects.emit(DicteeListEffect.ShowUndoSnackbar(item.list))
        }
    }

    private fun undoDelete(list: DicteeList) {
        viewModelScope.launch {
            dicteeRepository.createList(list)
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

    companion object {
        private const val WORD_PREVIEW_COUNT = 4
    }
}
