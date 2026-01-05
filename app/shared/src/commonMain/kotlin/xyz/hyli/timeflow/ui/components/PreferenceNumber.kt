/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.*

// ==================== Preference Number ====================

sealed class PreferenceNumberStyle {
    data class Slider(
        val showTicks: Boolean = false,
        val showValueBelow: Boolean = false
    ) : PreferenceNumberStyle()

    data class TextField(val keyboardType: KeyboardType = KeyboardType.Number) :
        PreferenceNumberStyle()

    data object Wheel : PreferenceNumberStyle()
}

@Composable
fun PreferenceNumber(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    stepSize: Int = 1,
    title: String,
    subtitle: String? = null,
    style: PreferenceNumberStyle = PreferenceNumberStyle.Slider(),
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled
) {
    val isEnabled by enabled.asState()
    val isVisible by visible.asState()

    when (style) {
        is PreferenceNumberStyle.Slider -> {
            AnimatedVisibility(
                visible = isVisible,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 400f
                    )
                ) + fadeIn(
                    animationSpec = tween(250)
                ),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 400f
                    )
                ) + fadeOut(
                    animationSpec = tween(250)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 1.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = 400f
                            )
                        ),
                    shape = getPreferenceItemShape(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    val sliderAlphaValue by animateFloatAsState(
                        targetValue = if (isEnabled) 1f else 0.38f,
                        animationSpec = tween(200),
                        label = "slider_alpha_animation"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .alpha(sliderAlphaValue)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val sliderColors = if (style.showTicks) {
                            SliderDefaults.colors()
                        } else {
                            SliderDefaults.colors(
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            )
                        }

                        Slider(
                            value = value.toFloat(),
                            onValueChange = {
                                if (isEnabled) {
                                    val newValue = (it / stepSize).toInt() * stepSize
                                    onValueChange(newValue.coerceIn(min, max))
                                }
                            },
                            valueRange = min.toFloat()..max.toFloat(),
                            steps = ((max.toFloat() - min.toFloat()) / stepSize.toFloat() - 1).toInt(),
                            enabled = isEnabled,
                            colors = sliderColors
                        )

                        if (style.showValueBelow) {
                            Text(
                                text = value.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        is PreferenceNumberStyle.TextField -> {
            var showDialog by remember { mutableStateOf(false) }

            BasePreference(
                title = title,
                subtitle = subtitle,
                enabled = enabled,
                visible = visible,
                onClick = if (isEnabled) {
                    { showDialog = true }
                } else null,
                trailingContent = {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )

            if (showDialog) {
                NumberInputDialog(
                    title = title,
                    initialValue = value,
                    min = min,
                    max = max,
                    stepSize = stepSize,
                    keyboardType = style.keyboardType,
                    onValueChange = { newValue ->
                        onValueChange(newValue)
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }
        }

        is PreferenceNumberStyle.Wheel -> {
            var showDialog by remember { mutableStateOf(false) }

            BasePreference(
                title = title,
                subtitle = subtitle,
                enabled = enabled,
                visible = visible,
                onClick = if (isEnabled) {
                    { showDialog = true }
                } else null,
                trailingContent = {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )

            if (showDialog) {
                NumberWheelPickerDialog(
                    title = title,
                    initialValue = value,
                    min = min,
                    max = max,
                    stepSize = stepSize,
                    onValueChange = { newValue ->
                        onValueChange(newValue)
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}

@Composable
fun NumberInputDialog(
    title: String,
    initialValue: Int,
    min: Int,
    max: Int,
    stepSize: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Number,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(initialValue.toString()) }
    var isValid by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val dialogState = rememberDialogState()

    LaunchedEffect(Unit) {
        dialogState.show()
    }

    LaunchedEffect(textValue) {
        try {
            val numValue = textValue.toIntOrNull()
            if (numValue == null) {
                isValid = false
                errorMessage = getString(Res.string.preference_number_validation_invalid)
            } else if (numValue < min || numValue > max) {
                isValid = false
                errorMessage = getString(
                    Res.string.preference_number_validation_range,
                    min.toString(),
                    max.toString()
                )
            } else if (stepSize > 1 && (numValue - min) % stepSize != 0) {
                isValid = false
                errorMessage =
                    getString(Res.string.preference_number_validation_step, stepSize.toString())
            } else {
                isValid = true
                errorMessage = ""
            }
        } catch (_: Exception) {
            isValid = false
            errorMessage = getString(Res.string.preference_number_validation_invalid)
        }
    }

    // 动态更新按钮状态
    LaunchedEffect(isValid) {
        dialogState.enableButton(DialogButtonType.Positive, isValid)
    }

    if (dialogState.visible) {
        MyDialog(
            state = dialogState,
            title = { Text(title) },
            buttons = DialogDefaults.buttons(
                positive = DialogButton(stringResource(Res.string.confirm)),
                negative = DialogButton(stringResource(Res.string.cancel))
            ),
            onEvent = { event ->
                when (event) {
                    is DialogEvent.Button -> {
                        if (event.isPositiveButton && isValid) {
                            textValue.toIntOrNull()?.let { onValueChange(it) }
                        } else if (!event.isPositiveButton) {
                            onDismiss()
                        }
                    }

                    DialogEvent.Dismissed -> onDismiss()
                }
            }
        ) {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isValid,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValid) {
                                textValue.toIntOrNull()?.let { onValueChange(it) }
                            }
                        }
                    ),
                    label = {
                        Text(
                            stringResource(
                                Res.string.preference_number_dialog_label,
                                min.toString(),
                                max.toString()
                            )
                        )
                    }
                )

                if (!isValid && errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NumberWheelPickerDialog(
    title: String,
    initialValue: Int,
    min: Int,
    max: Int,
    stepSize: Int = 1,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogState = rememberDialogState()
    var selectedValue by remember { mutableStateOf(initialValue) }
    val numbers = remember { (min..max step stepSize).toList() }

    LaunchedEffect(Unit) {
        dialogState.show()
    }

    if (dialogState.visible) {
        MyDialog(
            state = dialogState,
            title = { Text(title) },
            buttons = DialogDefaults.buttons(
                positive = DialogButton(stringResource(Res.string.confirm)),
                negative = DialogButton(stringResource(Res.string.cancel))
            ),
            onEvent = { event ->
                when (event) {
                    is DialogEvent.Button -> {
                        if (event.isPositiveButton) {
                            onValueChange(selectedValue)
                        } else {
                            onDismiss()
                        }
                    }

                    DialogEvent.Dismissed -> onDismiss()
                }
            }
        ) {
            val initialIndex = numbers.indexOf(initialValue).coerceAtLeast(0)
            WheelPicker(
                data = numbers,
                selectIndex = initialIndex,
                visibleCount = 3,
                modifier = Modifier.height(120.dp),
                onSelect = { _, item ->
                    selectedValue = item
                }
            ) { item ->
                Text(
                    text = item.toString(),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}