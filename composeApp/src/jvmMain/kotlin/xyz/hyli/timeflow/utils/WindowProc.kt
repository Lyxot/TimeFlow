/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import com.sun.jna.CallbackReference
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinReg
import com.sun.jna.platform.win32.WinUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.Window

internal val windowProc = MutableStateFlow<BasicWindowProc?>(null)

internal open class BasicWindowProc(
    private val user32: User32,
    window: Window,
) : WinUser.WindowProc, AutoCloseable {
    val WM_SETTINGCHANGE: Int = 0x001A
    protected val windowHandle: WinDef.HWND = WinDef.HWND(
        (window as? ComposeWindow)
            ?.let { Pointer(it.windowHandle) }
            ?: Native.getWindowPointer(window),
    )

    private val _accentColor: MutableStateFlow<Color> = MutableStateFlow(currentAccentColor())
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    private val defaultWindowProc =
        user32.SetWindowLongPtr(
            windowHandle,
            WinUser.GWL_WNDPROC,
            CallbackReference.getFunctionPointer(this),
        )

    override fun callback(
        hwnd: WinDef.HWND,
        uMsg: Int,
        wParam: WinDef.WPARAM,
        lParam: WinDef.LPARAM
    ): WinDef.LRESULT {
        if (uMsg == WM_SETTINGCHANGE) {
            val changedKey = Pointer(lParam.toLong()).getWideString(0)
            // Theme changed for color and darkTheme
            if (changedKey == "ImmersiveColorSet") {
                _accentColor.tryEmit(currentAccentColor())
            }
        }
        return callDefWindowProc(hwnd, uMsg, wParam, lParam)
    }

    private fun callDefWindowProc(
        hwnd: WinDef.HWND,
        uMsg: Int,
        wParam: WinDef.WPARAM,
        lParam: WinDef.LPARAM
    ): WinDef.LRESULT {
        return user32.CallWindowProc(defaultWindowProc, hwnd, uMsg, wParam, lParam)
    }

    private fun currentAccentColor(): Color {
        val value = Advapi32Util.registryGetIntValue(
            WinReg.HKEY_CURRENT_USER,
            "SOFTWARE\\Microsoft\\Windows\\DWM",
            "AccentColor",
        ).toLong()
        val alpha = (value and 0xFF000000)
        val green = (value and 0xFF).shl(16)
        val blue = (value and 0xFF00)
        val red = (value and 0xFF0000).shr(16)
        return Color((alpha or green or blue or red).toInt())
    }

    override fun close() {
        user32.SetWindowLongPtr(windowHandle, WinUser.GWL_WNDPROC, defaultWindowProc)
    }
}