package com.f1champions.domain.usecase

import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.domain.repository.F1Repository
import javax.inject.Inject

/**
 * Parameters for the [GetRaceWinnersForSeasonUseCase].
 *
 * @property year The championship year to fetch race winners for
 */
data class GetRaceWinnersParams(
    val year: Int
)

/**
 * Use case for retrieving race winners for a specific Formula 1 season.
 * This use case encapsulates the business logic for fetching race winners.
 */
class GetRaceWinnersForSeasonUseCase @Inject constructor(
    private val repository: F1Repository
) : UseCase<List<RaceWinnerInfo>, GetRaceWinnersParams> {

    /**
     * Executes the use case to fetch race winners for a specific season.
     *
     * @param params The parameters containing the season year
     * @return A list of [RaceWinnerInfo] containing race winner information for the season
     * @throws com.f1champions.domain.exception.F1NetworkException if there's a network error
     * @throws com.f1champions.domain.exception.F1InvalidSeasonException if the season year is invalid
     * @throws com.f1champions.domain.exception.F1SeasonNotFoundException if the season is not found
     * @throws com.f1champions.domain.exception.F1UnexpectedException if there's an unexpected error
     */
    override suspend fun invoke(params: GetRaceWinnersParams): List<RaceWinnerInfo> {
        return repository.getRaceWinners(params.year)
    }
} 