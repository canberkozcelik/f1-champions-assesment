package com.f1champions.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents a Formula 1 season and its champion")
data class SeasonDto(
    @Schema(description = "The year of the F1 season", example = "2023")
    @JsonProperty("year")
    val year: Int,

    @Schema(description = "Full name of the season champion", example = "Max Verstappen")
    @JsonProperty("championName")
    val championName: String,

    @Schema(description = "Total points scored by the champion", example = "454")
    @JsonProperty("championPoints")
    val championPoints: Int,

    @Schema(description = "Number of race wins by the champion", example = "19")
    @JsonProperty("championWins")
    val championWins: Int
)