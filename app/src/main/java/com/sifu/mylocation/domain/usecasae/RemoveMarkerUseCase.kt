package com.sifu.mylocation.domain.usecasae

import com.sifu.mylocation.domain.repository.LocationRepository

class RemoveMarkerUseCase(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(markerId: String): Result<Unit> =
        repository.removeMarker(markerId)
}