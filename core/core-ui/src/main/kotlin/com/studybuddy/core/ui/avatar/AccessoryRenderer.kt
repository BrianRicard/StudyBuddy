package com.studybuddy.core.ui.avatar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

// ─── Public dispatch functions ──────────────────────────────────────────────

fun DrawScope.drawHat(
    hatId: String,
    cx: Float,
    cy: Float,
    s: Float,
) {
    when (hatId) {
        "hat_tophat" -> drawTophat(cx, cy, s)
        "hat_crown" -> drawCrown(cx, cy, s)
        "hat_wizard" -> drawWizard(cx, cy, s)
        "hat_party" -> drawPartyHat(cx, cy, s)
        "hat_beret" -> drawBeret(cx, cy, s)
        "hat_flower" -> drawFlowerCrown(cx, cy, s)
        "hat_cap" -> drawCap(cx, cy, s)
        "hat_toque" -> drawToque(cx, cy, s)
        "hat_hockey_helmet" -> drawHockeyHelmet(cx, cy, s)
        "hat_maple_crown" -> drawMapleCrown(cx, cy, s)
        "hat_graduation" -> drawGraduation(cx, cy, s)
        "hat_chef" -> drawChefHat(cx, cy, s)
        "hat_hardhat" -> drawHardhat(cx, cy, s)
    }
}

fun DrawScope.drawFaceAccessory(
    faceId: String,
    cx: Float,
    cy: Float,
    s: Float,
) {
    when (faceId) {
        "face_shades" -> drawShades(cx, cy, s)
        "face_monocle" -> drawMonocle(cx, cy, s)
        "face_glasses" -> drawGlasses(cx, cy, s)
        "face_mask" -> drawTheatreMask(cx, cy, s)
        "face_star" -> drawStarMark(cx, cy, s)
        "face_maple_blush" -> drawMapleBlush(cx, cy, s)
        "face_hockey_mask" -> drawHockeyMask(cx, cy, s)
        "face_heart" -> drawHeartEyes(cx, cy, s)
        "face_clown" -> drawClownNose(cx, cy, s)
    }
}

fun DrawScope.drawOutfit(
    outfitId: String,
    cx: Float,
    cy: Float,
    s: Float,
) {
    when (outfitId) {
        "outfit_scarf" -> drawScarf(cx, cy, s)
        "outfit_bowtie" -> drawBowtie(cx, cy, s)
        "outfit_cape" -> drawCape(cx, cy, s)
        "outfit_medal" -> drawMedal(cx, cy, s)
        "outfit_necklace" -> drawNecklace(cx, cy, s)
        "outfit_hockey_jersey" -> drawHockeyJersey(cx, cy, s)
        "outfit_flannel" -> drawFlannel(cx, cy, s)
        "outfit_maple_tee" -> drawMapleTee(cx, cy, s)
        "outfit_tuxedo" -> drawTuxedo(cx, cy, s)
        "outfit_lab_coat" -> drawLabCoat(cx, cy, s)
        "outfit_raincoat" -> drawRaincoat(cx, cy, s)
        "outfit_superhero" -> drawSuperhero(cx, cy, s)
    }
}

fun DrawScope.drawPet(
    petId: String,
    cx: Float,
    cy: Float,
    s: Float,
) {
    when (petId) {
        "pet_chick" -> drawChick(cx, cy, s)
        "pet_hamster" -> drawHamster(cx, cy, s)
        "pet_fish" -> drawFish(cx, cy, s)
        "pet_snail" -> drawSnail(cx, cy, s)
        "pet_ladybug" -> drawLadybug(cx, cy, s)
        "pet_beaver" -> drawBeaver(cx, cy, s)
        "pet_loon" -> drawLoon(cx, cy, s)
        "pet_polar_bear" -> drawPolarBear(cx, cy, s)
        "pet_raccoon" -> drawRaccoon(cx, cy, s)
        "pet_maple_bug" -> drawMapleBug(cx, cy, s)
        "pet_narwhal" -> drawNarwhal(cx, cy, s)
    }
}

// ─── Shared helpers ─────────────────────────────────────────────────────────

private fun starPath(
    cx: Float,
    cy: Float,
    outerR: Float,
    innerR: Float,
): Path {
    val path = Path()
    for (i in 0 until 10) {
        val angle = Math.PI / 2.0 + i * Math.PI / 5.0
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + (r * cos(angle)).toFloat()
        val y = cy - (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun mapleLeafPath(
    cx: Float,
    cy: Float,
    s: Float,
): Path {
    val path = Path()
    path.moveTo(cx, cy - s)
    path.lineTo(cx - s * 0.15f, cy - s * 0.6f)
    path.lineTo(cx - s * 0.5f, cy - s * 0.7f)
    path.lineTo(cx - s * 0.35f, cy - s * 0.35f)
    path.lineTo(cx - s * 0.8f, cy - s * 0.25f)
    path.lineTo(cx - s * 0.45f, cy - s * 0.05f)
    path.lineTo(cx - s * 0.5f, cy + s * 0.4f)
    path.lineTo(cx - s * 0.15f, cy + s * 0.2f)
    path.lineTo(cx, cy + s * 0.6f)
    path.lineTo(cx + s * 0.15f, cy + s * 0.2f)
    path.lineTo(cx + s * 0.5f, cy + s * 0.4f)
    path.lineTo(cx + s * 0.45f, cy - s * 0.05f)
    path.lineTo(cx + s * 0.8f, cy - s * 0.25f)
    path.lineTo(cx + s * 0.35f, cy - s * 0.35f)
    path.lineTo(cx + s * 0.5f, cy - s * 0.7f)
    path.lineTo(cx + s * 0.15f, cy - s * 0.6f)
    path.close()
    return path
}

private fun heartPath(
    cx: Float,
    cy: Float,
    s: Float,
): Path {
    val path = Path()
    path.moveTo(cx, cy + s * 0.6f)
    path.cubicTo(cx - s, cy + s * 0.1f, cx - s, cy - s * 0.6f, cx, cy - s * 0.2f)
    path.cubicTo(cx + s, cy - s * 0.6f, cx + s, cy + s * 0.1f, cx, cy + s * 0.6f)
    path.close()
    return path
}

private fun crownPath(
    cx: Float,
    cy: Float,
    s: Float,
): Path {
    val path = Path()
    val baseY = cy + s * 0.5f
    val topY = cy - s * 0.5f
    path.moveTo(cx - s, baseY)
    path.lineTo(cx - s, topY + s * 0.3f)
    path.lineTo(cx - s * 0.5f, topY)
    path.lineTo(cx, topY + s * 0.3f)
    path.lineTo(cx + s * 0.5f, topY)
    path.lineTo(cx + s, topY + s * 0.3f)
    path.lineTo(cx + s, baseY)
    path.close()
    return path
}

private fun DrawScope.miniEye(
    center: Offset,
    radius: Float,
) {
    drawCircle(Color.White, radius, center)
    drawCircle(Color(0xFF1A1A1A), radius * 0.55f, center)
    drawCircle(Color.White, radius * 0.2f, center + Offset(radius * 0.2f, -radius * 0.2f))
}

private fun DrawScope.miniSmile(
    cx: Float,
    cy: Float,
    width: Float,
    strokeW: Float,
) {
    val path = Path().apply {
        moveTo(cx - width / 2, cy)
        cubicTo(cx - width / 4, cy + width * 0.4f, cx + width / 4, cy + width * 0.4f, cx + width / 2, cy)
    }
    drawPath(path, Color(0xFF1A1A1A), style = Stroke(strokeW, cap = StrokeCap.Round))
}

private fun DrawScope.accOval(
    color: Color,
    cx: Float,
    cy: Float,
    rw: Float,
    rh: Float,
) = drawOval(color, topLeft = Offset(cx - rw, cy - rh), size = Size(rw * 2, rh * 2))

// ─── Hats ───────────────────────────────────────────────────────────────────

private fun DrawScope.drawTophat(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Brim
    accOval(Color(0xFF1A1A1A), cx, cy + s * 0.55f, s * 1.1f, s * 0.2f)
    // Tall cylinder
    drawRoundRect(
        Color(0xFF1A1A1A),
        topLeft = Offset(cx - s * 0.6f, cy - s * 0.8f),
        size = Size(s * 1.2f, s * 1.3f),
        cornerRadius = CornerRadius(s * 0.15f),
    )
    // Band
    drawRect(
        Color(0xFFB71C1C),
        topLeft = Offset(cx - s * 0.6f, cy + s * 0.2f),
        size = Size(s * 1.2f, s * 0.2f),
    )
}

private fun DrawScope.drawCrown(
    cx: Float,
    cy: Float,
    s: Float,
) {
    drawPath(crownPath(cx, cy, s), Color(0xFFFFD700))
    // Jewels
    drawCircle(Color(0xFFD32F2F), s * 0.12f, Offset(cx, cy))
    drawCircle(Color(0xFF1565C0), s * 0.08f, Offset(cx - s * 0.5f, cy + s * 0.15f))
    drawCircle(Color(0xFF1565C0), s * 0.08f, Offset(cx + s * 0.5f, cy + s * 0.15f))
}

private fun DrawScope.drawWizard(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Cone
    val conePath = Path().apply {
        moveTo(cx, cy - s)
        lineTo(cx - s * 0.8f, cy + s * 0.7f)
        lineTo(cx + s * 0.8f, cy + s * 0.7f)
        close()
    }
    drawPath(conePath, Color(0xFF6A1B9A))
    // Stars
    drawPath(starPath(cx - s * 0.2f, cy, s * 0.12f, s * 0.05f), Color(0xFFFFD700))
    drawPath(starPath(cx + s * 0.15f, cy + s * 0.3f, s * 0.1f, s * 0.04f), Color(0xFFFFD700))
    drawPath(starPath(cx + s * 0.05f, cy - s * 0.3f, s * 0.08f, s * 0.03f), Color(0xFFFFF176))
    // Brim
    accOval(Color(0xFF6A1B9A), cx, cy + s * 0.7f, s * 0.9f, s * 0.15f)
}

private fun DrawScope.drawPartyHat(
    cx: Float,
    cy: Float,
    s: Float,
) {
    val conePath = Path().apply {
        moveTo(cx, cy - s)
        lineTo(cx - s * 0.7f, cy + s * 0.7f)
        lineTo(cx + s * 0.7f, cy + s * 0.7f)
        close()
    }
    drawPath(conePath, Color(0xFFE91E63))
    // Stripes
    val stripeColors = listOf(Color(0xFFFFEB3B), Color(0xFF4CAF50), Color(0xFF2196F3))
    stripeColors.forEachIndexed { i, c ->
        val y = cy - s * 0.3f + i * s * 0.35f
        val halfW = s * (0.2f + i * 0.15f)
        drawLine(c, Offset(cx - halfW, y), Offset(cx + halfW, y), s * 0.1f)
    }
    // Pom-pom
    drawCircle(Color(0xFFFFEB3B), s * 0.15f, Offset(cx, cy - s))
}

private fun DrawScope.drawBeret(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Flat tilted shape
    accOval(Color(0xFF1A1A1A), cx - s * 0.1f, cy + s * 0.1f, s * 0.9f, s * 0.45f)
    // Top nub
    drawCircle(Color(0xFF1A1A1A), s * 0.1f, Offset(cx, cy - s * 0.25f))
    // Band
    drawRect(
        Color(0xFF424242),
        topLeft = Offset(cx - s * 0.9f, cy + s * 0.35f),
        size = Size(s * 1.8f, s * 0.12f),
    )
}

private fun DrawScope.drawFlowerCrown(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Vine base
    val vinePath = Path().apply {
        moveTo(cx - s * 0.9f, cy)
        cubicTo(cx - s * 0.4f, cy - s * 0.3f, cx + s * 0.4f, cy - s * 0.3f, cx + s * 0.9f, cy)
    }
    drawPath(vinePath, Color(0xFF4CAF50), style = Stroke(s * 0.12f, cap = StrokeCap.Round))
    // Flowers
    val flowerPositions = listOf(-0.5f, 0f, 0.5f)
    val flowerColors = listOf(Color(0xFFE91E63), Color(0xFFFFEB3B), Color(0xFF9C27B0))
    flowerPositions.forEachIndexed { i, xOff ->
        val fx = cx + s * xOff
        val fy = cy - s * 0.2f
        // Petals
        for (a in 0 until 5) {
            val angle = a * 72.0 * Math.PI / 180.0
            val px = fx + (s * 0.12f * cos(angle)).toFloat()
            val py = fy + (s * 0.12f * sin(angle)).toFloat()
            drawCircle(flowerColors[i], s * 0.1f, Offset(px, py))
        }
        // Centre
        drawCircle(Color(0xFFFFEB3B), s * 0.06f, Offset(fx, fy))
    }
}

private fun DrawScope.drawCap(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Cap dome
    val capPath = Path().apply {
        moveTo(cx - s * 0.8f, cy + s * 0.2f)
        cubicTo(cx - s * 0.8f, cy - s * 0.6f, cx + s * 0.8f, cy - s * 0.6f, cx + s * 0.8f, cy + s * 0.2f)
        close()
    }
    drawPath(capPath, Color(0xFF1565C0))
    // Brim
    val brimPath = Path().apply {
        moveTo(cx - s * 0.3f, cy + s * 0.2f)
        cubicTo(cx + s * 0.5f, cy + s * 0.15f, cx + s * 1.0f, cy + s * 0.1f, cx + s * 1.2f, cy + s * 0.35f)
        lineTo(cx + s * 0.8f, cy + s * 0.4f)
        cubicTo(cx + s * 0.6f, cy + s * 0.3f, cx + s * 0.2f, cy + s * 0.3f, cx - s * 0.3f, cy + s * 0.2f)
        close()
    }
    drawPath(brimPath, Color(0xFF0D47A1))
    // Button on top
    drawCircle(Color(0xFF0D47A1), s * 0.08f, Offset(cx, cy - s * 0.45f))
}

private fun DrawScope.drawToque(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Knit body
    drawRoundRect(
        Color(0xFFD32F2F),
        topLeft = Offset(cx - s * 0.7f, cy - s * 0.3f),
        size = Size(s * 1.4f, s * 1.0f),
        cornerRadius = CornerRadius(s * 0.35f, s * 0.6f),
    )
    // Ribbing at bottom
    drawRect(
        Color(0xFFB71C1C),
        topLeft = Offset(cx - s * 0.7f, cy + s * 0.4f),
        size = Size(s * 1.4f, s * 0.3f),
    )
    // Knit lines
    for (i in 0..3) {
        val lineY = cy - s * 0.1f + i * s * 0.15f
        drawLine(
            Color(0xFFE57373),
            Offset(cx - s * 0.5f, lineY),
            Offset(cx + s * 0.5f, lineY),
            s * 0.04f,
        )
    }
    // Pom-pom
    drawCircle(Color(0xFFF5F5F5), s * 0.2f, Offset(cx, cy - s * 0.5f))
}

private fun DrawScope.drawHockeyHelmet(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Helmet dome
    val domePath = Path().apply {
        moveTo(cx - s * 0.85f, cy + s * 0.3f)
        cubicTo(cx - s * 0.85f, cy - s * 0.7f, cx + s * 0.85f, cy - s * 0.7f, cx + s * 0.85f, cy + s * 0.3f)
        close()
    }
    drawPath(domePath, Color(0xFF37474F))
    // Cage bars
    for (i in -2..2) {
        drawLine(
            Color(0xFFBDBDBD),
            Offset(cx + i * s * 0.2f, cy - s * 0.1f),
            Offset(cx + i * s * 0.15f, cy + s * 0.5f),
            s * 0.04f,
        )
    }
    // Horizontal cage bar
    drawLine(
        Color(0xFFBDBDBD),
        Offset(cx - s * 0.5f, cy + s * 0.15f),
        Offset(cx + s * 0.5f, cy + s * 0.15f),
        s * 0.04f,
    )
}

private fun DrawScope.drawMapleCrown(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Circlet
    drawCircle(
        Color(0xFF795548),
        s * 0.7f,
        Offset(cx, cy),
        style = Stroke(s * 0.08f),
    )
    // Maple leaves around the circlet
    val leafOffsets = listOf(-0.55f, 0f, 0.55f)
    leafOffsets.forEach { xOff ->
        drawPath(
            mapleLeafPath(cx + s * xOff, cy - s * 0.25f, s * 0.25f),
            Color(0xFFD32F2F),
        )
    }
}

private fun DrawScope.drawGraduation(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Mortarboard top
    val boardPath = Path().apply {
        moveTo(cx, cy - s * 0.3f)
        lineTo(cx + s, cy)
        lineTo(cx, cy + s * 0.3f)
        lineTo(cx - s, cy)
        close()
    }
    drawPath(boardPath, Color(0xFF1A1A1A))
    // Cap base
    drawRect(
        Color(0xFF1A1A1A),
        topLeft = Offset(cx - s * 0.5f, cy),
        size = Size(s, s * 0.5f),
    )
    // Button on top
    drawCircle(Color(0xFFFFD700), s * 0.08f, Offset(cx, cy))
    // Tassel
    drawLine(Color(0xFFFFD700), Offset(cx, cy), Offset(cx + s * 0.7f, cy + s * 0.5f), s * 0.06f)
    drawCircle(Color(0xFFFFD700), s * 0.06f, Offset(cx + s * 0.7f, cy + s * 0.5f))
}

private fun DrawScope.drawChefHat(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Tall puffy top
    drawCircle(Color.White, s * 0.55f, Offset(cx, cy - s * 0.3f))
    drawCircle(Color.White, s * 0.4f, Offset(cx - s * 0.3f, cy - s * 0.15f))
    drawCircle(Color.White, s * 0.4f, Offset(cx + s * 0.3f, cy - s * 0.15f))
    // Band
    drawRect(
        Color.White,
        topLeft = Offset(cx - s * 0.6f, cy + s * 0.1f),
        size = Size(s * 1.2f, s * 0.5f),
    )
    drawRect(
        Color(0xFFE0E0E0),
        topLeft = Offset(cx - s * 0.6f, cy + s * 0.4f),
        size = Size(s * 1.2f, s * 0.15f),
    )
}

private fun DrawScope.drawHardhat(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Dome
    val domePath = Path().apply {
        moveTo(cx - s * 0.9f, cy + s * 0.3f)
        cubicTo(cx - s * 0.9f, cy - s * 0.7f, cx + s * 0.9f, cy - s * 0.7f, cx + s * 0.9f, cy + s * 0.3f)
        close()
    }
    drawPath(domePath, Color(0xFFFDD835))
    // Brim
    drawRect(
        Color(0xFFF9A825),
        topLeft = Offset(cx - s, cy + s * 0.2f),
        size = Size(s * 2, s * 0.15f),
    )
    // Ridge on top
    drawLine(Color(0xFFF57F17), Offset(cx, cy - s * 0.6f), Offset(cx, cy - s * 0.1f), s * 0.08f)
}

// ─── Face ───────────────────────────────────────────────────────────────────

private fun DrawScope.drawShades(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Left lens
    accOval(Color(0xFF1A1A1A), cx - s * 0.45f, cy, s * 0.4f, s * 0.35f)
    // Right lens
    accOval(Color(0xFF1A1A1A), cx + s * 0.45f, cy, s * 0.4f, s * 0.35f)
    // Bridge
    drawLine(Color(0xFF424242), Offset(cx - s * 0.1f, cy), Offset(cx + s * 0.1f, cy), s * 0.06f)
    // Lens shine
    drawLine(
        Color(0x55FFFFFF),
        Offset(cx - s * 0.55f, cy - s * 0.15f),
        Offset(cx - s * 0.3f, cy - s * 0.1f),
        s * 0.05f,
        cap = StrokeCap.Round,
    )
    drawLine(
        Color(0x55FFFFFF),
        Offset(cx + s * 0.35f, cy - s * 0.15f),
        Offset(cx + s * 0.6f, cy - s * 0.1f),
        s * 0.05f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawMonocle(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Single circle frame
    drawCircle(Color(0xFFFFD700), s * 0.45f, Offset(cx + s * 0.2f, cy), style = Stroke(s * 0.06f))
    // Glass tint
    drawCircle(Color(0x22FFFFFF), s * 0.4f, Offset(cx + s * 0.2f, cy))
    // Chain
    val chainPath = Path().apply {
        moveTo(cx + s * 0.2f, cy + s * 0.45f)
        cubicTo(cx + s * 0.1f, cy + s * 0.7f, cx - s * 0.2f, cy + s * 0.8f, cx - s * 0.4f, cy + s * 0.9f)
    }
    drawPath(chainPath, Color(0xFFFFD700), style = Stroke(s * 0.03f))
}

private fun DrawScope.drawGlasses(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Left frame
    drawCircle(Color(0xFF424242), s * 0.35f, Offset(cx - s * 0.42f, cy), style = Stroke(s * 0.06f))
    // Right frame
    drawCircle(Color(0xFF424242), s * 0.35f, Offset(cx + s * 0.42f, cy), style = Stroke(s * 0.06f))
    // Bridge
    drawLine(Color(0xFF424242), Offset(cx - s * 0.1f, cy - s * 0.05f), Offset(cx + s * 0.1f, cy - s * 0.05f), s * 0.05f)
    // Temples
    drawLine(Color(0xFF424242), Offset(cx - s * 0.75f, cy), Offset(cx - s * 0.9f, cy - s * 0.1f), s * 0.04f)
    drawLine(Color(0xFF424242), Offset(cx + s * 0.75f, cy), Offset(cx + s * 0.9f, cy - s * 0.1f), s * 0.04f)
}

private fun DrawScope.drawTheatreMask(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Mask shape — comedy (smiling)
    val maskPath = Path().apply {
        moveTo(cx - s * 0.7f, cy - s * 0.4f)
        cubicTo(cx - s * 0.7f, cy - s * 0.8f, cx + s * 0.7f, cy - s * 0.8f, cx + s * 0.7f, cy - s * 0.4f)
        cubicTo(cx + s * 0.7f, cy + s * 0.3f, cx + s * 0.3f, cy + s * 0.7f, cx, cy + s * 0.5f)
        cubicTo(cx - s * 0.3f, cy + s * 0.7f, cx - s * 0.7f, cy + s * 0.3f, cx - s * 0.7f, cy - s * 0.4f)
    }
    drawPath(maskPath, Color(0xFFF5F5DC))
    // Eye holes
    accOval(Color(0xFF1A1A1A), cx - s * 0.3f, cy - s * 0.15f, s * 0.15f, s * 0.12f)
    accOval(Color(0xFF1A1A1A), cx + s * 0.3f, cy - s * 0.15f, s * 0.15f, s * 0.12f)
    // Smile
    miniSmile(cx, cy + s * 0.2f, s * 0.5f, s * 0.05f)
}

private fun DrawScope.drawStarMark(
    cx: Float,
    cy: Float,
    s: Float,
) {
    drawPath(starPath(cx + s * 0.35f, cy, s * 0.3f, s * 0.13f), Color(0xFFFFD700))
}

private fun DrawScope.drawMapleBlush(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Two small maple leaves on cheeks
    drawPath(mapleLeafPath(cx - s * 0.5f, cy + s * 0.1f, s * 0.2f), Color(0xFFE57373))
    drawPath(mapleLeafPath(cx + s * 0.5f, cy + s * 0.1f, s * 0.2f), Color(0xFFE57373))
}

private fun DrawScope.drawHockeyMask(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // White goalie mask shape
    val maskPath = Path().apply {
        moveTo(cx - s * 0.6f, cy - s * 0.5f)
        cubicTo(cx - s * 0.6f, cy - s * 0.8f, cx + s * 0.6f, cy - s * 0.8f, cx + s * 0.6f, cy - s * 0.5f)
        lineTo(cx + s * 0.5f, cy + s * 0.5f)
        cubicTo(cx + s * 0.3f, cy + s * 0.7f, cx - s * 0.3f, cy + s * 0.7f, cx - s * 0.5f, cy + s * 0.5f)
        close()
    }
    drawPath(maskPath, Color(0xFFF5F5F5))
    // Eye holes
    accOval(Color(0xFF1A1A1A), cx - s * 0.25f, cy - s * 0.1f, s * 0.15f, s * 0.12f)
    accOval(Color(0xFF1A1A1A), cx + s * 0.25f, cy - s * 0.1f, s * 0.15f, s * 0.12f)
    // Ventilation holes
    for (i in -1..1) {
        drawCircle(Color(0xFF424242), s * 0.04f, Offset(cx + i * s * 0.15f, cy + s * 0.3f))
    }
    // Chin
    drawLine(
        Color(0xFFBDBDBD),
        Offset(cx - s * 0.2f, cy + s * 0.45f),
        Offset(cx + s * 0.2f, cy + s * 0.45f),
        s * 0.04f,
    )
}

private fun DrawScope.drawHeartEyes(
    cx: Float,
    cy: Float,
    s: Float,
) {
    drawPath(heartPath(cx - s * 0.4f, cy, s * 0.3f), Color(0xFFE91E63))
    drawPath(heartPath(cx + s * 0.4f, cy, s * 0.3f), Color(0xFFE91E63))
}

private fun DrawScope.drawClownNose(
    cx: Float,
    cy: Float,
    s: Float,
) {
    drawCircle(Color(0xFFD32F2F), s * 0.3f, Offset(cx, cy + s * 0.1f))
    // Shine
    drawCircle(Color(0x44FFFFFF), s * 0.1f, Offset(cx - s * 0.08f, cy))
}

// ─── Outfits ────────────────────────────────────────────────────────────────

private fun DrawScope.drawScarf(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Wavy scarf around neck
    val scarfPath = Path().apply {
        moveTo(cx - s * 0.8f, cy - s * 0.2f)
        cubicTo(cx - s * 0.4f, cy + s * 0.1f, cx + s * 0.4f, cy - s * 0.1f, cx + s * 0.8f, cy + s * 0.1f)
    }
    drawPath(scarfPath, Color(0xFFD32F2F), style = Stroke(s * 0.3f, cap = StrokeCap.Round))
    // Hanging end
    drawRoundRect(
        Color(0xFFD32F2F),
        topLeft = Offset(cx + s * 0.5f, cy + s * 0.1f),
        size = Size(s * 0.25f, s * 0.7f),
        cornerRadius = CornerRadius(s * 0.08f),
    )
    // Fringe
    for (i in 0..2) {
        drawLine(
            Color(0xFFB71C1C),
            Offset(cx + s * 0.53f + i * s * 0.08f, cy + s * 0.75f),
            Offset(cx + s * 0.53f + i * s * 0.08f, cy + s * 0.9f),
            s * 0.03f,
        )
    }
}

private fun DrawScope.drawBowtie(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Left triangle
    val leftPath = Path().apply {
        moveTo(cx, cy)
        lineTo(cx - s * 0.7f, cy - s * 0.4f)
        lineTo(cx - s * 0.7f, cy + s * 0.4f)
        close()
    }
    drawPath(leftPath, Color(0xFFD32F2F))
    // Right triangle
    val rightPath = Path().apply {
        moveTo(cx, cy)
        lineTo(cx + s * 0.7f, cy - s * 0.4f)
        lineTo(cx + s * 0.7f, cy + s * 0.4f)
        close()
    }
    drawPath(rightPath, Color(0xFFD32F2F))
    // Center knot
    drawCircle(Color(0xFFB71C1C), s * 0.15f, Offset(cx, cy))
}

private fun DrawScope.drawCape(
    cx: Float,
    cy: Float,
    s: Float,
) {
    val capePath = Path().apply {
        moveTo(cx - s * 0.6f, cy - s * 0.3f)
        cubicTo(cx - s * 0.9f, cy + s * 0.5f, cx - s * 0.7f, cy + s * 1.0f, cx, cy + s * 0.8f)
        cubicTo(cx + s * 0.7f, cy + s * 1.0f, cx + s * 0.9f, cy + s * 0.5f, cx + s * 0.6f, cy - s * 0.3f)
        close()
    }
    drawPath(capePath, Color(0xFFD32F2F))
    // Collar
    accOval(Color(0xFFFFD700), cx, cy - s * 0.3f, s * 0.7f, s * 0.12f)
}

private fun DrawScope.drawMedal(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Ribbon
    val ribbonPath = Path().apply {
        moveTo(cx - s * 0.15f, cy - s * 0.6f)
        lineTo(cx, cy - s * 0.1f)
        lineTo(cx + s * 0.15f, cy - s * 0.6f)
    }
    drawPath(ribbonPath, Color(0xFF1565C0), style = Stroke(s * 0.12f))
    // Medal disc
    drawCircle(Color(0xFFFFD700), s * 0.3f, Offset(cx, cy + s * 0.15f))
    // Star on medal
    drawPath(starPath(cx, cy + s * 0.15f, s * 0.18f, s * 0.08f), Color(0xFFF57F17))
}

private fun DrawScope.drawNecklace(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // String arc
    val neckPath = Path().apply {
        moveTo(cx - s * 0.7f, cy - s * 0.3f)
        cubicTo(cx - s * 0.4f, cy + s * 0.4f, cx + s * 0.4f, cy + s * 0.4f, cx + s * 0.7f, cy - s * 0.3f)
    }
    drawPath(neckPath, Color(0xFF795548), style = Stroke(s * 0.04f))
    // Beads
    for (i in 0..6) {
        val t = i / 6.0f
        val bx = cx - s * 0.55f + t * s * 1.1f
        val by = cy - s * 0.2f + sin(t * Math.PI).toFloat() * s * 0.45f
        val beadColor = if (i % 2 == 0) Color(0xFF4CAF50) else Color(0xFFFF9800)
        drawCircle(beadColor, s * 0.08f, Offset(bx, by))
    }
}

private fun DrawScope.drawHockeyJersey(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Jersey body
    drawRoundRect(
        Color(0xFFD32F2F),
        topLeft = Offset(cx - s * 0.7f, cy - s * 0.4f),
        size = Size(s * 1.4f, s * 1.0f),
        cornerRadius = CornerRadius(s * 0.15f),
    )
    // Sleeves
    accOval(Color(0xFFD32F2F), cx - s * 0.85f, cy - s * 0.1f, s * 0.25f, s * 0.35f)
    accOval(Color(0xFFD32F2F), cx + s * 0.85f, cy - s * 0.1f, s * 0.25f, s * 0.35f)
    // Horizontal stripe
    drawRect(
        Color.White,
        topLeft = Offset(cx - s * 0.7f, cy + s * 0.1f),
        size = Size(s * 1.4f, s * 0.15f),
    )
    // Collar
    accOval(Color.White, cx, cy - s * 0.4f, s * 0.3f, s * 0.1f)
}

private fun DrawScope.drawFlannel(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Shirt body
    drawRoundRect(
        Color(0xFFD32F2F),
        topLeft = Offset(cx - s * 0.7f, cy - s * 0.4f),
        size = Size(s * 1.4f, s * 1.0f),
        cornerRadius = CornerRadius(s * 0.1f),
    )
    // Plaid pattern — vertical lines
    for (i in -3..3) {
        drawLine(
            Color(0xFF1A1A1A),
            Offset(cx + i * s * 0.2f, cy - s * 0.4f),
            Offset(cx + i * s * 0.2f, cy + s * 0.6f),
            s * 0.03f,
            alpha = 0.4f,
        )
    }
    // Horizontal lines
    for (i in -2..3) {
        drawLine(
            Color(0xFF1A1A1A),
            Offset(cx - s * 0.7f, cy - s * 0.2f + i * s * 0.2f),
            Offset(cx + s * 0.7f, cy - s * 0.2f + i * s * 0.2f),
            s * 0.03f,
            alpha = 0.4f,
        )
    }
    // Collar
    val leftCollar = Path().apply {
        moveTo(cx, cy - s * 0.4f)
        lineTo(cx - s * 0.25f, cy - s * 0.15f)
        lineTo(cx, cy - s * 0.1f)
        close()
    }
    val rightCollar = Path().apply {
        moveTo(cx, cy - s * 0.4f)
        lineTo(cx + s * 0.25f, cy - s * 0.15f)
        lineTo(cx, cy - s * 0.1f)
        close()
    }
    drawPath(leftCollar, Color(0xFFB71C1C))
    drawPath(rightCollar, Color(0xFFB71C1C))
}

private fun DrawScope.drawMapleTee(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // T-shirt body
    drawRoundRect(
        Color.White,
        topLeft = Offset(cx - s * 0.7f, cy - s * 0.3f),
        size = Size(s * 1.4f, s * 0.9f),
        cornerRadius = CornerRadius(s * 0.1f),
    )
    // Sleeves
    accOval(Color.White, cx - s * 0.8f, cy - s * 0.1f, s * 0.2f, s * 0.25f)
    accOval(Color.White, cx + s * 0.8f, cy - s * 0.1f, s * 0.2f, s * 0.25f)
    // Maple leaf on front
    drawPath(mapleLeafPath(cx, cy + s * 0.1f, s * 0.35f), Color(0xFFD32F2F))
    // Collar
    accOval(Color(0xFFE0E0E0), cx, cy - s * 0.3f, s * 0.25f, s * 0.08f)
}

private fun DrawScope.drawTuxedo(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Black jacket
    drawRoundRect(
        Color(0xFF1A1A1A),
        topLeft = Offset(cx - s * 0.7f, cy - s * 0.4f),
        size = Size(s * 1.4f, s * 1.0f),
        cornerRadius = CornerRadius(s * 0.1f),
    )
    // White shirt front (V-shape)
    val shirtPath = Path().apply {
        moveTo(cx - s * 0.25f, cy - s * 0.4f)
        lineTo(cx, cy + s * 0.2f)
        lineTo(cx + s * 0.25f, cy - s * 0.4f)
    }
    drawPath(shirtPath, Color.White)
    // Bowtie
    drawBowtie(cx, cy - s * 0.2f, s * 0.3f)
    // Lapels
    drawLine(Color(0xFF424242), Offset(cx - s * 0.25f, cy - s * 0.4f), Offset(cx - s * 0.4f, cy + s * 0.3f), s * 0.04f)
    drawLine(Color(0xFF424242), Offset(cx + s * 0.25f, cy - s * 0.4f), Offset(cx + s * 0.4f, cy + s * 0.3f), s * 0.04f)
}

private fun DrawScope.drawLabCoat(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // White coat body
    drawRoundRect(
        Color.White,
        topLeft = Offset(cx - s * 0.75f, cy - s * 0.4f),
        size = Size(s * 1.5f, s * 1.1f),
        cornerRadius = CornerRadius(s * 0.1f),
    )
    // Collar notches
    val leftCollar = Path().apply {
        moveTo(cx - s * 0.15f, cy - s * 0.4f)
        lineTo(cx - s * 0.35f, cy - s * 0.15f)
        lineTo(cx - s * 0.15f, cy - s * 0.05f)
        close()
    }
    val rightCollar = Path().apply {
        moveTo(cx + s * 0.15f, cy - s * 0.4f)
        lineTo(cx + s * 0.35f, cy - s * 0.15f)
        lineTo(cx + s * 0.15f, cy - s * 0.05f)
        close()
    }
    drawPath(leftCollar, Color(0xFFE0E0E0))
    drawPath(rightCollar, Color(0xFFE0E0E0))
    // Pocket
    drawRoundRect(
        Color(0xFFE0E0E0),
        topLeft = Offset(cx - s * 0.5f, cy + s * 0.15f),
        size = Size(s * 0.35f, s * 0.3f),
        cornerRadius = CornerRadius(s * 0.05f),
        style = Stroke(s * 0.03f),
    )
    // Buttons
    for (i in 0..2) {
        drawCircle(Color(0xFFBDBDBD), s * 0.04f, Offset(cx, cy - s * 0.15f + i * s * 0.25f))
    }
}

private fun DrawScope.drawRaincoat(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Yellow coat body
    drawRoundRect(
        Color(0xFFFDD835),
        topLeft = Offset(cx - s * 0.75f, cy - s * 0.4f),
        size = Size(s * 1.5f, s * 1.1f),
        cornerRadius = CornerRadius(s * 0.1f),
    )
    // Collar
    accOval(Color(0xFFF9A825), cx, cy - s * 0.35f, s * 0.4f, s * 0.12f)
    // Buttons (snaps)
    for (i in 0..3) {
        drawCircle(Color(0xFF795548), s * 0.05f, Offset(cx, cy - s * 0.2f + i * s * 0.2f))
    }
    // Pocket flap
    drawLine(
        Color(0xFFF9A825),
        Offset(cx - s * 0.5f, cy + s * 0.15f),
        Offset(cx - s * 0.15f, cy + s * 0.15f),
        s * 0.06f,
    )
}

private fun DrawScope.drawSuperhero(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Cape behind
    drawCape(cx, cy, s)
    // Chest emblem — shield shape
    val shieldPath = Path().apply {
        moveTo(cx, cy - s * 0.4f)
        lineTo(cx + s * 0.35f, cy - s * 0.2f)
        lineTo(cx + s * 0.3f, cy + s * 0.2f)
        lineTo(cx, cy + s * 0.4f)
        lineTo(cx - s * 0.3f, cy + s * 0.2f)
        lineTo(cx - s * 0.35f, cy - s * 0.2f)
        close()
    }
    drawPath(shieldPath, Color(0xFF1565C0))
    // Star on shield
    drawPath(starPath(cx, cy, s * 0.18f, s * 0.08f), Color(0xFFFFD700))
}

// ─── Pets ───────────────────────────────────────────────────────────────────

private fun DrawScope.drawChick(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Yellow body
    drawCircle(Color(0xFFFFEB3B), s * 0.7f, Offset(cx, cy))
    // Beak
    val beakPath = Path().apply {
        moveTo(cx + s * 0.5f, cy - s * 0.05f)
        lineTo(cx + s * 0.8f, cy + s * 0.05f)
        lineTo(cx + s * 0.5f, cy + s * 0.15f)
        close()
    }
    drawPath(beakPath, Color(0xFFFF8F00))
    // Eyes
    miniEye(Offset(cx - s * 0.15f, cy - s * 0.15f), s * 0.1f)
    miniEye(Offset(cx + s * 0.2f, cy - s * 0.15f), s * 0.1f)
    // Wing
    accOval(Color(0xFFFDD835), cx - s * 0.45f, cy + s * 0.1f, s * 0.2f, s * 0.3f)
}

private fun DrawScope.drawHamster(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Round body
    drawCircle(Color(0xFFFFCC80), s * 0.65f, Offset(cx, cy))
    // Ears
    drawCircle(Color(0xFFFFCC80), s * 0.2f, Offset(cx - s * 0.45f, cy - s * 0.45f))
    drawCircle(Color(0xFFFFCC80), s * 0.2f, Offset(cx + s * 0.45f, cy - s * 0.45f))
    drawCircle(Color(0xFFFFAB91), s * 0.12f, Offset(cx - s * 0.45f, cy - s * 0.45f))
    drawCircle(Color(0xFFFFAB91), s * 0.12f, Offset(cx + s * 0.45f, cy - s * 0.45f))
    // Cheeks
    drawCircle(Color(0xFFFFAB91), s * 0.18f, Offset(cx - s * 0.35f, cy + s * 0.1f))
    drawCircle(Color(0xFFFFAB91), s * 0.18f, Offset(cx + s * 0.35f, cy + s * 0.1f))
    // Eyes
    miniEye(Offset(cx - s * 0.2f, cy - s * 0.1f), s * 0.08f)
    miniEye(Offset(cx + s * 0.2f, cy - s * 0.1f), s * 0.08f)
    // Nose + mouth
    drawCircle(Color(0xFFE91E63), s * 0.05f, Offset(cx, cy + s * 0.08f))
    miniSmile(cx, cy + s * 0.15f, s * 0.25f, s * 0.03f)
}

private fun DrawScope.drawFish(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Body — oval
    accOval(Color(0xFF42A5F5), cx, cy, s * 0.7f, s * 0.45f)
    // Tail fin
    val tailPath = Path().apply {
        moveTo(cx - s * 0.6f, cy)
        lineTo(cx - s * 0.9f, cy - s * 0.4f)
        lineTo(cx - s * 0.9f, cy + s * 0.4f)
        close()
    }
    drawPath(tailPath, Color(0xFF1E88E5))
    // Dorsal fin
    val dorsalPath = Path().apply {
        moveTo(cx - s * 0.1f, cy - s * 0.4f)
        lineTo(cx + s * 0.1f, cy - s * 0.7f)
        lineTo(cx + s * 0.3f, cy - s * 0.4f)
        close()
    }
    drawPath(dorsalPath, Color(0xFF1E88E5))
    // Eye
    miniEye(Offset(cx + s * 0.3f, cy - s * 0.1f), s * 0.1f)
    // Mouth
    drawCircle(Color(0xFF1565C0), s * 0.06f, Offset(cx + s * 0.65f, cy + s * 0.05f))
    // Scales hint
    for (i in 0..2) {
        drawCircle(Color(0xFF90CAF9), s * 0.08f, Offset(cx - s * 0.1f + i * s * 0.2f, cy + s * 0.1f), alpha = 0.5f)
    }
}

private fun DrawScope.drawSnail(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Body (slug part)
    accOval(Color(0xFFA1887F), cx + s * 0.1f, cy + s * 0.3f, s * 0.7f, s * 0.25f)
    // Shell — spiral
    drawCircle(Color(0xFFFFB74D), s * 0.45f, Offset(cx - s * 0.1f, cy - s * 0.05f))
    drawCircle(Color(0xFFFFA726), s * 0.32f, Offset(cx - s * 0.05f, cy - s * 0.05f))
    drawCircle(Color(0xFFFF9800), s * 0.2f, Offset(cx, cy - s * 0.05f))
    drawCircle(Color(0xFFE65100), s * 0.1f, Offset(cx + s * 0.05f, cy - s * 0.05f))
    // Eye stalks
    drawLine(
        Color(0xFFA1887F),
        Offset(cx + s * 0.4f, cy + s * 0.15f),
        Offset(cx + s * 0.55f, cy - s * 0.25f),
        s * 0.05f,
    )
    drawLine(Color(0xFFA1887F), Offset(cx + s * 0.55f, cy + s * 0.15f), Offset(cx + s * 0.7f, cy - s * 0.2f), s * 0.05f)
    // Eyes
    miniEye(Offset(cx + s * 0.55f, cy - s * 0.3f), s * 0.07f)
    miniEye(Offset(cx + s * 0.7f, cy - s * 0.25f), s * 0.07f)
    miniSmile(cx + s * 0.5f, cy + s * 0.3f, s * 0.2f, s * 0.03f)
}

private fun DrawScope.drawLadybug(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Red dome body
    val domePath = Path().apply {
        moveTo(cx - s * 0.7f, cy + s * 0.15f)
        cubicTo(cx - s * 0.7f, cy - s * 0.6f, cx + s * 0.7f, cy - s * 0.6f, cx + s * 0.7f, cy + s * 0.15f)
        close()
    }
    drawPath(domePath, Color(0xFFD32F2F))
    // Center line
    drawLine(Color(0xFF1A1A1A), Offset(cx, cy - s * 0.55f), Offset(cx, cy + s * 0.15f), s * 0.04f)
    // Black spots
    drawCircle(Color(0xFF1A1A1A), s * 0.1f, Offset(cx - s * 0.3f, cy - s * 0.2f))
    drawCircle(Color(0xFF1A1A1A), s * 0.1f, Offset(cx + s * 0.3f, cy - s * 0.2f))
    drawCircle(Color(0xFF1A1A1A), s * 0.08f, Offset(cx - s * 0.15f, cy + s * 0.0f))
    drawCircle(Color(0xFF1A1A1A), s * 0.08f, Offset(cx + s * 0.15f, cy + s * 0.0f))
    // Head
    drawCircle(Color(0xFF1A1A1A), s * 0.2f, Offset(cx, cy - s * 0.55f))
    // Eyes
    miniEye(Offset(cx - s * 0.1f, cy - s * 0.6f), s * 0.06f)
    miniEye(Offset(cx + s * 0.1f, cy - s * 0.6f), s * 0.06f)
    // Antennae
    drawLine(Color(0xFF1A1A1A), Offset(cx - s * 0.1f, cy - s * 0.7f), Offset(cx - s * 0.25f, cy - s * 0.85f), s * 0.03f)
    drawLine(Color(0xFF1A1A1A), Offset(cx + s * 0.1f, cy - s * 0.7f), Offset(cx + s * 0.25f, cy - s * 0.85f), s * 0.03f)
}

private fun DrawScope.drawBeaver(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Flat tail
    accOval(Color(0xFF5D4037), cx - s * 0.5f, cy + s * 0.35f, s * 0.4f, s * 0.15f)
    // Brown body
    drawCircle(Color(0xFF795548), s * 0.55f, Offset(cx, cy))
    // Belly
    accOval(Color(0xFFD7CCC8), cx, cy + s * 0.1f, s * 0.3f, s * 0.25f)
    // Head
    drawCircle(Color(0xFF795548), s * 0.35f, Offset(cx, cy - s * 0.35f))
    // Ears
    drawCircle(Color(0xFF795548), s * 0.1f, Offset(cx - s * 0.3f, cy - s * 0.55f))
    drawCircle(Color(0xFF795548), s * 0.1f, Offset(cx + s * 0.3f, cy - s * 0.55f))
    // Eyes
    miniEye(Offset(cx - s * 0.12f, cy - s * 0.4f), s * 0.07f)
    miniEye(Offset(cx + s * 0.12f, cy - s * 0.4f), s * 0.07f)
    // Nose
    drawCircle(Color(0xFF3E2723), s * 0.06f, Offset(cx, cy - s * 0.25f))
    // Buck teeth
    drawRect(Color.White, topLeft = Offset(cx - s * 0.06f, cy - s * 0.18f), size = Size(s * 0.05f, s * 0.1f))
    drawRect(Color.White, topLeft = Offset(cx + s * 0.01f, cy - s * 0.18f), size = Size(s * 0.05f, s * 0.1f))
}

private fun DrawScope.drawLoon(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Body
    accOval(Color(0xFF1A1A1A), cx, cy + s * 0.1f, s * 0.6f, s * 0.35f)
    // White chest spots (checkerboard pattern)
    for (i in -2..2) {
        for (j in 0..1) {
            if ((i + j) % 2 == 0) {
                drawCircle(
                    Color.White,
                    s * 0.04f,
                    Offset(cx + i * s * 0.12f, cy + s * 0.05f + j * s * 0.12f),
                )
            }
        }
    }
    // Head
    drawCircle(Color(0xFF1A1A1A), s * 0.25f, Offset(cx, cy - s * 0.3f))
    // Red eye
    drawCircle(Color(0xFFD32F2F), s * 0.06f, Offset(cx + s * 0.1f, cy - s * 0.35f))
    drawCircle(Color(0xFF1A1A1A), s * 0.03f, Offset(cx + s * 0.1f, cy - s * 0.35f))
    // Beak
    val beakPath = Path().apply {
        moveTo(cx + s * 0.2f, cy - s * 0.3f)
        lineTo(cx + s * 0.55f, cy - s * 0.25f)
        lineTo(cx + s * 0.2f, cy - s * 0.2f)
        close()
    }
    drawPath(beakPath, Color(0xFF424242))
    // White neck band
    drawLine(Color.White, Offset(cx - s * 0.18f, cy - s * 0.1f), Offset(cx + s * 0.18f, cy - s * 0.1f), s * 0.05f)
}

private fun DrawScope.drawPolarBear(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // White round body
    drawCircle(Color(0xFFF5F5F5), s * 0.6f, Offset(cx, cy))
    // Head
    drawCircle(Color(0xFFF5F5F5), s * 0.4f, Offset(cx, cy - s * 0.35f))
    // Ears
    drawCircle(Color(0xFFF5F5F5), s * 0.12f, Offset(cx - s * 0.28f, cy - s * 0.58f))
    drawCircle(Color(0xFFF5F5F5), s * 0.12f, Offset(cx + s * 0.28f, cy - s * 0.58f))
    drawCircle(Color(0xFFE0E0E0), s * 0.07f, Offset(cx - s * 0.28f, cy - s * 0.58f))
    drawCircle(Color(0xFFE0E0E0), s * 0.07f, Offset(cx + s * 0.28f, cy - s * 0.58f))
    // Eyes
    miniEye(Offset(cx - s * 0.12f, cy - s * 0.4f), s * 0.06f)
    miniEye(Offset(cx + s * 0.12f, cy - s * 0.4f), s * 0.06f)
    // Nose
    drawCircle(Color(0xFF1A1A1A), s * 0.06f, Offset(cx, cy - s * 0.25f))
    miniSmile(cx, cy - s * 0.18f, s * 0.2f, s * 0.025f)
    // Paws hint
    accOval(Color(0xFFE0E0E0), cx - s * 0.3f, cy + s * 0.5f, s * 0.12f, s * 0.08f)
    accOval(Color(0xFFE0E0E0), cx + s * 0.3f, cy + s * 0.5f, s * 0.12f, s * 0.08f)
}

private fun DrawScope.drawRaccoon(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Grey body
    drawCircle(Color(0xFF757575), s * 0.55f, Offset(cx, cy))
    // Lighter belly
    accOval(Color(0xFFBDBDBD), cx, cy + s * 0.1f, s * 0.3f, s * 0.25f)
    // Head
    drawCircle(Color(0xFF757575), s * 0.35f, Offset(cx, cy - s * 0.35f))
    // Ears
    drawCircle(Color(0xFF616161), s * 0.12f, Offset(cx - s * 0.28f, cy - s * 0.55f))
    drawCircle(Color(0xFF616161), s * 0.12f, Offset(cx + s * 0.28f, cy - s * 0.55f))
    // Black mask marking
    accOval(Color(0xFF1A1A1A), cx - s * 0.15f, cy - s * 0.38f, s * 0.15f, s * 0.08f)
    accOval(Color(0xFF1A1A1A), cx + s * 0.15f, cy - s * 0.38f, s * 0.15f, s * 0.08f)
    // White muzzle
    accOval(Color(0xFFE0E0E0), cx, cy - s * 0.25f, s * 0.12f, s * 0.08f)
    // Eyes (on mask)
    miniEye(Offset(cx - s * 0.15f, cy - s * 0.38f), s * 0.06f)
    miniEye(Offset(cx + s * 0.15f, cy - s * 0.38f), s * 0.06f)
    // Nose
    drawCircle(Color(0xFF1A1A1A), s * 0.04f, Offset(cx, cy - s * 0.22f))
    // Striped tail
    val tailPath = Path().apply {
        moveTo(cx - s * 0.4f, cy + s * 0.4f)
        cubicTo(cx - s * 0.7f, cy + s * 0.2f, cx - s * 0.8f, cy - s * 0.1f, cx - s * 0.5f, cy - s * 0.2f)
    }
    drawPath(tailPath, Color(0xFF757575), style = Stroke(s * 0.15f, cap = StrokeCap.Round))
    // Tail stripes
    for (i in 0..2) {
        val t = 0.3f + i * 0.25f
        val sx = cx - s * (0.4f + t * 0.3f)
        val sy = cy + s * (0.3f - t * 0.5f)
        drawCircle(Color(0xFF1A1A1A), s * 0.06f, Offset(sx, sy))
    }
}

private fun DrawScope.drawMapleBug(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Small green body
    accOval(Color(0xFF4CAF50), cx, cy, s * 0.5f, s * 0.4f)
    // Leaf-shaped antenna
    drawLine(
        Color(0xFF388E3C),
        Offset(cx - s * 0.15f, cy - s * 0.35f),
        Offset(cx - s * 0.3f, cy - s * 0.65f),
        s * 0.04f,
    )
    drawLine(
        Color(0xFF388E3C),
        Offset(cx + s * 0.15f, cy - s * 0.35f),
        Offset(cx + s * 0.3f, cy - s * 0.65f),
        s * 0.04f,
    )
    // Tiny maple leaves at antenna tips
    drawPath(mapleLeafPath(cx - s * 0.3f, cy - s * 0.7f, s * 0.12f), Color(0xFF388E3C))
    drawPath(mapleLeafPath(cx + s * 0.3f, cy - s * 0.7f, s * 0.12f), Color(0xFF388E3C))
    // Eyes
    miniEye(Offset(cx - s * 0.12f, cy - s * 0.1f), s * 0.07f)
    miniEye(Offset(cx + s * 0.12f, cy - s * 0.1f), s * 0.07f)
    // Legs
    for (i in -1..1) {
        drawLine(
            Color(0xFF388E3C),
            Offset(cx - s * 0.4f, cy + i * s * 0.12f),
            Offset(cx - s * 0.6f, cy + i * s * 0.2f),
            s * 0.03f,
        )
        drawLine(
            Color(0xFF388E3C),
            Offset(cx + s * 0.4f, cy + i * s * 0.12f),
            Offset(cx + s * 0.6f, cy + i * s * 0.2f),
            s * 0.03f,
        )
    }
    miniSmile(cx, cy + s * 0.15f, s * 0.2f, s * 0.025f)
}

private fun DrawScope.drawNarwhal(
    cx: Float,
    cy: Float,
    s: Float,
) {
    // Blue body
    accOval(Color(0xFF42A5F5), cx, cy, s * 0.65f, s * 0.4f)
    // Belly
    accOval(Color(0xFFBBDEFB), cx, cy + s * 0.1f, s * 0.45f, s * 0.2f)
    // Horn (tusk)
    drawLine(
        Color(0xFFFFD700),
        Offset(cx + s * 0.5f, cy - s * 0.1f),
        Offset(cx + s * 1.0f, cy - s * 0.5f),
        s * 0.06f,
        cap = StrokeCap.Round,
    )
    // Spiral on horn
    for (i in 0..2) {
        val hx = cx + s * (0.6f + i * 0.12f)
        val hy = cy - s * (0.2f + i * 0.1f)
        drawCircle(Color(0xFFFFF176), s * 0.02f, Offset(hx, hy))
    }
    // Tail
    val tailPath = Path().apply {
        moveTo(cx - s * 0.6f, cy)
        lineTo(cx - s * 0.9f, cy - s * 0.3f)
        lineTo(cx - s * 0.9f, cy + s * 0.3f)
        close()
    }
    drawPath(tailPath, Color(0xFF1E88E5))
    // Eye
    miniEye(Offset(cx + s * 0.25f, cy - s * 0.1f), s * 0.08f)
    // Flipper
    accOval(Color(0xFF1E88E5), cx - s * 0.1f, cy + s * 0.3f, s * 0.15f, s * 0.08f)
    miniSmile(cx + s * 0.2f, cy + s * 0.1f, s * 0.2f, s * 0.025f)
}
