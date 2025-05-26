package com.f1champions.config

import com.f1champions.service.F1DataService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

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
