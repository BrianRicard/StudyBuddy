package com.studybuddy.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * A lazy list that supports long-press-to-drag reordering.
 *
 * @param items The list of items to display.
 * @param key A function that provides a stable key for each item.
 * @param onReorder Called when an item is dropped at a new position.
 * @param itemContent Composable for each item. Receives the item and whether it is being dragged.
 */
@Composable
fun <T : Any> ReorderableList(
    items: List<T>,
    key: (T) -> Any,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    itemContent: @Composable (T, Boolean) -> Unit,
) {
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items, key = { _, item -> key(item) }) { index, item ->
            val isDragging = index == draggedIndex
            val elevation by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 2.dp,
                label = "dragElevation",
            )
            val scale by animateFloatAsState(
                targetValue = if (isDragging) 1.03f else 1f,
                label = "dragScale",
            )

            Box(
                modifier = Modifier
                    .then(
                        if (isDragging) {
                            Modifier
                                .zIndex(1f)
                                .offset { IntOffset(0, dragOffsetY.toInt()) }
                        } else {
                            Modifier
                        },
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = elevation.toPx()
                    }
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetY += dragAmount.y
                                // Calculate target index based on drag offset
                                val itemHeight =
                                    listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                                if (itemHeight > 0) {
                                    val targetIndex =
                                        (index + (dragOffsetY / itemHeight).toInt()).coerceIn(
                                            0,
                                            items.lastIndex,
                                        )
                                    if (targetIndex != draggedIndex) {
                                        onReorder(draggedIndex, targetIndex)
                                        draggedIndex = targetIndex
                                        dragOffsetY = 0f
                                    }
                                }
                            },
                            onDragEnd = {
                                draggedIndex = -1
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                draggedIndex = -1
                                dragOffsetY = 0f
                            },
                        )
                    },
            ) {
                itemContent(item, isDragging)
            }
        }
    }
}
