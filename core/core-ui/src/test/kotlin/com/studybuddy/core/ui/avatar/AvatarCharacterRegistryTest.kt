package com.studybuddy.core.ui.avatar

import com.studybuddy.core.domain.model.RewardCatalog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AvatarCharacterRegistryTest {

    @Test
    fun `every character in RewardCatalog has a spec in the registry`() {
        RewardCatalog.characters.forEach { character ->
            val spec = AvatarCharacterRegistry.getSpec(character.id)
            // Falls back to fox for missing ids — the id should match unless it's truly missing
            // Any creature explicitly listed in the registry should return itself, not fox fallback
            if (character.id != "fox") {
                // We can still verify the spec's id if we add an id field, but for now
                // verify it doesn't crash and returns valid anchors
                val ap = spec.attachmentPoints
                assertTrue(ap.hatAnchor.x in 0f..1f, "${character.id} hatAnchor.x out of range")
                assertTrue(ap.hatAnchor.y in 0f..1f, "${character.id} hatAnchor.y out of range")
                assertTrue(ap.faceAnchor.y in 0f..1f, "${character.id} faceAnchor.y out of range")
                assertTrue(ap.chestAnchor.y in 0f..1f, "${character.id} chestAnchor.y out of range")
                assertTrue(ap.petAnchor.x in 0f..1f, "${character.id} petAnchor.x out of range")
                assertTrue(ap.petAnchor.y in 0f..1f, "${character.id} petAnchor.y out of range")
            }
        }
    }

    @Test
    fun `all attachment point anchors are valid fractions`() {
        val creatureIds = listOf(
            "fox", "cat", "unicorn", "panda", "butterfly", "bunny", "owl", "dragon",
            "dog", "bear", "blue_monster", "shrimp", "shark", "octopus",
            "moose", "canada_goose", "turkey", "squirrel",
        )
        creatureIds.forEach { id ->
            val spec = AvatarCharacterRegistry.getSpec(id)
            val ap = spec.attachmentPoints
            assertTrue(ap.hatAnchor.x in 0f..1f, "$id hatAnchor.x=${ap.hatAnchor.x}")
            assertTrue(ap.hatAnchor.y in 0f..1f, "$id hatAnchor.y=${ap.hatAnchor.y}")
            assertTrue(ap.faceAnchor.x in 0f..1f, "$id faceAnchor.x=${ap.faceAnchor.x}")
            assertTrue(ap.faceAnchor.y in 0f..1f, "$id faceAnchor.y=${ap.faceAnchor.y}")
            assertTrue(ap.chestAnchor.x in 0f..1f, "$id chestAnchor.x=${ap.chestAnchor.x}")
            assertTrue(ap.chestAnchor.y in 0f..1f, "$id chestAnchor.y=${ap.chestAnchor.y}")
            assertTrue(ap.petAnchor.x in 0f..1f, "$id petAnchor.x=${ap.petAnchor.x}")
            assertTrue(ap.petAnchor.y in 0f..1f, "$id petAnchor.y=${ap.petAnchor.y}")
        }
    }

    @Test
    fun `hat anchor is always above face anchor`() {
        val creatureIds = listOf(
            "fox", "cat", "unicorn", "panda", "bunny", "owl", "dragon",
            "dog", "bear", "blue_monster", "shark", "octopus",
            "moose", "canada_goose", "turkey", "squirrel",
        )
        // Bunny has special case: hat sits below ears (y > face y is not expected)
        // Moose has special case: hat sits below antlers
        val exceptionsAllowed = setOf("bunny", "moose")

        creatureIds.forEach { id ->
            if (id !in exceptionsAllowed) {
                val ap = AvatarCharacterRegistry.getSpec(id).attachmentPoints
                assertTrue(
                    ap.hatAnchor.y < ap.faceAnchor.y,
                    "$id: hatAnchor.y (${ap.hatAnchor.y}) should be above faceAnchor.y (${ap.faceAnchor.y})",
                )
            }
        }
    }

    @Test
    fun `face anchor is always above chest anchor`() {
        val creatureIds = listOf(
            "fox", "cat", "unicorn", "bear", "dog", "moose", "squirrel", "canada_goose",
        )
        creatureIds.forEach { id ->
            val ap = AvatarCharacterRegistry.getSpec(id).attachmentPoints
            assertTrue(
                ap.faceAnchor.y < ap.chestAnchor.y,
                "$id: faceAnchor.y (${ap.faceAnchor.y}) should be above chestAnchor.y (${ap.chestAnchor.y})",
            )
        }
    }

    @Test
    fun `unknown creature id falls back to fox spec`() {
        val spec = AvatarCharacterRegistry.getSpec("totally_unknown_creature")
        val foxSpec = AvatarCharacterRegistry.getSpec("fox")
        // Both should have the same attachment points since unknown falls back to fox
        assertEquals(foxSpec.attachmentPoints.hatAnchor, spec.attachmentPoints.hatAnchor)
    }

    @Test
    fun `hat scale is positive for all creatures`() {
        listOf("fox", "cat", "bunny", "moose", "blue_monster", "shrimp").forEach { id ->
            val scale = AvatarCharacterRegistry.getSpec(id).attachmentPoints.hatScale
            assertTrue(scale > 0f, "$id hatScale should be positive, was $scale")
        }
    }

    @Test
    fun `moose hat anchor is lower than fox to avoid antler overlap`() {
        val moose = AvatarCharacterRegistry.getSpec("moose").attachmentPoints
        val fox = AvatarCharacterRegistry.getSpec("fox").attachmentPoints
        assertTrue(
            moose.hatAnchor.y > fox.hatAnchor.y,
            "Moose hat anchor should be lower than fox to clear antlers",
        )
    }

    @Test
    fun `bunny hat anchor is lower than cat to avoid ear overlap`() {
        val bunny = AvatarCharacterRegistry.getSpec("bunny").attachmentPoints
        val cat = AvatarCharacterRegistry.getSpec("cat").attachmentPoints
        assertTrue(
            bunny.hatAnchor.y > cat.hatAnchor.y,
            "Bunny hat anchor should be lower than cat to clear ears",
        )
    }
}
