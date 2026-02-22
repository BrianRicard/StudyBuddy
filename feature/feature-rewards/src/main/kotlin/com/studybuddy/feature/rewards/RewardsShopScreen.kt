package com.studybuddy.feature.rewards

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.PointsBadge
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.theme.ArcticColorScheme
import com.studybuddy.core.ui.theme.CandyColorScheme
import com.studybuddy.core.ui.theme.CorrectGreen
import com.studybuddy.core.ui.theme.ForestColorScheme
import com.studybuddy.core.ui.theme.GalaxyColorScheme
import com.studybuddy.core.ui.theme.OceanColorScheme
import com.studybuddy.core.ui.theme.PointsGold
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.core.ui.theme.SunsetColorScheme
import kotlinx.coroutines.launch

@Composable
fun RewardsShopScreen(viewModel: RewardsShopViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RewardsShopEffect.PurchaseSuccess -> {
                    // Snackbar or celebration can be added here
                }
                is RewardsShopEffect.ThemeChanged -> {
                    // Theme change handled globally
                }
            }
        }
    }

    RewardsShopContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardsShopContent(
    state: RewardsShopState,
    onIntent: (RewardsShopIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
        pageCount = { RewardsTab.entries.size },
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab.ordinal) {
            pagerState.animateScrollToPage(state.selectedTab.ordinal)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val tab = RewardsTab.entries[pagerState.currentPage]
        if (tab != state.selectedTab) {
            onIntent(RewardsShopIntent.SelectTab(tab))
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Rewards Shop") },
                actions = {
                    PointsBadge(
                        points = state.starBalance,
                        modifier = Modifier.padding(end = 16.dp),
                    )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                RewardsTabBar(
                    selectedTab = state.selectedTab,
                    onTabSelected = { tab ->
                        onIntent(RewardsShopIntent.SelectTab(tab))
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                    },
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                ) { page ->
                    when (RewardsTab.entries[page]) {
                        RewardsTab.AVATAR -> AvatarTabContent(
                            ownedItemIds = state.ownedItemIds,
                            onPurchase = { onIntent(RewardsShopIntent.RequestPurchase(it)) },
                        )
                        RewardsTab.THEMES -> ThemesTabContent(
                            ownedItemIds = state.ownedItemIds,
                            activeTheme = state.activeTheme,
                            onActivate = { onIntent(RewardsShopIntent.ActivateTheme(it)) },
                            onPurchase = { onIntent(RewardsShopIntent.RequestPurchase(it)) },
                        )
                        RewardsTab.EFFECTS -> EffectsTabContent(
                            ownedItemIds = state.ownedItemIds,
                            onPurchase = { onIntent(RewardsShopIntent.RequestPurchase(it)) },
                        )
                        RewardsTab.TITLES -> TitlesTabContent(
                            ownedItemIds = state.ownedItemIds,
                            equippedTitle = state.equippedTitle,
                            onEquip = { onIntent(RewardsShopIntent.EquipTitle(it)) },
                        )
                    }
                }
            }
        }
    }

    state.showPurchaseDialog?.let { item ->
        PurchaseConfirmDialog(
            item = item,
            errorMessage = state.purchaseError,
            onConfirm = { onIntent(RewardsShopIntent.ConfirmPurchase) },
            onDismiss = { onIntent(RewardsShopIntent.DismissDialog) },
        )
    }
}

// region Tab Bar

@Composable
private fun RewardsTabBar(
    selectedTab: RewardsTab,
    onTabSelected: (RewardsTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier.fillMaxWidth(),
    ) {
        RewardsTab.entries.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = "${tab.tabIcon} ${tab.tabLabel}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

private val RewardsTab.tabLabel: String
    get() = when (this) {
        RewardsTab.AVATAR -> "Avatar"
        RewardsTab.THEMES -> "Themes"
        RewardsTab.EFFECTS -> "Effects"
        RewardsTab.TITLES -> "Titles"
    }

private val RewardsTab.tabIcon: String
    get() = when (this) {
        RewardsTab.AVATAR -> "\uD83D\uDC64"
        RewardsTab.THEMES -> "\uD83C\uDFA8"
        RewardsTab.EFFECTS -> "\u2728"
        RewardsTab.TITLES -> "\uD83C\uDFC5"
    }

// endregion

// region Avatar Tab

@Composable
private fun AvatarTabContent(
    ownedItemIds: Set<String>,
    onPurchase: (RewardItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = listOf(
        AvatarSection(title = "\uD83C\uDFA9 Hats", items = RewardCatalog.hats),
        AvatarSection(title = "\uD83D\uDD76\uFE0F Face", items = RewardCatalog.faceAccessories),
        AvatarSection(title = "\uD83D\uDC54 Outfits", items = RewardCatalog.outfits),
        AvatarSection(title = "\uD83D\uDC3E Pets", items = RewardCatalog.pets),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sections.forEach { section ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }
            items(items = section.items, key = { it.id }) { item ->
                AvatarItemCard(
                    item = item,
                    isOwned = item.id in ownedItemIds,
                    onPurchase = { onPurchase(item) },
                )
            }
        }
    }
}

private data class AvatarSection(val title: String, val items: List<RewardItem>)

@Composable
private fun AvatarItemCard(
    item: RewardItem,
    isOwned: Boolean,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isOwned) CorrectGreen else MaterialTheme.colorScheme.outlineVariant,
        label = "avatar-item-border",
    )

    Card(
        onClick = { if (!isOwned && item.cost > 0) onPurchase() },
        modifier = modifier.aspectRatio(0.85f),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) {
                CorrectGreen.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isOwned) 2.dp else 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (item.icon.isNotEmpty()) {
                Text(
                    text = item.icon,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isOwned) {
                OwnedBadge()
            } else if (item.cost > 0) {
                CostChip(cost = item.cost)
            }
        }
    }
}

// endregion

// region Themes Tab

@Composable
private fun ThemesTabContent(
    ownedItemIds: Set<String>,
    activeTheme: String,
    onActivate: (String) -> Unit,
    onPurchase: (RewardItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = RewardCatalog.themes, key = { it.id }) { theme ->
            val themeId = theme.id.removePrefix("theme_")
            val isOwned = theme.id in ownedItemIds
            val isActive = themeId.equals(activeTheme, ignoreCase = true)
            val gradientColors = getThemePreviewColors(themeId)

            ThemeCard(
                theme = theme,
                gradientColors = gradientColors,
                isOwned = isOwned,
                isActive = isActive,
                onActivate = { onActivate(themeId) },
                onPurchase = { onPurchase(theme) },
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: RewardItem,
    gradientColors: List<Color>,
    isOwned: Boolean,
    isActive: Boolean,
    onActivate: () -> Unit,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 2.dp,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                    ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${theme.icon} ${theme.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    theme.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                when {
                    isActive -> ActiveBadge()
                    isOwned -> StudyBuddyOutlinedButton(
                        text = "Activate",
                        onClick = onActivate,
                    )
                    else -> StudyBuddyButton(
                        text = "\u2B50 ${theme.cost}",
                        onClick = onPurchase,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = CorrectGreen.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = CorrectGreen,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Active",
                style = MaterialTheme.typography.labelMedium,
                color = CorrectGreen,
            )
        }
    }
}

private fun getThemePreviewColors(themeId: String): List<Color> =
    when (themeId.lowercase()) {
        "sunset" -> listOf(
            SunsetColorScheme.primary,
            SunsetColorScheme.secondary,
            SunsetColorScheme.tertiary,
        )
        "ocean" -> listOf(
            OceanColorScheme.primary,
            OceanColorScheme.secondary,
            OceanColorScheme.tertiary,
        )
        "forest" -> listOf(
            ForestColorScheme.primary,
            ForestColorScheme.secondary,
            ForestColorScheme.tertiary,
        )
        "galaxy" -> listOf(
            GalaxyColorScheme.primary,
            GalaxyColorScheme.secondary,
            GalaxyColorScheme.tertiary,
        )
        "candy" -> listOf(
            CandyColorScheme.primary,
            CandyColorScheme.secondary,
            CandyColorScheme.tertiary,
        )
        "arctic" -> listOf(
            ArcticColorScheme.primary,
            ArcticColorScheme.secondary,
            ArcticColorScheme.tertiary,
        )
        else -> listOf(
            SunsetColorScheme.primary,
            SunsetColorScheme.secondary,
            SunsetColorScheme.tertiary,
        )
    }

// endregion

// region Effects Tab

@Composable
private fun EffectsTabContent(
    ownedItemIds: Set<String>,
    onPurchase: (RewardItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "\uD83C\uDF89 Celebrations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        // Celebrations grid rendered as pairs of rows inside the LazyColumn
        val celebrationItems = RewardCatalog.effects
        val chunkedCelebrations = celebrationItems.chunked(2)
        items(items = chunkedCelebrations) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { effect ->
                    EffectCard(
                        item = effect,
                        isOwned = effect.id in ownedItemIds,
                        onPurchase = { onPurchase(effect) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill remaining space if odd number of items
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\uD83D\uDD0A Sounds",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        items(items = RewardCatalog.sounds, key = { it.id }) { sound ->
            SoundCard(
                item = sound,
                isOwned = sound.id in ownedItemIds,
                onPurchase = { onPurchase(sound) },
            )
        }
    }
}

@Composable
private fun EffectCard(
    item: RewardItem,
    isOwned: Boolean,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { if (!isOwned && item.cost > 0) onPurchase() },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) {
                CorrectGreen.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = item.icon,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (isOwned) {
                OwnedBadge()
            } else {
                CostChip(cost = item.cost)
            }
        }
    }
}

@Composable
private fun SoundCard(
    item: RewardItem,
    isOwned: Boolean,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { if (!isOwned && item.cost > 0) onPurchase() },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) {
                CorrectGreen.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.icon,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                item.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isOwned) {
                OwnedBadge()
            } else {
                CostChip(cost = item.cost)
            }
        }
    }
}

// endregion

// region Titles Tab

@Composable
private fun TitlesTabContent(
    ownedItemIds: Set<String>,
    equippedTitle: String?,
    onEquip: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = RewardCatalog.titles, key = { it.id }) { title ->
            val isUnlocked = title.id in ownedItemIds
            val isEquipped = title.id == equippedTitle

            TitleCard(
                title = title,
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                onEquip = { onEquip(title.id) },
            )
        }
    }
}

@Composable
private fun TitleCard(
    title: RewardItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isEquipped -> CorrectGreen.copy(alpha = 0.1f)
                isUnlocked -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEquipped) 4.dp else 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.icon,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                )
                title.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            when {
                isEquipped -> {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = CorrectGreen.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CorrectGreen,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Equipped",
                                style = MaterialTheme.typography.labelMedium,
                                color = CorrectGreen,
                            )
                        }
                    }
                }
                isUnlocked -> {
                    StudyBuddyOutlinedButton(
                        text = "Equip",
                        onClick = onEquip,
                    )
                }
                else -> {
                    LockedBadge()
                }
            }
        }
    }
}

@Composable
private fun LockedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Locked",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// endregion

// region Shared Badges

@Composable
private fun OwnedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = CorrectGreen.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = CorrectGreen,
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "Owned",
                style = MaterialTheme.typography.labelSmall,
                color = CorrectGreen,
            )
        }
    }
}

@Composable
private fun CostChip(
    cost: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = PointsGold.copy(alpha = 0.2f),
    ) {
        Text(
            text = "\u2B50 $cost",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

// endregion

// region Purchase Dialog

@Composable
private fun PurchaseConfirmDialog(
    item: RewardItem,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Get ${item.name}?")
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.icon,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This will cost \u2B50 ${item.cost} stars.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            StudyBuddyButton(
                text = "Buy \u2B50 ${item.cost}",
                onClick = onConfirm,
            )
        },
        dismissButton = {
            StudyBuddyOutlinedButton(
                text = "Cancel",
                onClick = onDismiss,
            )
        },
    )
}

// endregion

// region Previews

@Preview(showBackground = true)
@Composable
private fun RewardsShopAvatarTabPreview() {
    StudyBuddyTheme {
        RewardsShopContent(
            state = RewardsShopState(
                selectedTab = RewardsTab.AVATAR,
                ownedItemIds = RewardCatalog.starterItemIds + setOf("hat_crown", "pet_hamster"),
                starBalance = 320L,
                activeTheme = "sunset",
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardsShopThemesTabPreview() {
    StudyBuddyTheme {
        RewardsShopContent(
            state = RewardsShopState(
                selectedTab = RewardsTab.THEMES,
                ownedItemIds = RewardCatalog.starterItemIds + setOf("theme_ocean"),
                starBalance = 450L,
                activeTheme = "sunset",
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardsShopEffectsTabPreview() {
    StudyBuddyTheme {
        RewardsShopContent(
            state = RewardsShopState(
                selectedTab = RewardsTab.EFFECTS,
                ownedItemIds = RewardCatalog.starterItemIds + setOf(
                    "effect_fireworks",
                    "sound_fanfare",
                ),
                starBalance = 200L,
                activeTheme = "sunset",
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardsShopTitlesTabPreview() {
    StudyBuddyTheme {
        RewardsShopContent(
            state = RewardsShopState(
                selectedTab = RewardsTab.TITLES,
                ownedItemIds = RewardCatalog.starterItemIds + setOf("title_rising_star"),
                starBalance = 150L,
                activeTheme = "sunset",
                equippedTitle = "title_rising_star",
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardsShopLoadingPreview() {
    StudyBuddyTheme {
        RewardsShopContent(
            state = RewardsShopState(isLoading = true),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchaseDialogPreview() {
    StudyBuddyTheme {
        PurchaseConfirmDialog(
            item = RewardItem(
                id = "theme_galaxy",
                category = RewardCategory.THEME,
                name = "Galaxy",
                icon = "\uD83C\uDF0C",
                cost = 150,
                description = "Dark purple cosmos",
            ),
            errorMessage = null,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchaseDialogErrorPreview() {
    StudyBuddyTheme {
        PurchaseConfirmDialog(
            item = RewardItem(
                id = "effect_dragon",
                category = RewardCategory.EFFECT,
                name = "Dragon Fire",
                icon = "\uD83D\uDD25",
                cost = 150,
                description = "Breathe fire",
            ),
            errorMessage = "You need 75 more stars!",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

// endregion
