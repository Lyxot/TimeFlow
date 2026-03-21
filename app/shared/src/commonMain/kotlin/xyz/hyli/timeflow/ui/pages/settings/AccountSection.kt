/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.ui.pages.settings.subpage.LoginDialog
import xyz.hyli.timeflow.ui.pages.settings.subpage.LogoutConfirmDialog
import xyz.hyli.timeflow.ui.pages.settings.subpage.RegisterDialog
import xyz.hyli.timeflow.ui.pages.settings.subpage.SyncConflictDialog
import xyz.hyli.timeflow.ui.sync.SyncManager
import xyz.hyli.timeflow.ui.sync.SyncStatus
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun PreferenceScope.AccountSection(viewModel: TimeFlowViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()

    var showLoginDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var registerError by remember { mutableStateOf<String?>(null) }

    // Conflict dialog
    val firstConflict = syncState.conflicts.firstOrNull()
    if (firstConflict != null) {
        SyncConflictDialog(
            conflict = firstConflict,
            onResolve = { resolution -> viewModel.resolveConflict(resolution) }
        )
    }

    if (showLoginDialog) {
        LoginDialog(
            onDismiss = {
                showLoginDialog = false
                loginError = null
            },
            onLogin = { email, password ->
                viewModel.login(email, password) { result ->
                    result.onSuccess {
                        showLoginDialog = false
                        loginError = null
                    }.onFailure {
                        loginError = it.message
                    }
                }
            },
            errorMessage = resolveErrorMessage(loginError)
        )
    }

    if (showRegisterDialog) {
        RegisterDialog(
            onDismiss = {
                showRegisterDialog = false
                registerError = null
            },
            onRegister = { username, email, password, code ->
                viewModel.register(username, email, password, code) { result ->
                    result.onSuccess {
                        showRegisterDialog = false
                        registerError = null
                    }.onFailure {
                        registerError = it.message
                    }
                }
            },
            errorMessage = resolveErrorMessage(registerError)
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            }
        )
    }

    PreferenceSection(
        title = stringResource(Res.string.settings_title_account)
    ) {
        if (isLoggedIn && userInfo != null) {
            BasePreference(
                title = userInfo!!.username,
                subtitle = userInfo!!.email,
            )
        }

        // Server endpoint
        val insecureWarning = stringResource(Res.string.settings_warning_insecure_http)
        PreferenceInputText(
            value = settings.apiEndpoint ?: "",
            onValueChange = { endpoint ->
                viewModel.updateApiEndpoint(endpoint.ifBlank { null })
            },
            title = stringResource(Res.string.settings_title_server_endpoint),
            subtitle =
                if (settings.apiEndpoint == null) stringResource(Res.string.settings_subtitle_server_endpoint)
                else null,
            dialogHint = { currentValue ->
                if (currentValue.startsWith("http://")) {
                    Text(
                        text = insecureWarning,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        )

        if (!isLoggedIn) {
            // Login button
            BasePreference(
                title = stringResource(Res.string.settings_title_login),
                subtitle = stringResource(Res.string.settings_subtitle_not_logged_in),
                onClick = { showLoginDialog = true }
            )
            // Register button
            BasePreference(
                title = stringResource(Res.string.settings_title_register),
                onClick = { showRegisterDialog = true }
            )
        } else {
            // Sync button
            val syncSubtitle = when (syncState.status) {
                SyncStatus.SYNCING -> stringResource(Res.string.settings_value_syncing)
                SyncStatus.ERROR -> stringResource(
                    Res.string.settings_value_sync_error,
                    resolveErrorMessage(syncState.error) ?: ""
                )

                else -> {
                    val lastSynced = syncState.lastSyncedAt ?: settings.syncedAt
                    if (lastSynced != null) {
                        val local = lastSynced.toLocalDateTime(TimeZone.currentSystemDefault())
                            .let { LocalDateTime(it.date, LocalTime(it.hour, it.minute, it.second)) }
                        val format =
                            LocalDateTime.Format { date(LocalDate.Formats.ISO); char(' '); time(LocalTime.Formats.ISO) }
                        stringResource(Res.string.settings_subtitle_sync_last, format.format(local))
                    } else {
                        stringResource(Res.string.settings_subtitle_sync_never)
                    }
                }
            }
            BasePreference(
                title = stringResource(Res.string.settings_title_sync),
                subtitle = syncSubtitle,
                onClick = { viewModel.sync() },
                enabled = Dependency.State(viewModel.syncState.collectAsState()) {
                    it.status != SyncStatus.SYNCING
                }
            ) {
                SyncIconButton(viewModel)
            }
            // Logout button
            BasePreference(
                title = stringResource(Res.string.settings_title_logout),
                onClick = { showLogoutDialog = true }
            )
        }
    }
}

@Composable
fun resolveErrorMessage(error: String?): String? {
    if (error == null) return null
    return when (error) {
        SyncManager.ERROR_INVALID_CREDENTIALS -> stringResource(Res.string.error_invalid_credentials)
        SyncManager.ERROR_TOO_MANY_REQUESTS -> stringResource(Res.string.error_too_many_requests)
        SyncManager.ERROR_EMAIL_ALREADY_EXISTS -> stringResource(Res.string.error_email_already_exists)
        SyncManager.ERROR_NOT_FOUND -> stringResource(Res.string.error_not_found)
        SyncManager.ERROR_INVALID_INPUT -> stringResource(Res.string.error_invalid_input, "")
        SyncManager.ERROR_UNAUTHORIZED -> stringResource(Res.string.error_unauthorized)
        SyncManager.ERROR_SERVER -> stringResource(Res.string.error_server)
        SyncManager.ERROR_NETWORK -> stringResource(Res.string.error_network)
        SyncManager.ERROR_API_NOT_CONFIGURED -> stringResource(Res.string.error_api_not_configured)
        else -> error
    }
}
