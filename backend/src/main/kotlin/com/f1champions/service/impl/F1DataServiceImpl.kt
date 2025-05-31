package com.f1champions.service.impl

import com.f1champions.api.dto.RaceDto
import com.f1champions.api.dto.SeasonDto
import com.f1champions.client.ergast.dto.standings.ErgastDriverStandingsDto
import com.f1champions.entity.RaceEntity
import com.f1champions.entity.SeasonEntity
import com.f1champions.exception.ErgastApiException
import com.f1champions.exception.ErgastApiInvalidResponseException
import com.f1champions.repository.RaceRepository
import com.f1champions.repository.SeasonRepository
import com.f1champions.service.ErgastApiClient
import com.f1champions.service.F1DataService
import com.f1champions.service.RateLimiterService
import com.f1champions.util.RetryUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter

@Service
class F1DataServiceImpl(
    private val ergastApiClient: ErgastApiClient,
    private val seasonRepository: SeasonRepository,
    private val raceRepository: RaceRepository,
    private val rateLimiterService: RateLimiterService
) : F1DataService {

    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun fetchDriverStandingsWithRetry(year: Int): ErgastDriverStandingsDto {
        return RetryUtil.retryWithBackoff(
            operationName = "fetch driver standings for year $year"
        ) {
            rateLimiterService.executeWithRateLimit {
                ergastApiClient.getDriverStandings(year)
            }
        }
    }

    override suspend fun ensureSeasonsDataPopulated(): Boolean {
        val currentYear = Year.now().value
        val existingSeasons = seasonRepository.findAll().map { it.year }.toSet()
        val yearsToFetch = (2005..currentYear).filter { it !in existingSeasons }

        if (yearsToFetch.isEmpty()) {
            logger.info("All seasons from 2005 to $currentYear are already populated")
            return true
        }

        logger.info("Starting to populate data for years: ${yearsToFetch.joinToString()}")
        var dataFetched = false

        yearsToFetch.forEach { year ->
            try {
                logger.info("Processing year $year")
                val standings = fetchDriverStandingsWithRetry(year)
                val championData = standings.mrData.standingsTable.standingsLists.first()
                val driverStanding = championData.driverStandings.first()
                try {
                    val points = driverStanding.points.toDoubleOrNull()
                    val wins = driverStanding.wins.toIntOrNull()
                    if (points == null || wins == null) {
                        throw ErgastApiInvalidResponseException(
                            "Invalid points or wins format in response for year $year. " +
                                "Points: '${driverStanding.points}', Wins: '${driverStanding.wins}'"
                        )
                    }
                    val seasonEntity = SeasonEntity(
                        year = year,
                        championName = "${driverStanding.driver.givenName} ${driverStanding.driver.familyName}",
                        championDriverId = driverStanding.driver.driverId,
                        championPoints = points.toInt(),
                        championWins = wins
                    )
                    seasonRepository.save(seasonEntity)
                    logger.info("Successfully saved season data for year $year")
                    dataFetched = true
                } catch (e: Exception) {
                    logger.error("Error processing driver standings data for year $year: ${e.message}")
                    throw ErgastApiInvalidResponseException(
                        "Invalid points or wins format in response for year $year. " +
                            "Points: '${driverStanding.points}', Wins: '${driverStanding.wins}'",
                        e
                    )
                }
            } catch (e: ErgastApiException) {
                logger.error("Error fetching season data for year $year: ${e.message}")
                // Continue to next year
            }
        }

        logger.info("Finished populating data. Successfully fetched data: $dataFetched")
        return dataFetched
    }

    override suspend fun getAllSeasons(): List<SeasonDto> {
        return seasonRepository.findAllByOrderByYearAsc()
            .map { it.toDto() }
    }

    override suspend fun getRacesForSeason(year: Int): List<RaceDto> {
        // Validate year
        if (year < 2005 || year > Year.now().value) {
            throw IllegalArgumentException("Invalid year: $year. Must be between 2005 and current year.")
        }

        // Check if season exists
        val season = seasonRepository.findById(year).orElseThrow {
            NoSuchElementException(
                "Season data for year $year not found. Please ensure season data is populated first."
            )
        }

        // Check if races exist for this season
        val existingRaces = raceRepository.findBySeasonYearOrderByRoundAsc(year)
        if (existingRaces.isNotEmpty()) {
            return existingRaces.map { it.toDto() }
        }

        // Fetch race data from Ergast API with rate limiting and retry
        try {
            val response = RetryUtil.retryWithBackoff(
                operationName = "fetch race results for year $year"
            ) {
                rateLimiterService.executeWithRateLimit {
                    ergastApiClient.getRaceResults(year)
                }
            }

            val races = response.mrData.raceTable.races.map { raceDto ->
                try {
                    val winningResult = raceDto.results.first()
                    val winningDriver = winningResult.driver
                    val winningConstructor = winningResult.constructor

                    RaceEntity(
                        season = season,
                        round = raceDto.round.toInt(),
                        raceName = raceDto.raceName,
                        date = LocalDate.parse(raceDto.date, DateTimeFormatter.ISO_DATE),
                        circuitName = raceDto.circuit.circuitName,
                        winningDriverId = winningDriver.driverId,
                        winningDriverName = "${winningDriver.givenName} ${winningDriver.familyName}",
                        winningDriverNationality = winningDriver.nationality,
                        winningConstructorId = winningConstructor.constructorId,
                        winningConstructorName = winningConstructor.name,
                        isSeasonChampionWinner = winningDriver.driverId == season.championDriverId
                    )
                } catch (e: Exception) {
                    throw ErgastApiException("Failed to process race data for year $year: ${e.message}", e)
                }
            }

            // Save all races in a single transaction
            val savedRaces = raceRepository.saveAll(races)
            return savedRaces.map { it.toDto() }
        } catch (e: ErgastApiException) {
            // Re-throw ErgastApiException as is, since it's already the right type
            throw e
        }
    }

    private fun SeasonEntity.toDto() = SeasonDto(
        year = year,
        championName = championName,
        championPoints = championPoints,
        championWins = championWins
    )

    private fun RaceEntity.toDto() = RaceDto(
        round = round,
        raceName = raceName,
        date = date,
        circuitName = circuitName,
        winningDriverName = winningDriverName,
        winningDriverNationality = winningDriverNationality,
        winningConstructorName = winningConstructorName,
        isSeasonChampionWinner = isSeasonChampionWinner
    )
}
