package com.studybuddy.feature.dictee.list

import com.studybuddy.core.domain.model.DicteeList

data class DicteeListState(
    val lists: List<DicteeList> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val newListTitle: String = "",
    val newListLanguage: String = "fr",
    // Multi-select / challenge mode
    val isSelectMode: Boolean = false,
    val selectedListIds: Set<String> = emptySet(),
) {
    val canStartChallenge: Boolean get() = selectedListIds.size >= 2
}

sealed interface DicteeListIntent {
    data object LoadLists : DicteeListIntent
    data object ShowCreateDialog : DicteeListIntent
    data object DismissCreateDialog : DicteeListIntent
    data class UpdateNewListTitle(val title: String) : DicteeListIntent
    data class UpdateNewListLanguage(val language: String) : DicteeListIntent
    data object CreateList : DicteeListIntent
    data class DeleteList(val listId: String) : DicteeListIntent
    data class UndoDelete(val list: DicteeList) : DicteeListIntent
    data class OpenList(val listId: String) : DicteeListIntent

    // Challenge / multi-select
    data object ToggleSelectMode : DicteeListIntent
    data class ToggleListSelection(val listId: String) : DicteeListIntent
    data object StartChallenge : DicteeListIntent
}

sealed interface DicteeListEffect {
    data class NavigateToWords(val listId: String) : DicteeListEffect
    data class ShowUndoSnackbar(val list: DicteeList) : DicteeListEffect
    data class NavigateToChallenge(val listIds: List<String>) : DicteeListEffect
}
