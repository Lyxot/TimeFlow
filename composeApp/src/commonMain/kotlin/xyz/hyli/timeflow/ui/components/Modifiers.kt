/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isMacOS
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun Modifier.ifThen(
    condition: Boolean,
    modifier: Modifier.Companion.() -> Modifier?
): Modifier {
    return if (condition) this.then(modifier(Modifier.Companion) ?: Modifier) else this
}

@Composable
fun Modifier.commonPadding(
    addNavigationBarHorizontalPadding: Boolean = true,
    addMacOSWindowPadding: Boolean = true,
    addBottomPadding: Boolean = true
): Modifier {
    val navSuiteType by LocalNavSuiteType.current
    return this
        .ifThen(navSuiteType in NavigationBarType && addNavigationBarHorizontalPadding) {
            Modifier.padding(horizontal = 8.dp)
        }
        .ifThen(currentPlatform().isMacOS() && addMacOSWindowPadding) {
            Modifier.padding(vertical = 16.dp)
        }
        .ifThen(addBottomPadding) {
            Modifier.padding(
                bottom = maxOf(
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                    24.dp
                )
            )
        }
}