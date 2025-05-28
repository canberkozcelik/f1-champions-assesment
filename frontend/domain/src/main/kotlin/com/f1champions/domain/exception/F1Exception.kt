package com.f1champions.domain.exception

/**
 * Base sealed class for all F1-related exceptions in the domain layer.
 */
sealed class F1Exception(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when there's a network-related error.
 */
class F1NetworkException(message: String, cause: Throwable? = null) : F1Exception(message, cause)

/**
 * Exception thrown when a requested season is not found.
 */
class F1SeasonNotFoundException(message: String) : F1Exception(message)

/**
 * Exception thrown when a season year is invalid.
 */
class F1InvalidSeasonException(message: String) : F1Exception(message)

/**
 * Exception thrown when there's an unexpected error.
 */
class F1UnexpectedException(message: String, cause: Throwable? = null) : F1Exception(message, cause) 