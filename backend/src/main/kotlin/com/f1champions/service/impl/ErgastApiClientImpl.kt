package com.f1champions.service.impl

import com.f1champions.client.ergast.dto.results.ErgastRaceResultsDto
import com.f1champions.client.ergast.dto.standings.ErgastDriverStandingsDto
import com.f1champions.exception.ErgastApiDataNotFoundException
import com.f1champions.exception.ErgastApiException
import com.f1champions.exception.ErgastApiInvalidResponseException
import com.f1champions.exception.ErgastApiRateLimitException
import com.f1champions.exception.ErgastApiServiceUnavailableException
import com.f1champions.service.ErgastApiClient
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Duration

/**
 * Implementation of [ErgastApiClient] that uses Spring's WebClient to make HTTP requests to the Ergast F1 API.
 *
 * This implementation handles:
 * - HTTP communication with the Ergast API
 * - Response validation
 * - Error handling and mapping to domain-specific exceptions
 * - Timeout configuration
 */
@Service
class ErgastApiClientImpl(
    private val webClient: WebClient,
    @Value("\${ergast.api.endpoints.driver-standings}") private val driverStandingsEndpoint: String,
    @Value("\${ergast.api.endpoints.race-results}") private val raceResultsEndpoint: String
) : ErgastApiClient {

    override suspend fun getDriverStandings(year: Int): ErgastDriverStandingsDto {
        return try {
            val endpoint = driverStandingsEndpoint.replace("{year}", year.toString())

            val response = webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono<ErgastDriverStandingsDto>()
                .timeout(Duration.ofSeconds(10))
                .awaitSingleOrNull()

            if (response == null) {
                throw ErgastApiDataNotFoundException("No driver standings data found for year $year")
            }

            validateDriverStandingsResponse(response, year)
            response
        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> throw ErgastApiDataNotFoundException(
                    "Driver standings data not found for year $year",
                    e
                )

                HttpStatus.TOO_MANY_REQUESTS -> {
                    val retryAfter = e.headers.getFirst("Retry-After")?.toIntOrNull()
                    throw ErgastApiRateLimitException(
                        "Rate limit exceeded for Ergast API",
                        retryAfter,
                        e
                    )
                }

                HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT ->
                    throw ErgastApiServiceUnavailableException(
                        "Ergast API service is currently unavailable",
                        e
                    )

                else -> throw ErgastApiServiceUnavailableException(
                    "Error fetching driver standings data: ${e.message}",
                    e
                )
            }
        } catch (e: Exception) {
            when (e) {
                is ErgastApiException -> throw e
                else -> throw ErgastApiServiceUnavailableException(
                    "Error fetching driver standings data: ${e.message}",
                    e
                )
            }
        }
    }

    override suspend fun getRaceResults(year: Int): ErgastRaceResultsDto {
        return try {
            val endpoint = raceResultsEndpoint.replace("{year}", year.toString())

            val response = webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono<ErgastRaceResultsDto>()
                .timeout(Duration.ofSeconds(10))
                .awaitSingleOrNull()

            if (response == null) {
                throw ErgastApiDataNotFoundException("No race results data found for year $year")
            }

            validateRaceResultsResponse(response, year)
            response
        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> throw ErgastApiDataNotFoundException(
                    "Race results data not found for year $year",
                    e
                )

                HttpStatus.TOO_MANY_REQUESTS -> {
                    val retryAfter = e.headers.getFirst("Retry-After")?.toIntOrNull()
                    throw ErgastApiRateLimitException(
                        "Rate limit exceeded for Ergast API",
                        retryAfter,
                        e
                    )
                }

                HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT ->
                    throw ErgastApiServiceUnavailableException(
                        "Ergast API service is currently unavailable",
                        e
                    )

                else -> throw ErgastApiServiceUnavailableException(
                    "Error fetching race results data: ${e.message}",
                    e
                )
            }
        } catch (e: Exception) {
            when (e) {
                is ErgastApiException -> throw e
                else -> throw ErgastApiServiceUnavailableException(
                    "Error fetching race results data: ${e.message}",
                    e
                )
            }
        }
    }

    private fun validateDriverStandingsResponse(response: ErgastDriverStandingsDto, year: Int) {
        val standingsList = response.mrData.standingsTable.standingsLists
        if (standingsList.isEmpty()) {
            throw ErgastApiDataNotFoundException("No driver standings found for year $year")
        }

        val driverStandings = standingsList.first().driverStandings
        if (driverStandings.isEmpty()) {
            throw ErgastApiDataNotFoundException("No champion data found for year $year")
        }

        // Validate required fields
        val champion = driverStandings.first()
        if (champion.driver.driverId.isBlank() ||
            champion.driver.givenName.isBlank() ||
            champion.driver.familyName.isBlank()
        ) {
            throw ErgastApiInvalidResponseException(
                "Invalid champion data structure in response for year $year"
            )
        }
    }

    private fun validateRaceResultsResponse(response: ErgastRaceResultsDto, year: Int) {
        val races = response.mrData.raceTable.races
        if (races.isEmpty()) {
            throw ErgastApiDataNotFoundException("No race results found for year $year")
        }

        // Validate each race has required data
        races.forEach { race ->
            if (race.results.isEmpty()) {
                throw ErgastApiInvalidResponseException("Race ${race.raceName} has no results")
            }

            val winningResult = race.results.first()
            if (winningResult.driver.driverId.isBlank() ||
                winningResult.driver.givenName.isBlank() ||
                winningResult.driver.familyName.isBlank() ||
                winningResult.constructor.constructorId.isBlank() ||
                winningResult.constructor.name.isBlank()
            ) {
                throw ErgastApiInvalidResponseException("Invalid race result data structure for race ${race.raceName}")
            }
        }
    }
}
