/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.ai.ScheduleExtractor
import xyz.hyli.timeflow.data.AiProviderConfig
import xyz.hyli.timeflow.data.AiProviderFormat
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.Files.settingsFilePath
import xyz.hyli.timeflow.utils.Files.showFileInFileManager
import xyz.hyli.timeflow.utils.InputValidation
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop
import xyz.hyli.timeflow.utils.supportDynamicColor
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settingsState = viewModel.settings.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val uriHandler = LocalUriHandler.current
    val validationMessages = localizedValidationMessages()
    var showResetDialog by remember { mutableStateOf(false) }
    if (showResetDialog) {
        ResetAllConfirmDialog(
            isLoggedIn = isLoggedIn,
            onDismiss = { showResetDialog = false },
            onConfirm = {
                showResetDialog = false
                viewModel.resetAll()
            }
        )
    }
    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(
                text = stringResource(Res.string.page_settings),
                style = MaterialTheme.typography.titleLarge
            )
        }
    ) {
        PreferenceScreen(
            modifier = Modifier
                .fillMaxWidth()
                .bottomPadding()
        ) {
            // General Settings
            PreferenceSection(
                title = stringResource(Res.string.settings_title_general)
            ) {
                // Theme Settings
                val themeList = listOf(
                    stringResource(Res.string.settings_theme_system),
                    stringResource(Res.string.settings_theme_light),
                    stringResource(Res.string.settings_theme_dark)
                )
                PreferenceList(
                    style = PreferenceListStyle.Style.SegmentedButtons,
                    value = settings.themeMode,
                    onValueChange = {
                        viewModel.updateTheme(it)
                    },
                    items = ThemeMode.entries,
                    itemTextProvider = { themeList[ThemeMode.entries.indexOf(it)] },
                    title = stringResource(Res.string.settings_title_theme)
                )
                // Dynamic Color Settings
                val themeDynamicColorDependency =
                    Dependency.State(mutableStateOf(currentPlatform())) {
                        it.supportDynamicColor()
                    }
                PreferenceBool(
                    style = PreferenceBoolStyle.Style.Switch,
                    value = settings.themeDynamicColor,
                    onValueChange = {
                        viewModel.updateThemeDynamicColor(it)
                    },
                    title = stringResource(Res.string.settings_title_theme_dynamic_color),
                    subtitle = stringResource(Res.string.settings_subtitle_theme_dynamic_color),
                    visible = themeDynamicColorDependency,
                )
                // Theme Color Settings
                if (!settings.themeDynamicColor || !currentPlatform().supportDynamicColor()) {
                    PreferenceColor(
                        value = Color(settings.themeColor),
                        onValueChange = {
                            viewModel.updateThemeColor(it.toArgb())
                        },
                        title = stringResource(Res.string.settings_title_theme_color)
                    )
                }
                // Current selected schedule settings
                val selectedScheduleDependency = Dependency.State(settingsState) {
                    !it.isScheduleEmpty
                }
                BasePreference(
                    trailingContent = {
                        Text(
                            text = settings.selectedSchedule?.name ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    title = stringResource(Res.string.settings_title_selected_schedule),
                    subtitle =
                        if (settings.isScheduleEmpty)
                            stringResource(Res.string.settings_subtitle_schedule_empty)
                        else
                            null,
                    enabled = selectedScheduleDependency,
                    onClick = {
                        navHostController.navigate(Destination.Schedule.ScheduleList)
                    }
                )
                // Add New Schedule
                PreferenceInputText(
                    value = "",
                    onValueChange = { newScheduleName ->
                        if (newScheduleName.isNotEmpty()) {
                            val newSchedule = Schedule(
                                name = newScheduleName
                            )
                            viewModel.createSchedule(
                                newSchedule
                            )
                        }
                    },
                    title = stringResource(Res.string.settings_title_create_schedule),
                    subtitle = stringResource(Res.string.settings_subtitle_create_schedule),
                    validator = rememberDialogInputValidator(
                        validate = {
                            val error = InputValidation.validateName(it, messages = validationMessages)
                            if (error == null)
                                DialogInputValidator.Result.Valid
                            else
                                DialogInputValidator.Result.Error(error)
                        }
                    ),
                    maxLength = InputValidation.MAX_NAME_LENGTH
                )
            }
            PreferenceDivider()
            // Schedule Settings
            val scheduleDependency = Dependency.State(settingsState) {
                it.isScheduleSelected
            }
            PreferenceSection(
                title = stringResource(Res.string.settings_title_schedule),
                subtitle =
                    if (settings.isScheduleSelected)
                        null
                    else if (settings.isScheduleEmpty)
                        stringResource(Res.string.settings_subtitle_schedule_empty)
                    else
                        stringResource(Res.string.settings_subtitle_schedule_not_selected),
                enabled = scheduleDependency
            ) {
                val schedule by viewModel.selectedSchedule.collectAsState()
                if (schedule != null) {
                    ScheduleSettingsContent(
                        schedule = schedule!!,
                        onScheduleChanged = { viewModel.updateSchedule(schedule = it) },
                        lessonsPerDayContent = {
                            BasePreference(
                                title = stringResource(Res.string.settings_title_schedule_lessons_per_day),
                                subtitle = stringResource(Res.string.settings_subtitle_schedule_lessons_per_day),
                                onClick = {
                                    navHostController.navigate(Destination.Settings.LessonsPerDay)
                                },
                                enabled = scheduleDependency
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                                    contentDescription = null
                                )
                            }
                        },
                        courseListContent = {
                            BasePreference(
                                title = stringResource(Res.string.settings_title_course_list),
                                subtitle = stringResource(Res.string.settings_subtitle_course_list),
                                onClick = {
                                    navHostController.navigate(Destination.Settings.CourseList)
                                },
                                enabled = scheduleDependency
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            }
            PreferenceDivider()
            // Account & Sync
            AccountSection(viewModel)
            PreferenceDivider()
            // Advanced - AI Provider
            PreferenceSection(
                title = stringResource(Res.string.settings_title_advanced)
            ) {
                val aiConfig = settings.aiConfig ?: AiProviderConfig()
                PreferenceBool(
                    style = PreferenceBoolStyle.Style.Switch,
                    value = aiConfig.enabled,
                    onValueChange = {
                        viewModel.updateAiConfig(aiConfig.copy(enabled = it))
                    },
                    title = stringResource(Res.string.settings_title_ai_custom),
                    subtitle = stringResource(Res.string.settings_subtitle_ai_custom)
                )
                val aiEnabledDependency = Dependency.State(settingsState) {
                    (it.aiConfig ?: AiProviderConfig()).enabled
                }
                val providerLabels = mapOf(
                    AiProviderFormat.OPENAI to ("OpenAI" to ScheduleExtractor.OPENAI_URL),
                    AiProviderFormat.GOOGLE to ("Google" to ScheduleExtractor.GOOGLE_URL),
                    AiProviderFormat.ANTHROPIC to ("Anthropic" to ScheduleExtractor.ANTHROPIC_URL),
                )
                PreferenceList(
                    style = PreferenceListStyle.Style.SegmentedButtons,
                    value = aiConfig.provider,
                    onValueChange = { newProvider ->
                        if (aiConfig.endpoint.isBlank()) {
                            viewModel.updateAiConfig(
                                aiConfig.copy(
                                    provider = newProvider,
                                    endpoint = providerLabels[newProvider]?.second ?: ""
                                )
                            )
                        } else {
                            viewModel.updateAiConfig(aiConfig.copy(provider = newProvider))
                        }
                    },
                    items = AiProviderFormat.entries,
                    itemTextProvider = { providerLabels[it]?.first ?: it.name },
                    title = stringResource(Res.string.settings_title_ai_provider),
                    visible = aiEnabledDependency
                )
                PreferenceInputText(
                    value = aiConfig.endpoint,
                    onValueChange = { newEndpoint ->
                        viewModel.updateAiConfig(aiConfig.copy(endpoint = newEndpoint))
                    },
                    title = stringResource(Res.string.settings_title_ai_endpoint),
                    subtitle = stringResource(
                        Res.string.settings_subtitle_ai_endpoint,
                        providerLabels[aiConfig.provider] ?: ""
                    ).takeIf { aiConfig.endpoint.isBlank() },
                    visible = aiEnabledDependency
                )
                PreferenceInputText(
                    value = if (aiConfig.apiKey.isNotBlank()) "••••••••" else "",
                    onValueChange = { newKey ->
                        viewModel.updateAiConfig(aiConfig.copy(apiKey = newKey))
                    },
                    title = stringResource(Res.string.settings_title_ai_api_key),
                    visible = aiEnabledDependency
                )
                PreferenceInputText(
                    value = aiConfig.model,
                    onValueChange = { newModel ->
                        viewModel.updateAiConfig(aiConfig.copy(model = newModel))
                    },
                    title = stringResource(Res.string.settings_title_ai_model),
                    subtitle = stringResource(Res.string.settings_subtitle_ai_model).takeIf { aiConfig.model.isBlank() },
                    visible = aiEnabledDependency
                )
            }
            PreferenceDivider()
            // Other
            PreferenceSection(
                title = stringResource(Res.string.settings_title_other)
            ) {
                BasePreference(
                    title = stringResource(Res.string.settings_title_about),
                    onClick = {
                        navHostController.navigate(Destination.Settings.About)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                        contentDescription = null
                    )
                }
                val configPathDependency =
                    Dependency.State(mutableStateOf(currentPlatform())) {
                        it.isDesktop()
                    }
                BasePreference(
                    title = stringResource(Res.string.settings_title_config_path),
                    subtitle = settingsFilePath,
                    onClick = {
                        settingsFilePath?.let { showFileInFileManager?.invoke(it) }
                    },
                    visible = configPathDependency
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = null
                    )
                }
                val urlDonate = stringResource(Res.string.url_donate)
                BasePreference(
                    title = stringResource(Res.string.settings_title_donate),
                    onClick = {
                        uriHandler.openUri(urlDonate)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = null
                    )
                }
                val urlFeedback = stringResource(Res.string.url_feedback)
                BasePreference(
                    title = stringResource(Res.string.settings_title_feedback),
                    onClick = {
                        uriHandler.openUri(urlFeedback)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = null
                    )
                }
                BasePreference(
                    title = stringResource(Res.string.settings_title_reset_all),
                    onClick = { showResetDialog = true }
                )
            }
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDark by LocalThemeIsDark.current
                Icon(
                    modifier = Modifier
                        .size(192.dp)
                        .clip(RoundedCornerShape(45.dp)),
                    imageVector = vectorResource(
                        if (isDark) Res.drawable.ic_launcher_night
                        else Res.drawable.ic_launcher
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
                Text(
                    text = BuildConfig.APP_NAME,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineLarge
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("${stringResource(Res.string.settings_value_version)}: ${BuildConfig.APP_VERSION_NAME}-${BuildConfig.GIT_COMMIT_HASH}")
                    Text(
                        text = "©️ ${
                            Instant.fromEpochMilliseconds(BuildConfig.BUILD_TIME).toLocalDateTime(
                                TimeZone.currentSystemDefault()
                            ).year
                        } ${BuildConfig.AUTHOR}"
                    )
                }
                val urlChangelog =
                    stringResource(Res.string.url_changelog, BuildConfig.APP_VERSION_NAME)
                ElevatedButton(
                    onClick = {
                        uriHandler.openUri(urlChangelog)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.settings_title_changelog))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetAllConfirmDialog(
    isLoggedIn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var remainingSeconds by remember { mutableStateOf(5) }
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            remainingSeconds--
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_title_reset_all)) },
        text = {
            Text(
                stringResource(
                    if (isLoggedIn) Res.string.settings_subtitle_confirm_reset_logged_in
                    else Res.string.settings_subtitle_confirm_reset
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = remainingSeconds == 0
            ) {
                Text(
                    text = if (remainingSeconds > 0)
                        "${stringResource(Res.string.settings_title_reset_all)} (${remainingSeconds}s)"
                    else
                        stringResource(Res.string.settings_title_reset_all),
                    color = if (remainingSeconds == 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
