package com.studybuddy.shared.ink

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.digitalink.Ink

data class StrokePoint(val x: Float, val y: Float, val timestamp: Long)

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

/**
 * Enhanced handwriting canvas with notebook guide lines, smooth Bezier strokes,
 * undo support, and live recognition preview.
 *
 * @param modifier Modifier for the overall component.
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
    val strokes = remember { mutableStateListOf<List<StrokePoint>>() }
    var currentStroke by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }
    val canvasShape = RoundedCornerShape(16.dp)

    Column(modifier = modifier) {
        // Canvas area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(canvasShape)
                .background(color = HandwritingCanvasBg)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = canvasShape,
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke = listOf(
                                StrokePoint(offset.x, offset.y, System.currentTimeMillis()),
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentStroke = currentStroke + StrokePoint(
                                change.position.x,
                                change.position.y,
                                System.currentTimeMillis(),
                            )
                        },
                        onDragEnd = {
                            if (currentStroke.isNotEmpty()) {
                                strokes.add(currentStroke)
                                currentStroke = emptyList()
                                onInkReady(buildInk(strokes))
                            }
                        },
                    )
                },
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // 1. Draw guide lines
                drawGuideLines(guideLineStyle, size)

                // 2. Draw completed strokes with smooth Bezier curves
                val strokeStyle = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
                for (stroke in strokes) {
                    drawSmoothPath(stroke, HandwritingStrokeColor, strokeStyle)
                }

                // 3. Draw current in-progress stroke
                if (currentStroke.size >= 2) {
                    drawSmoothPath(currentStroke, HandwritingStrokeColor, strokeStyle)
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
                        if (strokes.isNotEmpty()) {
                            strokes.removeAt(strokes.lastIndex)
                            currentStroke = emptyList()
                            val ink = if (strokes.isNotEmpty()) buildInk(strokes) else null
                            onUndo(ink)
                        }
                    },
                    enabled = strokes.isNotEmpty(),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (strokes.isNotEmpty()) {
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
                    strokes.clear()
                    currentStroke = emptyList()
                    onClear()
                },
                enabled = strokes.isNotEmpty(),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Clear",
                    tint = if (strokes.isNotEmpty()) {
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
 */
private fun DrawScope.drawGuideLines(
    style: GuideLineStyle,
    canvasSize: Size,
) {
    val guideColor = HandwritingGuideColor
    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))

    when (style) {
        GuideLineStyle.NOTEBOOK -> {
            val baseline = canvasSize.height * 0.70f
            val midline = canvasSize.height * 0.45f
            val topline = canvasSize.height * 0.20f

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
 * Makes finger strokes look clean and pencil-like rather than jagged.
 */
private fun DrawScope.drawSmoothPath(
    points: List<StrokePoint>,
    color: Color,
    strokeStyle: Stroke,
) {
    if (points.size < 2) return
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
}

internal fun buildInk(strokes: List<List<StrokePoint>>): Ink {
    val inkBuilder = Ink.builder()
    for (stroke in strokes) {
        val strokeBuilder = Ink.Stroke.builder()
        for (point in stroke) {
            strokeBuilder.addPoint(Ink.Point.create(point.x, point.y, point.timestamp))
        }
        inkBuilder.addStroke(strokeBuilder.build())
    }
    return inkBuilder.build()
}
