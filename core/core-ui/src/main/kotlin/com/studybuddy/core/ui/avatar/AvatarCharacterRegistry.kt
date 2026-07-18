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

        // ── Verb Quest creatures ────────────────────────────────────────────

        CharacterSpec(
            id = "frog",
            primaryColor = Color(0xFF66BB6A),
            secondaryColor = Color(0xFFC8E6C9),
            accentColor = Color(0xFF2E7D32),
            attachmentPoints = AvatarAttachmentPoints(
                // Generated art: big eyes at the very top of the head
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.17f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "snail",
            primaryColor = Color(0xFFBCAAA4),
            secondaryColor = Color(0xFFFF8A65),
            accentColor = Color(0xFF5D4037),
            attachmentPoints = AvatarAttachmentPoints(
                // Generated art: head on the RIGHT, shell on the left.
                hatAnchor = Offset(0.72f, 0.28f),
                faceAnchor = Offset(0.70f, 0.50f),
                chestAnchor = Offset(0.65f, 0.68f),
                petAnchor = Offset(0.15f, 0.85f),
                hatScale = 0.75f,
                hatRotation = 8f,
            ),
        ),

        CharacterSpec(
            id = "ladybug",
            primaryColor = Color(0xFFE53935),
            secondaryColor = Color(0xFF212121),
            accentColor = Color(0xFFF5F5F5),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.08f),
                faceAnchor = Offset(0.50f, 0.24f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.8f,
            ),
        ),

        CharacterSpec(
            id = "lion",
            primaryColor = Color(0xFFFFB74D),
            secondaryColor = Color(0xFFE65100),
            accentColor = Color(0xFF4E342E),
            attachmentPoints = AvatarAttachmentPoints(
                // Big mane — hats sit slightly larger and higher
                hatAnchor = Offset(0.50f, 0.04f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.62f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.15f,
            ),
        ),

        // ── Generated hero roster (Flux PNG art) ────────────────────────────

        CharacterSpec(
            id = "hockey_duck",
            primaryColor = Color(0xFFFFD54F),
            secondaryColor = Color(0xFF1E5AA8),
            accentColor = Color(0xFFD32F2F),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in hockey helmet — the hat slot is not rendered
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.22f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "business_demon",
            primaryColor = Color(0xFFC62828),
            secondaryColor = Color(0xFF9E9E9E),
            accentColor = Color(0xFF1E5AA8),
            attachmentPoints = AvatarAttachmentPoints(
                // Hat sits between the little horns
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.52f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.8f,
            ),
        ),

        CharacterSpec(
            id = "hero_gecko",
            primaryColor = Color(0xFFB0BEC5),
            secondaryColor = Color(0xFFD32F2F),
            accentColor = Color(0xFF66BB6A),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in armored crest — the hat slot is not rendered
                hatAnchor = Offset(0.55f, 0.05f),
                faceAnchor = Offset(0.57f, 0.22f),
                chestAnchor = Offset(0.50f, 0.42f),
                petAnchor = Offset(0.85f, 0.82f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "atomic_tardigrade",
            primaryColor = Color(0xFF8FBCB4),
            secondaryColor = Color(0xFF9CFF57),
            accentColor = Color(0xFF2E7D32),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.04f),
                faceAnchor = Offset(0.50f, 0.22f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.05f,
            ),
        ),

        CharacterSpec(
            id = "gadget_octopus",
            primaryColor = Color(0xFFB39DDB),
            secondaryColor = Color(0xFFD7CCC8),
            accentColor = Color(0xFF5D4037),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in detective hat — the hat slot is not rendered
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.31f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.85f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "arcade_goose",
            primaryColor = Color(0xFF795548),
            secondaryColor = Color(0xFF212121),
            accentColor = Color(0xFFD32F2F),
            attachmentPoints = AvatarAttachmentPoints(
                // Head is top-RIGHT in the art; baked-in headband — no hat slot
                hatAnchor = Offset(0.60f, 0.03f),
                faceAnchor = Offset(0.60f, 0.09f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.85f),
                supportsHat = false,
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
        // ── New expanded roster (Flux-generated PNGs, batch 3) ──────────────

        CharacterSpec(
            id = "raccoon",
            primaryColor = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFF424242),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.22f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "koala",
            primaryColor = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFFECEFF1),
            accentColor = Color(0xFF212121),
            attachmentPoints = AvatarAttachmentPoints(
                // Big fluffy ears extend wide but not much taller than the head
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.24f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.9f,
            ),
        ),

        CharacterSpec(
            id = "otter",
            primaryColor = Color(0xFF795548),
            secondaryColor = Color(0xFFD7B89C),
            accentColor = Color(0xFF3E2723),
            attachmentPoints = AvatarAttachmentPoints(
                // Generated art: big round head, eyes quite high
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.15f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "sloth",
            primaryColor = Color(0xFFD7CCC8),
            secondaryColor = Color(0xFF8D6E63),
            accentColor = Color(0xFF5D4037),
            attachmentPoints = AvatarAttachmentPoints(
                // Shaggy fur fringes above the head — hat sits slightly smaller
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.9f,
            ),
        ),

        CharacterSpec(
            id = "hedgehog",
            primaryColor = Color(0xFF8D6E63),
            secondaryColor = Color(0xFFFFCCBC),
            accentColor = Color(0xFF4E342E),
            attachmentPoints = AvatarAttachmentPoints(
                // Tall spiky back — hat scaled down so it doesn't clash with spikes
                hatAnchor = Offset(0.50f, 0.08f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.50f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.8f,
            ),
        ),

        CharacterSpec(
            id = "flamingo",
            primaryColor = Color(0xFFF48FB1),
            secondaryColor = Color(0xFFE91E8C),
            accentColor = Color(0xFF212121),
            attachmentPoints = AvatarAttachmentPoints(
                // Generated art: head tilted up at the top-right, standing on one leg
                hatAnchor = Offset(0.62f, 0.04f),
                faceAnchor = Offset(0.62f, 0.10f),
                chestAnchor = Offset(0.50f, 0.38f),
                petAnchor = Offset(0.85f, 0.85f),
                hatRotation = 10f,
                hatScale = 0.75f,
            ),
        ),

        CharacterSpec(
            id = "red_panda",
            primaryColor = Color(0xFFBF5A3E),
            secondaryColor = Color(0xFFFFF3C7),
            accentColor = Color(0xFF3E2723),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.9f,
            ),
        ),

        CharacterSpec(
            id = "narwhal",
            primaryColor = Color(0xFF78909C),
            secondaryColor = Color(0xFFCFD8DC),
            accentColor = Color(0xFFFFF3C7),
            attachmentPoints = AvatarAttachmentPoints(
                // Horizontal pose, head/eyes on the left; baked-in tusk — hat slot not rendered
                hatAnchor = Offset(0.22f, 0.28f),
                faceAnchor = Offset(0.22f, 0.32f),
                chestAnchor = Offset(0.45f, 0.60f),
                petAnchor = Offset(0.85f, 0.82f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "axolotl",
            primaryColor = Color(0xFFFCE4EC),
            secondaryColor = Color(0xFFF48FB1),
            accentColor = Color(0xFF6A1B4D),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.95f,
            ),
        ),

        CharacterSpec(
            id = "peacock",
            primaryColor = Color(0xFF5C7C99),
            secondaryColor = Color(0xFF4CAF9E),
            accentColor = Color(0xFFE0708C),
            attachmentPoints = AvatarAttachmentPoints(
                // Generated art: fanned tail feathers dominate the frame — head is small
                // and centred near the top of the fan, so hats are scaled well down.
                hatAnchor = Offset(0.50f, 0.10f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.40f),
                petAnchor = Offset(0.85f, 0.85f),
                hatScale = 0.65f,
            ),
        ),

        CharacterSpec(
            id = "arctic_fox",
            primaryColor = Color(0xFFECEFF1),
            secondaryColor = Color(0xFFCFD8DC),
            accentColor = Color(0xFF1A1A1A),
            attachmentPoints = AvatarAttachmentPoints(
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
            ),
        ),

        CharacterSpec(
            id = "dolphin",
            primaryColor = Color(0xFF78909C),
            secondaryColor = Color(0xFFECEFF1),
            accentColor = Color(0xFF37474F),
            attachmentPoints = AvatarAttachmentPoints(
                // Generated art: leaping diagonal pose, head/eye at the top-right
                hatAnchor = Offset(0.62f, 0.08f),
                faceAnchor = Offset(0.68f, 0.20f),
                chestAnchor = Offset(0.45f, 0.45f),
                petAnchor = Offset(0.85f, 0.82f),
                hatRotation = -15f,
                hatScale = 0.8f,
            ),
        ),

        // ── Epic skins (batch 3) ────────────────────────────────────────────

        CharacterSpec(
            id = "astronaut_koala",
            primaryColor = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFFF5F5F5),
            accentColor = Color(0xFFB3E5FC),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in glass astronaut helmet — the hat slot is not rendered
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.22f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "chef_raccoon",
            primaryColor = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFFF5F5F5),
            accentColor = Color(0xFF6D4C41),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in tall chef hat — the hat slot is not rendered
                hatAnchor = Offset(0.50f, 0.10f),
                faceAnchor = Offset(0.50f, 0.26f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.85f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "explorer_sloth",
            primaryColor = Color(0xFFD7CCC8),
            secondaryColor = Color(0xFF9E9159),
            accentColor = Color(0xFF5D4037),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in safari hat and backpack — the hat slot is not rendered
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.55f),
                petAnchor = Offset(0.88f, 0.88f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "knight_hedgehog",
            primaryColor = Color(0xFF8D6E63),
            secondaryColor = Color(0xFFB0BEC5),
            accentColor = Color(0xFFFFCCBC),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in knight helmet and shield — the hat slot is not rendered
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.28f),
                chestAnchor = Offset(0.45f, 0.55f),
                petAnchor = Offset(0.88f, 0.85f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "pilot_narwhal",
            primaryColor = Color(0xFFCFD8DC),
            secondaryColor = Color(0xFF6D4C41),
            accentColor = Color(0xFFB3E5FC),
            attachmentPoints = AvatarAttachmentPoints(
                // Baked-in aviator goggles and pilot cap — the hat slot is not rendered
                hatAnchor = Offset(0.50f, 0.08f),
                faceAnchor = Offset(0.50f, 0.32f),
                chestAnchor = Offset(0.50f, 0.62f),
                petAnchor = Offset(0.85f, 0.88f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "artist_flamingo",
            primaryColor = Color(0xFFF48FB1),
            secondaryColor = Color(0xFFFFD54F),
            accentColor = Color(0xFF212121),
            attachmentPoints = AvatarAttachmentPoints(
                // Generated art: head at the top-left, baked-in beret — hat slot not rendered
                hatAnchor = Offset(0.38f, 0.05f),
                faceAnchor = Offset(0.38f, 0.13f),
                chestAnchor = Offset(0.45f, 0.42f),
                petAnchor = Offset(0.85f, 0.88f),
                supportsHat = false,
            ),
        ),

        CharacterSpec(
            id = "scientist_axolotl",
            primaryColor = Color(0xFFFCE4EC),
            secondaryColor = Color(0xFFF5F5F5),
            accentColor = Color(0xFF212121),
            attachmentPoints = AvatarAttachmentPoints(
                // Round glasses are baked into the face — face accessories will overlap slightly
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.58f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.9f,
            ),
        ),

        // ── Legendary creatures (batch 3, new mythics) ──────────────────────

        CharacterSpec(
            id = "crystal_stag",
            primaryColor = Color(0xFF3F51B5),
            secondaryColor = Color(0xFFB3E5FC),
            accentColor = Color(0xFFE1F5FE),
            attachmentPoints = AvatarAttachmentPoints(
                // Huge crystalline antlers — hat anchored low so it sits BELOW them
                hatAnchor = Offset(0.50f, 0.28f),
                faceAnchor = Offset(0.50f, 0.38f),
                chestAnchor = Offset(0.50f, 0.65f),
                petAnchor = Offset(0.85f, 0.85f),
                hatScale = 1.1f,
            ),
        ),

        CharacterSpec(
            id = "thunder_ram",
            primaryColor = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFFD4AF37),
            accentColor = Color(0xFF424242),
            attachmentPoints = AvatarAttachmentPoints(
                // Large curled horns frame the head — hat scaled down slightly
                hatAnchor = Offset(0.50f, 0.05f),
                faceAnchor = Offset(0.50f, 0.20f),
                chestAnchor = Offset(0.50f, 0.52f),
                petAnchor = Offset(0.85f, 0.85f),
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "moon_wolf",
            primaryColor = Color(0xFFB0BEC5),
            secondaryColor = Color(0xFFB3E5FC),
            accentColor = Color(0xFF546E7A),
            attachmentPoints = AvatarAttachmentPoints(
                // Cropped from a night-sky scene (moon/stars removed) — head sits centre-top
                hatAnchor = Offset(0.50f, 0.04f),
                faceAnchor = Offset(0.50f, 0.17f),
                chestAnchor = Offset(0.45f, 0.55f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 0.85f,
            ),
        ),

        CharacterSpec(
            id = "golden_griffin",
            primaryColor = Color(0xFFE8C468),
            secondaryColor = Color(0xFFFFF3C7),
            accentColor = Color(0xFFB08040),
            attachmentPoints = AvatarAttachmentPoints(
                // Quadruped side-profile — head/eagle face is at the top-left
                hatAnchor = Offset(0.20f, 0.05f),
                faceAnchor = Offset(0.20f, 0.15f),
                chestAnchor = Offset(0.35f, 0.42f),
                petAnchor = Offset(0.85f, 0.85f),
                hatRotation = -10f,
                hatScale = 0.7f,
            ),
        ),

        CharacterSpec(
            id = "abyss_kraken",
            primaryColor = Color(0xFF5E4B8B),
            secondaryColor = Color(0xFF7E57C2),
            accentColor = Color(0xFF64FFDA),
            attachmentPoints = AvatarAttachmentPoints(
                // Wide curling tentacles like the octopus — hat/face scaled up slightly
                hatAnchor = Offset(0.50f, 0.06f),
                faceAnchor = Offset(0.50f, 0.30f),
                chestAnchor = Offset(0.50f, 0.52f),
                petAnchor = Offset(0.85f, 0.82f),
                hatScale = 1.1f,
                faceScale = 1.1f,
            ),
        ),

        CharacterSpec(
            id = "mythic_pegasus",
            primaryColor = Color(0xFFF8F8FF),
            secondaryColor = Color(0xFFCE93D8),
            accentColor = Color(0xFFFFD700),
            attachmentPoints = AvatarAttachmentPoints(
                // Side-profile pose, head at the top-left with flowing mane
                hatAnchor = Offset(0.14f, 0.08f),
                faceAnchor = Offset(0.14f, 0.20f),
                chestAnchor = Offset(0.35f, 0.45f),
                petAnchor = Offset(0.85f, 0.85f),
                hatRotation = -15f,
                hatScale = 0.7f,
            ),
        ),
    )

    private val specsById: Map<String, CharacterSpec> = allSpecs.associateBy { it.id }

    fun getSpec(characterId: String): CharacterSpec = specsById[characterId] ?: specsById["fox"]!!
}
