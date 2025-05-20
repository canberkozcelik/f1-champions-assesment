package com.f1champions.config

import com.f1champions.service.F1DataService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import kotlinx.coroutines.runBlocking

@Component
class DataInitializer(
    private val f1DataService: F1DataService
) {
    @PostConstruct
    fun initialize() {
        runBlocking {
            f1DataService.ensureSeasonsDataPopulated()
        }
    }
} 