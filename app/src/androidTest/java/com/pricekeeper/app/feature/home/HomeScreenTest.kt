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
    fun homeScreen_displaysTwoEntryCards() {
        var receiptClicked = false
        var manualClicked = false

        composeTestRule.setContent {
            HomeScreen(
                onNavigateToReceipt = { receiptClicked = true },
                onNavigateToManual = { manualClicked = true }
            )
        }

        // Verify both cards are displayed
        composeTestRule.onNodeWithText("拍小票").assertIsDisplayed()
        composeTestRule.onNodeWithText("手动记").assertIsDisplayed()
        composeTestRule.onNodeWithText("OCR自动识别录入").assertIsDisplayed()
        composeTestRule.onNodeWithText("快速添加单品价格").assertIsDisplayed()
    }

    @Test
    fun homeScreen_receiptCard_clickTriggersNavigation() {
        var clicked = false

        composeTestRule.setContent {
            HomeScreen(
                onNavigateToReceipt = { clicked = true },
                onNavigateToManual = {}
            )
        }

        composeTestRule.onNodeWithText("拍小票").performClick()
        assert(clicked) { "Receipt card click should trigger navigation" }
    }

    @Test
    fun homeScreen_manualCard_clickTriggersNavigation() {
        var clicked = false

        composeTestRule.setContent {
            HomeScreen(
                onNavigateToReceipt = {},
                onNavigateToManual = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("手动记").performClick()
        assert(clicked) { "Manual card click should trigger navigation" }
    }
}
