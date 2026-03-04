package com.studybuddy.feature.dictee.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.EmptyState
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.SearchField
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.components.SwipeableListCard
import com.studybuddy.core.ui.modifier.animateItemAppearance

@Composable
fun DicteeListScreen(
    onNavigateToPractice: (String) -> Unit,
    onNavigateToChallenge: (List<String>) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DicteeListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DicteeListEffect.NavigateToPractice -> onNavigateToPractice(effect.listId)
                is DicteeListEffect.NavigateToAdd -> onNavigateToAdd()
                is DicteeListEffect.NavigateToEdit -> onNavigateToEdit(effect.listId)
                is DicteeListEffect.NavigateToChallenge -> onNavigateToChallenge(effect.listIds)
                is DicteeListEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(
                            CoreUiR.string.dictee_list_deleted,
                            effect.list.title,
                        ),
                        actionLabel = context.getString(CoreUiR.string.dictee_undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(DicteeListIntent.UndoDelete(effect.list))
                    }
                }
                is DicteeListEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        context.getString(effect.messageResId, *effect.args),
                    )
                }
            }
        }
    }

    DicteeListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DicteeListContent(
    state: DicteeListState,
    snackbarHostState: SnackbarHostState,
    onIntent: (DicteeListIntent) -> Unit,
    onNavigateToAdd: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isSelectMode) {
                            stringResource(CoreUiR.string.dictee_select_lists)
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
                    if (state.items.size >= 2) {
                        TextButton(onClick = { onIntent(DicteeListIntent.ToggleSelectMode) }) {
                            Text(
                                if (state.isSelectMode) {
                                    stringResource(CoreUiR.string.cancel)
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
                FloatingActionButton(onClick = onNavigateToAdd) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(CoreUiR.string.dictee_new_list),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingState()
                state.items.isEmpty() -> EmptyState(
                    title = stringResource(CoreUiR.string.dictee_no_lists),
                    message = stringResource(CoreUiR.string.dictee_no_lists_hint),
                )
                else -> {
                    val filtered = state.filteredItems
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            top = 8.dp,
                            bottom = if (state.isSelectMode) 100.dp else 80.dp,
                        ),
                    ) {
                        // Search field
                        if (!state.isSelectMode) {
                            item(key = "search") {
                                SearchField(
                                    query = state.searchQuery,
                                    onQueryChange = {
                                        onIntent(DicteeListIntent.UpdateSearch(it))
                                    },
                                    placeholder = stringResource(
                                        CoreUiR.string.search_placeholder_dictees,
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                        }

                        itemsIndexed(
                            filtered,
                            key = { _, it -> it.list.id },
                        ) { index, item ->
                            val isSelected = item.list.id in state.selectedListIds
                            DicteeListCard(
                                item = item,
                                isSelectMode = state.isSelectMode,
                                isSelected = isSelected,
                                onTap = {
                                    onIntent(DicteeListIntent.OpenList(item.list.id))
                                },
                                onEdit = {
                                    onIntent(DicteeListIntent.EditList(item.list.id))
                                },
                                modifier = Modifier.animateItemAppearance(index),
                            )
                        }
                    }
                }
            }

            // Challenge start bar
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
}

@Composable
private fun DicteeListCard(
    item: DicteeListItem,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "select-border",
    )

    if (isSelectMode) {
        StudyBuddyCard(
            onClick = onTap,
            modifier = modifier.border(
                width = 2.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            ),
        ) {
            DicteeCardContent(item = item, trailingContent = {
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
        SwipeableListCard(
            onEdit = onEdit,
            modifier = modifier,
        ) {
            StudyBuddyCard(onClick = onTap) {
                DicteeCardContent(item = item)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DicteeCardContent(
    item: DicteeListItem,
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
                    text = item.list.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = languageFlag(item.list.language),
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
            text = pluralStringResource(
                CoreUiR.plurals.dictee_word_count_plural,
                item.list.wordCount,
                item.list.wordCount,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Word preview chips
        if (item.wordPreview.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item.wordPreview.forEach { word ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = word,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
                if (item.list.wordCount > item.wordPreview.size) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "+${item.list.wordCount - item.wordPreview.size}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val masteryPercent = if (item.list.wordCount > 0) {
            item.list.masteredCount.toFloat() / item.list.wordCount
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
            items = listOf(
                DicteeListItem(
                    list = DicteeList(
                        id = "1",
                        profileId = "p",
                        title = "Les Animaux",
                        language = "fr",
                        wordCount = 10,
                        masteredCount = 7,
                        createdAt = kotlinx.datetime.Clock.System.now(),
                        updatedAt = kotlinx.datetime.Clock.System.now(),
                    ),
                    wordPreview = listOf("chat", "chien", "oiseau", "poisson"),
                ),
                DicteeListItem(
                    list = DicteeList(
                        id = "2",
                        profileId = "p",
                        title = "Week 2 \u2014 Colours",
                        language = "fr",
                        wordCount = 8,
                        masteredCount = 3,
                        createdAt = kotlinx.datetime.Clock.System.now(),
                        updatedAt = kotlinx.datetime.Clock.System.now(),
                    ),
                    wordPreview = listOf("rouge", "bleu", "vert"),
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
            items = listOf(
                DicteeListItem(
                    list = DicteeList(
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
                DicteeListItem(
                    list = DicteeList(
                        id = "2",
                        profileId = "p",
                        title = "Week 2 \u2014 Colours",
                        language = "fr",
                        wordCount = 8,
                        masteredCount = 3,
                        createdAt = kotlinx.datetime.Clock.System.now(),
                        updatedAt = kotlinx.datetime.Clock.System.now(),
                    ),
                ),
            ),
        ),
        snackbarHostState = SnackbarHostState(),
        onIntent = {},
    )
}
