package com.sifu.mylocation.domain.usecasae

import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetSavedMarkersUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<MapMarker>> =
        repository.getSavedMarkers()
}