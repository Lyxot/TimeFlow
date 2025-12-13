/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.win32.W32APIOptions
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.utils.toPath
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.painterResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.ic_launcher
import timeflow.composeapp.generated.resources.ic_launcher_night
import timeflow.composeapp.generated.resources.icon_rounded_dark
import timeflow.composeapp.generated.resources.icon_rounded_light
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.AppContent
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.datastore.dataStoreFileName
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.Factory
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import xyz.hyli.timeflow.ui.viewmodel.ViewModelOwner
import xyz.hyli.timeflow.utils.BasicWindowProc
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isMacOS
import xyz.hyli.timeflow.utils.isWindows
import xyz.hyli.timeflow.utils.windowProc
import xyz.hyli.timeflow.window.CustomWindow
import java.awt.Dimension
import kotlin.jvm.optionals.getOrNull

fun main() = application {
    FileKit.init(appId = "TimeFlow")
    @Suppress("KotlinConstantConditions")
    val factory = BuildConfig.PORTABLE.takeIf { it }?.let {
        ProcessHandle.current().info().command().getOrNull()?.toPath()?.parent?.let { base ->
            Factory(Path(base, dataStoreFileName).toString())
        }
    } ?: Factory()

    val appContainer = AppContainer(factory)
    val viewModelOwner = remember { ViewModelOwner(appContainer) }
    val windowState = rememberWindowState(width = 800.dp, height = 600.dp)

    var currentIconResource by remember { mutableStateOf(Res.drawable.ic_launcher) }


    if (currentPlatform().isWindows()) {
        CustomWindow(
            state = windowState,
            viewModel = viewModelOwner.timeFlowViewModel,
            defaultIcon = painterResource(currentIconResource),
            onCloseRequest = ::exitApplication
        ) {
            window.minimumSize = Dimension(480, 540)
            val isDark by LocalThemeIsDark.current
            LaunchedEffect(isDark) {
                currentIconResource =
                    if (isDark) Res.drawable.icon_rounded_dark else Res.drawable.icon_rounded_light
            }
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
            AppContent(viewModel = viewModelOwner.timeFlowViewModel).invoke()
        }
    } else {
        Window(
            title = BuildConfig.APP_NAME,
            state = windowState,
            icon = painterResource(currentIconResource),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(480, 540)
            val isDark by LocalThemeIsDark.current
            LaunchedEffect(isDark) {
                currentIconResource =
                    if (isDark) Res.drawable.ic_launcher_night else Res.drawable.ic_launcher
            }
            if (currentPlatform().isMacOS()) {
                SideEffect {
                    // https://www.formdev.com/flatlaf/macos/
                    window.rootPane.putClientProperty("apple.awt.application.appearance", "system")
                    window.rootPane.putClientProperty("apple.awt.fullscreenable", true)
                    window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
                    window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                    window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                }
            }
            App(viewModel = viewModelOwner.timeFlowViewModel)
        }
    }
}

