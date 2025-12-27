/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.materialkolor.ktx.animateColorScheme
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    viewModel: TimeFlowViewModel,
    typography: Typography? = null,
    content: @Composable () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val settings by viewModel.settings.collectAsState()
    val isDarkState = remember(systemIsDark, settings.themeMode) {
        if (settings.themeMode == ThemeMode.SYSTEM) // 0: System, 1: Light, 2: Dark
            mutableStateOf(systemIsDark)
        else mutableStateOf(settings.themeMode == ThemeMode.DARK)
    }
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState
    ) {
        val isDark by isDarkState
        val colorScheme = getColorScheme(isDark, settings.themeColor, settings.themeDynamicColor)
        SystemAppearance(!isDark)
        MaterialExpressiveTheme(
            colorScheme = animateColorScheme(
                colorScheme
            ),
            typography = typography,
            content = {
                Surface {
                    content()
                }
            }
        )
    }
}

@Composable
internal expect fun getColorScheme(
    isDark: Boolean,
    seedColor: Int,
    isDynamicColor: Boolean
): ColorScheme

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
