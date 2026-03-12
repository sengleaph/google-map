package com.sifu.mylocation.domain.model

data class Atm(
    val id: String,
    val name: String,
    val bank: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val is24Hours: Boolean,
    val currency: List<String>
)