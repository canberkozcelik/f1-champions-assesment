package com.f1champions.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "races")
data class RaceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "season_year",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_races_season")
    )
    val season: SeasonEntity,

    @Column(name = "round", nullable = false)
    val round: Int,

    @Column(name = "race_name", nullable = false)
    val raceName: String,

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @Column(name = "circuit_name", nullable = false)
    val circuitName: String,

    @Column(name = "winning_driver_id", nullable = false)
    val winningDriverId: String,

    @Column(name = "winning_driver_name", nullable = false)
    val winningDriverName: String,

    @Column(name = "winning_driver_nationality", nullable = false)
    val winningDriverNationality: String,

    @Column(name = "winning_constructor_id", nullable = false)
    val winningConstructorId: String,

    @Column(name = "winning_constructor_name", nullable = false)
    val winningConstructorName: String,

    @Column(name = "is_season_champion_winner", nullable = false)
    var isSeasonChampionWinner: Boolean = false
) 