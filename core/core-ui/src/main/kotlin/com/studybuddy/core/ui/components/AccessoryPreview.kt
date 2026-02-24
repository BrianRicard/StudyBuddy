package com.studybuddy.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studybuddy.core.ui.avatar.AvatarCharacterDrawables
import com.studybuddy.core.ui.avatar.drawFaceAccessory
import com.studybuddy.core.ui.avatar.drawHat
import com.studybuddy.core.ui.avatar.drawOutfit
import com.studybuddy.core.ui.avatar.drawPet

/**
 * Renders a small preview of an accessory item using the Canvas-based
 * AccessoryRenderer functions. Falls back to nothing for unknown/none items.
 */
@Composable
fun AccessoryPreview(
    itemId: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val s = this.size.minDimension * 0.8f

        when {
            itemId.startsWith("hat_") && itemId != "hat_none" -> drawHat(itemId, cx, cy, s)
            itemId.startsWith("face_") && itemId != "face_none" -> drawFaceAccessory(itemId, cx, cy, s)
            itemId.startsWith("outfit_") && itemId != "outfit_none" -> drawOutfit(itemId, cx, cy, s)
            itemId.startsWith("pet_") && itemId != "pet_none" -> drawPet(itemId, cx, cy, s)
        }
    }
}

/**
 * Renders a character body from its vector drawable resource.
 */
@Composable
fun CharacterPreview(
    characterId: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Image(
        painter = painterResource(id = AvatarCharacterDrawables.getDrawable(characterId)),
        contentDescription = characterId,
        modifier = modifier.size(size),
    )
}
