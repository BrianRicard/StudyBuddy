package com.studybuddy.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.adaptive.AdaptiveDimensDefaults
import com.studybuddy.core.ui.adaptive.LayoutType
import com.studybuddy.core.ui.adaptive.LocalLayoutType
import com.studybuddy.core.ui.animation.isReducedMotionEnabled
import com.studybuddy.core.ui.components.AvatarComposite
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.modifier.animateItemAppearance
import com.studybuddy.core.ui.modifier.bounceClick
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDictee: () -> Unit = {},
    onNavigateToMath: () -> Unit = {},
    onNavigateToMathChallenge: () -> Unit = {},
    onNavigateToPoems: () -> Unit = {},
    onNavigateToAvatar: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToRewards: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.OpenDictee -> onNavigateToDictee()
                HomeEffect.OpenMath -> onNavigateToMath()
                HomeEffect.OpenMathChallenge -> onNavigateToMathChallenge()
                HomeEffect.OpenPoems -> onNavigateToPoems()
                HomeEffect.OpenAvatar -> onNavigateToAvatar()
                HomeEffect.OpenStats -> onNavigateToStats()
                HomeEffect.OpenRewards -> onNavigateToRewards()
                HomeEffect.OpenSettings -> onNavigateToSettings()
            }
        }
    }

    if (state.isLoading) {
        LoadingState(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
        return
    }

    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val dimens = AdaptiveDimensDefaults.current()
    val layoutType = LocalLayoutType.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Header: Avatar + Greeting + Stars + Settings
            item {
                HomeHeader(
                    state = state,
                    onAvatarClick = { onIntent(HomeIntent.NavigateToAvatar) },
                    onStarsClick = { onIntent(HomeIntent.NavigateToStats) },
                    onSettingsClick = { onIntent(HomeIntent.NavigateToSettings) },
                    modifier = Modifier.animateItemAppearance(0),
                )
            }

            // Streak Banner
            item {
                StreakBanner(
                    dayStreak = state.dayStreak,
                    weekDots = state.weekDots,
                    modifier = Modifier.animateItemAppearance(1),
                )
            }

            // Daily Challenge
            item {
                DailyChallengeCard(
                    sessionsToday = state.sessionsToday,
                    dailyGoal = state.dailyGoal,
                    progress = state.dailyProgress,
                    isComplete = state.isDailyGoalReached,
                    modifier = Modifier.animateItemAppearance(2),
                )
            }

            // Mode Cards Grid
            item {
                ModeCardsGrid(
                    onDicteeClick = { onIntent(HomeIntent.NavigateToDictee) },
                    onMathClick = { onIntent(HomeIntent.NavigateToMath) },
                    onMathChallengeClick = { onIntent(HomeIntent.NavigateToMathChallenge) },
                    onPoemsClick = { onIntent(HomeIntent.NavigateToPoems) },
                    layoutType = layoutType,
                    modifier = Modifier.animateItemAppearance(3),
                )
            }

            // Recent Activity
            item {
                Text(
                    text = stringResource(CoreUiR.string.recent_activity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.animateItemAppearance(4),
                )
            }
            if (state.recentActivities.isNotEmpty()) {
                items(state.recentActivities) { activity ->
                    RecentActivityRow(activity)
                }
            } else {
                item {
                    EmptyRecentActivity()
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// region Header

@Composable
private fun HomeHeader(
    state: HomeState,
    onAvatarClick: () -> Unit,
    onStarsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                )
                .padding(4.dp),
        ) {
            AvatarComposite(
                config = state.avatarConfig,
                size = 64.dp,
            )
        }

        Spacer(Modifier.width(12.dp))

        // Greeting + Name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(state.greetingResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = state.profileName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Star Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onStarsClick)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(CoreUiR.drawable.ic_star_points),
                contentDescription = stringResource(CoreUiR.string.stars),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${state.totalStars}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(Modifier.width(4.dp))

        // Settings gear
        IconButton(onClick = onSettingsClick) {
            Image(
                painter = painterResource(CoreUiR.drawable.ic_nav_settings),
                contentDescription = stringResource(CoreUiR.string.nav_settings),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// endregion

// region Streak Banner

@Composable
private fun StreakBanner(
    dayStreak: Int,
    weekDots: List<Boolean>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(CoreUiR.drawable.ic_streak_flame),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (dayStreak > 0) {
                        pluralStringResource(CoreUiR.plurals.streak_label_plural, dayStreak, dayStreak)
                    } else {
                        stringResource(CoreUiR.string.streak_start)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Week dots (Mon-Sun)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val dayLabels = listOf(
                    stringResource(CoreUiR.string.day_mon),
                    stringResource(CoreUiR.string.day_tue),
                    stringResource(CoreUiR.string.day_wed),
                    stringResource(CoreUiR.string.day_thu),
                    stringResource(CoreUiR.string.day_fri),
                    stringResource(CoreUiR.string.day_sat),
                    stringResource(CoreUiR.string.day_sun),
                )
                weekDots.forEachIndexed { index, active ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = if (active) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (active) {
                                Text(
                                    text = "\u2713",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = dayLabels[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region Daily Challenge

@Composable
private fun DailyChallengeCard(
    sessionsToday: Int,
    dailyGoal: Int,
    progress: Float,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        if (isComplete) CoreUiR.drawable.ic_goal_complete else CoreUiR.drawable.ic_target_challenge,
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isComplete) {
                        stringResource(CoreUiR.string.daily_goal_complete)
                    } else {
                        stringResource(CoreUiR.string.daily_challenge)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(CoreUiR.string.daily_progress, sessionsToday, dailyGoal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// endregion

// region Mode Cards Grid

@Composable
private fun ModeCardsGrid(
    onDicteeClick: () -> Unit,
    onMathClick: () -> Unit,
    onMathChallengeClick: () -> Unit,
    onPoemsClick: () -> Unit,
    layoutType: LayoutType,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "modeCardBob")

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeCard(
                title = stringResource(CoreUiR.string.mode_dictee),
                subtitle = stringResource(CoreUiR.string.label_spelling_practice),
                onClick = onDicteeClick,
                modifier = Modifier.weight(1f),
                iconRes = CoreUiR.drawable.ic_dictee_illustration,
                containerColor = ModeCardColor.Primary,
                infiniteTransition = infiniteTransition,
                animationDelay = 0,
            )
            ModeCard(
                title = stringResource(CoreUiR.string.mode_math),
                subtitle = stringResource(CoreUiR.string.label_mental_math),
                onClick = onMathClick,
                modifier = Modifier.weight(1f),
                iconRes = CoreUiR.drawable.ic_math_illustration,
                containerColor = ModeCardColor.Secondary,
                infiniteTransition = infiniteTransition,
                animationDelay = BOB_ANIMATION_DELAY,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeCard(
                title = stringResource(CoreUiR.string.mode_poems),
                subtitle = stringResource(CoreUiR.string.label_reading_poems),
                onClick = onPoemsClick,
                modifier = Modifier.weight(1f),
                iconRes = CoreUiR.drawable.ic_poems_notepad,
                containerColor = ModeCardColor.Tertiary,
                infiniteTransition = infiniteTransition,
                animationDelay = BOB_ANIMATION_DELAY * 2,
            )
            ModeCard(
                title = stringResource(CoreUiR.string.mode_math_challenge),
                subtitle = stringResource(CoreUiR.string.label_falling_equations),
                onClick = onMathChallengeClick,
                modifier = Modifier.weight(1f),
                iconRes = CoreUiR.drawable.ic_target_challenge,
                containerColor = ModeCardColor.Primary,
                infiniteTransition = infiniteTransition,
                animationDelay = BOB_ANIMATION_DELAY * 3,
            )
        }
    }
}

private enum class ModeCardColor { Primary, Secondary, Tertiary }

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int = 0,
    isLocked: Boolean = false,
    containerColor: ModeCardColor = ModeCardColor.Primary,
    infiniteTransition: InfiniteTransition,
    animationDelay: Int = 0,
) {
    val reducedMotion = isReducedMotionEnabled()
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else BOB_AMPLITUDE,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = BOB_DURATION, delayMillis = animationDelay),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob_$title",
    )

    val cardContainerColor = if (isLocked) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        when (containerColor) {
            ModeCardColor.Primary -> MaterialTheme.colorScheme.primaryContainer
            ModeCardColor.Secondary -> MaterialTheme.colorScheme.secondaryContainer
            ModeCardColor.Tertiary -> MaterialTheme.colorScheme.tertiaryContainer
        }
    }

    val textColor = if (isLocked) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        when (containerColor) {
            ModeCardColor.Primary -> MaterialTheme.colorScheme.onPrimaryContainer
            ModeCardColor.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
            ModeCardColor.Tertiary -> MaterialTheme.colorScheme.onTertiaryContainer
        }
    }

    Card(
        modifier = modifier
            .height(MODE_CARD_HEIGHT)
            .graphicsLayer { translationY = bobOffset }
            .then(if (!isLocked) Modifier.bounceClick(onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (iconRes != 0) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = title,
                        modifier = Modifier.size(48.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = textColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(CoreUiR.string.locked),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// endregion

// region Recent Activity

@Composable
private fun EmptyRecentActivity() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(CoreUiR.drawable.ic_milestone_star),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(CoreUiR.string.home_no_activity),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(CoreUiR.string.home_no_activity_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RecentActivityRow(activity: RecentActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(
                    when (activity.source) {
                        PointSource.DICTEE -> CoreUiR.drawable.ic_dictee_illustration
                        PointSource.MATH -> CoreUiR.drawable.ic_math_illustration
                        PointSource.POEMS -> CoreUiR.drawable.ic_poems_notepad
                        else -> CoreUiR.drawable.ic_milestone_star
                    },
                ),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(activity.modeResId),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = activity.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${activity.points}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = resolveTimeAgo(activity.timeAgo),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun resolveTimeAgo(timeAgo: TimeAgo): String = when (timeAgo) {
    is TimeAgo.JustNow -> stringResource(CoreUiR.string.time_just_now)
    is TimeAgo.Minutes -> stringResource(CoreUiR.string.time_minutes_ago, timeAgo.minutes.toInt())
    is TimeAgo.Hours -> stringResource(CoreUiR.string.time_hours_ago, timeAgo.hours.toInt())
    is TimeAgo.Yesterday -> stringResource(CoreUiR.string.time_yesterday)
    is TimeAgo.Days -> stringResource(CoreUiR.string.time_days_ago, timeAgo.days.toInt())
}

// endregion

private val MODE_CARD_HEIGHT = 140.dp
private const val BOB_AMPLITUDE = 4f
private const val BOB_DURATION = 2000
private const val BOB_ANIMATION_DELAY = 300

// region Previews

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    StudyBuddyTheme {
        HomeContent(
            state = HomeState(
                profileName = "Sophie",
                totalStars = 1250,
                dayStreak = 3,
                weekDots = listOf(true, true, true, false, false, false, false),
                sessionsToday = 3,
                dailyGoal = 5,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    StudyBuddyTheme {
        HomeContent(
            state = HomeState(
                profileName = "New Kid",
                totalStars = 0,
                dayStreak = 0,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenGoalCompletePreview() {
    StudyBuddyTheme {
        HomeContent(
            state = HomeState(
                profileName = "Sophie",
                totalStars = 2500,
                dayStreak = 7,
                weekDots = listOf(true, true, true, true, true, true, true),
                sessionsToday = 5,
                dailyGoal = 5,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

// endregion
