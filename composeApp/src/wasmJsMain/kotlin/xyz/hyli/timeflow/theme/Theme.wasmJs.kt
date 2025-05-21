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