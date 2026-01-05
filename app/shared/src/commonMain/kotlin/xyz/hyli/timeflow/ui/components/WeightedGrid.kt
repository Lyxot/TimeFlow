/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.floor

/**
 * 一个基于权重占满行宽的网格布局，用于替代 FlowRow。
 * - 计算每行可容纳的元素数量（基于 itemSize 与可用宽度）
 * - 行内元素 weight(1f) 保持等宽
 * - 末行用占位 Spacer 填充，保证行宽撑满
 */
@Composable
fun WeightedGrid(
    modifier: Modifier = Modifier,
    itemWidth: Dp,
    horizontalSpacing: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    buttons: List<@Composable RowScope.() -> Unit>,
    columnModifier: Modifier = Modifier
) = BoxWithConstraints(modifier = modifier) {
    val itemsPerRow = maxOf(1, (maxWidth / itemWidth).toInt())
        WeightedGridContent(
            itemsPerRow = itemsPerRow,
            horizontalSpacing = horizontalSpacing,
            verticalSpacing = verticalSpacing,
            buttons = buttons,
            columnModifier = columnModifier
        )
    }

/**
 * 带长按拖拽触发的版本，保留原有简单 WeightedGrid。
 */
@Composable
fun WeightedGridWithDrag(
    modifier: Modifier = Modifier,
    itemSize: DpSize,
    horizontalSpacing: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    buttons: List<@Composable RowScope.() -> Unit>,
    onItemDrag: ((index: Int) -> Unit),
    triggerOnLongPress: Boolean = true
) = BoxWithConstraints(modifier = modifier) {
    val itemsPerRow = maxOf(1, (maxWidth / itemSize.width).toInt())

    val density = LocalDensity.current
    val maxWidthPx = with(density) { maxWidth.toPx() }
    val hSpacePx = with(density) { horizontalSpacing.toPx() }
    val vSpacePx = with(density) { verticalSpacing.toPx() }

        WeightedGridContent(
            itemsPerRow = itemsPerRow,
            horizontalSpacing = horizontalSpacing,
            verticalSpacing = verticalSpacing,
            buttons = buttons,
            columnModifier = Modifier.pointerInput(buttons.size, itemsPerRow, maxWidthPx, hSpacePx, vSpacePx, triggerOnLongPress) {
                val visited = mutableSetOf<Int>()
                val trigger: (Float, Float) -> Unit = { x, y ->
                    val effectiveItemWidth =
                        (maxWidthPx - hSpacePx * (itemsPerRow - 1)) / itemsPerRow
                    val effectiveItemHeight = itemSize.height.toPx()
                    val col = floor(x / (effectiveItemWidth + hSpacePx)).toInt()
                        .coerceIn(0, itemsPerRow - 1)
                    val row = floor(y / (effectiveItemHeight + vSpacePx)).toInt().coerceAtLeast(0)
                    val index = row * itemsPerRow + col
                    if (index in buttons.indices && visited.add(index)) {
                        onItemDrag(index)
                    }
                }

                if (triggerOnLongPress) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            visited.clear()
                            trigger(offset.x, offset.y)
                        },
                        onDrag = { change, _ ->
                            trigger(change.position.x, change.position.y)
                            change.consume()
                        }
                    )
                } else {
                    detectDragGestures(
                        onDragStart = { offset ->
                            visited.clear()
                            trigger(offset.x, offset.y)
                        },
                        onDrag = { change, _ ->
                            trigger(change.position.x, change.position.y)
                            change.consume()
                        }
                    )
                }
            }
        )
    }

@Composable
private fun WeightedGridContent(
    itemsPerRow: Int,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    buttons: List<@Composable RowScope.() -> Unit>,
    columnModifier: Modifier = Modifier
) {
    val rows = buttons.chunked(itemsPerRow)
    Column(
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        modifier = columnModifier
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                rowItems.forEach { content -> content() }
                if (rowItems.size < itemsPerRow) {
                    repeat(itemsPerRow - rowItems.size) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}

