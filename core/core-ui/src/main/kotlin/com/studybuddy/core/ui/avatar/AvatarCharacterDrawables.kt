package com.studybuddy.core.ui.avatar

import com.studybuddy.core.ui.R

/**
 * Maps character IDs to their drawable resource IDs — vector XMLs for the
 * original roster, generated PNGs (drawable-nodpi) for the newer characters.
 * Accessories are drawn via Canvas at attachment-point anchors either way.
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
        // Verb Quest creatures (Flux-generated PNGs)
        "frog" to R.drawable.avatar_frog,
        "snail" to R.drawable.avatar_snail,
        "ladybug" to R.drawable.avatar_ladybug,
        "lion" to R.drawable.avatar_lion,
        // Generated hero roster (Flux-generated PNGs)
        "hockey_duck" to R.drawable.avatar_hockey_duck,
        "business_demon" to R.drawable.avatar_business_demon,
        "hero_gecko" to R.drawable.avatar_hero_gecko,
        "atomic_tardigrade" to R.drawable.avatar_atomic_tardigrade,
        "gadget_octopus" to R.drawable.avatar_gadget_octopus,
        "arcade_goose" to R.drawable.avatar_arcade_goose,
        // Legendary skins
        "elder_dragon" to R.drawable.avatar_elder_dragon,
        "wizard_owl" to R.drawable.avatar_wizard_owl,
        "royal_unicorn" to R.drawable.avatar_royal_unicorn,
        "robot_dog" to R.drawable.avatar_robot_dog,
        "phoenix_butterfly" to R.drawable.avatar_phoenix_butterfly,
        "steampunk_hamster" to R.drawable.avatar_steampunk_hamster,
        "space_penguin" to R.drawable.avatar_space_penguin,
        // New expanded roster (Flux-generated PNGs, batch 3)
        "raccoon" to R.drawable.avatar_raccoon,
        "koala" to R.drawable.avatar_koala,
        "otter" to R.drawable.avatar_otter,
        "sloth" to R.drawable.avatar_sloth,
        "hedgehog" to R.drawable.avatar_hedgehog,
        "flamingo" to R.drawable.avatar_flamingo,
        "red_panda" to R.drawable.avatar_red_panda,
        "narwhal" to R.drawable.avatar_narwhal,
        "axolotl" to R.drawable.avatar_axolotl,
        "peacock" to R.drawable.avatar_peacock,
        "arctic_fox" to R.drawable.avatar_arctic_fox,
        "dolphin" to R.drawable.avatar_dolphin,
        "astronaut_koala" to R.drawable.avatar_astronaut_koala,
        "chef_raccoon" to R.drawable.avatar_chef_raccoon,
        "explorer_sloth" to R.drawable.avatar_explorer_sloth,
        "knight_hedgehog" to R.drawable.avatar_knight_hedgehog,
        "pilot_narwhal" to R.drawable.avatar_pilot_narwhal,
        "artist_flamingo" to R.drawable.avatar_artist_flamingo,
        "scientist_axolotl" to R.drawable.avatar_scientist_axolotl,
        "crystal_stag" to R.drawable.avatar_crystal_stag,
        "thunder_ram" to R.drawable.avatar_thunder_ram,
        "moon_wolf" to R.drawable.avatar_moon_wolf,
        "golden_griffin" to R.drawable.avatar_golden_griffin,
        "abyss_kraken" to R.drawable.avatar_abyss_kraken,
        "mythic_pegasus" to R.drawable.avatar_mythic_pegasus,
    )

    /**
     * Returns the drawable resource ID for the given character, or the fox fallback.
     */
    fun getDrawable(characterId: String): Int = drawableMap[characterId] ?: drawableMap["fox"]!!

    /**
     * Returns the drawable resource ID for the given character, or null when
     * the character has no image asset and should be Canvas-drawn instead
     * (see [CreatureCanvas]). Currently every catalog character has an asset;
     * the null path is a safety net for unknown ids.
     */
    fun getDrawableOrNull(characterId: String): Int? = drawableMap[characterId]
}
