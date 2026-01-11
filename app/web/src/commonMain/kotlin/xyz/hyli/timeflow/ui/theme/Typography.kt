/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

internal val webTypography: Typography
    @Composable
    get() {
        val fontFamily = NotoSansSC
        return MaterialTheme.typography.let {
            it.copy(
                displayLarge = it.displayLarge.copy(fontFamily = fontFamily),
                displayMedium = it.displayMedium.copy(fontFamily = fontFamily),
                displaySmall = it.displaySmall.copy(fontFamily = fontFamily),
                headlineLarge = it.headlineLarge.copy(fontFamily = fontFamily),
                headlineMedium = it.headlineMedium.copy(fontFamily = fontFamily),
                headlineSmall = it.headlineSmall.copy(fontFamily = fontFamily),
                titleLarge = it.titleLarge.copy(fontFamily = fontFamily),
                titleMedium = it.titleMedium.copy(fontFamily = fontFamily),
                titleSmall = it.titleSmall.copy(fontFamily = fontFamily),
                bodyLarge = it.bodyLarge.copy(fontFamily = fontFamily),
                bodyMedium = it.bodyMedium.copy(fontFamily = fontFamily),
                bodySmall = it.bodySmall.copy(fontFamily = fontFamily),
                labelLarge = it.labelLarge.copy(fontFamily = fontFamily),
                labelMedium = it.labelMedium.copy(fontFamily = fontFamily),
                labelSmall = it.labelSmall.copy(fontFamily = fontFamily),
            )
        }
    }