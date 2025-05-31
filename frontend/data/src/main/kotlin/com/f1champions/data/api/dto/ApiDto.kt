package com.f1champions.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Data class representing a season summary from the API.
 * Used for GET /api/seasons endpoint.
 */
@JsonClass(generateAdapter = true)
data class SeasonSummaryApiDto(
    val year: Int,
    @Json(name = "championName")
    val championName: String,
    @Json(name = "championPoints")
    val championPoints: Int,
    @Json(name = "championWins")
    val championWins: Int
)

/**
 * Data class representing a race detail from the API.
 * Used for GET /api/seasons/{year}/races endpoint.
 */
@JsonClass(generateAdapter = true)
data class RaceDetailApiDto(
    val round: Int,
    @Json(name = "raceName")
    val raceName: String,
    val date: LocalDate,
    @Json(name = "circuitName")
    val circuitName: String,
    @Json(name = "winningDriverName")
    val winningDriverName: String,
    @Json(name = "winningDriverNationality")
    val winningDriverNationality: String,
    @Json(name = "winningConstructorName")
    val winningConstructorName: String,
    @Json(name = "isSeasonChampionWinner")
    val isSeasonChampionWinner: Boolean
)

/**
 * Data class representing an error response from the API.
 * Used for all error responses (400, 404, 429, 500).
 */
@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    val timestamp: ZonedDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)