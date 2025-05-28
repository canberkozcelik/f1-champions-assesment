package com.f1champions.data.mapper

import com.f1champions.data.api.dto.SeasonSummaryApiDto
import com.f1champions.domain.model.SeasonChampionInfo

/**
 * Maps API DTOs to domain models for season-related data.
 */
object SeasonMapper {
    /**
     * Converts a [SeasonSummaryApiDto] to a [SeasonChampionInfo].
     *
     * @param dto The API DTO containing season champion data
     * @return A domain model representing the season champion
     */
    private fun toSeasonChampionInfo(dto: SeasonSummaryApiDto): SeasonChampionInfo {
        return SeasonChampionInfo(
            year = dto.year,
            championName = dto.championName,
            points = dto.championPoints,
            wins = dto.championWins,
        )
    }

    /**
     * Converts a list of [SeasonSummaryApiDto] to a list of [SeasonChampionInfo].
     *
     * @param dtos The list of API DTOs containing season champion data
     * @return A list of domain models representing season champions
     */
    fun toSeasonChampionInfoList(dtos: List<SeasonSummaryApiDto>): List<SeasonChampionInfo> {
        return dtos.map { toSeasonChampionInfo(it) }
    }
} 