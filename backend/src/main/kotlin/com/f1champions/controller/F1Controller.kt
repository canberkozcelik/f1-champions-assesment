package com.f1champions.controller

import com.f1champions.api.dto.RaceDto
import com.f1champions.api.dto.SeasonDto
import com.f1champions.service.F1DataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/seasons")
@Tag(
    name = "F1 Seasons",
    description = "APIs for Formula 1 seasons and races. Note: Season data is available from 2005 onwards."
)
class F1Controller(
    private val f1DataService: F1DataService
) {
    @Operation(
        summary = "Get all F1 seasons",
        description = "Retrieves a list of all Formula 1 seasons with their champions. " +
            "Only seasons from 2005 onwards are available as per the Ergast API data.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved seasons",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(
                            schema = Schema(implementation = SeasonDto::class)
                        )
                    )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error occurred while fetching season data",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    )
                ]
            )
        ]
    )
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getAllSeasons(): ResponseEntity<List<SeasonDto>> {
        val seasons = f1DataService.getAllSeasons()
        return ResponseEntity.ok(seasons)
    }

    @Operation(
        summary = "Get races for a specific season",
        description = "Retrieves all races and their winners for a given Formula 1 season. " +
            "The year must be between 2005 and the current year. " +
            "If the season data is not yet populated, you'll receive a 404 error.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved races for the season",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(
                            schema = Schema(implementation = RaceDto::class)
                        )
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid year provided. Year must be between 2005 and current year.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Season not found. This could mean either: " +
                    "1) The year is valid but the season data hasn't been populated yet, or " +
                    "2) The season data couldn't be retrieved from the external API.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error occurred while processing the request",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    )
                ]
            )
        ]
    )
    @GetMapping("/{year}/races")
    suspend fun getRacesForSeason(
        @Parameter(
            description = "Year of the F1 season",
            example = "2023",
            schema = Schema(
                type = "integer",
                minimum = "2005",
                maximum = "2024",
                description = "Must be between 2005 and current year"
            )
        )
        @PathVariable
        year: Int
    ): ResponseEntity<List<RaceDto>> {
        val races = f1DataService.getRacesForSeason(year)
        return ResponseEntity.ok(races)
    }
}

// This class is needed for Swagger documentation of error responses
@Schema(description = "Error response structure")
data class ErrorResponse(
    @Schema(description = "HTTP status code", example = "404")
    val status: Int,

    @Schema(description = "Error type", example = "Not Found")
    val error: String,

    @Schema(
        description = "Detailed error message",
        example = "Season data for year 2023 not found. Please ensure season data is populated first."
    )
    val message: String
)
