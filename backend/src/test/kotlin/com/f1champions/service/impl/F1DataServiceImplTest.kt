package com.f1champions.service.impl

import com.f1champions.client.ergast.common.ErgastCircuitDto
import com.f1champions.client.ergast.common.ErgastConstructorDto
import com.f1champions.client.ergast.common.ErgastDriverDto
import com.f1champions.client.ergast.common.ErgastLocationDto
import com.f1champions.client.ergast.dto.results.ErgastRaceResultsDto
import com.f1champions.client.ergast.dto.results.MRDataRaceResultsDto
import com.f1champions.client.ergast.dto.results.RaceResultDto
import com.f1champions.client.ergast.dto.results.RaceTableDto
import com.f1champions.client.ergast.dto.standings.DriverStandingDto
import com.f1champions.client.ergast.dto.standings.ErgastDriverStandingsDto
import com.f1champions.client.ergast.dto.standings.MRDataStandingsDto
import com.f1champions.client.ergast.dto.standings.StandingsListDto
import com.f1champions.client.ergast.dto.standings.StandingsTableDto
import com.f1champions.entity.RaceEntity
import com.f1champions.entity.SeasonEntity
import com.f1champions.exception.ErgastApiException
import com.f1champions.exception.ErgastApiServiceUnavailableException
import com.f1champions.repository.RaceRepository
import com.f1champions.repository.SeasonRepository
import com.f1champions.service.ErgastApiClient
import com.f1champions.service.RateLimiterService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.Year
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.f1champions.client.ergast.dto.results.RaceDto as ErgastRaceDto

class F1DataServiceImplTest {

    private lateinit var seasonRepository: SeasonRepository
    private lateinit var raceRepository: RaceRepository
    private lateinit var ergastApiClient: ErgastApiClient
    private lateinit var rateLimiterService: RateLimiterService
    private lateinit var f1DataService: F1DataServiceImpl

    @BeforeEach
    fun setup() {
        seasonRepository = mockk()
        raceRepository = mockk()
        ergastApiClient = mockk()
        rateLimiterService = mockk()
        f1DataService = F1DataServiceImpl(ergastApiClient, seasonRepository, raceRepository, rateLimiterService)

        // Default rate limiter behavior - just pass through the operation
        coEvery {
            rateLimiterService.executeWithRateLimit<Any>(any())
        } coAnswers {
            val operation = firstArg<suspend () -> Any>()
            withContext(EmptyCoroutineContext) {
                operation()
            }
        }
    }

    @Test
    fun `getAllSeasons should return seasons ordered by year`() = runBlocking {
        // Given
        val seasons = listOf(
            SeasonEntity(
                year = 2022,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 15
            ),
            SeasonEntity(
                year = 2023,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 19
            )
        )
        coEvery { seasonRepository.findAllByOrderByYearAsc() } returns seasons

        // When
        val result = f1DataService.getAllSeasons()

        // Then
        assertEquals(2, result.size)
        assertEquals(2022, result[0].year)
        assertEquals(2023, result[1].year)
        assertEquals("Max Verstappen", result[0].championName)
        assertEquals(454, result[0].championPoints)
        assertEquals(15, result[0].championWins)
    }

    @Test
    fun `getRacesForSeason should throw IllegalArgumentException for invalid year`() = runBlocking {
        // Given
        val invalidYear = 2004 // Before minimum valid year

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            f1DataService.getRacesForSeason(invalidYear)
        }
        assertEquals("Invalid year: $invalidYear. Must be between 2005 and current year.", exception.message)
    }

    @Test
    fun `getRacesForSeason should throw NoSuchElementException when season not found`() = runBlocking {
        // Given
        val year = 2023
        coEvery { seasonRepository.findById(year) } returns Optional.empty()

        // When/Then
        val exception = assertThrows<NoSuchElementException> {
            f1DataService.getRacesForSeason(year)
        }
        assertEquals("Season data for year $year not found. Please ensure season data is populated first.", exception.message)
    }

    @Test
    fun `getRacesForSeason should return existing races from database`() = runBlocking {
        // Given
        val year = 2023
        val season = SeasonEntity(
            year = year,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )
        val races = listOf(
            RaceEntity(
                season = season,
                round = 1,
                raceName = "Bahrain Grand Prix",
                date = LocalDate.of(2023, 3, 5),
                circuitName = "Bahrain International Circuit",
                winningDriverId = "max_verstappen",
                winningDriverName = "Max Verstappen",
                winningDriverNationality = "Dutch",
                winningConstructorId = "red_bull",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = true
            )
        )
        coEvery { seasonRepository.findById(year) } returns Optional.of(season)
        coEvery { raceRepository.findBySeasonYearOrderByRoundAsc(year) } returns races

        // When
        val result = f1DataService.getRacesForSeason(year)

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result[0].round)
        assertEquals("Bahrain Grand Prix", result[0].raceName)
        assertEquals("Max Verstappen", result[0].winningDriverName)
        assertTrue(result[0].isSeasonChampionWinner)
    }

    @Test
    fun `getRacesForSeason should fetch and save races from API when not in database`() = runBlocking {
        // Given
        val year = 2023
        val season = SeasonEntity(
            year = year,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )
        val ergastResponse = ErgastRaceResultsDto(
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
                        ErgastRaceDto(
                            season = "2023",
                            round = "1",
                            url = "http://en.wikipedia.org/wiki/2023_Bahrain_Grand_Prix",
                            raceName = "Bahrain Grand Prix",
                            circuit = ErgastCircuitDto(
                                circuitId = "bahrain",
                                url = "http://en.wikipedia.org/wiki/Bahrain_International_Circuit",
                                circuitName = "Bahrain International Circuit",
                                location = ErgastLocationDto(
                                    lat = "11.50",
                                    long = "12.20",
                                    locality = "BE",
                                    country = "Belgium"
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
                                        name = "Red Bull Racing",
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

        coEvery { seasonRepository.findById(year) } returns Optional.of(season)
        coEvery { raceRepository.findBySeasonYearOrderByRoundAsc(year) } returns emptyList()
        coEvery { ergastApiClient.getRaceResults(year) } returns ergastResponse
        coEvery { raceRepository.saveAll(any<List<RaceEntity>>()) } returns listOf(
            RaceEntity(
                season = season,
                round = 1,
                raceName = "Bahrain Grand Prix",
                date = LocalDate.of(2023, 3, 5),
                circuitName = "Bahrain International Circuit",
                winningDriverId = "max_verstappen",
                winningDriverName = "Max Verstappen",
                winningDriverNationality = "Dutch",
                winningConstructorId = "red_bull",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = true
            )
        )

        // When
        val result = f1DataService.getRacesForSeason(year)

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result[0].round)
        assertEquals("Bahrain Grand Prix", result[0].raceName)
        assertEquals("Max Verstappen", result[0].winningDriverName)
        assertTrue(result[0].isSeasonChampionWinner)
        coVerify { raceRepository.saveAll(any<List<RaceEntity>>()) }
        coVerify { ergastApiClient.getRaceResults(year) }
    }

    @Test
    fun `getRacesForSeason should handle empty races list from API`() = runBlocking {
        // Given
        val year = 2023
        val season = SeasonEntity(
            year = year,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )
        val ergastResponse = ErgastRaceResultsDto(
            mrData = MRDataRaceResultsDto(
                xmlns = "http://ergast.com/mrd/1.5",
                series = "f1",
                url = "http://ergast.com/api/f1/2023/results/1.json",
                limit = "30",
                offset = "0",
                total = "0",
                raceTable = RaceTableDto(
                    season = "2023",
                    races = emptyList()
                )
            )
        )

        coEvery { seasonRepository.findById(year) } returns Optional.of(season)
        coEvery { raceRepository.findBySeasonYearOrderByRoundAsc(year) } returns emptyList()
        coEvery { ergastApiClient.getRaceResults(year) } returns ergastResponse
        coEvery { raceRepository.saveAll(emptyList<RaceEntity>()) } returns emptyList()

        // When
        val result = f1DataService.getRacesForSeason(year)

        // Then
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { raceRepository.saveAll(emptyList<RaceEntity>()) }
        coVerify { ergastApiClient.getRaceResults(year) }
    }

    @Test
    fun `getRacesForSeason should handle invalid date format in API response`() = runBlocking {
        // Given
        val year = 2023
        val season = SeasonEntity(
            year = year,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )
        val ergastResponse = ErgastRaceResultsDto(
            mrData = MRDataRaceResultsDto(
                xmlns = "http://ergast.com/mrd/1.5",
                series = "f1",
                url = "http://ergast.com/api/f1/2023/results/1.json",
                limit = "30",
                offset = "0",
                total = "1",
                raceTable = RaceTableDto(
                    season = "2023",
                    races = listOf(
                        ErgastRaceDto(
                            season = "2023",
                            round = "1",
                            url = "http://en.wikipedia.org/wiki/2023_Bahrain_Grand_Prix",
                            raceName = "Bahrain Grand Prix",
                            circuit = ErgastCircuitDto(
                                circuitId = "bahrain",
                                url = "http://en.wikipedia.org/wiki/Bahrain_International_Circuit",
                                circuitName = "Bahrain International Circuit",
                                location = ErgastLocationDto(
                                    lat = "11.50",
                                    long = "12.20",
                                    locality = "BE",
                                    country = "Belgium"
                                )
                            ),
                            date = "invalid-date", // Invalid date format
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
                                        name = "Red Bull Racing",
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

        coEvery { seasonRepository.findById(year) } returns Optional.of(season)
        coEvery { raceRepository.findBySeasonYearOrderByRoundAsc(year) } returns emptyList()
        coEvery { ergastApiClient.getRaceResults(year) } returns ergastResponse

        // When/Then
        val exception = assertThrows<ErgastApiException> {
            f1DataService.getRacesForSeason(year)
        }
        assertTrue(exception.message?.contains("Failed to process race data for year $year") == true)
        assertTrue(exception.message?.contains("Text 'invalid-date' could not be parsed") == true)
    }

    @Test
    fun `getRacesForSeason should handle rate limiter errors`() = runBlocking {
        // Given
        val year = 2023
        val season = SeasonEntity(
            year = year,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )

        coEvery { seasonRepository.findById(year) } returns Optional.of(season)
        coEvery { raceRepository.findBySeasonYearOrderByRoundAsc(year) } returns emptyList()
        coEvery { rateLimiterService.executeWithRateLimit<Any>(any()) } throws
            IllegalStateException("Rate limit exceeded")

        // When/Then
        val exception = assertThrows<ErgastApiException> {
            f1DataService.getRacesForSeason(year)
        }
        assertTrue(exception.message?.contains("Rate limit exceeded while fetching race data for year $year") == true)

        // Verify rate limiter was called exactly once
        coVerify(exactly = 1) { rateLimiterService.executeWithRateLimit<Any>(any()) }
        // Verify no API calls were made
        coVerify(exactly = 0) { ergastApiClient.getRaceResults(any()) }
        // Verify no races were saved
        coVerify(exactly = 0) { raceRepository.saveAll<RaceEntity>(any()) }
    }

    @Test
    fun `ensureSeasonsDataPopulated should return true when all seasons are already populated`() = runBlocking {
        // Given
        val currentYear = Year.now().value
        val existingSeasons = (2005..currentYear).map { year ->
            SeasonEntity(
                year = year,
                championName = "Test Champion",
                championDriverId = "test_driver",
                championPoints = 100,
                championWins = 5
            )
        }
        coEvery { seasonRepository.findAll() } returns existingSeasons

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result) // Should return true because all seasons are already populated
        coVerify(exactly = 0) { ergastApiClient.getDriverStandings(any()) } // Should not try to fetch any data
        coVerify(exactly = 0) { seasonRepository.save(any()) } // Should not try to save any data
    }

    @Test
    fun `ensureSeasonsDataPopulated should return false when no new data could be fetched`() = runBlocking {
        // Given
        val currentYear = Year.now().value
        val existingSeasons = listOf(
            SeasonEntity(
                year = 2022,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 15
            )
        )

        // Mock error responses for all years from 2005 to current year
        (2005..currentYear).forEach { year ->
            if (year != 2022) { // Skip 2022 as it's already in existingSeasons
                coEvery { ergastApiClient.getDriverStandings(year) } throws ErgastApiServiceUnavailableException(
                    "Error fetching driver standings data: Internal Server Error"
                )
            }
        }

        coEvery { seasonRepository.findAll() } returns existingSeasons

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertFalse(result) // Should return false because no new data was fetched
        coVerify(exactly = 0) { seasonRepository.save(any()) } // Should not save any data
        (2005..currentYear).forEach { year ->
            if (year != 2022) {
                coVerify { ergastApiClient.getDriverStandings(year) } // Verify all years were attempted
            }
        }
    }

    @Test
    fun `ensureSeasonsDataPopulated should fetch and save missing seasons`() = runBlocking {
        // Given
        val currentYear = Year.now().value
        val existingSeasons = listOf(
            SeasonEntity(
                year = 2022,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 15
            )
        )

        // Mock responses for all years from 2005 to current year
        (2005..currentYear).forEach { year ->
            if (year != 2022) { // Skip 2022 as it's already in existingSeasons
                val ergastResponse = ErgastDriverStandingsDto(
                    mrData = MRDataStandingsDto(
                        xmlns = "http://ergast.com/mrd/1.5",
                        series = "f1",
                        url = "http://ergast.com/api/f1/$year/driverStandings/1.json",
                        limit = "30",
                        offset = "0",
                        total = "1",
                        standingsTable = StandingsTableDto(
                            season = year.toString(),
                            standingsLists = listOf(
                                StandingsListDto(
                                    season = year.toString(),
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
                                                    name = "Red Bull Racing",
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
                coEvery { ergastApiClient.getDriverStandings(year) } returns ergastResponse
                coEvery { seasonRepository.save(any()) } returns SeasonEntity(
                    year = year,
                    championName = "Max Verstappen",
                    championDriverId = "max_verstappen",
                    championPoints = 454,
                    championWins = 19
                )
            }
        }

        coEvery { seasonRepository.findAll() } returns existingSeasons

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result)
        coVerify(exactly = currentYear - 2005) { seasonRepository.save(any()) } // Should save all years except 2022
        (2005..currentYear).forEach { year ->
            if (year != 2022) {
                coVerify { ergastApiClient.getDriverStandings(year) }
            }
        }
    }

    @Test
    fun `ensureSeasonsDataPopulated should handle API errors gracefully`() = runBlocking {
        // Given
        val currentYear = Year.now().value
        val existingSeasons = listOf(
            SeasonEntity(
                year = 2022,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 15
            )
        )

        // Mock responses for all years from 2005 to current year
        (2005..currentYear).forEach { year ->
            if (year != 2022) { // Skip 2022 as it's already in existingSeasons
                if (year == 2023) {
                    // Success case for 2023
                    val ergastResponse = ErgastDriverStandingsDto(
                        mrData = MRDataStandingsDto(
                            xmlns = "http://ergast.com/mrd/1.5",
                            series = "f1",
                            url = "http://ergast.com/api/f1/$year/driverStandings/1.json",
                            limit = "30",
                            offset = "0",
                            total = "1",
                            standingsTable = StandingsTableDto(
                                season = year.toString(),
                                standingsLists = listOf(
                                    StandingsListDto(
                                        season = year.toString(),
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
                                                        name = "Red Bull Racing",
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
                    coEvery { ergastApiClient.getDriverStandings(year) } returns ergastResponse
                    coEvery { seasonRepository.save(any()) } returns SeasonEntity(
                        year = year,
                        championName = "Max Verstappen",
                        championDriverId = "max_verstappen",
                        championPoints = 454,
                        championWins = 19
                    )
                } else {
                    // Error case for other years
                    coEvery { ergastApiClient.getDriverStandings(year) } throws ErgastApiServiceUnavailableException(
                        "Error fetching driver standings data: Internal Server Error"
                    )
                }
            }
        }

        coEvery { seasonRepository.findAll() } returns existingSeasons

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result) // Should return true because 2023 was successfully fetched and saved
        coVerify(exactly = 1) { seasonRepository.save(any()) } // Only 2023 should be saved
        (2005..currentYear).forEach { year ->
            if (year != 2022) {
                coVerify { ergastApiClient.getDriverStandings(year) } // Verify all years were attempted
            }
        }
    }

    @Test
    fun `ensureSeasonsDataPopulated should handle malformed champion data`() = runBlocking {
        // Given
        val currentYear = Year.now().value
        val existingSeasons = listOf(
            SeasonEntity(
                year = 2022,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 15
            )
        )

        // Mock responses for all years from 2005 to current year
        (2005..currentYear).forEach { year ->
            if (year != 2022) { // Skip 2022 as it's already in existingSeasons
                val ergastResponse = ErgastDriverStandingsDto(
                    mrData = MRDataStandingsDto(
                        xmlns = "http://ergast.com/mrd/1.5",
                        series = "f1",
                        url = "http://ergast.com/api/f1/$year/driverStandings/1.json",
                        limit = "30",
                        offset = "0",
                        total = "1",
                        standingsTable = StandingsTableDto(
                            season = year.toString(),
                            standingsLists = listOf(
                                StandingsListDto(
                                    season = year.toString(),
                                    round = "22",
                                    driverStandings = listOf(
                                        DriverStandingDto(
                                            position = "1",
                                            points = "invalid", // Invalid points format
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
                                                    name = "Red Bull Racing",
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
                coEvery { ergastApiClient.getDriverStandings(year) } returns ergastResponse
            }
        }

        coEvery { seasonRepository.findAll() } returns existingSeasons

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertFalse(result) // Should return false because no new data was fetched
        coVerify(exactly = 0) { seasonRepository.save(any()) } // Should not save due to invalid data
        (2005..currentYear).forEach { year ->
            if (year != 2022) {
                coVerify { ergastApiClient.getDriverStandings(year) }
            }
        }
    }

    @Test
    fun `ensureSeasonsDataPopulated should handle invalid numeric formats in response`() = runBlocking {
        // Given
        val currentYear = Year.now().value
        val existingSeasons = listOf(
            SeasonEntity(
                year = 2022,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 15
            )
        )

        // Mock responses for all years from 2005 to current year
        (2005..currentYear).forEach { year ->
            if (year != 2022) { // Skip 2022 as it's already in existingSeasons
                val ergastResponse = ErgastDriverStandingsDto(
                    mrData = MRDataStandingsDto(
                        xmlns = "http://ergast.com/mrd/1.5",
                        series = "f1",
                        url = "http://ergast.com/api/f1/$year/driverStandings/1.json",
                        limit = "30",
                        offset = "0",
                        total = "1",
                        standingsTable = StandingsTableDto(
                            season = year.toString(),
                            standingsLists = listOf(
                                StandingsListDto(
                                    season = year.toString(),
                                    round = "22",
                                    driverStandings = listOf(
                                        DriverStandingDto(
                                            position = "1",
                                            points = if (year == 2023) "invalid_points" else "454", // Invalid points for 2023
                                            wins = if (year == 2024) "invalid_wins" else "19", // Invalid wins for 2024
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
                                                    name = "Red Bull Racing",
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
                coEvery { ergastApiClient.getDriverStandings(year) } returns ergastResponse
                if (year != 2023 && year != 2024) { // Only save for years with valid data
                    coEvery { seasonRepository.save(any()) } returns SeasonEntity(
                        year = year,
                        championName = "Max Verstappen",
                        championDriverId = "max_verstappen",
                        championPoints = 454,
                        championWins = 19
                    )
                }
            }
        }

        coEvery { seasonRepository.findAll() } returns existingSeasons

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result) // Should return true because some years were successfully fetched and saved
        coVerify(exactly = currentYear - 2005 - 2) { seasonRepository.save(any()) } // Should save all years except 2022, 2023, and 2024
        (2005..currentYear).forEach { year ->
            if (year != 2022) {
                coVerify { ergastApiClient.getDriverStandings(year) } // Verify all years were attempted
            }
        }
    }

    @Test
    fun `ensureSeasonsDataPopulated should handle rate limiter errors`() = runBlocking {
        // Given
        val existingSeasons = listOf(
            SeasonEntity(
                year = 2022,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 15
            )
        )

        coEvery { seasonRepository.findAll() } returns existingSeasons
        coEvery { rateLimiterService.executeWithRateLimit<Any>(any()) } throws
            IllegalStateException("Rate limit exceeded")

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertFalse(result) // Should return false because no data was fetched

        // Verify rate limiter was called at least once (for the first year attempt)
        coVerify(atLeast = 1) { rateLimiterService.executeWithRateLimit<Any>(any()) }
        // Verify no API calls were made after rate limit error
        coVerify(exactly = 0) { ergastApiClient.getDriverStandings(any()) }
        // Verify no seasons were saved
        coVerify(exactly = 0) { seasonRepository.save(any()) }
    }
}
