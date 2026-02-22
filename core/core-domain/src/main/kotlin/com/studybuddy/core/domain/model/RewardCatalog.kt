package com.studybuddy.core.domain.model

object RewardCatalog {

    // ── Characters (bodies) ────────────────────────────────────────────────────
    // emoji field is kept as a text fallback; actual rendering uses CreatureCanvas.
    val characters = listOf(
        // Original roster
        CharacterBody("fox", "Fox", "\uD83E\uDD8A"),
        CharacterBody("cat", "Cat", "\uD83D\uDC31"),
        CharacterBody("unicorn", "Unicorn", "\uD83E\uDD84"),
        CharacterBody("panda", "Panda", "\uD83D\uDC3C"),
        CharacterBody("butterfly", "Butterfly", "\uD83E\uDD8B"),
        CharacterBody("bunny", "Bunny", "\uD83D\uDC30"),
        CharacterBody("owl", "Owl", "\uD83E\uDD89"),
        CharacterBody("dragon", "Dragon", "\uD83D\uDC09"),
        // New: Canadian & marine creatures
        CharacterBody("dog", "Dog", "\uD83D\uDC36"),
        CharacterBody("bear", "Bear", "\uD83D\uDC3B"),
        CharacterBody("blue_monster", "Blue Monster", "\uD83D\uDC7E"),
        CharacterBody("shrimp", "Shrimp", "\uD83E\uDDE4"),
        CharacterBody("shark", "Shark", "\uD83E\uDD88"),
        CharacterBody("octopus", "Octopus", "\uD83D\uDC19"),
        CharacterBody("moose", "Moose", "\uD83E\uDEAB"),
        CharacterBody("canada_goose", "Canada Goose", "\uD83E\uDD9A"),
        CharacterBody("turkey", "Turkey", "\uD83E\uDD83"),
        CharacterBody("squirrel", "Squirrel", "\uD83D\uDC3F\uFE0F"),
    )

    // ── Hats ──────────────────────────────────────────────────────────────────
    val hats = listOf(
        RewardItem("hat_none", RewardCategory.HAT, "None", "", 0),
        RewardItem("hat_tophat", RewardCategory.HAT, "Top Hat", "\uD83C\uDFA9", 0),
        RewardItem("hat_crown", RewardCategory.HAT, "Crown", "\uD83D\uDC51", 50),
        RewardItem("hat_wizard", RewardCategory.HAT, "Wizard", "\uD83E\uDDD9", 75),
        RewardItem("hat_party", RewardCategory.HAT, "Party Hat", "\uD83E\uDD73", 0),
        RewardItem("hat_beret", RewardCategory.HAT, "Beret", "\uD83E\uDDE2", 40),
        RewardItem("hat_flower", RewardCategory.HAT, "Flower Crown", "\uD83C\uDF3A", 30),
        RewardItem("hat_cap", RewardCategory.HAT, "Ball Cap", "\uD83E\uDDE2", 25),
        // Canadian / new
        RewardItem("hat_toque", RewardCategory.HAT, "Toque", "\uD83E\uDDE3", 0,
            "Classic Canadian winter hat — toasty warm!"),
        RewardItem("hat_hockey_helmet", RewardCategory.HAT, "Hockey Helmet", "\uD83C\uDFC2", 50,
            "Protect that big brain!"),
        RewardItem("hat_maple_crown", RewardCategory.HAT, "Maple Crown", "\uD83C\uDF41", 60,
            "Ruler of the north"),
        RewardItem("hat_graduation", RewardCategory.HAT, "Graduation Cap", "\uD83C\uDF93", 55,
            "Scholar of the year"),
        RewardItem("hat_chef", RewardCategory.HAT, "Chef Hat", "\uD83D\uDC68\u200D\uD83C\uDF73", 35,
            "Master chef in the making"),
        RewardItem("hat_hardhat", RewardCategory.HAT, "Hard Hat", "\uD83D\uDEE1\uFE0F", 30,
            "Safety first!"),
    )

    // ── Face accessories ───────────────────────────────────────────────────────
    val faceAccessories = listOf(
        RewardItem("face_none", RewardCategory.FACE, "None", "", 0),
        RewardItem("face_shades", RewardCategory.FACE, "Shades", "\uD83D\uDD76\uFE0F", 0),
        RewardItem("face_monocle", RewardCategory.FACE, "Monocle", "\uD83E\uDDD0", 60),
        RewardItem("face_glasses", RewardCategory.FACE, "Glasses", "\uD83D\uDC53", 0),
        RewardItem("face_mask", RewardCategory.FACE, "Theatre Mask", "\uD83C\uDFAD", 45),
        RewardItem("face_star", RewardCategory.FACE, "Star Mark", "\u2B50", 35),
        // New
        RewardItem("face_maple_blush", RewardCategory.FACE, "Maple Blush", "\uD83C\uDF41", 30,
            "Rosy maple-leaf cheeks"),
        RewardItem("face_hockey_mask", RewardCategory.FACE, "Hockey Mask", "\uD83D\uDD74\uFE0F", 45,
            "Goalie game face on"),
        RewardItem("face_heart", RewardCategory.FACE, "Heart Eyes", "\uD83D\uDC95", 30,
            "So much love!"),
        RewardItem("face_clown", RewardCategory.FACE, "Clown Nose", "\uD83E\uDD21", 25,
            "Honk honk!"),
    )

    // ── Outfits ────────────────────────────────────────────────────────────────
    val outfits = listOf(
        RewardItem("outfit_none", RewardCategory.OUTFIT, "None", "", 0),
        RewardItem("outfit_scarf", RewardCategory.OUTFIT, "Scarf", "\uD83E\uDDE3", 0),
        RewardItem("outfit_bowtie", RewardCategory.OUTFIT, "Bow Tie", "\uD83C\uDF80", 30),
        RewardItem("outfit_cape", RewardCategory.OUTFIT, "Cape", "\uD83E\uDDB8", 80),
        RewardItem("outfit_medal", RewardCategory.OUTFIT, "Gold Medal", "\uD83C\uDFC5", 60),
        RewardItem("outfit_necklace", RewardCategory.OUTFIT, "Necklace", "\uD83D\uDCFF", 40),
        // New
        RewardItem("outfit_hockey_jersey", RewardCategory.OUTFIT, "Hockey Jersey", "\uD83C\uDFC2", 70,
            "Ready for the big game"),
        RewardItem("outfit_flannel", RewardCategory.OUTFIT, "Flannel Shirt", "\uD83E\uDEB5", 45,
            "Lumberjack chic"),
        RewardItem("outfit_maple_tee", RewardCategory.OUTFIT, "Maple Leaf Tee", "\uD83C\uDF41", 35,
            "True Canadian pride"),
        RewardItem("outfit_tuxedo", RewardCategory.OUTFIT, "Tuxedo", "\uD83E\uDD35", 90,
            "Dressed to impress"),
        RewardItem("outfit_lab_coat", RewardCategory.OUTFIT, "Lab Coat", "\uD83E\uDDEA", 65,
            "Science is cool"),
        RewardItem("outfit_raincoat", RewardCategory.OUTFIT, "Rain Coat", "\uD83C\uDF27\uFE0F", 50,
            "April showers ready"),
        RewardItem("outfit_superhero", RewardCategory.OUTFIT, "Superhero Suit", "\uD83E\uDDB8", 100,
            "Hero of the classroom"),
    )

    // ── Pets ──────────────────────────────────────────────────────────────────
    val pets = listOf(
        RewardItem("pet_none", RewardCategory.PET, "None", "", 0),
        RewardItem("pet_chick", RewardCategory.PET, "Chick", "\uD83D\uDC25", 0),
        RewardItem("pet_hamster", RewardCategory.PET, "Hamster", "\uD83D\uDC39", 50),
        RewardItem("pet_fish", RewardCategory.PET, "Fish", "\uD83D\uDC20", 35),
        RewardItem("pet_snail", RewardCategory.PET, "Snail", "\uD83D\uDC0C", 25),
        RewardItem("pet_ladybug", RewardCategory.PET, "Ladybug", "\uD83D\uDC1E", 30),
        // New Canadian & marine pets
        RewardItem("pet_beaver", RewardCategory.PET, "Beaver", "\uD83E\uDDAB", 45,
            "Canada's national symbol"),
        RewardItem("pet_loon", RewardCategory.PET, "Loon", "\uD83E\uDD86", 40,
            "Canada's national bird"),
        RewardItem("pet_polar_bear", RewardCategory.PET, "Polar Bear Cub", "\uD83D\uDC3B\u200D\u2744\uFE0F", 60,
            "Arctic cuddle buddy"),
        RewardItem("pet_raccoon", RewardCategory.PET, "Raccoon", "\uD83E\uDD9D", 40,
            "Little masked bandit"),
        RewardItem("pet_maple_bug", RewardCategory.PET, "Maple Bug", "\uD83D\uDC1B", 20,
            "Tiny and cute"),
        RewardItem("pet_narwhal", RewardCategory.PET, "Narwhal", "\uD83E\uDD84", 55,
            "Unicorn of the sea!"),
    )

    // ── Themes ────────────────────────────────────────────────────────────────
    val themes = listOf(
        RewardItem("theme_sunset", RewardCategory.THEME, "Sunset", "\uD83C\uDF05", 0, "Warm orange tones (default)"),
        RewardItem("theme_ocean", RewardCategory.THEME, "Ocean", "\uD83C\uDF0A", 100, "Cool blue waves"),
        RewardItem("theme_forest", RewardCategory.THEME, "Forest", "\uD83C\uDF32", 100, "Fresh green vibes"),
        RewardItem("theme_galaxy", RewardCategory.THEME, "Galaxy", "\uD83C\uDF0C", 150, "Dark purple cosmos"),
        RewardItem("theme_candy", RewardCategory.THEME, "Candy", "\uD83C\uDF6C", 120, "Sweet pink tones"),
        RewardItem("theme_arctic", RewardCategory.THEME, "Arctic", "\u2744\uFE0F", 100, "Icy cyan cool"),
        RewardItem("theme_maple", RewardCategory.THEME, "Maple", "\uD83C\uDF41", 110, "Autumn red and gold"),
    )

    // ── Effects ───────────────────────────────────────────────────────────────
    val effects = listOf(
        RewardItem("effect_confetti", RewardCategory.EFFECT, "Confetti", "\uD83C\uDF89", 0, "Classic celebration"),
        RewardItem("effect_fireworks", RewardCategory.EFFECT, "Fireworks", "\uD83C\uDF86", 80, "Light up the sky"),
        RewardItem("effect_unicorn", RewardCategory.EFFECT, "Unicorn Dance", "\uD83E\uDD84", 120, "Magical sparkles"),
        RewardItem("effect_rainbow", RewardCategory.EFFECT, "Rainbow Burst", "\uD83C\uDF08", 90, "Colorful rainbow"),
        RewardItem("effect_stars", RewardCategory.EFFECT, "Star Shower", "\u2B50", 70, "Raining stars"),
        RewardItem("effect_rockstar", RewardCategory.EFFECT, "Rock Star", "\uD83C\uDFB8", 100, "Rock and roll"),
        RewardItem("effect_champion", RewardCategory.EFFECT, "Champion", "\uD83C\uDFC6", 110, "Victory moment"),
        RewardItem("effect_dragon", RewardCategory.EFFECT, "Dragon Fire", "\uD83D\uDD25", 150, "Breathe fire"),
        RewardItem("effect_maple_storm", RewardCategory.EFFECT, "Maple Storm", "\uD83C\uDF41", 95, "Leaves in the wind"),
    )

    // ── Sounds ────────────────────────────────────────────────────────────────
    val sounds = listOf(
        RewardItem("sound_chime", RewardCategory.SOUND, "Chime", "\uD83D\uDD14", 0, "Gentle chime"),
        RewardItem("sound_fanfare", RewardCategory.SOUND, "Fanfare", "\uD83C\uDFBA", 50, "Trumpet fanfare"),
        RewardItem("sound_arcade", RewardCategory.SOUND, "Arcade", "\uD83C\uDFAE", 60, "Retro arcade"),
        RewardItem("sound_musical", RewardCategory.SOUND, "Musical", "\uD83C\uDFB5", 40, "Musical notes"),
        RewardItem("sound_goose", RewardCategory.SOUND, "Goose Honk", "\uD83E\uDD9A", 35, "Classic Canadian"),
    )

    // ── Titles ────────────────────────────────────────────────────────────────
    val titles = listOf(
        RewardItem("title_rising_star", RewardCategory.TITLE, "Rising Star", "\uD83C\uDF1F", 0, "Earn 100 stars"),
        RewardItem("title_word_wizard", RewardCategory.TITLE, "Word Wizard", "\uD83D\uDCDD", 0, "Master 25 words"),
        RewardItem("title_speed_demon", RewardCategory.TITLE, "Speed Demon", "\u26A1", 0, "Avg response < 3s"),
        RewardItem("title_streak_champion", RewardCategory.TITLE, "Streak Champion", "\uD83D\uDD25", 0, "7-day streak"),
        RewardItem(
            "title_perfect_scholar",
            RewardCategory.TITLE,
            "Perfect Scholar",
            "\uD83C\uDFC6",
            0,
            "100% on 10 sessions",
        ),
        RewardItem(
            "title_star_collector",
            RewardCategory.TITLE,
            "Star Collector",
            "\uD83D\uDC8E",
            0,
            "Earn 5,000 stars",
        ),
        RewardItem("title_polyglot", RewardCategory.TITLE, "Polyglot", "\uD83C\uDF0D", 0, "Practice in 3 languages"),
        RewardItem("title_grand_master", RewardCategory.TITLE, "Grand Master", "\uD83C\uDF93", 0, "Unlock all titles"),
        RewardItem("title_true_north", RewardCategory.TITLE, "True North", "\uD83C\uDDE8\uD83C\uDDE6", 0, "Complete a challenge session"),
        RewardItem("title_mix_master", RewardCategory.TITLE, "Mix Master", "\uD83C\uDFA7", 0, "Complete 5 challenge sessions"),
    )

    /** All avatar-category items (hats + face + outfits + pets). */
    val avatarItems: List<RewardItem>
        get() = hats + faceAccessories + outfits + pets

    /** All purchasable items across every category. */
    val allItems: List<RewardItem>
        get() = avatarItems + themes + effects + sounds + titles

    /** Items that come free for new players. */
    val starterItemIds = setOf(
        "hat_none", "hat_tophat", "hat_party", "hat_toque",
        "face_none", "face_shades", "face_glasses",
        "outfit_none", "outfit_scarf",
        "pet_none", "pet_chick",
        "theme_sunset",
        "effect_confetti",
        "sound_chime",
    )

    fun getItemsByCategory(category: RewardCategory): List<RewardItem> = allItems.filter { it.category == category }

    fun getItemById(id: String): RewardItem? = allItems.firstOrNull { it.id == id }

    fun isStarterItem(itemId: String): Boolean = itemId in starterItemIds
}

data class CharacterBody(val id: String, val name: String, val emoji: String)
