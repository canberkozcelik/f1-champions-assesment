package com.f1champions.data.remote

import com.f1champions.data.api.F1ChampionsApi
import com.f1champions.data.api.dto.ApiErrorDto
import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.data.api.dto.SeasonSummaryApiDto
import com.f1champions.data.remote.exception.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class F1ChampionsRemoteDataSourceTest {

    private lateinit var api: F1ChampionsApi
    private lateinit var moshi: Moshi
    private lateinit var apiErrorAdapter: JsonAdapter<ApiErrorDto>
    private lateinit var dataSource: F1ChampionsRemoteDataSource

    @Before
    fun setup() {
        api = mockk()
        apiErrorAdapter = mockk()
        moshi = mockk {
            every { adapter(ApiErrorDto::class.java) } returns apiErrorAdapter
        }
        dataSource = F1ChampionsRemoteDataSource(api, moshi)
    }

    @Test
    fun `getSeasons returns list of champions on success`() = runTest {
        // Given
        val champions = listOf(
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
        coEvery { api.getSeasons() } returns champions

        // When
        val result = dataSource.getSeasons()

        // Then
        assertEquals(2, result.size)
        with(result[0]) {
            assertEquals(2023, year)
            assertEquals("Max Verstappen", championName)
            assertEquals(575, championPoints)
            assertEquals(19, championWins)
        }
    }

    @Test
    fun `getRacesForSeason returns list of races on success`() = runTest {
        // Given
        val races = listOf(
            RaceDetailApiDto(
                round = 1,
                raceName = "Bahrain Grand Prix",
                date = LocalDate.of(2024, 3, 2),
                circuitName = "Bahrain International Circuit",
                winningDriverName = "Max Verstappen",
                winningDriverNationality = "Dutch",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = true
            ),
            RaceDetailApiDto(
                round = 2,
                raceName = "Saudi Arabian Grand Prix",
                date = LocalDate.of(2024, 3, 9),
                circuitName = "Jeddah Corniche Circuit",
                winningDriverName = "Sergio Perez",
                winningDriverNationality = "Mexican",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = false
            )
        )
        coEvery { api.getRacesForSeason(2024) } returns races

        // When
        val result = dataSource.getRacesForSeason(2024)

        // Then
        assertEquals(2, result.size)
        with(result[0]) {
            assertEquals("Bahrain Grand Prix", raceName)
            assertEquals("Max Verstappen", winningDriverName)
            assertTrue(isSeasonChampionWinner)
        }
        with(result[1]) {
            assertEquals("Saudi Arabian Grand Prix", raceName)
            assertEquals("Sergio Perez", winningDriverName)
            assertFalse(isSeasonChampionWinner)
        }
    }

    @Test
    fun `getSeasons throws NoInternetException when UnknownHostException occurs`() = runTest {
        // Given
        coEvery { api.getSeasons() } throws UnknownHostException()

        // When/Then
        val exception = assertFailsWith<NoInternetException> {
            dataSource.getSeasons()
        }
        assertEquals("No internet connection while fetching seasons", exception.message)
        assertEquals("/seasons", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
        assertTrue(exception.isOffline)
    }

    @Test
    fun `getSeasons throws ConnectionTimeoutException when SocketTimeoutException occurs`() = runTest {
        // Given
        coEvery { api.getSeasons() } throws SocketTimeoutException()

        // When/Then
        val exception = assertFailsWith<ConnectionTimeoutException> {
            dataSource.getSeasons()
        }
        assertEquals("Connection timed out while fetching seasons", exception.message)
        assertEquals("/seasons", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
        assertTrue(exception.isTimeout)
    }

    @Test
    fun `getRacesForSeason throws InvalidSeasonException when API returns 400`() = runTest {
        // Given
        val errorBody = ApiErrorDto(
            timestamp = ZonedDateTime.now(),
            status = 400,
            error = "Bad Request",
            message = "Invalid season year: 1899",
            path = "/seasons/1899/races"
        )
        val errorJson = "{\"error\":\"Bad Request\",\"message\":\"Invalid season year: 1899\"}"
        every { apiErrorAdapter.toJson(errorBody) } returns errorJson
        val response = Response.error<Any>(
            400,
            errorJson.toResponseBody("application/json".toMediaType())
        )
        coEvery { api.getRacesForSeason(1899) } throws HttpException(response)

        // When/Then
        val exception = assertFailsWith<InvalidSeasonException> {
            dataSource.getRacesForSeason(1899)
        }
        assertEquals("Invalid season year: 1899", exception.message)
        assertEquals("/seasons/1899/races", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `getRacesForSeason throws SeasonNotFoundException when API returns 404`() = runTest {
        // Given
        val errorBody = ApiErrorDto(
            timestamp = ZonedDateTime.now(),
            status = 404,
            error = "Not Found",
            message = "Season not found: 2025",
            path = "/seasons/2025/races"
        )
        val errorJson = "{\"error\":\"Not Found\",\"message\":\"Season not found: 2025\"}"
        every { apiErrorAdapter.toJson(errorBody) } returns errorJson
        val response = Response.error<Any>(
            404,
            errorJson.toResponseBody("application/json".toMediaType())
        )
        coEvery { api.getRacesForSeason(2025) } throws HttpException(response)

        // When/Then
        val exception = assertFailsWith<SeasonNotFoundException> {
            dataSource.getRacesForSeason(2025)
        }
        assertEquals("Season not found: 2025", exception.message)
        assertEquals("/seasons/2025/races", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `getSeasons throws RateLimitException when API returns 429`() = runTest {
        // Given
        val errorBody = ApiErrorDto(
            timestamp = ZonedDateTime.now(),
            status = 429,
            error = "Too Many Requests",
            message = "Rate limit exceeded",
            path = "/seasons"
        )
        val errorJson = "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}"
        every { apiErrorAdapter.fromJson(errorJson) } returns errorBody
        val response = Response.error<Any>(
            429,
            errorJson.toResponseBody("application/json".toMediaType())
        )
        coEvery { api.getSeasons() } throws HttpException(response)

        // When/Then
        val exception = assertFailsWith<RateLimitException> {
            dataSource.getSeasons()
        }
        assertEquals("Rate limit exceeded", exception.message)
        assertEquals("/seasons", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
        assertEquals(429, exception.statusCode)
    }

    @Test
    fun `getSeasons throws ServerException when API returns 500`() = runTest {
        // Given
        val errorBody = ApiErrorDto(
            timestamp = ZonedDateTime.now(),
            status = 500,
            error = "Internal Server Error",
            message = "Something went wrong",
            path = "/seasons"
        )
        val errorJson = "{\"error\":\"Internal Server Error\",\"message\":\"Something went wrong\"}"
        every { apiErrorAdapter.fromJson(errorJson) } returns errorBody
        val response = Response.error<Any>(
            500,
            errorJson.toResponseBody("application/json".toMediaType())
        )
        coEvery { api.getSeasons() } throws HttpException(response)

        // When/Then
        val exception = assertFailsWith<ServerException> {
            dataSource.getSeasons()
        }
        assertEquals("Something went wrong", exception.message)
        assertEquals("/seasons", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
        assertEquals(500, exception.statusCode)
    }

    @Test
    fun `getSeasons throws GeneralNetworkException when IOException occurs`() = runTest {
        // Given
        coEvery { api.getSeasons() } throws IOException("Network error")

        // When/Then
        val exception = assertFailsWith<GeneralNetworkException> {
            dataSource.getSeasons()
        }
        assertEquals("Failed to connect to the server while fetching seasons", exception.message)
        assertEquals("/seasons", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
    }

    @Test
    fun `getSeasons throws ParseException when response is malformed`() = runTest {
        // Given
        coEvery { api.getSeasons() } throws JsonDataException("Expected BEGIN_ARRAY but was BEGIN_OBJECT")

        // When/Then
        val exception = assertFailsWith<ParseException> {
            dataSource.getSeasons()
        }
        assertEquals("Failed to parse seasons response", exception.message)
        assertEquals("/seasons", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
    }

    @Test
    fun `getSeasons throws ParseException when response has invalid data type`() = runTest {
        // Given
        coEvery { api.getSeasons() } throws JsonDataException("Expected int but was string at path $.year")

        // When/Then
        val exception = assertFailsWith<ParseException> {
            dataSource.getSeasons()
        }
        assertEquals("Failed to parse seasons response", exception.message)
        assertEquals("/seasons", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
    }

    @Test
    fun `getSeasons returns empty list when API returns no seasons`() = runTest {
        // Given
        coEvery { api.getSeasons() } returns emptyList()

        // When
        val result = dataSource.getSeasons()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRacesForSeason returns empty list when API returns no races`() = runTest {
        // Given
        coEvery { api.getRacesForSeason(2024) } returns emptyList()

        // When
        val result = dataSource.getRacesForSeason(2024)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRacesForSeason throws ParseException when race date format is invalid`() = runTest {
        // Given
        coEvery { api.getRacesForSeason(2024) } throws JsonDataException(
            "Expected a string but was NUMBER at path $.date"
        )

        // When/Then
        val exception = assertFailsWith<ParseException> {
            dataSource.getRacesForSeason(2024)
        }
        assertEquals("Failed to parse races response for season 2024", exception.message)
        assertEquals("/seasons/2024/races", exception.requestUrl)
        assertEquals("GET", exception.requestMethod)
    }
} 