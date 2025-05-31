package com.f1champions.data.repository

import com.f1champions.data.mapper.RaceMapper
import com.f1champions.data.mapper.SeasonMapper
import com.f1champions.data.remote.F1ChampionsRemoteDataSource
import com.f1champions.data.remote.exception.*
import com.f1champions.domain.exception.*
import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.domain.model.SeasonChampionInfo
import com.f1champions.domain.repository.F1Repository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [F1Repository] that uses [F1ChampionsRemoteDataSource] to fetch data
 * and maps it to domain models using the appropriate mappers.
 *
 * This implementation handles mapping between data layer exceptions and domain exceptions,
 * providing appropriate context and error messages for each error case.
 */
@Singleton
class F1RepositoryImpl @Inject constructor(
    private val remoteDataSource: F1ChampionsRemoteDataSource
) : F1Repository {

    /**
     * Fetches the list of F1 World Champions.
     *
     * @return List of [SeasonChampionInfo] containing champion information
     * @throws F1NetworkException if there's a network error (offline, timeout, or general)
     * @throws F1RateLimitException if the API rate limit is exceeded
     * @throws F1ServerException if there's a server error
     * @throws F1UnexpectedException for any other unexpected errors
     */
    override suspend fun getSeasonChampions(): List<SeasonChampionInfo> = try {
        SeasonMapper.toSeasonChampionInfoList(remoteDataSource.getSeasons())
    } catch (e: NetworkException) {
        when {
            e.isOffline -> throw F1NetworkException(
                errorCode = "NETWORK_OFFLINE",
                message = "No internet connection available",
                isOffline = true,
                cause = e
            )

            e.isTimeout -> throw F1NetworkException(
                errorCode = "NETWORK_TIMEOUT",
                message = "Connection timed out while fetching season champions",
                isOffline = false,
                cause = e
            )

            else -> throw F1NetworkException(
                errorCode = "NETWORK_ERROR",
                message = "Network error while fetching season champions: ${e.message}",
                isOffline = false,
                cause = e
            )
        }
    } catch (e: RateLimitException) {
        throw F1RateLimitException(
            errorCode = "RATE_LIMIT_EXCEEDED",
            message = "Too many requests. Please try again later.",
        )
    } catch (e: ServerException) {
        throw F1ServerException(
            errorCode = "SERVER_ERROR",
            statusCode = e.statusCode,
            message = "Server error while fetching season champions: ${e.message}",
            cause = e
        )
    } catch (e: ApiException) {
        throw F1UnexpectedException(
            errorCode = "API_ERROR",
            message = "API error while fetching season champions: ${e.message}",
            cause = e
        )
    } catch (e: ParseException) {
        throw F1UnexpectedException(
            errorCode = "PARSE_ERROR",
            message = "Failed to parse season champions data",
            cause = e
        )
    } catch (e: Exception) {
        throw F1UnexpectedException(
            errorCode = "UNEXPECTED_ERROR",
            message = "Unexpected error while fetching season champions",
            cause = e
        )
    }

    /**
     * Fetches the race winners for a specific season.
     *
     * @param year The season year to fetch races for
     * @return List of [RaceWinnerInfo] containing race information
     * @throws F1NetworkException if there's a network error (offline, timeout, or general)
     * @throws F1InvalidSeasonException if the season year is invalid
     * @throws F1SeasonNotFoundException if the season is not found
     * @throws F1RateLimitException if the API rate limit is exceeded
     * @throws F1ServerException if there's a server error
     * @throws F1UnexpectedException for any other unexpected errors
     */
    override suspend fun getRaceWinners(year: Int): List<RaceWinnerInfo> = try {
        RaceMapper.toRaceWinnerInfoList(remoteDataSource.getRacesForSeason(year))
    } catch (e: NetworkException) {
        when {
            e.isOffline -> throw F1NetworkException(
                errorCode = "NETWORK_OFFLINE",
                message = "No internet connection available",
                isOffline = true,
                cause = e
            )

            e.isTimeout -> throw F1NetworkException(
                errorCode = "NETWORK_TIMEOUT",
                message = "Connection timed out while fetching race winners for season $year",
                isOffline = false,
                cause = e
            )

            else -> throw F1NetworkException(
                errorCode = "NETWORK_ERROR",
                message = "Network error while fetching race winners for season $year: ${e.message}",
                isOffline = false,
                cause = e
            )
        }
    } catch (e: InvalidSeasonException) {
        throw F1InvalidSeasonException(
            errorCode = "INVALID_SEASON",
            year = year,
            message = "Invalid season year: $year",
        )
    } catch (e: SeasonNotFoundException) {
        throw F1SeasonNotFoundException(
            errorCode = "SEASON_NOT_FOUND",
            year = year,
            message = "Season not found: $year",
        )
    } catch (e: RateLimitException) {
        throw F1RateLimitException(
            errorCode = "RATE_LIMIT_EXCEEDED",
            message = "Too many requests. Please try again later.",
        )
    } catch (e: ServerException) {
        throw F1ServerException(
            errorCode = "SERVER_ERROR",
            statusCode = e.statusCode,
            message = "Server error while fetching race winners for season $year: ${e.message}",
            cause = e
        )
    } catch (e: ApiException) {
        throw F1UnexpectedException(
            errorCode = "API_ERROR",
            message = "API error while fetching race winners for season $year: ${e.message}",
            cause = e
        )
    } catch (e: ParseException) {
        throw F1UnexpectedException(
            errorCode = "PARSE_ERROR",
            message = "Failed to parse race winners data for season $year",
            cause = e
        )
    } catch (e: Exception) {
        throw F1UnexpectedException(
            errorCode = "UNEXPECTED_ERROR",
            message = "Unexpected error while fetching race winners for season $year",
            cause = e
        )
    }
} 