package xyz.hyli.timeflow

import TimeFlow.composeApp.BuildConfig
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable
import timeflow.composeapp.generated.resources.*
import xyz.hyli.timeflow.theme.AppTheme
import xyz.hyli.timeflow.theme.LocalThemeIsDark
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
internal fun App() = AppTheme {
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
            var isDark by LocalThemeIsDark.current
            val icon = remember(isDark) {
                if (isDark) Res.drawable.ic_light_mode
                else Res.drawable.ic_dark_mode
            }

            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
                onClick = { isDark = !isDark },
                content = {
                    Icon(vectorResource(icon), contentDescription = null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(Res.string.theme))
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
