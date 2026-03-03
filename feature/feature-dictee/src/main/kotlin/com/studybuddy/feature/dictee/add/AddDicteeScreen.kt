package com.studybuddy.feature.dictee.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun AddDicteeScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddDicteeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AddDicteeEffect.NavigateBack -> onNavigateBack()
                is AddDicteeEffect.ShowSnackbar -> {
                    val message = if (effect.formatArg > 0) {
                        context.getString(effect.messageResId, effect.formatArg)
                    } else {
                        context.getString(effect.messageResId)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    AddDicteeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDicteeContent(
    state: AddDicteeState,
    snackbarHostState: SnackbarHostState,
    onIntent: (AddDicteeIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val txtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (text != null) {
                onIntent(AddDicteeIntent.ImportFile(text, "text/plain"))
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let {
            val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (text != null) {
                onIntent(AddDicteeIntent.ImportFile(text, "text/csv"))
            }
        }
    }

    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (text != null) {
                onIntent(AddDicteeIntent.ImportFile(text, "application/json"))
            }
        }
    }

    if (showDeleteDialog) {
        DeleteListDialog(
            onConfirm = {
                showDeleteDialog = false
                onIntent(AddDicteeIntent.DeleteList)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditMode) {
                                CoreUiR.string.dictee_edit_list
                            } else {
                                CoreUiR.string.dictee_new_word_list
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (state.isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(CoreUiR.string.dictee_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            // Scrollable form fields at the top
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onIntent(AddDicteeIntent.UpdateTitle(it)) },
                    label = { Text(stringResource(CoreUiR.string.dictee_list_title_label)) },
                    placeholder = { Text(stringResource(CoreUiR.string.dictee_list_title_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(CoreUiR.string.dictee_language),
                    style = MaterialTheme.typography.labelLarge,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LanguageDicteeChip("fr", "Fran\u00e7ais", state.language, onIntent)
                    LanguageDicteeChip("en", "English", state.language, onIntent)
                    LanguageDicteeChip("de", "Deutsch", state.language, onIntent)
                }

                // Word entry row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = state.currentWordInput,
                        onValueChange = { onIntent(AddDicteeIntent.UpdateWordInput(it)) },
                        label = { Text(stringResource(CoreUiR.string.dictee_add_word)) },
                        placeholder = {
                            Text(stringResource(CoreUiR.string.dictee_word_entry_hint))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { onIntent(AddDicteeIntent.AddWord) },
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { onIntent(AddDicteeIntent.AddWord) },
                        enabled = state.currentWordInput.isNotBlank(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(CoreUiR.string.dictee_add),
                        )
                    }
                }

                if (state.words.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            CoreUiR.string.dictee_words_count,
                            state.words.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Word list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.words, key = { it.id }) { draft ->
                    WordDraftItem(
                        word = draft.word,
                        onRemove = { onIntent(AddDicteeIntent.RemoveWord(draft.id)) },
                    )
                }
            }

            // Bottom section: import + save
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = stringResource(CoreUiR.string.dictee_or_import),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { txtLauncher.launch("text/plain") },
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(CoreUiR.string.dictee_import_txt))
                    }
                    OutlinedButton(
                        onClick = { csvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values")) },
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(CoreUiR.string.dictee_import_csv))
                    }
                    OutlinedButton(
                        onClick = { jsonLauncher.launch("application/json") },
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(CoreUiR.string.dictee_import_json))
                    }
                }

                Button(
                    onClick = { onIntent(AddDicteeIntent.Save) },
                    enabled = state.isValid && !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            stringResource(
                                if (state.isEditMode) {
                                    CoreUiR.string.dictee_save_list
                                } else {
                                    CoreUiR.string.dictee_create
                                },
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WordDraftItem(
    word: String,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(CoreUiR.string.dictee_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LanguageDicteeChip(
    code: String,
    label: String,
    selectedLanguage: String,
    onIntent: (AddDicteeIntent) -> Unit,
) {
    FilterChip(
        selected = selectedLanguage == code,
        onClick = { onIntent(AddDicteeIntent.UpdateLanguage(code)) },
        label = { Text(label) },
    )
}

@Composable
private fun DeleteListDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CoreUiR.string.dictee_delete_list_title)) },
        text = { Text(stringResource(CoreUiR.string.dictee_delete_list_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(CoreUiR.string.dictee_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.dictee_cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AddDicteeScreenPreview() {
    StudyBuddyTheme {
        AddDicteeContent(
            state = AddDicteeState(
                title = "Week 1 Words",
                words = listOf(
                    DicteeWordDraft("1", "bonjour"),
                    DicteeWordDraft("2", "merci"),
                    DicteeWordDraft("3", "au revoir"),
                ),
            ),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddDicteeEditModePreview() {
    StudyBuddyTheme {
        AddDicteeContent(
            state = AddDicteeState(
                title = "Difficult Words",
                isEditMode = true,
                editingSetId = "abc",
                words = listOf(
                    DicteeWordDraft("1", "aujourd'hui"),
                    DicteeWordDraft("2", "malheureusement"),
                ),
            ),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
