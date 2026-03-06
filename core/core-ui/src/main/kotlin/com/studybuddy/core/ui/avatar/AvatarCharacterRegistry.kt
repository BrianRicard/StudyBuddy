package com.studybuddy.core.ui.avatar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Per-creature visual specification: colours used when Canvas-drawing the creature,
 * plus the attachment point anchors for all accessory slots.
 */
data class CharacterSpec(
    val id: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val attachmentPoints: AvatarAttachmentPoints,
)

/**
 * Registry of all playable creatures.
 *
 * Attachment point design guide:
 *   - hatAnchor   → top edge of head (y ≈ 0.05–0.15 for most creatures)
 *   - faceAnchor  → eye level        (y ≈ 0.25–0.40)
 *   - chestAnchor → neck / chest     (y ≈ 0.55–0.65)
 *   - petAnchor   → bottom-right companion position
 *
 * x = 0.5 means horizontally centred; shift left/right for asymmetric poses.
 */
object AvatarCharacterRegistry {

    private val allSpecs = listOf(

        // ── Original roster ────────────────────────────────────────────────

        CharacterSpec(
            id = "fox",
            primaryColor = Color(0xFFE07B39),
            secondaryColor = Color(0xFFF5CBA7),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "cat",
            primaryColor = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFFF8BBD0),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "unicorn",
            primaryColor = Color(0xFFF8F8FF),
            secondaryColor = Color(0xFFCE93D8),
            accentColor = Color(0xFFFFD700),
            attachmentPoints = AvatarAttachmentPoints(
                // Horn tips above head — hat sits higher
                hatAnchor = Offset(0.50f, 0.02f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                // hats appear slightly smaller over the horn
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "panda",
            primaryColor = Color(0xFFF5F5F5),
            secondaryColor = Color(0xFF212121),
            accentColor = Color(0xFF424242),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.05f,
            ),
        ),

        CharacterSpec(
            id = "butterfly",
            primaryColor = Color(0xFFFF6F00),
            secondaryColor = Color(0xFFFFF176),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.10f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.78f),
                hatScale = 0.8f,
            ),
        ),

        CharacterSpec(
            id = "bunny",
            primaryColor = Color(0xFFECEFF1),
            secondaryColor = Color(0xFFF8BBD0),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                // Bunny ears are very tall — hat anchor pushed way down so it clears the ears
                hatAnchor = Offset(0.50f, 0.20f),
                faceAnchor = Offset(0.50f, 0.38f),
                chestAnchor = Offset(0.50f, 0.65f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "owl",
            primaryColor = Color(0xFF795548),
            secondaryColor = Color(0xFFFFCC02),
            accentColor = Color(0xFFFF8F00),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.80f),
            ),
        ),

        CharacterSpec(
            id = "dragon",
            primaryColor = Color(0xFF388E3C),
            secondaryColor = Color(0xFFB71C1C),
            accentColor = Color(0xFFFFD700),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatRotation = -5f,
            ),
        ),

        // ── New Canadian creatures ──────────────────────────────────────────

        CharacterSpec(
            id = "dog",
            primaryColor = Color(0xFFB5834A),
            secondaryColor = Color(0xFFE8C99A),
            accentColor = Color(0xFF6D4C41),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.08f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "bear",
            primaryColor = Color(0xFF8D6E63),
            secondaryColor = Color(0xFFD7B89C),
            accentColor = Color(0xFF4E342E),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.07f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.1f,
            ),
        ),

        CharacterSpec(
            id = "blue_monster",
            primaryColor = Color(0xFF1976D2),
            secondaryColor = Color(0xFF64B5F6),
            accentColor = Color(0xFFFFFF00),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.04f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.15f,
                faceScale = 1.2f,
            ),
        ),

        CharacterSpec(
            id = "shrimp",
            primaryColor = Color(0xFFFF8A65),
            secondaryColor = Color(0xFFFFCCBC),
            accentColor = Color(0xFFE64A19),
            attachmentPoints = AvatarAttachmentPoints(
                // Shrimp curls upward — head is centre-right
                hatAnchor = Offset(0.62f, 0.08f),
                faceAnchor = Offset(0.62f, 0.25f),
                chestAnchor = Offset(0.55f, 0.50f),
                petAnchor = Offset(0.85f, 0.82f),
                hatRotation = 15f,
                hatScale = 0.8f,
            ),
        ),

        CharacterSpec(
            id = "shark",
            primaryColor = Color(0xFF546E7A),
            secondaryColor = Color(0xFFECEFF1),
            accentColor = Color(0xFF263238),
            attachmentPoints = AvatarAttachmentPoints(
                // Dorsal fin on back — hat sits on top of the body mass
                hatAnchor = Offset(0.50f, 0.08f),
                faceAnchor = Offset(0.65f, 0.40f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.78f),
                hatScale = 0.9f,
            ),
        ),

        CharacterSpec(
            id = "octopus",
            primaryColor = Color(0xFF7B1FA2),
            secondaryColor = Color(0xFFCE93D8),
            accentColor = Color(0xFFFF6F00),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.52f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.15f,
                faceScale = 1.2f,
            ),
        ),

        CharacterSpec(
            id = "moose",
            primaryColor = Color(0xFF5D4037),
            secondaryColor = Color(0xFF8D6E63),
            accentColor = Color(0xFF3E2723),
            attachmentPoints = AvatarAttachmentPoints(
                // Moose has huge antlers — hat anchored low so it sits BELOW them
                hatAnchor = Offset(0.50f, 0.30f),
                faceAnchor = Offset(0.50f, 0.42f),
                chestAnchor = Offset(0.50f, 0.65f),
                petAnchor = Offset(0.85f, 0.85f),
                hatScale = 1.2f,
            ),
        ),

        CharacterSpec(
            id = "canada_goose",
            primaryColor = Color(0xFF795548),
            secondaryColor = Color(0xFF212121),
            accentColor = Color(0xFFF5F5F5),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.22f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "turkey",
            primaryColor = Color(0xFF6D4C41),
            secondaryColor = Color(0xFFD32F2F),
            accentColor = Color(0xFFFF8F00),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.10f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.9f,
            ),
        ),

        CharacterSpec(
            id = "squirrel",
            primaryColor = Color(0xFFA1622F),
            secondaryColor = Color(0xFFD4956A),
            accentColor = Color(0xFF6D3B1E),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.07f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        // ── Epic skins ──────────────────────────────────────────────────────

        CharacterSpec(
            id = "cyberpunk_bunny",
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFFE040FB),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.18f),
                faceAnchor = Offset(0.50f, 0.36f),
                chestAnchor = Offset(0.50f, 0.63f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "engineer_cat",
            primaryColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFF9E9E9E),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "samurai_fox",
            primaryColor = Color(0xFFE07B39),
            secondaryColor = Color(0xFFC62828),
            accentColor = Color(0xFFFFD700),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.04f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.9f,
            ),
        ),

        CharacterSpec(
            id = "pirate_panda",
            primaryColor = Color(0xFFF5F5F5),
            secondaryColor = Color(0xFF212121),
            accentColor = Color(0xFFFFD700),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.05f,
            ),
        ),

        CharacterSpec(
            id = "ninja_squirrel",
            primaryColor = Color(0xFF37474F),
            secondaryColor = Color(0xFFA1622F),
            accentColor = Color(0xFFD32F2F),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.07f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "dj_hedgehog",
            primaryColor = Color(0xFF795548),
            secondaryColor = Color(0xFFE040FB),
            accentColor = Color(0xFF00E5FF),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        // ── Legendary skins ─────────────────────────────────────────────────

        CharacterSpec(
            id = "elder_dragon",
            primaryColor = Color(0xFF6A1B9A),
            secondaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFFFF6F00),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatRotation = -5f,
            ),
        ),

        CharacterSpec(
            id = "wizard_owl",
            primaryColor = Color(0xFF4A148C),
            secondaryColor = Color(0xFFCE93D8),
            accentColor = Color(0xFF00E5FF),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.03f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.80f),
            ),
        ),

        CharacterSpec(
            id = "royal_unicorn",
            primaryColor = Color(0xFFF8F8FF),
            secondaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFFE040FB),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.02f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "robot_dog",
            primaryColor = Color(0xFF90A4AE),
            secondaryColor = Color(0xFF00E5FF),
            accentColor = Color(0xFFFF1744),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.08f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "phoenix_butterfly",
            primaryColor = Color(0xFFFF6F00),
            secondaryColor = Color(0xFFFF1744),
            accentColor = Color(0xFFFFD700),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.10f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.78f),
                hatScale = 0.8f,
            ),
        ),

        CharacterSpec(
            id = "steampunk_hamster",
            primaryColor = Color(0xFFFFB74D),
            secondaryColor = Color(0xFF8D6E63),
            accentColor = Color(0xFFBF8040),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.07f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "space_penguin",
            primaryColor = Color(0xFF263238),
            secondaryColor = Color(0xFFECEFF1),
            accentColor = Color(0xFF2196F3),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),
    )

    private val specsById: Map<String, CharacterSpec> = allSpecs.associateBy { it.id }

    fun getSpec(characterId: String): CharacterSpec = specsById[characterId] ?: specsById["fox"]!!
}
