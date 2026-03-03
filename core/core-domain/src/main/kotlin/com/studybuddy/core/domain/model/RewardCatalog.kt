package com.studybuddy.core.domain.model

object RewardCatalog {

    // ── Characters (bodies) ────────────────────────────────────────────────────
    // emoji field is kept as a text fallback; actual rendering uses CreatureCanvas.
    val characters = listOf(
        // Free starter characters
        CharacterBody("bunny", "Bunny", "\uD83D\uDC30"),
        CharacterBody("squirrel", "Squirrel", "\uD83D\uDC3F\uFE0F"),
        CharacterBody("dog", "Dog", "\uD83D\uDC36"),
        // Purchasable characters
        CharacterBody("fox", "Fox", "\uD83E\uDD8A"),
        CharacterBody("cat", "Cat", "\uD83D\uDC31"),
        CharacterBody("unicorn", "Unicorn", "\uD83E\uDD84"),
        CharacterBody("panda", "Panda", "\uD83D\uDC3C"),
        CharacterBody("butterfly", "Butterfly", "\uD83E\uDD8B"),
        CharacterBody("owl", "Owl", "\uD83E\uDD89"),
        CharacterBody("dragon", "Dragon", "\uD83D\uDC09"),
        CharacterBody("bear", "Bear", "\uD83D\uDC3B"),
        CharacterBody("blue_monster", "Blue Monster", "\uD83D\uDC7E"),
        CharacterBody("shrimp", "Shrimp", "\uD83E\uDDE4"),
        CharacterBody("shark", "Shark", "\uD83E\uDD88"),
        CharacterBody("octopus", "Octopus", "\uD83D\uDC19"),
        CharacterBody("moose", "Moose", "\uD83E\uDEAB"),
        CharacterBody("canada_goose", "Canada Goose", "\uD83E\uDD9A"),
        CharacterBody("turkey", "Turkey", "\uD83E\uDD83"),
    )

    // ── Character purchase items ────────────────────────────────────────────────
    // Maps each character to a RewardItem with a star cost and tier.
    // bunny, squirrel, dog are free starters; others cost stars to unlock.
    //
    // Tier pricing guide:
    //   STARTER  (0-25):   free starters
    //   COMMON   (30-75):  basic animals
    //   RARE     (100-200): popular/appealing animals
    //   EPIC     (300-500): fantasy creatures
    //   LEGENDARY(750-1500): premium/special characters
    val characterItems = listOf(
        // Starter (free)
        RewardItem("char_bunny", RewardCategory.CHARACTER, "Bunny", "\uD83D\uDC30", 0, AvatarTier.STARTER),
        RewardItem("char_squirrel", RewardCategory.CHARACTER, "Squirrel", "\uD83D\uDC3F\uFE0F", 0, AvatarTier.STARTER),
        RewardItem("char_dog", RewardCategory.CHARACTER, "Dog", "\uD83D\uDC36", 0, AvatarTier.STARTER),
        // Common
        RewardItem("char_cat", RewardCategory.CHARACTER, "Cat", "\uD83D\uDC31", 40, AvatarTier.COMMON),
        RewardItem("char_shrimp", RewardCategory.CHARACTER, "Shrimp", "\uD83E\uDDE4", 45, AvatarTier.COMMON),
        RewardItem("char_turkey", RewardCategory.CHARACTER, "Turkey", "\uD83E\uDD83", 50, AvatarTier.COMMON),
        RewardItem("char_owl", RewardCategory.CHARACTER, "Owl", "\uD83E\uDD89", 60, AvatarTier.COMMON),
        RewardItem("char_fox", RewardCategory.CHARACTER, "Fox", "\uD83E\uDD8A", 65, AvatarTier.COMMON),
        // Rare
        RewardItem("char_bear", RewardCategory.CHARACTER, "Bear", "\uD83D\uDC3B", 100, AvatarTier.RARE),
        RewardItem("char_butterfly", RewardCategory.CHARACTER, "Butterfly", "\uD83E\uDD8B", 120, AvatarTier.RARE),
        RewardItem("char_panda", RewardCategory.CHARACTER, "Panda", "\uD83D\uDC3C", 140, AvatarTier.RARE),
        RewardItem("char_canada_goose", RewardCategory.CHARACTER, "Canada Goose", "\uD83E\uDD9A", 150, AvatarTier.RARE),
        RewardItem("char_shark", RewardCategory.CHARACTER, "Shark", "\uD83E\uDD88", 175, AvatarTier.RARE),
        RewardItem("char_moose", RewardCategory.CHARACTER, "Moose", "\uD83E\uDEAB", 200, AvatarTier.RARE),
        // Epic
        RewardItem("char_octopus", RewardCategory.CHARACTER, "Octopus", "\uD83D\uDC19", 300, AvatarTier.EPIC),
        RewardItem("char_blue_monster", RewardCategory.CHARACTER, "Blue Monster", "\uD83D\uDC7E", 350, AvatarTier.EPIC),
        RewardItem("char_unicorn", RewardCategory.CHARACTER, "Unicorn", "\uD83E\uDD84", 400, AvatarTier.EPIC),
        // Legendary
        RewardItem("char_dragon", RewardCategory.CHARACTER, "Dragon", "\uD83D\uDC09", 750, AvatarTier.LEGENDARY),
    )

    // ── Hats ──────────────────────────────────────────────────────────────────
    val hats = listOf(
        RewardItem("hat_none", RewardCategory.HAT, "None", "", 0, AvatarTier.STARTER),
        RewardItem("hat_tophat", RewardCategory.HAT, "Top Hat", "\uD83C\uDFA9", 0, AvatarTier.STARTER),
        RewardItem("hat_party", RewardCategory.HAT, "Party Hat", "\uD83E\uDD73", 0, AvatarTier.STARTER),
        RewardItem("hat_crown", RewardCategory.HAT, "Crown", "\uD83D\uDC51", 120, AvatarTier.RARE),
        RewardItem("hat_wizard", RewardCategory.HAT, "Wizard Hat", "\uD83E\uDDD9", 300, AvatarTier.EPIC),
    )

    // ── Face accessories ───────────────────────────────────────────────────────
    val faceAccessories = listOf(
        RewardItem("face_none", RewardCategory.FACE, "None", "", 0, AvatarTier.STARTER),
        RewardItem("face_shades", RewardCategory.FACE, "Sunglasses", "\uD83D\uDD76\uFE0F", 0, AvatarTier.STARTER),
        RewardItem("face_mask", RewardCategory.FACE, "Mask", "\uD83C\uDFAD", 75, AvatarTier.COMMON),
        RewardItem("face_monocle", RewardCategory.FACE, "Monocle", "\uD83E\uDDD0", 150, AvatarTier.RARE),
    )

    // ── Outfits ────────────────────────────────────────────────────────────────
    val outfits = listOf(
        RewardItem("outfit_none", RewardCategory.OUTFIT, "None", "", 0, AvatarTier.STARTER),
    )

    // ── Pets ──────────────────────────────────────────────────────────────────
    val pets = listOf(
        RewardItem("pet_none", RewardCategory.PET, "None", "", 0, AvatarTier.STARTER),
        RewardItem("pet_chick", RewardCategory.PET, "Chick", "\uD83D\uDC25", 0, AvatarTier.STARTER),
        RewardItem("pet_fish", RewardCategory.PET, "Fish", "\uD83D\uDC20", 60, AvatarTier.COMMON),
    )

    // ── Themes ────────────────────────────────────────────────────────────────
    val themes = listOf(
        RewardItem(
            "theme_sunset",
            RewardCategory.THEME,
            "Sunset",
            "\uD83C\uDF05",
            0,
            AvatarTier.STARTER,
            "Warm orange tones (default)",
        ),
        RewardItem(
            "theme_ocean",
            RewardCategory.THEME,
            "Ocean",
            "\uD83C\uDF0A",
            100,
            AvatarTier.RARE,
            "Cool blue waves",
        ),
        RewardItem(
            "theme_forest",
            RewardCategory.THEME,
            "Forest",
            "\uD83C\uDF32",
            100,
            AvatarTier.RARE,
            "Fresh green vibes",
        ),
        RewardItem(
            "theme_arctic",
            RewardCategory.THEME,
            "Arctic",
            "\u2744\uFE0F",
            120,
            AvatarTier.RARE,
            "Icy cyan cool",
        ),
        RewardItem(
            "theme_maple",
            RewardCategory.THEME,
            "Maple",
            "\uD83C\uDF41",
            150,
            AvatarTier.RARE,
            "Autumn red and gold",
        ),
        RewardItem(
            "theme_candy",
            RewardCategory.THEME,
            "Candy",
            "\uD83C\uDF6C",
            200,
            AvatarTier.RARE,
            "Sweet pink tones",
        ),
        RewardItem(
            "theme_galaxy",
            RewardCategory.THEME,
            "Galaxy",
            "\uD83C\uDF0C",
            350,
            AvatarTier.EPIC,
            "Dark purple cosmos",
        ),
    )

    // ── Effects ───────────────────────────────────────────────────────────────
    val effects = listOf(
        RewardItem(
            "effect_confetti",
            RewardCategory.EFFECT,
            "Confetti",
            "\uD83C\uDF89",
            0,
            AvatarTier.STARTER,
            "Classic celebration",
        ),
        RewardItem(
            "effect_stars",
            RewardCategory.EFFECT,
            "Star Shower",
            "\u2B50",
            50,
            AvatarTier.COMMON,
            "Raining stars",
        ),
        RewardItem(
            "effect_fireworks",
            RewardCategory.EFFECT,
            "Fireworks",
            "\uD83C\uDF86",
            75,
            AvatarTier.COMMON,
            "Light up the sky",
        ),
        RewardItem(
            "effect_rainbow",
            RewardCategory.EFFECT,
            "Rainbow Burst",
            "\uD83C\uDF08",
            100,
            AvatarTier.RARE,
            "Colorful rainbow",
        ),
        RewardItem(
            "effect_maple_storm",
            RewardCategory.EFFECT,
            "Maple Storm",
            "\uD83C\uDF41",
            120,
            AvatarTier.RARE,
            "Leaves in the wind",
        ),
        RewardItem(
            "effect_rockstar",
            RewardCategory.EFFECT,
            "Rock Star",
            "\uD83C\uDFB8",
            150,
            AvatarTier.RARE,
            "Rock and roll",
        ),
        RewardItem(
            "effect_champion",
            RewardCategory.EFFECT,
            "Champion",
            "\uD83C\uDFC6",
            200,
            AvatarTier.RARE,
            "Victory moment",
        ),
        RewardItem(
            "effect_unicorn",
            RewardCategory.EFFECT,
            "Unicorn Dance",
            "\uD83E\uDD84",
            350,
            AvatarTier.EPIC,
            "Magical sparkles",
        ),
        RewardItem(
            "effect_dragon",
            RewardCategory.EFFECT,
            "Dragon Fire",
            "\uD83D\uDD25",
            500,
            AvatarTier.EPIC,
            "Breathe fire",
        ),
    )

    // ── Sounds ────────────────────────────────────────────────────────────────
    val sounds = listOf(
        RewardItem("sound_chime", RewardCategory.SOUND, "Chime", "\uD83D\uDD14", 0, AvatarTier.STARTER, "Gentle chime"),
        RewardItem(
            "sound_goose",
            RewardCategory.SOUND,
            "Goose Honk",
            "\uD83E\uDD9A",
            35,
            AvatarTier.COMMON,
            "Classic Canadian",
        ),
        RewardItem(
            "sound_musical",
            RewardCategory.SOUND,
            "Musical",
            "\uD83C\uDFB5",
            50,
            AvatarTier.COMMON,
            "Musical notes",
        ),
        RewardItem(
            "sound_fanfare",
            RewardCategory.SOUND,
            "Fanfare",
            "\uD83C\uDFBA",
            75,
            AvatarTier.COMMON,
            "Trumpet fanfare",
        ),
        RewardItem(
            "sound_arcade",
            RewardCategory.SOUND,
            "Arcade",
            "\uD83C\uDFAE",
            100,
            AvatarTier.RARE,
            "Retro arcade",
        ),
    )

    // ── Titles ────────────────────────────────────────────────────────────────
    val titles = listOf(
        RewardItem(
            "title_rising_star",
            RewardCategory.TITLE,
            "Rising Star",
            "\uD83C\uDF1F",
            0,
            AvatarTier.STARTER,
            "Earn 100 stars",
        ),
        RewardItem(
            "title_word_wizard",
            RewardCategory.TITLE,
            "Word Wizard",
            "\uD83D\uDCDD",
            0,
            AvatarTier.STARTER,
            "Master 25 words",
        ),
        RewardItem(
            "title_speed_demon",
            RewardCategory.TITLE,
            "Speed Demon",
            "\u26A1",
            0,
            AvatarTier.STARTER,
            "Avg response < 3s",
        ),
        RewardItem(
            "title_streak_champion",
            RewardCategory.TITLE,
            "Streak Champion",
            "\uD83D\uDD25",
            0,
            AvatarTier.STARTER,
            "7-day streak",
        ),
        RewardItem(
            "title_perfect_scholar",
            RewardCategory.TITLE,
            "Perfect Scholar",
            "\uD83C\uDFC6",
            0,
            AvatarTier.STARTER,
            "100% on 10 sessions",
        ),
        RewardItem(
            "title_star_collector",
            RewardCategory.TITLE,
            "Star Collector",
            "\uD83D\uDC8E",
            0,
            AvatarTier.STARTER,
            "Earn 5,000 stars",
        ),
        RewardItem(
            "title_polyglot",
            RewardCategory.TITLE,
            "Polyglot",
            "\uD83C\uDF0D",
            0,
            AvatarTier.STARTER,
            "Practice in 3 languages",
        ),
        RewardItem(
            "title_grand_master",
            RewardCategory.TITLE,
            "Grand Master",
            "\uD83C\uDF93",
            0,
            AvatarTier.STARTER,
            "Unlock all titles",
        ),
        RewardItem(
            "title_true_north",
            RewardCategory.TITLE,
            "True North",
            "\uD83C\uDDE8\uD83C\uDDE6",
            0,
            AvatarTier.STARTER,
            "Complete a challenge session",
        ),
        RewardItem(
            "title_mix_master",
            RewardCategory.TITLE,
            "Mix Master",
            "\uD83C\uDFA7",
            0,
            AvatarTier.STARTER,
            "Complete 5 challenge sessions",
        ),
    )

    /** All avatar-category items (hats + face + outfits + pets). */
    val avatarItems: List<RewardItem>
        get() = hats + faceAccessories + outfits + pets

    /** All purchasable items across every category. */
    val allItems: List<RewardItem>
        get() = characterItems + avatarItems + themes + effects + sounds + titles

    /** Items that come free for new players. */
    val starterItemIds = setOf(
        "char_bunny", "char_squirrel", "char_dog",
        "hat_none", "hat_tophat", "hat_party",
        "face_none", "face_shades",
        "outfit_none",
        "pet_none", "pet_chick",
        "theme_sunset",
        "effect_confetti",
        "sound_chime",
    )

    /** Convert a character body ID (e.g. "fox") to its reward item ID (e.g. "char_fox"). */
    fun characterRewardId(bodyId: String): String = "char_$bodyId"

    /** Check if a character body is owned (free or purchased). */
    fun isCharacterOwned(
        bodyId: String,
        ownedItemIds: Set<String>,
    ): Boolean = characterRewardId(bodyId) in ownedItemIds

    /** Get the RewardItem for a character by its body ID. */
    fun getCharacterItem(bodyId: String): RewardItem? =
        characterItems.firstOrNull { it.id == characterRewardId(bodyId) }

    fun getItemsByCategory(category: RewardCategory): List<RewardItem> = allItems.filter { it.category == category }

    fun getItemById(id: String): RewardItem? = allItems.firstOrNull { it.id == id }

    fun isStarterItem(itemId: String): Boolean = itemId in starterItemIds

    /** Get all items of a specific tier across all categories. */
    fun getItemsByTier(tier: AvatarTier): List<RewardItem> = allItems.filter { it.tier == tier }
}

data class CharacterBody(val id: String, val name: String, val emoji: String)
