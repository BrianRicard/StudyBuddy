package com.studybuddy.shared.ink

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.digitalink.Ink

/**
 * Guide line styles mimicking notebook/cahier paper.
 */
enum class GuideLineStyle {
    /** 3 lines: top dashed, middle dashed, baseline solid — like French cahier paper. */
    NOTEBOOK,

    /** Just a baseline — simpler for older kids. */
    SINGLE_LINE,

    /** No guide lines. */
    NONE,
}

// Shared color tokens for the handwriting canvas
private val HandwritingStrokeColor = Color(0xFF1A237E)
private val HandwritingGuideColor = Color(0xFFD0D0D0)
private val HandwritingCanvasBg = Color(0xFFFFF8E1)
private val CorrectTextColor = Color(0xFF2E7D32)
private val StylusHoverColor = Color(0xFF1A237E)

// Base stroke width for finger input
private const val BASE_STROKE_WIDTH_DP = 5f

/**
 * Enhanced handwriting canvas with notebook guide lines, smooth Bezier strokes,
 * undo support, live recognition preview, stylus support with pressure sensitivity,
 * palm rejection, and normalized coordinates for rotation survival.
 *
 * @param modifier Modifier for the overall component. Pass height via modifier;
 *   the canvas uses [heightIn] with a 200dp floor so callers can expand it on tablets.
 * @param guideLineStyle Style of guide lines to display.
 * @param recognizedText Live ML Kit recognition preview text (empty = no preview).
 * @param referenceWord Reference word to compare against for green checkmark.
 * @param onInkReady Called after each stroke with the full Ink object.
 * @param onClear Called when all strokes are cleared.
 * @param onUndo Called when the last stroke is undone. Returns remaining strokes as Ink, or null.
 */
@Composable
fun HandwritingCanvas(
    modifier: Modifier = Modifier,
    guideLineStyle: GuideLineStyle = GuideLineStyle.NOTEBOOK,
    recognizedText: String = "",
    referenceWord: String = "",
    onInkReady: (Ink) -> Unit,
    onClear: () -> Unit,
    onUndo: ((Ink?) -> Unit)? = null,
) {
    // Store strokes as normalized coordinates — survive rotation
    val normalizedStrokes = remember { mutableStateListOf<List<NormalizedStrokePoint>>() }
    var currentStrokePixels by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var hoverPosition by remember { mutableStateOf<Offset?>(null) }
    val canvasShape = RoundedCornerShape(16.dp)

    Column(modifier = modifier) {
        // Canvas area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(min = 200.dp)
                .clip(canvasShape)
                .background(color = HandwritingCanvasBg)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = canvasShape,
                )
                // Stylus hover detection
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val stylusChange = event.changes.firstOrNull {
                                it.type == PointerType.Stylus && !it.pressed
                            }
                            hoverPosition = stylusChange?.position
                        }
                    }
                }
                // Drawing input with stylus pressure support and palm rejection
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitPointerEvent()
                            val firstChange = down.changes.firstOrNull { it.pressed } ?: continue

                            val isStylus = firstChange.type == PointerType.Stylus
                            val pressure = if (isStylus) firstChange.pressure else 1f

                            currentStrokePixels = listOf(
                                StrokePoint(
                                    x = firstChange.position.x,
                                    y = firstChange.position.y,
                                    timestamp = System.currentTimeMillis(),
                                    pressure = pressure,
                                ),
                            )
                            firstChange.consume()
                            hoverPosition = null

                            var cancelled = false

                            // Track drag
                            while (true) {
                                val moveEvent = awaitPointerEvent()
                                val change = moveEvent.changes.firstOrNull() ?: break

                                if (!change.pressed) {
                                    // Pointer up
                                    change.consume()
                                    break
                                }

                                // Palm rejection: if pointer type changed (system sends cancel)
                                // or if we detect a touch while using stylus
                                if (isStylus && change.type != PointerType.Stylus) {
                                    cancelled = true
                                    change.consume()
                                    break
                                }

                                val movePressure = if (isStylus) change.pressure else 1f
                                currentStrokePixels = currentStrokePixels + StrokePoint(
                                    x = change.position.x,
                                    y = change.position.y,
                                    timestamp = System.currentTimeMillis(),
                                    pressure = movePressure,
                                )
                                change.consume()
                            }

                            // Finish stroke
                            if (!cancelled && currentStrokePixels.isNotEmpty() &&
                                canvasSize.width > 0 && canvasSize.height > 0
                            ) {
                                val normalized = currentStrokePixels.map {
                                    it.normalize(canvasSize.width, canvasSize.height)
                                }
                                normalizedStrokes.add(normalized)
                                currentStrokePixels = emptyList()
                                onInkReady(
                                    buildInk(normalizedStrokes, canvasSize.width, canvasSize.height),
                                )
                            } else {
                                // Cancelled (palm rejection) — discard
                                currentStrokePixels = emptyList()
                            }
                        }
                    }
                },
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                canvasSize = size

                // 1. Draw guide lines (multi-line when tall enough)
                drawGuideLines(guideLineStyle, size)

                // 2. Draw completed strokes — denormalize from stored normalized coords
                for (normalizedStroke in normalizedStrokes) {
                    val pixelPoints = normalizedStroke.map {
                        it.denormalize(size.width, size.height)
                    }
                    drawSmoothPath(pixelPoints, HandwritingStrokeColor)
                }

                // 3. Draw current in-progress stroke
                if (currentStrokePixels.size >= 2) {
                    drawSmoothPath(currentStrokePixels, HandwritingStrokeColor)
                }

                // 4. Stylus hover cursor
                hoverPosition?.let { pos ->
                    drawCircle(
                        color = StylusHoverColor.copy(alpha = 0.3f),
                        radius = 8.dp.toPx(),
                        center = pos,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Controls row: preview + buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Live recognition preview
            HandwritingPreviewLabel(
                previewText = recognizedText,
                matchesReference = recognizedText.isNotEmpty() &&
                    recognizedText.lowercase().trim() == referenceWord.lowercase().trim(),
                modifier = Modifier.weight(1f),
            )

            // Undo button
            if (onUndo != null) {
                IconButton(
                    onClick = {
                        if (normalizedStrokes.isNotEmpty()) {
                            normalizedStrokes.removeAt(normalizedStrokes.lastIndex)
                            currentStrokePixels = emptyList()
                            val ink = if (normalizedStrokes.isNotEmpty() &&
                                canvasSize.width > 0 && canvasSize.height > 0
                            ) {
                                buildInk(normalizedStrokes, canvasSize.width, canvasSize.height)
                            } else {
                                null
                            }
                            onUndo(ink)
                        }
                    },
                    enabled = normalizedStrokes.isNotEmpty(),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (normalizedStrokes.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Clear button
            IconButton(
                onClick = {
                    normalizedStrokes.clear()
                    currentStrokePixels = emptyList()
                    onClear()
                },
                enabled = normalizedStrokes.isNotEmpty(),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Clear",
                    tint = if (normalizedStrokes.isNotEmpty()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    },
                )
            }
        }
    }
}

/**
 * Live recognition preview label shown below the canvas.
 * Shows green checkmark when recognized text matches the reference word.
 */
@Composable
private fun HandwritingPreviewLabel(
    previewText: String,
    matchesReference: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = previewText,
        transitionSpec = {
            fadeIn(androidx.compose.animation.core.tween(200)) togetherWith
                fadeOut(androidx.compose.animation.core.tween(200))
        },
        label = "previewText",
        modifier = modifier,
    ) { text ->
        if (text.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        color = if (matchesReference) {
                            CorrectTextColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                )
                if (matchesReference) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = CorrectTextColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Draw guide lines mimicking notebook/cahier paper.
 * When the canvas is tall enough (>250dp), repeats the 3-line pattern for multiple lines.
 */
private fun DrawScope.drawGuideLines(
    style: GuideLineStyle,
    canvasSize: Size,
) {
    val guideColor = HandwritingGuideColor
    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))

    when (style) {
        GuideLineStyle.NOTEBOOK -> {
            // Line set height: the space needed for one top/mid/baseline group
            val lineSetHeight = canvasSize.height * 0.50f
            val lineCount = (canvasSize.height / lineSetHeight).toInt().coerceIn(1, 4)

            for (i in 0 until lineCount) {
                val setTop = i * (canvasSize.height / lineCount)
                val setHeight = canvasSize.height / lineCount

                val topline = setTop + setHeight * 0.20f
                val midline = setTop + setHeight * 0.45f
                val baseline = setTop + setHeight * 0.70f

                // Baseline — solid, slightly darker
                drawLine(
                    guideColor.copy(alpha = 0.6f),
                    Offset(0f, baseline),
                    Offset(canvasSize.width, baseline),
                    strokeWidth = 2f,
                )
                // Midline — dashed
                drawLine(
                    guideColor.copy(alpha = 0.4f),
                    Offset(0f, midline),
                    Offset(canvasSize.width, midline),
                    strokeWidth = 1f,
                    pathEffect = dashedEffect,
                )
                // Top line — dashed
                drawLine(
                    guideColor.copy(alpha = 0.4f),
                    Offset(0f, topline),
                    Offset(canvasSize.width, topline),
                    strokeWidth = 1f,
                    pathEffect = dashedEffect,
                )
            }
        }
        GuideLineStyle.SINGLE_LINE -> {
            val baseline = canvasSize.height * 0.70f
            drawLine(
                guideColor.copy(alpha = 0.5f),
                Offset(0f, baseline),
                Offset(canvasSize.width, baseline),
                strokeWidth = 2f,
            )
        }
        GuideLineStyle.NONE -> { /* no guides */ }
    }
}

/**
 * Draw a smooth path through stroke points using quadratic Bezier interpolation.
 * Supports pressure-sensitive stroke width for stylus input.
 */
private fun DrawScope.drawSmoothPath(
    points: List<StrokePoint>,
    color: Color,
) {
    if (points.size < 2) return

    val baseWidth = BASE_STROKE_WIDTH_DP.dp.toPx()
    val hasVaryingPressure = points.any { it.pressure != 1f }

    if (!hasVaryingPressure) {
        // Constant-width path (finger input)
        val strokeStyle = Stroke(
            width = baseWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val midX = (points[i - 1].x + points[i].x) / 2f
                val midY = (points[i - 1].y + points[i].y) / 2f
                quadraticTo(
                    points[i - 1].x,
                    points[i - 1].y,
                    midX,
                    midY,
                )
            }
            lineTo(points.last().x, points.last().y)
        }
        drawPath(path, color, style = strokeStyle)
    } else {
        // Pressure-sensitive: draw segment-by-segment with varying width
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val avgPressure = (prev.pressure + curr.pressure) / 2f
            val width = baseWidth * (0.4f + avgPressure * 1.2f)

            val segmentStyle = Stroke(
                width = width,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )

            val segment = Path().apply {
                moveTo(prev.x, prev.y)
                lineTo(curr.x, curr.y)
            }
            drawPath(segment, color, style = segmentStyle)
        }
    }
}
