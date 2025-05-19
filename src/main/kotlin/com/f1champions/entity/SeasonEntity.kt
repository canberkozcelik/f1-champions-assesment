package com.f1champions.entity

import jakarta.persistence.*

@Entity
@Table(name = "seasons")
data class SeasonEntity(
    @Id
    @Column(name = "`year`", nullable = false)
    val year: Int,

    @Column(name = "champion_name", nullable = false)
    val championName: String,

    @Column(name = "champion_driver_id", nullable = false)
    val championDriverId: String,

    @Column(name = "champion_points", nullable = false)
    val championPoints: Int,

    @Column(name = "champion_wins", nullable = false)
    val championWins: Int
) 