package com.studybuddy.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.ui.avatar.AvatarCharacterRegistry
import com.studybuddy.core.ui.avatar.CreatureCanvas
import com.studybuddy.core.ui.theme.StudyBuddyTheme

/**
 * Renders a creature avatar with accessories snapped to per-character attachment points.
 *
 * Each creature in [AvatarCharacterRegistry] defines fractional anchor positions for:
 *  - hats    → top of head
 *  - face    → eye level (masks, glasses, stars)
 *  - outfits → neck / chest (scarves, bow ties, medals)
 *  - pets    → companion position
 *
 * Accessories are centred at their anchor and scaled relative to [size].
 */
@Composable
fun AvatarComposite(
    config: AvatarConfig,
    modifier: Modifier = Modifier,
    size: Dp = DEFAULT_AVATAR_SIZE,
) {
    val character = RewardCatalog.characters.firstOrNull { it.id == config.bodyId }
        ?: RewardCatalog.characters.first()
    val hat = RewardCatalog.hats.firstOrNull { it.id == config.hatId }
    val face = RewardCatalog.faceAccessories.firstOrNull { it.id == config.faceId }
    val outfit = RewardCatalog.outfits.firstOrNull { it.id == config.outfitId }
    val pet = RewardCatalog.pets.firstOrNull { it.id == config.petId }

    val spec = AvatarCharacterRegistry.getSpec(config.bodyId)
    val ap = spec.attachmentPoints

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = "Avatar: ${character.name}" +
                    (hat?.takeIf { it.icon.isNotEmpty() }?.let { " wearing ${it.name}" } ?: "") +
                    (face?.takeIf { it.icon.isNotEmpty() }?.let { " with ${it.name}" } ?: "") +
                    (outfit?.takeIf { it.icon.isNotEmpty() }?.let { ", ${it.name}" } ?: "") +
                    (pet?.takeIf { it.icon.isNotEmpty() }?.let { " and ${it.name}" } ?: "")
            },
    ) {
        // ── Body — Canvas-drawn creature ──────────────────────────────────
        CreatureCanvas(spec = spec, size = size)

        // ── Hat ───────────────────────────────────────────────────────────
        if (hat != null && hat.icon.isNotEmpty()) {
            val hatSize = size * HAT_SCALE * ap.hatScale
            // Centre of the accessory is placed at the anchor point
            val offsetX = size * ap.hatAnchor.x - hatSize / 2
            val offsetY = size * ap.hatAnchor.y - hatSize / 2
            Text(
                text = hat.icon,
                fontSize = (hatSize.value).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .absoluteOffset(x = offsetX, y = offsetY)
                    .rotate(ap.hatRotation),
            )
        }

        // ── Face accessory ────────────────────────────────────────────────
        if (face != null && face.icon.isNotEmpty()) {
            val faceSize = size * FACE_SCALE * ap.faceScale
            val offsetX = size * ap.faceAnchor.x - faceSize / 2
            val offsetY = size * ap.faceAnchor.y - faceSize / 2
            Text(
                text = face.icon,
                fontSize = (faceSize.value).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.absoluteOffset(x = offsetX, y = offsetY),
            )
        }

        // ── Outfit / neck accessory ───────────────────────────────────────
        if (outfit != null && outfit.icon.isNotEmpty()) {
            val outfitSize = size * OUTFIT_SCALE
            val offsetX = size * ap.chestAnchor.x - outfitSize / 2
            val offsetY = size * ap.chestAnchor.y - outfitSize / 2
            Text(
                text = outfit.icon,
                fontSize = (outfitSize.value).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.absoluteOffset(x = offsetX, y = offsetY),
            )
        }

        // ── Pet companion ─────────────────────────────────────────────────
        if (pet != null && pet.icon.isNotEmpty()) {
            val petSize = size * PET_SCALE
            val offsetX = size * ap.petAnchor.x - petSize / 2
            val offsetY = size * ap.petAnchor.y - petSize / 2
            Text(
                text = pet.icon,
                fontSize = (petSize.value).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.absoluteOffset(x = offsetX, y = offsetY),
            )
        }
    }
}

private val DEFAULT_AVATAR_SIZE = 130.dp

// Accessory size as fraction of total avatar size
private const val HAT_SCALE = 0.28f
private const val FACE_SCALE = 0.24f
private const val OUTFIT_SCALE = 0.24f
private const val PET_SCALE = 0.22f

@Preview(showBackground = true)
@Composable
private fun AvatarCompositeFullPreview() {
    StudyBuddyTheme {
        AvatarComposite(
            config = AvatarConfig(
                bodyId = "cat",
                hatId = "hat_crown",
                faceId = "face_shades",
                outfitId = "outfit_scarf",
                petId = "pet_chick",
            ),
            size = 130.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarCompositeMoosePreview() {
    StudyBuddyTheme {
        AvatarComposite(
            config = AvatarConfig(
                bodyId = "moose",
                hatId = "hat_toque",
                faceId = "face_none",
                outfitId = "outfit_hockey_jersey",
                petId = "pet_beaver",
            ),
            size = 130.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarCompositeSmallPreview() {
    StudyBuddyTheme {
        AvatarComposite(
            config = AvatarConfig.default(),
            size = 50.dp,
        )
    }
}
