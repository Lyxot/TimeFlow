/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.back
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.getLibrariesState
import xyz.hyli.timeflow.utils.isDesktop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(navHostController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (currentPlatform().isDesktop())
                    Modifier.padding(vertical = 16.dp)
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
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
        val libraries by getLibrariesState()
        LibrariesContainer(libraries, Modifier.fillMaxSize())
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
