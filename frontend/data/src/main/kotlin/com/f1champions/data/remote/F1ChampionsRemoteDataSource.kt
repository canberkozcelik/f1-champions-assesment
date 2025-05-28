package com.f1champions.data.remote

import com.f1champions.data.api.F1ChampionsApi
import com.f1champions.data.api.dto.ApiErrorDto
import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.data.api.dto.SeasonSummaryApiDto
import com.f1champions.data.remote.exception.*
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for F1 Champions API.
 * Handles network calls and error handling for the API endpoints.
 */
@Singleton
class F1ChampionsRemoteDataSource @Inject constructor(
    private val api: F1ChampionsApi,
    private val moshi: Moshi
) {
    /**
     * Fetches the list of F1 World Champions.
     *
     * @return List of [SeasonSummaryApiDto] containing champion information
     * @throws NetworkException if there's a network error
     * @throws ApiException if the API returns an error response
     */
    suspend fun getSeasons(): List<SeasonSummaryApiDto> = try {
        api.getSeasons()
    } catch (e: HttpException) {
        throw mapToApiException(e)
    } catch (e: IOException) {
        throw NetworkException("Failed to connect to the server", e)
    }

    /**
     * Fetches the race results for a specific season.
     *
     * @param year The season year to fetch races for
     * @return List of [RaceDetailApiDto] containing race information
     * @throws NetworkException if there's a network error
     * @throws ApiException if the API returns an error response
     * @throws InvalidSeasonException if the season year is invalid
     */
    suspend fun getRacesForSeason(year: Int): List<RaceDetailApiDto> = try {
        api.getRacesForSeason(year)
    } catch (e: HttpException) {
        when (e.code()) {
            400 -> throw InvalidSeasonException("Invalid season year: $year")
            404 -> throw SeasonNotFoundException("Season not found: $year")
            else -> throw mapToApiException(e)
        }
    } catch (e: IOException) {
        throw NetworkException("Failed to connect to the server", e)
    }

    private fun mapToApiException(exception: HttpException): ApiException {
        val errorBody = exception.response()?.errorBody()?.string()
        return try {
            val apiError = errorBody?.let { 
                moshi.adapter(ApiErrorDto::class.java).fromJson(it)
            }
            when (exception.code()) {
                400 -> BadRequestException(apiError?.message ?: "Invalid request")
                401 -> UnauthorizedException(apiError?.message ?: "Unauthorized")
                403 -> ForbiddenException(apiError?.message ?: "Access forbidden")
                404 -> NotFoundException(apiError?.message ?: "Resource not found")
                429 -> RateLimitException(apiError?.message ?: "Too many requests")
                500 -> ServerException(apiError?.message ?: "Internal server error")
                else -> UnknownApiException(
                    statusCode = exception.code(),
                    message = apiError?.message ?: "Unknown error occurred"
                )
            }
        } catch (e: Exception) {
            UnknownApiException(
                statusCode = exception.code(),
                message = "Failed to parse error response"
            )
        }
    }
} 