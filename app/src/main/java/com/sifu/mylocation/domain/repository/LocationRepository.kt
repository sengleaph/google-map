package com.sifu.mylocation.domain.repository

import com.sifu.mylocation.domain.model.Atm
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import kotlinx.coroutines.flow.Flow
 
interface LocationRepository {
    suspend fun getCurrentLocation(): Result<MapLocation>
    fun getSavedMarkers(): Flow<List<MapMarker>>
    suspend fun addMarker(marker: MapMarker): Result<Unit>
    suspend fun removeMarker(markerId: String): Result<Unit>
    suspend fun searchLocation(query: String): Result<List<MapLocation>>

}