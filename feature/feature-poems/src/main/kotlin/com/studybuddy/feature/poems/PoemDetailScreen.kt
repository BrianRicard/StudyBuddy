package com.studybuddy.feature.poems

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.adaptive.ContentConstraint
import com.studybuddy.core.ui.adaptive.LayoutType
import com.studybuddy.core.ui.adaptive.LocalLayoutType
import com.studybuddy.core.ui.adaptive.TwoPaneLayout
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.poems.detail.ModelDownloadSheet
import com.studybuddy.feature.poems.detail.ResultBottomSheet
import com.studybuddy.feature.poems.detail.ResultContent
import com.studybuddy.feature.poems.detail.WordInfo
import com.studybuddy.feature.poems.detail.WordState

private val CorrectBg = Color(0xFFC8E6C9)
private val IncorrectBg = Color(0xFFFFCDD2)
private val UnclearBg = Color(0xFFFFF9C4)
private const val SKIPPED_ALPHA = 0.35f

@Composable
fun PoemDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PoemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onIntent(PoemDetailIntent.AudioPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PoemDetailEffect.RequestAudioPermission -> {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                is PoemDetailEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                }
            }
        }
    }

    PoemDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemDetailContent(
    state: PoemDetailState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (PoemDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    if (state.isLoading) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(CoreUiR.string.poems_title)) },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val layoutType = LocalLayoutType.current
    when (layoutType) {
        LayoutType.COMPACT -> PoemDetailCompactContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = onIntent,
            onNavigateBack = onNavigateBack,
        )
        LayoutType.MEDIUM -> PoemDetailMediumContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = onIntent,
            onNavigateBack = onNavigateBack,
        )
        LayoutType.EXPANDED -> PoemDetailExpandedContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = onIntent,
            onNavigateBack = onNavigateBack,
        )
    }
}

// region COMPACT layout — identical to previous implementation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemDetailCompactContent(
    state: PoemDetailState,
    snackbarHostState: SnackbarHostState,
    onIntent: (PoemDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val poem = state.poem ?: return
    val isRecording = state.recordingState == RecordingState.RECORDING
    val isProcessing = state.recordingState == RecordingState.PROCESSING

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PoemDetailTopBar(poem = poem, onIntent = onIntent, onNavigateBack = onNavigateBack)
        },
        floatingActionButton = {
            DualFabs(
                isRecording = isRecording,
                isProcessing = isProcessing,
                isReadingAloud = state.isReadingAloud,
                amplitude = state.currentAmplitude,
                onMicClick = {
                    if (isRecording) {
                        onIntent(PoemDetailIntent.StopRecording)
                    } else {
                        onIntent(PoemDetailIntent.StartRecording)
                    }
                },
                onTtsClick = {
                    if (state.isReadingAloud) {
                        onIntent(PoemDetailIntent.StopReadAloud)
                    } else {
                        onIntent(PoemDetailIntent.StartReadAloud)
                    }
                },
            )
        },
    ) { padding ->
        PoemLines(
            state = state,
            poem = poem,
            isProcessing = isProcessing,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
        )
    }

    // Result bottom sheet (COMPACT only)
    if (state.showResultSheet && state.score != null) {
        ResultBottomSheet(
            score = state.score,
            words = state.words,
            onTapWord = { onIntent(PoemDetailIntent.TapWord(it)) },
            onTryAgain = { onIntent(PoemDetailIntent.Reset) },
            onDismiss = { onIntent(PoemDetailIntent.DismissResultSheet) },
        )
    }

    if (state.showModelDownload) {
        ModelDownloadSheet(
            progress = state.modelDownloadProgress,
            onDismiss = {},
        )
    }
}

// endregion

// region MEDIUM layout — constrained width, larger FABs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemDetailMediumContent(
    state: PoemDetailState,
    snackbarHostState: SnackbarHostState,
    onIntent: (PoemDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val poem = state.poem ?: return
    val isRecording = state.recordingState == RecordingState.RECORDING
    val isProcessing = state.recordingState == RecordingState.PROCESSING

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PoemDetailTopBar(poem = poem, onIntent = onIntent, onNavigateBack = onNavigateBack)
        },
        floatingActionButton = {
            DualFabs(
                isRecording = isRecording,
                isProcessing = isProcessing,
                isReadingAloud = state.isReadingAloud,
                amplitude = state.currentAmplitude,
                onMicClick = {
                    if (isRecording) {
                        onIntent(PoemDetailIntent.StopRecording)
                    } else {
                        onIntent(PoemDetailIntent.StartRecording)
                    }
                },
                onTtsClick = {
                    if (state.isReadingAloud) {
                        onIntent(PoemDetailIntent.StopReadAloud)
                    } else {
                        onIntent(PoemDetailIntent.StartReadAloud)
                    }
                },
            )
        },
    ) { padding ->
        ContentConstraint(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PoemLines(
                state = state,
                poem = poem,
                isProcessing = isProcessing,
                poemTextStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            )
        }
    }

    if (state.showResultSheet && state.score != null) {
        ResultBottomSheet(
            score = state.score,
            words = state.words,
            onTapWord = { onIntent(PoemDetailIntent.TapWord(it)) },
            onTryAgain = { onIntent(PoemDetailIntent.Reset) },
            onDismiss = { onIntent(PoemDetailIntent.DismissResultSheet) },
        )
    }

    if (state.showModelDownload) {
        ModelDownloadSheet(
            progress = state.modelDownloadProgress,
            onDismiss = {},
        )
    }
}

// endregion

// region EXPANDED layout — two-pane: poem on left, controls + results on right

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemDetailExpandedContent(
    state: PoemDetailState,
    snackbarHostState: SnackbarHostState,
    onIntent: (PoemDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val poem = state.poem ?: return
    val isRecording = state.recordingState == RecordingState.RECORDING
    val isProcessing = state.recordingState == RecordingState.PROCESSING
    val isScored = state.recordingState == RecordingState.SCORED

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PoemDetailTopBar(poem = poem, onIntent = onIntent, onNavigateBack = onNavigateBack)
        },
    ) { padding ->
        TwoPaneLayout(
            leftWeight = 0.4f,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            leftPane = {
                // Left pane: poem text card (always visible)
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                ) {
                    PoemLines(
                        state = state,
                        poem = poem,
                        isProcessing = false,
                        poemTextStyle = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    )
                }
            },
            rightPane = {
                // Right pane: recording controls + inline results
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Recording controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DualFabsHorizontal(
                            isRecording = isRecording,
                            isProcessing = isProcessing,
                            isReadingAloud = state.isReadingAloud,
                            amplitude = state.currentAmplitude,
                            onMicClick = {
                                if (isRecording) {
                                    onIntent(PoemDetailIntent.StopRecording)
                                } else {
                                    onIntent(PoemDetailIntent.StartRecording)
                                }
                            },
                            onTtsClick = {
                                if (state.isReadingAloud) {
                                    onIntent(PoemDetailIntent.StopReadAloud)
                                } else {
                                    onIntent(PoemDetailIntent.StartReadAloud)
                                }
                            },
                        )
                    }

                    // Processing indicator
                    if (isProcessing) {
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(CoreUiR.string.poems_processing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Inline results (no bottom sheet on EXPANDED)
                    if (isScored && state.score != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        ResultContent(
                            score = state.score,
                            words = state.words,
                            onTapWord = { onIntent(PoemDetailIntent.TapWord(it)) },
                            onTryAgain = { onIntent(PoemDetailIntent.Reset) },
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            },
        )
    }

    if (state.showModelDownload) {
        ModelDownloadSheet(
            progress = state.modelDownloadProgress,
            onDismiss = {},
        )
    }
}

// endregion

// region Shared composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemDetailTopBar(
    poem: Poem,
    onIntent: (PoemDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(poem.title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(CoreUiR.string.navigate_back),
                )
            }
        },
        actions = {
            IconButton(onClick = { onIntent(PoemDetailIntent.ToggleFavourite) }) {
                Icon(
                    imageVector = if (poem.isFavourite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = stringResource(CoreUiR.string.poems_favourite),
                    tint = if (poem.isFavourite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        },
    )
}

@Composable
private fun PoemLines(
    state: PoemDetailState,
    poem: Poem,
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
    poemTextStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val wordsByLine = state.words.groupBy { it.lineIndex }
    val hasScoring = state.recordingState == RecordingState.SCORED

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = poem.author,
                style = MaterialTheme.typography.titleSmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        itemsIndexed(poem.lines) { index, _ ->
            val lineWords = wordsByLine[index] ?: emptyList()
            if (hasScoring) {
                PoemLineWords(
                    words = lineWords,
                    isHighlighted = state.isReadingAloud && index == state.currentReadLine,
                    textStyle = poemTextStyle,
                    onTapWord = { globalIdx -> state.poem?.let { onTapWordIntent(globalIdx) } },
                )
            } else {
                PoemLineItem(
                    line = lineWords.joinToString(" ") { it.text },
                    isHighlighted = state.isReadingAloud && index == state.currentReadLine,
                    textStyle = poemTextStyle,
                )
            }
        }

        if (isProcessing) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(CoreUiR.string.poems_processing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

private fun onTapWordIntent(globalIdx: Int): PoemDetailIntent = PoemDetailIntent.TapWord(globalIdx)

@Composable
private fun DualFabs(
    isRecording: Boolean,
    isProcessing: Boolean,
    isReadingAloud: Boolean,
    amplitude: Float,
    onMicClick: () -> Unit,
    onTtsClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // TTS FAB (smaller, secondary)
        if (!isRecording && !isProcessing) {
            SmallFloatingActionButton(
                onClick = onTtsClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = if (isReadingAloud) {
                        Icons.Filled.Stop
                    } else {
                        Icons.AutoMirrored.Filled.VolumeUp
                    },
                    contentDescription = stringResource(
                        if (isReadingAloud) {
                            CoreUiR.string.poems_stop_reading
                        } else {
                            CoreUiR.string.poems_read_aloud
                        },
                    ),
                )
            }
        }

        // Mic FAB (primary, pulses red while recording)
        MicFab(
            isRecording = isRecording,
            isProcessing = isProcessing,
            amplitude = amplitude,
            onClick = onMicClick,
        )
    }
}

/**
 * Horizontal FAB layout for EXPANDED two-pane right panel.
 */
@Composable
private fun DualFabsHorizontal(
    isRecording: Boolean,
    isProcessing: Boolean,
    isReadingAloud: Boolean,
    amplitude: Float,
    onMicClick: () -> Unit,
    onTtsClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // TTS FAB
        if (!isRecording && !isProcessing) {
            FloatingActionButton(
                onClick = onTtsClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = if (isReadingAloud) {
                        Icons.Filled.Stop
                    } else {
                        Icons.AutoMirrored.Filled.VolumeUp
                    },
                    contentDescription = stringResource(
                        if (isReadingAloud) {
                            CoreUiR.string.poems_stop_reading
                        } else {
                            CoreUiR.string.poems_read_aloud
                        },
                    ),
                )
            }
        }

        // Mic FAB
        MicFab(
            isRecording = isRecording,
            isProcessing = isProcessing,
            amplitude = amplitude,
            onClick = onMicClick,
        )
    }
}

@Composable
private fun MicFab(
    isRecording: Boolean,
    isProcessing: Boolean,
    amplitude: Float,
    onClick: () -> Unit,
) {
    if (isProcessing) return

    val fabColor = if (isRecording) {
        Color(0xFFE53935)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val scale = if (isRecording) {
        val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1f + amplitude.coerceIn(0f, 0.3f),
            animationSpec = infiniteRepeatable(
                animation = tween(300),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "micScale",
        )
        pulseScale
    } else {
        1f
    }

    FloatingActionButton(
        onClick = onClick,
        containerColor = fabColor,
        modifier = Modifier.scale(scale),
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = stringResource(
                if (isRecording) {
                    CoreUiR.string.poems_stop_recording
                } else {
                    CoreUiR.string.poems_start_recording
                },
            ),
            tint = if (isRecording) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun PoemLineItem(
    line: String,
    isHighlighted: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.background
        },
        label = "lineHighlight",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = line,
            style = textStyle,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PoemLineWords(
    words: List<WordInfo>,
    isHighlighted: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    onTapWord: (Int) -> Unit,
) {
    val lineBg by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.background
        },
        label = "lineHighlight",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(lineBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            words.forEach { word ->
                val wordBg = when (word.state) {
                    WordState.CORRECT -> CorrectBg
                    WordState.INCORRECT -> IncorrectBg
                    WordState.UNCLEAR -> UnclearBg
                    WordState.UNREAD, WordState.SKIPPED -> Color.Transparent
                }

                val wordAlpha = if (word.state == WordState.SKIPPED) SKIPPED_ALPHA else 1f

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(wordBg)
                        .clickable { onTapWord(word.globalIndex) }
                        .padding(horizontal = 2.dp)
                        .alpha(wordAlpha),
                ) {
                    Text(
                        text = word.text,
                        style = textStyle,
                        color = if (isHighlighted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                    )
                }
            }
        }
    }
}

// endregion

@Preview(showBackground = true)
@Composable
private fun PoemDetailPreview() {
    StudyBuddyTheme {
        PoemDetailContent(
            state = PoemDetailState(
                isLoading = false,
                poem = Poem(
                    id = "1",
                    title = "Wandrers Nachtlied",
                    author = "Johann Wolfgang von Goethe",
                    lines = listOf(
                        "\u00dcber allen Gipfeln",
                        "Ist Ruh,",
                        "In allen Wipfeln",
                        "Sp\u00fcrest du",
                        "Kaum einen Hauch;",
                        "Die V\u00f6gelein schweigen im Walde.",
                        "Warte nur, balde",
                        "Ruhest du auch.",
                    ),
                    language = "de",
                    source = PoemSource.BUNDLED,
                ),
                isReadingAloud = true,
                currentReadLine = 2,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
