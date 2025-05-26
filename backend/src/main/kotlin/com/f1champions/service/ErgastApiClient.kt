package com.f1champions.service

import com.f1champions.client.ergast.dto.results.ErgastRaceResultsDto
import com.f1champions.client.ergast.dto.standings.ErgastDriverStandingsDto
import com.f1champions.exception.ErgastApiDataNotFoundException
import com.f1champions.exception.ErgastApiInvalidResponseException
import com.f1champions.exception.ErgastApiRateLimitException
import com.f1champions.exception.ErgastApiServiceUnavailableException

/**
 * Interface for interacting with the Ergast F1 API.
 * Provides methods to fetch driver standings and race results.
 */
interface ErgastApiClient {
    /**
     * Fetches the driver standings for a given year.
     *
     * @param year The year to fetch standings for
     * @return The driver standings data
     * @throws ErgastApiDataNotFoundException if no data is found for the year
     * @throws ErgastApiRateLimitException if the API rate limit is exceeded
     * @throws ErgastApiServiceUnavailableException if the API is unavailable
     * @throws ErgastApiInvalidResponseException if the response data is invalid
     */
    suspend fun getDriverStandings(year: Int): ErgastDriverStandingsDto

    /**
     * Fetches the race results for a given year.
     *
     * @param year The year to fetch results for
     * @return The race results data
     * @throws ErgastApiDataNotFoundException if no data is found for the year
     * @throws ErgastApiRateLimitException if the API rate limit is exceeded
     * @throws ErgastApiServiceUnavailableException if the API is unavailable
     * @throws ErgastApiInvalidResponseException if the response data is invalid
     */
    suspend fun getRaceResults(year: Int): ErgastRaceResultsDto
}
