package com.sifu.mylocation.domain.repository

import com.sifu.mylocation.data.dto.BranchListDto
import com.sifu.mylocation.data.mapper.toDomain
import com.sifu.mylocation.domain.model.BranchMarker
import com.sifu.mylocation.domain.repository.BranchRepository

interface LocalBranchDataSource {
    suspend fun getBranchList(): BranchListDto
}