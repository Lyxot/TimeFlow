/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 一个基于权重占满行宽的网格布局，用于替代 FlowRow。
 * - 计算每行可容纳的元素数量（基于 itemSize 与可用宽度）
 * - 行内元素 weight(1f) 保持等宽
 * - 末行用占位 Spacer 填充，保证行宽撑满
 */
@Composable
fun WeightedGrid(
    modifier: Modifier = Modifier,
    itemSize: Dp,
    horizontalSpacing: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    buttons: List<@Composable RowScope.() -> Unit>
) {
    BoxWithConstraints(modifier = modifier) {
        val itemsPerRow = maxOf(1, (maxWidth / itemSize).toInt())
        val rows = buttons.chunked(itemsPerRow)

        Column(verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
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
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

