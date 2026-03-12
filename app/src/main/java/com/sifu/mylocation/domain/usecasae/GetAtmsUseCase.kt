package com.sifu.mylocation.domain.usecasae

import com.sifu.mylocation.domain.model.Atm
import com.sifu.mylocation.domain.repository.AtmRepository

class GetAtmsUseCase(private val repository: AtmRepository) {
    suspend operator fun invoke(): Result<List<Atm>> = repository.getAtms()
}