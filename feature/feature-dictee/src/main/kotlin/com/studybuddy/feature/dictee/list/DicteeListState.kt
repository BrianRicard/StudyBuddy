package com.studybuddy.feature.dictee.list

import androidx.annotation.StringRes
import com.studybuddy.core.domain.model.DicteeList

data class DicteeListItem(
    val list: DicteeList,
    val wordPreview: List<String> = emptyList(),
) {
    val isDefault: Boolean get() = list.id.startsWith("fr_dictee_")
}

data class DicteeListState(
    val items: List<DicteeListItem> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    // Multi-select / challenge mode
    val isSelectMode: Boolean = false,
    val selectedListIds: Set<String> = emptySet(),
) {
    val canStartChallenge: Boolean get() = selectedListIds.size >= 2

    val filteredItems: List<DicteeListItem>
        get() = if (searchQuery.isBlank()) {
            items
        } else {
            items.filter {
                it.list.title.contains(searchQuery, ignoreCase = true) ||
                    it.wordPreview.any { w -> w.contains(searchQuery, ignoreCase = true) }
            }
        }
}

sealed interface DicteeListIntent {
    data object LoadLists : DicteeListIntent
    data class OpenList(val listId: String) : DicteeListIntent
    data class EditList(val listId: String) : DicteeListIntent
    data class DeleteList(val listId: String) : DicteeListIntent
    data class UndoDelete(val list: DicteeList) : DicteeListIntent
    data class UpdateSearch(val query: String) : DicteeListIntent

    // Challenge / multi-select
    data object ToggleSelectMode : DicteeListIntent
    data class ToggleListSelection(val listId: String) : DicteeListIntent
    data object StartChallenge : DicteeListIntent
}

sealed interface DicteeListEffect {
    data class NavigateToPractice(val listId: String) : DicteeListEffect
    data class NavigateToAdd(val unused: Unit = Unit) : DicteeListEffect
    data class NavigateToEdit(val listId: String) : DicteeListEffect
    data class ShowUndoSnackbar(val list: DicteeList) : DicteeListEffect
    data class NavigateToChallenge(val listIds: List<String>) : DicteeListEffect
    data class ShowToast(
        @StringRes val messageResId: Int,
        val args: Array<Any> = emptyArray(),
    ) : DicteeListEffect
}
