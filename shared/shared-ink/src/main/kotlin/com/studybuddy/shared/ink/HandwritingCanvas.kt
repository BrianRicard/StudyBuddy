package com.studybuddy.shared.ink

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.digitalink.Ink

data class StrokePoint(val x: Float, val y: Float, val timestamp: Long)

@Composable
fun HandwritingCanvas(
    modifier: Modifier = Modifier,
    onInkReady: (Ink) -> Unit,
    onClear: () -> Unit,
) {
    val strokes = remember { mutableStateListOf<List<StrokePoint>>() }
    var currentStroke by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
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
                // Draw ruled lines
                val lineColor = Color.LightGray.copy(alpha = 0.5f)
                val lineCount = 4
                for (i in 1..lineCount) {
                    val y = size.height * i / (lineCount + 1)
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }

                // Draw completed strokes
                val strokeStyle = Stroke(
                    width = 4f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
                val inkColor = Color.Black
                for (stroke in strokes) {
                    if (stroke.size < 2) continue
                    val path = Path().apply {
                        moveTo(stroke.first().x, stroke.first().y)
                        for (point in stroke.drop(1)) {
                            lineTo(point.x, point.y)
                        }
                    }
                    drawPath(path, inkColor, style = strokeStyle)
                }

                // Draw current stroke
                if (currentStroke.size >= 2) {
                    val path = Path().apply {
                        moveTo(currentStroke.first().x, currentStroke.first().y)
                        for (point in currentStroke.drop(1)) {
                            lineTo(point.x, point.y)
                        }
                    }
                    drawPath(path, inkColor, style = strokeStyle)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                strokes.clear()
                currentStroke = emptyList()
                onClear()
            },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Clear")
        }
    }
}

private fun buildInk(strokes: List<List<StrokePoint>>): Ink {
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
