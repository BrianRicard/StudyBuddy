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
    fun `toque is a free starter hat`() {
        val toque = RewardCatalog.getItemById("hat_toque")
        assertNotNull(toque)
        assertEquals(0, toque!!.cost)
        assertTrue(RewardCatalog.isStarterItem("hat_toque"))
    }

    @Test
    fun `isStarterItem returns true for classic starter items`() {
        assertTrue(RewardCatalog.isStarterItem("hat_tophat"))
        assertTrue(RewardCatalog.isStarterItem("face_shades"))
        assertTrue(RewardCatalog.isStarterItem("pet_chick"))
    }

    @Test
    fun `isStarterItem returns false for premium items`() {
        assertFalse(RewardCatalog.isStarterItem("hat_crown"))
        assertFalse(RewardCatalog.isStarterItem("pet_hamster"))
        assertFalse(RewardCatalog.isStarterItem("outfit_cape"))
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
    fun `new Canadian pets are in catalog`() {
        assertNotNull(RewardCatalog.getItemById("pet_beaver"))
        assertNotNull(RewardCatalog.getItemById("pet_loon"))
        assertNotNull(RewardCatalog.getItemById("pet_polar_bear"))
        assertNotNull(RewardCatalog.getItemById("pet_raccoon"))
    }

    @Test
    fun `hockey jersey exists and has correct category`() {
        val jersey = RewardCatalog.getItemById("outfit_hockey_jersey")
        assertNotNull(jersey)
        assertEquals(RewardCategory.OUTFIT, jersey!!.category)
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
}
