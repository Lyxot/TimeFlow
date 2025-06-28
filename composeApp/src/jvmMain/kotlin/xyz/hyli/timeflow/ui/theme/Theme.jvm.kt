package xyz.hyli.timeflow.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme

@Composable
internal actual fun getColorScheme(
    isDark: Boolean,
    seedColor: Int,
    isDynamicColor: Boolean
): ColorScheme {
    return rememberDynamicColorScheme(
        seedColor = Color(seedColor), isDark = isDark, isAmoled = false
    )
}

@Composable
internal actual fun SystemAppearance(isDark: Boolean) {
}