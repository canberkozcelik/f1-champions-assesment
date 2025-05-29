package com.f1champions.data.di

import com.f1champions.data.api.F1ChampionsApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides network-related dependencies.
 * Includes Retrofit, OkHttp, and Moshi configurations.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a configured OkHttpClient instance with logging interceptor.
     * Logging level is set based on the environment:
     * - Development: BODY level for detailed logging
     * - Other environments: BASIC level for minimal logging
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(networkConfig: NetworkConfig): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (networkConfig.environment == "Development") {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides a configured Moshi instance with Kotlin support and custom adapters.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(LocalDate::class.java, object : com.squareup.moshi.JsonAdapter<LocalDate>() {
                private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

                override fun fromJson(reader: com.squareup.moshi.JsonReader): LocalDate? {
                    val dateStr = reader.nextString()
                    return if (dateStr != null) LocalDate.parse(dateStr, formatter) else null
                }

                override fun toJson(writer: com.squareup.moshi.JsonWriter, value: LocalDate?) {
                    writer.value(value?.format(formatter))
                }
            })
            .build()
    }

    /**
     * Provides a configured Retrofit instance with Moshi converter and OkHttp client.
     * Uses the baseUrl from NetworkConfig which is environment-specific.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi, networkConfig: NetworkConfig): Retrofit {
        return Retrofit.Builder()
            .baseUrl(networkConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Provides the F1ChampionsApi service interface implementation.
     */
    @Provides
    @Singleton
    fun provideF1ChampionsApi(retrofit: Retrofit): F1ChampionsApi {
        return retrofit.create(F1ChampionsApi::class.java)
    }
} 