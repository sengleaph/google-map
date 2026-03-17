package com.sifu.mylocation.domain.repository

import com.sifu.mylocation.data.dto.BranchDto

interface BranchRepository {
    suspend fun getBranches(): Result<List<BranchDto>>
}