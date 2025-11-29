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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.materialkolor.ktx.animateColorScheme
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AppTheme(
    viewModel: TimeFlowViewModel,
    content: @Composable () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val settings by viewModel.settings.collectAsState()
    val isDarkState = remember(systemIsDark, settings.theme) {
        if (settings.theme != 1 && settings.theme != 2) // 0: System, 1: Light, 2: Dark
            mutableStateOf(systemIsDark)
        else mutableStateOf(settings.theme == 2)
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
