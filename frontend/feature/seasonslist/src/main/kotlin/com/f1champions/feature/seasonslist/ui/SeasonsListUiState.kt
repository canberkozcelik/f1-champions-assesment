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
     * @property message Error message to display to the user
     */
    data class Error(
        val message: String
    ) : SeasonsListUiState()
} 