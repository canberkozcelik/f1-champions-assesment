package com.f1champions.controller

import com.f1champions.api.dto.RaceDto
import com.f1champions.api.dto.SeasonDto
import com.f1champions.service.F1DataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/seasons")
@Tag(name = "F1 Seasons", description = "APIs for Formula 1 seasons and races")
class F1Controller(
    private val f1DataService: F1DataService
) {
    @Operation(
        summary = "Get all F1 seasons",
        description = "Retrieves a list of all Formula 1 seasons with their champions",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved seasons",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SeasonDto::class)
                )]
            )
        ]
    )
    @GetMapping
    suspend fun getAllSeasons(): ResponseEntity<List<SeasonDto>> {
        val seasons = f1DataService.getAllSeasons()
        return ResponseEntity.ok(seasons)
    }

    @Operation(
        summary = "Get races for a specific season",
        description = "Retrieves all races and their winners for a given Formula 1 season",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved races",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = RaceDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Season not found"
            )
        ]
    )
    @GetMapping("/{year}/races")
    suspend fun getRacesForSeason(
        @Parameter(description = "Year of the F1 season", example = "2023")
        @PathVariable year: Int
    ): ResponseEntity<List<RaceDto>> {
        val races = f1DataService.getRacesForSeason(year)
        return ResponseEntity.ok(races)
    }
} 