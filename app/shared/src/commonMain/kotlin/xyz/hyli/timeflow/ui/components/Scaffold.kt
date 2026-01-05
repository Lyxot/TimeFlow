/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isMacOS
import xyz.hyli.timeflow.utils.isWindows

enum class TopAppBarType {
    Small,
    CenterAligned
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScaffold(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    ),
    topAppBarType: TopAppBarType = TopAppBarType.Small,
    topAppBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = WindowInsets(),
    content: @Composable (BoxScope.() -> Unit),
) {
    val navSuiteType by LocalNavSuiteType.current
    Scaffold(
        modifier = if (scrollBehavior != null) {
            modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            modifier
        },
        topBar = {
            val windowPaddingValues =
                if (currentPlatform().isMacOS() && navSuiteType in NavigationBarType) {
                    PaddingValues(top = 8.dp)
                } else if (currentPlatform().isWindows()) {
                    PaddingValues(top = 12.dp)
                } else {
                    PaddingValues(0.dp)
                }
            when (topAppBarType) {
                TopAppBarType.Small -> TopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = topAppBarColors,
                    scrollBehavior = scrollBehavior,
                    contentPadding = windowPaddingValues
                )

                TopAppBarType.CenterAligned -> CenterAlignedTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = topAppBarColors,
                    scrollBehavior = scrollBehavior,
                    contentPadding = windowPaddingValues
                )
            }
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = { paddingValues ->
            val targetColor by remember(scrollBehavior, topAppBarColors) {
                derivedStateOf {
                    val overlappingFraction = scrollBehavior?.state?.overlappedFraction ?: 0f
                    lerp(
                        topAppBarColors.containerColor,
                        topAppBarColors.scrolledContainerColor,
                        FastOutLinearInEasing.transform(if (overlappingFraction > 0.01f) 1f else 0f),
                    )
                }
            }
            val appBarContainerColor = animateColorAsState(targetColor)
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .ifThen(navSuiteType !in NavigationBarType) {
                        Modifier
                            .drawRoundedCornerBackground(
                                containerColor = appBarContainerColor.value,
                                cornerRadius = 16.dp,
                                topStart = true,
                                topEnd = false,
                                bottomStart = true,
                                bottomEnd = false
                            )
                    },
                content = content
            )
        }
    )
}
