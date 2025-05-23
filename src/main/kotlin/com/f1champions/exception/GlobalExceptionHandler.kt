package com.f1champions.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid argument: ${ex.message}")
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ex.message ?: "Invalid argument provided",
            request
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}")
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.message ?: "Requested resource not found",
            request
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Internal server error: ${ex.message}", ex)
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred while processing your request",
            request
        )
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handleWebClientResponseException(
        ex: WebClientResponseException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("External API error: ${ex.message}", ex)
        return createErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            "Unable to fetch data from external service",
            request
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception: ${ex.message}", ex)
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred",
            request
        )
    }

    @ExceptionHandler(ErgastApiServiceUnavailableException::class)
    fun handleErgastApiServiceUnavailable(
        ex: ErgastApiServiceUnavailableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Ergast API service unavailable: ${ex.message}", ex)
        return createErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            "The Formula 1 data service is currently unavailable. Please try again later.",
            request
        )
    }

    @ExceptionHandler(ErgastApiDataNotFoundException::class)
    fun handleErgastApiDataNotFound(
        ex: ErgastApiDataNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Ergast API data not found: ${ex.message}")
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.message ?: "The requested Formula 1 data could not be found",
            request
        )
    }

    @ExceptionHandler(ErgastApiRateLimitException::class)
    fun handleErgastApiRateLimit(
        ex: ErgastApiRateLimitException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Ergast API rate limit exceeded: ${ex.message}")
        val headers = HttpHeaders()
        ex.retryAfterSeconds?.let { seconds ->
            headers.set("Retry-After", seconds.toString())
        }
        return createErrorResponse(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too Many Requests",
            "Rate limit exceeded. Please try again later.",
            request,
            headers
        )
    }

    @ExceptionHandler(ErgastApiInvalidResponseException::class)
    fun handleErgastApiInvalidResponse(
        ex: ErgastApiInvalidResponseException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Ergast API invalid response: ${ex.message}", ex)
        return createErrorResponse(
            HttpStatus.BAD_GATEWAY,
            "Bad Gateway",
            "Received invalid data from Formula 1 data service",
            request
        )
    }

    @ExceptionHandler(ErgastApiException::class)
    fun handleErgastApiException(
        ex: ErgastApiException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Ergast API error: ${ex.message}", ex)
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An error occurred while fetching Formula 1 data",
            request
        )
    }

    private fun createErrorResponse(
        status: HttpStatus,
        error: String,
        message: String,
        request: WebRequest,
        headers: HttpHeaders = HttpHeaders()
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = error,
            message = message,
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(status)
            .headers(headers)
            .body(errorResponse)
    }
}
