/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@Composable
internal actual fun getColorScheme(
    isDark: Boolean,
    seedColor: Int,
    isDynamicColor: Boolean
): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDynamicColor) {
        val context = LocalContext.current
        if (isDark) {
            remember { dynamicDarkColorScheme(context) }
        } else {
            remember { dynamicLightColorScheme(context) }
        }
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
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val activity = view.context as ComponentActivity
        if (isDark) {
            activity.enableEdgeToEdge(
//                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            )
        } else {
            activity.enableEdgeToEdge(
//                    statusBarStyle = SystemBarStyle.light(
//                        android.graphics.Color.TRANSPARENT,
//                        android.graphics.Color.TRANSPARENT,
//                    ),
                navigationBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                )
            )
        }
    }
}
