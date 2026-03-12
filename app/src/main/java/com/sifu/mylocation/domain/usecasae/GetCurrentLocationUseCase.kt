package com.sifu.mylocation.domain.usecasae

import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentLocationUseCase(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(): Result<MapLocation> =
        repository.getCurrentLocation()
}
 

 

 

 
