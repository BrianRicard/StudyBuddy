package com.studybuddy.feature.conjugation.speak

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.conjugation.components.StepProgressBar
import com.studybuddy.feature.conjugation.components.praiseRes

@Composable
fun SpeakScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpeakViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onIntent(SpeakIntent.AudioPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SpeakEffect.RequestAudioPermission ->
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

                is SpeakEffect.Completed -> onNavigateBack()
            }
        }
    }

    SpeakContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeakContent(
    state: SpeakState,
    onIntent: (SpeakIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.conjugation_speak_title)) },
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
        val stage = state.stage ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepProgressBar(
                current = state.index + if (state.phase == SpeakPhase.HEARD) 1 else 0,
                total = state.total,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(CoreUiR.string.conjugation_speak_instruction),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stage.verb.display(state.person),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(onClick = { onIntent(SpeakIntent.PlayForm) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(CoreUiR.string.conjugation_speak_listen))
            }

            when (state.phase) {
                SpeakPhase.PROCESSING -> ProcessingSection()
                SpeakPhase.HEARD -> HeardSection(state = state, onIntent = onIntent)
                else -> PracticeSection(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun ProcessingSection() {
    CircularProgressIndicator()
    Text(
        text = stringResource(CoreUiR.string.conjugation_speak_processing),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun HeardSection(
    state: SpeakState,
    onIntent: (SpeakIntent) -> Unit,
) {
    Text(
        text = stringResource(praiseRes(state.index)),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.tertiary,
    )
    Text(
        text = stringResource(CoreUiR.string.conjugation_speak_heard),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
    StudyBuddyButton(
        text = stringResource(CoreUiR.string.conjugation_next),
        onClick = { onIntent(SpeakIntent.Next) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isSaving,
    )
}

@Composable
private fun PracticeSection(
    state: SpeakState,
    onIntent: (SpeakIntent) -> Unit,
) {
    if (state.phase == SpeakPhase.ENCOURAGE) {
        Text(
            text = stringResource(CoreUiR.string.conjugation_speak_encourage),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }

    if (state.isMicMode) {
        MicButton(state = state, onIntent = onIntent)
    } else {
        EchoSection(onIntent = onIntent)
    }

    if (state.phase == SpeakPhase.ENCOURAGE) {
        // Never blocking: after a try the child may simply move on.
        OutlinedButton(onClick = { onIntent(SpeakIntent.Next) }) {
            Text(stringResource(CoreUiR.string.conjugation_next))
        }
    }
}

@Composable
private fun MicButton(
    state: SpeakState,
    onIntent: (SpeakIntent) -> Unit,
) {
    val isRecording = state.phase == SpeakPhase.RECORDING

    FilledIconButton(
        onClick = {
            onIntent(if (isRecording) SpeakIntent.StopRecording else SpeakIntent.StartRecording)
        },
        modifier = Modifier.size(88.dp),
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = stringResource(
                if (isRecording) {
                    CoreUiR.string.conjugation_speak_stop
                } else {
                    CoreUiR.string.conjugation_speak_tap_mic
                },
            ),
            modifier = Modifier.size(40.dp),
        )
    }
    Text(
        text = stringResource(
            if (isRecording) {
                CoreUiR.string.conjugation_speak_stop
            } else {
                CoreUiR.string.conjugation_speak_tap_mic
            },
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EchoSection(onIntent: (SpeakIntent) -> Unit) {
    Text(
        text = stringResource(CoreUiR.string.conjugation_speak_echo_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    FilledIconButton(
        onClick = { onIntent(SpeakIntent.ConfirmEcho) },
        modifier = Modifier.size(88.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = stringResource(CoreUiR.string.conjugation_speak_i_said_it),
            modifier = Modifier.size(40.dp),
        )
    }
    Text(
        text = stringResource(CoreUiR.string.conjugation_speak_i_said_it),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun SpeakPreview() {
    StudyBuddyTheme {
        SpeakContent(
            state = SpeakState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
