package com.f1champions.entity

import jakarta.persistence.*

@Entity
@Table(name = "seasons")
data class SeasonEntity(
    @Id
    @Column(name = "year", nullable = false)
    val year: Int,

    @Column(name = "world_champion_driver_id", nullable = false)
    val worldChampionDriverId: String,

    @Column(name = "world_champion_name", nullable = false)
    val worldChampionName: String,

    @Column(name = "world_champion_nationality", nullable = false)
    val worldChampionNationality: String,

    @Column(name = "world_champion_constructor_id", nullable = false)
    val worldChampionConstructorId: String,

    @Column(name = "world_champion_constructor_name", nullable = false)
    val worldChampionConstructorName: String
) 