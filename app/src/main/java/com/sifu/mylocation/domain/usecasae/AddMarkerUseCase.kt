package com.sifu.mylocation.domain.usecasae

import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.repository.LocationRepository

class AddMarkerUseCase(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(marker: MapMarker): Result<Unit> =
        repository.addMarker(marker)
}