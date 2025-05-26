package com.f1champions.repository

import com.f1champions.entity.SeasonEntity
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.Year
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class SeasonRepositoryTest {

    @Autowired
    private lateinit var seasonRepository: SeasonRepository

    @Test
    fun `should save and retrieve season`() {
        // Given
        val season = SeasonEntity(
            year = 2023,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )

        // When
        seasonRepository.save(season)
        val retrievedSeason = seasonRepository.findById(2023).orElse(null)

        // Then
        assertNotNull(retrievedSeason)
        assertEquals(season.year, retrievedSeason.year)
        assertEquals(season.championName, retrievedSeason.championName)
        assertEquals(season.championDriverId, retrievedSeason.championDriverId)
        assertEquals(season.championPoints, retrievedSeason.championPoints)
        assertEquals(season.championWins, retrievedSeason.championWins)
    }

    @Test
    fun `should find all seasons ordered by year`() {
        // Given
        val season2022 = SeasonEntity(
            year = 2022,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 15
        )
        val season2023 = SeasonEntity(
            year = 2023,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )
        seasonRepository.save(season2023)
        seasonRepository.save(season2022)

        // When
        val seasons = seasonRepository.findAllByOrderByYearAsc()

        // Then
        assertEquals(2, seasons.size)
        assertTrue(seasons[0].year < seasons[1].year)
        assertEquals(2022, seasons[0].year)
        assertEquals(2023, seasons[1].year)
    }

    @Test
    fun `should return empty list when no seasons exist`() {
        // When
        val seasons = seasonRepository.findAllByOrderByYearAsc()

        // Then
        assertTrue(seasons.isEmpty())
    }

    @Test
    fun `should return null when finding non-existent season`() {
        // When
        val season = seasonRepository.findById(9999).orElse(null)

        // Then
        assertNull(season)
    }

    @Test
    fun `should handle minimum valid year`() {
        // Given
        val season = SeasonEntity(
            year = 2005, // First supported season
            championName = "Fernando Alonso",
            championDriverId = "fernando_alonso",
            championPoints = 133,
            championWins = 7
        )

        // When
        seasonRepository.save(season)
        val retrievedSeason = seasonRepository.findById(2005).orElse(null)

        // Then
        assertNotNull(retrievedSeason)
        assertEquals(2005, retrievedSeason.year)
    }

    @Test
    fun `should handle current year`() {
        // Given
        val currentYear = Year.now().value
        val season = SeasonEntity(
            year = currentYear,
            championName = "Max Verstappen",
            championDriverId = "max_verstappen",
            championPoints = 454,
            championWins = 19
        )

        // When
        seasonRepository.save(season)
        val retrievedSeason = seasonRepository.findById(currentYear).orElse(null)

        // Then
        assertNotNull(retrievedSeason)
        assertEquals(currentYear, retrievedSeason.year)
    }

    @Test
    fun `should save multiple seasons in bulk`() {
        // Given
        val seasons = listOf(
            SeasonEntity(
                year = 2021,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 395,
                championWins = 10
            ),
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

        // When
        val savedSeasons = seasonRepository.saveAll(seasons)
        val retrievedSeasons = seasonRepository.findAllByOrderByYearAsc()

        // Then
        assertEquals(3, savedSeasons.size)
        assertEquals(3, retrievedSeasons.size)
        assertTrue(retrievedSeasons.all { it.year in 2021..2023 })
    }
}
