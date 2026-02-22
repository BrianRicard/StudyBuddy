package com.studybuddy.core.ui.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Public entry point ──────────────────────────────────────────────────────

@Composable
fun CreatureCanvas(
    spec: CharacterSpec,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        drawCreature(spec)
    }
}

fun DrawScope.drawCreature(spec: CharacterSpec) {
    when (spec.id) {
        "fox" -> drawFox(spec)
        "cat" -> drawCat(spec)
        "unicorn" -> drawUnicorn(spec)
        "panda" -> drawPanda(spec)
        "butterfly" -> drawButterfly(spec)
        "bunny" -> drawBunny(spec)
        "owl" -> drawOwl(spec)
        "dragon" -> drawDragon(spec)
        "dog" -> drawDog(spec)
        "bear" -> drawBear(spec)
        "blue_monster" -> drawBlueMonster(spec)
        "shrimp" -> drawShrimp(spec)
        "shark" -> drawShark(spec)
        "octopus" -> drawOctopus(spec)
        "moose" -> drawMoose(spec)
        "canada_goose" -> drawCanadaGoose(spec)
        "turkey" -> drawTurkey(spec)
        "squirrel" -> drawSquirrel(spec)
        else -> drawFox(spec)
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

private fun DrawScope.eye(
    center: Offset,
    radius: Float,
) {
    drawCircle(Color.White, radius, center)
    drawCircle(Color(0xFF1A1A1A), radius * 0.55f, center)
    drawCircle(Color.White, radius * 0.18f, center + Offset(radius * 0.25f, -radius * 0.25f))
}

private fun DrawScope.smile(
    color: Color,
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    strokeWidth: Float,
) {
    val path = Path().apply {
        moveTo(centerX - width / 2, centerY)
        cubicTo(
            centerX - width / 4,
            centerY + height,
            centerX + width / 4,
            centerY + height,
            centerX + width / 2,
            centerY,
        )
    }
    drawPath(path, color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.blush(
    center: Offset,
    radius: Float,
) {
    drawCircle(Color(0xFFFFB3C1), radius, center, alpha = 0.55f)
}

private fun DrawScope.tri(
    a: Offset,
    b: Offset,
    c: Offset,
    color: Color,
) {
    drawPath(
        Path().apply {
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            close()
        },
        color,
    )
}

private fun DrawScope.oval(
    color: Color,
    cx: Float,
    cy: Float,
    rw: Float,
    rh: Float,
) = drawOval(color, topLeft = Offset(cx - rw, cy - rh), size = Size(rw * 2, rh * 2))

// ─── Fox ──────────────────────────────────────────────────────────────────────

private fun DrawScope.drawFox(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Tail
    val tailPath = Path().apply {
        moveTo(w * 0.22f, h * 0.90f)
        cubicTo(w * -0.05f, h * 0.78f, w * -0.05f, h * 0.55f, w * 0.20f, h * 0.60f)
    }
    drawPath(tailPath, c1, style = Stroke(w * 0.12f, cap = StrokeCap.Round))
    // Tail white tip
    drawCircle(c2, w * 0.08f, Offset(w * 0.20f, h * 0.60f))

    // Body
    oval(c1, w * 0.50f, h * 0.68f, w * 0.28f, h * 0.24f)
    // Chest patch
    oval(c2, w * 0.50f, h * 0.72f, w * 0.16f, h * 0.16f)

    // Head
    drawCircle(c1, w * 0.23f, Offset(w * 0.50f, h * 0.30f))

    // Ears
    tri(Offset(w * 0.30f, h * 0.12f), Offset(w * 0.22f, h * 0.30f), Offset(w * 0.40f, h * 0.25f), c1)
    tri(Offset(w * 0.70f, h * 0.12f), Offset(w * 0.60f, h * 0.25f), Offset(w * 0.78f, h * 0.30f), c1)
    // Inner ears
    tri(Offset(w * 0.30f, h * 0.15f), Offset(w * 0.25f, h * 0.28f), Offset(w * 0.38f, h * 0.24f), c2)
    tri(Offset(w * 0.70f, h * 0.15f), Offset(w * 0.62f, h * 0.24f), Offset(w * 0.75f, h * 0.28f), c2)

    // Muzzle
    oval(c2, w * 0.50f, h * 0.38f, w * 0.14f, h * 0.09f)

    // Eyes
    eye(Offset(w * 0.38f, h * 0.27f), w * 0.06f)
    eye(Offset(w * 0.62f, h * 0.27f), w * 0.06f)

    // Nose
    drawCircle(Color(0xFF1A1A1A), w * 0.04f, Offset(w * 0.50f, h * 0.36f))

    // Mouth
    smile(Color(0xFF1A1A1A), w * 0.50f, h * 0.39f, w * 0.10f, h * 0.04f, w * 0.018f)

    // Blush
    blush(Offset(w * 0.30f, h * 0.35f), w * 0.06f)
    blush(Offset(w * 0.70f, h * 0.35f), w * 0.06f)
}

// ─── Cat ──────────────────────────────────────────────────────────────────────

private fun DrawScope.drawCat(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Tail — curls up on right
    val tailPath = Path().apply {
        moveTo(w * 0.78f, h * 0.90f)
        cubicTo(w * 1.05f, h * 0.82f, w * 1.05f, h * 0.55f, w * 0.80f, h * 0.58f)
    }
    drawPath(tailPath, c1, style = Stroke(w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Body
    oval(c1, w * 0.50f, h * 0.68f, w * 0.27f, h * 0.24f)
    oval(c2, w * 0.50f, h * 0.73f, w * 0.14f, h * 0.14f)

    // Head
    drawCircle(c1, w * 0.23f, Offset(w * 0.50f, h * 0.30f))

    // Ears — pointed cat ears
    tri(Offset(w * 0.30f, h * 0.09f), Offset(w * 0.22f, h * 0.28f), Offset(w * 0.38f, h * 0.24f), c1)
    tri(Offset(w * 0.70f, h * 0.09f), Offset(w * 0.62f, h * 0.24f), Offset(w * 0.78f, h * 0.28f), c1)
    tri(Offset(w * 0.30f, h * 0.13f), Offset(w * 0.25f, h * 0.26f), Offset(w * 0.36f, h * 0.23f), c2)
    tri(Offset(w * 0.70f, h * 0.13f), Offset(w * 0.64f, h * 0.23f), Offset(w * 0.75f, h * 0.26f), c2)

    // Eyes
    eye(Offset(w * 0.38f, h * 0.28f), w * 0.065f)
    eye(Offset(w * 0.62f, h * 0.28f), w * 0.065f)

    // Nose
    tri(Offset(w * 0.50f, h * 0.36f), Offset(w * 0.44f, h * 0.41f), Offset(w * 0.56f, h * 0.41f), c2)

    // Whiskers
    for (sign in listOf(-1f, 1f)) {
        val wy = if (sign < 0) h * 0.39f else h * 0.43f
        drawLine(Color(0xFF777777), Offset(w * 0.08f, wy), Offset(w * 0.42f, h * 0.40f), w * 0.012f)
        drawLine(Color(0xFF777777), Offset(w * 0.58f, h * 0.40f), Offset(w * 0.92f, wy), w * 0.012f)
    }

    // Mouth
    smile(Color(0xFF888888), w * 0.50f, h * 0.40f, w * 0.10f, h * 0.04f, w * 0.015f)

    blush(Offset(w * 0.30f, h * 0.34f), w * 0.055f)
    blush(Offset(w * 0.70f, h * 0.34f), w * 0.055f)
}

// ─── Unicorn ──────────────────────────────────────────────────────────────────

private fun DrawScope.drawUnicorn(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor
    val c3 = spec.accentColor

    // Body
    oval(c1, w * 0.50f, h * 0.68f, w * 0.28f, h * 0.24f)

    // Mane — colourful stripes along neck
    val maneColors = listOf(Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFF4CAF50), Color(0xFF2196F3))
    maneColors.forEachIndexed { i, mc ->
        val cx = w * (0.30f - i * 0.03f)
        oval(mc, cx, h * 0.46f, w * 0.045f, h * 0.14f)
    }

    // Head
    drawCircle(c1, w * 0.22f, Offset(w * 0.50f, h * 0.32f))

    // Horn
    val hornPath = Path().apply {
        moveTo(w * 0.50f, h * 0.05f)
        lineTo(w * 0.44f, h * 0.18f)
        lineTo(w * 0.56f, h * 0.18f)
        close()
    }
    drawPath(hornPath, c3)
    // Horn stripes
    for (i in 1..3) {
        val y = h * (0.07f + i * 0.03f)
        drawLine(Color(0xFFFFD700), Offset(w * (0.50f - 0.015f * i), y), Offset(w * (0.50f + 0.015f * i), y), w * 0.01f)
    }

    // Ears
    tri(Offset(w * 0.31f, h * 0.14f), Offset(w * 0.24f, h * 0.30f), Offset(w * 0.38f, h * 0.26f), c1)
    tri(Offset(w * 0.69f, h * 0.14f), Offset(w * 0.62f, h * 0.26f), Offset(w * 0.76f, h * 0.30f), c1)
    tri(Offset(w * 0.31f, h * 0.17f), Offset(w * 0.27f, h * 0.28f), Offset(w * 0.36f, h * 0.25f), c2)
    tri(Offset(w * 0.69f, h * 0.17f), Offset(w * 0.64f, h * 0.25f), Offset(w * 0.73f, h * 0.28f), c2)

    eye(Offset(w * 0.39f, h * 0.30f), w * 0.065f)
    eye(Offset(w * 0.62f, h * 0.30f), w * 0.065f)

    drawCircle(Color(0xFFFF9BAA), w * 0.03f, Offset(w * 0.50f, h * 0.38f))
    smile(Color(0xFFFF9BAA), w * 0.50f, h * 0.40f, w * 0.10f, h * 0.04f, w * 0.015f)
    blush(Offset(w * 0.30f, h * 0.36f), w * 0.06f)
    blush(Offset(w * 0.70f, h * 0.36f), w * 0.06f)
}

// ─── Panda ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawPanda(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    oval(c1, w * 0.50f, h * 0.68f, w * 0.28f, h * 0.26f)
    // Arms
    oval(c2, w * 0.20f, h * 0.72f, w * 0.10f, h * 0.14f)
    oval(c2, w * 0.80f, h * 0.72f, w * 0.10f, h * 0.14f)

    drawCircle(c1, w * 0.24f, Offset(w * 0.50f, h * 0.30f))

    // Ears — round black
    drawCircle(c2, w * 0.09f, Offset(w * 0.30f, h * 0.10f))
    drawCircle(c2, w * 0.09f, Offset(w * 0.70f, h * 0.10f))

    // Eye patches — black blobs
    oval(c2, w * 0.37f, h * 0.28f, w * 0.09f, h * 0.07f)
    oval(c2, w * 0.63f, h * 0.28f, w * 0.09f, h * 0.07f)

    eye(Offset(w * 0.37f, h * 0.28f), w * 0.055f)
    eye(Offset(w * 0.63f, h * 0.28f), w * 0.055f)

    oval(Color(0xFFE0E0E0), w * 0.50f, h * 0.38f, w * 0.12f, h * 0.08f)
    drawCircle(c2, w * 0.04f, Offset(w * 0.50f, h * 0.36f))
    smile(Color(0xFF757575), w * 0.50f, h * 0.41f, w * 0.10f, h * 0.04f, w * 0.015f)
}

// ─── Butterfly ────────────────────────────────────────────────────────────────

private fun DrawScope.drawButterfly(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Upper wings
    oval(c1, w * 0.22f, h * 0.38f, w * 0.22f, h * 0.28f)
    oval(c1, w * 0.78f, h * 0.38f, w * 0.22f, h * 0.28f)
    // Wing patterns
    oval(c2, w * 0.22f, h * 0.38f, w * 0.12f, h * 0.16f)
    oval(c2, w * 0.78f, h * 0.38f, w * 0.12f, h * 0.16f)
    // Lower wings
    oval(c1, w * 0.25f, h * 0.65f, w * 0.16f, h * 0.18f)
    oval(c1, w * 0.75f, h * 0.65f, w * 0.16f, h * 0.18f)

    // Body
    oval(Color(0xFF4A148C), w * 0.50f, h * 0.55f, w * 0.06f, h * 0.25f)

    // Head
    drawCircle(Color(0xFF4A148C), w * 0.12f, Offset(w * 0.50f, h * 0.25f))

    // Antennae
    drawLine(Color(0xFF1A1A1A), Offset(w * 0.46f, h * 0.16f), Offset(w * 0.35f, h * 0.05f), w * 0.018f)
    drawLine(Color(0xFF1A1A1A), Offset(w * 0.54f, h * 0.16f), Offset(w * 0.65f, h * 0.05f), w * 0.018f)
    drawCircle(Color(0xFF1A1A1A), w * 0.025f, Offset(w * 0.35f, h * 0.05f))
    drawCircle(Color(0xFF1A1A1A), w * 0.025f, Offset(w * 0.65f, h * 0.05f))

    eye(Offset(w * 0.43f, h * 0.24f), w * 0.05f)
    eye(Offset(w * 0.57f, h * 0.24f), w * 0.05f)
    smile(Color(0xFFEF9A9A), w * 0.50f, h * 0.30f, w * 0.08f, h * 0.03f, w * 0.015f)
}

// ─── Bunny ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawBunny(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Long ears
    oval(c1, w * 0.35f, h * 0.14f, w * 0.08f, h * 0.18f)
    oval(c1, w * 0.65f, h * 0.14f, w * 0.08f, h * 0.18f)
    oval(c2, w * 0.35f, h * 0.14f, w * 0.04f, h * 0.13f)
    oval(c2, w * 0.65f, h * 0.14f, w * 0.04f, h * 0.13f)

    // Body
    oval(c1, w * 0.50f, h * 0.70f, w * 0.26f, h * 0.23f)

    // Head
    drawCircle(c1, w * 0.20f, Offset(w * 0.50f, h * 0.44f))

    // Fluffy tail
    drawCircle(c2, w * 0.08f, Offset(w * 0.76f, h * 0.80f))

    eye(Offset(w * 0.41f, h * 0.40f), w * 0.06f)
    eye(Offset(w * 0.59f, h * 0.40f), w * 0.06f)

    // Nose — pink dot
    drawCircle(c2, w * 0.03f, Offset(w * 0.50f, h * 0.47f))

    // Whiskers
    drawLine(Color(0xFFBBBBBB), Offset(w * 0.10f, h * 0.46f), Offset(w * 0.44f, h * 0.47f), w * 0.010f)
    drawLine(Color(0xFFBBBBBB), Offset(w * 0.56f, h * 0.47f), Offset(w * 0.90f, h * 0.46f), w * 0.010f)

    smile(Color(0xFFFF8A80), w * 0.50f, h * 0.49f, w * 0.09f, h * 0.03f, w * 0.014f)
    blush(Offset(w * 0.33f, h * 0.46f), w * 0.05f)
    blush(Offset(w * 0.67f, h * 0.46f), w * 0.05f)
}

// ─── Owl ──────────────────────────────────────────────────────────────────────

private fun DrawScope.drawOwl(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor
    val c3 = spec.accentColor

    // Body
    oval(c1, w * 0.50f, h * 0.65f, w * 0.26f, h * 0.28f)
    // Tummy
    oval(Color(0xFFEFEBE9), w * 0.50f, h * 0.70f, w * 0.16f, h * 0.20f)
    // Wing stripes
    for (i in 0..2) {
        drawLine(
            Color(0xFF4E342E),
            Offset(w * 0.25f, h * (0.55f + i * 0.08f)),
            Offset(w * 0.42f, h * (0.55f + i * 0.08f)),
            w * 0.02f,
        )
        drawLine(
            Color(0xFF4E342E),
            Offset(w * 0.58f, h * (0.55f + i * 0.08f)),
            Offset(w * 0.75f, h * (0.55f + i * 0.08f)),
            w * 0.02f,
        )
    }

    // Head — wide, round
    drawCircle(c1, w * 0.25f, Offset(w * 0.50f, h * 0.30f))

    // Ear tufts
    tri(Offset(w * 0.36f, h * 0.09f), Offset(w * 0.30f, h * 0.22f), Offset(w * 0.43f, h * 0.20f), c1)
    tri(Offset(w * 0.64f, h * 0.09f), Offset(w * 0.57f, h * 0.20f), Offset(w * 0.70f, h * 0.22f), c1)

    // Large owl eyes
    drawCircle(Color(0xFFFFF9C4), w * 0.10f, Offset(w * 0.37f, h * 0.28f))
    drawCircle(Color(0xFFFFF9C4), w * 0.10f, Offset(w * 0.63f, h * 0.28f))
    drawCircle(c3, w * 0.07f, Offset(w * 0.37f, h * 0.28f))
    drawCircle(c3, w * 0.07f, Offset(w * 0.63f, h * 0.28f))
    drawCircle(Color(0xFF1A1A1A), w * 0.045f, Offset(w * 0.37f, h * 0.28f))
    drawCircle(Color(0xFF1A1A1A), w * 0.045f, Offset(w * 0.63f, h * 0.28f))
    drawCircle(Color.White, w * 0.014f, Offset(w * 0.39f, h * 0.26f))
    drawCircle(Color.White, w * 0.014f, Offset(w * 0.65f, h * 0.26f))

    // Beak
    tri(Offset(w * 0.50f, h * 0.35f), Offset(w * 0.44f, h * 0.41f), Offset(w * 0.56f, h * 0.41f), c3)

    // Feet
    for (x in listOf(0.40f, 0.60f)) {
        drawLine(c3, Offset(w * x, h * 0.92f), Offset(w * (x - 0.06f), h * 0.98f), w * 0.025f)
        drawLine(c3, Offset(w * x, h * 0.92f), Offset(w * x, h * 0.98f), w * 0.025f)
        drawLine(c3, Offset(w * x, h * 0.92f), Offset(w * (x + 0.06f), h * 0.98f), w * 0.025f)
    }
}

// ─── Dragon ───────────────────────────────────────────────────────────────────

private fun DrawScope.drawDragon(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Wings — behind body
    val leftWing = Path().apply {
        moveTo(w * 0.35f, h * 0.45f)
        cubicTo(w * -0.05f, h * 0.25f, w * 0.00f, h * 0.60f, w * 0.22f, h * 0.65f)
        close()
    }
    val rightWing = Path().apply {
        moveTo(w * 0.65f, h * 0.45f)
        cubicTo(w * 1.05f, h * 0.25f, w * 1.00f, h * 0.60f, w * 0.78f, h * 0.65f)
        close()
    }
    drawPath(leftWing, c2)
    drawPath(rightWing, c2)

    // Body
    oval(c1, w * 0.50f, h * 0.65f, w * 0.26f, h * 0.26f)
    // Belly scales
    for (i in 0..3) {
        oval(Color(0xFF81C784), w * 0.50f, h * (0.60f + i * 0.06f), w * (0.14f - i * 0.02f), h * 0.04f)
    }

    // Tail
    val tailPath = Path().apply {
        moveTo(w * 0.78f, h * 0.88f)
        cubicTo(w * 1.05f, h * 0.88f, w * 1.02f, h * 0.70f, w * 0.82f, h * 0.68f)
    }
    drawPath(tailPath, c1, style = Stroke(w * 0.10f, cap = StrokeCap.Round))
    // Tail spike
    tri(Offset(w * 0.82f, h * 0.65f), Offset(w * 0.92f, h * 0.58f), Offset(w * 0.88f, h * 0.68f), c2)

    // Head
    drawRoundRect(
        c1,
        topLeft = Offset(w * 0.30f, h * 0.18f),
        size = Size(w * 0.40f, h * 0.28f),
        cornerRadius = CornerRadius(w * 0.10f),
    )

    // Horns
    tri(Offset(w * 0.38f, h * 0.14f), Offset(w * 0.34f, h * 0.24f), Offset(w * 0.44f, h * 0.24f), c2)
    tri(Offset(w * 0.62f, h * 0.14f), Offset(w * 0.56f, h * 0.24f), Offset(w * 0.66f, h * 0.24f), c2)

    // Nostrils
    drawCircle(Color(0xFF2E7D32), w * 0.025f, Offset(w * 0.44f, h * 0.41f))
    drawCircle(Color(0xFF2E7D32), w * 0.025f, Offset(w * 0.56f, h * 0.41f))

    eye(Offset(w * 0.40f, h * 0.28f), w * 0.065f)
    eye(Offset(w * 0.60f, h * 0.28f), w * 0.065f)

    // Fire breath hint
    val firePath = Path().apply {
        moveTo(w * 0.50f, h * 0.44f)
        cubicTo(w * 0.35f, h * 0.55f, w * 0.40f, h * 0.65f, w * 0.25f, h * 0.72f)
    }
    drawPath(firePath, Color(0xFFFF6F00), style = Stroke(w * 0.04f, cap = StrokeCap.Round))
    drawPath(firePath, Color(0xFFFFD600), style = Stroke(w * 0.02f, cap = StrokeCap.Round))
}

// ─── Dog ──────────────────────────────────────────────────────────────────────

private fun DrawScope.drawDog(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Tail wagging right
    val tailPath = Path().apply {
        moveTo(w * 0.78f, h * 0.85f)
        cubicTo(w * 1.02f, h * 0.75f, w * 0.98f, h * 0.60f, w * 0.80f, h * 0.62f)
    }
    drawPath(tailPath, c1, style = Stroke(w * 0.09f, cap = StrokeCap.Round))

    // Body
    oval(c1, w * 0.50f, h * 0.68f, w * 0.27f, h * 0.24f)
    oval(c2, w * 0.50f, h * 0.74f, w * 0.15f, h * 0.14f)

    // Floppy ears
    oval(c1, w * 0.22f, h * 0.35f, w * 0.10f, h * 0.16f)
    oval(c1, w * 0.78f, h * 0.35f, w * 0.10f, h * 0.16f)

    // Head
    drawCircle(c1, w * 0.22f, Offset(w * 0.50f, h * 0.30f))

    // Muzzle
    oval(c2, w * 0.50f, h * 0.38f, w * 0.14f, h * 0.09f)

    eye(Offset(w * 0.39f, h * 0.26f), w * 0.065f)
    eye(Offset(w * 0.61f, h * 0.26f), w * 0.065f)

    // Nose
    oval(Color(0xFF1A1A1A), w * 0.50f, h * 0.35f, w * 0.05f, h * 0.03f)

    // Tongue
    oval(Color(0xFFE91E63), w * 0.50f, h * 0.44f, w * 0.05f, h * 0.04f)

    smile(Color(0xFF9E9E9E), w * 0.50f, h * 0.40f, w * 0.10f, h * 0.04f, w * 0.015f)
    blush(Offset(w * 0.30f, h * 0.34f), w * 0.055f)
    blush(Offset(w * 0.70f, h * 0.34f), w * 0.055f)
}

// ─── Bear ─────────────────────────────────────────────────────────────────────

private fun DrawScope.drawBear(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Body — large and round
    oval(c1, w * 0.50f, h * 0.68f, w * 0.30f, h * 0.26f)
    oval(c2, w * 0.50f, h * 0.73f, w * 0.17f, h * 0.16f)

    // Head
    drawCircle(c1, w * 0.25f, Offset(w * 0.50f, h * 0.30f))

    // Round ears
    drawCircle(c1, w * 0.09f, Offset(w * 0.28f, h * 0.10f))
    drawCircle(c1, w * 0.09f, Offset(w * 0.72f, h * 0.10f))
    drawCircle(c2, w * 0.06f, Offset(w * 0.28f, h * 0.10f))
    drawCircle(c2, w * 0.06f, Offset(w * 0.72f, h * 0.10f))

    // Muzzle
    oval(c2, w * 0.50f, h * 0.38f, w * 0.15f, h * 0.10f)

    eye(Offset(w * 0.38f, h * 0.27f), w * 0.065f)
    eye(Offset(w * 0.62f, h * 0.27f), w * 0.065f)

    oval(Color(0xFF4E342E), w * 0.50f, h * 0.36f, w * 0.06f, h * 0.04f)
    smile(Color(0xFF6D4C41), w * 0.50f, h * 0.41f, w * 0.10f, h * 0.04f, w * 0.016f)
    blush(Offset(w * 0.31f, h * 0.36f), w * 0.055f)
    blush(Offset(w * 0.69f, h * 0.36f), w * 0.055f)

    // Honey pot accessory hint
    drawRoundRect(
        Color(0xFFFFB300),
        topLeft = Offset(w * 0.72f, h * 0.78f),
        size = Size(w * 0.14f, h * 0.12f),
        cornerRadius = CornerRadius(w * 0.03f),
    )
    drawLine(Color(0xFF795548), Offset(w * 0.72f, h * 0.80f), Offset(w * 0.86f, h * 0.80f), w * 0.02f)
}

// ─── Blue Monster ────────────────────────────────────────────────────────────

private fun DrawScope.drawBlueMonster(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Fuzzy body — draw many small circles around edge for fur effect
    for (i in 0..11) {
        val angle = i * (360f / 12) * (Math.PI / 180f).toFloat()
        drawCircle(
            c1,
            w * 0.07f,
            Offset(
                w * 0.50f + w * 0.28f * kotlin.math.cos(angle.toDouble()).toFloat(),
                h * 0.68f + h * 0.24f * kotlin.math.sin(angle.toDouble()).toFloat(),
            ),
        )
    }
    oval(c1, w * 0.50f, h * 0.68f, w * 0.28f, h * 0.24f)

    // Head — round with fur bumps
    for (i in 0..7) {
        val angle = i * (360f / 8) * (Math.PI / 180f).toFloat()
        drawCircle(
            c1,
            w * 0.06f,
            Offset(
                w * 0.50f + w * 0.24f * kotlin.math.cos(angle.toDouble()).toFloat(),
                h * 0.30f + h * 0.20f * kotlin.math.sin(angle.toDouble()).toFloat(),
            ),
        )
    }
    oval(c1, w * 0.50f, h * 0.30f, w * 0.24f, h * 0.20f)

    // Googly eyes — large white with iris
    drawCircle(Color.White, w * 0.10f, Offset(w * 0.36f, h * 0.26f))
    drawCircle(Color.White, w * 0.10f, Offset(w * 0.64f, h * 0.26f))
    drawCircle(Color(0xFF1565C0), w * 0.07f, Offset(w * 0.37f, h * 0.27f))
    drawCircle(Color(0xFF1565C0), w * 0.07f, Offset(w * 0.65f, h * 0.27f))
    drawCircle(Color(0xFF0D1A26), w * 0.045f, Offset(w * 0.37f, h * 0.27f))
    drawCircle(Color(0xFF0D1A26), w * 0.045f, Offset(w * 0.65f, h * 0.27f))
    drawCircle(Color.White, w * 0.015f, Offset(w * 0.39f, h * 0.25f))
    drawCircle(Color.White, w * 0.015f, Offset(w * 0.67f, h * 0.25f))

    // Silly smile with teeth
    smile(Color(0xFF1A1A1A), w * 0.50f, h * 0.36f, w * 0.16f, h * 0.06f, w * 0.018f)
    // Teeth
    for (i in -1..1) {
        drawRect(Color.White, topLeft = Offset(w * (0.46f + i * 0.06f), h * 0.37f), size = Size(w * 0.04f, h * 0.04f))
    }

    // Claws — bottom
    for (x in listOf(0.30f, 0.50f, 0.70f)) {
        tri(
            Offset(w * (x - 0.04f), h * 0.92f),
            Offset(w * x, h * 0.98f),
            Offset(w * (x + 0.04f), h * 0.92f),
            c2,
        )
    }
}

// ─── Shrimp ───────────────────────────────────────────────────────────────────

private fun DrawScope.drawShrimp(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Segmented curved body
    val segments = 7
    for (i in 0 until segments) {
        val t = i.toFloat() / (segments - 1)
        val cx = w * (0.65f - t * 0.35f)
        val cy = h * (0.20f + t * 0.65f)
        val segW = w * (0.18f - t * 0.06f)
        val segH = h * 0.06f
        oval(c1, cx, cy, segW, segH)
        if (i % 2 == 0) oval(c2, cx, cy, segW * 0.6f, segH * 0.6f)
    }

    // Tail fan
    for (angle in listOf(-30f, -10f, 10f, 30f)) {
        val radians = (angle * Math.PI / 180.0).toFloat()
        val tx = w * 0.30f + w * 0.14f * kotlin.math.cos(radians.toDouble()).toFloat()
        val ty = h * 0.85f + h * 0.10f * kotlin.math.sin(radians.toDouble()).toFloat()
        drawLine(c1, Offset(w * 0.30f, h * 0.85f), Offset(tx, ty), w * 0.04f, cap = StrokeCap.Round)
    }

    // Head / rostrum
    drawCircle(c1, w * 0.12f, Offset(w * 0.65f, h * 0.20f))
    // Rostrum spike
    drawLine(c1, Offset(w * 0.65f, h * 0.10f), Offset(w * 0.80f, h * 0.05f), w * 0.025f, cap = StrokeCap.Round)

    // Antennae
    drawLine(Color(0xFFBF360C), Offset(w * 0.70f, h * 0.12f), Offset(w * 0.95f, h * 0.02f), w * 0.012f)
    drawLine(Color(0xFFBF360C), Offset(w * 0.62f, h * 0.12f), Offset(w * 0.90f, h * 0.08f), w * 0.012f)

    // Legs — 5 pairs on underside
    for (i in 0..4) {
        val lx = w * (0.55f - i * 0.08f)
        val ly = h * (0.30f + i * 0.08f)
        drawLine(c2, Offset(lx, ly), Offset(lx - w * 0.08f, ly + h * 0.08f), w * 0.015f)
    }

    eye(Offset(w * 0.68f, h * 0.18f), w * 0.04f)
}

// ─── Shark ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawShark(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Tail fin
    tri(Offset(w * 0.12f, h * 0.55f), Offset(w * 0.05f, h * 0.40f), Offset(w * 0.05f, h * 0.70f), c1)

    // Body — torpedo shape
    val bodyPath = Path().apply {
        moveTo(w * 0.12f, h * 0.55f)
        cubicTo(w * 0.20f, h * 0.38f, w * 0.70f, h * 0.35f, w * 0.90f, h * 0.50f)
        cubicTo(w * 0.70f, h * 0.72f, w * 0.20f, h * 0.72f, w * 0.12f, h * 0.55f)
    }
    drawPath(bodyPath, c1)

    // White belly
    val bellyPath = Path().apply {
        moveTo(w * 0.20f, h * 0.55f)
        cubicTo(w * 0.30f, h * 0.46f, w * 0.68f, h * 0.44f, w * 0.85f, h * 0.50f)
        cubicTo(w * 0.68f, h * 0.62f, w * 0.30f, h * 0.62f, w * 0.20f, h * 0.55f)
    }
    drawPath(bellyPath, c2)

    // Dorsal fin
    val dorsalPath = Path().apply {
        moveTo(w * 0.45f, h * 0.36f)
        lineTo(w * 0.55f, h * 0.12f)
        lineTo(w * 0.65f, h * 0.36f)
        close()
    }
    drawPath(dorsalPath, c1)

    // Pectoral fins
    tri(Offset(w * 0.50f, h * 0.50f), Offset(w * 0.40f, h * 0.70f), Offset(w * 0.62f, h * 0.68f), c1)

    // Friendly smile & eye
    eye(Offset(w * 0.78f, h * 0.44f), w * 0.055f)
    smile(Color(0xFF1A1A1A), w * 0.82f, h * 0.50f, w * 0.12f, h * 0.05f, w * 0.018f)

    // Teeth — peeking out
    for (i in 0..2) {
        tri(
            Offset(w * (0.74f + i * 0.05f), h * 0.51f),
            Offset(w * (0.76f + i * 0.05f), h * 0.56f),
            Offset(w * (0.78f + i * 0.05f), h * 0.51f),
            Color.White,
        )
    }

    // Gill lines
    for (i in 0..2) {
        val gx = w * (0.60f + i * 0.06f)
        drawLine(Color(0xFF455A64), Offset(gx, h * 0.42f), Offset(gx, h * 0.62f), w * 0.015f)
    }
}

// ─── Octopus ─────────────────────────────────────────────────────────────────

private fun DrawScope.drawOctopus(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // 8 tentacles
    for (i in 0..7) {
        val angle = (i * 45f - 90f) * (Math.PI / 180f).toFloat()
        val endX = w * 0.50f + w * 0.45f * kotlin.math.cos(angle.toDouble()).toFloat()
        val endY = h * 0.65f + h * 0.38f * kotlin.math.sin(angle.toDouble()).toFloat()
        val ctrlX = w * 0.50f + w * 0.30f * kotlin.math.cos(angle.toDouble()).toFloat()
        val ctrlY = h * 0.65f + h * 0.22f * kotlin.math.sin(angle.toDouble()).toFloat()
        val tentaclePath = Path().apply {
            moveTo(w * 0.50f, h * 0.65f)
            quadraticTo(ctrlX + w * 0.05f * (if (i % 2 == 0) 1 else -1), ctrlY, endX, endY)
        }
        drawPath(tentaclePath, c1, style = Stroke(w * 0.07f, cap = StrokeCap.Round))
        drawPath(tentaclePath, c2, style = Stroke(w * 0.03f, cap = StrokeCap.Round))

        // Sucker dots along tentacle
        drawCircle(c2, w * 0.018f, Offset((w * 0.50f + ctrlX) / 2, (h * 0.65f + ctrlY) / 2))
    }

    // Mantle (body/head)
    val mantlePath = Path().apply {
        moveTo(w * 0.50f, h * 0.06f)
        cubicTo(w * 0.85f, h * 0.06f, w * 0.90f, h * 0.40f, w * 0.80f, h * 0.55f)
        cubicTo(w * 0.65f, h * 0.68f, w * 0.35f, h * 0.68f, w * 0.20f, h * 0.55f)
        cubicTo(w * 0.10f, h * 0.40f, w * 0.15f, h * 0.06f, w * 0.50f, h * 0.06f)
        close()
    }
    drawPath(mantlePath, c1)

    // Mantle spots
    for (spot in listOf(Offset(0.42f, 0.20f), Offset(0.60f, 0.28f), Offset(0.38f, 0.40f))) {
        drawCircle(c2, w * 0.04f, Offset(w * spot.x, h * spot.y), alpha = 0.6f)
    }

    eye(Offset(w * 0.36f, h * 0.35f), w * 0.085f)
    eye(Offset(w * 0.64f, h * 0.35f), w * 0.085f)

    smile(Color(0xFF6A1B9A), w * 0.50f, h * 0.48f, w * 0.14f, h * 0.06f, w * 0.018f)
    blush(Offset(w * 0.26f, h * 0.42f), w * 0.06f)
    blush(Offset(w * 0.74f, h * 0.42f), w * 0.06f)
}

// ─── Moose ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawMoose(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Antlers — wide palmate
    fun antlerSide(flipX: Boolean) {
        val sign = if (flipX) -1f else 1f
        val bx = w * 0.50f + sign * w * 0.18f
        val by = h * 0.10f
        // Main beam
        drawLine(c2, Offset(bx, by + h * 0.12f), Offset(bx + sign * w * 0.20f, by - h * 0.04f), w * 0.025f)
        // Tines
        for (i in 0..2) {
            val tx = bx + sign * w * (0.06f + i * 0.08f)
            val ty = by - h * 0.01f + i * h * 0.03f
            drawLine(c2, Offset(tx, ty + h * 0.10f), Offset(tx + sign * w * 0.04f, ty), w * 0.018f)
        }
        // Brow tine
        drawLine(
            c2,
            Offset(bx + sign * w * 0.04f, by + h * 0.08f),
            Offset(bx + sign * w * 0.12f, by + h * 0.03f),
            w * 0.018f,
        )
    }
    antlerSide(flipX = false)
    antlerSide(flipX = true)

    // Body
    oval(c1, w * 0.50f, h * 0.68f, w * 0.28f, h * 0.24f)

    // Long neck
    oval(c1, w * 0.50f, h * 0.48f, w * 0.12f, h * 0.14f)

    // Head — elongated
    drawRoundRect(
        c1,
        topLeft = Offset(w * 0.34f, h * 0.25f),
        size = Size(w * 0.32f, h * 0.24f),
        cornerRadius = CornerRadius(w * 0.10f),
    )

    // Big moose nose
    oval(c2, w * 0.50f, h * 0.44f, w * 0.14f, h * 0.07f)
    drawCircle(Color(0xFF3E2723), w * 0.025f, Offset(w * 0.44f, h * 0.43f))
    drawCircle(Color(0xFF3E2723), w * 0.025f, Offset(w * 0.56f, h * 0.43f))

    eye(Offset(w * 0.38f, h * 0.32f), w * 0.06f)
    eye(Offset(w * 0.62f, h * 0.32f), w * 0.06f)

    smile(Color(0xFF6D4C41), w * 0.50f, h * 0.47f, w * 0.10f, h * 0.03f, w * 0.015f)
    blush(Offset(w * 0.30f, h * 0.36f), w * 0.05f)
    blush(Offset(w * 0.70f, h * 0.36f), w * 0.05f)

    // Maple leaf mark on shoulder
    drawCircle(Color(0xFFD32F2F), w * 0.04f, Offset(w * 0.65f, h * 0.60f), alpha = 0.7f)
}

// ─── Canada Goose ────────────────────────────────────────────────────────────

private fun DrawScope.drawCanadaGoose(spec: CharacterSpec) {
    val w = size.width
    val h = size.height

    // Body — warm brown
    oval(Color(0xFF795548), w * 0.50f, h * 0.68f, w * 0.28f, h * 0.22f)
    // Wing texture
    for (i in 0..2) {
        oval(Color(0xFF5D4037), w * 0.50f, h * (0.62f + i * 0.06f), w * (0.22f - i * 0.04f), h * 0.03f)
    }

    // Neck — long elegant black
    oval(Color(0xFF212121), w * 0.50f, h * 0.46f, w * 0.09f, h * 0.18f)

    // Head — round black
    drawCircle(Color(0xFF212121), w * 0.14f, Offset(w * 0.50f, h * 0.24f))

    // Distinctive white chin strap
    val strapPath = Path().apply {
        moveTo(w * 0.36f, h * 0.28f)
        cubicTo(w * 0.30f, h * 0.34f, w * 0.35f, h * 0.40f, w * 0.42f, h * 0.40f)
        cubicTo(w * 0.55f, h * 0.40f, w * 0.68f, h * 0.34f, w * 0.64f, h * 0.28f)
    }
    drawPath(strapPath, Color.White, style = Stroke(w * 0.07f, cap = StrokeCap.Round))

    // Orange beak
    tri(Offset(w * 0.50f, h * 0.24f), Offset(w * 0.44f, h * 0.18f), Offset(w * 0.56f, h * 0.18f), Color(0xFFFF8F00))
    drawLine(Color(0xFF1A1A1A), Offset(w * 0.45f, h * 0.21f), Offset(w * 0.55f, h * 0.21f), w * 0.012f)

    eye(Offset(w * 0.44f, h * 0.22f), w * 0.04f)

    // Tail — upward pointing
    tri(Offset(w * 0.50f, h * 0.88f), Offset(w * 0.40f, h * 0.92f), Offset(w * 0.60f, h * 0.92f), Color(0xFF4E342E))

    // Webbed feet
    for (fx in listOf(0.38f, 0.62f)) {
        tri(
            Offset(w * fx, h * 0.92f),
            Offset(w * (fx - 0.07f), h * 0.97f),
            Offset(w * (fx + 0.07f), h * 0.97f),
            Color(0xFFFF8F00),
        )
    }
}

// ─── Turkey ───────────────────────────────────────────────────────────────────

private fun DrawScope.drawTurkey(spec: CharacterSpec) {
    val w = size.width
    val h = size.height

    // Fan tail — colourful feathers fanning behind
    val tailColors = listOf(
        Color(0xFFD32F2F),
        Color(0xFFFF8F00),
        Color(0xFFF9A825),
        Color(0xFF388E3C),
        Color(0xFF1565C0),
        Color(0xFF6A1B9A),
    )
    tailColors.forEachIndexed { i, tc ->
        val angle = (-60f + i * 24f) * (Math.PI / 180.0).toFloat()
        val ex = w * 0.50f + w * 0.35f * kotlin.math.cos(angle.toDouble()).toFloat()
        val ey = h * 0.55f - h * 0.35f * kotlin.math.sin(angle.toDouble()).toFloat()
        val tailPath = Path().apply {
            moveTo(w * 0.50f, h * 0.55f)
            quadraticTo(w * 0.50f, h * 0.30f, ex, ey)
        }
        drawPath(tailPath, tc, style = Stroke(w * 0.07f, cap = StrokeCap.Round))
        drawCircle(tc, w * 0.04f, Offset(ex, ey))
    }

    // Plump body
    oval(Color(0xFF6D4C41), w * 0.50f, h * 0.70f, w * 0.26f, h * 0.23f)
    oval(Color(0xFF8D6E63), w * 0.50f, h * 0.75f, w * 0.15f, h * 0.14f)

    // Head — small atop thick neck
    oval(Color(0xFF6D4C41), w * 0.50f, h * 0.45f, w * 0.09f, h * 0.12f)
    drawCircle(Color(0xFF5D4037), w * 0.12f, Offset(w * 0.50f, h * 0.30f))

    // Wattle — red drooping blob
    val wattlePath = Path().apply {
        moveTo(w * 0.50f, h * 0.34f)
        cubicTo(w * 0.56f, h * 0.38f, w * 0.58f, h * 0.44f, w * 0.52f, h * 0.47f)
        cubicTo(w * 0.46f, h * 0.44f, w * 0.48f, h * 0.38f, w * 0.50f, h * 0.34f)
    }
    drawPath(wattlePath, Color(0xFFB71C1C))

    // Snood — blue warty bump above beak
    drawCircle(Color(0xFF1565C0), w * 0.035f, Offset(w * 0.50f, h * 0.22f))

    // Beak
    tri(Offset(w * 0.50f, h * 0.28f), Offset(w * 0.44f, h * 0.24f), Offset(w * 0.56f, h * 0.24f), Color(0xFFFF8F00))

    eye(Offset(w * 0.42f, h * 0.28f), w * 0.04f)

    // Feet
    for (fx in listOf(0.38f, 0.62f)) {
        for (toe in listOf(-0.05f, 0f, 0.05f)) {
            drawLine(
                Color(0xFFFF8F00),
                Offset(w * fx, h * 0.92f),
                Offset(w * (fx + toe), h * 0.98f),
                w * 0.02f,
                cap = StrokeCap.Round,
            )
        }
    }
}

// ─── Squirrel ─────────────────────────────────────────────────────────────────

private fun DrawScope.drawSquirrel(spec: CharacterSpec) {
    val w = size.width
    val h = size.height
    val c1 = spec.primaryColor
    val c2 = spec.secondaryColor

    // Large fluffy tail — signature squirrel feature
    val tailPath = Path().apply {
        moveTo(w * 0.72f, h * 0.85f)
        cubicTo(w * 1.10f, h * 0.70f, w * 1.00f, h * 0.20f, w * 0.60f, h * 0.30f)
        cubicTo(w * 0.55f, h * 0.28f, w * 0.58f, h * 0.40f, w * 0.62f, h * 0.45f)
    }
    drawPath(tailPath, c2, style = Stroke(w * 0.18f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(tailPath, c1, style = Stroke(w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Body
    oval(c1, w * 0.44f, h * 0.70f, w * 0.24f, h * 0.22f)
    oval(Color(0xFFFFCC80), w * 0.44f, h * 0.74f, w * 0.13f, h * 0.13f)

    // Head
    drawCircle(c1, w * 0.20f, Offset(w * 0.44f, h * 0.34f))

    // Ears
    drawCircle(c1, w * 0.07f, Offset(w * 0.32f, h * 0.17f))
    drawCircle(c1, w * 0.07f, Offset(w * 0.56f, h * 0.17f))
    drawCircle(c2, w * 0.045f, Offset(w * 0.32f, h * 0.17f))
    drawCircle(c2, w * 0.045f, Offset(w * 0.56f, h * 0.17f))

    // Chubby cheeks — holding acorn
    oval(Color(0xFFFFCC80), w * 0.28f, h * 0.38f, w * 0.08f, h * 0.07f)
    oval(Color(0xFFFFCC80), w * 0.60f, h * 0.38f, w * 0.08f, h * 0.07f)

    eye(Offset(w * 0.36f, h * 0.30f), w * 0.06f)
    eye(Offset(w * 0.52f, h * 0.30f), w * 0.06f)

    // Nose
    drawCircle(Color(0xFF4E342E), w * 0.028f, Offset(w * 0.44f, h * 0.37f))
    smile(Color(0xFF8D6E63), w * 0.44f, h * 0.39f, w * 0.09f, h * 0.03f, w * 0.014f)

    // Acorn in hands
    drawCircle(Color(0xFF6D4C41), w * 0.05f, Offset(w * 0.30f, h * 0.74f))
    drawRoundRect(
        Color(0xFFFF8F00),
        topLeft = Offset(w * 0.26f, h * 0.76f),
        size = Size(w * 0.09f, h * 0.08f),
        cornerRadius = CornerRadius(w * 0.04f),
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun FoxPreview() {
    CreatureCanvas(AvatarCharacterRegistry.getSpec("fox"), size = 130.dp)
}

@Preview(showBackground = true)
@Composable
private fun MoosePreview() {
    CreatureCanvas(AvatarCharacterRegistry.getSpec("moose"), size = 130.dp)
}

@Preview(showBackground = true)
@Composable
private fun CanadaGoosePreview() {
    CreatureCanvas(AvatarCharacterRegistry.getSpec("canada_goose"), size = 130.dp)
}

@Preview(showBackground = true)
@Composable
private fun OctopusPreview() {
    CreatureCanvas(AvatarCharacterRegistry.getSpec("octopus"), size = 130.dp)
}
