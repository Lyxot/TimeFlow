package xyz.hyli.timeflow.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import xyz.hyli.timeflow.viewmodel.TimeFlowViewModel

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@Composable
internal fun AppTheme(
    viewModel: TimeFlowViewModel,
    content: @Composable () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    val isDarkState = remember(systemIsDark, uiState.theme) {
        if (uiState.theme != 1 && uiState.theme != 2) // 0: System, 1: Light, 2: Dark
            mutableStateOf(systemIsDark)
        else mutableStateOf(uiState.theme == 2)
    }
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState
    ) {
        val isDark by isDarkState
        SystemAppearance(!isDark)
        MaterialTheme(
            colorScheme = getColorScheme(isDark),
            content = { Surface(content = content) }
        )
    }
}

@Composable
internal expect fun getColorScheme(isDark: Boolean): ColorScheme

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
