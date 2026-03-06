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
        // Epic skins
        "cyberpunk_bunny" to R.drawable.avatar_cyberpunk_bunny,
        "engineer_cat" to R.drawable.avatar_engineer_cat,
        "samurai_fox" to R.drawable.avatar_samurai_fox,
        "pirate_panda" to R.drawable.avatar_pirate_panda,
        "ninja_squirrel" to R.drawable.avatar_ninja_squirrel,
        "dj_hedgehog" to R.drawable.avatar_dj_hedgehog,
        // Legendary skins
        "elder_dragon" to R.drawable.avatar_elder_dragon,
        "wizard_owl" to R.drawable.avatar_wizard_owl,
        "royal_unicorn" to R.drawable.avatar_royal_unicorn,
        "robot_dog" to R.drawable.avatar_robot_dog,
        "phoenix_butterfly" to R.drawable.avatar_phoenix_butterfly,
        "steampunk_hamster" to R.drawable.avatar_steampunk_hamster,
        "space_penguin" to R.drawable.avatar_space_penguin,
    )

    /**
     * Returns the drawable resource ID for the given character, or the fox fallback.
     */
    fun getDrawable(characterId: String): Int = drawableMap[characterId] ?: drawableMap["fox"]!!
}
