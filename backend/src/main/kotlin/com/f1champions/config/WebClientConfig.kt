package com.f1champions.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun ergastWebClient(@Value("\${ergast.api.base-url}") baseUrl: String): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}
