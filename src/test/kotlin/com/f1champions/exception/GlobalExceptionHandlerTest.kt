package com.f1champions.exception

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class GlobalExceptionHandlerTest {

    private val exceptionHandler = GlobalExceptionHandler()

    @Test
    fun `handleIllegalArgumentException returns 400 Bad Request`() {
        // Given
        val exception = IllegalArgumentException("Invalid year: 2004")
        val request = createMockRequest("/api/seasons/2004/races")

        // When
        val response = exceptionHandler.handleIllegalArgumentException(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.BAD_REQUEST,
            expectedError = "Bad Request",
            expectedMessage = "Invalid year: 2004",
            expectedPath = "/api/seasons/2004/races"
        )
    }

    @Test
    fun `handleNoSuchElementException returns 404 Not Found`() {
        // Given
        val exception = NoSuchElementException("Season data for year 2023 not found")
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleNoSuchElementException(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.NOT_FOUND,
            expectedError = "Not Found",
            expectedMessage = "Season data for year 2023 not found",
            expectedPath = "/api/seasons/2023/races"
        )
    }

    @Test
    fun `handleIllegalStateException returns 500 Internal Server Error`() {
        // Given
        val exception = IllegalStateException("Failed to process race data")
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleIllegalStateException(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            expectedError = "Internal Server Error",
            expectedMessage = "An unexpected error occurred while processing your request",
            expectedPath = "/api/seasons/2023/races"
        )
    }

    @Test
    fun `handleWebClientResponseException returns 503 Service Unavailable`() {
        // Given
        val exception = WebClientResponseException.create(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Service Unavailable",
            HttpHeaders(),
            ByteArray(0),
            null
        )
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleWebClientResponseException(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.SERVICE_UNAVAILABLE,
            expectedError = "Service Unavailable",
            expectedMessage = "Unable to fetch data from external service",
            expectedPath = "/api/seasons/2023/races"
        )
    }

    @Test
    fun `handleErgastApiServiceUnavailable returns 503 Service Unavailable`() {
        // Given
        val exception = ErgastApiServiceUnavailableException("Service down")
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleErgastApiServiceUnavailable(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.SERVICE_UNAVAILABLE,
            expectedError = "Service Unavailable",
            expectedMessage = "The Formula 1 data service is currently unavailable. Please try again later.",
            expectedPath = "/api/seasons/2023/races"
        )
    }

    @Test
    fun `handleErgastApiDataNotFound returns 404 Not Found with custom message`() {
        // Given
        val exception = ErgastApiDataNotFoundException("Data for 2023 not found")
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleErgastApiDataNotFound(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.NOT_FOUND,
            expectedError = "Not Found",
            expectedMessage = "Data for 2023 not found",
            expectedPath = "/api/seasons/2023/races"
        )
    }

    @Test
    fun `handleErgastApiRateLimit returns 429 Too Many Requests with Retry-After header`() {
        // Given
        val exception = ErgastApiRateLimitException("Rate limit", 60)
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleErgastApiRateLimit(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.TOO_MANY_REQUESTS,
            expectedError = "Too Many Requests",
            expectedMessage = "Rate limit exceeded. Please try again later.",
            expectedPath = "/api/seasons/2023/races"
        )
        assertEquals("60", response.headers.getFirst("Retry-After"))
    }

    @Test
    fun `handleErgastApiRateLimit returns 429 Too Many Requests without Retry-After header when seconds is null`() {
        // Given
        val exception = ErgastApiRateLimitException("Rate limit", null)
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleErgastApiRateLimit(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.TOO_MANY_REQUESTS,
            expectedError = "Too Many Requests",
            expectedMessage = "Rate limit exceeded. Please try again later.",
            expectedPath = "/api/seasons/2023/races"
        )
        assertTrue(response.headers.getFirst("Retry-After") == null)
    }

    @Test
    fun `handleErgastApiInvalidResponse returns 502 Bad Gateway`() {
        // Given
        val exception = ErgastApiInvalidResponseException("Invalid JSON")
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleErgastApiInvalidResponse(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.BAD_GATEWAY,
            expectedError = "Bad Gateway",
            expectedMessage = "Received invalid data from Formula 1 data service",
            expectedPath = "/api/seasons/2023/races"
        )
    }

    @Test
    fun `handleErgastApiException returns 500 Internal Server Error for unknown ErgastApiException`() {
        // Given
        class MyCustomErgastError(message: String) : ErgastApiException(message)
        val exception = MyCustomErgastError("Some F1 API error")
        val request = createMockRequest("/api/seasons/2023/races")

        // When
        val response = exceptionHandler.handleErgastApiException(exception, request)

        // Then
        assertErrorResponse(
            response = response,
            expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            expectedError = "Internal Server Error",
            expectedMessage = "An error occurred while fetching Formula 1 data",
            expectedPath = "/api/seasons/2023/races"
        )
    }

    @Test
    fun `error response includes timestamp`() {
        // Given
        val exception = IllegalArgumentException("Test error")
        val request = createMockRequest("/api/test")

        // When
        val response = exceptionHandler.handleIllegalArgumentException(exception, request)

        // Then
        assertNotNull(response.body?.timestamp)
        assertTrue(response.body?.timestamp is LocalDateTime)
    }

    private fun createMockRequest(path: String): ServletWebRequest {
        val mockRequest = MockHttpServletRequest()
        mockRequest.requestURI = path
        return ServletWebRequest(mockRequest)
    }

    private fun assertErrorResponse(
        response: ResponseEntity<ErrorResponse>,
        expectedStatus: HttpStatus,
        expectedError: String,
        expectedMessage: String,
        expectedPath: String
    ) {
        assertEquals(expectedStatus, response.statusCode)
        assertNotNull(response.body)
        response.body?.let { errorResponse ->
            assertEquals(expectedStatus.value(), errorResponse.status)
            assertEquals(expectedError, errorResponse.error)
            assertEquals(expectedMessage, errorResponse.message)
            assertEquals(expectedPath, errorResponse.path)
            assertNotNull(errorResponse.timestamp)
        }
    }
}
