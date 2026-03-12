package com.sifu.mylocation.domain.usecasae

import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.repository.LocationRepository

class SearchLocationUseCase(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(query: String): Result<List<MapLocation>> =
        repository.searchLocation(query)
}
