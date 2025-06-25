package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

enum class ColorPickerStyle {
    THEME,
    COMMON
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ColorPicker(
    color: MutableState<Color>,
    alphaSupported: Boolean,
    style: ColorPickerStyle,
    showColor: Color = color.value,
    onColorChange: (Color) -> Unit
) {
    val density = LocalDensity.current
    LaunchedEffect(Unit) {
        if (!alphaSupported) {
            // 如果不支持 alpha，则将颜色的 alpha 设置为 1
            color.value = color.value.copy(alpha = 1f)
        }
    }
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
                    drawCheckerboard(density)
                }
                Spacer(
                    modifier = Modifier
                        .height(64.dp)
                        .aspectRatio(1f)
                        .background(color.value, MaterialTheme.shapes.small)
                )
            }

            when (style) {
                ColorPickerStyle.THEME -> {
                    ThemeColorButton(
                        baseColor = color.value,
                        selected = false,
                        cardColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        enabled = false
                    )
                }

                ColorPickerStyle.COMMON -> {
                    ColorButton(
                        color = showColor,
                        selected = false,
                        cardColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        enabled = false
                    )
                }
            }

            Card {
                Column(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = if (alphaSupported) "ARGB" else "RGB",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "#" + color.value.toArgb().toHexString().let {
                            if (alphaSupported) it
                            else it.substring(2)  // 去掉前两位的 alpha 值
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (alphaSupported) {
            ColorSlider("A", color.value.alpha) {
                color.value = color.value.copy(alpha = it)
                onColorChange(color.value)
            }
        }
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

private fun DrawScope.drawCheckerboard(density: Density) {
    val pixelSize = with(density) { 4.dp.toPx() }
    val colors = listOf(Color(0xFFC2C2C2), Color(0xFFF3F3F3))
    val sizePixel = Size(pixelSize, pixelSize)

    for (c in 0 until size.width.toInt() step pixelSize.toInt()) {
        for (r in 0 until size.height.toInt() step pixelSize.toInt()) {
            drawRect(
                color = colors[(c / pixelSize.toInt() + r / pixelSize.toInt()) % 2],
                topLeft = Offset(c.toFloat(), r.toFloat()),
                size = sizePixel
            )
        }
    }
}

