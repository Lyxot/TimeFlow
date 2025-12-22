/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
internal actual fun getColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        rememberDynamicColorScheme(seedColor = SeedColor, isDark = true, isAmoled = true, style = PaletteStyle.Vibrant)
    } else {
        rememberDynamicColorScheme(seedColor = SeedColor, isDark = false, isAmoled = true, style = PaletteStyle.Vibrant)
    }
}

@Composable
internal actual fun SystemAppearance(isDark: Boolean) {
}