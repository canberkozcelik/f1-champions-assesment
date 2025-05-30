package com.f1champions.domain.usecase

import com.f1champions.domain.exception.F1InvalidSeasonException
import com.f1champions.domain.exception.F1NetworkException
import com.f1champions.domain.exception.F1RateLimitException
import com.f1champions.domain.exception.F1SeasonNotFoundException
import com.f1champions.domain.exception.F1ServerException
import com.f1champions.domain.exception.F1UnexpectedException
import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.domain.repository.F1Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class GetRaceWinnersForSeasonUseCaseTest {

    private lateinit var repository: F1Repository
    private lateinit var useCase: GetRaceWinnersForSeasonUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetRaceWinnersForSeasonUseCase(repository)
    }

    @Test
    fun `when repository returns race winners, use case returns same list`() = runTest {
        // Given
        val year = 2023
        val expectedWinners = listOf(
            RaceWinnerInfo(
                raceName = "Bahrain Grand Prix",
                circuitName = "Bahrain International Circuit",
                date = "March 5, 2023",
                winnerName = "Max Verstappen",
                constructorName = "Red Bull Racing",
                isSeasonChampionWinner = true
            ),
            RaceWinnerInfo(
                raceName = "Saudi Arabian Grand Prix",
                circuitName = "Jeddah Corniche Circuit",
                date = "March 19, 2023",
                winnerName = "Sergio Perez",
                constructorName = "Red Bull Racing",
                isSeasonChampionWinner = false
            )
        )
        coEvery { repository.getRaceWinners(year) } returns expectedWinners

        // When
        val result = useCase(GetRaceWinnersParams(year))

        // Then
        assertEquals(expectedWinners, result)
    }

    @Test
    fun `when repository returns empty list, use case returns empty list`() = runTest {
        // Given
        val year = 2023
        val emptyList = emptyList<RaceWinnerInfo>()
        coEvery { repository.getRaceWinners(year) } returns emptyList

        // When
        val result = useCase(GetRaceWinnersParams(year))

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `when repository throws network error, use case propagates F1NetworkException`() = runTest {
        // Given
        val year = 2023
        val networkError = F1NetworkException(
            isOffline = true,
            message = "No internet connection"
        )
        coEvery { repository.getRaceWinners(year) } throws networkError

        // When/Then
        val exception = assertFailsWith<F1NetworkException> {
            useCase(GetRaceWinnersParams(year))
        }
        assertEquals(networkError.message, exception.message)
        assertEquals(networkError.isOffline, exception.isOffline)
    }

    @Test
    fun `when repository throws invalid season error for year before F1, use case propagates F1InvalidSeasonException`() = runTest {
        // Given
        val year = 1949 // Before F1 started
        val invalidSeasonError = F1InvalidSeasonException(year = year)
        coEvery { repository.getRaceWinners(year) } throws invalidSeasonError

        // When/Then
        val exception = assertFailsWith<F1InvalidSeasonException> {
            useCase(GetRaceWinnersParams(year))
        }
        assertEquals(year, exception.year)
    }

    @Test
    fun `when repository throws invalid season error for future year, use case propagates F1InvalidSeasonException`() = runTest {
        // Given
        val year = 2025 // Future year
        val invalidSeasonError = F1InvalidSeasonException(year = year)
        coEvery { repository.getRaceWinners(year) } throws invalidSeasonError

        // When/Then
        val exception = assertFailsWith<F1InvalidSeasonException> {
            useCase(GetRaceWinnersParams(year))
        }
        assertEquals(year, exception.year)
    }

    @Test
    fun `when repository throws season not found error, use case propagates F1SeasonNotFoundException`() = runTest {
        // Given
        val year = 2024 // Future season
        val seasonNotFoundError = F1SeasonNotFoundException(year = year)
        coEvery { repository.getRaceWinners(year) } throws seasonNotFoundError

        // When/Then
        val exception = assertFailsWith<F1SeasonNotFoundException> {
            useCase(GetRaceWinnersParams(year))
        }
        assertEquals(year, exception.year)
    }

    @Test
    fun `when repository throws rate limit error, use case propagates F1RateLimitException`() = runTest {
        // Given
        val year = 2023
        val rateLimitError = F1RateLimitException()
        coEvery { repository.getRaceWinners(year) } throws rateLimitError

        // When/Then
        val exception = assertFailsWith<F1RateLimitException> {
            useCase(GetRaceWinnersParams(year))
        }
        assertEquals(rateLimitError.errorCode, exception.errorCode)
        assertEquals(rateLimitError.message, exception.message)
    }

    @Test
    fun `when repository throws server error, use case propagates F1ServerException`() = runTest {
        // Given
        val year = 2023
        val serverError = F1ServerException(
            statusCode = 503,
            message = "Service temporarily unavailable"
        )
        coEvery { repository.getRaceWinners(year) } throws serverError

        // When/Then
        val exception = assertFailsWith<F1ServerException> {
            useCase(GetRaceWinnersParams(year))
        }
        assertEquals(serverError.statusCode, exception.statusCode)
        assertEquals(serverError.errorCode, exception.errorCode)
        assertEquals(serverError.message, exception.message)
    }

    @Test
    fun `when repository throws unexpected error, use case propagates F1UnexpectedException`() = runTest {
        // Given
        val year = 2023
        val unexpectedError = F1UnexpectedException(
            message = "Unexpected error occurred"
        )
        coEvery { repository.getRaceWinners(year) } throws unexpectedError

        // When/Then
        val exception = assertFailsWith<F1UnexpectedException> {
            useCase(GetRaceWinnersParams(year))
        }
        assertEquals(unexpectedError.message, exception.message)
    }
} 