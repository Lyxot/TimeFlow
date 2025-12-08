/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.harmonize
import com.materialkolor.ktx.lighten
import com.materialkolor.rememberDynamicColorScheme
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.preference_color_dialog_title
import timeflow.composeapp.generated.resources.preference_color_tab_custom
import timeflow.composeapp.generated.resources.preference_color_tab_presets
import xyz.hyli.timeflow.ui.components.WeightedGrid
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

// ==================== Preference Color ====================

@Composable
fun PreferenceColor(
    value: Color,
    onValueChange: (Color) -> Unit,
    alphaSupported: Boolean = false,
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
            ThemeColorButton(
                baseColor = value,
                selected = false,
                size = 32.dp,
                enabled = isEnabled,
                cardColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                onClick = {
                    showDialog = true
                }
            )
        }
    )

    if (showDialog) {
        ColorPickerDialog(
            initialColor = value,
            alphaSupported = alphaSupported,
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
    alphaSupported: Boolean = false,
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
                    alphaSupported = alphaSupported,
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
    alphaSupported: Boolean = false,
    onColorChange: (Color) -> Unit
) {
    Column(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
    ) {
        Crossfade(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            targetState = colorState.value,
            label = "color"
        ) { page ->
            when (page) {
                ColorPage.Custom -> {
                    ColorPicker(
                        color = color,
                        alphaSupported = alphaSupported,
                        style = ColorPickerStyle.THEME,
                        onColorChange = onColorChange
                    )
                }

                ColorPage.Presets -> {
                    WeightedGrid(
                        modifier = Modifier.fillMaxWidth(),
                        itemSize = 64.dp,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                        buttons = buildList<@Composable RowScope.() -> Unit> {
                            ColorDefinitions.COLORS.forEach { preset ->
                                add(
                                    @Composable {
                                        ThemeColorButton(
                                            onClick = {
                                                color.value = preset
                                                onColorChange(preset)
                                            },
                                            baseColor = preset,
                                            selected = color.value == preset,
                                            modifier = Modifier
                                                .weight(1f),
                                            cardColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}


// ==================== 颜色定义和工具类 ====================

object ColorDefinitions {
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

@Composable
fun ThemeColorButton(
    onClick: () -> Unit = { },
    baseColor: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enabled: Boolean = true,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    val isDark by LocalThemeIsDark.current
    val colorScheme = rememberDynamicColorScheme(
        seedColor = baseColor,
        isDark = isDark,
        isAmoled = !currentPlatform().isDesktop()
    )
    val color1 = colorScheme.primaryContainer
    val color2 = colorScheme.primaryContainer.let {
        if (isDark) it.lighten(1.3f)
        else it.darken(1.3f)
    }
    val color3 = colorScheme.primaryContainer.let {
        if (isDark) it.lighten(1.75f)
        else it.darken(1.75f)
    }

    ColorButton(
        onClick = onClick,
        color = color1,
        selected = selected,
        modifier = modifier,
        size = size,
        enabled = enabled,
        cardColor = cardColor,
        containerColor = containerColor
    ) {
        Surface(
            color = color2,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(maxWidth / 2),
        ) { }
        Surface(
            color = color3,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(maxWidth / 2),
        ) { }
    }
}

@Composable
fun ColorButton(
    onClick: () -> Unit = { },
    color: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enabled: Boolean = true,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable @UiComposable (BoxWithConstraintsScope.() -> Unit) = { } // Placeholder for custom content
) = ColorButtonContent(
    selected = selected,
    size = size,
    modifier = modifier,
    cardColor = cardColor,
    containerColor = containerColor,
    onClick = onClick,
    enabled = enabled,
    onDraw = {
        drawCircle(color)
    },
    content = content
)

@Composable
fun CustomColorButton(
    onClick: () -> Unit = { },
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enabled: Boolean = true,
    paletteColors: List<Color> = listOf(
        Color.Red,
        Color.Magenta,
        Color.Blue,
        Color.Cyan,
        Color.Green,
        Color.Yellow,
        Color.Red // 再次添加红色以形成无缝循环
    ),
    content: @Composable @UiComposable (BoxWithConstraintsScope.() -> Unit) = { } // Placeholder for custom content
) {
    val harmonizedPalette = paletteColors.map {
        it.harmonize(MaterialTheme.colorScheme.secondaryContainer, true)
    }
    val sweepGradientBrush = Brush.sweepGradient(colors = harmonizedPalette)
    ColorButtonContent(
        selected = selected,
        size = size,
        modifier = modifier,
        cardColor = MaterialTheme.colorScheme.surfaceContainer,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        onClick = onClick,
        enabled = enabled,
        onDraw = {
            drawCircle(brush = sweepGradientBrush)
        },
        content = content
    )
}

@Composable
private fun ColorButtonContent(
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp,
    enabled: Boolean = true,
    cardColor: Color,
    containerColor: Color,
    onDraw: DrawScope.() -> Unit,
    content: @Composable @UiComposable (BoxWithConstraintsScope.() -> Unit)
) {
    val containerSize by animateDpAsState(targetValue = if (selected) size / 80 * 28 else 0.dp)
    val iconSize by animateDpAsState(targetValue = if (selected) size / 80 * 16 else 0.dp)

    Surface(
        modifier = modifier
            .size(size)
            .aspectRatio(1f),
        shape = CircleShape,
        color = cardColor,
        onClick = onClick,
        enabled = enabled,
    ) {
        Box(Modifier.fillMaxSize()) {
            BoxWithConstraints(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .drawBehind { onDraw() }
                    .align(Alignment.Center),
            ) {
                content()
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .size(containerSize)
                        .background(containerColor),
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

