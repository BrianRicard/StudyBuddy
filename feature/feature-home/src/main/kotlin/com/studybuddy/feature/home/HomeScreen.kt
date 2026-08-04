package com.studybuddy.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.adaptive.AdaptiveDimensDefaults
import com.studybuddy.core.ui.adaptive.LayoutType
import com.studybuddy.core.ui.adaptive.LocalLayoutType
import com.studybuddy.core.ui.components.AvatarComposite
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.modifier.animateItemAppearance
import com.studybuddy.core.ui.modifier.bounceClick
import com.studybuddy.core.ui.theme.CorrectGreen
import com.studybuddy.core.ui.theme.GRAPHICAL_MIN_RATIO
import com.studybuddy.core.ui.theme.PointsGold
import com.studybuddy.core.ui.theme.StreakOrange
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.core.ui.theme.SubjectArcade
import com.studybuddy.core.ui.theme.SubjectDictee
import com.studybuddy.core.ui.theme.SubjectMath
import com.studybuddy.core.ui.theme.SubjectPalette
import com.studybuddy.core.ui.theme.SubjectPoems
import com.studybuddy.core.ui.theme.SubjectReading
import com.studybuddy.core.ui.theme.SubjectVerbs
import com.studybuddy.core.ui.theme.TEXT_MIN_RATIO
import com.studybuddy.core.ui.theme.ensureContrastWith
import com.studybuddy.core.ui.theme.subjectPalette

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDictee: () -> Unit = {},
    onNavigateToMath: () -> Unit = {},
    onNavigateToMathChallenge: () -> Unit = {},
    onNavigateToPoems: () -> Unit = {},
    onNavigateToReading: () -> Unit = {},
    onNavigateToConjugation: () -> Unit = {},
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
                HomeEffect.OpenReading -> onNavigateToReading()
                HomeEffect.OpenConjugation -> onNavigateToConjugation()
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

    HomeContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val dimens = AdaptiveDimensDefaults.current()
    val quest = buildHomeQuest(
        plan = state.todayPlan,
        tablesDue = state.tablesDue,
        atelierDueVerbs = state.atelierDueVerbs,
        planBonusPoints = state.planBonusPoints,
    )
    val isWide = LocalLayoutType.current != LayoutType.COMPACT

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        if (isWide) {
            // On a tablet the avatar scene would leave the right half of the screen
            // empty, so the scene and the quest take one column and the games take
            // the other, each scrolling on its own.
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = dimens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(SCENE_PANE_WEIGHT)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { HeaderChips(state, onIntent) }
                    item { AvatarScene(state, onIntent) }
                    item { QuestCard(quest, onIntent) }
                    // Latest lives here on a tablet rather than under the games:
                    // it fills a column that would otherwise be half empty, and
                    // keeps the game grid to one screenful.
                    latest(state)
                    item { Spacer(Modifier.height(16.dp)) }
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f - SCENE_PANE_WEIGHT)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    games(state, onIntent)
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { HeaderChips(state, onIntent) }
                item { AvatarScene(state, onIntent) }
                item { QuestCard(quest, onIntent) }
                games(state, onIntent)
                latest(state)
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

private fun LazyListScope.games(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    item { SectionTitle(stringResource(CoreUiR.string.home_pick_a_game)) }
    item { GameGrid(state, onIntent, Modifier.animateItemAppearance(0)) }
}

private fun LazyListScope.latest(state: HomeState) {
    item { SectionTitle(stringResource(CoreUiR.string.home_latest)) }
    if (state.recentActivities.isNotEmpty()) {
        items(state.recentActivities, key = { it.source }) { activity -> LatestRow(activity) }
    } else {
        item { EmptyLatest() }
    }
}

// region Header

@Composable
private fun HeaderChips(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.dayStreak > 0) {
            CountChip(
                iconRes = CoreUiR.drawable.ic_streak_flame,
                value = state.dayStreak.toString(),
                palette = subjectPalette(StreakOrange),
                contentDescription = pluralStringResource(
                    CoreUiR.plurals.home_streak_days,
                    state.dayStreak,
                    state.dayStreak,
                ),
            )
        }
        CountChip(
            iconRes = CoreUiR.drawable.ic_star_points,
            value = state.totalStars.toString(),
            palette = subjectPalette(PointsGold),
            contentDescription = stringResource(CoreUiR.string.stars),
            tintIcon = false,
            onClick = { onIntent(HomeIntent.NavigateToStats) },
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onIntent(HomeIntent.NavigateToSettings) }) {
            Icon(
                painter = painterResource(CoreUiR.drawable.ic_settings_gear),
                contentDescription = stringResource(CoreUiR.string.nav_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun CountChip(
    @DrawableRes iconRes: Int,
    value: String,
    palette: SubjectPalette,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tintIcon: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = MIN_TOUCH_TARGET)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.bounceClick(onClick) else Modifier)
            .background(palette.container, CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (tintIcon) palette.accent else Color.Unspecified,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = palette.onContainer,
        )
    }
}

/** Avatar, greeting bubble and name — the character of the screen. */
@Composable
private fun AvatarScene(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    // The adaptive size is keyed to the *width* class but spends *height*: a phone
    // in landscape is EXPANDED and only ~370dp tall, where a 150dp avatar would
    // push the quest — the one thing the child is meant to act on — off-screen.
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val avatarSize = minOf(
        AdaptiveDimensDefaults.current().homeAvatarSize,
        screenHeight * AVATAR_MAX_SCREEN_FRACTION,
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
        ) {
            Text(
                text = stringResource(CoreUiR.string.home_greeting_bubble, state.profileName),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier.bounceClick { onIntent(HomeIntent.NavigateToAvatar) },
        ) {
            AvatarComposite(config = state.avatarConfig, size = avatarSize)
        }
        Text(
            text = state.profileName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// endregion

// region Today's quest

@Composable
private fun QuestCard(
    quest: HomeQuest,
    onIntent: (HomeIntent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (quest) {
                is HomeQuest.Free -> FreeChoice()
                is HomeQuest.Complete -> PlanComplete(quest.bonusPoints)
                is HomeQuest.Ready -> {
                    val remaining = quest.tasks.count { !it.isDone }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(
                                if (quest.fromParent) {
                                    CoreUiR.string.home_quest_plan
                                } else {
                                    CoreUiR.string.home_quest_ready
                                },
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = pluralStringResource(
                                CoreUiR.plurals.home_quest_things,
                                remaining,
                                remaining,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = StreakOrange.ensureContrastWith(
                                MaterialTheme.colorScheme.surface,
                                TEXT_MIN_RATIO,
                            ),
                        )
                    }
                    // The Start button goes on the first task still outstanding —
                    // a child given three buttons picks none of them.
                    val firstOutstanding = quest.tasks.indexOfFirst { !it.isDone }
                    quest.tasks.forEachIndexed { index, task ->
                        Spacer(Modifier.height(10.dp))
                        QuestTaskRow(
                            task = task,
                            showStart = index == firstOutstanding,
                            onIntent = onIntent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestTaskRow(
    task: QuestTask,
    showStart: Boolean,
    onIntent: (HomeIntent) -> Unit,
) {
    val palette = subjectPalette(task.hue)
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MIN_TOUCH_TARGET)
            .clip(RoundedCornerShape(14.dp))
            .bounceClick { onIntent(task.intent) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        IconTile(
            iconRes = task.iconRes,
            palette = palette,
            tileSize = 38.dp,
            iconSize = 21.dp,
        )
        Text(
            text = pluralStringResource(task.titleRes, task.count, task.count),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            // Struck through rather than removed: a finished task is the child's win,
            // and taking it off the list hides the progress she just made.
            textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
            // 0.55 measured 3.36:1 — below the 4.5 bar, on text that is already
            // struck through and so harder to read to begin with.
            color = if (task.isDone) onSurface.copy(alpha = DONE_LABEL_ALPHA) else onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            // The title yields, never the chip: a squeezed label still wraps to two
            // lines, whereas a squeezed Start button stops being tappable.
            modifier = Modifier.weight(1f),
        )
        if (task.isDone) {
            Icon(
                painter = painterResource(CoreUiR.drawable.ic_check),
                contentDescription = stringResource(CoreUiR.string.home_task_done),
                tint = CorrectGreen.ensureContrastWith(MaterialTheme.colorScheme.surface, GRAPHICAL_MIN_RATIO),
                modifier = Modifier.size(22.dp),
            )
        } else if (showStart) {
            StartChip(onClick = { onIntent(task.intent) })
        }
    }
}

/** The one call to action on the screen, so its label is contrast-corrected per theme. */
@Composable
private fun StartChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The label is already white, so it cannot be pushed any further — it is the
    // fill that has to darken. Sunset's orange is 2.8:1 under white text untouched.
    val label = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = modifier.bounceClick(onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.ensureContrastWith(label, TEXT_MIN_RATIO),
    ) {
        Text(
            text = stringResource(CoreUiR.string.home_start),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = label,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

/** Every task the parent set is done. Pure celebration — nothing left to ask for. */
@Composable
private fun PlanComplete(bonusPoints: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(
            iconRes = CoreUiR.drawable.ic_check,
            palette = subjectPalette(CorrectGreen),
            tileSize = 44.dp,
            iconSize = 26.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(CoreUiR.string.home_quest_complete_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (bonusPoints > 0) {
                    stringResource(CoreUiR.string.home_quest_complete_bonus, bonusPoints)
                } else {
                    stringResource(CoreUiR.string.home_quest_complete_subtitle)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FreeChoice() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(
            iconRes = CoreUiR.drawable.ic_milestone_star,
            palette = subjectPalette(PointsGold),
            tileSize = 44.dp,
            iconSize = 26.dp,
            tintIcon = false,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(CoreUiR.string.home_quest_free_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(CoreUiR.string.home_quest_free_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// endregion

// region Games

@Composable
private fun GameGrid(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardHeight = gameCardHeight()
    val tallHeight = cardHeight * 2 + GAME_GAP

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GAME_GAP)) {
        Row(horizontalArrangement = Arrangement.spacedBy(GAME_GAP)) {
            GameCard(
                title = stringResource(CoreUiR.string.mode_conjugation),
                subtitle = stringResource(CoreUiR.string.label_conjugation),
                iconRes = CoreUiR.drawable.ic_subject_verbs,
                hue = SubjectVerbs,
                badge = state.atelierDueVerbs,
                onClick = { onIntent(HomeIntent.NavigateToConjugation) },
                modifier = Modifier
                    .weight(1f)
                    .height(tallHeight),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GAME_GAP),
            ) {
                GameCard(
                    title = stringResource(CoreUiR.string.mode_dictee),
                    subtitle = stringResource(CoreUiR.string.label_spelling_practice),
                    iconRes = CoreUiR.drawable.ic_subject_dictee,
                    hue = SubjectDictee,
                    onClick = { onIntent(HomeIntent.NavigateToDictee) },
                    modifier = Modifier.height(cardHeight),
                )
                GameCard(
                    title = stringResource(CoreUiR.string.mode_math),
                    subtitle = stringResource(CoreUiR.string.label_mental_math),
                    iconRes = CoreUiR.drawable.ic_subject_math,
                    hue = SubjectMath,
                    badge = state.tablesDue,
                    onClick = { onIntent(HomeIntent.NavigateToMath) },
                    modifier = Modifier.height(cardHeight),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GAME_GAP)) {
            GameCard(
                title = stringResource(CoreUiR.string.mode_poems),
                subtitle = stringResource(CoreUiR.string.label_reading_poems),
                iconRes = CoreUiR.drawable.ic_subject_poems,
                hue = SubjectPoems,
                onClick = { onIntent(HomeIntent.NavigateToPoems) },
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
            )
            GameCard(
                title = stringResource(CoreUiR.string.nav_reading),
                subtitle = stringResource(CoreUiR.string.label_reading_comprehension),
                iconRes = CoreUiR.drawable.ic_subject_reading,
                hue = SubjectReading,
                onClick = { onIntent(HomeIntent.NavigateToReading) },
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
            )
        }
        GameCard(
            title = stringResource(CoreUiR.string.mode_math_challenge),
            subtitle = stringResource(CoreUiR.string.label_falling_equations),
            iconRes = CoreUiR.drawable.ic_subject_arcade,
            hue = SubjectArcade,
            onClick = { onIntent(HomeIntent.NavigateToMathChallenge) },
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight),
        )
    }
}

/**
 * Height of one game card. Every card is fixed-height so the featured card can
 * span exactly two rows, which means the height has to be *computed*: the tile
 * and paddings never move, but the two label lines grow with the system font,
 * and at fontScale 1.3 they would otherwise be clipped off the bottom.
 *
 * The adaptive dimension is the floor — it gives a tablet a roomier card than
 * the labels strictly need.
 */
@Composable
private fun gameCardHeight(): Dp {
    val typography = MaterialTheme.typography
    val labels = with(LocalDensity.current) {
        typography.titleSmall.lineHeight.toDp() + typography.bodySmall.lineHeight.toDp()
    }
    // The slack absorbs sub-dp rounding in line measurement, which would otherwise
    // shave the descenders off the subtitle when `needed` wins exactly.
    val needed = GAME_CARD_PADDING * 2 + GAME_TILE_SIZE + GAME_TILE_GAP + labels + 4.dp
    return maxOf(AdaptiveDimensDefaults.current().homeGameCardHeight, needed)
}

@Composable
private fun GameCard(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    hue: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Int = 0,
) {
    val palette = subjectPalette(hue)
    val surface = MaterialTheme.colorScheme.surface
    // The tile sits on plain surface, not on the card's tint, so it needs its own
    // correction rather than the card's.
    val tilePalette = SubjectPalette(
        accent = hue.ensureContrastWith(surface, GRAPHICAL_MIN_RATIO),
        container = surface,
        onContainer = palette.onContainer,
    )

    Card(
        modifier = modifier.bounceClick(onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(GAME_CARD_PADDING),
            ) {
                IconTile(
                    iconRes = iconRes,
                    palette = tilePalette,
                    tileSize = GAME_TILE_SIZE,
                    iconSize = 26.dp,
                )
                Spacer(Modifier.height(GAME_TILE_GAP))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.onContainer.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (badge > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = CircleShape,
                    color = surface,
                    shadowElevation = 1.dp,
                ) {
                    Text(
                        text = badge.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        // The "you have N due" number, so it clears the text bar, not the icon one.
                        color = hue.ensureContrastWith(surface, TEXT_MIN_RATIO),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun IconTile(
    @DrawableRes iconRes: Int,
    palette: SubjectPalette,
    tileSize: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    tintIcon: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(tileSize)
            .background(palette.container, RoundedCornerShape(tileSize / 3)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (tintIcon) palette.accent else Color.Unspecified,
            modifier = Modifier.size(iconSize),
        )
    }
}

// endregion

// region Latest

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
private fun LatestRow(activity: RecentActivity) {
    val surface = MaterialTheme.colorScheme.surface
    val visuals = activity.source.visuals()
    val spent = activity.points < 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconTile(
                iconRes = visuals.iconRes,
                palette = subjectPalette(visuals.hue),
                tileSize = 32.dp,
                iconSize = 18.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(activity.modeResId),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = resolveTimeAgo(activity.timeAgo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Spent points arrive as negative events, so never hardcode the sign.
            Text(
                text = if (spent) activity.points.toString() else "+${activity.points}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (spent) SpentRed else StreakOrange.ensureContrastWith(surface, TEXT_MIN_RATIO),
            )
        }
    }
}

/** Icon and hue for a points source, so Latest matches the game cards. */
private data class SourceVisuals(
    @DrawableRes val iconRes: Int,
    val hue: Color,
)

private fun PointSource.visuals(): SourceVisuals = when (this) {
    PointSource.DICTEE -> SourceVisuals(CoreUiR.drawable.ic_subject_dictee, SubjectDictee)
    PointSource.MATH -> SourceVisuals(CoreUiR.drawable.ic_subject_math, SubjectMath)
    PointSource.POEMS -> SourceVisuals(CoreUiR.drawable.ic_subject_poems, SubjectPoems)
    PointSource.READING -> SourceVisuals(CoreUiR.drawable.ic_subject_reading, SubjectReading)
    PointSource.CONJUGATION -> SourceVisuals(CoreUiR.drawable.ic_subject_verbs, SubjectVerbs)
    PointSource.CHALLENGE -> SourceVisuals(CoreUiR.drawable.ic_subject_arcade, SubjectArcade)
    PointSource.DAILY_LOGIN -> SourceVisuals(CoreUiR.drawable.ic_streak_flame, StreakOrange)
    // A gift is the parent handing stars over; a redemption is the parent taking them
    // back for a real-world reward; a purchase is the child spending them in the shop.
    PointSource.GIFT -> SourceVisuals(CoreUiR.drawable.ic_gift_trade, PointsGold)
    PointSource.REDEMPTION -> SourceVisuals(CoreUiR.drawable.ic_gift_trade, SpentRed)
    PointSource.PURCHASE -> SourceVisuals(CoreUiR.drawable.ic_gift_trade, SpentRed)
    PointSource.PLAN_BONUS -> SourceVisuals(CoreUiR.drawable.ic_milestone_star, PointsGold)
}

@Composable
private fun EmptyLatest() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(CoreUiR.drawable.ic_milestone_star),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(34.dp),
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
private fun resolveTimeAgo(timeAgo: TimeAgo): String = when (timeAgo) {
    is TimeAgo.JustNow -> stringResource(CoreUiR.string.time_just_now)
    is TimeAgo.Minutes -> stringResource(CoreUiR.string.time_minutes_ago, timeAgo.minutes.toInt())
    is TimeAgo.Hours -> stringResource(CoreUiR.string.time_hours_ago, timeAgo.hours.toInt())
    is TimeAgo.Yesterday -> stringResource(CoreUiR.string.time_yesterday)
    is TimeAgo.Days -> stringResource(CoreUiR.string.time_days_ago, timeAgo.days.toInt())
}

// endregion

private val GAME_GAP = 11.dp
private val GAME_CARD_PADDING = 14.dp
private val GAME_TILE_SIZE = 44.dp
private val GAME_TILE_GAP = 10.dp
private const val SCENE_PANE_WEIGHT = 0.42f
private const val AVATAR_MAX_SCREEN_FRACTION = 0.28f
private const val DONE_LABEL_ALPHA = 0.72f

private val SpentRed = Color(0xFFB24A3A)

/** Material's minimum comfortable target; a 7-year-old's aim is not a designer's. */
private val MIN_TOUCH_TARGET = 48.dp

// region Previews

private fun previewState() = HomeState(
    profileName = "Myriam",
    totalStars = 240,
    dayStreak = 4,
    tablesDue = 3,
    atelierDueVerbs = 5,
    recentActivities = listOf(
        RecentActivity(
            modeResId = CoreUiR.string.mode_dictee,
            source = PointSource.DICTEE,
            points = 80,
            timeAgo = TimeAgo.Minutes(12),
        ),
        // A gift is stars the parent granted, so it is positive; a purchase is the
        // negative branch. Both must render, hence one of each.
        RecentActivity(
            modeResId = CoreUiR.string.home_source_gift,
            source = PointSource.GIFT,
            points = 200,
            timeAgo = TimeAgo.Yesterday,
        ),
        RecentActivity(
            modeResId = CoreUiR.string.rewards_title,
            source = PointSource.PURCHASE,
            points = -500,
            timeAgo = TimeAgo.Days(2),
        ),
    ),
    isLoading = false,
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    StudyBuddyTheme {
        HomeContent(state = previewState(), onIntent = {})
    }
}

/** Nothing due — the quest offers rather than nags. */
@Preview(showBackground = true)
@Composable
private fun HomeScreenFreeChoicePreview() {
    StudyBuddyTheme {
        HomeContent(
            state = previewState().copy(
                tablesDue = 0,
                atelierDueVerbs = 0,
                dayStreak = 0,
                recentActivities = emptyList(),
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 900, heightDp = 1000)
@Composable
private fun HomeScreenTabletPreview() {
    CompositionLocalProvider(LocalLayoutType provides LayoutType.MEDIUM) {
        StudyBuddyTheme {
            HomeContent(state = previewState(), onIntent = {})
        }
    }
}

@Preview(showBackground = true, fontScale = 1.3f)
@Composable
private fun HomeScreenLargeFontPreview() {
    StudyBuddyTheme {
        HomeContent(state = previewState(), onIntent = {})
    }
}

// endregion
