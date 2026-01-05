/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Suppress("unused", "FunctionName")
@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController(
    viewModel: TimeFlowViewModel
): UIViewController = ComposeUIViewController(
    configure = {
        parallelRendering = true
    }
) {
    App(
        viewModel = viewModel
    )
}
