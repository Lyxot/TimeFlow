/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.localizedValidationMessages
import xyz.hyli.timeflow.utils.InputValidation
import xyz.hyli.timeflow.utils.InputValidation.codePointCount

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    errorMessage: String? = null,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val messages = localizedValidationMessages()

    val emailError = remember(email, messages) {
        if (email.isEmpty()) null else InputValidation.validateEmail(email, messages)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_title_login)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(Res.string.account_title_email)) },
                    placeholder = { Text(stringResource(Res.string.account_hint_email)) },
                    supportingText = emailError?.let { { Text(it) } },
                    isError = emailError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.account_title_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLogin(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank() && emailError == null
            ) {
                Text(stringResource(Res.string.settings_title_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onRegister: (username: String, email: String, password: String, code: String?) -> Unit,
    onSendCode: ((email: String) -> Unit)? = null,
    errorMessage: String? = null,
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val messages = localizedValidationMessages()

    val usernameError = remember(username, messages) {
        if (username.isEmpty()) null else InputValidation.validateUsername(username, messages)
    }
    val emailError = remember(email, messages) {
        if (email.isEmpty()) null else InputValidation.validateEmail(email, messages)
    }
    val passwordError = remember(password, messages) {
        if (password.isEmpty()) null else InputValidation.validatePassword(password, messages)
    }
    val confirmPasswordError = remember(password, confirmPassword) {
        if (confirmPassword.isEmpty()) null
        else if (confirmPassword != password) "" // placeholder, actual message from string resource
        else null
    }

    val hasValidationError = usernameError != null || emailError != null ||
            passwordError != null || confirmPasswordError != null
    val allFieldsFilled = username.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && confirmPassword.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_title_register)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(Res.string.account_title_username)) },
                    suffix = {
                        Text(
                            "${username.codePointCount()}/${InputValidation.MAX_USERNAME_LENGTH}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    supportingText = usernameError?.let { { Text(it) } },
                    isError = usernameError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(Res.string.account_title_email)) },
                    placeholder = { Text(stringResource(Res.string.account_hint_email)) },
                    supportingText = emailError?.let { { Text(it) } },
                    isError = emailError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.account_title_password)) },
                    placeholder = {
                        Text(
                            stringResource(
                                Res.string.account_hint_password,
                                InputValidation.MIN_PASSWORD_LENGTH
                            )
                        )
                    },
                    supportingText = passwordError?.let { { Text(it) } },
                    isError = passwordError != null,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(Res.string.account_title_confirm_password)) },
                    placeholder = { Text(stringResource(Res.string.account_hint_confirm_password)) },
                    supportingText = if (confirmPasswordError != null) {
                        { Text(stringResource(Res.string.account_error_password_mismatch)) }
                    } else null,
                    isError = confirmPasswordError != null,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (onSendCode != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text(stringResource(Res.string.account_title_verification_code)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { onSendCode(email) },
                            enabled = email.isNotBlank() && emailError == null,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(stringResource(Res.string.account_button_send_code))
                        }
                    }
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRegister(username, email, password, code.ifBlank { null }) },
                enabled = allFieldsFilled && !hasValidationError
            ) {
                Text(stringResource(Res.string.settings_title_register))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_title_logout)) },
        text = { Text(stringResource(Res.string.account_subtitle_confirm_logout)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.settings_title_logout))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
