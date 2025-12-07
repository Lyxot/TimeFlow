/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.window

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.jetbrains.JBR
import com.jetbrains.WindowDecorations
import com.jetbrains.WindowMove
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.ui.theme.AppTheme
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import java.awt.event.MouseEvent

// a window frame which is rendered fully with Compose, including title bar
@Composable
private fun FrameWindowScope.CustomWindowFrame(
    title: String,
    titlePosition: TitlePosition,
    windowIcon: Painter?,
    onRequestMinimize: (() -> Unit)?,
    onRequestClose: () -> Unit,
    onRequestToggleMaximize: (() -> Unit)?,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(Modifier.fillMaxSize()) {
            SnapDraggableToolbar(
                title = title,
                windowIcon = windowIcon,
                titlePosition = titlePosition,
                start = start,
                end = end,
                onRequestMinimize = onRequestMinimize,
                onRequestClose = onRequestClose,
                onRequestToggleMaximize = onRequestToggleMaximize
            )
            Box(Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
fun isWindowFocused(): Boolean = LocalWindowInfo.current.isWindowFocused

@Composable
fun isWindowMaximized(): Boolean = LocalWindowState.current.placement == WindowPlacement.Maximized

@Composable
fun isWindowFloating(): Boolean = LocalWindowState.current.placement == WindowPlacement.Floating

@Composable
fun FrameWindowScope.SnapDraggableToolbar(
    title: String,
    windowIcon: Painter?,
    titlePosition: TitlePosition,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    val titleBar = TitleBar.getPlatformTitleBar()
    if (JBR.isWindowDecorationsSupported()) {
        val density = LocalDensity.current
        var headerHeight by remember {
            mutableFloatStateOf(titleBar.titleBarHeight.value)
        }
        val customTitleBar = remember {
            JBR.getWindowDecorations().createCustomTitleBar()
        }
        LaunchedEffect(headerHeight) {
            customTitleBar.height = headerHeight
            customTitleBar.putProperty("controls.visible", false)
            val previousPlacement = window.placement
            JBR.getWindowDecorations().setCustomTitleBar(window, customTitleBar)
            // JBR resets window placement to Floating so we should restore our placement.
            if (window.placement != previousPlacement) {
                window.placement = previousPlacement
            }
        }
        Box(
            Modifier
                .onSizeChanged {
                    headerHeight = density.run { it.height.toDp() }.value
                }
        ) {
            Spacer(
                Modifier
                    .matchParentSize()
                    .customTitleBarMouseEventHandler(customTitleBar)
            )
            FrameContent(
                titleBar = titleBar,
                modifier = Modifier,
                title = title,
                windowIcon = windowIcon,
                titlePosition = titlePosition,
                start = start,
                end = end,
                onRequestMinimize = onRequestMinimize,
                onRequestToggleMaximize = onRequestToggleMaximize,
                onRequestClose = onRequestClose
            )
        }
    } else {
        SystemDraggableSection(
            onRequestToggleMaximize = onRequestToggleMaximize,
        ) { modifier ->
            FrameContent(
                titleBar = titleBar,
                modifier = modifier,
                title = title,
                windowIcon = windowIcon,
                titlePosition = titlePosition,
                start = start,
                end = end,
                onRequestMinimize = onRequestMinimize,
                onRequestToggleMaximize = onRequestToggleMaximize,
                onRequestClose = onRequestClose
            )
        }
    }
}

@Composable
internal fun FrameWindowScope.SystemDraggableSection(
    onRequestToggleMaximize: (() -> Unit)?,
    content: @Composable (Modifier) -> Unit
) {
    val windowMove: WindowMove? = JBR.getWindowMove()
    val viewConfig = LocalViewConfiguration.current
    var lastPress by remember { mutableLongStateOf(0L) }
    if (windowMove != null) {
        content(
            Modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            if (event.type == PointerEventType.Press && event.buttons.isPrimaryPressed) {
                                windowMove.startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
                                val now = System.currentTimeMillis()
                                val delta = now - lastPress
                                if (delta in viewConfig.doubleTapMinTimeMillis..viewConfig.doubleTapTimeoutMillis) {
                                    onRequestToggleMaximize?.invoke()
                                }
                                lastPress = now
                            }
                        }
                    }
                },
        )
    } else {
        WindowDraggableArea {
            content(Modifier)
        }
    }
}

internal fun Modifier.customTitleBarMouseEventHandler(
    titleBar: WindowDecorations.CustomTitleBar
): Modifier =
    pointerInput(titleBar) {
        awaitPointerEventScope {
            var inUserControl = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                event.changes.forEach {
                    if (!it.isConsumed && !inUserControl) {
                        titleBar.forceHitTest(false)
                    } else {
                        if (event.type == PointerEventType.Press) {
                            inUserControl = true
                        }
                        if (event.type == PointerEventType.Release) {
                            inUserControl = false
                        }
                        titleBar.forceHitTest(true)
                    }
                }
            }
        }
    }

@Composable
private fun FrameContent(
    titleBar: TitleBar,
    modifier: Modifier,
    title: String,
    windowIcon: Painter?,
    titlePosition: TitlePosition,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    titleBar.RenderTitleBar(
        titleBar = titleBar,
        modifier = modifier.fillMaxWidth(),
        title = title,
        windowIcon = windowIcon,
        titlePosition = titlePosition,
        start = start,
        end = end,
        onRequestMinimize = onRequestMinimize,
        onRequestToggleMaximize = onRequestToggleMaximize,
        onRequestClose = onRequestClose,
    )
}

@Composable
fun CustomWindow(
    state: WindowState,
    viewModel: TimeFlowViewModel,
    defaultIcon: Painter,
    onCloseRequest: () -> Unit,
    title: String = BuildConfig.APP_NAME,
    titlePosition: TitlePosition = TitlePosition.default(),
    start: (@Composable () -> Unit)? = null,
    end: (@Composable () -> Unit)? = null,
    resizable: Boolean = true,
    onRequestMinimize: (() -> Unit)? = { state.isMinimized = true },
    onRequestToggleMaximize: (() -> Unit)? = {
        val next = if (state.placement == WindowPlacement.Maximized) {
            WindowPlacement.Floating
        } else {
            WindowPlacement.Maximized
        }
        state.placement = next
    },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    alwaysOnTop: Boolean = false,
    preventMinimize: Boolean = onRequestMinimize == null,
    content: @Composable FrameWindowScope.() -> Unit,
) {
    val transparent: Boolean
    val undecorated: Boolean
    val isAeroSnapSupported = JBR.isWindowDecorationsSupported()
    if (isAeroSnapSupported) {
        transparent = false
        undecorated = false
    } else {
        transparent = true
        undecorated = true
    }
    Window(
        state = state,
        transparent = transparent,
        undecorated = undecorated,
        icon = defaultIcon,
        title = title,
        resizable = resizable,
        onCloseRequest = onCloseRequest,
        onKeyEvent = onKeyEvent,
        alwaysOnTop = alwaysOnTop,
    ) {
        val focusClearModifier = remember { Modifier.clearFocusOnTap() }
        CompositionLocalProvider(
            LocalWindowState provides state,
        ) {
            AppTheme(viewModel) {
                CustomWindowFrame(
                    title = title,
                    titlePosition = titlePosition,
                    windowIcon = null,
                    onRequestMinimize = onRequestMinimize,
                    onRequestClose = onCloseRequest,
                    onRequestToggleMaximize = onRequestToggleMaximize,
                    start = start,
                    end = end,
                ) {
                    Box(
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 8.dp)
                            .then(focusClearModifier)
                    ) {
                        content()
                    }
                }
            }
        }
        if (preventMinimize) {
            PreventMinimize()
        }
    }
}

@Composable
private fun PreventMinimize() {
    val state = LocalWindowState.current
    LaunchedEffect(state.isMinimized) {
        if (state.isMinimized) {
            state.isMinimized = false
        }
    }
}

private fun Modifier.clearFocusOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Main)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Main)
            if (upEvent != null) {
                focusManager.clearFocus()
            }
        }
    }
}

@Immutable
data class TitlePosition(
    val centered: Boolean = false,
    val afterStart: Boolean = false,
    val padding: PaddingValues = PaddingValues(0.dp),
) {
    companion object {
        fun default() = TitlePosition()
    }
}

private val LocalWindowState =
    compositionLocalOf<WindowState> { error("window controller not provided") }
