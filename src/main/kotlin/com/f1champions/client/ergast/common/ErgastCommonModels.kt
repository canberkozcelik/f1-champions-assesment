package com.f1champions.client.ergast.common

import com.fasterxml.jackson.annotation.JsonProperty

// Common DTOs
// (Use @JsonProperty if your Kotlin property names differ from JSON keys,
//  or if JSON keys start with uppercase, which is not idiomatic for Kotlin properties)

// --- Driver ---
data class ErgastDriverDto(
    @JsonProperty("driverId") val driverId: String,
    @JsonProperty("permanentNumber") val permanentNumber: String?, // Can be null for older drivers
    @JsonProperty("code") val code: String?, // Can be null
    @JsonProperty("url") val url: String?,
    @JsonProperty("givenName") val givenName: String,
    @JsonProperty("familyName") val familyName: String,
    @JsonProperty("dateOfBirth") val dateOfBirth: String,
    @JsonProperty("nationality") val nationality: String
)

// --- Constructor ---
data class ErgastConstructorDto(
    @JsonProperty("constructorId") val constructorId: String,
    @JsonProperty("url") val url: String?,
    @JsonProperty("name") val name: String,
    @JsonProperty("nationality") val nationality: String
)

// --- Location (nested in Circuit) ---
data class ErgastLocationDto(
    @JsonProperty("lat") val lat: String,
    @JsonProperty("long") val long: String,
    @JsonProperty("locality") val locality: String,
    @JsonProperty("country") val country: String
)

// --- Circuit ---
data class ErgastCircuitDto(
    @JsonProperty("circuitId") val circuitId: String,
    @JsonProperty("url") val url: String?,
    @JsonProperty("circuitName") val circuitName: String,
    @JsonProperty("Location") val location: ErgastLocationDto
)