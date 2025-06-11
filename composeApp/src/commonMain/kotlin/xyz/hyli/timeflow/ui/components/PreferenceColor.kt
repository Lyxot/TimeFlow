package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.preference_color_dialog_title
import timeflow.composeapp.generated.resources.preference_color_tab_custom
import timeflow.composeapp.generated.resources.preference_color_tab_presets

// ==================== Preference Color ====================

@Composable
fun PreferenceColor(
    value: Color,
    onValueChange: (Color) -> Unit,
    title: String,
    subtitle: String? = null,
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled
) {
    val isEnabled by enabled.asState()

    var showDialog by remember { mutableStateOf(false) }

    BasePreference(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        visible = visible,
        onClick = if (isEnabled) {
            { showDialog = true }
        } else null,
        leadingContent = null,
        trailingContent = {
            ColorButton(
                baseColor = value,
                selected = false,
                size = 32.dp,
                enabled = false,
                cardColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    )

    if (showDialog) {
        ColorPickerDialog(
            initialColor = value,
            onColorSelected = { selectedColor ->
                onValueChange(selectedColor)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    val dialogState = rememberDialogState()

    // Dialog内部状态
    val colorState = remember { mutableStateOf(ColorPage.Presets) }

    LaunchedEffect(Unit) {
        dialogState.show()
    }

    if (dialogState.visible) {
        MyDialog(
            state = dialogState,
            title = { Text(stringResource(Res.string.preference_color_dialog_title)) },
            buttons = DialogDefaults.buttons(
                positive = DialogButton(stringResource(Res.string.confirm)),
                negative = DialogButton(stringResource(Res.string.cancel))
            ),
            onEvent = { event ->
                when (event) {
                    is DialogEvent.Button -> {
                        if (event.isPositiveButton) {
                            onColorSelected(selectedColor)
                        } else {
                            onDismiss()
                        }
                    }

                    DialogEvent.Dismissed -> onDismiss()
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 标题选项卡
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (colorState.value == ColorPage.Custom) .5f else 1f),
                        onClick = {
                            colorState.value = ColorPage.Presets
                        }
                    ) {
                        Text(text = stringResource(Res.string.preference_color_tab_presets))
                    }
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (colorState.value == ColorPage.Presets) .5f else 1f),
                        onClick = { colorState.value = ColorPage.Custom }
                    ) {
                        Text(text = stringResource(Res.string.preference_color_tab_custom))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 内容区域
                ColorContent(
                    color = remember { mutableStateOf(selectedColor) },
                    colorState = colorState,
                    onColorChange = { selectedColor = it }
                )
            }
        }
    }
}

private enum class ColorPage {
    Custom,
    Presets
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ColorContent(
    color: MutableState<Color>,
    colorState: MutableState<ColorPage>,
    onColorChange: (Color) -> Unit
) {
    val density = LocalDensity.current

    Column(modifier = Modifier.animateContentSize()) {
        Crossfade(
            targetState = colorState.value,
            label = "color"
        ) { page ->
            when (page) {
                ColorPage.Custom -> {
                    Column {
                        // 颜色预览
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box {
                                Canvas(
                                    modifier = Modifier
                                        .height(64.dp)
                                        .aspectRatio(1f)
                                        .clip(MaterialTheme.shapes.small)
                                ) {
                                    ColorUtil.drawCheckerboard(this, density)
                                }
                                Spacer(
                                    modifier = Modifier
                                        .height(64.dp)
                                        .aspectRatio(1f)
                                        .background(color.value, MaterialTheme.shapes.small)
                                )
                            }

                            ColorButton(
                                baseColor = color.value,
                                selected = false,
                                cardColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                enabled = false
                            )

                            Card {
                                Column(
                                    modifier = Modifier
                                        .padding(all = 8.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "RGB",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "#" + color.value.toArgb().toHexString()
                                            .substring(2),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        ColorSlider("R", color.value.red) {
                            color.value = color.value.copy(red = it)
                            onColorChange(color.value)
                        }
                        ColorSlider("G", color.value.green) {
                            color.value = color.value.copy(green = it)
                            onColorChange(color.value)
                        }
                        ColorSlider("B", color.value.blue) {
                            color.value = color.value.copy(blue = it)
                            onColorChange(color.value)
                        }
                    }
                }

                ColorPage.Presets -> {
                    FlowRow(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ColorDefinitions.COLORS.forEach { it ->
                            ColorButton(
                                onClick = {
                                    color.value = it
                                    onColorChange(it)
                                },
                                baseColor = it,
                                selected = color.value == it,
                                modifier = Modifier.padding(4.dp),
                                cardColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (value: Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(16.dp),
            textAlign = TextAlign.Center
        )
        Slider(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onValueChange
        )
        Text(
            text = (255f * value).toInt().toString(),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

// ==================== 颜色定义和工具类 ====================

private object ColorDefinitions {
    val COLORS_RED = Color(0xFFF44336)
    val COLORS_PINK = Color(0xFFE91E63)
    val COLORS_PURPLE = Color(0xFF9C27B0)
    val COLORS_DEEP_PURPLE = Color(0xFF673AB7)
    val COLORS_INDIGO = Color(0xFF3F51B5)
    val COLORS_BLUE = Color(0xFF2196F3)
    val COLORS_LIGHT_BLUE = Color(0xFF03A9F4)
    val COLORS_CYAN = Color(0xFF00BCD4)
    val COLORS_TEAL = Color(0xFF009688)
    val COLORS_GREEN = Color(0xFF4CAF50)
    val COLORS_LIGHT_GREEN = Color(0xFF8BC34A)
    val COLORS_LIME = Color(0xFFCDDC39)
    val COLORS_YELLOW = Color(0xFFFFEB3B)
    val COLORS_AMBER = Color(0xFFFFC107)
    val COLORS_ORANGE = Color(0xFFFF9800)
    val COLORS_DEEP_ORANGE = Color(0xFFFF5722)
    val COLORS_BROWN = Color(0xFF795548)
    val COLORS_GRAY = Color(0xFF9E9E9E)
    val COLORS_BLUE_GRAY = Color(0xFF607D8B)

    val COLORS = listOf(
        COLORS_RED, COLORS_PINK, COLORS_PURPLE, COLORS_DEEP_PURPLE,
        COLORS_INDIGO, COLORS_BLUE, COLORS_LIGHT_BLUE, COLORS_CYAN,
        COLORS_TEAL, COLORS_GREEN, COLORS_LIGHT_GREEN, COLORS_LIME,
        COLORS_YELLOW, COLORS_AMBER, COLORS_ORANGE, COLORS_DEEP_ORANGE,
        COLORS_BROWN, COLORS_GRAY, COLORS_BLUE_GRAY
    )
}

private object ColorUtil {
    fun drawCheckerboard(drawScope: DrawScope, density: Density) {
        val pixelSize = with(density) { 4.dp.toPx().toInt() }
        val color1 = Color(0xFFC2C2C2)
        val color2 = Color(0xFFF3F3F3)
        val sizePixel = Size(pixelSize.toFloat(), pixelSize.toFloat())
        for (c in 0 until drawScope.size.width.toInt() step pixelSize) {
            for (r in 0 until drawScope.size.height.toInt() step pixelSize) {
                val color = if ((c / pixelSize + r / pixelSize) % 2 == 0) color1 else color2
                drawScope.drawRect(color, topLeft = Offset(c.toFloat(), r.toFloat()), sizePixel)
            }
        }
    }

}

@Composable
fun ColorButton(
    onClick: () -> Unit = { },
    baseColor: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enabled: Boolean = true,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    val containerSize by animateDpAsState(targetValue = if (selected) size / 80 * 28 else 0.dp)
    val iconSize by animateDpAsState(targetValue = if (selected) size / 80 * 16 else 0.dp)

    Surface(
        modifier = modifier
            .size(size)
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        onClick = onClick,
        enabled = enabled,
    ) {
        Box(Modifier.fillMaxSize()) {
            val colorScheme = dynamicColorScheme(
                seedColor = baseColor,
                isDark = false,
                isAmoled = true,
                style = PaletteStyle.Vibrant
            )
            val color1 = colorScheme.inversePrimary
            val color2 = colorScheme.primaryContainer
            val color3 = colorScheme.primary

            BoxWithConstraints(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .drawBehind { drawCircle(color1) }
                    .align(Alignment.Center),
            ) {
                Surface(
                    color = color2,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(maxWidth / 2),
                ) {}
                Surface(
                    color = color3,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(maxWidth / 2),
                ) {}
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .size(containerSize)
                        .drawBehind { drawCircle(containerColor) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize).align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}