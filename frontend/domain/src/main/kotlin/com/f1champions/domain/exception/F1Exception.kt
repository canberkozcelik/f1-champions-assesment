package com.f1champions.domain.exception

/**
 * Base sealed class for all F1-related exceptions in the domain layer.
 * Each exception type includes an error code for consistent error handling in the UI.
 */
sealed class F1Exception(
    open val errorCode: String,
    override val message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when there's a network-related error.
 * This includes connection failures, timeouts, and other network issues.
 *
 * @property isOffline Whether the error is due to no internet connection
 * @property errorCode Unique error code for UI handling
 * @property message Human-readable error message
 * @property cause Original exception that caused this error
 */
data class F1NetworkException(
    val isOffline: Boolean = false,
    override val errorCode: String = "NETWORK_ERROR",
    override val message: String,
    override val cause: Throwable? = null
) : F1Exception(errorCode, message, cause)

/**
 * Exception thrown when a requested season is not found.
 * This occurs when trying to access data for a season that doesn't exist.
 *
 * @property year The season year that was not found
 * @property errorCode Unique error code for UI handling
 * @property message Human-readable error message
 */
data class F1SeasonNotFoundException(
    val year: Int,
    override val errorCode: String = "SEASON_NOT_FOUND",
    override val message: String = "Season not found: $year"
) : F1Exception(errorCode, message)

/**
 * Exception thrown when a season year is invalid.
 * This occurs when the provided season year is outside valid F1 seasons.
 *
 * @property year The invalid season year
 * @property errorCode Unique error code for UI handling
 * @property message Human-readable error message
 */
data class F1InvalidSeasonException(
    val year: Int,
    override val errorCode: String = "INVALID_SEASON",
    override val message: String = "Invalid season year: $year. F1 seasons range from 1950 to present."
) : F1Exception(errorCode, message)

/**
 * Exception thrown when the API rate limit is exceeded.
 * This occurs when too many requests are made in a short time period.
 * The backend handles rate limiting, so we just inform the user about the error.
 *
 * @property errorCode Unique error code for UI handling
 * @property message Human-readable error message
 */
data class F1RateLimitException(
    override val errorCode: String = "RATE_LIMIT_EXCEEDED",
    override val message: String = "Too many requests. Please try again later."
) : F1Exception(errorCode, message)

/**
 * Exception thrown when there's a server-side error.
 * This includes 5xx errors from the API.
 *
 * @property statusCode The HTTP status code from the server
 * @property errorCode Unique error code for UI handling
 * @property message Human-readable error message
 * @property cause Original exception that caused this error
 */
data class F1ServerException(
    val statusCode: Int,
    override val errorCode: String = "SERVER_ERROR",
    override val message: String,
    override val cause: Throwable? = null
) : F1Exception(errorCode, message, cause)

/**
 * Exception thrown when there's an unexpected error.
 * This is a fallback for unhandled error cases.
 *
 * @property errorCode Unique error code for UI handling
 * @property message Human-readable error message
 * @property cause Original exception that caused this error
 */
data class F1UnexpectedException(
    override val errorCode: String = "UNEXPECTED_ERROR",
    override val message: String,
    override val cause: Throwable? = null
) : F1Exception(errorCode, message, cause) 