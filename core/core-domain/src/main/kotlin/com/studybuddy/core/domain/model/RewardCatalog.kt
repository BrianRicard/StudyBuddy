package com.studybuddy.core.domain.model

object RewardCatalog {

    // --- Characters (bodies) ---
    val characters = listOf(
        CharacterBody("fox", "Fox", "\uD83E\uDD8A"),
        CharacterBody("cat", "Cat", "\uD83D\uDC31"),
        CharacterBody("unicorn", "Unicorn", "\uD83E\uDD84"),
        CharacterBody("panda", "Panda", "\uD83D\uDC3C"),
        CharacterBody("butterfly", "Butterfly", "\uD83E\uDD8B"),
        CharacterBody("bunny", "Bunny", "\uD83D\uDC30"),
        CharacterBody("owl", "Owl", "\uD83E\uDD89"),
        CharacterBody("dragon", "Dragon", "\uD83D\uDC09"),
    )

    // --- Hats ---
    val hats = listOf(
        RewardItem("hat_none", RewardCategory.HAT, "None", "", 0),
        RewardItem("hat_tophat", RewardCategory.HAT, "Top Hat", "\uD83C\uDFA9", 0),
        RewardItem("hat_crown", RewardCategory.HAT, "Crown", "\uD83D\uDC51", 50),
        RewardItem("hat_wizard", RewardCategory.HAT, "Wizard", "\uD83E\uDDD9", 75),
        RewardItem("hat_party", RewardCategory.HAT, "Party", "\uD83E\uDD73", 0),
        RewardItem("hat_beret", RewardCategory.HAT, "Beret", "\uD83E\uDDE2", 40),
        RewardItem("hat_flower", RewardCategory.HAT, "Flower", "\uD83C\uDF3A", 30),
        RewardItem("hat_cap", RewardCategory.HAT, "Cap", "\uD83E\uDDE2", 25),
    )

    // --- Face accessories ---
    val faceAccessories = listOf(
        RewardItem("face_none", RewardCategory.FACE, "None", "", 0),
        RewardItem("face_shades", RewardCategory.FACE, "Shades", "\uD83D\uDD76\uFE0F", 0),
        RewardItem("face_monocle", RewardCategory.FACE, "Monocle", "\uD83E\uDDD0", 60),
        RewardItem("face_glasses", RewardCategory.FACE, "Glasses", "\uD83D\uDC53", 0),
        RewardItem("face_mask", RewardCategory.FACE, "Mask", "\uD83C\uDFAD", 45),
        RewardItem("face_star", RewardCategory.FACE, "Star", "\u2B50", 35),
    )

    // --- Outfits ---
    val outfits = listOf(
        RewardItem("outfit_none", RewardCategory.OUTFIT, "None", "", 0),
        RewardItem("outfit_scarf", RewardCategory.OUTFIT, "Scarf", "\uD83E\uDDE3", 0),
        RewardItem("outfit_bowtie", RewardCategory.OUTFIT, "Bow Tie", "\uD83C\uDF80", 30),
        RewardItem("outfit_cape", RewardCategory.OUTFIT, "Cape", "\uD83E\uDDB8", 80),
        RewardItem("outfit_medal", RewardCategory.OUTFIT, "Medal", "\uD83C\uDFC5", 60),
        RewardItem("outfit_necklace", RewardCategory.OUTFIT, "Necklace", "\uD83D\uDCFF", 40),
    )

    // --- Pets ---
    val pets = listOf(
        RewardItem("pet_none", RewardCategory.PET, "None", "", 0),
        RewardItem("pet_chick", RewardCategory.PET, "Chick", "\uD83D\uDC25", 0),
        RewardItem("pet_hamster", RewardCategory.PET, "Hamster", "\uD83D\uDC39", 50),
        RewardItem("pet_fish", RewardCategory.PET, "Fish", "\uD83D\uDC20", 35),
        RewardItem("pet_snail", RewardCategory.PET, "Snail", "\uD83D\uDC0C", 25),
        RewardItem("pet_ladybug", RewardCategory.PET, "Ladybug", "\uD83D\uDC1E", 30),
    )

    // --- Themes ---
    val themes = listOf(
        RewardItem("theme_sunset", RewardCategory.THEME, "Sunset", "\uD83C\uDF05", 0, "Warm orange tones (default)"),
        RewardItem("theme_ocean", RewardCategory.THEME, "Ocean", "\uD83C\uDF0A", 100, "Cool blue waves"),
        RewardItem("theme_forest", RewardCategory.THEME, "Forest", "\uD83C\uDF32", 100, "Fresh green vibes"),
        RewardItem("theme_galaxy", RewardCategory.THEME, "Galaxy", "\uD83C\uDF0C", 150, "Dark purple cosmos"),
        RewardItem("theme_candy", RewardCategory.THEME, "Candy", "\uD83C\uDF6C", 120, "Sweet pink tones"),
        RewardItem("theme_arctic", RewardCategory.THEME, "Arctic", "\u2744\uFE0F", 100, "Icy cyan cool"),
    )

    // --- Effects ---
    val effects = listOf(
        RewardItem("effect_confetti", RewardCategory.EFFECT, "Confetti", "\uD83C\uDF89", 0, "Classic celebration"),
        RewardItem("effect_fireworks", RewardCategory.EFFECT, "Fireworks", "\uD83C\uDF86", 80, "Light up the sky"),
        RewardItem("effect_unicorn", RewardCategory.EFFECT, "Unicorn Dance", "\uD83E\uDD84", 120, "Magical sparkles"),
        RewardItem("effect_rainbow", RewardCategory.EFFECT, "Rainbow Burst", "\uD83C\uDF08", 90, "Colorful rainbow"),
        RewardItem("effect_stars", RewardCategory.EFFECT, "Star Shower", "\u2B50", 70, "Raining stars"),
        RewardItem("effect_rockstar", RewardCategory.EFFECT, "Rock Star", "\uD83C\uDFB8", 100, "Rock and roll"),
        RewardItem("effect_champion", RewardCategory.EFFECT, "Champion", "\uD83C\uDFC6", 110, "Victory moment"),
        RewardItem("effect_dragon", RewardCategory.EFFECT, "Dragon Fire", "\uD83D\uDD25", 150, "Breathe fire"),
    )

    // --- Sounds ---
    val sounds = listOf(
        RewardItem("sound_chime", RewardCategory.SOUND, "Chime", "\uD83D\uDD14", 0, "Gentle chime"),
        RewardItem("sound_fanfare", RewardCategory.SOUND, "Fanfare", "\uD83C\uDFBA", 50, "Trumpet fanfare"),
        RewardItem("sound_arcade", RewardCategory.SOUND, "Arcade", "\uD83C\uDFAE", 60, "Retro arcade"),
        RewardItem("sound_musical", RewardCategory.SOUND, "Musical", "\uD83C\uDFB5", 40, "Musical notes"),
    )

    // --- Titles ---
    val titles = listOf(
        RewardItem("title_rising_star", RewardCategory.TITLE, "Rising Star", "\uD83C\uDF1F", 0, "Earn 100 stars"),
        RewardItem("title_word_wizard", RewardCategory.TITLE, "Word Wizard", "\uD83D\uDCDD", 0, "Master 25 words"),
        RewardItem("title_speed_demon", RewardCategory.TITLE, "Speed Demon", "\u26A1", 0, "Avg response < 3s"),
        RewardItem("title_streak_champion", RewardCategory.TITLE, "Streak Champion", "\uD83D\uDD25", 0, "7-day streak"),
        RewardItem(
            "title_perfect_scholar", RewardCategory.TITLE,
            "Perfect Scholar", "\uD83C\uDFC6", 0, "100% on 10 sessions",
        ),
        RewardItem(
            "title_star_collector", RewardCategory.TITLE,
            "Star Collector", "\uD83D\uDC8E", 0, "Earn 5,000 stars",
        ),
        RewardItem("title_polyglot", RewardCategory.TITLE, "Polyglot", "\uD83C\uDF0D", 0, "Practice in 3 languages"),
        RewardItem("title_grand_master", RewardCategory.TITLE, "Grand Master", "\uD83C\uDF93", 0, "Unlock all titles"),
    )

    /** All avatar-category items (hats + face + outfits + pets). */
    val avatarItems: List<RewardItem>
        get() = hats + faceAccessories + outfits + pets

    /** All purchasable items across every category. */
    val allItems: List<RewardItem>
        get() = avatarItems + themes + effects + sounds + titles

    /** Items that come free for new players. */
    val starterItemIds = setOf(
        "hat_none", "hat_tophat", "hat_party",
        "face_none", "face_shades", "face_glasses",
        "outfit_none", "outfit_scarf",
        "pet_none", "pet_chick",
        "theme_sunset",
        "effect_confetti",
        "sound_chime",
    )

    fun getItemsByCategory(category: RewardCategory): List<RewardItem> =
        allItems.filter { it.category == category }

    fun getItemById(id: String): RewardItem? =
        allItems.firstOrNull { it.id == id }

    fun isStarterItem(itemId: String): Boolean = itemId in starterItemIds
}

data class CharacterBody(
    val id: String,
    val name: String,
    val emoji: String,
)
