package com.f1champions.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test

/**
 * Tests for the [LoadingIndicator] composable.
 */
class LoadingIndicatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingIndicator_isDisplayed() {
        // Given
        composeTestRule.setContent {
            LoadingIndicator()
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Loading indicator")
            .assertIsDisplayed()
    }
} 