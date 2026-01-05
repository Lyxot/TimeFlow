/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modal(
    onKeyEvent: (KeyEvent) -> Boolean,
    content: @Composable () -> Unit
) = Dialog(
    onDismissRequest = {},
    properties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformInsets = false,
        useSoftwareKeyboardInset = false,
        usePlatformDefaultWidth = false,
        scrimColor = Color.Transparent
    ),
    content = {
        Box(Modifier.fillMaxSize().onKeyEvent(onKeyEvent)) {
            content()
        }
    }
)