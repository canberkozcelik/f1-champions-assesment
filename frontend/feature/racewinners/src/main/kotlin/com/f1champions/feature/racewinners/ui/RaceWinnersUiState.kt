package com.f1champions.feature.racewinners.ui

import com.f1champions.domain.model.RaceWinnerInfo
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
     * Error state with an error message.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : RaceWinnersUiState
}