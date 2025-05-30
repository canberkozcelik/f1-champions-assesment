package com.f1champions.feature.racewinners.mapper

import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.feature.racewinners.model.RaceWinner
import com.f1champions.feature.racewinners.model.Winner
import javax.inject.Inject

/**
 * Mapper class responsible for converting domain [RaceWinnerInfo] to UI [RaceWinner] model.
 * This class follows clean architecture principles by:
 * 1. Being a pure function (no side effects)
 * 2. Living in the presentation layer
 * 3. Keeping domain model clean and independent
 * 4. Handling all UI-specific transformations
 */
class RaceWinnerMapper @Inject constructor() {
    /**
     * Maps a domain [RaceWinnerInfo] to a UI [RaceWinner] model.
     *
     * @param domainModel The domain model to convert
     * @return The UI model representation
     */
    fun toUiModel(domainModel: RaceWinnerInfo): RaceWinner = RaceWinner(
        raceName = domainModel.raceName,
        date = domainModel.date,
        winner = Winner(
            fullName = domainModel.winnerName,
            constructor = domainModel.constructorName
        ),
        isSeasonChampionWinner = domainModel.isSeasonChampionWinner
    )

    /**
     * Maps a list of domain [RaceWinnerInfo] to a list of UI [RaceWinner] models.
     *
     * @param domainModels The list of domain models to convert
     * @return The list of UI model representations
     */
    fun toUiModels(domainModels: List<RaceWinnerInfo>): List<RaceWinner> =
        domainModels.map(::toUiModel)
} 