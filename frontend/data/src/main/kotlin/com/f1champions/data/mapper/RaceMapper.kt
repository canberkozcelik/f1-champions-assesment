package com.f1champions.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.domain.model.RaceWinnerInfo
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Maps API DTOs to domain models for race-related data.
 */
object RaceMapper {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

    /**
     * Converts a [RaceDetailApiDto] to a [RaceWinnerInfo].
     *
     * @param dto The API DTO containing race winner data
     * @return A domain model representing the race winner
     */
    private fun toRaceWinnerInfo(dto: RaceDetailApiDto): RaceWinnerInfo {
        return RaceWinnerInfo(
            raceName = dto.raceName,
            circuitName = dto.circuitName,
            date = dto.date.format(DATE_FORMATTER),
            winnerName = dto.winningDriverName,
            constructorName = dto.winningConstructorName,
            isSeasonChampionWinner = dto.isSeasonChampionWinner
        )
    }

    /**
     * Converts a list of [RaceDetailApiDto] to a list of [RaceWinnerInfo].
     *
     * @param dtos The list of API DTOs containing race winner data
     * @return A list of domain models representing race winners
     */
    fun toRaceWinnerInfoList(dtos: List<RaceDetailApiDto>): List<RaceWinnerInfo> {
        return dtos.map { toRaceWinnerInfo(it) }
    }
} 