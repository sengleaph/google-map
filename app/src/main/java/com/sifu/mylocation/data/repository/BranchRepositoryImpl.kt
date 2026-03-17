// data/repository/BranchRepositoryImpl.kt
package com.sifu.mylocation.data.repository

import com.sifu.mylocation.data.dto.BranchDto
import com.sifu.mylocation.domain.repository.BranchRepository
import com.sifu.mylocation.domain.repository.LocalBranchDataSource

class BranchRepositoryImpl(
    private val localDataSource: LocalBranchDataSource
) : BranchRepository {

    override suspend fun getBranches(): Result<List<BranchDto>> =
        runCatching {
            localDataSource.getBranchList().marker
        }
}