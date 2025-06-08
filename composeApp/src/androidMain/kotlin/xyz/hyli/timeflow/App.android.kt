package xyz.hyli.timeflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

class AppActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge(
            // 透明状态栏
//            statusBarStyle = SystemBarStyle.auto(
//                android.graphics.Color.TRANSPARENT,
//                android.graphics.Color.TRANSPARENT,
//            ),
            // 透明导航栏
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        setContent {
            val app = LocalContext.current.applicationContext as TimeFlow
            val extras = remember(app) {
                val container = app.container
                TimeFlowViewModel.newCreationExtras(container)
            }
            val viewModel: TimeFlowViewModel = viewModel(
                factory = TimeFlowViewModel.Factory,
                extras = extras,
            )
            val size = calculateWindowSizeClass(this)
            App(
                viewModel = viewModel,
                windowSizeClass = size
            )
        }
    }
}
