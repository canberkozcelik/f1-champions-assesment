package com.f1champions.feature.racewinners.ui

import com.f1champions.feature.racewinners.model.RaceWinner

/**
 * UI state for the Race Winners screen.
 */
sealed interface RaceWinnersUiState {
    /**
     * Initial loading state.
     */
    data object Loading : RaceWinnersUiState

    /**
     * The screen has successfully loaded race winners.
     *
     * @property raceWinners The list of race winners to display
     */
    data class Success(
        val raceWinners: List<RaceWinner>
    ) : RaceWinnersUiState

    /**
     * Error state with detailed error information.
     *
     * @property errorType The type of error that occurred
     * @property message Error message to display
     * @property canRetry Whether the operation can be retried
     */
    data class Error(
        val errorType: ErrorType,
        val message: String,
        val canRetry: Boolean = true
    ) : RaceWinnersUiState
}

/**
 * Types of errors that can occur in the race winners screen.
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
     * Season not found
     */
    SEASON_NOT_FOUND,

    /**
     * Invalid season year
     */
    INVALID_SEASON,

    /**
     * Unexpected error occurred
     */
    UNEXPECTED
}