package com.f1champions.app.di

import com.f1champions.app.BuildConfig
import com.f1champions.data.di.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides app-specific dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the NetworkConfig implementation using BuildConfig values.
     * This bridges the app module's build configuration with the data module's network setup.
     */
    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = object : NetworkConfig {
        override val baseUrl: String = BuildConfig.BASE_URL
        override val environment: String = BuildConfig.ENVIRONMENT
    }
} 