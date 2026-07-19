package com.studybuddy.feature.conjugation.atelier

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.conjugation.AtelierVerbGarden
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.model.conjugation.VerbGroup
import com.studybuddy.core.domain.model.srs.LeitnerGrowth
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.modifier.accessibleClickable
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun AtelierScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDrill: (mode: String, verbId: String?, tense: String?) -> Unit,
    viewModel: AtelierViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AtelierEffect.NavigateToDrill ->
                    onNavigateToDrill(effect.mode.name, effect.verbId, effect.tense?.name)
            }
        }
    }

    AtelierContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AtelierContent(
    state: AtelierState,
    snackbarHostState: SnackbarHostState,
    onIntent: (AtelierIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.atelier_title)) },
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
            item { AtelierHeader(state = state, onIntent = onIntent) }

            VerbGroup.entries.forEach { group ->
                val rows = state.verbs.filter { it.verb.group == group }
                if (rows.isEmpty()) return@forEach

                item(key = group.name) {
                    Text(
                        text = group.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(rows, key = { it.verb.id }) { row ->
                    VerbGardenRow(row = row, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun AtelierHeader(
    state: AtelierState,
    onIntent: (AtelierIntent) -> Unit,
) {
    Column {
        Text(
            text = stringResource(CoreUiR.string.atelier_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onIntent(AtelierIntent.StartRevision) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(CoreUiR.string.atelier_revision) + " — " +
                    if (state.dueCardCount > 0) {
                        pluralStringResource(
                            CoreUiR.plurals.atelier_due_cards_plural,
                            state.dueCardCount,
                            state.dueCardCount,
                        )
                    } else {
                        stringResource(CoreUiR.string.atelier_all_watered)
                    },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onIntent(AtelierIntent.StartSurprise) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(CoreUiR.string.atelier_surprise))
        }
    }
}

@Composable
private fun VerbGardenRow(
    row: AtelierVerbGarden,
    onIntent: (AtelierIntent) -> Unit,
) {
    StudyBuddyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = row.verb.infinitive,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            ConjugationTense.entries.forEach { tense ->
                GardenCell(
                    tense = tense,
                    growth = row.growth[tense] ?: LeitnerGrowth.SEED,
                    onClick = { onIntent(AtelierIntent.OpenCell(row.verb.id, tense)) },
                    verbLabel = row.verb.infinitive,
                )
            }
        }
    }
}

@Composable
private fun GardenCell(
    tense: ConjugationTense,
    growth: LeitnerGrowth,
    onClick: () -> Unit,
    verbLabel: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .accessibleClickable(
                label = "$verbLabel, ${tense.displayName}",
                onClick = onClick,
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Text(
            text = growth.emoji,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = tense.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val LeitnerGrowth.emoji: String
    get() = when (this) {
        LeitnerGrowth.SEED -> "🌱"
        LeitnerGrowth.SPROUT -> "🌿"
        LeitnerGrowth.FLOWER -> "🌸"
        LeitnerGrowth.TREE -> "🌳"
    }

@Preview(showBackground = true)
@Composable
private fun AtelierScreenPreview() {
    StudyBuddyTheme {
        AtelierContent(
            state = AtelierState(
                dueCardCount = 7,
                dueVerbCount = 3,
                verbs = FrenchVerbs.all.take(4).map { verb ->
                    AtelierVerbGarden(
                        verb = verb,
                        growth = ConjugationTense.entries.associateWith { LeitnerGrowth.SPROUT },
                    )
                },
                isLoading = false,
            ),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
