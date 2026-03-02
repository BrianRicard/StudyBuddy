package com.studybuddy.feature.dictee.list

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.EmptyState
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.modifier.animateItemAppearance

@Composable
fun DicteeListScreen(
    onNavigateToWords: (String) -> Unit,
    onNavigateToChallenge: (List<String>) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DicteeListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            val csv = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (csv != null) {
                viewModel.onIntent(DicteeListIntent.ImportCsv(csv))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DicteeListEffect.NavigateToWords -> onNavigateToWords(effect.listId)
                is DicteeListEffect.NavigateToChallenge -> onNavigateToChallenge(effect.listIds)
                is DicteeListEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(CoreUiR.string.dictee_list_deleted, effect.list.title),
                        actionLabel = context.getString(CoreUiR.string.dictee_undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(DicteeListIntent.UndoDelete(effect.list))
                    }
                }
                is DicteeListEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId, *effect.args))
                }
            }
        }
    }

    DicteeListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onImportCsv = { importLauncher.launch("text/*") },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DicteeListContent(
    state: DicteeListState,
    snackbarHostState: SnackbarHostState,
    onIntent: (DicteeListIntent) -> Unit,
    onImportCsv: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isSelectMode) {
                            stringResource(
                                CoreUiR.string.dictee_select_lists,
                            )
                        } else {
                            stringResource(CoreUiR.string.dictee_list_title)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (!state.isSelectMode) {
                        TextButton(onClick = onImportCsv) {
                            Text(stringResource(CoreUiR.string.dictee_import))
                        }
                    }
                    if (state.lists.size >= 2) {
                        TextButton(onClick = { onIntent(DicteeListIntent.ToggleSelectMode) }) {
                            Text(
                                if (state.isSelectMode) {
                                    stringResource(
                                        CoreUiR.string.cancel,
                                    )
                                } else {
                                    stringResource(CoreUiR.string.dictee_mix_lists)
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!state.isSelectMode) {
                FloatingActionButton(onClick = { onIntent(DicteeListIntent.ShowCreateDialog) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(CoreUiR.string.dictee_new_list))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingState()
                state.lists.isEmpty() -> EmptyState(
                    title = stringResource(CoreUiR.string.dictee_no_lists),
                    message = stringResource(CoreUiR.string.dictee_no_lists_hint),
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        top = 8.dp,
                        bottom = if (state.isSelectMode) 100.dp else 80.dp,
                    ),
                ) {
                    itemsIndexed(state.lists, key = { _, it -> it.id }) { index, list ->
                        val isSelected = list.id in state.selectedListIds
                        DicteeListItem(
                            list = list,
                            isSelectMode = state.isSelectMode,
                            isSelected = isSelected,
                            onTap = { onIntent(DicteeListIntent.OpenList(list.id)) },
                            onDelete = { onIntent(DicteeListIntent.DeleteList(list.id)) },
                            modifier = Modifier.animateItemAppearance(index),
                        )
                    }
                }
            }

            // Challenge start bar — slides in from bottom when 2+ lists are selected
            AnimatedVisibility(
                visible = state.isSelectMode && state.canStartChallenge,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                ChallengeStartBar(
                    selectedCount = state.selectedListIds.size,
                    onStart = { onIntent(DicteeListIntent.StartChallenge) },
                )
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
    isSelectMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "select-border",
    )

    if (isSelectMode) {
        // In select mode: no swipe-to-dismiss, show checkbox overlay
        StudyBuddyCard(
            onClick = onTap,
            modifier = modifier.border(
                width = 2.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            ),
        ) {
            DicteeListItemContent(list = list, trailingContent = {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(CoreUiR.string.dictee_selected),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    )
                }
            })
        }
    } else {
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
            modifier = modifier,
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
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(CoreUiR.string.dictee_delete),
                        tint = Color.White,
                    )
                }
            },
            enableDismissFromStartToEnd = false,
        ) {
            StudyBuddyCard(onClick = onTap) {
                DicteeListItemContent(list = list)
            }
        }
    }
}

@Composable
private fun DicteeListItemContent(
    list: DicteeList,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = languageFlag(list.language),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailingContent()
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = pluralStringResource(CoreUiR.plurals.dictee_word_count_plural, list.wordCount, list.wordCount),
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

@Composable
private fun ChallengeStartBar(
    selectedCount: Int,
    onStart: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(CoreUiR.string.dictee_lists_selected, selectedCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Button(onClick = onStart) {
            Text(stringResource(CoreUiR.string.dictee_start_challenge))
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
        title = { Text(stringResource(CoreUiR.string.dictee_new_word_list)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(CoreUiR.string.dictee_list_title_label)) },
                    placeholder = { Text(stringResource(CoreUiR.string.dictee_list_title_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(CoreUiR.string.dictee_language), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally,
                    ),
                ) {
                    SupportedLocale.entries.forEach { locale ->
                        val isSelected = language == locale.code
                        TextButton(
                            onClick = { onLanguageChange(locale.code) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = languageFlag(locale.code),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = locale.displayName,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = title.isNotBlank()) {
                Text(stringResource(CoreUiR.string.dictee_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(CoreUiR.string.cancel)) }
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
                DicteeList(
                    id = "2",
                    profileId = "p",
                    title = "Week 2 — Colours",
                    language = "fr",
                    wordCount = 8,
                    masteredCount = 3,
                    createdAt = kotlinx.datetime.Clock.System.now(),
                    updatedAt = kotlinx.datetime.Clock.System.now(),
                ),
            ),
        ),
        snackbarHostState = SnackbarHostState(),
        onIntent = {},
    )
}

@Preview
@Composable
private fun DicteeListSelectModePreview() {
    DicteeListContent(
        state = DicteeListState(
            isLoading = false,
            isSelectMode = true,
            selectedListIds = setOf("1"),
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
                DicteeList(
                    id = "2",
                    profileId = "p",
                    title = "Week 2 — Colours",
                    language = "fr",
                    wordCount = 8,
                    masteredCount = 3,
                    createdAt = kotlinx.datetime.Clock.System.now(),
                    updatedAt = kotlinx.datetime.Clock.System.now(),
                ),
            ),
        ),
        snackbarHostState = SnackbarHostState(),
        onIntent = {},
    )
}
