package com.f1champions.repository

import com.f1champions.entity.RaceEntity
import com.f1champions.entity.SeasonEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class RaceRepositoryTest {

    @Autowired
    private lateinit var raceRepository: RaceRepository

    @Autowired
    private lateinit var seasonRepository: SeasonRepository

    private lateinit var season2023: SeasonEntity

    @BeforeEach
    fun setup() {
        // Create a season for testing
        season2023 = SeasonEntity(
            year = 2023,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )
        seasonRepository.save(season2023)
    }

    @Test
    fun `should save and retrieve race`() {
        // Given
        val race = RaceEntity(
            season = season2023,
            round = 1,
            raceName = "Bahrain Grand Prix",
            date = LocalDate.of(2023, 3, 5),
            circuitName = "Bahrain International Circuit",
            winningDriverId = "max_verstappen",
            winningDriverName = "Max Verstappen",
            winningDriverNationality = "Dutch",
            winningConstructorId = "red_bull",
            winningConstructorName = "Red Bull",
            isSeasonChampionWinner = true
        )

        // When
        val savedRace = raceRepository.save(race)
        val retrievedRace = raceRepository.findById(savedRace.id!!).orElse(null)

        // Then
        assertNotNull(retrievedRace)
        assertEquals(race.round, retrievedRace.round)
        assertEquals(race.raceName, retrievedRace.raceName)
        assertEquals(race.date, retrievedRace.date)
        assertEquals(race.circuitName, retrievedRace.circuitName)
        assertEquals(race.winningDriverName, retrievedRace.winningDriverName)
        assertEquals(race.winningDriverNationality, retrievedRace.winningDriverNationality)
        assertEquals(race.winningConstructorName, retrievedRace.winningConstructorName)
        assertEquals(race.isSeasonChampionWinner, retrievedRace.isSeasonChampionWinner)
    }

    @Test
    fun `should find all races for season ordered by round`() {
        // Given
        val race1 = RaceEntity(
            season = season2023,
            round = 2,
            raceName = "Saudi Arabian Grand Prix",
            date = LocalDate.of(2023, 3, 19),
            circuitName = "Jeddah Corniche Circuit",
            winningDriverId = "sergio_perez",
            winningDriverName = "Sergio Perez",
            winningDriverNationality = "Mexican",
            winningConstructorId = "red_bull",
            winningConstructorName = "Red Bull",
            isSeasonChampionWinner = false
        )
        val race2 = RaceEntity(
            season = season2023,
            round = 1,
            raceName = "Bahrain Grand Prix",
            date = LocalDate.of(2023, 3, 5),
            circuitName = "Bahrain International Circuit",
            winningDriverId = "max_verstappen",
            winningDriverName = "Max Verstappen",
            winningDriverNationality = "Dutch",
            winningConstructorId = "red_bull",
            winningConstructorName = "Red Bull",
            isSeasonChampionWinner = true
        )
        raceRepository.save(race2) // Save in reverse order
        raceRepository.save(race1)

        // When
        val races = raceRepository.findBySeasonYearOrderByRoundAsc(2023)

        // Then
        assertEquals(2, races.size)
        assertTrue(races[0].round < races[1].round)
        assertEquals(1, races[0].round)
        assertEquals(2, races[1].round)
    }

    @Test
    fun `should return empty list when no races exist for season`() {
        // When
        val races = raceRepository.findBySeasonYearOrderByRoundAsc(2023)

        // Then
        assertTrue(races.isEmpty())
    }

    @Test
    fun `should save multiple races in bulk`() {
        // Given
        val races = listOf(
            RaceEntity(
                season = season2023,
                round = 1,
                raceName = "Bahrain Grand Prix",
                date = LocalDate.of(2023, 3, 5),
                circuitName = "Bahrain International Circuit",
                winningDriverId = "max_verstappen",
                winningDriverName = "Max Verstappen",
                winningDriverNationality = "Dutch",
                winningConstructorId = "red_bull",
                winningConstructorName = "Red Bull",
                isSeasonChampionWinner = true
            ),
            RaceEntity(
                season = season2023,
                round = 2,
                raceName = "Saudi Arabian Grand Prix",
                date = LocalDate.of(2023, 3, 19),
                circuitName = "Jeddah Corniche Circuit",
                winningDriverId = "sergio_perez",
                winningDriverName = "Sergio Perez",
                winningDriverNationality = "Mexican",
                winningConstructorId = "red_bull",
                winningConstructorName = "Red Bull",
                isSeasonChampionWinner = false
            )
        )

        // When
        val savedRaces = raceRepository.saveAll(races)
        val retrievedRaces = raceRepository.findBySeasonYearOrderByRoundAsc(2023)

        // Then
        assertEquals(2, savedRaces.size)
        assertEquals(2, retrievedRaces.size)
        assertTrue(retrievedRaces.all { it.season.year == 2023 })
    }
} 