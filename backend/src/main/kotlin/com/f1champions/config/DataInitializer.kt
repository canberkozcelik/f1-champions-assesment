package com.f1champions.config

import com.f1champions.exception.ErgastApiException
import com.f1champions.service.F1DataService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val f1DataService: F1DataService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun initialize() {
        runBlocking {
            try {
                val success = f1DataService.ensureSeasonsDataPopulated()
                if (success) {
                    logger.info("Successfully populated all season data")
                } else {
                    logger.warn(
                        "Some season data could not be populated due to rate limiting or other issues. " +
                            "The application will continue to run, and data will be fetched on-demand."
                    )
                }
            } catch (e: ErgastApiException) {
                logger.error("Error during initial data population: ${e.message}")
                logger.warn("The application will continue to run, and data will be fetched on-demand.")
            } catch (e: Exception) {
                logger.error("Unexpected error during initial data population: ${e.message}", e)
                logger.warn("The application will continue to run, and data will be fetched on-demand.")
            }
        }
    }
}
