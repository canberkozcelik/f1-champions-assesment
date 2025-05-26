package com.f1champions.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("F1 Champions API")
                    .description(
                        """
                        API for retrieving Formula 1 World Champions and race results.
                        
                        ## Features
                        - Get F1 World Champions from 2005 to present
                        - Get race results for each season
                        - Historical data from Ergast API
                        
                        ## Data Sources
                        This API uses the Ergast API (http://ergast.com/mrd/) as its data source.
                        All F1 data is provided through their public API.
                        
                        ## Rate Limiting
                        Please be mindful of the Ergast API rate limits:
                        - 4 calls per second
                        - 200 calls per hour
                        """.trimIndent()
                    )
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("F1 Champions Team")
                            .email("your.email@example.com")
                            .url("https://github.com/yourusername/f1-champions-assesment")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server")
                )
            )
            .tags(
                listOf(
                    Tag()
                        .name("Champions")
                        .description("Operations about F1 World Champions"),
                    Tag()
                        .name("Races")
                        .description("Operations about F1 race results"),
                    Tag()
                        .name("Health")
                        .description("Health check endpoints")
                )
            )
    }
}
