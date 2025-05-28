package com.f1champions.feature.racewinners.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1champions.domain.repository.F1Repository
import com.f1champions.feature.racewinners.mapper.RaceWinnerMapper
import com.f1champions.feature.racewinners.model.RaceWinner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Race Winners screen.
 * Manages the screen's state and business logic, including:
 * - Loading race winners for a specific year
 * - Transforming domain models to UI models
 * - Handling loading and error states
 */
@HiltViewModel
class RaceWinnersViewModel @Inject constructor(
    private val repository: F1Repository,
    private val mapper: RaceWinnerMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<RaceWinnersUiState>(RaceWinnersUiState.Loading)
    val uiState: StateFlow<RaceWinnersUiState> = _uiState.asStateFlow()

    /**
     * Loads race winners for the specified year.
     * Updates the UI state based on the result.
     *
     * @param year The championship year to load race winners for
     */
    fun loadRaceWinners(year: Int) {
        viewModelScope.launch {
            _uiState.value = RaceWinnersUiState.Loading
            try {
                val raceWinners = repository.getRaceWinners(year)
                _uiState.update { 
                    RaceWinnersUiState.Success(mapper.toUiModels(raceWinners))
                }
            } catch (e: Exception) {
                _uiState.update { 
                    RaceWinnersUiState.Error(e.message ?: "Failed to load race winners")
                }
            }
        }
    }
}