package com.studybuddy.feature.rewards

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.AvatarTier
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.AccessoryPreview
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.PointsBadge
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.modifier.animateItemAppearance
import com.studybuddy.core.ui.modifier.bounceClick
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
fun RewardsShopScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RewardsShopViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RewardsShopEffect.PurchaseSuccess -> {
                    snackbarHostState.showSnackbar("Unlocked ${effect.itemName}!")
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
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardsShopContent(
    state: RewardsShopState,
    onIntent: (RewardsShopIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.rewards_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    PointsBadge(
                        points = state.starBalance,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                            selectedTier = state.selectedTier,
                            onSelectTier = { onIntent(RewardsShopIntent.SelectTier(it)) },
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Image(
                            painter = painterResource(tab.tabIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = when (tab) {
                                RewardsTab.AVATAR -> stringResource(CoreUiR.string.rewards_tab_avatar)
                                RewardsTab.THEMES -> stringResource(CoreUiR.string.rewards_tab_themes)
                                RewardsTab.EFFECTS -> stringResource(CoreUiR.string.rewards_tab_effects)
                                RewardsTab.TITLES -> stringResource(CoreUiR.string.rewards_tab_titles)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
            )
        }
    }
}

private val RewardsTab.tabIconRes: Int
    @DrawableRes get() = when (this) {
        RewardsTab.AVATAR -> CoreUiR.drawable.ic_shop_characters
        RewardsTab.THEMES -> CoreUiR.drawable.ic_shop_themes
        RewardsTab.EFFECTS -> CoreUiR.drawable.ic_shop_effects
        RewardsTab.TITLES -> CoreUiR.drawable.ic_trophy
    }

// endregion

// region Avatar Tab

@Composable
private fun AvatarTabContent(
    ownedItemIds: Set<String>,
    selectedTier: AvatarTier?,
    onSelectTier: (AvatarTier?) -> Unit,
    onPurchase: (RewardItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun filterByTier(items: List<RewardItem>): List<RewardItem> =
        if (selectedTier == null) items else items.filter { it.tier == selectedTier }

    val characterItems = filterByTier(
        RewardCatalog.characterItems.filterNot { it.cost == 0 && it.tier == AvatarTier.STARTER },
    )
    val sections = listOf(
        AvatarSection(
            title = stringResource(CoreUiR.string.rewards_characters_section),
            items = characterItems,
        ),
        AvatarSection(
            title = stringResource(CoreUiR.string.rewards_hats_section),
            items = filterByTier(RewardCatalog.hats.filterNot { it.id.endsWith("_none") }),
        ),
        AvatarSection(
            title = stringResource(CoreUiR.string.rewards_face_section),
            items = filterByTier(
                RewardCatalog.faceAccessories.filterNot { it.id.endsWith("_none") },
            ),
        ),
        AvatarSection(
            title = stringResource(CoreUiR.string.rewards_outfits_section),
            items = filterByTier(RewardCatalog.outfits.filterNot { it.id.endsWith("_none") }),
        ),
        AvatarSection(
            title = stringResource(CoreUiR.string.rewards_pets_section),
            items = filterByTier(RewardCatalog.pets.filterNot { it.id.endsWith("_none") }),
        ),
    ).filter { it.items.isNotEmpty() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TierFilterRow(
                selectedTier = selectedTier,
                onSelectTier = onSelectTier,
            )
        }
        sections.forEach { section ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }
            itemsIndexed(items = section.items, key = { _, it -> it.id }) { index, item ->
                AvatarItemCard(
                    item = item,
                    isOwned = item.id in ownedItemIds,
                    onPurchase = { onPurchase(item) },
                    modifier = Modifier.animateItemAppearance(index),
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
    val tierColor = tierColor(item.tier)
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .bounceClick { if (!isOwned && item.cost > 0) onPurchase() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) {
                CorrectGreen.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isOwned) 2.dp else 0.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (item.category == RewardCategory.CHARACTER) {
                Text(
                    text = item.icon,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                AccessoryPreview(
                    itemId = item.id,
                    size = 32.dp,
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
            Spacer(modifier = Modifier.height(2.dp))
            if (item.tier != AvatarTier.STARTER && !isOwned) {
                TierBadge(tier = item.tier)
                Spacer(modifier = Modifier.height(2.dp))
            }
            if (isOwned) {
                OwnedBadge()
            } else if (item.cost > 0) {
                CostChip(cost = item.cost)
            }
        }
    }
}

@Composable
private fun TierFilterRow(
    selectedTier: AvatarTier?,
    onSelectTier: (AvatarTier?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilterChipItem(
            label = "All",
            selected = selectedTier == null,
            onClick = { onSelectTier(null) },
        )
        AvatarTier.entries.forEach { tier ->
            FilterChipItem(
                label = tier.label,
                selected = selectedTier == tier,
                color = tierColor(tier),
                onClick = { onSelectTier(tier) },
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        modifier = modifier.bounceClick(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) color.copy(alpha = 0.2f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun TierBadge(
    tier: AvatarTier,
    modifier: Modifier = Modifier,
) {
    val color = tierColor(tier)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = tier.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
        )
    }
}

@Composable
private fun tierColor(tier: AvatarTier): Color = when (tier) {
    AvatarTier.STARTER -> CorrectGreen
    AvatarTier.COMMON -> Color(0xFF5B8DEF)
    AvatarTier.RARE -> Color(0xFF9B59B6)
    AvatarTier.EPIC -> Color(0xFFE67E22)
    AvatarTier.LEGENDARY -> Color(0xFFE74C3C)
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
        itemsIndexed(items = RewardCatalog.themes, key = { _, it -> it.id }) { index, theme ->
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
                modifier = Modifier.animateItemAppearance(index),
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
                        text = stringResource(CoreUiR.string.rewards_activate),
                        onClick = onActivate,
                    )
                    else -> Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        TierBadge(tier = theme.tier)
                        StudyBuddyButton(
                            text = "\u2B50 ${theme.cost}",
                            onClick = onPurchase,
                        )
                    }
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
                text = stringResource(CoreUiR.string.rewards_active),
                style = MaterialTheme.typography.labelMedium,
                color = CorrectGreen,
            )
        }
    }
}

private fun getThemePreviewColors(themeId: String): List<Color> = when (themeId.lowercase()) {
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
                text = stringResource(CoreUiR.string.rewards_celebrations),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        // Celebrations grid rendered as pairs of rows inside the LazyColumn
        val celebrationItems = RewardCatalog.effects
        val chunkedCelebrations = celebrationItems.chunked(2)
        itemsIndexed(items = chunkedCelebrations) { index, rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemAppearance(index),
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
                text = stringResource(CoreUiR.string.rewards_sounds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        itemsIndexed(items = RewardCatalog.sounds, key = { _, it -> it.id }) { index, sound ->
            SoundCard(
                item = sound,
                isOwned = sound.id in ownedItemIds,
                onPurchase = { onPurchase(sound) },
                modifier = Modifier.animateItemAppearance(index),
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
            Spacer(modifier = Modifier.height(4.dp))
            if (isOwned) {
                OwnedBadge()
            } else {
                if (item.tier != AvatarTier.STARTER) {
                    TierBadge(tier = item.tier)
                    Spacer(modifier = Modifier.height(2.dp))
                }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items = RewardCatalog.titles, key = { _, it -> it.id }) { index, title ->
            val isUnlocked = title.id in ownedItemIds
            val isEquipped = title.id == equippedTitle

            TitleCard(
                title = title,
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                onEquip = { onEquip(title.id) },
                modifier = Modifier.animateItemAppearance(index),
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                text = stringResource(CoreUiR.string.rewards_equipped),
                                style = MaterialTheme.typography.labelMedium,
                                color = CorrectGreen,
                            )
                        }
                    }
                }
                isUnlocked -> {
                    StudyBuddyOutlinedButton(
                        text = stringResource(CoreUiR.string.rewards_equip),
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
                text = stringResource(CoreUiR.string.locked),
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
                text = stringResource(CoreUiR.string.rewards_owned),
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
            Text(text = stringResource(CoreUiR.string.rewards_get_item, item.name))
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
                if (item.tier != AvatarTier.STARTER) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TierBadge(tier = item.tier)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(CoreUiR.string.rewards_cost_message, item.cost),
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
                text = stringResource(CoreUiR.string.rewards_buy_item, item.cost),
                onClick = onConfirm,
            )
        },
        dismissButton = {
            StudyBuddyOutlinedButton(
                text = stringResource(CoreUiR.string.cancel),
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
                ownedItemIds = RewardCatalog.starterItemIds + setOf("hat_crown", "pet_fish"),
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
