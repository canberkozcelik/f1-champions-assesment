package com.f1champions.feature.racewinners.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1champions.core.ui.components.ErrorType
import com.f1champions.domain.exception.*
import com.f1champions.domain.repository.F1Repository
import com.f1champions.feature.racewinners.mapper.RaceWinnerMapper
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
            } catch (e: F1NetworkException) {
                _uiState.update { 
                    RaceWinnersUiState.Error(
                        errorType = if (e.isOffline) ErrorType.OFFLINE else ErrorType.TIMEOUT,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: F1InvalidSeasonException) {
                _uiState.update { 
                    RaceWinnersUiState.Error(
                        errorType = ErrorType.INVALID,
                        message = e.message,
                        canRetry = false
                    )
                }
            } catch (e: F1SeasonNotFoundException) {
                _uiState.update { 
                    RaceWinnersUiState.Error(
                        errorType = ErrorType.NOT_FOUND,
                        message = e.message,
                        canRetry = false
                    )
                }
            } catch (e: F1RateLimitException) {
                _uiState.update { 
                    RaceWinnersUiState.Error(
                        errorType = ErrorType.RATE_LIMIT,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: F1ServerException) {
                _uiState.update { 
                    RaceWinnersUiState.Error(
                        errorType = ErrorType.SERVER_ERROR,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: F1Exception) {
                _uiState.update { 
                    RaceWinnersUiState.Error(
                        errorType = ErrorType.UNEXPECTED,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    RaceWinnersUiState.Error(
                        errorType = ErrorType.UNEXPECTED,
                        message = "An unexpected error occurred",
                        canRetry = true
                    )
                }
            }
        }
    }

    /**
     * Retries loading race winners after an error.
     *
     * @param year The championship year to retry loading
     */
    fun retry(year: Int) {
        loadRaceWinners(year)
    }
}