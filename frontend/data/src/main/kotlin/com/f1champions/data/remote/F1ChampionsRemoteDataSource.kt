package com.f1champions.data.remote

import com.f1champions.data.api.F1ChampionsApi
import com.f1champions.data.api.dto.ApiErrorDto
import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.data.api.dto.SeasonSummaryApiDto
import com.f1champions.data.remote.exception.*
import com.squareup.moshi.Moshi
import okhttp3.Request
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
        throw mapToApiException(e, "GET", "/seasons")
    } catch (e: SocketTimeoutException) {
        throw ConnectionTimeoutException(
            message = "Connection timed out while fetching seasons",
            cause = e,
            requestUrl = "/seasons",
            requestMethod = "GET"
        )
    } catch (e: UnknownHostException) {
        throw NoInternetException(
            message = "No internet connection while fetching seasons",
            cause = e,
            requestUrl = "/seasons",
            requestMethod = "GET"
        )
    } catch (e: IOException) {
        throw GeneralNetworkException(
            message = "Failed to connect to the server while fetching seasons",
            cause = e,
            requestUrl = "/seasons",
            requestMethod = "GET"
        )
    } catch (e: Exception) {
        throw ParseException(
            message = "Failed to parse seasons response",
            cause = e,
            requestUrl = "/seasons",
            requestMethod = "GET"
        )
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
        val path = "/seasons/$year/races"
        when (e.code()) {
            400 -> throw InvalidSeasonException(
                message = "Invalid season year: $year",
                requestUrl = path,
                requestMethod = "GET",
                responseHeaders = e.response()?.headers()?.toMultimap(),
                responseBody = e.response()?.errorBody()?.string()
            )

            404 -> throw SeasonNotFoundException(
                message = "Season not found: $year",
                requestUrl = path,
                requestMethod = "GET",
                responseHeaders = e.response()?.headers()?.toMultimap(),
                responseBody = e.response()?.errorBody()?.string()
            )

            else -> throw mapToApiException(e, "GET", path)
        }
    } catch (e: SocketTimeoutException) {
        throw ConnectionTimeoutException(
            message = "Connection timed out while fetching races for season $year",
            cause = e,
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
    } catch (e: UnknownHostException) {
        throw NoInternetException(
            message = "No internet connection while fetching races for season $year",
            cause = e,
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
    } catch (e: IOException) {
        throw GeneralNetworkException(
            message = "Failed to connect to the server while fetching races for season $year",
            cause = e,
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
    } catch (e: Exception) {
        throw ParseException(
            message = "Failed to parse races response for season $year",
            cause = e,
            requestUrl = "/seasons/$year/races",
            requestMethod = "GET"
        )
    }

    private fun mapToApiException(
        exception: HttpException,
        method: String,
        path: String
    ): ApiException {
        val response = exception.response()
        val errorBody = response?.errorBody()?.string()
        val headers = response?.headers()?.toMultimap()

        return try {
            val apiError = errorBody?.let {
                moshi.adapter(ApiErrorDto::class.java).fromJson(it)
            }
            when (exception.code()) {
                400 -> BadRequestException(
                    message = apiError?.message ?: "Invalid request",
                    requestUrl = path,
                    requestMethod = method,
                    responseHeaders = headers,
                    responseBody = errorBody
                )

                401 -> UnauthorizedException(
                    message = apiError?.message ?: "Unauthorized",
                    requestUrl = path,
                    requestMethod = method,
                    responseHeaders = headers,
                    responseBody = errorBody
                )

                403 -> ForbiddenException(
                    message = apiError?.message ?: "Access forbidden",
                    requestUrl = path,
                    requestMethod = method,
                    responseHeaders = headers,
                    responseBody = errorBody
                )

                404 -> NotFoundException(
                    message = apiError?.message ?: "Resource not found",
                    requestUrl = path,
                    requestMethod = method,
                    responseHeaders = headers,
                    responseBody = errorBody
                )

                429 -> RateLimitException(
                    message = apiError?.message ?: "Too many requests",
                    requestUrl = path,
                    requestMethod = method,
                    responseHeaders = headers,
                    responseBody = errorBody
                )

                500 -> ServerException(
                    message = apiError?.message ?: "Internal server error",
                    requestUrl = path,
                    requestMethod = method,
                    responseHeaders = headers,
                    responseBody = errorBody
                )

                else -> UnknownApiException(
                    statusCode = exception.code(),
                    message = apiError?.message ?: "Unknown error occurred",
                    requestUrl = path,
                    requestMethod = method,
                    responseHeaders = headers,
                    responseBody = errorBody
                )
            }
        } catch (e: Exception) {
            UnknownApiException(
                statusCode = exception.code(),
                message = "Failed to parse error response",
                cause = e,
                requestUrl = path,
                requestMethod = method,
                responseHeaders = headers,
                responseBody = errorBody
            )
        }
    }
} 