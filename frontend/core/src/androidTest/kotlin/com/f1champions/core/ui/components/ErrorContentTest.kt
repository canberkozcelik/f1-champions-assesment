package com.f1champions.core.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * Tests for the [ErrorContent] composable.
 */
class ErrorContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorContent_displaysMessage() {
        // Given
        val errorMessage = "Test error message"
        
        // When
        composeTestRule.setContent {
            ErrorContent(
                errorType = ErrorType.UNEXPECTED,
                message = errorMessage,
                canRetry = false,
                onRetry = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText(errorMessage)
            .assertIsDisplayed()
    }

    @Test
    fun errorContent_withRetry_showsRetryButton() {
        // Given
        val errorMessage = "Test error message"
        var retryClicked = false
        
        // When
        composeTestRule.setContent {
            ErrorContent(
                errorType = ErrorType.UNEXPECTED,
                message = errorMessage,
                canRetry = true,
                onRetry = { retryClicked = true }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .performClick()

        assert(retryClicked) { "Retry button click was not handled" }
    }

    @Test
    fun errorContent_withoutRetry_hidesRetryButton() {
        // Given
        val errorMessage = "Test error message"
        
        // When
        composeTestRule.setContent {
            ErrorContent(
                errorType = ErrorType.UNEXPECTED,
                message = errorMessage,
                canRetry = false,
                onRetry = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Retry")
            .assertDoesNotExist()
    }
} 