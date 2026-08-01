package com.studybuddy.feature.math.tables

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.mathfacts.TableGarden
import com.studybuddy.core.domain.model.srs.LeitnerGrowth
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun TablesGardenScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDrill: (mode: String, table: Int?) -> Unit,
    viewModel: TablesGardenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TablesGardenEffect.NavigateToDrill ->
                    onNavigateToDrill(effect.mode.name, effect.table)
            }
        }
    }

    TablesGardenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TablesGardenContent(
    state: TablesGardenState,
    onIntent: (TablesGardenIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.tables_title)) },
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
        if (state.isLoading) {
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(CoreUiR.string.tables_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onIntent(TablesGardenIntent.StartRevision) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(CoreUiR.string.srs_revision) + " — " + state.revisionLabel(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onIntent(TablesGardenIntent.StartSurprise) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(CoreUiR.string.srs_surprise))
                    }
                }
            }

            items(state.tables, key = { it.table }) { row ->
                TableRow(row = row, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun TableRow(
    row: TableGarden,
    onIntent: (TablesGardenIntent) -> Unit,
) {
    StudyBuddyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(TablesGardenIntent.OpenTable(row.table)) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(CoreUiR.string.tables_row, row.table),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            // The emoji carries all the progress, so give TalkBack real words.
            val growthLabel = stringResource(row.growth.labelRes)
            Text(
                text = row.growth.emoji,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { contentDescription = growthLabel },
            )
        }
    }
}

/**
 * Due cards first, then unplanted ones — a revision session serves new facts
 * once the due ones run out, so a fresh garden must never claim to be watered.
 */
@Composable
private fun TablesGardenState.revisionLabel(): String = when {
    dueCardCount > 0 -> pluralStringResource(
        CoreUiR.plurals.srs_due_cards_plural,
        dueCardCount,
        dueCardCount,
    )

    newCardCount > 0 -> pluralStringResource(
        CoreUiR.plurals.srs_new_cards_plural,
        newCardCount,
        newCardCount,
    )

    else -> stringResource(CoreUiR.string.srs_all_watered)
}

private val LeitnerGrowth.emoji: String
    get() = when (this) {
        LeitnerGrowth.SEED -> "🌱"
        LeitnerGrowth.SPROUT -> "🌿"
        LeitnerGrowth.FLOWER -> "🌸"
        LeitnerGrowth.TREE -> "🌳"
    }

private val LeitnerGrowth.labelRes: Int
    get() = when (this) {
        LeitnerGrowth.SEED -> CoreUiR.string.srs_growth_seed
        LeitnerGrowth.SPROUT -> CoreUiR.string.srs_growth_sprout
        LeitnerGrowth.FLOWER -> CoreUiR.string.srs_growth_flower
        LeitnerGrowth.TREE -> CoreUiR.string.srs_growth_tree
    }

@Preview(showBackground = true)
@Composable
private fun TablesGardenPreview() {
    StudyBuddyTheme {
        TablesGardenContent(
            state = TablesGardenState(
                dueCardCount = 7,
                tables = (2..9).map { TableGarden(it, LeitnerGrowth.SPROUT) },
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
