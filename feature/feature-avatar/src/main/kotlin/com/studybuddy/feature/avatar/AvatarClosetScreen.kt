package com.studybuddy.feature.avatar

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.studybuddy.core.ui.R as CoreUiR
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

// region Character Grid

@Composable
private fun CharacterGrid(
    characters: List<CharacterBody>,
    selectedBodyId: String,
    ownedItemIds: Set<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMN_COUNT),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items = characters, key = { _, item -> item.id }) { index, character ->
            val isOwned = RewardCatalog.isCharacterOwned(character.id, ownedItemIds)
            val isSelected = character.id == selectedBodyId
            val charItem = RewardCatalog.getCharacterItem(character.id)

            CharacterCard(
                character = character,
                isOwned = isOwned,
                isSelected = isSelected,
                cost = charItem?.cost ?: 0,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .bounceClick(onClick),
        shape = MaterialTheme.shapes.medium,
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp),
            ) {
                CharacterPreview(
                    characterId = character.id,
                    size = 48.dp,
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

            // Lock icon for unowned characters (top-start corner)
            if (!isOwned) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(16.dp),
                )
            }

            // Star cost badge for unowned characters (bottom-end corner)
            if (!isOwned && cost > 0) {
                CostBadge(
                    cost = cost,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
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

private const val GRID_COLUMN_COUNT = 3

// region Previews

@Preview(showBackground = true)
@Composable
private fun AvatarClosetScreenPreview() {
    StudyBuddyTheme {
        AvatarClosetContent(
            state = AvatarClosetState(
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
