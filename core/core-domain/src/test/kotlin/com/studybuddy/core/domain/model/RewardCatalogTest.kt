package com.studybuddy.core.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RewardCatalogTest {

    @Test
    fun `characters list includes original and new Canadian creatures`() {
        val ids = RewardCatalog.characters.map { it.id }
        // Original roster
        assertTrue("fox" in ids)
        assertTrue("cat" in ids)
        assertTrue("unicorn" in ids)
        assertTrue("dragon" in ids)
        // New Canadian & marine creatures
        assertTrue("moose" in ids)
        assertTrue("canada_goose" in ids)
        assertTrue("turkey" in ids)
        assertTrue("squirrel" in ids)
        assertTrue("bear" in ids)
        assertTrue("shark" in ids)
        assertTrue("octopus" in ids)
        assertTrue("shrimp" in ids)
        assertTrue("blue_monster" in ids)
        assertTrue("dog" in ids)
    }

    @Test
    fun `all items have unique ids`() {
        val ids = RewardCatalog.allItems.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `all characters have unique ids`() {
        val ids = RewardCatalog.characters.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `getItemById returns correct item`() {
        val crown = RewardCatalog.getItemById("hat_crown")
        assertNotNull(crown)
        assertEquals("Crown", crown!!.name)
        assertEquals(RewardCategory.HAT, crown.category)
    }

    @Test
    fun `getItemById returns null for unknown id`() {
        assertNull(RewardCatalog.getItemById("nonexistent"))
    }

    @Test
    fun `getItemsByCategory returns only items of that category`() {
        val hats = RewardCatalog.getItemsByCategory(RewardCategory.HAT)
        assertTrue(hats.all { it.category == RewardCategory.HAT })
    }

    @Test
    fun `starter items are all free`() {
        RewardCatalog.starterItemIds.forEach { id ->
            val item = RewardCatalog.getItemById(id)
            if (item != null) {
                assertEquals(0, item.cost, "Starter item $id should be free but costs ${item.cost}")
            }
        }
    }

    @Test
    fun `party hat is a free starter hat`() {
        val party = RewardCatalog.getItemById("hat_party")
        assertNotNull(party)
        assertEquals(0, party!!.cost)
        assertTrue(RewardCatalog.isStarterItem("hat_party"))
    }

    @Test
    fun `isStarterItem returns true for classic starter items`() {
        assertTrue(RewardCatalog.isStarterItem("hat_tophat"))
        assertTrue(RewardCatalog.isStarterItem("face_shades"))
        assertTrue(RewardCatalog.isStarterItem("pet_chick"))
    }

    @Test
    fun `isStarterItem returns true for free starter characters`() {
        assertTrue(RewardCatalog.isStarterItem("char_bunny"))
        assertTrue(RewardCatalog.isStarterItem("char_squirrel"))
        assertTrue(RewardCatalog.isStarterItem("char_dog"))
    }

    @Test
    fun `isStarterItem returns false for premium items`() {
        assertFalse(RewardCatalog.isStarterItem("hat_crown"))
        assertFalse(RewardCatalog.isStarterItem("pet_fish"))
        assertFalse(RewardCatalog.isStarterItem("face_monocle"))
    }

    @Test
    fun `isStarterItem returns false for premium characters`() {
        assertFalse(RewardCatalog.isStarterItem("char_unicorn"))
        assertFalse(RewardCatalog.isStarterItem("char_dragon"))
        assertFalse(RewardCatalog.isStarterItem("char_fox"))
    }

    @Test
    fun `characterItems has one entry per character`() {
        assertEquals(
            RewardCatalog.characters.size,
            RewardCatalog.characterItems.size,
            "characterItems and characters lists must have the same size",
        )
    }

    @Test
    fun `getCharacterItem returns correct item for known body`() {
        val item = RewardCatalog.getCharacterItem("unicorn")
        assertNotNull(item)
        assertEquals("char_unicorn", item!!.id)
        assertEquals(RewardCategory.CHARACTER, item.category)
        assertTrue(item.cost > 0)
    }

    @Test
    fun `isCharacterOwned returns true for starter characters`() {
        assertTrue(RewardCatalog.isCharacterOwned("bunny", RewardCatalog.starterItemIds))
        assertTrue(RewardCatalog.isCharacterOwned("squirrel", RewardCatalog.starterItemIds))
        assertTrue(RewardCatalog.isCharacterOwned("dog", RewardCatalog.starterItemIds))
    }

    @Test
    fun `isCharacterOwned returns false for non-owned characters`() {
        assertFalse(RewardCatalog.isCharacterOwned("dragon", RewardCatalog.starterItemIds))
        assertFalse(RewardCatalog.isCharacterOwned("unicorn", RewardCatalog.starterItemIds))
    }

    @Test
    fun `every avatar category has at least one free none item`() {
        val avatarCategories = listOf(
            RewardCategory.HAT,
            RewardCategory.FACE,
            RewardCategory.OUTFIT,
            RewardCategory.PET,
        )
        avatarCategories.forEach { category ->
            val items = RewardCatalog.getItemsByCategory(category)
            assertTrue(
                items.any { it.cost == 0 },
                "Category $category should have at least one free item",
            )
        }
    }

    @Test
    fun `sunset theme is free`() {
        val sunset = RewardCatalog.getItemById("theme_sunset")
        assertNotNull(sunset)
        assertEquals(0, sunset!!.cost)
    }

    @Test
    fun `avatarItems contains only avatar categories`() {
        val avatarCategories = setOf(
            RewardCategory.HAT,
            RewardCategory.FACE,
            RewardCategory.OUTFIT,
            RewardCategory.PET,
        )
        RewardCatalog.avatarItems.forEach { item ->
            assertTrue(
                item.category in avatarCategories,
                "Item ${item.id} has category ${item.category} which is not an avatar category",
            )
        }
    }

    @Test
    fun `trimmed pets are in catalog`() {
        assertNotNull(RewardCatalog.getItemById("pet_chick"))
        assertNotNull(RewardCatalog.getItemById("pet_fish"))
    }

    @Test
    fun `maple theme exists`() {
        assertNotNull(RewardCatalog.getItemById("theme_maple"))
        assertEquals(RewardCategory.THEME, RewardCatalog.getItemById("theme_maple")!!.category)
    }

    @Test
    fun `challenge titles exist`() {
        assertNotNull(RewardCatalog.getItemById("title_true_north"))
        assertNotNull(RewardCatalog.getItemById("title_mix_master"))
    }

    // ── Tier pricing tests ──────────────────────────────────────────────────

    @Test
    fun `all items have a tier assigned`() {
        RewardCatalog.allItems.forEach { item ->
            assertNotNull(item.tier, "Item ${item.id} should have a tier")
        }
    }

    @Test
    fun `item costs fall within their tier range`() {
        RewardCatalog.allItems.filter { it.cost > 0 }.forEach { item ->
            val tier = item.tier
            assertTrue(
                item.cost in tier.minCost..tier.maxCost,
                "Item ${item.id} costs ${item.cost} but tier ${tier.label} range is ${tier.minCost}-${tier.maxCost}",
            )
        }
    }

    @Test
    fun `free items are starter tier`() {
        RewardCatalog.allItems.filter { it.cost == 0 }.forEach { item ->
            assertEquals(
                AvatarTier.STARTER,
                item.tier,
                "Free item ${item.id} should be STARTER tier",
            )
        }
    }

    @Test
    fun `dragon is legendary tier`() {
        val dragon = RewardCatalog.getItemById("char_dragon")
        assertNotNull(dragon)
        assertEquals(AvatarTier.LEGENDARY, dragon!!.tier)
        assertTrue(dragon.cost >= AvatarTier.LEGENDARY.minCost)
    }

    @Test
    fun `unicorn is epic tier`() {
        val unicorn = RewardCatalog.getItemById("char_unicorn")
        assertNotNull(unicorn)
        assertEquals(AvatarTier.EPIC, unicorn!!.tier)
    }

    @Test
    fun `galaxy theme is epic tier`() {
        val galaxy = RewardCatalog.getItemById("theme_galaxy")
        assertNotNull(galaxy)
        assertEquals(AvatarTier.EPIC, galaxy!!.tier)
    }

    @Test
    fun `getItemsByTier returns only items of that tier`() {
        AvatarTier.entries.forEach { tier ->
            val items = RewardCatalog.getItemsByTier(tier)
            items.forEach { item ->
                assertEquals(
                    tier,
                    item.tier,
                    "Item ${item.id} returned by getItemsByTier(${tier.label}) has wrong tier",
                )
            }
        }
    }

    @Test
    fun `each tier has at least one item`() {
        AvatarTier.entries.forEach { tier ->
            assertTrue(
                RewardCatalog.getItemsByTier(tier).isNotEmpty(),
                "Tier ${tier.label} should have at least one item",
            )
        }
    }

    // ── Upgrade stability tests ──────────────────────────────────────────

    /**
     * Verifies that all canonical item IDs remain present in the catalog.
     * If an ID is removed or renamed, existing OwnedRewardEntity rows referencing
     * that ID become orphaned (the user loses access to their purchase).
     */
    @Test
    fun `catalog preserves all canonical item IDs across versions`() {
        val expectedIds = setOf(
            // Characters
            "char_bunny", "char_squirrel", "char_dog", "char_fox", "char_cat",
            "char_unicorn", "char_panda", "char_butterfly", "char_owl", "char_dragon",
            "char_bear", "char_blue_monster", "char_shrimp", "char_shark", "char_octopus",
            "char_moose", "char_canada_goose", "char_turkey",
            // Hats
            "hat_none", "hat_tophat", "hat_party", "hat_crown", "hat_wizard",
            // Face
            "face_none", "face_shades", "face_mask", "face_monocle",
            // Outfit
            "outfit_none",
            // Pets
            "pet_none", "pet_chick", "pet_fish",
            // Themes
            "theme_sunset", "theme_ocean", "theme_forest", "theme_galaxy",
            "theme_candy", "theme_arctic", "theme_maple",
            // Effects
            "effect_confetti", "effect_stars", "effect_fireworks", "effect_rainbow",
            "effect_maple_storm", "effect_rockstar", "effect_champion",
            "effect_unicorn", "effect_dragon",
            // Sounds
            "sound_chime", "sound_goose", "sound_musical", "sound_fanfare", "sound_arcade",
            // Titles
            "title_rising_star", "title_word_wizard", "title_speed_demon",
            "title_streak_champion", "title_perfect_scholar", "title_star_collector",
            "title_polyglot", "title_grand_master", "title_true_north", "title_mix_master",
        )
        val actualIds = RewardCatalog.allItems.map { it.id }.toSet()
        val missing = expectedIds - actualIds
        assertTrue(missing.isEmpty(), "Canonical item IDs missing from catalog (would orphan purchases): $missing")
    }

    @Test
    fun `starter item IDs all resolve to catalog items`() {
        RewardCatalog.starterItemIds.forEach { id ->
            assertNotNull(
                RewardCatalog.getItemById(id),
                "Starter item '$id' is not in the catalog — onOpen backfill and runtime union would fail",
            )
        }
    }

    @Test
    fun `character reward IDs match character body IDs`() {
        RewardCatalog.characters.forEach { body ->
            val rewardId = RewardCatalog.characterRewardId(body.id)
            assertNotNull(
                RewardCatalog.getItemById(rewardId),
                "Character body '${body.id}' has no matching reward item '$rewardId'",
            )
        }
    }

    @Test
    fun `character prices increase across tiers`() {
        val chars = RewardCatalog.characterItems.filter { it.cost > 0 }
        val commonMax = chars.filter { it.tier == AvatarTier.COMMON }.maxOf { it.cost }
        val rareMin = chars.filter { it.tier == AvatarTier.RARE }.minOf { it.cost }
        val epicMin = chars.filter { it.tier == AvatarTier.EPIC }.minOf { it.cost }
        val legendaryMin = chars.filter { it.tier == AvatarTier.LEGENDARY }.minOf { it.cost }

        assertTrue(rareMin > commonMax, "Rare min ($rareMin) should exceed Common max ($commonMax)")
        assertTrue(epicMin > rareMin, "Epic min ($epicMin) should exceed Rare min ($rareMin)")
        assertTrue(legendaryMin > epicMin, "Legendary min ($legendaryMin) should exceed Epic min ($epicMin)")
    }
}
