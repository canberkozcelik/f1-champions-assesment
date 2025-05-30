package com.f1champions.feature.racewinners.ui

import com.f1champions.core.ui.components.ErrorType
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