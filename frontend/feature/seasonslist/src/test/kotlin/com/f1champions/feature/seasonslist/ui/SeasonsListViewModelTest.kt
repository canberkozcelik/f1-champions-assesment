package com.f1champions.feature.seasonslist.ui

import app.cash.turbine.test
import com.f1champions.domain.exception.*
import com.f1champions.domain.model.SeasonChampionInfo
import com.f1champions.domain.usecase.GetSeasonChampionsUseCase
import com.f1champions.feature.seasonslist.ui.SeasonsListUiState.Error
import com.f1champions.feature.seasonslist.ui.SeasonsListUiState.Initial
import com.f1champions.feature.seasonslist.ui.SeasonsListUiState.Loading
import com.f1champions.feature.seasonslist.ui.SeasonsListUiState.Success
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeasonsListViewModelTest {

    private lateinit var viewModel: SeasonsListViewModel
    private lateinit var getSeasonChampionsUseCase: GetSeasonChampionsUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getSeasonChampionsUseCase = mockk()
        viewModel = SeasonsListViewModel(getSeasonChampionsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Initial`() = runTest {
        // Given
        val newViewModel = SeasonsListViewModel(getSeasonChampionsUseCase)

        // Then
        newViewModel.uiState.test {
            assertEquals(Initial, awaitItem())
        }
    }

    @Test
    fun `loadSeasons emits Loading then Success when use case returns data`() = runTest {
        // Given
        val seasons = listOf(
            SeasonChampionInfo(2023, "Max Verstappen", points = 395, wins = 19),
            SeasonChampionInfo(2022, "Max Verstappen", points = 390, wins = 17),
            SeasonChampionInfo(2021, "Max Verstappen", points = 299, wins = 15)
        )
        coEvery { getSeasonChampionsUseCase() } returns seasons

        // When
        viewModel.loadSeasons()
        viewModel.uiState.test {
            // Then
            assertEquals(Initial, awaitItem())
            assertEquals(Loading, awaitItem())
            val successState = awaitItem() as Success
            assertEquals(seasons, successState.seasons)
        }
    }

    @Test
    fun `loadSeasons emits Loading then Error with OFFLINE type when network is offline`() = runTest {
        // Given
        coEvery { getSeasonChampionsUseCase() } throws F1NetworkException(
            isOffline = true,
            message = "The server is offline."
        )

        // When
        viewModel.loadSeasons()
        viewModel.uiState.test {
            // Then
            assertEquals(Initial, awaitItem())
            assertEquals(Loading, awaitItem())
            val errorState = awaitItem() as Error
            assertEquals(ErrorType.OFFLINE, errorState.errorType)
            assertEquals(true, errorState.canRetry)
        }
    }

    @Test
    fun `loadSeasons emits Loading then Error with TIMEOUT type when network times out`() = runTest {
        // Given
        coEvery { getSeasonChampionsUseCase() } throws F1NetworkException(
            isOffline = false,
            message = "The server call timed out."
        )

        // When
        viewModel.loadSeasons()
        viewModel.uiState.test {
            // Then
            assertEquals(Initial, awaitItem())
            assertEquals(Loading, awaitItem())
            val errorState = awaitItem() as Error
            assertEquals(ErrorType.TIMEOUT, errorState.errorType)
            assertEquals(true, errorState.canRetry)
        }
    }

    @Test
    fun `loadSeasons emits Loading then Error with RATE_LIMIT type when rate limit exceeded`() = runTest {
        // Given
        coEvery { getSeasonChampionsUseCase() } throws F1RateLimitException()

        // When
        viewModel.loadSeasons()
        viewModel.uiState.test {
            // Then
            assertEquals(Initial, awaitItem())
            assertEquals(Loading, awaitItem())
            val errorState = awaitItem() as Error
            assertEquals(ErrorType.RATE_LIMIT, errorState.errorType)
            assertEquals(true, errorState.canRetry)
        }
    }

    @Test
    fun `loadSeasons emits Loading then Error with SERVER_ERROR type when server error occurs`() = runTest {
        // Given
        coEvery { getSeasonChampionsUseCase() } throws F1ServerException(
            statusCode = 500,
            message = "Unexpected Server Error"
        )

        // When
        viewModel.loadSeasons()
        viewModel.uiState.test {
            // Then
            assertEquals(Initial, awaitItem())
            assertEquals(Loading, awaitItem())
            val errorState = awaitItem() as Error
            assertEquals(ErrorType.SERVER_ERROR, errorState.errorType)
            assertEquals(true, errorState.canRetry)
        }
    }

    @Test
    fun `loadSeasons emits Loading then Error with UNEXPECTED type when unexpected error occurs`() = runTest {
        // Given
        coEvery { getSeasonChampionsUseCase() } throws F1UnexpectedException(message = "Unexpected Server Error")

        // When
        viewModel.loadSeasons()
        viewModel.uiState.test {
            // Then
            assertEquals(Initial, awaitItem())
            assertEquals(Loading, awaitItem())
            val errorState = awaitItem() as Error
            assertEquals(ErrorType.UNEXPECTED, errorState.errorType)
            assertEquals(true, errorState.canRetry)
        }
    }

    @Test
    fun `retry reloads seasons and emits correct states`() = runTest {
        // Given
        val seasons = listOf(
            SeasonChampionInfo(2023, "Max Verstappen", points = 395, wins = 19),
            SeasonChampionInfo(2022, "Max Verstappen", points = 299, wins = 16)
        )
        coEvery { getSeasonChampionsUseCase() } throws F1NetworkException(
            isOffline = true,
            message = "Network error occurred."
        ) andThenThrows F1NetworkException(
            isOffline = true,
            message = "Network error occurred."
        ) andThen seasons

        // When
        viewModel.loadSeasons()
        viewModel.uiState.test {
            // First load attempt
            assertEquals(Initial, awaitItem())
            assertEquals(Loading, awaitItem())
            val firstErrorState = awaitItem() as Error
            assertEquals(ErrorType.OFFLINE, firstErrorState.errorType)

            // Retry
            viewModel.retry()
            assertEquals(Loading, awaitItem())
            val secondErrorState = awaitItem() as Error
            assertEquals(ErrorType.OFFLINE, secondErrorState.errorType)

            // Second retry
            viewModel.retry()
            assertEquals(Loading, awaitItem())
            val successState = awaitItem() as Success
            assertEquals(seasons, successState.seasons)
        }
    }
} 