package com.f1champions.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
    description = "Represents a Formula 1 race and its winner",
    example = """
        {
            "round": 1,
            "raceName": "Bahrain Grand Prix",
            "date": "2023-03-05",
            "circuitName": "Bahrain International Circuit",
            "winningDriverName": "Max Verstappen",
            "winningDriverNationality": "Dutch",
            "winningConstructorName": "Red Bull",
            "isSeasonChampionWinner": true
        }
    """
)
data class RaceDto(
    @Schema(
        description = "Race round number in the season (1-based index)",
        example = "1",
        minimum = "1",
        maximum = "24"
    )
    @JsonProperty("round")
    val round: Int,

    @Schema(
        description = "Official name of the race, including 'Grand Prix' suffix",
        example = "Bahrain Grand Prix",
        pattern = "^[A-Za-z ]+ Grand Prix$"
    )
    @JsonProperty("raceName")
    val raceName: String,

    @Schema(
        description = "Date when the race was held (in ISO-8601 format)",
        example = "2023-03-05",
        type = "string",
        format = "date"
    )
    @JsonProperty("date")
    val date: LocalDate,

    @Schema(
        description = "Official name of the circuit where the race was held",
        example = "Bahrain International Circuit",
        pattern = "^[A-Za-z ]+ Circuit$"
    )
    @JsonProperty("circuitName")
    val circuitName: String,

    @Schema(
        description = "Full name of the race winner (given name + family name)",
        example = "Max Verstappen",
        pattern = "^[A-Za-z ]+$"
    )
    @JsonProperty("winningDriverName")
    val winningDriverName: String,

    @Schema(
        description = "Nationality of the race winner",
        example = "Dutch",
        pattern = "^[A-Za-z ]+$"
    )
    @JsonProperty("winningDriverNationality")
    val winningDriverNationality: String,

    @Schema(
        description = "Name of the constructor (team) that won the race",
        example = "Red Bull",
        pattern = "^[A-Za-z ]+$"
    )
    @JsonProperty("winningConstructorName")
    val winningConstructorName: String,

    @Schema(
        description = "Indicates if the race winner is also the season champion",
        example = "true",
        type = "boolean"
    )
    @JsonProperty("isSeasonChampionWinner")
    val isSeasonChampionWinner: Boolean
)
