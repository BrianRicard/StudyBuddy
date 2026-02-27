package com.studybuddy.feature.dictee.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.EmptyState
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyCard

@Composable
fun DicteeWordEntryScreen(
    onNavigateToPractice: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DicteeWordEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DicteeWordEntryEffect.NavigateToPractice -> onNavigateToPractice(effect.listId)
                is DicteeWordEntryEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(CoreUiR.string.dictee_word_deleted, effect.word.word),
                        actionLabel = context.getString(CoreUiR.string.dictee_undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(DicteeWordEntryIntent.UndoDeleteWord(effect.word))
                    }
                }
                is DicteeWordEntryEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(effect.messageResId),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    DicteeWordEntryContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DicteeWordEntryContent(
    state: DicteeWordEntryState,
    snackbarHostState: SnackbarHostState,
    onIntent: (DicteeWordEntryIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.list?.title ?: stringResource(CoreUiR.string.dictee_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(DicteeWordEntryIntent.ToggleEditMode) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(CoreUiR.string.dictee_toggle_edit),
                            tint = if (state.isEditMode) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp),
        ) {
            when {
                state.isLoading -> LoadingState()
                else -> {
                    if (state.words.isEmpty()) {
                        Box(modifier = Modifier.weight(1f)) {
                            EmptyState(
                                title = stringResource(CoreUiR.string.dictee_no_words),
                                message = stringResource(CoreUiR.string.dictee_no_words_hint),
                            )
                        }
                    } else {
                        val listState = rememberLazyListState()
                        LaunchedEffect(state.words.size) {
                            if (state.words.isNotEmpty()) {
                                listState.animateScrollToItem(state.words.size - 1)
                            }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                top = 8.dp,
                                bottom = 8.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(state.words, key = { it.id }) { word ->
                                WordItem(
                                    word = word,
                                    isEditMode = state.isEditMode,
                                    onPlay = { onIntent(DicteeWordEntryIntent.PlayWord(word.word)) },
                                    onDelete = { onIntent(DicteeWordEntryIntent.DeleteWord(word.id)) },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add word input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = state.newWordText,
                            onValueChange = { onIntent(DicteeWordEntryIntent.UpdateNewWordText(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(CoreUiR.string.dictee_add_a_word)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (state.newWordText.isNotBlank()) {
                                        onIntent(DicteeWordEntryIntent.AddWord)
                                    }
                                },
                            ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StudyBuddyButton(
                            text = stringResource(CoreUiR.string.dictee_add),
                            onClick = { onIntent(DicteeWordEntryIntent.AddWord) },
                            enabled = state.newWordText.isNotBlank(),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StudyBuddyButton(
                        text = stringResource(CoreUiR.string.dictee_start_practice),
                        onClick = { onIntent(DicteeWordEntryIntent.StartPractice) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.words.isNotEmpty(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun WordItem(
    word: DicteeWord,
    isEditMode: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
) {
    StudyBuddyCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mastery indicator dot
            val masteryPercent = if (word.attempts > 0) {
                word.correctCount.toFloat() / word.attempts
            } else {
                0f
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(end = 0.dp),
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = when {
                            word.attempts == 0 -> androidx.compose.ui.graphics.Color.Gray
                            masteryPercent >= 0.8f -> androidx.compose.ui.graphics.Color(0xFF43A047)
                            masteryPercent >= 0.5f -> androidx.compose.ui.graphics.Color(0xFFFDD835)
                            else -> androidx.compose.ui.graphics.Color(0xFFE53935)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = word.word,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = onPlay) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(CoreUiR.string.dictee_play),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            if (isEditMode) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(CoreUiR.string.dictee_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DicteeWordEntryScreenPreview() {
    DicteeWordEntryContent(
        state = DicteeWordEntryState(
            isLoading = false,
            words = listOf(
                DicteeWord(id = "1", listId = "l", word = "maison", attempts = 5, correctCount = 4),
                DicteeWord(id = "2", listId = "l", word = "chat", attempts = 3, correctCount = 1),
                DicteeWord(id = "3", listId = "l", word = "école"),
            ),
        ),
        snackbarHostState = SnackbarHostState(),
        onIntent = {},
        onNavigateBack = {},
    )
}
