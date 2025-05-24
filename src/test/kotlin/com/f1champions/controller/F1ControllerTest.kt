package com.f1champions.controller

import com.f1champions.api.dto.SeasonDto
import com.f1champions.exception.ErgastApiDataNotFoundException
import com.f1champions.exception.ErgastApiInvalidResponseException
import com.f1champions.exception.ErgastApiRateLimitException
import com.f1champions.exception.ErgastApiServiceUnavailableException
import com.f1champions.service.F1DataService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(F1Controller::class)
class F1ControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean(relaxed = true)
    private lateinit var f1DataService: F1DataService

    @Test
    fun `GET seasons returns 200 OK and list of seasons when service returns data`() = runTest {
        // Given
        val mockSeasons = listOf(
            SeasonDto(2023, "Max Verstappen", 454, 19),
            SeasonDto(2022, "Max Verstappen", 454, 15)
        )
        coEvery { f1DataService.getAllSeasons() } returns mockSeasons

        // When
        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/seasons")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk) // Check initial status
            .andExpect(request().asyncStarted()) // Verify that async processing has started
            .andDo(print()) // Print details after initial request (optional, for debugging)
            .andReturn() // Get the MvcResult to perform async dispatch

        // Then: Perform async dispatch and assert on the final response
        mockMvc.perform(asyncDispatch(mvcResult)) // Perform the async dispatch
            .andDo(print()) // Print details of the final response
            .andExpect(status().isOk) // Assert final status
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE)) // Assert content type
            .andExpect(jsonPath("$.size()").value(mockSeasons.size))
            .andExpect(jsonPath("$[0].year").value(2023))
            .andExpect(jsonPath("$[0].championName").value("Max Verstappen"))
            .andExpect(jsonPath("$[0].championPoints").value(454))
            .andExpect(jsonPath("$[0].championWins").value(19))
            .andExpect(jsonPath("$[1].year").value(2022))

        // Verify that the mocked service method was called
        coVerify(exactly = 1) { f1DataService.getAllSeasons() }
    }

    @Test
    fun `GET seasons returns 500 when service throws IllegalStateException`() = runTest {
        // Given
        coEvery { f1DataService.getAllSeasons() } throws IllegalStateException("Failed to process season data")

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred while processing your request"))
    }

    @Test
    fun `GET races for season returns 400 when year is invalid`() = runTest {
        // Given
        val invalidYear = 2004 // Before 2005
        coEvery { f1DataService.getRacesForSeason(invalidYear) } throws
            IllegalArgumentException("Invalid year: $invalidYear. Must be between 2005 and current year.")

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons/$invalidYear/races")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Invalid year: $invalidYear. Must be between 2005 and current year."))
    }

    @Test
    fun `GET races for season returns 404 when season not found`() = runTest {
        // Given
        val year = 2023
        coEvery { f1DataService.getRacesForSeason(year) } throws
            NoSuchElementException("Season data for year $year not found. Please ensure season data is populated first.")

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons/$year/races")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Season data for year $year not found. Please ensure season data is populated first."))
    }

    @Test
    fun `GET races for season returns 404 when Ergast API data not found`() = runTest {
        // Given
        val year = 2023
        coEvery { f1DataService.getRacesForSeason(year) } throws
            ErgastApiDataNotFoundException("No race results data found for year $year")

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons/$year/races")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("No race results data found for year $year"))
    }

    @Test
    fun `GET races for season returns 503 when Ergast API is unavailable`() = runTest {
        // Given
        val year = 2023
        coEvery { f1DataService.getRacesForSeason(year) } throws
            ErgastApiServiceUnavailableException("Ergast API is currently unavailable. Please try again later.")

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons/$year/races")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isServiceUnavailable)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.error").value("Service Unavailable"))
            .andExpect(jsonPath("$.message").value("The Formula 1 data service is currently unavailable. Please try again later."))
    }

    @Test
    fun `GET races for season returns 429 when Ergast API rate limit is exceeded with retry after`() = runTest {
        // Given
        val year = 2023
        coEvery { f1DataService.getRacesForSeason(year) } throws
            ErgastApiRateLimitException("Rate limit exceeded", 60)

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons/$year/races")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isTooManyRequests)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(429))
            .andExpect(jsonPath("$.error").value("Too Many Requests"))
            .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please try again later."))
            .andExpect(header().string("Retry-After", "60"))
    }

    @Test
    fun `GET races for season returns 429 when Ergast API rate limit is exceeded without retry after`() = runTest {
        // Given
        val year = 2023
        coEvery { f1DataService.getRacesForSeason(year) } throws
            ErgastApiRateLimitException("Rate limit exceeded", null)

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons/$year/races")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isTooManyRequests)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(429))
            .andExpect(jsonPath("$.error").value("Too Many Requests"))
            .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please try again later."))
            .andExpect(header().doesNotExist("Retry-After"))
    }

    @Test
    fun `GET races for season returns 502 when Ergast API response is invalid`() = runTest {
        // Given
        val year = 2023
        coEvery { f1DataService.getRacesForSeason(year) } throws
            ErgastApiInvalidResponseException("Invalid JSON response from Ergast API")

        // When/Then
        val mvcResult = mockMvc.perform(
            get("/api/seasons/$year/races")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andDo(print())
            .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andDo(print())
            .andExpect(status().isBadGateway)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.status").value(502))
            .andExpect(jsonPath("$.error").value("Bad Gateway"))
            .andExpect(jsonPath("$.message").value("Received invalid data from Formula 1 data service"))
    }
}
