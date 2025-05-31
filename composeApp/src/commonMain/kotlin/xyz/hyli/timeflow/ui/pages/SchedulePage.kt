package xyz.hyli.timeflow.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import timeflow.composeapp.generated.resources.IndieFlower_Regular
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.compose_multiplatform
import timeflow.composeapp.generated.resources.cyclone
import timeflow.composeapp.generated.resources.greeting
import timeflow.composeapp.generated.resources.ic_cyclone
import timeflow.composeapp.generated.resources.ic_rotate_right
import timeflow.composeapp.generated.resources.open_github
import timeflow.composeapp.generated.resources.run
import timeflow.composeapp.generated.resources.stop
import timeflow.composeapp.generated.resources.theme
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.getPlatform
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun ScheduleScreen(
    viewModel: TimeFlowViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf(BuildConfig.APP_VERSION_NAME, BuildConfig.APP_VERSION_CODE, BuildConfig.BUILD_TIME).forEach {
            item {
                Text(it.toString())
            }
        }

        item {
            var showContent by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .adaptiveClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { showContent = !showContent }
                    ),
            ) {
                Text("Click me!")
            }

            AnimatedVisibility(showContent) {
                val platformName = remember { getPlatform().name }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: " + stringResource(Res.string.greeting, platformName))
                }
            }
        }

        item {
            Text(
                text = stringResource(Res.string.cyclone),
                fontFamily = FontFamily(Font(Res.font.IndieFlower_Regular)),
                style = MaterialTheme.typography.displayLarge
            )
        }

        item {
            var isRotating by remember { mutableStateOf(false) }
            val rotate = remember { Animatable(0f) }
            val target = 360f
            if (isRotating) {
                LaunchedEffect(Unit) {
                    while (isActive) {
                        val remaining = (target - rotate.value) / target
                        rotate.animateTo(target, animationSpec = tween((1_000 * remaining).toInt(), easing = LinearEasing))
                        rotate.snapTo(0f)
                    }
                }
            }

            Image(
                modifier = Modifier
                    .size(250.dp)
                    .padding(16.dp)
                    .run { rotate(rotate.value) },
                imageVector = vectorResource(Res.drawable.ic_cyclone),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = null
            )

            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = { isRotating = !isRotating },
                content = {
                    Icon(vectorResource(Res.drawable.ic_rotate_right), contentDescription = null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(if (isRotating) Res.string.stop else Res.string.run)
                    )
                }
            )
        }

        item {
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
                onClick = {
                    if (uiState.theme == 0) {
                        viewModel.setTheme(1) // Switch to light theme
                    } else if (uiState.theme == 1) {
                        viewModel.setTheme(2) // Switch to dark theme
                    } else {
                        viewModel.setTheme(0) // Switch to system theme
                    }
                },
                content = {
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(Res.string.theme))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        if (uiState.theme == 1) "Light"
                        else if (uiState.theme == 2) "Dark"
                        else "System"
                    )
                }
            )
        }

        item {
            val uriHandler = LocalUriHandler.current
            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
                onClick = { uriHandler.openUri("https://github.com/terrakok") },
            ) {
                Text(stringResource(Res.string.open_github))
            }
        }
    }
}