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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.materialkolor.rememberDynamicColorScheme

@Composable
internal actual fun getColorScheme(isDark: Boolean, seedColor: Long, isDynamicColor: Boolean): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDynamicColor) {
        if (isDark) {
            dynamicDarkColorScheme(LocalContext.current)
        } else {
            dynamicLightColorScheme(LocalContext.current)
        }
    } else {
        rememberDynamicColorScheme(seedColor = Color(seedColor), isDark = isDark, isAmoled = true)
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
