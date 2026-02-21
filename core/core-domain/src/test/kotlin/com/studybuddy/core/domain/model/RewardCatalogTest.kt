package com.studybuddy.core.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RewardCatalogTest {

    @Test
    fun `characters list has 8 entries`() {
        assertEquals(8, RewardCatalog.characters.size)
    }

    @Test
    fun `hats list has 8 entries`() {
        assertEquals(8, RewardCatalog.hats.size)
    }

    @Test
    fun `face accessories list has 6 entries`() {
        assertEquals(6, RewardCatalog.faceAccessories.size)
    }

    @Test
    fun `outfits list has 6 entries`() {
        assertEquals(6, RewardCatalog.outfits.size)
    }

    @Test
    fun `pets list has 6 entries`() {
        assertEquals(6, RewardCatalog.pets.size)
    }

    @Test
    fun `themes list has 6 entries`() {
        assertEquals(6, RewardCatalog.themes.size)
    }

    @Test
    fun `effects list has 8 entries`() {
        assertEquals(8, RewardCatalog.effects.size)
    }

    @Test
    fun `sounds list has 4 entries`() {
        assertEquals(4, RewardCatalog.sounds.size)
    }

    @Test
    fun `titles list has 8 entries`() {
        assertEquals(8, RewardCatalog.titles.size)
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
        assertEquals(8, hats.size)
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
    fun `isStarterItem returns true for starter items`() {
        assertTrue(RewardCatalog.isStarterItem("hat_tophat"))
        assertTrue(RewardCatalog.isStarterItem("face_shades"))
        assertTrue(RewardCatalog.isStarterItem("pet_chick"))
    }

    @Test
    fun `isStarterItem returns false for non-starter items`() {
        assertFalse(RewardCatalog.isStarterItem("hat_crown"))
        assertFalse(RewardCatalog.isStarterItem("pet_hamster"))
    }

    @Test
    fun `every category has at least one free none item`() {
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
                "Item ${item.id} has category ${item.category} but should be an avatar category",
            )
        }
    }
}
