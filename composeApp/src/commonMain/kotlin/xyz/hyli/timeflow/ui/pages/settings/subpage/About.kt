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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.about_title_author
import timeflow.composeapp.generated.resources.about_title_homepage
import timeflow.composeapp.generated.resources.about_title_license
import timeflow.composeapp.generated.resources.about_title_license_notice
import timeflow.composeapp.generated.resources.about_title_source_code
import timeflow.composeapp.generated.resources.about_value_description
import timeflow.composeapp.generated.resources.about_value_download
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.ic_launcher
import timeflow.composeapp.generated.resources.ic_launcher_night
import timeflow.composeapp.generated.resources.settings_title_about
import timeflow.composeapp.generated.resources.url_github_profile
import timeflow.composeapp.generated.resources.url_github_repository
import timeflow.composeapp.generated.resources.url_homepage
import timeflow.composeapp.generated.resources.url_license
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.ui.navigation.SettingsDestination
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun AboutScreen(navHostController: NavHostController) {
    val isDark by LocalThemeIsDark.current
    val uriHandler = LocalUriHandler.current
    val urlHomepage = stringResource(Res.string.url_homepage)
    val urlProfile = stringResource(Res.string.url_github_profile)
    val urlSourceCode = stringResource(Res.string.url_github_repository)
    val urlLicense = stringResource(Res.string.url_license)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .then(
                if (currentPlatform().isDesktop())
                    Modifier.padding(vertical = 16.dp)
                else Modifier
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .then(
                        if (currentPlatform().isDesktop())
                            Modifier.padding(end = 16.dp)
                        else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            navHostController.popBackStack()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                }
                Text(stringResource(Res.string.settings_title_about))
                Spacer(Modifier.weight(1f))
            }

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
            Text(stringResource(Res.string.about_value_description))
            Text(stringResource(Res.string.about_value_download))
        }
        Row(
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
        Spacer(
            modifier = Modifier.height(
                maxOf(
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                    24.dp
                )
            )
        )
    }
}