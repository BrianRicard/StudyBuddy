package com.studybuddy.feature.dictee.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.ui.R as CoreUiR
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
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
import kotlinx.datetime.Clock

@HiltViewModel
class AddDicteeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dicteeRepository: DicteeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddDicteeState())
    val state: StateFlow<AddDicteeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AddDicteeEffect>()
    val effects: SharedFlow<AddDicteeEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    init {
        val setId = savedStateHandle.get<String>("setId")
        if (setId != null) {
            _state.update { it.copy(isEditMode = true, editingSetId = setId) }
            loadExistingList(setId)
        }
    }

    private fun loadExistingList(setId: String) {
        viewModelScope.launch {
            val list = dicteeRepository.getList(setId).first() ?: return@launch
            val words = dicteeRepository.getWordsForList(setId).first()
            _state.update {
                it.copy(
                    title = list.title,
                    language = list.language,
                    words = words.map { w -> DicteeWordDraft(id = w.id, word = w.word) },
                )
            }
        }
    }

    fun onIntent(intent: AddDicteeIntent) {
        when (intent) {
            is AddDicteeIntent.UpdateTitle -> _state.update { it.copy(title = intent.title) }
            is AddDicteeIntent.UpdateLanguage ->
                _state.update { it.copy(language = intent.language) }
            is AddDicteeIntent.UpdateWordInput ->
                _state.update { it.copy(currentWordInput = intent.input) }
            is AddDicteeIntent.AddWord -> addWord()
            is AddDicteeIntent.RemoveWord -> removeWord(intent.id)
            is AddDicteeIntent.Save -> save()
            is AddDicteeIntent.ImportFile -> importFile(intent.content, intent.mimeType)
            is AddDicteeIntent.DeleteList -> deleteList()
        }
    }

    private fun addWord() {
        val input = _state.value.currentWordInput.trim()
        if (input.isBlank()) return

        val draft = DicteeWordDraft(id = UUID.randomUUID().toString(), word = input)
        _state.update {
            it.copy(
                words = it.words + draft,
                currentWordInput = "",
            )
        }
    }

    private fun removeWord(id: String) {
        _state.update { it.copy(words = it.words.filter { w -> w.id != id }) }
    }

    private fun save() {
        val current = _state.value
        if (!current.isValid || current.isSaving) return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            if (current.isEditMode && current.editingSetId != null) {
                updateExistingList(current)
            } else {
                createNewList(current)
            }
        }
    }

    private suspend fun createNewList(current: AddDicteeState) {
        val now = Clock.System.now()
        val listId = UUID.randomUUID().toString()
        val list = DicteeList(
            id = listId,
            profileId = profileId,
            title = current.title.trim(),
            language = current.language,
            createdAt = now,
            updatedAt = now,
        )
        dicteeRepository.createList(list)

        current.words.forEach { draft ->
            dicteeRepository.addWord(
                DicteeWord(
                    id = draft.id,
                    listId = listId,
                    word = draft.word,
                ),
            )
        }

        _effects.emit(AddDicteeEffect.ShowSnackbar(CoreUiR.string.dictee_list_saved))
        _effects.emit(AddDicteeEffect.NavigateBack)
    }

    private suspend fun updateExistingList(current: AddDicteeState) {
        val setId = current.editingSetId ?: return
        val existing = dicteeRepository.getList(setId).first() ?: return
        val existingWords = dicteeRepository.getWordsForList(setId).first()

        dicteeRepository.updateList(
            existing.copy(
                title = current.title.trim(),
                language = current.language,
                updatedAt = Clock.System.now(),
            ),
        )

        val currentIds = current.words.map { it.id }.toSet()
        existingWords.filter { it.id !in currentIds }.forEach {
            dicteeRepository.deleteWord(it.id)
        }

        val existingIds = existingWords.map { it.id }.toSet()
        current.words.filter { it.id !in existingIds }.forEach { draft ->
            dicteeRepository.addWord(
                DicteeWord(id = draft.id, listId = setId, word = draft.word),
            )
        }

        _effects.emit(AddDicteeEffect.ShowSnackbar(CoreUiR.string.dictee_list_updated))
        _effects.emit(AddDicteeEffect.NavigateBack)
    }

    private fun importFile(
        content: String,
        mimeType: String,
    ) {
        viewModelScope.launch {
            val result = when {
                mimeType.contains("json") -> DicteeFileParser.parseJsonFile(content)
                mimeType.contains("csv") -> DicteeFileParser.parseCsvFile(content)
                else -> DicteeFileParser.parseTextFile(content)
            }

            result.fold(
                onSuccess = { words ->
                    val drafts = words.map {
                        DicteeWordDraft(id = UUID.randomUUID().toString(), word = it)
                    }
                    _state.update { it.copy(words = it.words + drafts) }
                    _effects.emit(
                        AddDicteeEffect.ShowSnackbar(
                            CoreUiR.string.dictee_imported_count,
                            words.size,
                        ),
                    )
                },
                onFailure = {
                    _effects.emit(
                        AddDicteeEffect.ShowSnackbar(CoreUiR.string.dictee_import_error),
                    )
                },
            )
        }
    }

    private fun deleteList() {
        val setId = _state.value.editingSetId ?: return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            dicteeRepository.deleteList(setId)
            _effects.emit(AddDicteeEffect.NavigateBack)
        }
    }
}
