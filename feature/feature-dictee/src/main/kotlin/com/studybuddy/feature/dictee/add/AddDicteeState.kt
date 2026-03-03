package com.studybuddy.feature.dictee.add

import androidx.annotation.StringRes

data class DicteeWordDraft(
    val id: String,
    val word: String,
)

data class AddDicteeState(
    val title: String = "",
    val language: String = "fr",
    val words: List<DicteeWordDraft> = emptyList(),
    val currentWordInput: String = "",
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val editingSetId: String? = null,
) {
    val isValid: Boolean
        get() = title.isNotBlank() && words.isNotEmpty()
}

sealed interface AddDicteeIntent {
    data class UpdateTitle(val title: String) : AddDicteeIntent
    data class UpdateLanguage(val language: String) : AddDicteeIntent
    data class UpdateWordInput(val input: String) : AddDicteeIntent
    data object AddWord : AddDicteeIntent
    data class RemoveWord(val id: String) : AddDicteeIntent
    data object Save : AddDicteeIntent
    data class ImportFile(val content: String, val mimeType: String) : AddDicteeIntent
    data object DeleteList : AddDicteeIntent
}

sealed interface AddDicteeEffect {
    data object NavigateBack : AddDicteeEffect
    data class ShowSnackbar(
        @StringRes val messageResId: Int,
        val formatArg: Int = 0,
    ) : AddDicteeEffect
}
