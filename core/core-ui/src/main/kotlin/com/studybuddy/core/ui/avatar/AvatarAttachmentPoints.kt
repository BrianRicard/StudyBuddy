package com.studybuddy.core.ui.avatar

import androidx.compose.ui.geometry.Offset

/**
 * Fractional anchor positions for each accessory slot, relative to the creature's bounding box.
 * All values are in [0f..1f] where (0,0) = top-left and (1,1) = bottom-right.
 *
 * These allow accessories to "snap" to anatomically correct positions on any creature,
 * regardless of how their body is shaped or proportioned.
 */
data class AvatarAttachmentPoints(
    /** Center-top of head — where hats, crowns, and hair accessories sit. */
    val hatAnchor: Offset,

    /** Eye-level center — where glasses, masks, and face accessories appear. */
    val faceAnchor: Offset,

    /** Neck/chest area — where scarves, bow ties, medals, and outfits attach. */
    val chestAnchor: Offset,

    /** Companion position — where the pet companion sits. Usually bottom-right. */
    val petAnchor: Offset,

    /** Rotation to apply to the hat (degrees). Useful for tilted or angled heads. */
    val hatRotation: Float = 0f,

    /**
     * Scale multiplier for hat size relative to the default.
     * Large-headed creatures (e.g. moose) get 1.2f; small-headed get 0.85f.
     */
    val hatScale: Float = 1.0f,

    /**
     * Scale multiplier for face accessories.
     * Wide faces (e.g. octopus) get 1.2f; narrow faces get 0.9f.
     */
    val faceScale: Float = 1.0f,
)
