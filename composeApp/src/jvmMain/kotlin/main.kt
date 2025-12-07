/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.win32.W32APIOptions
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.Factory
import xyz.hyli.timeflow.ui.viewmodel.ViewModelOwner
import xyz.hyli.timeflow.utils.BasicWindowProc
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isWindows
import xyz.hyli.timeflow.utils.windowProc
import java.awt.Dimension

fun main() = application {
    val appContainer = AppContainer(Factory())
    Window(
        title = "TimeFlow",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(480, 540)
        App(
            viewModel = ViewModelOwner(appContainer).timeFlowViewModel
        )
        if (currentPlatform().isWindows()) {
            DisposableEffect(window) {
                val user32: User32 =
                    Native.load("user32", User32::class.java, W32APIOptions.DEFAULT_OPTIONS)
                windowProc.tryEmit(
                    BasicWindowProc(
                        user32 = user32,
                        window = window
                    )
                )
                onDispose {
                    windowProc.value?.close()
                    windowProc.tryEmit(null)
                }
            }
        }
    }
}

