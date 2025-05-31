package com.f1champions.data.remote.exception

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Base sealed class for all remote-related exceptions.
 * This includes network errors and API errors.
 */
sealed class RemoteException(
    override val message: String,
    override val cause: Throwable? = null,
    val requestUrl: String? = null,
    val requestMethod: String? = null
) : IOException(message, cause)

/**
 * Exception thrown when there's a network-related error.
 * This includes connection failures, timeouts, etc.
 */
sealed class NetworkException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null
) : RemoteException(message, cause, requestUrl, requestMethod) {
    val isOffline: Boolean
        get() = cause is UnknownHostException

    val isTimeout: Boolean
        get() = cause is SocketTimeoutException
}

/**
 * Exception thrown for general network errors that don't fit other categories.
 */
class GeneralNetworkException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null
) : NetworkException(message, cause, requestUrl, requestMethod)

/**
 * Exception thrown when there's a connection timeout.
 */
class ConnectionTimeoutException(
    message: String = "Connection timed out",
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null
) : NetworkException(message, cause, requestUrl, requestMethod)

/**
 * Exception thrown when there's no internet connection.
 */
class NoInternetException(
    message: String = "No internet connection",
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null
) : NetworkException(message, cause, requestUrl, requestMethod)

/**
 * Base sealed class for all API-related exceptions.
 * Each HTTP status code has its own exception type.
 */
sealed class ApiException(
    val statusCode: Int,
    override val message: String,
    override val cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    val responseHeaders: Map<String, List<String>>? = null,
    val responseBody: String? = null
) : RemoteException(message, cause, requestUrl, requestMethod)

// HTTP Status Code Exceptions
class BadRequestException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(400, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

class UnauthorizedException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(401, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

class ForbiddenException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(403, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

class NotFoundException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(404, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

class RateLimitException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(429, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

class ServerException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(500, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

/**
 * Exception thrown for unhandled HTTP status codes.
 * This is a fallback for status codes that don't have specific exceptions.
 */
class UnknownApiException(
    statusCode: Int,
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(
    statusCode,
    message,
    cause,
    requestUrl,
    requestMethod,
    responseHeaders,
    responseBody
)

/**
 * Business-specific exceptions.
 * These represent domain-specific error cases.
 */
class InvalidSeasonException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(400, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

class SeasonNotFoundException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : ApiException(404, message, cause, requestUrl, requestMethod, responseHeaders, responseBody)

/**
 * Exception thrown when the response cannot be parsed.
 */
class ParseException(
    message: String,
    cause: Throwable? = null,
    requestUrl: String? = null,
    requestMethod: String? = null,
    responseHeaders: Map<String, List<String>>? = null,
    responseBody: String? = null
) : RemoteException(message, cause, requestUrl, requestMethod) 