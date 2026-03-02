package com.studybuddy.core.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private const val PARTICLE_COUNT = 60
private val ConfettiColors = listOf(
    Color(0xFFFF6B6B),
    Color(0xFFFFD93D),
    Color(0xFF6BCB77),
    Color(0xFF4D96FF),
    Color(0xFFFF6BD6),
    Color(0xFFAB46D2),
)

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val speed: Float,
    val drift: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
)

private fun createParticles(random: Random): List<ConfettiParticle> = List(PARTICLE_COUNT) {
    ConfettiParticle(
        x = random.nextFloat(),
        startY = random.nextFloat() * -0.3f,
        width = 6f + random.nextFloat() * 8f,
        height = 4f + random.nextFloat() * 6f,
        color = ConfettiColors[random.nextInt(ConfettiColors.size)],
        speed = 0.6f + random.nextFloat() * 0.6f,
        drift = (random.nextFloat() - 0.5f) * 0.15f,
        rotationSpeed = 180f + random.nextFloat() * 360f,
        initialRotation = random.nextFloat() * 360f,
    )
}

@Composable
fun CelebrationOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    if (!visible) return

    val reducedMotion = isReducedMotionEnabled()
    val progress = remember { Animatable(0f) }
    val fadeAlpha = remember { Animatable(1f) }
    val particles = remember { createParticles(Random) }

    LaunchedEffect(visible) {
        if (reducedMotion) {
            kotlinx.coroutines.delay(1500)
            onDismiss()
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        fadeAlpha.snapTo(1f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        )
        fadeAlpha.animateTo(0f, animationSpec = tween(500))
        onDismiss()
    }

    if (reducedMotion) return

    Canvas(
        modifier = modifier.fillMaxSize(),
    ) {
        val t = progress.value
        val alpha = fadeAlpha.value

        particles.forEach { p ->
            val px = (p.x + p.drift * t) * size.width
            val py = (p.startY + p.speed * t) * size.height
            val rotation = p.initialRotation + p.rotationSpeed * t
            val wobble = sin(t * 6f + p.initialRotation) * 0.3f

            if (py > -20f && py < size.height + 20f) {
                rotate(
                    degrees = rotation + wobble * 30f,
                    pivot = Offset(px, py),
                ) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(px - p.width / 2f, py - p.height / 2f),
                        size = Size(p.width, p.height),
                    )
                }
            }
        }
    }
}
