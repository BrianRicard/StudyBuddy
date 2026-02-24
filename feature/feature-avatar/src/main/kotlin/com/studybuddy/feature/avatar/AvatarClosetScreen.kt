package com.studybuddy.feature.avatar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.CharacterBody
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardCategory
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.ui.components.AccessoryPreview
import com.studybuddy.core.ui.components.AvatarComposite
import com.studybuddy.core.ui.components.CharacterPreview
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.PointsBadge
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
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
                title = { Text("Avatar Closet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                // Large avatar preview with real-time config updates
                AvatarPreviewSection(
                    config = state.avatarConfig ?: AvatarConfig.default(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal character selector (8 characters)
                CharacterSelector(
                    characters = RewardCatalog.characters,
                    selectedBodyId = state.avatarConfig?.bodyId ?: "",
                    onSelect = {
                        onIntent(AvatarClosetIntent.SelectCharacter(it))
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4 accessory category tabs
                AccessoryTabBar(
                    selectedTab = state.selectedTab,
                    onTabSelected = {
                        onIntent(AvatarClosetIntent.SelectTab(it))
                    },
                )

                // 3-column items grid fills remaining space
                ItemsGrid(
                    items = getItemsForTab(state.selectedTab),
                    ownedItemIds = state.ownedItemIds,
                    equippedItemId = getEquippedIdForTab(
                        tab = state.selectedTab,
                        config = state.avatarConfig,
                    ),
                    onEquip = { onIntent(AvatarClosetIntent.EquipItem(it)) },
                    onPurchase = {
                        onIntent(AvatarClosetIntent.RequestPurchase(it))
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    // Purchase confirmation dialog shown when user taps an unowned item
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        AvatarComposite(
            config = config,
            size = 130.dp,
        )
    }
}

// endregion

// region Character Selector

@Composable
private fun CharacterSelector(
    characters: List<CharacterBody>,
    selectedBodyId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = characters, key = { it.id }) { character ->
            CharacterChip(
                character = character,
                isSelected = character.id == selectedBodyId,
                onClick = { onSelect(character.id) },
            )
        }
    }
}

@Composable
private fun CharacterChip(
    character: CharacterBody,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "character-border",
    )

    Card(
        onClick = onClick,
        modifier = modifier.width(72.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CharacterPreview(
                characterId = character.id,
                size = 36.dp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = character.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// endregion

// region Accessory Tabs

@Composable
private fun AccessoryTabBar(
    selectedTab: AccessoryTab,
    onTabSelected: (AccessoryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier.fillMaxWidth(),
    ) {
        AccessoryTab.entries.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = "${tab.icon} ${tab.label}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

// endregion

// region Items Grid

@Composable
private fun ItemsGrid(
    items: List<RewardItem>,
    ownedItemIds: Set<String>,
    equippedItemId: String?,
    onEquip: (String) -> Unit,
    onPurchase: (RewardItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMN_COUNT),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = items, key = { it.id }) { item ->
            val isOwned = item.id in ownedItemIds
            val isEquipped = item.id == equippedItemId

            ItemCard(
                item = item,
                isOwned = isOwned,
                isEquipped = isEquipped,
                onClick = {
                    if (isOwned) {
                        onEquip(item.id)
                    } else {
                        onPurchase(item)
                    }
                },
            )
        }
    }
}

@Composable
private fun ItemCard(
    item: RewardItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isEquipped -> CorrectGreen
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "item-border",
    )

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (isEquipped) 2.dp else 1.dp,
            color = borderColor,
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isEquipped -> CorrectGreen.copy(alpha = 0.1f)
                isOwned -> MaterialTheme.colorScheme.surface
                else ->
                    MaterialTheme.colorScheme.surfaceVariant
                        .copy(alpha = 0.5f)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEquipped) 4.dp else 1.dp,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Icon + name centered in card
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AccessoryPreview(
                    itemId = item.id,
                    size = 32.dp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            // Equipped checkmark (top-end corner)
            if (isEquipped) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Equipped",
                    tint = CorrectGreen,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp),
                )
            }

            // Star cost badge for unowned items (bottom-end corner)
            if (!isOwned && item.cost > 0) {
                CostBadge(
                    cost = item.cost,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                )
            }
        }
    }
}

@Composable
private fun CostBadge(
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
            Text(text = "Get ${item.name}?")
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

// region Helper Functions

private fun getItemsForTab(tab: AccessoryTab): List<RewardItem> = when (tab) {
    AccessoryTab.HATS -> RewardCatalog.hats
    AccessoryTab.FACE -> RewardCatalog.faceAccessories
    AccessoryTab.OUTFIT -> RewardCatalog.outfits
    AccessoryTab.PETS -> RewardCatalog.pets
}

private fun getEquippedIdForTab(
    tab: AccessoryTab,
    config: AvatarConfig?,
): String? {
    if (config == null) return null
    return when (tab) {
        AccessoryTab.HATS -> config.hatId
        AccessoryTab.FACE -> config.faceId
        AccessoryTab.OUTFIT -> config.outfitId
        AccessoryTab.PETS -> config.petId
    }
}

private const val GRID_COLUMN_COUNT = 3

// endregion

// region Previews

@Preview(showBackground = true)
@Composable
private fun AvatarClosetScreenPreview() {
    StudyBuddyTheme {
        AvatarClosetContent(
            state = AvatarClosetState(
                avatarConfig = AvatarConfig(
                    bodyId = "unicorn",
                    hatId = "hat_crown",
                    faceId = "face_shades",
                    outfitId = "outfit_cape",
                    petId = "pet_chick",
                ),
                selectedTab = AccessoryTab.HATS,
                ownedItemIds = RewardCatalog.starterItemIds +
                    setOf("hat_crown"),
                starBalance = 250L,
                isLoading = false,
            ),
            onIntent = {},
        )
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
                id = "hat_wizard",
                category = RewardCategory.HAT,
                name = "Wizard",
                icon = "\uD83E\uDDD9",
                cost = 75,
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
                id = "hat_wizard",
                category = RewardCategory.HAT,
                name = "Wizard",
                icon = "\uD83E\uDDD9",
                cost = 75,
            ),
            errorMessage = "You need 30 more stars!",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

// endregion
