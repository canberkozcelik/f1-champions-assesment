package com.f1champions.service

import com.f1champions.api.dto.SeasonDto
import com.f1champions.api.dto.RaceDto

interface F1DataService {
    /**
     * Checks if seasons data is populated and fetches missing data if needed
     * @return true if data was fetched, false if data was already up to date
     */
    suspend fun ensureSeasonsDataPopulated(): Boolean

    /**
     * Retrieves all seasons data ordered by year
     * @return List of SeasonDto objects
     */
    suspend fun getAllSeasons(): List<SeasonDto>

    /**
     * Retrieves all races for a specific season year
     * @param year The season year to fetch races for
     * @return List of RaceDto objects ordered by round
     * @throws IllegalArgumentException if year is invalid
     * @throws NoSuchElementException if season data is not found
     */
    suspend fun getRacesForSeason(year: Int): List<RaceDto>
} 