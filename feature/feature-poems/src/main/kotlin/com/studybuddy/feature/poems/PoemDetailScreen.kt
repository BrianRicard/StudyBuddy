package com.studybuddy.feature.poems

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun PoemDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PoemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PoemDetailEffect.SpeakLine -> {
                    // TTS handled by parent or injected manager
                }
                is PoemDetailEffect.StopSpeaking -> {
                    // TTS stop
                }
            }
        }
    }

    PoemDetailContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemDetailContent(
    state: PoemDetailState,
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

    val poem = state.poem ?: return

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (state.isReadingAloud) {
                        onIntent(PoemDetailIntent.StopReadAloud)
                    } else {
                        onIntent(PoemDetailIntent.StartReadAloud)
                    }
                },
            ) {
                Icon(
                    imageVector = if (state.isReadingAloud) {
                        Icons.Filled.Stop
                    } else {
                        Icons.AutoMirrored.Filled.VolumeUp
                    },
                    contentDescription = stringResource(
                        if (state.isReadingAloud) {
                            CoreUiR.string.poems_stop_reading
                        } else {
                            CoreUiR.string.poems_read_aloud
                        },
                    ),
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
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

            itemsIndexed(poem.lines) { index, line ->
                PoemLineItem(
                    line = line,
                    isHighlighted = state.isReadingAloud && index == state.currentReadLine,
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (state.readingScore != null) {
                    ReadingScoreCard(score = state.readingScore)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PoemLineItem(
    line: String,
    isHighlighted: Boolean,
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
            style = MaterialTheme.typography.bodyLarge,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )
    }
}

@Composable
private fun ReadingScoreCard(score: Float) {
    val percentage = (score * 100).toInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(CoreUiR.string.poems_reading_score),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when {
                percentage >= 90 -> stringResource(CoreUiR.string.poems_score_excellent)
                percentage >= 70 -> stringResource(CoreUiR.string.poems_score_good)
                percentage >= 50 -> stringResource(CoreUiR.string.poems_score_keep_trying)
                else -> stringResource(CoreUiR.string.poems_score_practice_more)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

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
