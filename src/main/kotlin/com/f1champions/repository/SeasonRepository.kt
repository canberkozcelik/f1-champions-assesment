package com.f1champions.repository

import com.f1champions.entity.SeasonEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SeasonRepository : JpaRepository<SeasonEntity, Int> {
    /**
     * Finds all seasons ordered by year.
     * @return list of seasons ordered by year
     */
    fun findAllByOrderByYearAsc(): List<SeasonEntity>
} 