package com.f1champions.data.remote.exception

import java.io.IOException

/**
 * Base sealed class for all remote-related exceptions.
 * This includes network errors and API errors.
 */
sealed class RemoteException(message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * Exception thrown when there's a network-related error.
 * This includes connection failures, timeouts, etc.
 */
class NetworkException(message: String, cause: Throwable? = null) : RemoteException(message, cause)

/**
 * Base sealed class for all API-related exceptions.
 * Each HTTP status code has its own exception type.
 */
sealed class ApiException(
    val statusCode: Int,
    override val message: String,
    cause: Throwable? = null
) : RemoteException(message, cause)

// HTTP Status Code Exceptions
class BadRequestException(message: String) : ApiException(400, message)
class UnauthorizedException(message: String) : ApiException(401, message)
class ForbiddenException(message: String) : ApiException(403, message)
class NotFoundException(message: String) : ApiException(404, message)
class RateLimitException(message: String) : ApiException(429, message)
class ServerException(message: String) : ApiException(500, message)

/**
 * Exception thrown for unhandled HTTP status codes.
 * This is a fallback for status codes that don't have specific exceptions.
 */
class UnknownApiException(
    statusCode: Int,
    message: String,
    cause: Throwable? = null
) : ApiException(statusCode, message, cause)

/**
 * Business-specific exceptions.
 * These represent domain-specific error cases.
 */
class InvalidSeasonException(message: String) : ApiException(400, message)
class SeasonNotFoundException(message: String) : ApiException(404, message) 