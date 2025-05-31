package com.f1champions.repository

import com.f1champions.entity.RaceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RaceRepository : JpaRepository<RaceEntity, Long> {
    /**
     * Finds all races for a given season year, ordered by the round number in ascending order.
     * Spring Data JPA will automatically generate the query for this method.
     * Essential for the GET /api/seasons/{year}/races endpoint.
     *
     * @param seasonYear The year of the season.
     * @return A list of RaceEntity objects for that season, ordered by round.
     */
    fun findBySeasonYearOrderByRoundAsc(seasonYear: Int): List<RaceEntity>
}
