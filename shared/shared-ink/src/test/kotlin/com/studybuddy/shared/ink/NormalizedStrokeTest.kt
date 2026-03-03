package com.studybuddy.shared.ink

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NormalizedStrokeTest {

    @Test
    fun `normalize then denormalize round-trips correctly`() {
        val point = StrokePoint(x = 150f, y = 300f, timestamp = 1000L, pressure = 0.8f)
        val canvasWidth = 500f
        val canvasHeight = 600f

        val normalized = point.normalize(canvasWidth, canvasHeight)
        val denormalized = normalized.denormalize(canvasWidth, canvasHeight)

        assertEquals(point.x, denormalized.x, 0.01f)
        assertEquals(point.y, denormalized.y, 0.01f)
        assertEquals(point.timestamp, denormalized.timestamp)
        assertEquals(point.pressure, denormalized.pressure, 0.01f)
    }

    @Test
    fun `normalize produces fractions in 0 to 1 range`() {
        val point = StrokePoint(x = 250f, y = 400f, timestamp = 2000L)
        val normalized = point.normalize(500f, 800f)

        assertEquals(0.5f, normalized.xFraction, 0.001f)
        assertEquals(0.5f, normalized.yFraction, 0.001f)
    }

    @Test
    fun `denormalize to different canvas size rescales correctly`() {
        val point = StrokePoint(x = 100f, y = 200f, timestamp = 3000L)
        val normalized = point.normalize(400f, 800f) // 0.25, 0.25

        // Denormalize to a different (rotated) canvas
        val denormalized = normalized.denormalize(800f, 400f)

        assertEquals(200f, denormalized.x, 0.01f) // 0.25 * 800
        assertEquals(100f, denormalized.y, 0.01f) // 0.25 * 400
    }

    @Test
    fun `normalize handles zero canvas dimensions gracefully`() {
        val point = StrokePoint(x = 50f, y = 100f, timestamp = 0L)
        val normalized = point.normalize(0f, 0f)

        assertEquals(0f, normalized.xFraction)
        assertEquals(0f, normalized.yFraction)
    }

    @Test
    fun `NormalizedStrokePoint preserves pressure`() {
        val normalized = NormalizedStrokePoint(
            xFraction = 0.5f,
            yFraction = 0.5f,
            timestamp = 100L,
            pressure = 0.6f,
        )
        assertEquals(0.6f, normalized.pressure)

        val denormalized = normalized.denormalize(400f, 400f)
        assertEquals(0.6f, denormalized.pressure)
    }
}
