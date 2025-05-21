package com.f1champions.service

import com.f1champions.client.ergast.common.ErgastCircuitDto
import com.f1champions.client.ergast.common.ErgastConstructorDto
import com.f1champions.client.ergast.common.ErgastDriverDto
import com.f1champions.client.ergast.common.ErgastLocationDto
import com.f1champions.client.ergast.dto.results.ErgastRaceResultsDto
import com.f1champions.client.ergast.dto.results.MRDataRaceResultsDto
import com.f1champions.client.ergast.dto.results.RaceDto
import com.f1champions.client.ergast.dto.results.RaceResultDto
import com.f1champions.client.ergast.dto.results.RaceTableDto
import com.f1champions.client.ergast.dto.standings.DriverStandingDto
import com.f1champions.client.ergast.dto.standings.ErgastDriverStandingsDto
import com.f1champions.client.ergast.dto.standings.MRDataStandingsDto
import com.f1champions.client.ergast.dto.standings.StandingsListDto
import com.f1champions.client.ergast.dto.standings.StandingsTableDto
import com.f1champions.exception.ErgastApiDataNotFoundException
import com.f1champions.exception.ErgastApiRateLimitException
import com.f1champions.service.impl.ErgastApiClientImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ErgastApiClientTest {

    private lateinit var webClient: WebClient
    private lateinit var ergastApiClient: ErgastApiClientImpl
    private lateinit var webClientRequest: WebClient.RequestHeadersUriSpec<*>
    private lateinit var webClientRequestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var webClientResponseSpec: WebClient.ResponseSpec

    @BeforeEach
    fun setup() {
        webClient = mockk()
        webClientRequest = mockk()
        webClientRequestHeadersSpec = mockk()
        webClientResponseSpec = mockk()
        ergastApiClient = ErgastApiClientImpl(webClient)

        // Setup common WebClient mock chain
        coEvery { webClient.get() } returns webClientRequest
        coEvery { webClientRequest.uri(any<String>()) } returns webClientRequestHeadersSpec
        coEvery { webClientRequestHeadersSpec.retrieve() } returns webClientResponseSpec
    }

    @Nested
    inner class GetDriverStandingsTests {
        @Test
        fun `getDriverStandings returns valid data when API call succeeds`() = runBlocking {
            // Given
            val expectedResponse = createValidDriverStandingsResponse()
            coEvery { webClientResponseSpec.bodyToMono<ErgastDriverStandingsDto>() } returns Mono.just(expectedResponse)

            // When
            val result = ergastApiClient.getDriverStandings(2023)

            // Then
            assertNotNull(result)
            assertEquals(expectedResponse, result)
        }

        @Test
        fun `getDriverStandings throws ErgastApiDataNotFoundException when API returns 404`() = runBlocking {
            // Given
            coEvery { webClientResponseSpec.bodyToMono<ErgastDriverStandingsDto>() } throws
                WebClientResponseException.create(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    HttpHeaders(),
                    ByteArray(0),
                    null
                )

            // When/Then
            val exception = assertThrows<ErgastApiDataNotFoundException> {
                ergastApiClient.getDriverStandings(2023)
            }
            assertEquals("Driver standings data not found for year 2023", exception.message)
        }

        @Test
        fun `getDriverStandings throws ErgastApiRateLimitException when API returns 429`() = runBlocking {
            // Given
            val headers = HttpHeaders()
            headers.set("Retry-After", "60")
            coEvery { webClientResponseSpec.bodyToMono<ErgastDriverStandingsDto>() } throws
                WebClientResponseException.create(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too Many Requests",
                    headers,
                    ByteArray(0),
                    null
                )

            // When/Then
            val exception = assertThrows<ErgastApiRateLimitException> {
                ergastApiClient.getDriverStandings(2023)
            }
            assertEquals("Rate limit exceeded for Ergast API", exception.message)
            assertEquals(60, exception.retryAfterSeconds)
        }
    }

    @Nested
    inner class GetRaceResultsTests {
        @Test
        fun `getRaceResults returns valid data when API call succeeds`() = runBlocking {
            // Given
            val expectedResponse = createValidRaceResultsResponse()
            coEvery { webClientResponseSpec.bodyToMono<ErgastRaceResultsDto>() } returns Mono.just(expectedResponse)

            // When
            val result = ergastApiClient.getRaceResults(2023)

            // Then
            assertNotNull(result)
            assertEquals(expectedResponse, result)
        }

        @Test
        fun `getRaceResults throws ErgastApiDataNotFoundException when API returns 404`() = runBlocking {
            // Given
            coEvery { webClientResponseSpec.bodyToMono<ErgastRaceResultsDto>() } throws
                WebClientResponseException.create(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    HttpHeaders(),
                    ByteArray(0),
                    null
                )

            // When/Then
            val exception = assertThrows<ErgastApiDataNotFoundException> {
                ergastApiClient.getRaceResults(2023)
            }
            assertEquals("Race results data not found for year 2023", exception.message)
        }

        @Test
        fun `getRaceResults throws ErgastApiRateLimitException when API returns 429`() = runBlocking {
            // Given
            val headers = HttpHeaders()
            headers.set("Retry-After", "60")
            coEvery { webClientResponseSpec.bodyToMono<ErgastRaceResultsDto>() } throws
                WebClientResponseException.create(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too Many Requests",
                    headers,
                    ByteArray(0),
                    null
                )

            // When/Then
            val exception = assertThrows<ErgastApiRateLimitException> {
                ergastApiClient.getRaceResults(2023)
            }
            assertEquals("Rate limit exceeded for Ergast API", exception.message)
            assertEquals(60, exception.retryAfterSeconds)
        }
    }

    // Helper functions to create test data
    private fun createValidDriverStandingsResponse(): ErgastDriverStandingsDto {
        return ErgastDriverStandingsDto(
            mrData = MRDataStandingsDto(
                xmlns = "http://ergast.com/mrd/1.5",
                series = "f1",
                url = "http://ergast.com/api/f1/2023/driverStandings/1.json",
                limit = "30",
                offset = "0",
                total = "1",
                standingsTable = StandingsTableDto(
                    season = "2023",
                    standingsLists = listOf(
                        StandingsListDto(
                            season = "2023",
                            round = "22",
                            driverStandings = listOf(
                                DriverStandingDto(
                                    position = "1",
                                    points = "454",
                                    wins = "19",
                                    driver = ErgastDriverDto(
                                        driverId = "max_verstappen",
                                        permanentNumber = "33",
                                        code = "VER",
                                        url = "http://en.wikipedia.org/wiki/Max_Verstappen",
                                        givenName = "Max",
                                        familyName = "Verstappen",
                                        dateOfBirth = "1997-09-30",
                                        nationality = "Dutch"
                                    ),
                                    constructors = listOf(
                                        ErgastConstructorDto(
                                            constructorId = "red_bull",
                                            url = "http://en.wikipedia.org/wiki/Red_Bull_Racing",
                                            name = "Red Bull",
                                            nationality = "Austrian"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    private fun createValidRaceResultsResponse(): ErgastRaceResultsDto {
        return ErgastRaceResultsDto(
            mrData = MRDataRaceResultsDto(
                xmlns = "http://ergast.com/mrd/1.5",
                series = "f1",
                url = "http://ergast.com/api/f1/2023/results/1.json",
                limit = "30",
                offset = "0",
                total = "22",
                raceTable = RaceTableDto(
                    season = "2023",
                    races = listOf(
                        RaceDto(
                            season = "2023",
                            round = "1",
                            url = "http://en.wikipedia.org/wiki/2023_Bahrain_Grand_Prix",
                            raceName = "Bahrain Grand Prix",
                            circuit = ErgastCircuitDto(
                                circuitId = "bahrain",
                                url = "http://en.wikipedia.org/wiki/Bahrain_International_Circuit",
                                circuitName = "Bahrain International Circuit",
                                location = ErgastLocationDto(
                                    lat = "26.0325",
                                    long = "50.5106",
                                    locality = "Sakhir",
                                    country = "Bahrain"
                                )
                            ),
                            date = "2023-03-05",
                            time = "15:00:00Z",
                            results = listOf(
                                RaceResultDto(
                                    number = "1",
                                    position = "1",
                                    points = "25",
                                    driver = ErgastDriverDto(
                                        driverId = "max_verstappen",
                                        permanentNumber = "33",
                                        code = "VER",
                                        url = "http://en.wikipedia.org/wiki/Max_Verstappen",
                                        givenName = "Max",
                                        familyName = "Verstappen",
                                        dateOfBirth = "1997-09-30",
                                        nationality = "Dutch"
                                    ),
                                    constructor = ErgastConstructorDto(
                                        constructorId = "red_bull",
                                        url = "http://en.wikipedia.org/wiki/Red_Bull_Racing",
                                        name = "Red Bull",
                                        nationality = "Austrian"
                                    ),
                                    grid = "1",
                                    laps = "57",
                                    status = "Finished"
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
