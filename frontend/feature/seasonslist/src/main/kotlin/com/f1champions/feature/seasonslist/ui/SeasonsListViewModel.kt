package com.f1champions.feature.seasonslist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1champions.domain.exception.*
import com.f1champions.domain.usecase.GetSeasonChampionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the seasons list screen.
 * Manages the UI state and handles data fetching for the list of Formula 1 World Champions.
 */
@HiltViewModel
class SeasonsListViewModel @Inject constructor(
    private val getSeasonChampionsUseCase: GetSeasonChampionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SeasonsListUiState>(SeasonsListUiState.Initial)
    val uiState: StateFlow<SeasonsListUiState> = _uiState.asStateFlow()

    init {
        loadSeasons()
    }

    /**
     * Loads the list of Formula 1 World Champions.
     * Updates the UI state based on the loading result.
     */
    private fun loadSeasons() {
        viewModelScope.launch {
            _uiState.update { SeasonsListUiState.Loading }
            
            try {
                val seasons = getSeasonChampionsUseCase()
                _uiState.update { SeasonsListUiState.Success(seasons) }
            } catch (e: F1NetworkException) {
                _uiState.update { 
                    SeasonsListUiState.Error(
                        errorType = if (e.isOffline) ErrorType.OFFLINE else ErrorType.TIMEOUT,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: F1RateLimitException) {
                _uiState.update { 
                    SeasonsListUiState.Error(
                        errorType = ErrorType.RATE_LIMIT,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: F1ServerException) {
                _uiState.update { 
                    SeasonsListUiState.Error(
                        errorType = ErrorType.SERVER_ERROR,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: F1Exception) {
                _uiState.update { 
                    SeasonsListUiState.Error(
                        errorType = ErrorType.UNEXPECTED,
                        message = e.message,
                        canRetry = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    SeasonsListUiState.Error(
                        errorType = ErrorType.UNEXPECTED,
                        message = "An unexpected error occurred",
                        canRetry = true
                    )
                }
            }
        }
    }

    /**
     * Retries loading the seasons list after an error.
     */
    fun retry() {
        loadSeasons()
    }
} 