package com.studybuddy.feature.poems

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun PoemsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: PoemsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PoemsEffect.NavigateToDetail -> onNavigateToDetail(effect.poemId)
            }
        }
    }

    PoemsContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onNavigateToCreate = onNavigateToCreate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemsContent(
    state: PoemsState,
    onIntent: (PoemsIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
) {
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
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(CoreUiR.string.poems_create),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
            ) {
                Tab(
                    selected = state.selectedTab == PoemsTab.BROWSE,
                    onClick = { onIntent(PoemsIntent.SelectTab(PoemsTab.BROWSE)) },
                    text = { Text(stringResource(CoreUiR.string.poems_tab_browse)) },
                )
                Tab(
                    selected = state.selectedTab == PoemsTab.FAVOURITES,
                    onClick = { onIntent(PoemsIntent.SelectTab(PoemsTab.FAVOURITES)) },
                    text = { Text(stringResource(CoreUiR.string.poems_tab_favourites)) },
                )
                Tab(
                    selected = state.selectedTab == PoemsTab.MY_POEMS,
                    onClick = { onIntent(PoemsIntent.SelectTab(PoemsTab.MY_POEMS)) },
                    text = { Text(stringResource(CoreUiR.string.poems_tab_my_poems)) },
                )
            }

            AnimatedVisibility(visible = state.selectedTab == PoemsTab.BROWSE) {
                LanguageChips(
                    selectedLanguage = state.selectedLanguage,
                    onSelect = { onIntent(PoemsIntent.SelectLanguage(it)) },
                )
            }

            when {
                state.isLoading && state.selectedTab == PoemsTab.BROWSE -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.displayPoems.isEmpty() -> {
                    EmptyPoemsState(tab = state.selectedTab)
                }
                else -> {
                    PoemsList(
                        poems = state.displayPoems,
                        onPoemClick = { onIntent(PoemsIntent.OpenPoem(it.id)) },
                        onFavouriteClick = { onIntent(PoemsIntent.ToggleFavourite(it)) },
                    )
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
        LanguageChip("en", "English", selectedLanguage, onSelect)
        LanguageChip("fr", "Fran\u00e7ais", selectedLanguage, onSelect)
        LanguageChip("de", "Deutsch", selectedLanguage, onSelect)
    }
}

@Composable
private fun LanguageChip(
    code: String,
    label: String,
    selectedLanguage: String,
    onSelect: (String) -> Unit,
) {
    FilterChip(
        selected = selectedLanguage == code,
        onClick = { onSelect(code) },
        label = { Text(label) },
    )
}

@Composable
private fun PoemsList(
    poems: List<Poem>,
    onPoemClick: (Poem) -> Unit,
    onFavouriteClick: (Poem) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(poems, key = { it.id }) { poem ->
            PoemCard(
                poem = poem,
                onClick = { onPoemClick(poem) },
                onFavouriteClick = { onFavouriteClick(poem) },
            )
        }
    }
}

@Composable
private fun PoemCard(
    poem: Poem,
    onClick: () -> Unit,
    onFavouriteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
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
                    text = poem.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = poem.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = poem.lines.firstOrNull().orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onFavouriteClick) {
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
        }
    }
}

@Composable
private fun EmptyPoemsState(tab: PoemsTab) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = when (tab) {
                    PoemsTab.MY_POEMS -> Icons.Default.Edit
                    PoemsTab.FAVOURITES -> Icons.Filled.FavoriteBorder
                    PoemsTab.BROWSE -> Icons.Filled.FavoriteBorder
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(
                    when (tab) {
                        PoemsTab.MY_POEMS -> CoreUiR.string.poems_no_my_poems
                        PoemsTab.FAVOURITES -> CoreUiR.string.poems_no_favourites
                        PoemsTab.BROWSE -> CoreUiR.string.poems_no_poems
                    },
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PoemsScreenPreview() {
    StudyBuddyTheme {
        PoemsContent(
            state = PoemsState(
                isLoading = false,
                poems = listOf(
                    Poem(
                        id = "1",
                        title = "Le Corbeau et le Renard",
                        author = "Jean de La Fontaine",
                        lines = listOf("Ma\u00eetre Corbeau, sur un arbre perch\u00e9"),
                        language = "fr",
                        source = PoemSource.BUNDLED,
                    ),
                    Poem(
                        id = "2",
                        title = "Wandrers Nachtlied",
                        author = "Goethe",
                        lines = listOf("\u00dcber allen Gipfeln ist Ruh"),
                        language = "de",
                        source = PoemSource.BUNDLED,
                        isFavourite = true,
                    ),
                ),
            ),
            onIntent = {},
            onNavigateBack = {},
            onNavigateToCreate = {},
        )
    }
}
