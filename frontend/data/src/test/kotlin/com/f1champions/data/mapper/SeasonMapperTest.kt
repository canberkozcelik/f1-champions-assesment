package com.f1champions.data.mapper

import com.f1champions.data.api.dto.SeasonSummaryApiDto
import com.f1champions.domain.model.SeasonChampionInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonMapperTest {

    @Test
    fun `toSeasonChampionInfoList maps single season champion correctly`() {
        // Given
        val dto = SeasonSummaryApiDto(
            year = 2023,
            championName = "Max Verstappen",
            championPoints = 575,
            championWins = 19
        )

        // When
        val result = SeasonMapper.toSeasonChampionInfoList(listOf(dto))

        // Then
        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals(2023, year)
            assertEquals("Max Verstappen", championName)
            assertEquals(575, points)
            assertEquals(19, wins)
        }
    }

    @Test
    fun `toSeasonChampionInfoList maps multiple season champions correctly`() {
        // Given
        val dtos = listOf(
            SeasonSummaryApiDto(
                year = 2023,
                championName = "Max Verstappen",
                championPoints = 575,
                championWins = 19
            ),
            SeasonSummaryApiDto(
                year = 2022,
                championName = "Max Verstappen",
                championPoints = 454,
                championWins = 15
            ),
            SeasonSummaryApiDto(
                year = 2021,
                championName = "Max Verstappen",
                championPoints = 395,
                championWins = 10
            )
        )

        // When
        val result = SeasonMapper.toSeasonChampionInfoList(dtos)

        // Then
        assertEquals(3, result.size)
        
        // Verify first champion
        with(result[0]) {
            assertEquals(2023, year)
            assertEquals("Max Verstappen", championName)
            assertEquals(575, points)
            assertEquals(19, wins)
        }

        // Verify second champion
        with(result[1]) {
            assertEquals(2022, year)
            assertEquals("Max Verstappen", championName)
            assertEquals(454, points)
            assertEquals(15, wins)
        }

        // Verify third champion
        with(result[2]) {
            assertEquals(2021, year)
            assertEquals("Max Verstappen", championName)
            assertEquals(395, points)
            assertEquals(10, wins)
        }
    }

    @Test
    fun `toSeasonChampionInfoList returns empty list when input is empty`() {
        // Given
        val emptyList = emptyList<SeasonSummaryApiDto>()

        // When
        val result = SeasonMapper.toSeasonChampionInfoList(emptyList)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toSeasonChampionInfoList handles zero values correctly`() {
        // Given
        val dto = SeasonSummaryApiDto(
            year = 1950,
            championName = "Giuseppe Farina",
            championPoints = 0,
            championWins = 0
        )

        // When
        val result = SeasonMapper.toSeasonChampionInfoList(listOf(dto))

        // Then
        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals(1950, year)
            assertEquals("Giuseppe Farina", championName)
            assertEquals(0, points)
            assertEquals(0, wins)
        }
    }
} 