package com.studybuddy.feature.math.setup

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.Operator
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun MathSetupScreen(
    viewModel: MathSetupViewModel = hiltViewModel(),
    onStartGame: (MathSetupState) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MathSetupContent(
        state = state,
        onIntent = viewModel::onIntent,
        onStartGame = { onStartGame(state) },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MathSetupContent(
    state: MathSetupState,
    onIntent: (MathSetupIntent) -> Unit,
    onStartGame: () -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.mode_math)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- Operator multi-select ---
            SectionLabel(text = stringResource(CoreUiR.string.math_operators))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Operator.entries.forEach { operator ->
                    val selected = operator in state.selectedOperators
                    val operatorDesc = if (selected) {
                        stringResource(CoreUiR.string.math_operator_selected, operator.name)
                    } else {
                        stringResource(CoreUiR.string.math_operator_not_selected, operator.name)
                    }
                    FilterChip(
                        selected = selected,
                        onClick = {
                            onIntent(MathSetupIntent.ToggleOperator(operator))
                        },
                        label = {
                            Text(
                                text = operator.symbol,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = operatorDesc
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            // --- Number range ---
            SectionLabel(text = stringResource(CoreUiR.string.math_number_range))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(CoreUiR.string.math_min_value, state.numberRangeMin),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(CoreUiR.string.math_max_value, state.numberRangeMax),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            RangeSlider(
                value = state.numberRangeMin.toFloat()..state.numberRangeMax.toFloat(),
                onValueChange = { range ->
                    onIntent(
                        MathSetupIntent.SetRangeMin(range.start.toInt()),
                    )
                    onIntent(
                        MathSetupIntent.SetRangeMax(range.endInclusive.toInt()),
                    )
                },
                valueRange = 1f..MathSetupViewModel.MAX_SETUP_RANGE.toFloat(),
                steps = MathSetupViewModel.MAX_SETUP_RANGE - 2,
                modifier = Modifier.fillMaxWidth(),
            )

            // --- Time per problem ---
            SectionLabel(text = stringResource(CoreUiR.string.math_time_per_problem))
            TimerSegmentedRow(
                selectedSeconds = state.timerSeconds,
                onSelect = { onIntent(MathSetupIntent.SetTimer(it)) },
            )

            // --- Number of problems ---
            SectionLabel(text = stringResource(CoreUiR.string.math_problem_count))
            ProblemCountSegmentedRow(
                selectedCount = state.problemCount,
                onSelect = { onIntent(MathSetupIntent.SetProblemCount(it)) },
            )

            // --- Smart mode info banner ---
            SmartModeBanner()

            // --- Go! button ---
            StudyBuddyButton(
                text = stringResource(CoreUiR.string.math_go),
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerSegmentedRow(
    selectedSeconds: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        10 to "10s",
        15 to "15s",
        30 to "30s",
        0 to "\u221E",
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (seconds, label) ->
            SegmentedButton(
                selected = selectedSeconds == seconds,
                onClick = { onSelect(seconds) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
            ) {
                Text(text = label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProblemCountSegmentedRow(
    selectedCount: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(10, 20, 50)

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, count ->
            SegmentedButton(
                selected = selectedCount == count,
                onClick = { onSelect(count) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
            ) {
                Text(text = count.toString())
            }
        }
    }
}

@Composable
private fun SmartModeBanner(modifier: Modifier = Modifier) {
    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(
                    text = stringResource(CoreUiR.string.math_smart_mode),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(CoreUiR.string.math_smart_mode_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MathSetupScreenPreview() {
    StudyBuddyTheme {
        MathSetupContent(
            state = MathSetupState(
                selectedOperators = setOf(Operator.PLUS, Operator.MINUS),
                numberRangeMin = 1,
                numberRangeMax = 12,
                timerSeconds = 15,
                problemCount = 20,
            ),
            onIntent = {},
            onStartGame = {},
        )
    }
}
