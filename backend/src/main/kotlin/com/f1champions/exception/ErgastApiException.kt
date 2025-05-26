package com.f1champions.exception

/**
 * Base exception class for all Ergast API related errors.
 * This allows catching all Ergast-specific exceptions in one catch block if needed.
 */
open class ErgastApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Thrown when the Ergast API service is unavailable or experiencing issues.
 * This includes network errors, timeouts, and 5xx server errors.
 */
class ErgastApiServiceUnavailableException(
    message: String,
    cause: Throwable? = null
) : ErgastApiException(message, cause)

/**
 * Thrown when expected data is not found in the Ergast API response.
 * This includes 404 responses and cases where the response is valid but missing expected data.
 */
class ErgastApiDataNotFoundException(
    message: String,
    cause: Throwable? = null
) : ErgastApiException(message, cause)

/**
 * Thrown when the Ergast API rate limit is exceeded.
 * Includes the retry-after period if provided by the API.
 */
class ErgastApiRateLimitException(
    message: String,
    val retryAfterSeconds: Int? = null,
    cause: Throwable? = null
) : ErgastApiException(message, cause)

/**
 * Thrown when the Ergast API response is invalid or malformed.
 * This includes cases where the response is not valid JSON or missing required fields.
 */
class ErgastApiInvalidResponseException(
    message: String,
    cause: Throwable? = null
) : ErgastApiException(message, cause)
