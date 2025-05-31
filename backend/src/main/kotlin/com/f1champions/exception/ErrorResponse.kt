package com.f1champions.exception

import java.time.LocalDateTime

/**
 * Standardized error response format for the API.
 * This ensures consistent error responses across all endpoints.
 *
 * @property timestamp When the error occurred
 * @property status HTTP status code
 * @property error HTTP status message
 * @property message Detailed error message
 * @property path The request path that caused the error
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
