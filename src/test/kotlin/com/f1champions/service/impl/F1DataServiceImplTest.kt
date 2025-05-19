package com.f1champions.service.impl

import com.f1champions.client.ergast.common.ErgastCircuitDto
import com.f1champions.client.ergast.common.ErgastConstructorDto
import com.f1champions.client.ergast.common.ErgastDriverDto
import com.f1champions.client.ergast.common.ErgastLocationDto
import com.f1champions.client.ergast.dto.standings.*
import com.f1champions.client.ergast.dto.results.*
import com.f1champions.entity.RaceEntity
import com.f1champions.entity.SeasonEntity
import com.f1champions.repository.RaceRepository
import com.f1champions.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.Year
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.f1champions.client.ergast.dto.results.RaceDto as ErgastRaceDto

class F1DataServiceImplTest {

    private lateinit var seasonRepository: SeasonRepository
    private lateinit var raceRepository: RaceRepository
    private lateinit var webClient: WebClient
    private lateinit var f1DataService: F1DataServiceImpl

    @BeforeEach
    fun setup() {
        seasonRepository = mockk()
        raceRepository = mockk()
        webClient = mockk()
        f1DataService = F1DataServiceImpl(seasonRepository, raceRepository, webClient)
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
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve().bodyToMono<ErgastRaceResultsDto>() } returns Mono.just(ergastResponse)
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
    }

    @Test
    fun `getRacesForSeason should throw IllegalStateException when API response is null`() = runBlocking {
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
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve().bodyToMono<ErgastRaceResultsDto>() } returns Mono.empty()

        // When/Then
        val exception = assertThrows<IllegalStateException> {
            f1DataService.getRacesForSeason(year)
        }
        assertEquals("Failed to process race data for year $year: Failed to fetch race data from Ergast API for year $year", exception.message)
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
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve().bodyToMono<ErgastRaceResultsDto>() } returns Mono.just(ergastResponse)
        coEvery { raceRepository.saveAll(emptyList<RaceEntity>()) } returns emptyList()

        // When
        val result = f1DataService.getRacesForSeason(year)

        // Then
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { raceRepository.saveAll(emptyList<RaceEntity>()) }
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
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/$year/results/1.json").retrieve().bodyToMono<ErgastRaceResultsDto>() } returns Mono.just(ergastResponse)

        // When/Then
        val exception = assertThrows<IllegalStateException> {
            f1DataService.getRacesForSeason(year)
        }
        assertTrue(exception.message?.contains("Failed to process race data for year $year") == true)
        assertTrue(exception.message?.contains("Text 'invalid-date' could not be parsed") == true)
    }

    @Test
    fun `ensureSeasonsDataPopulated should return false when no data needs to be fetched`() = runBlocking {
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
        assertFalse(result)
        coVerify(exactly = 0) { webClient.get() }
    }

    @Test
    fun `ensureSeasonsDataPopulated should fetch and save missing seasons`() = runBlocking {
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
        val ergastResponse = ErgastDriverStandingsDto(
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

        coEvery { seasonRepository.findAll() } returns existingSeasons
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve().bodyToMono<ErgastDriverStandingsDto>() } returns Mono.just(ergastResponse)
        coEvery { seasonRepository.save(any()) } returns SeasonEntity(
            year = 2023,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result)
        coVerify { seasonRepository.save(any()) }
    }

    @Test
    fun `ensureSeasonsDataPopulated should handle API errors gracefully`() = runBlocking {
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
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve().bodyToMono<ErgastDriverStandingsDto>() } throws WebClientResponseException(500, "Internal Server Error", null, null, null)

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result) // Should still return true as we attempted to fetch data
        coVerify(exactly = 0) { seasonRepository.save(any()) }
    }

    @Test
    fun `ensureSeasonsDataPopulated should handle partial success`() = runBlocking {
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
        val ergastResponse2023 = ErgastDriverStandingsDto(
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

        coEvery { seasonRepository.findAll() } returns existingSeasons
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve().bodyToMono<ErgastDriverStandingsDto>() } returns Mono.just(ergastResponse2023)
        coEvery { webClient.get().uri("/2024/driverStandings/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2024/driverStandings/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2024/driverStandings/1.json").retrieve().bodyToMono<ErgastDriverStandingsDto>() } throws WebClientResponseException(500, "Internal Server Error", null, null, null)
        coEvery { seasonRepository.save(any()) } returns SeasonEntity(
            year = 2023,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result) // Should return true as we attempted to fetch data
        coVerify(exactly = 1) { seasonRepository.save(any()) } // Only 2023 should be saved
    }

    @Test
    fun `ensureSeasonsDataPopulated should handle malformed champion data`() = runBlocking {
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
        val ergastResponse = ErgastDriverStandingsDto(
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

        coEvery { seasonRepository.findAll() } returns existingSeasons
        coEvery { webClient.get() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json") } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve() } returns mockk(relaxed = true)
        coEvery { webClient.get().uri("/2023/driverStandings/1.json").retrieve().bodyToMono<ErgastDriverStandingsDto>() } returns Mono.just(ergastResponse)

        // When
        val result = f1DataService.ensureSeasonsDataPopulated()

        // Then
        assertTrue(result) // Should return true as we attempted to fetch data
        coVerify(exactly = 0) { seasonRepository.save(any()) } // Should not save due to invalid data
    }
} 