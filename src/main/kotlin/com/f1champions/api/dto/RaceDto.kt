package com.f1champions.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Represents a Formula 1 race and its winner")
data class RaceDto(
    @Schema(description = "Race round number in the season", example = "1")
    @JsonProperty("round")
    val round: Int,

    @Schema(description = "Name of the race", example = "Bahrain Grand Prix")
    @JsonProperty("raceName")
    val raceName: String,

    @Schema(description = "Date of the race", example = "2023-03-05")
    @JsonProperty("date")
    val date: LocalDate,

    @Schema(description = "Name of the circuit", example = "Bahrain International Circuit")
    @JsonProperty("circuitName")
    val circuitName: String,

    @Schema(description = "Full name of the race winner", example = "Max Verstappen")
    @JsonProperty("winningDriverName")
    val winningDriverName: String,

    @Schema(description = "Nationality of the race winner", example = "Dutch")
    @JsonProperty("winningDriverNationality")
    val winningDriverNationality: String,

    @Schema(description = "Name of the winning constructor", example = "Red Bull")
    @JsonProperty("winningConstructorName")
    val winningConstructorName: String,

    @Schema(description = "Indicates if the race winner is also the season champion", example = "true")
    @JsonProperty("isSeasonChampionWinner")
    val isSeasonChampionWinner: Boolean
) 