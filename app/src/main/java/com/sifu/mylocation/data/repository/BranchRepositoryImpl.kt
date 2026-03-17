// data/repository/LocalBranchRepository.kt
package com.sifu.mylocation.data.repository

import com.sifu.mylocation.data.local.LocalAtmDataSource
import com.sifu.mylocation.data.mapper.toDomain
import com.sifu.mylocation.domain.model.BranchMarker
import com.sifu.mylocation.domain.repository.BranchRepository

class LocalBranchRepository(
    private val dataSource: LocalAtmDataSource
) : BranchRepository {

    override suspend fun getBranchMarkers(): Result<List<BranchMarker>> =
        runCatching {
            dataSource.getAtms()
                .mapNotNull { it.toDomain() }  // skips rows with invalid lat/lng
        }
}