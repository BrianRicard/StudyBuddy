package com.studybuddy.feature.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.animation.isReducedMotionEnabled
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.modifier.animateItemAppearance
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.theme.CorrectGreen
import com.studybuddy.core.ui.theme.PointsGold
import com.studybuddy.core.ui.theme.StreakOrange
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StatsContent(state = state, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatsContent(
    state: StatsState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(CoreUiR.string.stats_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                LoadingState()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 1. Summary row: 3 stat cards side by side
                item {
                    SummaryRow(
                        state = state,
                        modifier = Modifier.animateItemAppearance(0),
                    )
                }

                // 2. Weekly chart section
                item {
                    WeeklyChartSection(
                        weeklyData = state.weeklyData,
                        modifier = Modifier.animateItemAppearance(1),
                    )
                }

                // 3. Trends section
                item {
                    TrendsSection(
                        dicteeAccuracy = state.dicteeAccuracy,
                        dicteeAccuracyTrend = state.dicteeAccuracyTrend,
                        mathAvgSpeed = state.mathAvgSpeed,
                        mathSpeedTrend = state.mathSpeedTrend,
                        modifier = Modifier.animateItemAppearance(2),
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// region Summary Row

@Composable
private fun SummaryRow(
    state: StatsState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryStatCard(
            icon = "\u2B50",
            value = formatStarCount(state.totalStars),
            label = stringResource(CoreUiR.string.stats_total_stars),
            valueColor = PointsGold,
            modifier = Modifier.weight(1f),
        )
        SummaryStatCard(
            icon = "\uD83D\uDD25",
            value = state.dayStreak.toString(),
            label = stringResource(CoreUiR.string.stats_day_streak),
            valueColor = StreakOrange,
            modifier = Modifier.weight(1f),
        )
        SummaryStatCard(
            icon = "\uD83D\uDCD6",
            value = state.totalSessions.toString(),
            label = stringResource(CoreUiR.string.stats_total_sessions),
            valueColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryStatCard(
    icon: String,
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = LABEL_ALPHA),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun formatStarCount(stars: Long): String = when {
    stars >= THOUSAND -> "%.1fk".format(stars / THOUSAND.toDouble())
    else -> stars.toString()
}

// endregion

// region Weekly Chart

@Composable
private fun WeeklyChartSection(
    weeklyData: List<DayData>,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(CoreUiR.string.stats_weekly_chart),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeeklyChart(
                weeklyData = weeklyData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CHART_HEIGHT.dp),
            )
        }
    }
}

@Composable
internal fun WeeklyChart(
    weeklyData: List<DayData>,
    modifier: Modifier = Modifier,
) {
    val maxPoints = (weeklyData.maxOfOrNull { it.points } ?: 0).coerceAtLeast(1)
    val reducedMotion = isReducedMotionEnabled()

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTriggered = true }

    val animatedFractions = weeklyData.map { dayData ->
        val targetFraction = if (maxPoints > 0) {
            dayData.points.toFloat() / maxPoints.toFloat()
        } else {
            0f
        }
        animateFloatAsState(
            targetValue = if (animationTriggered || reducedMotion) targetFraction else 0f,
            animationSpec = tween(
                durationMillis = if (reducedMotion) 0 else ANIMATION_DURATION_MS,
            ),
            label = "bar-${dayData.dayOfWeek}",
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val fadedPrimaryColor = primaryColor.copy(alpha = BAR_INACTIVE_ALPHA)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = LABEL_ALPHA)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = labelColor,
        textAlign = TextAlign.Center,
    )
    val valueStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )

    Canvas(modifier = modifier) {
        val totalBars = weeklyData.size
        if (totalBars == 0) return@Canvas

        val labelAreaHeight = LABEL_AREA_HEIGHT_DP.dp.toPx()
        val valueAreaHeight = VALUE_AREA_HEIGHT_DP.dp.toPx()
        val barAreaHeight = size.height - labelAreaHeight - valueAreaHeight
        val barWidth = size.width / (totalBars * 2f)
        val barSpacing = size.width / totalBars

        weeklyData.forEachIndexed { index, dayData ->
            val animatedFraction by animatedFractions[index]
            val barHeight = (barAreaHeight * animatedFraction)
                .coerceAtLeast(if (dayData.points > 0) MIN_BAR_HEIGHT_DP.dp.toPx() else 0f)
            val barColor = if (dayData.isToday) primaryColor else fadedPrimaryColor

            val barX = barSpacing * index + (barSpacing - barWidth) / 2f
            val barY = valueAreaHeight + barAreaHeight - barHeight

            // Draw the bar with rounded top corners
            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x = barX, y = barY),
                    size = Size(width = barWidth, height = barHeight),
                    cornerRadius = CornerRadius(
                        x = BAR_CORNER_RADIUS_DP.dp.toPx(),
                        y = BAR_CORNER_RADIUS_DP.dp.toPx(),
                    ),
                )
            }

            // Draw point value above the bar
            val valueText = if (dayData.points > 0) dayData.points.toString() else ""
            if (valueText.isNotEmpty()) {
                val valueLayoutResult = textMeasurer.measure(
                    text = valueText,
                    style = valueStyle.copy(
                        color = if (dayData.isToday) primaryColor else labelColor,
                    ),
                )
                drawText(
                    textLayoutResult = valueLayoutResult,
                    topLeft = Offset(
                        x = barX + barWidth / 2f - valueLayoutResult.size.width / 2f,
                        y = barY - valueLayoutResult.size.height - VALUE_PADDING_DP.dp.toPx(),
                    ),
                )
            }

            // Draw day label below the bar
            val labelLayoutResult = textMeasurer.measure(
                text = dayData.dayOfWeek,
                style = labelStyle.copy(
                    fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (dayData.isToday) primaryColor else labelColor,
                ),
            )
            drawText(
                textLayoutResult = labelLayoutResult,
                topLeft = Offset(
                    x = barX + barWidth / 2f - labelLayoutResult.size.width / 2f,
                    y = size.height - labelAreaHeight +
                        (labelAreaHeight - labelLayoutResult.size.height) / 2f,
                ),
            )
        }
    }
}

// endregion

// region Trends Section

@Composable
private fun TrendsSection(
    dicteeAccuracy: Float?,
    dicteeAccuracyTrend: Float?,
    mathAvgSpeed: Long?,
    mathSpeedTrend: Long?,
    modifier: Modifier = Modifier,
) {
    val hasDicteeData = dicteeAccuracy != null
    val hasMathData = mathAvgSpeed != null

    if (!hasDicteeData && !hasMathData) return

    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(CoreUiR.string.stats_trends),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (hasDicteeData) {
                TrendRow(
                    icon = "\uD83D\uDCDD",
                    label = stringResource(CoreUiR.string.stats_dictee_accuracy),
                    currentValue = formatPercentage(dicteeAccuracy!!),
                    trend = dicteeAccuracyTrend,
                    trendFormatter = { trend ->
                        val arrow = if (trend >= 0) "\u2191" else "\u2193"
                        val sign = if (trend >= 0) "+" else ""
                        "$arrow $sign${formatPercentage(trend)}"
                    },
                    isPositiveBetter = true,
                )
            }

            if (hasDicteeData && hasMathData) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (hasMathData) {
                val fasterText = stringResource(CoreUiR.string.stats_faster)
                val slowerText = stringResource(CoreUiR.string.stats_slower)
                TrendRow(
                    icon = "\u2795",
                    label = stringResource(CoreUiR.string.stats_math_speed),
                    currentValue = formatResponseTime(mathAvgSpeed!!),
                    trend = mathSpeedTrend?.toFloat(),
                    trendFormatter = { trend ->
                        val trendMs = trend.toLong()
                        // Negative trend means faster (improvement)
                        val arrow = if (trendMs <= 0) "\u2191" else "\u2193"
                        val diffFormatted = formatResponseTime(
                            kotlin.math.abs(trendMs),
                        )
                        "$arrow ${if (trendMs <= 0) fasterText else slowerText} by $diffFormatted"
                    },
                    isPositiveBetter = false,
                )
            }
        }
    }
}

@Composable
private fun TrendRow(
    icon: String,
    label: String,
    currentValue: String,
    trend: Float?,
    trendFormatter: (Float) -> String,
    isPositiveBetter: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = LABEL_ALPHA),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (trend != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    val isImproving = if (isPositiveBetter) trend >= 0f else trend <= 0f
                    Text(
                        text = trendFormatter(trend),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isImproving) CorrectGreen else StreakOrange,
                    )
                }
            }
        }
    }
}

private fun formatPercentage(value: Float): String = "${(value * PERCENTAGE_MULTIPLIER).toInt()}%"

private fun formatResponseTime(ms: Long): String {
    val seconds = ms / MS_PER_SECOND.toDouble()
    return "%.1fs".format(seconds)
}

// endregion

// region Constants

private const val LABEL_ALPHA = 0.7f
private const val BAR_INACTIVE_ALPHA = 0.4f
private const val PERCENTAGE_MULTIPLIER = 100
private const val MS_PER_SECOND = 1000
private const val THOUSAND = 1000L
private const val CHART_HEIGHT = 200
private const val ANIMATION_DURATION_MS = 800
private const val LABEL_AREA_HEIGHT_DP = 24
private const val VALUE_AREA_HEIGHT_DP = 20
private const val MIN_BAR_HEIGHT_DP = 4
private const val BAR_CORNER_RADIUS_DP = 4
private const val VALUE_PADDING_DP = 2

// endregion

// region Previews

@Preview(showBackground = true)
@Composable
private fun StatsContentPreview() {
    StudyBuddyTheme {
        StatsContent(
            state = StatsState(
                totalStars = 1250,
                dayStreak = 7,
                totalSessions = 42,
                weeklyData = listOf(
                    DayData(dayOfWeek = "Mon", points = 45, isToday = false),
                    DayData(dayOfWeek = "Tue", points = 80, isToday = false),
                    DayData(dayOfWeek = "Wed", points = 30, isToday = false),
                    DayData(dayOfWeek = "Thu", points = 95, isToday = false),
                    DayData(dayOfWeek = "Fri", points = 60, isToday = true),
                    DayData(dayOfWeek = "Sat", points = 0, isToday = false),
                    DayData(dayOfWeek = "Sun", points = 0, isToday = false),
                ),
                dicteeAccuracy = 0.89f,
                dicteeAccuracyTrend = 0.11f,
                mathAvgSpeed = 5100,
                mathSpeedTrend = -3100L,
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsContentEmptyPreview() {
    StudyBuddyTheme {
        StatsContent(
            state = StatsState(
                totalStars = 0,
                dayStreak = 0,
                totalSessions = 0,
                weeklyData = listOf(
                    DayData(dayOfWeek = "Mon", points = 0, isToday = false),
                    DayData(dayOfWeek = "Tue", points = 0, isToday = false),
                    DayData(dayOfWeek = "Wed", points = 0, isToday = true),
                    DayData(dayOfWeek = "Thu", points = 0, isToday = false),
                    DayData(dayOfWeek = "Fri", points = 0, isToday = false),
                    DayData(dayOfWeek = "Sat", points = 0, isToday = false),
                    DayData(dayOfWeek = "Sun", points = 0, isToday = false),
                ),
                dicteeAccuracy = null,
                dicteeAccuracyTrend = null,
                mathAvgSpeed = null,
                mathSpeedTrend = null,
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsContentLoadingPreview() {
    StudyBuddyTheme {
        StatsContent(state = StatsState(isLoading = true))
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyChartPreview() {
    StudyBuddyTheme {
        WeeklyChart(
            weeklyData = listOf(
                DayData(dayOfWeek = "Mon", points = 45, isToday = false),
                DayData(dayOfWeek = "Tue", points = 80, isToday = false),
                DayData(dayOfWeek = "Wed", points = 30, isToday = false),
                DayData(dayOfWeek = "Thu", points = 95, isToday = false),
                DayData(dayOfWeek = "Fri", points = 60, isToday = true),
                DayData(dayOfWeek = "Sat", points = 0, isToday = false),
                DayData(dayOfWeek = "Sun", points = 0, isToday = false),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
    }
}

// endregion
