package xyz.hyli.timeflow.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
internal actual fun SystemAppearance(isDark: Boolean) {
    val view = LocalView.current
    val backgroundColor = getColorScheme(isDark).background.toArgb()
    LaunchedEffect(isDark) {
//        val window = (view.context as Activity).window
//        WindowInsetsControllerCompat(window, window.decorView).apply {
//            isAppearanceLightStatusBars = !isDark
//            isAppearanceLightNavigationBars = !isDark
//        }
        (view.context as ComponentActivity).enableEdgeToEdge(
            navigationBarStyle = if (isDark) {
                SystemBarStyle.dark(backgroundColor)
            } else {
                SystemBarStyle.light(backgroundColor, 0)
            }
        )
    }
}
