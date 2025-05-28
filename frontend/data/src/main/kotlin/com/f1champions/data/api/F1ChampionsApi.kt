package com.f1champions.data.api

import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.data.api.dto.SeasonSummaryApiDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for F1 Champions API endpoints.
 * All functions are suspend functions to support coroutines.
 */
interface F1ChampionsApi {
    /**
     * Fetches the list of F1 World Champions.
     * GET /api/seasons
     *
     * @return List of [SeasonSummaryApiDto] containing champion information for each season
     */
    @GET("api/seasons")
    suspend fun getSeasons(): List<SeasonSummaryApiDto>

    /**
     * Fetches the race results for a specific season.
     * GET /api/seasons/{year}/races
     *
     * @param year The season year to fetch races for
     * @return List of [RaceDetailApiDto] containing race information for the season
     */
    @GET("api/seasons/{year}/races")
    suspend fun getRacesForSeason(@Path("year") year: Int): List<RaceDetailApiDto>
} 