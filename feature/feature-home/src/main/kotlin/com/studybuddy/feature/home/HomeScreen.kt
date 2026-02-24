package com.studybuddy.feature.home

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studybuddy.core.ui.components.AvatarComposite
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDictee: () -> Unit = {},
    onNavigateToMath: () -> Unit = {},
    onNavigateToAvatar: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToRewards: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.OpenDictee -> onNavigateToDictee()
                HomeEffect.OpenMath -> onNavigateToMath()
                HomeEffect.OpenAvatar -> onNavigateToAvatar()
                HomeEffect.OpenStats -> onNavigateToStats()
                HomeEffect.OpenRewards -> onNavigateToRewards()
                HomeEffect.OpenSettings -> onNavigateToSettings()
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Header: Avatar + Greeting + Stars + Settings
            item {
                HomeHeader(
                    state = state,
                    onAvatarClick = { viewModel.onIntent(HomeIntent.NavigateToAvatar) },
                    onStarsClick = { viewModel.onIntent(HomeIntent.NavigateToStats) },
                    onSettingsClick = { viewModel.onIntent(HomeIntent.NavigateToSettings) },
                )
            }

            // Streak Banner
            item {
                StreakBanner(
                    dayStreak = state.dayStreak,
                    weekDots = state.weekDots,
                )
            }

            // Daily Challenge
            item {
                DailyChallengeCard(
                    sessionsToday = state.sessionsToday,
                    dailyGoal = state.dailyGoal,
                    progress = state.dailyProgress,
                    isComplete = state.isDailyGoalReached,
                )
            }

            // Mode Cards Grid (2x2)
            item {
                ModeCardsGrid(
                    onDicteeClick = { viewModel.onIntent(HomeIntent.NavigateToDictee) },
                    onMathClick = { viewModel.onIntent(HomeIntent.NavigateToMath) },
                )
            }

            // Recent Activity
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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

@Composable
private fun HomeHeader(
    state: HomeState,
    onAvatarClick: () -> Unit,
    onStarsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                text = state.greeting,
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
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Stars",
                tint = MaterialTheme.colorScheme.primary,
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

        // Settings
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
            )
        }
    }
}

@Composable
private fun StreakBanner(
    dayStreak: Int,
    weekDots: List<Boolean>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "\uD83D\uDD25", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (dayStreak > 0) "$dayStreak day streak!" else "Start your streak!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Week dots (Mon-Sun)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
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
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
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

@Composable
private fun DailyChallengeCard(
    sessionsToday: Int,
    dailyGoal: Int,
    progress: Float,
    isComplete: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isComplete) "\uD83C\uDF89" else "\uD83C\uDFAF",
                    fontSize = 20.sp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isComplete) "Daily Goal Complete!" else "Daily Challenge",
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
                text = "$sessionsToday / $dailyGoal sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ModeCardsGrid(
    onDicteeClick: () -> Unit,
    onMathClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "modeCardBob")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeCard(
                emoji = "\u270D\uFE0F",
                title = "Dictée",
                subtitle = "Spelling practice",
                onClick = onDicteeClick,
                modifier = Modifier.weight(1f),
                infiniteTransition = infiniteTransition,
                animationDelay = 0,
            )
            ModeCard(
                emoji = "\uD83E\uDDEE",
                title = "Speed Math",
                subtitle = "Mental math drills",
                onClick = onMathClick,
                modifier = Modifier.weight(1f),
                infiniteTransition = infiniteTransition,
                animationDelay = BOB_ANIMATION_DELAY,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeCard(
                emoji = "\uD83D\uDCDC",
                title = "Poems",
                subtitle = "Coming soon",
                onClick = {},
                modifier = Modifier.weight(1f),
                isLocked = true,
                infiniteTransition = infiniteTransition,
                animationDelay = BOB_ANIMATION_DELAY * 2,
            )
            ModeCard(
                emoji = "\u2795",
                title = "More",
                subtitle = "Coming soon",
                onClick = {},
                modifier = Modifier.weight(1f),
                isLocked = true,
                infiniteTransition = infiniteTransition,
                animationDelay = BOB_ANIMATION_DELAY * 3,
            )
        }
    }
}

@Composable
private fun ModeCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    infiniteTransition: InfiniteTransition,
    animationDelay: Int = 0,
) {
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = BOB_AMPLITUDE,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = BOB_DURATION, delayMillis = animationDelay),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob_$title",
    )

    Card(
        modifier = modifier
            .height(MODE_CARD_HEIGHT)
            .graphicsLayer { translationY = bobOffset }
            .clickable(enabled = !isLocked, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 4.dp),
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
                Text(text = emoji, fontSize = 36.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyRecentActivity() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(text = "\uD83C\uDF1F", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No activity yet",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Start a Dictée or Math session to see your progress here!",
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (activity.mode == "Dictée") "\u270D\uFE0F" else "\uD83E\uDDEE",
                fontSize = 24.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.mode,
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
                    text = activity.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val MODE_CARD_HEIGHT = 140.dp
private const val BOB_AMPLITUDE = 4f
private const val BOB_DURATION = 2000
private const val BOB_ANIMATION_DELAY = 300

@Preview
@Composable
private fun HomeScreenPreview() {
    StudyBuddyTheme {
        HomeScreen()
    }
}
