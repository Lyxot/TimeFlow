/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.ui.theme.webTypography
import xyz.hyli.timeflow.ui.viewmodel.ViewModelOwner

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    val appContainer = AppContainer()
    ComposeViewport(body) {
        val viewModelOwner = remember { ViewModelOwner(appContainer) }
        App(
            viewModelOwner.timeFlowViewModel,
            webTypography
        )
    }
}