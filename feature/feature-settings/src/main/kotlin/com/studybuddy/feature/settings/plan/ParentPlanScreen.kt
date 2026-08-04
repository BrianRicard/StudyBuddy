package com.studybuddy.feature.settings.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.PlanDayResult
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.theme.CorrectGreen
import com.studybuddy.core.ui.theme.GRAPHICAL_MIN_RATIO
import com.studybuddy.core.ui.theme.PointsGold
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.core.ui.theme.SubjectArcade
import com.studybuddy.core.ui.theme.SubjectDictee
import com.studybuddy.core.ui.theme.SubjectMath
import com.studybuddy.core.ui.theme.SubjectPoems
import com.studybuddy.core.ui.theme.SubjectReading
import com.studybuddy.core.ui.theme.SubjectVerbs
import com.studybuddy.core.ui.theme.TEXT_MIN_RATIO
import com.studybuddy.core.ui.theme.ensureContrastWith
import com.studybuddy.core.ui.theme.subjectPalette
import kotlinx.datetime.isoDayNumber

@Composable
fun ParentPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: ParentPlanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ParentPlanContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentPlanContent(
    state: ParentPlanState,
    onIntent: (ParentPlanIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.plan_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(CoreUiR.string.plan_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { DayStrip(state, onIntent) }
            items(LearningMode.entries) { mode -> ModeRow(mode, state.countFor(mode), onIntent) }
            item { DayActions(onIntent) }
            item { BonusCard(state.completionBonus, onIntent) }
            item { HistoryCard(state.history) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** `items` for a fixed list — a tiny helper so the enum can be laid out inline. */
private fun androidx.compose.foundation.lazy.LazyListScope.items(
    modes: List<LearningMode>,
    row: @Composable (LearningMode) -> Unit,
) = modes.forEach { mode -> item(key = mode.name) { row(mode) } }

@Composable
private fun DayStrip(
    state: ParentPlanState,
    onIntent: (ParentPlanIntent) -> Unit,
) {
    val tasksSetLabel = stringResource(CoreUiR.string.plan_day_has_tasks)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ParentPlanViewModel.ALL_DAYS.forEach { day ->
            val selected = day == state.selectedDay
            val container = if (selected) {
                MaterialTheme.colorScheme.primary.ensureContrastWith(
                    MaterialTheme.colorScheme.onPrimary,
                    TEXT_MIN_RATIO,
                )
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    // Seven across a 360dp phone is 41.7dp wide, so the height has to
                    // carry the target; selection is conveyed by colour alone otherwise.
                    .sizeIn(minHeight = MIN_TOUCH_TARGET)
                    .semantics { this.selected = selected },
                shape = RoundedCornerShape(14.dp),
                color = container,
                onClick = { onIntent(ParentPlanIntent.SelectDay(day)) },
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(dayLabelRes(day)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    // A dot rather than a number: at this size a count is unreadable,
                    // and "is anything set" is the question a parent scans for.
                    Spacer(Modifier.height(4.dp))
                    val hasTasks = state.taskCountForDay(day) > 0
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .semantics {
                                // Colour-only information otherwise: this dot is the
                                // only thing saying which days are configured.
                                if (hasTasks) contentDescription = tasksSetLabel
                            }
                            .background(
                                color = when {
                                    !hasTasks -> Color.Transparent
                                    selected -> MaterialTheme.colorScheme.onPrimary
                                    // Raw gold measures 1.09:1 on surfaceVariant.
                                    else -> PointsGold.ensureContrastWith(container, GRAPHICAL_MIN_RATIO)
                                },
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeRow(
    mode: LearningMode,
    count: Int,
    onIntent: (ParentPlanIntent) -> Unit,
) {
    val visuals = mode.visuals()
    val palette = subjectPalette(visuals.hue)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (count > 0) palette.container else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(visuals.iconRes),
                    contentDescription = null,
                    tint = visuals.hue.ensureContrastWith(
                        MaterialTheme.colorScheme.surface,
                        GRAPHICAL_MIN_RATIO,
                    ),
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = stringResource(visuals.nameRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) palette.onContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Stepper(
                value = count,
                onStep = { delta -> onIntent(ParentPlanIntent.ChangeCount(mode, delta)) },
                max = ParentPlanViewModel.MAX_SESSIONS_PER_MODE,
                valueDescription = pluralStringResource(CoreUiR.plurals.plan_sessions, count, count),
            )
        }
    }
}

@Composable
private fun Stepper(
    value: Int,
    onStep: (Int) -> Unit,
    max: Int,
    valueDescription: String,
    step: Int = 1,
    min: Int = 0,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton(
            label = "−",
            enabled = value > min,
            contentDescription = stringResource(CoreUiR.string.plan_decrease),
            onClick = { onStep(-step) },
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .sizeIn(minWidth = 40.dp)
                .padding(horizontal = 4.dp)
                .semantics { contentDescription = valueDescription },
        )
        StepperButton(
            label = "+",
            enabled = value < max,
            contentDescription = stringResource(CoreUiR.string.plan_increase),
            onClick = { onStep(step) },
        )
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        // "+" and "−" mean nothing read aloud, so the button carries a real description.
        modifier = Modifier
            .size(MIN_TOUCH_TARGET)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        // The circle stays when disabled so "+" and "−" do not go asymmetric.
        color = MaterialTheme.colorScheme.surfaceVariant,
        enabled = enabled,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = if (enabled) 1f else DISABLED_GLYPH_ALPHA),
            )
        }
    }
}

@Composable
private fun DayActions(onIntent: (ParentPlanIntent) -> Unit) {
    // TextButton defaults its content colour to raw `primary`, which is below 4.5:1
    // on the background in every one of the six themes — Arctic is 2.06:1.
    val label = MaterialTheme.colorScheme.primary
        .ensureContrastWith(MaterialTheme.colorScheme.background, TEXT_MIN_RATIO)
    val colors = ButtonDefaults.textButtonColors(contentColor = label)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onIntent(ParentPlanIntent.CopyDayToAll) }, colors = colors) {
            Text(stringResource(CoreUiR.string.plan_copy_to_all))
        }
        TextButton(onClick = { onIntent(ParentPlanIntent.ClearDay) }, colors = colors) {
            Text(stringResource(CoreUiR.string.plan_clear_day))
        }
    }
}

@Composable
private fun BonusCard(
    bonus: Int,
    onIntent: (ParentPlanIntent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(CoreUiR.string.plan_bonus_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(CoreUiR.string.plan_bonus_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Stepper(
                value = bonus,
                onStep = { delta ->
                    onIntent(
                        ParentPlanIntent.SetCompletionBonus(
                            (bonus + delta).coerceIn(0, PointValues.MAX_PLAN_COMPLETION_BONUS),
                        ),
                    )
                },
                max = PointValues.MAX_PLAN_COMPLETION_BONUS,
                step = BONUS_STEP,
                valueDescription = stringResource(CoreUiR.string.plan_increase),
            )
        }
    }
}

/** The parent's view of how the week has gone. The child never sees this. */
@Composable
private fun HistoryCard(history: List<PlanDayResult>) {
    if (history.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(CoreUiR.string.plan_history_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                history.forEach { day -> HistoryDot(day, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun HistoryDot(
    day: PlanDayResult,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    // A missed day is grey, never red: this is information for a parent, not a verdict.
    val fill = when {
        day.isRestDay -> MaterialTheme.colorScheme.surfaceVariant
        day.isComplete -> CorrectGreen.ensureContrastWith(surface, GRAPHICAL_MIN_RATIO)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(dayLabelRes(day.date.dayOfWeek.isoDayNumber)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 26.dp, minHeight = 26.dp)
                .background(fill, CircleShape)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when {
                    day.isRestDay -> "–"
                    day.isComplete -> "✓"
                    else -> "${day.completedCount}/${day.plannedCount}"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                // Corrected against the fill, not the page: on Galaxy the near-white
                // onSurface measures 2.27:1 on the green.
                color = MaterialTheme.colorScheme.onSurface.ensureContrastWith(fill, TEXT_MIN_RATIO),
            )
        }
    }
}

private data class ModeVisuals(
    val nameRes: Int,
    val iconRes: Int,
    val hue: Color,
)

private fun LearningMode.visuals(): ModeVisuals = when (this) {
    LearningMode.DICTEE -> ModeVisuals(CoreUiR.string.mode_dictee, CoreUiR.drawable.ic_subject_dictee, SubjectDictee)
    LearningMode.SPEED_MATH -> ModeVisuals(CoreUiR.string.mode_math, CoreUiR.drawable.ic_subject_math, SubjectMath)
    LearningMode.VERB_QUEST ->
        ModeVisuals(CoreUiR.string.mode_conjugation, CoreUiR.drawable.ic_subject_verbs, SubjectVerbs)
    LearningMode.POEMS -> ModeVisuals(CoreUiR.string.mode_poems, CoreUiR.drawable.ic_subject_poems, SubjectPoems)
    LearningMode.READING -> ModeVisuals(CoreUiR.string.nav_reading, CoreUiR.drawable.ic_subject_reading, SubjectReading)
    LearningMode.MATH_CHALLENGE ->
        ModeVisuals(CoreUiR.string.mode_math_challenge, CoreUiR.drawable.ic_subject_arcade, SubjectArcade)
}

private fun dayLabelRes(isoDay: Int): Int = when (isoDay) {
    1 -> CoreUiR.string.day_mon
    2 -> CoreUiR.string.day_tue
    3 -> CoreUiR.string.day_wed
    4 -> CoreUiR.string.day_thu
    5 -> CoreUiR.string.day_fri
    6 -> CoreUiR.string.day_sat
    else -> CoreUiR.string.day_sun
}

private val MIN_TOUCH_TARGET = 48.dp
private const val BONUS_STEP = 10

/** Enough to read as disabled without dropping the glyph to 1.85:1. */
private const val DISABLED_GLYPH_ALPHA = 0.55f

@Preview(showBackground = true)
@Composable
private fun ParentPlanPreview() {
    StudyBuddyTheme {
        ParentPlanContent(
            state = ParentPlanState(selectedDay = 2, isLoading = false),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
