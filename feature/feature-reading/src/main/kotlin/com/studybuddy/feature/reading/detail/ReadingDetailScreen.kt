package com.studybuddy.feature.reading.detail

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.feature.reading.R

@Composable
fun ReadingDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestions: (String, Long) -> Unit,
    viewModel: ReadingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ReadingDetailEffect.NavigateToQuestions ->
                    onNavigateToQuestions(effect.passageId, effect.readingTimeMs)
            }
        }
    }

    ReadingDetailContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReadingDetailContent(
    state: ReadingDetailState,
    onIntent: (ReadingDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val passage = state.passage

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(passage?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    SmallFloatingActionButton(
                        onClick = {
                            if (state.isReadingAloud) {
                                onIntent(ReadingDetailIntent.StopReadAloud)
                            } else {
                                onIntent(ReadingDetailIntent.StartReadAloud)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Icon(
                            imageVector = if (state.isReadingAloud) {
                                Icons.Filled.Stop
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = stringResource(
                                if (state.isReadingAloud) {
                                    R.string.reading_stop_reading
                                } else {
                                    R.string.reading_read_aloud
                                },
                            ),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading || passage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                val attribution = passage.sourceAttribution
                if (attribution != null) {
                    Text(
                        text = attribution,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                PassageText(
                    text = passage.passage,
                    currentHighlightSentence = state.currentHighlightSentence,
                    onWordTap = { onIntent(ReadingDetailIntent.TapWord(it)) },
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onIntent(ReadingDetailIntent.ReadyForQuestions) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.reading_im_ready))
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Word popup
            if (state.wordPopup != null) {
                WordPopup(
                    data = state.wordPopup,
                    onDismiss = { onIntent(ReadingDetailIntent.DismissWordPopup) },
                    onSpeak = { onIntent(ReadingDetailIntent.SpeakWord(state.wordPopup.word)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PassageText(
    text: String,
    currentHighlightSentence: Int,
    onWordTap: (String) -> Unit,
) {
    val sentences = ReadingDetailViewModel.splitSentences(text)

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sentences.forEachIndexed { sentenceIndex, sentence ->
            val isHighlighted = sentenceIndex == currentHighlightSentence
            val bgColor by animateColorAsState(
                targetValue = if (isHighlighted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.background
                },
                label = "sentenceHighlight",
            )
            val textColor = if (isHighlighted) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onBackground
            }

            val words = sentence.split(" ")
            words.forEach { word ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(bgColor)
                        .clickable { onWordTap(word) }
                        .padding(horizontal = 1.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "$word ",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                        ),
                        color = textColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun WordPopup(
    data: WordPopupData,
    onDismiss: () -> Unit,
    onSpeak: () -> Unit,
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = data.word,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSpeak) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = stringResource(R.string.reading_speak_word),
                        )
                    }
                }

                if (data.entry != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = data.entry.definition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (data.entry.translations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        data.entry.translations.forEach { (lang, translation) ->
                            Row {
                                Text(
                                    text = "$lang: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = translation,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.reading_tap_to_hear),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
