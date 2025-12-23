/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.supportDynamicColor

val accentColorFlow = MutableStateFlow<Color?>(null)

@OptIn(ExperimentalCoroutinesApi::class)
val accentColor: Flow<Color>
    get() = accentColorFlow.flatMapLatest {
        flowOf(it ?: Color.Unspecified)
    }

@Composable
internal actual fun getColorScheme(
    isDark: Boolean,
    seedColor: Int,
    isDynamicColor: Boolean
): ColorScheme {
    val accentColor by accentColor.collectAsState(initial = Color.Unspecified)
    return if (currentPlatform().supportDynamicColor() && isDynamicColor && accentColor != Color.Unspecified) {
        rememberDynamicColorScheme(
            seedColor = accentColor, isDark = isDark, specVersion = ColorSpec.SpecVersion.SPEC_2025
        )
    } else {
        rememberDynamicColorScheme(
            seedColor = Color(seedColor),
            isDark = isDark,
            specVersion = ColorSpec.SpecVersion.SPEC_2025
        )
    }
}

@Composable
internal actual fun SystemAppearance(isDark: Boolean) {
}
