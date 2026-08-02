package com.studybuddy.feature.avatar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.AvatarTier
import com.studybuddy.core.domain.model.CharacterBody
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.adaptive.AdaptiveDimensDefaults
import com.studybuddy.core.ui.adaptive.LayoutType
import com.studybuddy.core.ui.adaptive.LocalLayoutType
import com.studybuddy.core.ui.components.AvatarComposite
import com.studybuddy.core.ui.components.CharacterPreview
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.PointsBadge
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.modifier.animateItemAppearance
import com.studybuddy.core.ui.modifier.bounceClick
import com.studybuddy.core.ui.theme.CorrectGreen
import com.studybuddy.core.ui.theme.PointsGold
import com.studybuddy.core.ui.theme.StudyBuddyTheme

/**
 * Entry-point composable for the Avatar Closet screen.
 * Wires [AvatarClosetViewModel] to the stateless [AvatarClosetContent].
 */
@Composable
fun AvatarClosetScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: AvatarClosetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AvatarClosetEffect.PurchaseSuccess -> {
                    // Snackbar or toast can be wired here
                }
                is AvatarClosetEffect.ShowError -> {
                    // Error handling can be wired here
                }
            }
        }
    }

    AvatarClosetContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarClosetContent(
    state: AvatarClosetState,
    onIntent: (AvatarClosetIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.avatar_title)) },
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Large avatar preview
                AvatarPreviewSection(
                    config = state.avatarConfig ?: AvatarConfig.default(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Character grid fills remaining space
                CharacterGrid(
                    characters = RewardCatalog.characters,
                    selectedBodyId = state.avatarConfig?.bodyId ?: "",
                    ownedItemIds = state.ownedItemIds,
                    onSelect = {
                        onIntent(AvatarClosetIntent.SelectCharacter(it))
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    // Purchase confirmation dialog
    state.showPurchaseDialog?.let { item ->
        PurchaseDialog(
            item = item,
            errorMessage = state.purchaseError,
            onConfirm = { onIntent(AvatarClosetIntent.ConfirmPurchase) },
            onDismiss = { onIntent(AvatarClosetIntent.DismissPurchaseDialog) },
        )
    }
}

// region Avatar Preview

@Composable
private fun AvatarPreviewSection(
    config: AvatarConfig,
    modifier: Modifier = Modifier,
) {
    // The hero size comes from the *width* class, but it spends *height*: a phone
    // in landscape is EXPANDED and barely 400dp tall, so cap it against the screen
    // or the grid below is pushed out of sight.
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val heroSize = minOf(
        AdaptiveDimensDefaults.current().avatarHeroSize,
        screenHeight * HERO_MAX_SCREEN_FRACTION,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        AvatarComposite(
            config = config,
            size = heroSize,
        )
    }
}

// endregion

// region Character Grid

@Composable
private fun CharacterGrid(
    characters: List<CharacterBody>,
    selectedBodyId: String,
    ownedItemIds: Set<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Sort characters by tier ordinal then by price within each tier
    val sortedCharacters = remember(characters) {
        characters.sortedWith(
            compareBy<CharacterBody> {
                RewardCatalog.getCharacterItem(it.id)?.tier?.ordinal ?: 0
            }.thenBy {
                RewardCatalog.getCharacterItem(it.id)?.cost ?: 0
            },
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = AdaptiveDimensDefaults.current().avatarCellMinSize),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items = sortedCharacters, key = { _, item -> item.id }) { index, character ->
            val isOwned = RewardCatalog.isCharacterOwned(character.id, ownedItemIds)
            val isSelected = character.id == selectedBodyId
            val charItem = RewardCatalog.getCharacterItem(character.id)

            CharacterCard(
                character = character,
                isOwned = isOwned,
                isSelected = isSelected,
                cost = charItem?.cost ?: 0,
                tier = charItem?.tier ?: AvatarTier.STARTER,
                onClick = { onSelect(character.id) },
                modifier = Modifier.animateItemAppearance(index),
            )
        }
    }
}

@Composable
private fun CharacterCard(
    character: CharacterBody,
    isOwned: Boolean,
    isSelected: Boolean,
    cost: Int,
    tier: AvatarTier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tierBorder = when (tier) {
        AvatarTier.EPIC -> BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(EpicPurple, EpicPurpleLight),
            ),
        )
        AvatarTier.LEGENDARY -> BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(LegendaryGold, LegendaryAmber, LegendaryGold),
            ),
        )
        else -> null
    }

    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .bounceClick(onClick),
        shape = MaterialTheme.shapes.medium,
        border = tierBorder,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> CorrectGreen.copy(alpha = 0.12f)
                isOwned -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isSelected -> 4.dp
                isOwned -> 1.dp
                else -> 0.dp
            },
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // The art takes a share of the width, but never more height than is
            // left once the name and the badge band have had theirs — otherwise a
            // long name runs into the price on a phone-sized cell.
            val labelHeight = with(LocalDensity.current) {
                MaterialTheme.typography.labelMedium.lineHeight.toDp()
            }
            val artSize = (maxHeight - labelHeight - CARD_CHROME)
                .coerceIn(0.dp, maxWidth * CHARACTER_SIZE_FRACTION)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(
                    start = CARD_PADDING,
                    end = CARD_PADDING,
                    top = CARD_PADDING,
                    bottom = CARD_PADDING + BADGE_BAND,
                ),
            ) {
                CharacterPreview(
                    characterId = character.id,
                    size = artSize,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Selected checkmark (top-end corner)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(CoreUiR.string.rewards_equipped),
                    tint = CorrectGreen,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(18.dp),
                )
            }

            // Tier badge for Epic/Legendary (top-start corner when owned, below lock when not)
            if (tier == AvatarTier.EPIC || tier == AvatarTier.LEGENDARY) {
                TierBadge(
                    tier = tier,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                )
            }

            // Lock icon for unowned characters (bottom-start corner)
            if (!isOwned) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .size(16.dp),
                )
            }

            // Star cost badge for unowned characters (bottom-end corner)
            if (!isOwned && cost > 0) {
                CostBadge(
                    cost = cost,
                    tier = tier,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                )
            }
        }
    }
}

@Composable
private fun TierBadge(
    tier: AvatarTier,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (tier) {
        AvatarTier.EPIC -> EpicPurple.copy(alpha = 0.15f) to EpicPurple
        AvatarTier.LEGENDARY -> LegendaryGold.copy(alpha = 0.15f) to LegendaryAmber
        else -> return
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = bgColor,
    ) {
        Text(
            text = tier.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
        )
    }
}

@Composable
private fun CostBadge(
    cost: Int,
    tier: AvatarTier = AvatarTier.STARTER,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (tier) {
        AvatarTier.EPIC -> EpicPurple.copy(alpha = 0.15f)
        AvatarTier.LEGENDARY -> LegendaryGold.copy(alpha = 0.15f)
        else -> PointsGold.copy(alpha = 0.2f)
    }
    val textColor = when (tier) {
        AvatarTier.EPIC -> EpicPurple
        AvatarTier.LEGENDARY -> LegendaryAmber
        else -> Color.Unspecified
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = bgColor,
    ) {
        Text(
            text = "\u2B50 $cost",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 2.dp,
            ),
        )
    }
}

// endregion

// region Purchase Dialog

@Composable
private fun PurchaseDialog(
    item: RewardItem,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(CoreUiR.string.avatar_get_item, item.name))
        },
        text = {
            Column {
                Text(
                    text = "${item.icon} ${item.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(CoreUiR.string.avatar_cost_message, item.cost),
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
                text = stringResource(CoreUiR.string.avatar_buy_item, item.cost),
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

/** Share of the card width the character art takes when height allows. */
private const val CHARACTER_SIZE_FRACTION = 0.68f

/** Share of the screen height the hero avatar may take. */
private const val HERO_MAX_SCREEN_FRACTION = 0.35f

private val CARD_PADDING = 8.dp

/** Bottom strip of a card reserved for the lock and price badges. */
private val BADGE_BAND = 24.dp

/** Everything in a card that is not the art or the name: padding, gap, badges. */
private val CARD_CHROME = CARD_PADDING * 2 + 4.dp + BADGE_BAND

// Tier accent colors
private val EpicPurple = Color(0xFF9C27B0)
private val EpicPurpleLight = Color(0xFFCE93D8)
private val LegendaryGold = Color(0xFFFFD700)
private val LegendaryAmber = Color(0xFFFFA000)

// region Previews

private fun previewState() = AvatarClosetState(
    avatarConfig = AvatarConfig(
        bodyId = "bunny",
        hatId = "hat_none",
        faceId = "face_none",
        outfitId = "outfit_none",
        petId = "pet_none",
    ),
    ownedItemIds = RewardCatalog.starterItemIds,
    starBalance = 250L,
    isLoading = false,
)

@Preview(showBackground = true)
@Composable
private fun AvatarClosetScreenPreview() {
    StudyBuddyTheme {
        AvatarClosetContent(state = previewState(), onIntent = {})
    }
}

/**
 * Tablet portrait. [LocalLayoutType] must be provided explicitly — it is a
 * static CompositionLocal set only by MainActivity, so a `widthDp` preview alone
 * still renders the compact branch.
 */
@Preview(widthDp = 800, heightDp = 1000)
@Composable
private fun AvatarClosetTabletPreview() {
    CompositionLocalProvider(LocalLayoutType provides LayoutType.MEDIUM) {
        StudyBuddyTheme {
            AvatarClosetContent(state = previewState(), onIntent = {})
        }
    }
}

/** The tightest case: a phone-sized cell with the largest accessibility font. */
@Preview(showBackground = true, fontScale = 1.3f)
@Composable
private fun AvatarClosetLargeFontPreview() {
    StudyBuddyTheme {
        AvatarClosetContent(state = previewState(), onIntent = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarClosetLoadingPreview() {
    StudyBuddyTheme {
        AvatarClosetContent(
            state = AvatarClosetState(isLoading = true),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchaseDialogPreview() {
    StudyBuddyTheme {
        PurchaseDialog(
            item = RewardItem(
                id = "char_unicorn",
                category = RewardCategory.CHARACTER,
                name = "Unicorn",
                icon = "\uD83E\uDD84",
                cost = 100,
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
        PurchaseDialog(
            item = RewardItem(
                id = "char_dragon",
                category = RewardCategory.CHARACTER,
                name = "Dragon",
                icon = "\uD83D\uDC09",
                cost = 120,
            ),
            errorMessage = "You need 30 more stars!",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

// endregion
