package com.f1champions.service.impl

import com.f1champions.api.dto.SeasonDto
import com.f1champions.api.dto.RaceDto
import com.f1champions.client.ergast.dto.results.ErgastRaceResultsDto
import com.f1champions.client.ergast.dto.standings.ErgastDriverStandingsDto
import com.f1champions.entity.SeasonEntity
import com.f1champions.entity.RaceEntity
import com.f1champions.repository.SeasonRepository
import com.f1champions.repository.RaceRepository
import com.f1champions.service.F1DataService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Year
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class F1DataServiceImpl(
    private val seasonRepository: SeasonRepository,
    private val raceRepository: RaceRepository,
    private val webClient: WebClient
) : F1DataService {

    override suspend fun ensureSeasonsDataPopulated(): Boolean {
        val currentYear = Year.now().value
        val existingSeasons = seasonRepository.findAll().map { it.year }.toSet()
        val yearsToFetch = (2005..currentYear).filter { it !in existingSeasons }

        if (yearsToFetch.isEmpty()) {
            return false
        }

        yearsToFetch.forEach { year ->
            try {
                val response = webClient.get()
                    .uri("/$year/driverStandings/1.json")
                    .retrieve()
                    .bodyToMono<ErgastDriverStandingsDto>()
                    .awaitSingleOrNull()

                response?.let { ergastResponse ->
                    val championData = ergastResponse.mrData.standingsTable?.standingsLists?.firstOrNull()
                    if (championData != null) {
                        val driverStanding = championData.driverStandings.first()
                        val seasonEntity = SeasonEntity(
                            year = year,
                            championName = "${driverStanding.driver.givenName} ${driverStanding.driver.familyName}",
                            championDriverId = driverStanding.driver.driverId,
                            championPoints = driverStanding.points.toDouble().toInt(),
                            championWins = driverStanding.wins.toInt()
                        )
                        seasonRepository.save(seasonEntity)
                    }
                }
            } catch (e: Exception) {
                // Log error but continue with other years
                println("Error fetching data for year $year: ${e.message}")
            }
        }

        return true
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
            NoSuchElementException("Season data for year $year not found. Please ensure season data is populated first.")
        }

        // Check if races exist for this season
        val existingRaces = raceRepository.findBySeasonYearOrderByRoundAsc(year)
        if (existingRaces.isNotEmpty()) {
            return existingRaces.map { it.toDto() }
        }

        // Fetch race data from Ergast API
        try {
            val response = webClient.get()
                .uri("/$year/results/1.json")
                .retrieve()
                .bodyToMono<ErgastRaceResultsDto>()
                .awaitSingleOrNull()

            if (response == null) {
                throw IllegalStateException("Failed to fetch race data from Ergast API for year $year")
            }

            val races = response.mrData.raceTable.races.map { raceDto ->
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
            }

            // Save all races in a single transaction
            val savedRaces = raceRepository.saveAll(races)
            return savedRaces.map { it.toDto() }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to process race data for year $year: ${e.message}", e)
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