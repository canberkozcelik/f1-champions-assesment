package com.f1champions.feature.racewinners.mapper

import com.f1champions.domain.model.RaceWinnerInfo
import com.f1champions.feature.racewinners.model.RaceWinner
import com.f1champions.feature.racewinners.model.Winner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RaceWinnerMapperTest {

    private lateinit var mapper: RaceWinnerMapper

    @Before
    fun setup() {
        mapper = RaceWinnerMapper()
    }

    @Test
    fun `toUiModel maps single race winner correctly`() {
        // Given
        val domainModel = RaceWinnerInfo(
            raceName = "Monaco Grand Prix",
            circuitName = "Circuit de Monaco",
            date = "May 28, 2023",
            winnerName = "Max Verstappen",
            constructorName = "Red Bull Racing",
            isSeasonChampionWinner = true
        )

        // When
        val result = mapper.toUiModel(domainModel)

        // Then
        assertEquals("Monaco Grand Prix", result.raceName)
        assertEquals("May 28, 2023", result.date)
        assertEquals("Max Verstappen", result.winner.fullName)
        assertEquals("Red Bull Racing", result.winner.constructor)
    }

    @Test
    fun `toUiModels maps list of race winners correctly`() {
        // Given
        val domainModels = listOf(
            RaceWinnerInfo(
                raceName = "Monaco Grand Prix",
                circuitName = "Circuit de Monaco",
                date = "May 28, 2023",
                winnerName = "Max Verstappen",
                constructorName = "Red Bull Racing",
                isSeasonChampionWinner = true
            ),
            RaceWinnerInfo(
                raceName = "Spanish Grand Prix",
                circuitName = "Circuit de Barcelona-Catalunya",
                date = "June 4, 2023",
                winnerName = "Sergio Perez",
                constructorName = "Red Bull Racing",
                isSeasonChampionWinner = false
            )
        )

        // When
        val result = mapper.toUiModels(domainModels)

        // Then
        assertEquals(2, result.size)
        
        // Verify first race
        with(result[0]) {
            assertEquals("Monaco Grand Prix", raceName)
            assertEquals("May 28, 2023", date)
            assertEquals("Max Verstappen", winner.fullName)
            assertEquals("Red Bull Racing", winner.constructor)
        }

        // Verify second race
        with(result[1]) {
            assertEquals("Spanish Grand Prix", raceName)
            assertEquals("June 4, 2023", date)
            assertEquals("Sergio Perez", winner.fullName)
            assertEquals("Red Bull Racing", winner.constructor)
        }
    }

    @Test
    fun `toUiModels returns empty list when input is empty`() {
        // Given
        val emptyList = emptyList<RaceWinnerInfo>()

        // When
        val result = mapper.toUiModels(emptyList)

        // Then
        assertTrue(result.isEmpty())
    }
} 