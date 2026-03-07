package com.studybuddy.feature.reading.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.model.ReadingTheme
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.reading.R

@Composable
fun ReadingHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPassage: (String) -> Unit,
    viewModel: ReadingHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ReadingHomeEffect.NavigateToPassage -> onNavigateToPassage(effect.passageId)
            }
        }
    }

    ReadingHomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingHomeContent(
    state: ReadingHomeState,
    onIntent: (ReadingHomeIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reading_title)) },
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
                .padding(padding),
        ) {
            LanguageChips(
                selectedLanguage = state.selectedLanguage,
                onSelect = { onIntent(ReadingHomeIntent.SelectLanguage(it)) },
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.passages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.reading_no_passages),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.passages, key = { it.id }) { passage ->
                        PassageCard(
                            passage = passage,
                            onClick = {
                                if (!passage.isLocked) {
                                    onIntent(ReadingHomeIntent.OpenPassage(passage.id))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageChips(
    selectedLanguage: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedLanguage == "EN",
            onClick = { onSelect("EN") },
            label = { Text("English") },
        )
        FilterChip(
            selected = selectedLanguage == "FR",
            onClick = { onSelect("FR") },
            label = { Text("Fran\u00e7ais") },
        )
        FilterChip(
            selected = selectedLanguage == "DE",
            onClick = { onSelect("DE") },
            label = { Text("Deutsch") },
        )
    }
}

@Composable
private fun PassageCard(
    passage: ReadingPassage,
    onClick: () -> Unit,
) {
    val alpha = if (passage.isLocked) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .alpha(alpha),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = passage.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ThemeTag(theme = passage.theme)
                    Text(
                        text = "~${passage.wordCount} ${stringResource(R.string.reading_words)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TierIndicator(tier = passage.tier)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (passage.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.reading_locked),
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (passage.bestScore != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${passage.bestScore}/${passage.bestTotal}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeTag(theme: ReadingTheme) {
    val label = when (theme) {
        ReadingTheme.ANIMALS -> stringResource(R.string.reading_theme_animals)
        ReadingTheme.ADVENTURE -> stringResource(R.string.reading_theme_adventure)
        ReadingTheme.FAMILY -> stringResource(R.string.reading_theme_family)
        ReadingTheme.SCHOOL -> stringResource(R.string.reading_theme_school)
        ReadingTheme.NATURE -> stringResource(R.string.reading_theme_nature)
        ReadingTheme.SCIENCE -> stringResource(R.string.reading_theme_science)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun TierIndicator(tier: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(tier) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.tertiary,
            )
        }
        repeat(4 - tier) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .alpha(0.2f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadingHomePreview() {
    StudyBuddyTheme {
        ReadingHomeContent(
            state = ReadingHomeState(
                isLoading = false,
                passages = listOf(
                    ReadingPassage(
                        id = "1",
                        language = "EN",
                        tier = 1,
                        theme = ReadingTheme.ANIMALS,
                        title = "The Tortoise and the Hare",
                        passage = "",
                        wordCount = 62,
                        source = "PUBLIC_DOMAIN",
                        sourceAttribution = "Adapted from Aesop",
                        questions = emptyList(),
                        bestScore = 3,
                        bestTotal = 3,
                    ),
                    ReadingPassage(
                        id = "2",
                        language = "EN",
                        tier = 2,
                        theme = ReadingTheme.ADVENTURE,
                        title = "The Boy Who Cried Wolf",
                        passage = "",
                        wordCount = 78,
                        source = "PUBLIC_DOMAIN",
                        sourceAttribution = "Adapted from Aesop",
                        questions = emptyList(),
                        isLocked = true,
                    ),
                ),
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
