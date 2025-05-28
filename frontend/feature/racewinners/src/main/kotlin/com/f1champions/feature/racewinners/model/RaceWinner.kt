package com.f1champions.feature.racewinners.model

/**
 * UI model representing a race winner for display in the UI.
 * This model is specifically designed for the race winners screen and contains
 * only the data needed for display purposes.
 *
 * @property raceName The name of the race (e.g., "Monaco Grand Prix")
 * @property date The formatted date of the race
 * @property winner Information about the race winner
 */
data class RaceWinner(
    val raceName: String,
    val date: String,
    val winner: Winner
)

/**
 * UI model representing the winner of a race.
 *
 * @property fullName The winner's name
 * @property constructor The constructor (team) name
 */
data class Winner(
    val fullName: String,
    val constructor: String
) 