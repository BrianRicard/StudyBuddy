package com.studybuddy.core.ui.avatar

import com.studybuddy.core.ui.R

/**
 * Maps character IDs to their vector drawable resource IDs.
 *
 * The hybrid avatar system renders creature bodies from polished vector
 * drawable XMLs (with gradients and smooth curves) while accessories
 * are still drawn via Canvas at attachment-point anchors.
 */
object AvatarCharacterDrawables {

    private val drawableMap: Map<String, Int> = mapOf(
        "fox" to R.drawable.avatar_fox,
        "cat" to R.drawable.avatar_cat,
        "unicorn" to R.drawable.avatar_unicorn,
        "panda" to R.drawable.avatar_panda,
        "butterfly" to R.drawable.avatar_butterfly,
        "bunny" to R.drawable.avatar_bunny,
        "owl" to R.drawable.avatar_owl,
        "dragon" to R.drawable.avatar_dragon,
        "dog" to R.drawable.avatar_dog,
        "bear" to R.drawable.avatar_bear,
        "blue_monster" to R.drawable.avatar_blue_monster,
        "shrimp" to R.drawable.avatar_shrimp,
        "shark" to R.drawable.avatar_shark,
        "octopus" to R.drawable.avatar_octopus,
        "moose" to R.drawable.avatar_moose,
        "canada_goose" to R.drawable.avatar_canada_goose,
        "turkey" to R.drawable.avatar_turkey,
        "squirrel" to R.drawable.avatar_squirrel,
    )

    /**
     * Returns the drawable resource ID for the given character, or the fox fallback.
     */
    fun getDrawable(characterId: String): Int =
        drawableMap[characterId] ?: drawableMap["fox"]!!
}
