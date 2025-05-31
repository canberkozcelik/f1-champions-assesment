package com.f1champions.data.di

import com.f1champions.data.repository.F1RepositoryImpl
import com.f1champions.domain.repository.F1Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Provides the implementation of [F1Repository].
     */
    @Binds
    @Singleton
    abstract fun bindF1Repository(
        repositoryImpl: F1RepositoryImpl
    ): F1Repository
} 