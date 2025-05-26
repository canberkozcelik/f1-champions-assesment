package com.f1champions.client.ergast.dto.results

import com.f1champions.client.ergast.common.ErgastCircuitDto
import com.f1champions.client.ergast.common.ErgastConstructorDto
import com.f1champions.client.ergast.common.ErgastDriverDto
import com.fasterxml.jackson.annotation.JsonProperty

data class ErgastRaceResultsDto(
    @JsonProperty("MRData") val mrData: MRDataRaceResultsDto
)

data class MRDataRaceResultsDto(
    @JsonProperty("xmlns") val xmlns: String,
    @JsonProperty("series") val series: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("limit") val limit: String,
    @JsonProperty("offset") val offset: String,
    @JsonProperty("total") val total: String,
    @JsonProperty("RaceTable") val raceTable: RaceTableDto
)

data class RaceTableDto(
    @JsonProperty("season") val season: String,
    @JsonProperty("Races") val races: List<RaceDto>
)

data class RaceDto(
    @JsonProperty("season") val season: String,
    @JsonProperty("round") val round: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("raceName") val raceName: String,
    @JsonProperty("Circuit") val circuit: ErgastCircuitDto,
    @JsonProperty("date") val date: String,
    @JsonProperty("time") val time: String?,
    @JsonProperty("Results") val results: List<RaceResultDto>
)

data class RaceResultDto(
    @JsonProperty("number") val number: String,
    @JsonProperty("position") val position: String,
    @JsonProperty("points") val points: String,
    @JsonProperty("Driver") val driver: ErgastDriverDto,
    @JsonProperty("Constructor") val constructor: ErgastConstructorDto,
    @JsonProperty("grid") val grid: String,
    @JsonProperty("laps") val laps: String,
    @JsonProperty("status") val status: String
)
