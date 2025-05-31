package com.f1champions.util

import com.f1champions.exception.ErgastApiException
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

object RetryUtil {
    private val logger = LoggerFactory.getLogger(RetryUtil::class.java)

    suspend fun <T> retryWithBackoff(
        times: Int = 3,
        initialDelay: Long = 1000, // 1 second
        maxDelay: Long = 10000, // 10 seconds
        factor: Double = 2.0,
        operationName: String = "operation",
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        var lastException: Exception? = null

        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                val isRateLimitError = when (e) {
                    is IllegalStateException -> e.message?.contains("Rate limit exceeded", ignoreCase = true) == true
                    is RequestNotPermitted -> true
                    else -> false
                }

                if (!isRateLimitError) {
                    logger.error("Unexpected error during $operationName: ${e.message}")
                    throw ErgastApiException("Unexpected error during $operationName", e)
                }

                if (attempt == times - 1) {
                    logger.error("Failed to execute $operationName after $times attempts. Last error: ${e.message}")
                    throw ErgastApiException("Rate limit exceeded while executing $operationName", e)
                }

                logger.warn(
                    "Attempt ${attempt + 1} of $times failed for $operationName: ${e.message}. " +
                        "Retrying in ${currentDelay}ms..."
                )
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }

        // This should never be reached due to the throw in the last attempt
        throw ErgastApiException("Failed to execute $operationName", lastException)
    }
}
