package com.studybuddy.feature.dictee.words

import androidx.annotation.StringRes
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord

data class DicteeWordEntryState(
    val list: DicteeList? = null,
    val words: List<DicteeWord> = emptyList(),
    val isLoading: Boolean = true,
    val newWordText: String = "",
    val isEditMode: Boolean = false,
    @StringRes val errorMessageResId: Int? = null,
)

sealed interface DicteeWordEntryIntent {
    data class LoadWords(val listId: String) : DicteeWordEntryIntent
    data class UpdateNewWordText(val text: String) : DicteeWordEntryIntent
    data object AddWord : DicteeWordEntryIntent
    data class DeleteWord(val wordId: String) : DicteeWordEntryIntent
    data class UndoDeleteWord(val word: DicteeWord) : DicteeWordEntryIntent
    data class PlayWord(val word: String) : DicteeWordEntryIntent
    data object ToggleEditMode : DicteeWordEntryIntent
    data object StartPractice : DicteeWordEntryIntent
    data object DismissError : DicteeWordEntryIntent
}

sealed interface DicteeWordEntryEffect {
    data class NavigateToPractice(val listId: String) : DicteeWordEntryEffect
    data class ShowUndoSnackbar(val word: DicteeWord) : DicteeWordEntryEffect
    data class ShowError(@StringRes val messageResId: Int) : DicteeWordEntryEffect
}
