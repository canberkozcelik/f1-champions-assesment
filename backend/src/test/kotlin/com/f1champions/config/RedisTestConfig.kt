package com.f1champions.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.time.Duration

@TestConfiguration
@EnableCaching
@Profile("test")
@Testcontainers
class RedisTestConfig {
    companion object {
        private const val REDIS_PASSWORD_FILE = "/run/secrets/redis_password"
        private const val DEFAULT_REDIS_PORT = 6379

        // Detect if we're running in Docker Compose
        private val isRunningInDocker = System.getenv("SPRING_REDIS_HOST") != null

        // Only create container when running locally
        @Container
        private val redisContainer = if (!isRunningInDocker) {
            GenericContainer(DockerImageName.parse("redis:7.2-alpine"))
                .withExposedPorts(DEFAULT_REDIS_PORT)
                .withReuse(true)
        } else {
            null
        }

        @JvmStatic
        @DynamicPropertySource
        fun redisProperties(registry: DynamicPropertyRegistry) {
            if (!isRunningInDocker) {
                // Start container if not already running
                if (redisContainer != null && !redisContainer.isRunning) {
                    redisContainer.start()
                }
                // Configure Redis properties for local testing
                registry.add("spring.redis.host") { "localhost" }
                registry.add("spring.redis.port") { redisContainer?.getMappedPort(DEFAULT_REDIS_PORT) ?: DEFAULT_REDIS_PORT }
            }
        }

        private fun readRedisPassword(): String? {
            return if (isRunningInDocker) {
                try {
                    File(REDIS_PASSWORD_FILE).readText().trim()
                } catch (e: Exception) {
                    null
                }
            } else {
                null // No password for local Redis container
            }
        }
    }

    @Bean
    @Primary
    fun testRedisConnectionFactory(): RedisConnectionFactory {
        // Start container if running locally and not already started
        if (!isRunningInDocker && redisContainer != null && !redisContainer.isRunning) {
            redisContainer.start()
        }

        val config = RedisStandaloneConfiguration().apply {
            if (isRunningInDocker) {
                // Use Docker Compose Redis configuration
                hostName = System.getenv("SPRING_REDIS_HOST") ?: "redis"
                port = System.getenv("SPRING_REDIS_PORT")?.toIntOrNull() ?: DEFAULT_REDIS_PORT
                password = RedisPassword.of(readRedisPassword())
            } else {
                // Use local TestContainers Redis configuration
                hostName = "localhost"
                port = redisContainer?.getMappedPort(DEFAULT_REDIS_PORT) ?: DEFAULT_REDIS_PORT
            }
        }
        return LettuceConnectionFactory(config)
    }

    @Bean
    @Primary
    fun testCacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues()
            .entryTtl(Duration.ofMinutes(1))

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration(
                "seasons",
                config.entryTtl(Duration.ofMinutes(1))
            )
            .withCacheConfiguration(
                "races",
                config.entryTtl(Duration.ofMinutes(1))
            )
            .build()
    }
}
