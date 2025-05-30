package com.f1champions.feature.racewinners.ui

import app.cash.turbine.test
import com.f1champions.core.ui.components.ErrorType
import com.f1champions.domain.exception.*
import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.domain.repository.F1Repository
import com.f1champions.feature.racewinners.mapper.RaceWinnerMapper
import com.f1champions.feature.racewinners.model.RaceWinner
import com.f1champions.feature.racewinners.model.Winner
import io.mockk.coEvery
import io.mockk.every
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
class RaceWinnersViewModelTest {

    private lateinit var viewModel: RaceWinnersViewModel
    private lateinit var repository: F1Repository
    private lateinit var mapper: RaceWinnerMapper
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        mapper = mockk()
        viewModel = RaceWinnersViewModel(repository, mapper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Given
        val newViewModel = RaceWinnersViewModel(repository, mapper)

        // Then
        newViewModel.uiState.test {
            assertEquals(RaceWinnersUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `loadRaceWinners emits Loading then Success when repository returns data`() = runTest {
        // Given
        val year = 2023
        val domainRaceWinners = listOf(
            RaceWinnerInfo(
                raceName = "Monaco GP",
                date = "2023-05-28",
                winnerName = "Max Verstappen",
                constructorName = "Red Bull",
                circuitName = "Circuit de Monaco",
                isSeasonChampionWinner = true
            ),
            RaceWinnerInfo(
                raceName = "Spanish GP",
                date = "2023-06-04",
                winnerName = "Max Verstappen",
                constructorName = "Red Bull",
                circuitName = "The Circuit de Barcelona",
                isSeasonChampionWinner = true
            )
        )
        val uiRaceWinners = listOf(
            RaceWinner("Monaco GP", "2023-05-28", Winner("Max Verstappen", "Red Bull")),
            RaceWinner("Spanish GP", "2023-06-04", Winner("Max Verstappen", "Red Bull"))
        )
        coEvery { repository.getRaceWinners(year) } returns domainRaceWinners
        every { mapper.toUiModels(domainRaceWinners) } returns uiRaceWinners

        // When
        viewModel.loadRaceWinners(year)

        // Then
        viewModel.uiState.test {
            assertEquals(RaceWinnersUiState.Loading, awaitItem())
            val successState = awaitItem() as RaceWinnersUiState.Success
            assertEquals(uiRaceWinners, successState.raceWinners)
        }
    }

    @Test
    fun `loadRaceWinners emits Loading then Error with OFFLINE type when network is offline`() =
        runTest {
            // Given
            val year = 2023
            coEvery { repository.getRaceWinners(year) } throws F1NetworkException(
                isOffline = true,
                message = "The server is offline."
            )

            // When
            viewModel.loadRaceWinners(year)

            // Then
            viewModel.uiState.test {
                assertEquals(RaceWinnersUiState.Loading, awaitItem())
                val errorState = awaitItem() as RaceWinnersUiState.Error
                assertEquals(ErrorType.OFFLINE, errorState.errorType)
                assertEquals(true, errorState.canRetry)
            }
        }

    @Test
    fun `loadRaceWinners emits Loading then Error with TIMEOUT type when network times out`() =
        runTest {
            // Given
            val year = 2023
            coEvery { repository.getRaceWinners(year) } throws F1NetworkException(
                isOffline = false,
                message = "The server timed out."
            )

            // When
            viewModel.loadRaceWinners(year)

            // Then
            viewModel.uiState.test {
                assertEquals(RaceWinnersUiState.Loading, awaitItem())
                val errorState = awaitItem() as RaceWinnersUiState.Error
                assertEquals(ErrorType.TIMEOUT, errorState.errorType)
                assertEquals(true, errorState.canRetry)
            }
        }

    @Test
    fun `loadRaceWinners emits Loading then Error with INVALID_SEASON type when season is invalid`() =
        runTest {
            // Given
            val year = 1949 // Before F1 started
            coEvery { repository.getRaceWinners(year) } throws F1InvalidSeasonException(year)

            // When
            viewModel.loadRaceWinners(year)

            // Then
            viewModel.uiState.test {
                assertEquals(RaceWinnersUiState.Loading, awaitItem())
                val errorState = awaitItem() as RaceWinnersUiState.Error
                assertEquals(ErrorType.INVALID, errorState.errorType)
                assertEquals(false, errorState.canRetry)
            }
        }

    @Test
    fun `loadRaceWinners emits Loading then Error with SEASON_NOT_FOUND type when season not found`() =
        runTest {
            // Given
            val year = 2023
            coEvery { repository.getRaceWinners(year) } throws F1SeasonNotFoundException(year)

            // When
            viewModel.loadRaceWinners(year)

            // Then
            viewModel.uiState.test {
                assertEquals(RaceWinnersUiState.Loading, awaitItem())
                val errorState = awaitItem() as RaceWinnersUiState.Error
                assertEquals(ErrorType.NOT_FOUND, errorState.errorType)
                assertEquals(false, errorState.canRetry)
            }
        }

    @Test
    fun `loadRaceWinners emits Loading then Error with RATE_LIMIT type when rate limit exceeded`() =
        runTest {
            // Given
            val year = 2023
            coEvery { repository.getRaceWinners(year) } throws F1RateLimitException()

            // When
            viewModel.loadRaceWinners(year)

            // Then
            viewModel.uiState.test {
                assertEquals(RaceWinnersUiState.Loading, awaitItem())
                val errorState = awaitItem() as RaceWinnersUiState.Error
                assertEquals(ErrorType.RATE_LIMIT, errorState.errorType)
                assertEquals(true, errorState.canRetry)
            }
        }

    @Test
    fun `loadRaceWinners emits Loading then Error with SERVER_ERROR type when server error occurs`() =
        runTest {
            // Given
            val year = 2023
            coEvery { repository.getRaceWinners(year) } throws F1ServerException(
                statusCode = 500,
                message = "The server error occurred."
            )

            // When
            viewModel.loadRaceWinners(year)

            // Then
            viewModel.uiState.test {
                assertEquals(RaceWinnersUiState.Loading, awaitItem())
                val errorState = awaitItem() as RaceWinnersUiState.Error
                assertEquals(ErrorType.SERVER_ERROR, errorState.errorType)
                assertEquals(true, errorState.canRetry)
            }
        }

    @Test
    fun `loadRaceWinners emits Loading then Error with UNEXPECTED type when unexpected error occurs`() =
        runTest {
            // Given
            val year = 2023
            coEvery { repository.getRaceWinners(year) } throws F1UnexpectedException(message = "Unexpected Server Error")

            // When
            viewModel.loadRaceWinners(year)

            // Then
            viewModel.uiState.test {
                assertEquals(RaceWinnersUiState.Loading, awaitItem())
                val errorState = awaitItem() as RaceWinnersUiState.Error
                assertEquals(ErrorType.UNEXPECTED, errorState.errorType)
                assertEquals(true, errorState.canRetry)
            }
        }

    @Test
    fun `retry reloads race winners and emits correct states`() = runTest {
        // Given
        val year = 2023
        val domainRaceWinners = listOf(
            RaceWinnerInfo(
                raceName = "Monaco GP",
                date = "2023-05-28",
                winnerName = "Max Verstappen",
                constructorName = "Red Bull",
                circuitName = "Circuit de Monaco",
                isSeasonChampionWinner = true
            )
        )
        val uiRaceWinners = listOf(
            RaceWinner("Monaco GP", "2023-05-28", Winner("Max Verstappen", "Red Bull"))
        )
        coEvery { repository.getRaceWinners(year) } throws F1NetworkException(
            isOffline = true,
            message = "Network error occurred."
        ) andThenThrows F1NetworkException(
            isOffline = true,
            message = "Network error occurred."
        ) andThen domainRaceWinners
        every { mapper.toUiModels(domainRaceWinners) } returns uiRaceWinners

        // When
        viewModel.loadRaceWinners(year)

        // Then
        viewModel.uiState.test {
            // First load attempt
            assertEquals(RaceWinnersUiState.Loading, awaitItem())
            val firstErrorState = awaitItem() as RaceWinnersUiState.Error
            assertEquals(ErrorType.OFFLINE, firstErrorState.errorType)

            // Retry
            viewModel.retry(year)
            assertEquals(RaceWinnersUiState.Loading, awaitItem())
            val secondErrorState = awaitItem() as RaceWinnersUiState.Error
            assertEquals(ErrorType.OFFLINE, secondErrorState.errorType)

            // Second retry
            viewModel.retry(year)
            assertEquals(RaceWinnersUiState.Loading, awaitItem())
            val successState = awaitItem() as RaceWinnersUiState.Success
            assertEquals(uiRaceWinners, successState.raceWinners)
        }
    }
} 