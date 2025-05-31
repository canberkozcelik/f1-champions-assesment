package com.f1champions.data.di

/**
 * Configuration interface for network-related settings.
 * This allows the app module to provide environment-specific configuration
 * while keeping the data module environment-agnostic.
 */
interface NetworkConfig {
    val baseUrl: String
    val environment: String
} 