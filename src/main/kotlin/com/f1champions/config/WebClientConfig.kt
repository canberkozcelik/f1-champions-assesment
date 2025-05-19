package com.f1champions.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun ergastWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://ergast.com/api/f1")
            .build()
    }
} 