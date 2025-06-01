package com.f1champions.service.impl.cache

import com.f1champions.config.RedisTestConfig
import com.f1champions.entity.RaceEntity
import com.f1champions.entity.SeasonEntity
import com.f1champions.repository.RaceRepository
import com.f1champions.repository.SeasonRepository
import com.f1champions.service.ErgastApiClient
import com.f1champions.service.RateLimiterService
import com.f1champions.service.impl.F1DataServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate
import java.time.Year
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "spring.cache.type=redis"
    ]
)
@Import(RedisTestConfig::class)
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.yml"])
class F1DataServiceCacheTest {

    @Autowired
    private lateinit var cacheManager: CacheManager

    private lateinit var seasonRepository: SeasonRepository
    private lateinit var raceRepository: RaceRepository
    private lateinit var ergastApiClient: ErgastApiClient
    private lateinit var rateLimiterService: RateLimiterService
    private lateinit var f1DataService: F1DataServiceImpl

    @BeforeEach
    fun setup() {
        // Clear all caches before each test
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }

        // Setup mocks
        seasonRepository = mockk()
        raceRepository = mockk()
        ergastApiClient = mockk()
        rateLimiterService = mockk()
        f1DataService = F1DataServiceImpl(ergastApiClient, seasonRepository, raceRepository, rateLimiterService)

        // Default rate limiter behavior
        coEvery {
            rateLimiterService.executeWithRateLimit<Any>(any())
        } coAnswers {
            val operation = firstArg<suspend () -> Any>()
            operation()
        }
    }

    @Test
    fun `getAllSeasons should cache results`() = runBlocking {
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
        val result1 = f1DataService.getAllSeasons()
        val result2 = f1DataService.getAllSeasons()

        // Then
        assertEquals(2, result1.size)
        assertEquals(2, result2.size)
        coVerify(exactly = 1) { seasonRepository.findAllByOrderByYearAsc() } // Should only be called once

        // Verify cache
        val cachedSeasons = cacheManager.getCache("seasons")?.get("all")
        assertNotNull(cachedSeasons)
    }

    @Test
    fun `getRacesForSeason should cache results`() = runBlocking {
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
        val result1 = f1DataService.getRacesForSeason(year)
        val result2 = f1DataService.getRacesForSeason(year)

        // Then
        assertEquals(1, result1.size)
        assertEquals(1, result2.size)
        coVerify(exactly = 1) { raceRepository.findBySeasonYearOrderByRoundAsc(year) } // Should only be called once

        // Verify cache
        val cachedRaces = cacheManager.getCache("races")?.get(year.toString())
        assertNotNull(cachedRaces)
    }

    @Test
    fun `ensureSeasonsDataPopulated should evict all caches`() = runBlocking {
        // Given
        val currentYear = Year.now().value
        val seasons = listOf(
            SeasonEntity(
                year = currentYear,
                championName = "Max Verstappen",
                championDriverId = "max_verstappen",
                championPoints = 454,
                championWins = 19
            )
        )
        coEvery { seasonRepository.findAll() } returns seasons
        coEvery { seasonRepository.findAllByOrderByYearAsc() } returns seasons

        // When
        f1DataService.getAllSeasons() // Populate seasons cache
        f1DataService.ensureSeasonsDataPopulated() // Should evict all caches
        f1DataService.getAllSeasons() // Should hit database again

        // Then
        coVerify(exactly = 2) { seasonRepository.findAllByOrderByYearAsc() } // Called twice because cache was evicted
    }
}
