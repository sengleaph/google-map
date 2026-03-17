package com.sifu.mylocation.domain.usecasae

import com.sifu.mylocation.data.dto.BranchDto
import com.sifu.mylocation.domain.model.BranchMarker
import com.sifu.mylocation.domain.repository.BranchRepository

class GetBranchesUseCase(
    private val repository: BranchRepository
) {
    suspend operator fun invoke(): Result<List<BranchDto>> =
        repository.getBranches()
}