package com.f1champions.client.ergast.dto.standings

import com.f1champions.client.ergast.common.ErgastConstructorDto
import com.f1champions.client.ergast.common.ErgastDriverDto
import com.fasterxml.jackson.annotation.JsonProperty

data class ErgastDriverStandingsDto(
    @JsonProperty("MRData") val mrData: MRDataStandingsDto
)

data class MRDataStandingsDto(
    @JsonProperty("xmlns") val xmlns: String,
    @JsonProperty("series") val series: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("limit") val limit: String,
    @JsonProperty("offset") val offset: String,
    @JsonProperty("total") val total: String,
    @JsonProperty("StandingsTable") val standingsTable: StandingsTableDto
)

data class StandingsTableDto(
    @JsonProperty("season") val season: String,
    @JsonProperty("StandingsLists") val standingsLists: List<StandingsListDto>
)

data class StandingsListDto(
    @JsonProperty("season") val season: String,
    @JsonProperty("round") val round: String,
    @JsonProperty("DriverStandings") val driverStandings: List<DriverStandingDto>
)

data class DriverStandingDto(
    @JsonProperty("position") val position: String,
    @JsonProperty("points") val points: String,
    @JsonProperty("wins") val wins: String,
    @JsonProperty("Driver") val driver: ErgastDriverDto, // Reusing common DTO
    @JsonProperty("Constructors") val constructors: List<ErgastConstructorDto> // Reusing common DTO
)