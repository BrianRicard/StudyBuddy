package com.studybuddy.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.ui.avatar.AvatarCharacterDrawables
import com.studybuddy.core.ui.avatar.AvatarCharacterRegistry
import com.studybuddy.core.ui.avatar.drawFaceAccessory
import com.studybuddy.core.ui.avatar.drawHat
import com.studybuddy.core.ui.avatar.drawOutfit
import com.studybuddy.core.ui.avatar.drawPet
import com.studybuddy.core.ui.theme.StudyBuddyTheme

/**
 * Renders a creature avatar using a hybrid approach:
 *
 * 1. **Body** — polished vector drawable (with gradients, smooth curves)
 * 2. **Accessories** — Canvas-drawn at per-character attachment points
 *
 * Each creature in [AvatarCharacterRegistry] defines fractional anchor positions for:
 *  - hats    -> top of head
 *  - face    -> eye level (masks, glasses, stars)
 *  - outfits -> neck / chest (scarves, bow ties, medals)
 *  - pets    -> companion position
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

    val semanticsModifier = modifier
        .size(size)
        .semantics {
            contentDescription = "Avatar: ${character.name}" +
                (hat?.takeIf { it.id != "hat_none" }?.let { " wearing ${it.name}" } ?: "") +
                (face?.takeIf { it.id != "face_none" }?.let { " with ${it.name}" } ?: "") +
                (outfit?.takeIf { it.id != "outfit_none" }?.let { ", ${it.name}" } ?: "") +
                (pet?.takeIf { it.id != "pet_none" }?.let { " and ${it.name}" } ?: "")
        }

    Box(modifier = semanticsModifier) {
        // ── Body — polished vector drawable ─────────────────────────────
        Image(
            painter = painterResource(AvatarCharacterDrawables.getDrawable(config.bodyId)),
            contentDescription = null,
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit,
        )

        // ── Accessories — Canvas overlay at attachment points ───────────
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outfit (drawn behind face / hat)
            if (outfit != null && outfit.id != "outfit_none") {
                val s = w * OUTFIT_SCALE
                val cx = w * ap.chestAnchor.x
                val cy = h * ap.chestAnchor.y
                drawOutfit(outfit.id, cx, cy, s / 2)
            }

            // Face accessory
            if (face != null && face.id != "face_none") {
                val s = w * FACE_SCALE * ap.faceScale
                val cx = w * ap.faceAnchor.x
                val cy = h * ap.faceAnchor.y
                drawFaceAccessory(face.id, cx, cy, s / 2)
            }

            // Hat (with rotation)
            if (hat != null && hat.id != "hat_none") {
                val s = w * HAT_SCALE * ap.hatScale
                val cx = w * ap.hatAnchor.x
                val cy = h * ap.hatAnchor.y
                if (ap.hatRotation != 0f) {
                    rotate(ap.hatRotation, pivot = Offset(cx, cy)) {
                        drawHat(hat.id, cx, cy, s / 2)
                    }
                } else {
                    drawHat(hat.id, cx, cy, s / 2)
                }
            }

            // Pet companion
            if (pet != null && pet.id != "pet_none") {
                val s = w * PET_SCALE
                val cx = w * ap.petAnchor.x
                val cy = h * ap.petAnchor.y
                drawPet(pet.id, cx, cy, s / 2)
            }
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
