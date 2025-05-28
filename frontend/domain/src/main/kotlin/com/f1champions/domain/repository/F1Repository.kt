package com.f1champions.domain.repository

import com.f1champions.domain.exception.F1Exception
import com.f1champions.domain.exception.F1InvalidSeasonException
import com.f1champions.domain.exception.F1NetworkException
import com.f1champions.domain.exception.F1SeasonNotFoundException
import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.domain.model.SeasonChampionInfo

/**
 * Repository interface for accessing Formula 1 championship data.
 * This interface defines the contract for retrieving season champions and race winners.
 */
interface F1Repository {
    /**
     * Retrieves a list of all Formula 1 World Champions.
     *
     * @return List of [SeasonChampionInfo] containing champion information for each season
     * @throws com.f1champions.domain.exception.F1NetworkException if there's a network error
     * @throws com.f1champions.domain.exception.F1UnexpectedException if there's an unexpected error
     */
    suspend fun getSeasonChampions(): List<SeasonChampionInfo>

    /**
     * Retrieves a list of race winners for a specific season.
     *
     * @param year The championship year to fetch race winners for
     * @return List of [RaceWinnerInfo] containing race winner information for the season
     * @throws com.f1champions.domain.exception.F1NetworkException if there's a network error
     * @throws com.f1champions.domain.exception.F1InvalidSeasonException if the season year is invalid
     * @throws com.f1champions.domain.exception.F1SeasonNotFoundException if the season is not found
     * @throws com.f1champions.domain.exception.F1UnexpectedException if there's an unexpected error
     */
    suspend fun getRaceWinners(year: Int): List<RaceWinnerInfo>
} 