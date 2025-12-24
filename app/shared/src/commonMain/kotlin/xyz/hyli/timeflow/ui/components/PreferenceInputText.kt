/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.cancel
import xyz.hyli.timeflow.shared.generated.resources.confirm

// ==================== Preference Input Text ====================

@Composable
fun PreferenceInputText(
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    subtitle: String? = null,
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled,
    validator: DialogInputValidator? = null
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
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    )

    if (showDialog) {
        TextInputDialog(
            title = title,
            initialValue = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                showDialog = false
            },
            onDismiss = { showDialog = false },
            validator = validator
        )
    }
}

@Composable
fun TextInputDialog(
    title: String,
    initialValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    validator: DialogInputValidator? = null
) {
    var textValue by remember { mutableStateOf(initialValue) }
    var isValid by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val dialogState = rememberDialogState()

    LaunchedEffect(Unit) {
        dialogState.show()
    }

    LaunchedEffect(textValue) {
        validator?.let { v ->
            when (val result = v.validate(textValue)) {
                is DialogInputValidator.Result.Valid -> {
                    isValid = true
                    errorMessage = ""
                }

                is DialogInputValidator.Result.Error -> {
                    isValid = false
                    errorMessage = result.errorText
                }
            }
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
                            onValueChange(textValue)
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValid) onValueChange(textValue)
                        }
                    )
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