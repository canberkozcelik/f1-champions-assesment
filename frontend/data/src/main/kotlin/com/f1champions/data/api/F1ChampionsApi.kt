package com.f1champions.data.api

import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.data.api.dto.SeasonSummaryApiDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for F1 Champions API endpoints.
 * All functions are suspend functions to support coroutines.
 * 
 * Note: The /api prefix is handled by the BASE_URL configuration.
 * This interface focuses on the domain-specific endpoints.
 */
interface F1ChampionsApi {
    /**
     * Fetches the list of F1 World Champions.
     * GET /seasons
     *
     * @return List of [SeasonSummaryApiDto] containing champion information for each season
     */
    @GET("seasons")
    suspend fun getSeasons(): List<SeasonSummaryApiDto>

    /**
     * Fetches the race results for a specific season.
     * GET /seasons/{year}/races
     *
     * @param year The season year to fetch races for
     * @return List of [RaceDetailApiDto] containing race information for the season
     */
    @GET("seasons/{year}/races")
    suspend fun getRacesForSeason(@Path("year") year: Int): List<RaceDetailApiDto>
} 