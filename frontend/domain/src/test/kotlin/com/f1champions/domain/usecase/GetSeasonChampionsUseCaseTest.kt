package com.f1champions.domain.usecase

import com.f1champions.domain.exception.F1NetworkException
import com.f1champions.domain.exception.F1RateLimitException
import com.f1champions.domain.exception.F1ServerException
import com.f1champions.domain.exception.F1UnexpectedException
import com.f1champions.domain.model.SeasonChampionInfo
import com.f1champions.domain.repository.F1Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class GetSeasonChampionsUseCaseTest {

    private lateinit var repository: F1Repository
    private lateinit var useCase: GetSeasonChampionsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSeasonChampionsUseCase(repository)
    }

    @Test
    fun `when repository returns champions, use case returns same list`() = runTest {
        // Given
        val expectedChampions = listOf(
            SeasonChampionInfo(
                year = 2023,
                championName = "Max Verstappen",
                points = 575,
                wins = 19
            ),
            SeasonChampionInfo(
                year = 2022,
                championName = "Max Verstappen",
                points = 454,
                wins = 15
            )
        )
        coEvery { repository.getSeasonChampions() } returns expectedChampions

        // When
        val result = useCase()

        // Then
        assertEquals(expectedChampions, result)
    }

    @Test
    fun `when repository returns empty list, use case returns empty list`() = runTest {
        // Given
        val emptyList = emptyList<SeasonChampionInfo>()
        coEvery { repository.getSeasonChampions() } returns emptyList

        // When
        val result = useCase()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `when repository throws network error, use case propagates F1NetworkException`() = runTest {
        // Given
        val networkError = F1NetworkException(
            isOffline = true,
            message = "No internet connection"
        )
        coEvery { repository.getSeasonChampions() } throws networkError

        // When/Then
        val exception = assertFailsWith<F1NetworkException> {
            useCase()
        }
        assertEquals(networkError.message, exception.message)
        assertEquals(networkError.isOffline, exception.isOffline)
    }

    @Test
    fun `when repository throws rate limit error, use case propagates F1RateLimitException`() = runTest {
        // Given
        val rateLimitError = F1RateLimitException()
        coEvery { repository.getSeasonChampions() } throws rateLimitError

        // When/Then
        val exception = assertFailsWith<F1RateLimitException> {
            useCase()
        }
        assertEquals(rateLimitError.errorCode, exception.errorCode)
        assertEquals(rateLimitError.message, exception.message)
    }

    @Test
    fun `when repository throws server error, use case propagates F1ServerException`() = runTest {
        // Given
        val serverError = F1ServerException(
            statusCode = 503,
            message = "Service temporarily unavailable"
        )
        coEvery { repository.getSeasonChampions() } throws serverError

        // When/Then
        val exception = assertFailsWith<F1ServerException> {
            useCase()
        }
        assertEquals(serverError.statusCode, exception.statusCode)
        assertEquals(serverError.errorCode, exception.errorCode)
        assertEquals(serverError.message, exception.message)
    }

    @Test
    fun `when repository throws unexpected error, use case propagates F1UnexpectedException`() = runTest {
        // Given
        val unexpectedError = F1UnexpectedException(
            message = "Unexpected error occurred"
        )
        coEvery { repository.getSeasonChampions() } throws unexpectedError

        // When/Then
        val exception = assertFailsWith<F1UnexpectedException> {
            useCase()
        }
        assertEquals(unexpectedError.message, exception.message)
    }
} 