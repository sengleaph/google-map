package com.sifu.mylocation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AtmDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("bank") val bank: String,
    @SerialName("address") val address: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("is24Hours") val is24Hours: Boolean,
    @SerialName("currency") val currency: List<String>
)