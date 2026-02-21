package com.studybuddy.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.CharacterBody
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.ui.theme.StudyBuddyTheme

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

    val bodyFontSize = (size.value * BODY_FONT_RATIO).sp
    val accessoryFontSize = (size.value * ACCESSORY_FONT_RATIO).sp
    val petFontSize = (size.value * PET_FONT_RATIO).sp
    val hatOffset = -(size.value * HAT_OFFSET_RATIO).dp
    val faceOffset = (size.value * FACE_OFFSET_RATIO).dp
    val outfitOffset = (size.value * OUTFIT_OFFSET_RATIO).dp
    val petOffsetX = (size.value * PET_OFFSET_X_RATIO).dp
    val petOffsetY = (size.value * PET_OFFSET_Y_RATIO).dp

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = "Avatar: ${character.name}" +
                    (hat?.takeIf { it.icon.isNotEmpty() }?.let { " with ${it.name}" } ?: "") +
                    (pet?.takeIf { it.icon.isNotEmpty() }?.let { " and ${it.name}" } ?: "")
            },
        contentAlignment = Alignment.Center,
    ) {
        // Body (character)
        Text(
            text = character.emoji,
            fontSize = bodyFontSize,
            textAlign = TextAlign.Center,
        )

        // Hat (above center)
        if (hat != null && hat.icon.isNotEmpty()) {
            Text(
                text = hat.icon,
                fontSize = accessoryFontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = hatOffset),
            )
        }

        // Face accessory (right side)
        if (face != null && face.icon.isNotEmpty()) {
            Text(
                text = face.icon,
                fontSize = accessoryFontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = faceOffset),
            )
        }

        // Outfit (below center)
        if (outfit != null && outfit.icon.isNotEmpty()) {
            Text(
                text = outfit.icon,
                fontSize = accessoryFontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = outfitOffset),
            )
        }

        // Pet (bottom-right corner)
        if (pet != null && pet.icon.isNotEmpty()) {
            Text(
                text = pet.icon,
                fontSize = petFontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = petOffsetX, y = petOffsetY),
            )
        }
    }
}

private val DEFAULT_AVATAR_SIZE = 130.dp
private const val BODY_FONT_RATIO = 0.5f
private const val ACCESSORY_FONT_RATIO = 0.22f
private const val PET_FONT_RATIO = 0.2f
private const val HAT_OFFSET_RATIO = 0.08f
private const val FACE_OFFSET_RATIO = 0.08f
private const val OUTFIT_OFFSET_RATIO = 0.08f
private const val PET_OFFSET_X_RATIO = 0.08f
private const val PET_OFFSET_Y_RATIO = 0.08f

@Preview(showBackground = true)
@Composable
private fun AvatarCompositePreview() {
    StudyBuddyTheme {
        AvatarComposite(
            config = AvatarConfig(
                bodyId = "unicorn",
                hatId = "hat_crown",
                faceId = "face_shades",
                outfitId = "outfit_cape",
                petId = "pet_chick",
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
