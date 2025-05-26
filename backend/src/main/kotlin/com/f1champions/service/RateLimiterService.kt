package com.f1champions.service

import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RateLimiterService(
    private val rateLimiterRegistry: RateLimiterRegistry
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val burstLimiter: RateLimiter = rateLimiterRegistry.rateLimiter("ergastApi")
    private val hourlyLimiter: RateLimiter = rateLimiterRegistry.rateLimiter("ergastApiHourly")

    suspend fun <T> executeWithRateLimit(operation: suspend () -> T): T {
        return try {
            // Apply both rate limiters in sequence using suspend functions
            burstLimiter.executeSuspendFunction {
                hourlyLimiter.executeSuspendFunction {
                    operation()
                }
            }
        } catch (e: Exception) {
            logger.error("Rate limit exceeded for Ergast API: ${e.message}")
            throw e
        }
    }

    fun getBurstLimiterMetrics(): RateLimiter.Metrics = burstLimiter.metrics

    fun getHourlyLimiterMetrics(): RateLimiter.Metrics = hourlyLimiter.metrics
}
