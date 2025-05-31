package com.f1champions.domain.model

/**
 * Domain model representing a Formula 1 World Champion for a season.
 *
 * @property year The championship year
 * @property championName The full name of the champion driver
 * @property points The total points scored by the champion
 * @property wins The number of race wins achieved by the champion
 */
data class SeasonChampionInfo(
    val year: Int,
    val championName: String,
    val points: Int,
    val wins: Int,
)