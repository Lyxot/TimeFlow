/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import timeflow.app.generated.resources.Res
import timeflow.app.generated.resources.about_title_author
import timeflow.app.generated.resources.about_title_homepage
import timeflow.app.generated.resources.about_title_license
import timeflow.app.generated.resources.about_title_license_notice
import timeflow.app.generated.resources.about_title_source_code
import timeflow.app.generated.resources.about_value_description
import timeflow.app.generated.resources.about_value_download
import timeflow.app.generated.resources.ic_launcher
import timeflow.app.generated.resources.ic_launcher_night
import timeflow.app.generated.resources.settings_title_about
import timeflow.app.generated.resources.url_github_profile
import timeflow.app.generated.resources.url_github_repository
import timeflow.app.generated.resources.url_homepage
import timeflow.app.generated.resources.url_license
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.NavigationBackIcon
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.components.navigationBarHorizontalPadding
import xyz.hyli.timeflow.ui.navigation.SettingsDestination
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navHostController: NavHostController) {
    val isDark by LocalThemeIsDark.current
    val uriHandler = LocalUriHandler.current
    val urlHomepage = stringResource(Res.string.url_homepage)
    val urlProfile = stringResource(Res.string.url_github_profile)
    val urlSourceCode = stringResource(Res.string.url_github_repository)
    val urlLicense = stringResource(Res.string.url_license)
    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(stringResource(Res.string.settings_title_about))
        },
        navigationIcon = {
            NavigationBackIcon(navHostController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarHorizontalPadding()
                .bottomPadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(192.dp)
                        .clip(RoundedCornerShape(45.dp))
                        .align(Alignment.CenterHorizontally),
                    imageVector = vectorResource(
                        if (isDark) Res.drawable.ic_launcher_night
                        else Res.drawable.ic_launcher
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = BuildConfig.APP_NAME,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "©️ ${
                        Instant.fromEpochMilliseconds(BuildConfig.BUILD_TIME).toLocalDateTime(
                            TimeZone.currentSystemDefault()
                        ).year
                    } ${BuildConfig.AUTHOR}",
                )
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    shape = ButtonDefaults.shape,
                    onClick = {
                        uriHandler.openUri(urlHomepage)
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.url_homepage),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(Res.string.about_value_description)
                )
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(Res.string.about_value_download)
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.about_title_author))
                TextButton(
                    shape = ButtonDefaults.shape,
                    onClick = {
                        uriHandler.openUri(urlProfile)
                    }
                ) {
                    Text(
                        text = "@${BuildConfig.AUTHOR}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            TextButton(
                modifier = Modifier.padding(start = 4.dp),
                shape = ButtonDefaults.shape,
                onClick = {
                    uriHandler.openUri(urlHomepage)
                }
            ) {
                Text(
                    text = stringResource(Res.string.about_title_homepage),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(
                modifier = Modifier.padding(start = 4.dp),
                shape = ButtonDefaults.shape,
                onClick = {
                    uriHandler.openUri(urlSourceCode)
                }
            ) {
                Text(
                    text = stringResource(Res.string.about_title_source_code),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(
                modifier = Modifier.padding(start = 4.dp),
                shape = ButtonDefaults.shape,
                onClick = {
                    uriHandler.openUri(urlLicense)
                }
            ) {
                Text(
                    text = stringResource(Res.string.about_title_license),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(
                modifier = Modifier.padding(start = 4.dp),
                shape = ButtonDefaults.shape,
                onClick = {
                    navHostController.navigate(SettingsDestination.License.name)
                }
            ) {
                Text(
                    text = stringResource(Res.string.about_title_license_notice),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}