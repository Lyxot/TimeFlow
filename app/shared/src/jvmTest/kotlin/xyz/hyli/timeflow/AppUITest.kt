/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.viewmodel.FakeDataRepository

class AppUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TimeFlowViewModel
    private lateinit var fakeRepository: FakeDataRepository

    @Before
    fun setUp() {
        fakeRepository = FakeDataRepository()
        viewModel = TimeFlowViewModel(fakeRepository)
    }

    @Test
    fun testInitialScreen() {
        composeTestRule.setContent {
            App(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("ScheduleNavItem").assertExists()
    }
}
