/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun Modifier.ifThen(
    condition: Boolean,
    modifier: Modifier.Companion.() -> Modifier?
): Modifier = if (condition) this.then(modifier(Modifier.Companion) ?: Modifier) else this

@Composable
fun Modifier.navigationBarHorizontalPadding(): Modifier {
    val navSuiteType by LocalNavSuiteType.current
    return this.ifThen(navSuiteType in NavigationBarType) {
        Modifier.padding(horizontal = 8.dp)
    }
}

@Composable
fun Modifier.bottomPadding(): Modifier = this.padding(
    bottom = maxOf(
        WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
        24.dp
    )
)

/**
 * 绘制带圆角的背景遮罩（绘制圆角区域外的部分）
 * @param containerColor 遮罩颜色（圆角区域外的背景色）
 * @param cornerRadius 圆角半径
 * @param topStart 左上角是否圆角
 * @param topEnd 右上角是否圆角
 * @param bottomStart 左下角是否圆角
 * @param bottomEnd 右下角是否圆角
 */
fun Modifier.drawRoundedCornerBackground(
    containerColor: Color,
    cornerRadius: Dp = 24.dp,
    topStart: Boolean = true,
    topEnd: Boolean = false,
    bottomStart: Boolean = true,
    bottomEnd: Boolean = false
): Modifier = this
    .drawBehind {
        val radiusPx = cornerRadius.toPx()

        // 创建整个区域的矩形路径
        val fullRect = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
        }

        // 创建圆角矩形路径
        val roundedRect = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    topLeftCornerRadius = if (topStart) CornerRadius(
                        radiusPx,
                        radiusPx
                    ) else CornerRadius.Zero,
                    topRightCornerRadius = if (topEnd) CornerRadius(
                        radiusPx,
                        radiusPx
                    ) else CornerRadius.Zero,
                    bottomRightCornerRadius = if (bottomEnd) CornerRadius(
                        radiusPx,
                        radiusPx
                    ) else CornerRadius.Zero,
                    bottomLeftCornerRadius = if (bottomStart) CornerRadius(
                        radiusPx,
                        radiusPx
                    ) else CornerRadius.Zero
                )
            )
        }

        // 从整个矩形中减去圆角矩形，得到反向路径（圆角区域外的部分）
        val inversePath = Path().apply {
            op(fullRect, roundedRect, PathOperation.Difference)
        }

        drawPath(
            path = inversePath,
            color = containerColor
        )
    }
    .clip(
        RoundedCornerShape(
            topStart = if (topStart) cornerRadius else 0.dp,
            topEnd = if (topEnd) cornerRadius else 0.dp,
            bottomStart = if (bottomStart) cornerRadius else 0.dp,
            bottomEnd = if (bottomEnd) cornerRadius else 0.dp
        )
    )

