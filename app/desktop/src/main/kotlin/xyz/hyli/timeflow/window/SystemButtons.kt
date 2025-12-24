/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.window

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import xyz.hyli.timeflow.desktop.generated.resources.Res
import xyz.hyli.timeflow.desktop.generated.resources.ic_fluent_dismiss_48_regular
import xyz.hyli.timeflow.desktop.generated.resources.ic_fluent_square_48_regular
import xyz.hyli.timeflow.desktop.generated.resources.ic_fluent_square_multiple_48_regular
import xyz.hyli.timeflow.desktop.generated.resources.ic_fluent_subtract_48_filled

@Composable
internal fun WindowsSystemButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
    buttons: List<SystemButtonType>,
) {
    Row(
        modifier = Modifier.fillMaxHeight().wrapContentHeight(Alignment.Top),
        verticalAlignment = Alignment.Top
    ) {
        val closePainter = painterResource(Res.drawable.ic_fluent_dismiss_48_regular)
        val minimizePainter = painterResource(Res.drawable.ic_fluent_subtract_48_filled)
        val maximizePainter = painterResource(Res.drawable.ic_fluent_square_48_regular)
        val floatingPainter = painterResource(Res.drawable.ic_fluent_square_multiple_48_regular)
        buttons.forEach {
            when (it) {
                SystemButtonType.Close -> {
                    TitleBarIconButton(
                        painter = closePainter,
                        contentDescription = "Close window",
                        hoverBackground = MaterialTheme.colorScheme.error,
                        hoverContentColor = MaterialTheme.colorScheme.onError,
                        onClick = onRequestClose,
                    )
                }

                SystemButtonType.Minimize -> {
                    onRequestMinimize?.let {
                        TitleBarIconButton(
                            painter = minimizePainter,
                            contentDescription = "Minimize window",
                            onClick = onRequestMinimize,
                        )
                    }
                }

                SystemButtonType.Maximize -> {
                    onToggleMaximize?.let {
                        TitleBarIconButton(
                            painter = if (isWindowMaximized()) floatingPainter else maximizePainter,
                            contentDescription = "Toggle maximize",
                            onClick = onToggleMaximize,
                        )
                    }
                }
            }
        }
    }
}
