package com.f1champions.domain.usecase

import com.f1champions.domain.model.SeasonChampionInfo
import com.f1champions.domain.repository.F1Repository
import javax.inject.Inject

/**
 * Use case for retrieving a list of Formula 1 World Champions.
 * This use case encapsulates the business logic for fetching season champions.
 */
class GetSeasonChampionsUseCase @Inject constructor(
    private val repository: F1Repository
) : NoParamsUseCase<List<SeasonChampionInfo>> {

    /**
     * Executes the use case to fetch all Formula 1 World Champions.
     *
     * @return A list of [SeasonChampionInfo] containing champion information for each season
     * @throws com.f1champions.domain.exception.F1NetworkException if there's a network error
     * @throws com.f1champions.domain.exception.F1UnexpectedException if there's an unexpected error
     */
    override suspend fun invoke(): List<SeasonChampionInfo> {
        return repository.getSeasonChampions()
    }
} 