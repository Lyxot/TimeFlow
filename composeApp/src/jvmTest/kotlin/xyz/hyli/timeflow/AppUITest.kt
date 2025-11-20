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
