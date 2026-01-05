/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.about_title_license_notice
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.NavigationBackIcon
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.components.navigationBarHorizontalPadding
import xyz.hyli.timeflow.utils.getLibrariesState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(navHostController: NavHostController) {
    val libraries by getLibrariesState()
    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(
                text = stringResource(Res.string.about_title_license_notice)
            )
        },
        navigationIcon = {
            NavigationBackIcon(navHostController)
        }
    ) {
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarHorizontalPadding(),
            footer = {
                item {
                    Spacer(Modifier.bottomPadding())
                }
            }
        )
    }
}
