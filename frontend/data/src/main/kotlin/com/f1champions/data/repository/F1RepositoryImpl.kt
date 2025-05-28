package com.f1champions.data.repository

import com.f1champions.data.mapper.RaceMapper
import com.f1champions.data.mapper.SeasonMapper
import com.f1champions.data.remote.F1ChampionsRemoteDataSource
import com.f1champions.data.remote.exception.ApiException
import com.f1champions.data.remote.exception.InvalidSeasonException
import com.f1champions.data.remote.exception.NetworkException
import com.f1champions.data.remote.exception.SeasonNotFoundException
import com.f1champions.domain.exception.F1InvalidSeasonException
import com.f1champions.domain.exception.F1NetworkException
import com.f1champions.domain.exception.F1SeasonNotFoundException
import com.f1champions.domain.exception.F1UnexpectedException
import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.domain.model.SeasonChampionInfo
import com.f1champions.domain.repository.F1Repository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [F1Repository] that uses [F1ChampionsRemoteDataSource] to fetch data
 * and maps it to domain models using the appropriate mappers.
 */
@Singleton
class F1RepositoryImpl @Inject constructor(
    private val remoteDataSource: F1ChampionsRemoteDataSource
) : F1Repository {

    override suspend fun getSeasonChampions(): List<SeasonChampionInfo> = try {
        SeasonMapper.toSeasonChampionInfoList(remoteDataSource.getSeasons())
    } catch (e: NetworkException) {
        throw F1NetworkException("Failed to fetch season champions", e)
    } catch (e: ApiException) {
        throw F1UnexpectedException("Failed to fetch season champions: ${e.message}", e)
    } catch (e: Exception) {
        throw F1UnexpectedException("Unexpected error while fetching season champions", e)
    }

    override suspend fun getRaceWinners(year: Int): List<RaceWinnerInfo> = try {
        RaceMapper.toRaceWinnerInfoList(remoteDataSource.getRacesForSeason(year))
    } catch (e: NetworkException) {
        throw F1NetworkException("Failed to fetch race winners for season $year", e)
    } catch (e: InvalidSeasonException) {
        throw F1InvalidSeasonException("Invalid season year: $year")
    } catch (e: SeasonNotFoundException) {
        throw F1SeasonNotFoundException("Season not found: $year")
    } catch (e: ApiException) {
        throw F1UnexpectedException("Failed to fetch race winners: ${e.message}", e)
    } catch (e: Exception) {
        throw F1UnexpectedException("Unexpected error while fetching race winners", e)
    }
} 