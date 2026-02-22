package com.studybuddy.feature.dictee.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.ui.components.EmptyState
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.StudyBuddyCard

@Composable
fun DicteeListScreen(
    onNavigateToWords: (String) -> Unit,
    viewModel: DicteeListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DicteeListEffect.NavigateToWords -> onNavigateToWords(effect.listId)
                is DicteeListEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "List \"${effect.list.title}\" deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(DicteeListIntent.UndoDelete(effect.list))
                    }
                }
            }
        }
    }

    DicteeListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DicteeListContent(
    state: DicteeListState,
    snackbarHostState: SnackbarHostState,
    onIntent: (DicteeListIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dictée") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onIntent(DicteeListIntent.ShowCreateDialog) }) {
                Icon(Icons.Default.Add, contentDescription = "New List")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingState()
                state.lists.isEmpty() -> EmptyState(
                    title = "No Word Lists Yet",
                    message = "Tap + to create your first dictée list!",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.lists, key = { it.id }) { list ->
                        DicteeListItem(
                            list = list,
                            onTap = { onIntent(DicteeListIntent.OpenList(list.id)) },
                            onDelete = { onIntent(DicteeListIntent.DeleteList(list.id)) },
                        )
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateListDialog(
            title = state.newListTitle,
            language = state.newListLanguage,
            onTitleChange = { onIntent(DicteeListIntent.UpdateNewListTitle(it)) },
            onLanguageChange = { onIntent(DicteeListIntent.UpdateNewListLanguage(it)) },
            onConfirm = { onIntent(DicteeListIntent.CreateList) },
            onDismiss = { onIntent(DicteeListIntent.DismissCreateDialog) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DicteeListItem(
    list: DicteeList,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color.Transparent
                },
                label = "swipe-bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        StudyBuddyCard(onClick = onTap) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = list.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = languageFlag(list.language),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${list.wordCount} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                val masteryPercent = if (list.wordCount > 0) {
                    list.masteredCount.toFloat() / list.wordCount
                } else {
                    0f
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { masteryPercent },
                        modifier = Modifier.weight(1f).height(8.dp),
                        color = masteryColor(masteryPercent),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(masteryPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateListDialog(
    title: String,
    language: String,
    onTitleChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Word List") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("List Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Language", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SupportedLocale.entries.forEach { locale ->
                        TextButton(
                            onClick = { onLanguageChange(locale.code) },
                        ) {
                            Text(
                                text = "${languageFlag(locale.code)} ${locale.displayName}",
                                color = if (language == locale.code) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = title.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun languageFlag(code: String): String = when (code) {
    "fr" -> "\uD83C\uDDEB\uD83C\uDDF7"
    "en" -> "\uD83C\uDDEC\uD83C\uDDE7"
    "de" -> "\uD83C\uDDE9\uD83C\uDDEA"
    else -> "\uD83C\uDFF3\uFE0F"
}

private fun masteryColor(percent: Float): Color = when {
    percent < 0.5f -> Color(0xFFE53935)
    percent < 0.8f -> Color(0xFFFDD835)
    else -> Color(0xFF43A047)
}

@Preview
@Composable
private fun DicteeListScreenPreview() {
    DicteeListContent(
        state = DicteeListState(
            isLoading = false,
            lists = listOf(
                DicteeList(
                    id = "1",
                    profileId = "p",
                    title = "Les Animaux",
                    language = "fr",
                    wordCount = 10,
                    masteredCount = 7,
                    createdAt = kotlinx.datetime.Clock.System.now(),
                    updatedAt = kotlinx.datetime.Clock.System.now(),
                ),
            ),
        ),
        snackbarHostState = SnackbarHostState(),
        onIntent = {},
    )
}
