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
import androidx.compose.runtime.collectAsState
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
import timeflow.app.desktop.generated.resources.Res
import timeflow.app.desktop.generated.resources.icon_rounded_dark
import timeflow.app.desktop.generated.resources.icon_rounded_light
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.AppContent
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.datastore.dataStoreFileName
import xyz.hyli.timeflow.datastore.settingsFilePath
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.Factory
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import xyz.hyli.timeflow.ui.theme.accentColorFlow
import xyz.hyli.timeflow.ui.viewmodel.ViewModelOwner
import xyz.hyli.timeflow.utils.Files
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isMacOS
import xyz.hyli.timeflow.utils.isWindows
import xyz.hyli.timeflow.window.BasicWindowProc
import xyz.hyli.timeflow.window.CustomWindow
import xyz.hyli.timeflow.window.windowProc
import java.awt.Desktop
import java.awt.Dimension
import java.io.File
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

    var currentIconResource by remember { mutableStateOf(Res.drawable.icon_rounded_dark) }


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
            val windowProc by windowProc.collectAsState(initial = null)
            LaunchedEffect(windowProc) {
                accentColorFlow.tryEmit(
                    windowProc?.accentColor?.value
                )
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
                    if (isDark) Res.drawable.icon_rounded_dark else Res.drawable.icon_rounded_light
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
    Files.settingsFilePath = settingsFilePath
    Files.showFileInFileManager = { path ->
        val file = File(path)
        if (file.exists()) {
            try {
                Desktop.getDesktop().browseFileDirectory(file)
            } catch (e: Exception) {
                Desktop.getDesktop().open(file.parentFile)
            }
        }
    }
}

