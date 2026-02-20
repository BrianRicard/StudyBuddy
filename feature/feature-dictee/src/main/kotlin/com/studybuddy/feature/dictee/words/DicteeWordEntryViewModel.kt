package com.studybuddy.feature.dictee.words

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.usecase.dictee.AddWordUseCase
import com.studybuddy.shared.tts.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DicteeWordEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dicteeRepository: DicteeRepository,
    private val addWordUseCase: AddWordUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val listId: String = checkNotNull(savedStateHandle["listId"])

    private val _state = MutableStateFlow(DicteeWordEntryState())
    val state: StateFlow<DicteeWordEntryState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<DicteeWordEntryEffect>()
    val effects: SharedFlow<DicteeWordEntryEffect> = _effects.asSharedFlow()

    init {
        onIntent(DicteeWordEntryIntent.LoadWords(listId))
    }

    fun onIntent(intent: DicteeWordEntryIntent) {
        when (intent) {
            is DicteeWordEntryIntent.LoadWords -> loadWords(intent.listId)
            is DicteeWordEntryIntent.UpdateNewWordText -> {
                _state.update { it.copy(newWordText = intent.text) }
            }
            is DicteeWordEntryIntent.AddWord -> addWord()
            is DicteeWordEntryIntent.DeleteWord -> deleteWord(intent.wordId)
            is DicteeWordEntryIntent.UndoDeleteWord -> undoDeleteWord(intent.word)
            is DicteeWordEntryIntent.PlayWord -> playWord(intent.word)
            is DicteeWordEntryIntent.ToggleEditMode -> {
                _state.update { it.copy(isEditMode = !it.isEditMode) }
            }
            is DicteeWordEntryIntent.StartPractice -> {
                if (_state.value.words.isNotEmpty()) {
                    viewModelScope.launch {
                        _effects.emit(DicteeWordEntryEffect.NavigateToPractice(listId))
                    }
                }
            }
        }
    }

    private fun loadWords(listId: String) {
        viewModelScope.launch {
            launch {
                dicteeRepository.getList(listId).collect { list ->
                    _state.update { it.copy(list = list) }
                }
            }
            launch {
                dicteeRepository.getWordsForList(listId).collect { words ->
                    _state.update { it.copy(words = words, isLoading = false) }
                }
            }
        }
    }

    private fun addWord() {
        val text = _state.value.newWordText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            val word = DicteeWord(
                id = UUID.randomUUID().toString(),
                listId = listId,
                word = text,
            )
            addWordUseCase(word)
            _state.update { it.copy(newWordText = "") }
        }
    }

    private fun deleteWord(wordId: String) {
        val word = _state.value.words.find { it.id == wordId } ?: return
        viewModelScope.launch {
            dicteeRepository.deleteWord(wordId)
            _effects.emit(DicteeWordEntryEffect.ShowUndoSnackbar(word))
        }
    }

    private fun undoDeleteWord(word: DicteeWord) {
        viewModelScope.launch {
            dicteeRepository.addWord(word)
        }
    }

    private fun playWord(word: String) {
        val list = _state.value.list ?: return
        val locale = SupportedLocale.fromCode(list.language).javaLocale
        ttsManager.speak(word, locale)
    }
}
