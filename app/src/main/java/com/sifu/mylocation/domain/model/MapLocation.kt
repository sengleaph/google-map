package com.sifu.mylocation.domain.model

data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val title: String = "",
    val snippet: String = ""
)
 
data class MapMarker(
    val id: String,
    val location: MapLocation,
    val title: String,
    val snippet: String = "",
    val isSelected: Boolean = false
)
 
enum class MapType {
    NORMAL,
    SATELLITE,
    HYBRID,
    TERRAIN
}