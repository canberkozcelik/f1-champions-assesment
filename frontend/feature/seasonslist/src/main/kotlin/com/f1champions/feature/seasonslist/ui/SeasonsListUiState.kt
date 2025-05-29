package com.f1champions.feature.seasonslist.ui

import com.f1champions.domain.model.SeasonChampionInfo

/**
 * UI state for the seasons list screen.
 */
sealed class SeasonsListUiState {
    /**
     * Initial state when the screen is first loaded.
     */
    data object Initial : SeasonsListUiState()

    /**
     * State when the seasons list is being loaded.
     */
    data object Loading : SeasonsListUiState()

    /**
     * State when the seasons list has been successfully loaded.
     *
     * @property seasons List of season champions to display
     */
    data class Success(
        val seasons: List<SeasonChampionInfo>
    ) : SeasonsListUiState()

    /**
     * State when there's an error loading the seasons list.
     *
     * @property errorType The type of error that occurred
     * @property message Error message to display to the user
     * @property canRetry Whether the operation can be retried
     */
    data class Error(
        val errorType: ErrorType,
        val message: String,
        val canRetry: Boolean = true
    ) : SeasonsListUiState()
}

/**
 * Types of errors that can occur in the seasons list screen.
 */
enum class ErrorType {
    /**
     * Network is offline or unavailable
     */
    OFFLINE,

    /**
     * Connection timed out
     */
    TIMEOUT,

    /**
     * Server returned an error
     */
    SERVER_ERROR,

    /**
     * Rate limit exceeded
     */
    RATE_LIMIT,

    /**
     * Unexpected error occurred
     */
    UNEXPECTED
} 