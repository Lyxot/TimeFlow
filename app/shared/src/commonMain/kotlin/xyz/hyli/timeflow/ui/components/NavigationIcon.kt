/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.app.shared.generated.resources.Res
import timeflow.app.shared.generated.resources.back

@Composable
fun NavigationBackIcon(
    navHostController: NavHostController,
    onClick: (() -> Unit) = {
        navHostController.popBackStack()
    },
    imageVector: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack,
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(Res.string.back)
        )
    }
}
