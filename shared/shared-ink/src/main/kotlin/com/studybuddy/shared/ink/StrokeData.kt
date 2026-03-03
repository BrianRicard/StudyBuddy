package com.studybuddy.shared.ink

import com.google.mlkit.vision.digitalink.Ink

/**
 * A stroke point with optional pressure for stylus support.
 */
data class StrokePoint(
    val x: Float,
    val y: Float,
    val timestamp: Long,
    val pressure: Float = 1f,
)

/**
 * A stroke point stored as normalized fractions of canvas dimensions.
 * Survives rotation because coordinates are resolution-independent.
 */
data class NormalizedStrokePoint(
    val xFraction: Float,
    val yFraction: Float,
    val timestamp: Long,
    val pressure: Float = 1f,
)

/**
 * Normalize a pixel-space [StrokePoint] to [0..1] fractions of the given canvas dimensions.
 */
fun StrokePoint.normalize(canvasWidth: Float, canvasHeight: Float): NormalizedStrokePoint {
    return NormalizedStrokePoint(
        xFraction = if (canvasWidth > 0f) x / canvasWidth else 0f,
        yFraction = if (canvasHeight > 0f) y / canvasHeight else 0f,
        timestamp = timestamp,
        pressure = pressure,
    )
}

/**
 * Denormalize a [NormalizedStrokePoint] back to pixel-space for the given canvas dimensions.
 */
fun NormalizedStrokePoint.denormalize(canvasWidth: Float, canvasHeight: Float): StrokePoint {
    return StrokePoint(
        x = xFraction * canvasWidth,
        y = yFraction * canvasHeight,
        timestamp = timestamp,
        pressure = pressure,
    )
}

/**
 * Build an ML Kit [Ink] from denormalized strokes (needs pixel coordinates).
 */
internal fun buildInk(
    normalizedStrokes: List<List<NormalizedStrokePoint>>,
    canvasWidth: Float,
    canvasHeight: Float,
): Ink {
    val inkBuilder = Ink.builder()
    for (stroke in normalizedStrokes) {
        val strokeBuilder = Ink.Stroke.builder()
        for (point in stroke) {
            val pixel = point.denormalize(canvasWidth, canvasHeight)
            strokeBuilder.addPoint(Ink.Point.create(pixel.x, pixel.y, pixel.timestamp))
        }
        inkBuilder.addStroke(strokeBuilder.build())
    }
    return inkBuilder.build()
}
