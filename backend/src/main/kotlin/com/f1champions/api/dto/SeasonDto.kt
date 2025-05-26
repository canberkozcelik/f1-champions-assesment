package com.f1champions.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Represents a Formula 1 season and its champion",
    example = """
        {
            "year": 2023,
            "championName": "Max Verstappen",
            "championPoints": 454,
            "championWins": 19
        }
    """
)
data class SeasonDto(
    @Schema(
        description = "The year of the F1 season",
        example = "2023",
        minimum = "2005",
        maximum = "2024"
    )
    @JsonProperty("year")
    val year: Int,

    @Schema(
        description = "Full name of the season champion (given name + family name)",
        example = "Max Verstappen",
        pattern = "^[A-Za-z ]+$"
    )
    @JsonProperty("championName")
    val championName: String,

    @Schema(
        description = "Total points scored by the champion in the season",
        example = "454",
        minimum = "0"
    )
    @JsonProperty("championPoints")
    val championPoints: Int,

    @Schema(
        description = "Number of race wins achieved by the champion in the season",
        example = "19",
        minimum = "0",
        maximum = "24"
    )
    @JsonProperty("championWins")
    val championWins: Int
)
