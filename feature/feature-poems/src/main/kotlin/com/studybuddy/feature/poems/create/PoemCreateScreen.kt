package com.studybuddy.feature.poems.create

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun PoemCreateScreen(
    onNavigateBack: () -> Unit,
    viewModel: PoemCreateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PoemCreateEffect.NavigateBack -> onNavigateBack()
                is PoemCreateEffect.ShowSnackbar -> {
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

    PoemCreateContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemCreateContent(
    state: PoemCreateState,
    snackbarHostState: SnackbarHostState,
    onIntent: (PoemCreateIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current

    val txtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (text != null) {
                onIntent(PoemCreateIntent.ImportFile(text, "text/plain"))
            }
        }
    }

    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (text != null) {
                onIntent(PoemCreateIntent.ImportFile(text, "application/json"))
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.poems_create)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { onIntent(PoemCreateIntent.UpdateTitle(it)) },
                label = { Text(stringResource(CoreUiR.string.poems_title_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.author,
                onValueChange = { onIntent(PoemCreateIntent.UpdateAuthor(it)) },
                label = { Text(stringResource(CoreUiR.string.poems_author_label)) },
                placeholder = { Text(stringResource(CoreUiR.string.poems_author_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(CoreUiR.string.poems_language),
                style = MaterialTheme.typography.labelLarge,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LanguageCreateChip("en", "English", state.language, onIntent)
                LanguageCreateChip("fr", "Fran\u00e7ais", state.language, onIntent)
                LanguageCreateChip("de", "Deutsch", state.language, onIntent)
            }

            OutlinedTextField(
                value = state.body,
                onValueChange = { onIntent(PoemCreateIntent.UpdateBody(it)) },
                label = { Text(stringResource(CoreUiR.string.poems_body_label)) },
                placeholder = { Text(stringResource(CoreUiR.string.poems_body_hint)) },
                minLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { onIntent(PoemCreateIntent.Save) },
                enabled = state.isValid && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(CoreUiR.string.poems_save))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(CoreUiR.string.poems_or_import),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { txtLauncher.launch("text/plain") },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(CoreUiR.string.poems_import_txt))
                }
                OutlinedButton(
                    onClick = { jsonLauncher.launch("application/json") },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(CoreUiR.string.poems_import_json))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageCreateChip(
    code: String,
    label: String,
    selectedLanguage: String,
    onIntent: (PoemCreateIntent) -> Unit,
) {
    FilterChip(
        selected = selectedLanguage == code,
        onClick = { onIntent(PoemCreateIntent.UpdateLanguage(code)) },
        label = { Text(label) },
    )
}

@Preview(showBackground = true)
@Composable
private fun PoemCreateScreenPreview() {
    StudyBuddyTheme {
        PoemCreateContent(
            state = PoemCreateState(title = "My Poem", body = "Roses are red\nViolets are blue"),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
