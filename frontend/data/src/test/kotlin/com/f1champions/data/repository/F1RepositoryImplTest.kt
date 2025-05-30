package com.f1champions.data.repository

import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.data.api.dto.SeasonSummaryApiDto
import com.f1champions.data.mapper.RaceMapper
import com.f1champions.data.mapper.SeasonMapper
import com.f1champions.data.remote.F1ChampionsRemoteDataSource
import com.f1champions.data.remote.exception.*
import com.f1champions.domain.exception.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertFailsWith
import java.net.UnknownHostException
import java.net.SocketTimeoutException

class F1RepositoryImplTest {

    private lateinit var remoteDataSource: F1ChampionsRemoteDataSource
    private lateinit var repository: F1RepositoryImpl

    @Before
    fun setup() {
        remoteDataSource = mockk()
        repository = F1RepositoryImpl(remoteDataSource)
    }

    // Season Champions Tests

    @Test
    fun `when getSeasonChampions succeeds, returns mapped season champions`() = runTest {
        // Given
        val apiResponse = listOf(
            SeasonSummaryApiDto(
                year = 2023,
                championName = "Max Verstappen",
                championPoints = 575,
                championWins = 19
            ),
            SeasonSummaryApiDto(
                year = 2022,
                championName = "Max Verstappen",
                championPoints = 454,
                championWins = 15
            )
        )
        val expectedChampions = SeasonMapper.toSeasonChampionInfoList(apiResponse)
        coEvery { remoteDataSource.getSeasons() } returns apiResponse

        // When
        val result = repository.getSeasonChampions()

        // Then
        assertEquals(expectedChampions, result)
    }

    @Test
    fun `when getSeasonChampions returns empty list, returns empty list`() = runTest {
        // Given
        val emptyList = emptyList<SeasonSummaryApiDto>()
        coEvery { remoteDataSource.getSeasons() } returns emptyList

        // When
        val result = repository.getSeasonChampions()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `when getSeasonChampions throws NoInternetException, propagates F1NetworkException with offline flag`() = runTest {
        // Given
        val unknownHostException = UnknownHostException("Unable to resolve host")
        val networkError = NoInternetException(
            message = "No internet connection",
            requestUrl = "/seasons",
            requestMethod = "GET",
            cause = unknownHostException  // Add the actual cause that determines isOffline
        )
        coEvery { remoteDataSource.getSeasons() } throws networkError

        // When/Then
        val exception = assertFailsWith<F1NetworkException> {
            repository.getSeasonChampions()
        }
        assertTrue(exception.isOffline)
        assertEquals("No internet connection available", exception.message)
        assertEquals("NETWORK_OFFLINE", exception.errorCode)
        assertEquals(networkError, exception.cause)  // Verify the cause is preserved
    }

    @Test
    fun `when getSeasonChampions throws ConnectionTimeoutException, propagates F1NetworkException with timeout`() = runTest {
        // Given
        val socketTimeoutException = SocketTimeoutException("Connection timed out")
        val timeoutError = ConnectionTimeoutException(
            message = "Connection timed out",
            requestUrl = "/seasons",
            requestMethod = "GET",
            cause = socketTimeoutException  // Add the actual cause that determines isTimeout in data layer
        )
        coEvery { remoteDataSource.getSeasons() } throws timeoutError

        // When/Then
        val exception = assertFailsWith<F1NetworkException> {
            repository.getSeasonChampions()
        }
        assertTrue(!exception.isOffline)  // Verify it's not an offline error
        assertEquals("Connection timed out while fetching season champions", exception.message)
        assertEquals("NETWORK_TIMEOUT", exception.errorCode)
        assertEquals(timeoutError, exception.cause)  // Verify the cause is preserved
    }

    @Test
    fun `when getSeasonChampions throws RateLimitException, propagates F1RateLimitException`() = runTest {
        // Given
        val rateLimitError = RateLimitException(
            message = "Too many requests",
            requestUrl = "/seasons",
            requestMethod = "GET"
        )
        coEvery { remoteDataSource.getSeasons() } throws rateLimitError

        // When/Then
        val exception = assertFailsWith<F1RateLimitException> {
            repository.getSeasonChampions()
        }
        assertEquals("RATE_LIMIT_EXCEEDED", exception.errorCode)
        assertEquals("Too many requests. Please try again later.", exception.message)
    }

    @Test
    fun `when getSeasonChampions throws ServerException, propagates F1ServerException`() = runTest {
        // Given
        val serverError = ServerException(
            message = "Internal server error",
            requestUrl = "/seasons",
            requestMethod = "GET"
        )
        coEvery { remoteDataSource.getSeasons() } throws serverError

        // When/Then
        val exception = assertFailsWith<F1ServerException> {
            repository.getSeasonChampions()
        }
        assertEquals(500, exception.statusCode)
        assertEquals("SERVER_ERROR", exception.errorCode)
        assertEquals("Server error while fetching season champions: Internal server error", exception.message)
    }

    // Race Winners Tests

    @Test
    fun `when getRaceWinners succeeds, returns mapped race winners`() = runTest {
        // Given
        val year = 2023
        val apiResponse = listOf(
            RaceDetailApiDto(
                round = 1,
                raceName = "Bahrain Grand Prix",
                date = LocalDate.of(2023, 3, 5),
                circuitName = "Bahrain International Circuit",
                winningDriverName = "Max Verstappen",
                winningDriverNationality = "Dutch",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = true
            ),
            RaceDetailApiDto(
                round = 2,
                raceName = "Saudi Arabian Grand Prix",
                date = LocalDate.of(2023, 3, 19),
                circuitName = "Jeddah Corniche Circuit",
                winningDriverName = "Sergio Perez",
                winningDriverNationality = "Mexican",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = false
            )
        )
        val expectedWinners = RaceMapper.toRaceWinnerInfoList(apiResponse)
        coEvery { remoteDataSource.getRacesForSeason(year) } returns apiResponse

        // When
        val result = repository.getRaceWinners(year)

        // Then
        assertEquals(expectedWinners, result)
    }

    @Test
    fun `when getRaceWinners returns empty list, returns empty list`() = runTest {
        // Given
        val year = 2023
        val emptyList = emptyList<RaceDetailApiDto>()
        coEvery { remoteDataSource.getRacesForSeason(year) } returns emptyList

        // When
        val result = repository.getRaceWinners(year)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `when getRaceWinners throws InvalidSeasonException, propagates F1InvalidSeasonException`() = runTest {
        // Given
        val year = 1949 // Before F1 started
        val invalidSeasonError = InvalidSeasonException(
            message = "Invalid season year",
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
        coEvery { remoteDataSource.getRacesForSeason(year) } throws invalidSeasonError

        // When/Then
        val exception = assertFailsWith<F1InvalidSeasonException> {
            repository.getRaceWinners(year)
        }
        assertEquals(year, exception.year)
        assertEquals("INVALID_SEASON", exception.errorCode)
        assertEquals("Invalid season year: $year", exception.message)
    }

    @Test
    fun `when getRaceWinners throws SeasonNotFoundException, propagates F1SeasonNotFoundException`() = runTest {
        // Given
        val year = 2024 // Future season
        val seasonNotFoundError = SeasonNotFoundException(
            message = "Season not found",
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
        coEvery { remoteDataSource.getRacesForSeason(year) } throws seasonNotFoundError

        // When/Then
        val exception = assertFailsWith<F1SeasonNotFoundException> {
            repository.getRaceWinners(year)
        }
        assertEquals(year, exception.year)
        assertEquals("SEASON_NOT_FOUND", exception.errorCode)
        assertEquals("Season not found: $year", exception.message)
    }

    @Test
    fun `when getRaceWinners throws NoInternetException, propagates F1NetworkException with offline flag`() = runTest {
        // Given
        val year = 2023
        val unknownHostException = UnknownHostException("Unable to resolve host")
        val networkError = NoInternetException(
            message = "No internet connection",
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET",
            cause = unknownHostException  // Add the actual cause that determines isOffline
        )
        coEvery { remoteDataSource.getRacesForSeason(year) } throws networkError

        // When/Then
        val exception = assertFailsWith<F1NetworkException> {
            repository.getRaceWinners(year)
        }
        assertTrue(exception.isOffline)
        assertEquals("No internet connection available", exception.message)
        assertEquals("NETWORK_OFFLINE", exception.errorCode)
        assertEquals(networkError, exception.cause)  // Verify the cause is preserved
    }

    @Test
    fun `when getRaceWinners throws RateLimitException, propagates F1RateLimitException`() = runTest {
        // Given
        val year = 2023
        val rateLimitError = RateLimitException(
            message = "Too many requests",
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
        coEvery { remoteDataSource.getRacesForSeason(year) } throws rateLimitError

        // When/Then
        val exception = assertFailsWith<F1RateLimitException> {
            repository.getRaceWinners(year)
        }
        assertEquals("RATE_LIMIT_EXCEEDED", exception.errorCode)
        assertEquals("Too many requests. Please try again later.", exception.message)
    }

    @Test
    fun `when getRaceWinners throws ServerException, propagates F1ServerException`() = runTest {
        // Given
        val year = 2023
        val serverError = ServerException(
            message = "Internal server error",
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
        coEvery { remoteDataSource.getRacesForSeason(year) } throws serverError

        // When/Then
        val exception = assertFailsWith<F1ServerException> {
            repository.getRaceWinners(year)
        }
        assertEquals(500, exception.statusCode)
        assertEquals("SERVER_ERROR", exception.errorCode)
        assertEquals("Server error while fetching race winners for season $year: Internal server error", exception.message)
    }

    @Test
    fun `when getRaceWinners throws ParseException, propagates F1UnexpectedException`() = runTest {
        // Given
        val year = 2023
        val parseError = ParseException(
            message = "Failed to parse response",
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
        coEvery { remoteDataSource.getRacesForSeason(year) } throws parseError

        // When/Then
        val exception = assertFailsWith<F1UnexpectedException> {
            repository.getRaceWinners(year)
        }
        assertEquals("PARSE_ERROR", exception.errorCode)
        assertEquals("Failed to parse race winners data for season $year", exception.message)
    }
} 