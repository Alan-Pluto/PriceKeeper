package com.pricekeeper.app.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

/**
 * Basic UI snapshot/behavior test for HomeScreen.
 * Run on device/emulator: ./gradlew connectedAndroidTest
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysManualEntryCard() {
        var manualClicked = false

        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(isLoading = false),
                onNavigateToManual = { manualClicked = true }
            )
        }

        composeTestRule.onNodeWithText("手动记一笔").assertIsDisplayed()
        composeTestRule.onNodeWithText("最近记录").assertIsDisplayed()
        composeTestRule.onNodeWithText("还没有记录").assertIsDisplayed()
    }

    @Test
    fun homeScreen_manualCard_clickTriggersNavigation() {
        var clicked = false

        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(isLoading = false),
                onNavigateToManual = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("手动记一笔").performClick()
        assert(clicked) { "Manual card click should trigger navigation" }
    }
}
