package com.studybuddy.feature.math.setup

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.MathProblem
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MathSetupContent(
    state: MathSetupState,
    onIntent: (MathSetupIntent) -> Unit,
    onStartGame: () -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Operators
            OperatorSection(
                selectedOperators = state.selectedOperators,
                onToggle = { onIntent(MathSetupIntent.ToggleOperator(it)) },
            )

            // Number Range
            NumberRangeSection(
                rangeMin = state.numberRangeMin,
                rangeMax = state.numberRangeMax,
                isCustom = state.isCustomRange,
                onSelectPreset = { min, max ->
                    onIntent(MathSetupIntent.SelectRangePreset(min, max))
                },
                onSelectCustom = { onIntent(MathSetupIntent.SelectCustomRange) },
                onMinChange = { onIntent(MathSetupIntent.SetRangeMin(it)) },
                onMaxChange = { onIntent(MathSetupIntent.SetRangeMax(it)) },
            )

            // Time Limit
            TimeLimitSection(
                selectedSeconds = state.timerSeconds,
                onSelect = { onIntent(MathSetupIntent.SetTimer(it)) },
            )

            // Problem Count
            ProblemCountSection(
                selectedCount = state.problemCount,
                onSelect = { onIntent(MathSetupIntent.SetProblemCount(it)) },
            )

            // Preview
            PreviewSection(state = state)

            // Start button
            StudyBuddyButton(
                text = stringResource(CoreUiR.string.math_start_game),
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OperatorSection(
    selectedOperators: Set<Operator>,
    onToggle: (Operator) -> Unit,
) {
    SectionCard(title = stringResource(CoreUiR.string.math_operators)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Show main 4 operators (skip POWER for kid-friendly UI)
            val mainOperators = listOf(
                Operator.PLUS,
                Operator.MINUS,
                Operator.MULTIPLY,
                Operator.DIVIDE,
            )
            mainOperators.forEach { operator ->
                val selected = operator in selectedOperators
                val operatorDesc = if (selected) {
                    stringResource(CoreUiR.string.math_operator_selected, operator.name)
                } else {
                    stringResource(CoreUiR.string.math_operator_not_selected, operator.name)
                }
                FilterChip(
                    selected = selected,
                    onClick = { onToggle(operator) },
                    label = {
                        Text(
                            text = operator.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .semantics { contentDescription = operatorDesc },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        Text(
            text = stringResource(CoreUiR.string.math_operators_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NumberRangeSection(
    rangeMin: Int,
    rangeMax: Int,
    isCustom: Boolean,
    onSelectPreset: (Int, Int) -> Unit,
    onSelectCustom: () -> Unit,
    onMinChange: (Int) -> Unit,
    onMaxChange: (Int) -> Unit,
) {
    SectionCard(title = stringResource(CoreUiR.string.math_number_range)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MathSetupViewModel.RANGE_PRESETS.forEach { preset ->
                val isSelected = !isCustom && rangeMin == preset.min && rangeMax == preset.max
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectPreset(preset.min, preset.max) },
                    label = { Text(preset.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
            FilterChip(
                selected = isCustom,
                onClick = onSelectCustom,
                label = { Text(stringResource(CoreUiR.string.math_custom)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }

        AnimatedVisibility(visible = isCustom) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = rangeMin.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { onMinChange(it) }
                        },
                        label = { Text(stringResource(CoreUiR.string.math_range_min)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = rangeMax.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { onMaxChange(it) }
                        },
                        label = { Text(stringResource(CoreUiR.string.math_range_max)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                val sliderMax = (rangeMax.coerceAtLeast(100)).toFloat()
                RangeSlider(
                    value = rangeMin.toFloat()..rangeMax.toFloat(),
                    onValueChange = { range ->
                        onMinChange(range.start.toInt())
                        onMaxChange(range.endInclusive.toInt())
                    },
                    valueRange = 0f..sliderMax,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = stringResource(CoreUiR.string.math_custom_range_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeLimitSection(
    selectedSeconds: Int,
    onSelect: (Int) -> Unit,
) {
    SectionCard(title = stringResource(CoreUiR.string.math_time_limit)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MathSetupViewModel.TIMER_OPTIONS.forEach { (seconds, label) ->
                FilterChip(
                    selected = selectedSeconds == seconds,
                    onClick = { onSelect(seconds) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProblemCountSection(
    selectedCount: Int,
    onSelect: (Int) -> Unit,
) {
    SectionCard(title = stringResource(CoreUiR.string.math_problem_count)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MathSetupViewModel.PROBLEM_COUNT_OPTIONS.forEach { count ->
                FilterChip(
                    selected = selectedCount == count,
                    onClick = { onSelect(count) },
                    label = { Text(count.toString()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PreviewSection(state: MathSetupState) {
    val sampleProblems = remember(
        state.selectedOperators,
        state.numberRangeMin,
        state.numberRangeMax,
    ) {
        generateSampleProblems(state)
    }

    SectionCard(title = stringResource(CoreUiR.string.math_preview)) {
        sampleProblems.forEach { problem ->
            Text(
                text = "${problem.displayString} = ?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    StudyBuddyCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

private fun generateSampleProblems(
    state: MathSetupState,
    count: Int = 3,
): List<MathProblem> {
    val operators = state.selectedOperators.toList().ifEmpty { listOf(Operator.PLUS) }
    val range = state.numberRangeMin..state.numberRangeMax

    return (1..count).map {
        val op = operators.random()
        when (op) {
            Operator.PLUS -> {
                val a = range.random()
                val b = range.random()
                MathProblem(a, b, op, a + b)
            }
            Operator.MINUS -> {
                val a = range.random()
                val b = (range.first..a).random()
                MathProblem(a, b, op, a - b)
            }
            Operator.MULTIPLY -> {
                val a = range.random()
                val b = (range.first..range.last.coerceAtMost(12)).random()
                MathProblem(a, b, op, a * b)
            }
            Operator.DIVIDE -> {
                val b = (1..range.last.coerceAtMost(12)).random()
                val quotient = range.random()
                MathProblem(b * quotient, b, op, quotient)
            }
            Operator.POWER -> {
                val base = (2..5).random()
                val exp = (1..3).random()
                var result = 1
                repeat(exp) { result *= base }
                MathProblem(base, exp, op, result)
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
                numberRangeMax = 20,
                timerSeconds = 60,
                problemCount = 10,
            ),
            onIntent = {},
            onStartGame = {},
        )
    }
}
