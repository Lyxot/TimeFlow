/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.window

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface TitleBar {
    val titleBarHeight: Dp
    val systemButtonsPosition: SystemButtonsPosition

    @Composable
    fun RenderSystemButtons(
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onToggleMaximize: (() -> Unit)?,
    )

    @Composable
    fun RenderTitleBarContent(
        title: String,
        titlePosition: TitlePosition,
        modifier: Modifier,
        windowIcon: Painter?,
        start: (@Composable () -> Unit)?,
        end: (@Composable () -> Unit)?,
    )

    @Composable
    fun RenderTitleBar(
        modifier: Modifier,
        titleBar: TitleBar,
        title: String,
        windowIcon: Painter?,
        titlePosition: TitlePosition,
        start: (@Composable () -> Unit)?,
        end: (@Composable () -> Unit)?,
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onRequestToggleMaximize: (() -> Unit)?,
    )

    companion object {
        val DefaultTitleBarHeight: Dp = 32.dp

        // 保留旧名称，避免后续迁移遗漏
        val DefaultTitleBarHeigh: Dp = DefaultTitleBarHeight
        fun getPlatformTitleBar(): TitleBar {
            return WindowsTitleBar
        }
    }
}

enum class SystemButtonType {
    Close,
    Minimize,
    Maximize,
}

data class SystemButtonsPosition(
    val buttons: List<SystemButtonType>,
    val isLeft: Boolean,
)

@Composable
internal fun CommonTitleBarContent(
    modifier: Modifier,
    title: String,
    windowIcon: Painter?,
    titlePosition: TitlePosition,
    start: @Composable (() -> Unit)?,
    end: @Composable (() -> Unit)?
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(16.dp))
            windowIcon?.let {
                Image(it, null, Modifier.size(16.dp).padding(end = 8.dp))
            }
        }
        if (!titlePosition.afterStart) {
            Title(
                modifier = Modifier
                    .let { base ->
                        if (titlePosition.centered) {
                            base.weight(1f).let {
                                if (start == null) it.wrapContentWidth() else it
                            }
                        } else base
                    }
                    .padding(titlePosition.padding),
                title = title
            )
        }
        start?.let {
            Row {
                start()
                Spacer(Modifier.width(8.dp))
            }
        }
        if (titlePosition.afterStart) {
            Title(
                modifier = Modifier
                    .weight(1f)
                    .let { base ->
                        if (titlePosition.centered) base.wrapContentWidth() else base
                    }
                    .padding(titlePosition.padding),
                title = title
            )
        }
        if (!titlePosition.centered && !titlePosition.afterStart) {
            Spacer(Modifier.weight(1f))
        }
        end?.let {
            Row {
                end()
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun Title(
    modifier: Modifier,
    title: String,
) {
    val focused = isWindowFocused()
    val alpha by animateFloatAsState(if (focused) 1f else 0.6f)
    Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        modifier = modifier
    )
}

@Composable
internal fun CommonRenderTitleBar(
    modifier: Modifier,
    titleBar: TitleBar,
    title: String,
    windowIcon: Painter? = null,
    titlePosition: TitlePosition,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
) {
    Row(
        modifier.height(titleBar.titleBarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val systemButtonsAtFirst = titleBar.systemButtonsPosition.isLeft

        if (systemButtonsAtFirst) {
            titleBar.RenderSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onRequestToggleMaximize,
            )
        }
        titleBar.RenderTitleBarContent(
            title = title,
            titlePosition = titlePosition,
            modifier = Modifier.weight(1f),
            windowIcon = windowIcon,
            start = start,
            end = end
        )
        if (!systemButtonsAtFirst) {
            titleBar.RenderSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onRequestToggleMaximize,
            )
        }
    }
}

@Composable
internal fun TitleBarIconButton(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hoverBackground: Color = MaterialTheme.colorScheme.surfaceVariant,
    hoverContentColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(
        if (enabled && isHovered) hoverBackground else Color.Transparent
    )
    val tint by animateColorAsState(
        if (enabled && isHovered) hoverContentColor else MaterialTheme.colorScheme.onSurface
    )
    // TODO: 需要的话可在此添加 tooltip 或快捷键提示
    Box(
        modifier = modifier
            .fillMaxHeight()
            .hoverable(interactionSource, enabled)
            .clickable(enabled = enabled, onClick = onClick)
            .background(background)
            .width(48.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
