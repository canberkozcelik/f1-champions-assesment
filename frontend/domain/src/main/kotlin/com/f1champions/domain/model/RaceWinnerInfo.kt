package com.f1champions.domain.model

/**
 * Domain model representing a Formula 1 race winner.
 *
 * @property raceName The name of the Grand Prix
 * @property circuitName The name of the circuit where the race was held
 * @property date The date of the race
 * @property winnerName The full name of the winning driver
 * @property constructorName The name of the constructor (team) the winner drove for
 * @property isSeasonChampionWinner The winner of the race is also seasons champion
 */
data class RaceWinnerInfo(
    val raceName: String,
    val circuitName: String,
    val date: String,
    val winnerName: String,
    val constructorName: String,
    val isSeasonChampionWinner: Boolean,
) 