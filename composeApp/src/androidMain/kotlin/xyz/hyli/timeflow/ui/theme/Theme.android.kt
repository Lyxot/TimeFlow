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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.materialkolor.rememberDynamicColorScheme

@Composable
internal actual fun getColorScheme(isDark: Boolean): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) {
            dynamicDarkColorScheme(LocalContext.current)
        } else {
            dynamicLightColorScheme(LocalContext.current)
        }
    } else {
        rememberDynamicColorScheme(seedColor = SeedColor, isDark = isDark, isAmoled = true)
    }
}

@Composable
internal actual fun SystemAppearance(isDark: Boolean, navigationBarColor: Color) {
    val view = LocalView.current
    LaunchedEffect(isDark, navigationBarColor) {
        (view.context as ComponentActivity).enableEdgeToEdge(
            navigationBarStyle = if (isDark) {
                SystemBarStyle.dark(navigationBarColor.toArgb())
            } else {
                SystemBarStyle.light(navigationBarColor.toArgb(), 0)
            }
        )
    }
}
