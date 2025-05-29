package com.f1champions.data.mapper

import com.f1champions.data.api.dto.RaceDetailApiDto
import com.f1champions.domain.model.RaceWinnerInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RaceMapperTest {

    @Test
    fun `toRaceWinnerInfoList maps single race winner correctly`() {
        // Given
        val dto = RaceDetailApiDto(
            round = 1,
            raceName = "Bahrain Grand Prix",
            date = LocalDate.of(2024, 3, 2),
            circuitName = "Bahrain International Circuit",
            winningDriverName = "Max Verstappen",
            winningDriverNationality = "Dutch",
            winningConstructorName = "Red Bull Racing",
            isSeasonChampionWinner = true
        )

        // When
        val result = RaceMapper.toRaceWinnerInfoList(listOf(dto))

        // Then
        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals("Bahrain Grand Prix", raceName)
            assertEquals("Bahrain International Circuit", circuitName)
            assertEquals("March 2, 2024", date)
            assertEquals("Max Verstappen", winnerName)
            assertEquals("Red Bull Racing", constructorName)
            assertTrue(isSeasonChampionWinner)
        }
    }

    @Test
    fun `toRaceWinnerInfoList maps multiple race winners correctly`() {
        // Given
        val dtos = listOf(
            RaceDetailApiDto(
                round = 1,
                raceName = "Bahrain Grand Prix",
                date = LocalDate.of(2024, 3, 2),
                circuitName = "Bahrain International Circuit",
                winningDriverName = "Max Verstappen",
                winningDriverNationality = "Dutch",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = true
            ),
            RaceDetailApiDto(
                round = 2,
                raceName = "Saudi Arabian Grand Prix",
                date = LocalDate.of(2024, 3, 9),
                circuitName = "Jeddah Corniche Circuit",
                winningDriverName = "Sergio Perez",
                winningDriverNationality = "Mexican",
                winningConstructorName = "Red Bull Racing",
                isSeasonChampionWinner = false
            )
        )

        // When
        val result = RaceMapper.toRaceWinnerInfoList(dtos)

        // Then
        assertEquals(2, result.size)
        
        // Verify first race
        with(result[0]) {
            assertEquals("Bahrain Grand Prix", raceName)
            assertEquals("Bahrain International Circuit", circuitName)
            assertEquals("March 2, 2024", date)
            assertEquals("Max Verstappen", winnerName)
            assertEquals("Red Bull Racing", constructorName)
            assertTrue(isSeasonChampionWinner)
        }

        // Verify second race
        with(result[1]) {
            assertEquals("Saudi Arabian Grand Prix", raceName)
            assertEquals("Jeddah Corniche Circuit", circuitName)
            assertEquals("March 9, 2024", date)
            assertEquals("Sergio Perez", winnerName)
            assertEquals("Red Bull Racing", constructorName)
            assertTrue(!isSeasonChampionWinner)
        }
    }

    @Test
    fun `toRaceWinnerInfoList returns empty list when input is empty`() {
        // Given
        val emptyList = emptyList<RaceDetailApiDto>()

        // When
        val result = RaceMapper.toRaceWinnerInfoList(emptyList)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toRaceWinnerInfoList formats date correctly`() {
        // Given
        val dtos = listOf(
            RaceDetailApiDto(
                round = 1,
                raceName = "Test Grand Prix",
                date = LocalDate.of(2024, 1, 1), // January 1
                circuitName = "Test Circuit",
                winningDriverName = "Test Driver",
                winningDriverNationality = "Test",
                winningConstructorName = "Test Team",
                isSeasonChampionWinner = false
            ),
            RaceDetailApiDto(
                round = 2,
                raceName = "Test Grand Prix 2",
                date = LocalDate.of(2024, 12, 31), // December 31
                circuitName = "Test Circuit 2",
                winningDriverName = "Test Driver 2",
                winningDriverNationality = "Test",
                winningConstructorName = "Test Team 2",
                isSeasonChampionWinner = false
            )
        )

        // When
        val result = RaceMapper.toRaceWinnerInfoList(dtos)

        // Then
        assertEquals(2, result.size)
        assertEquals("January 1, 2024", result[0].date)
        assertEquals("December 31, 2024", result[1].date)
    }
} 