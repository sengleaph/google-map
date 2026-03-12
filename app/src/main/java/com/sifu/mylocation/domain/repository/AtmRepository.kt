package com.sifu.mylocation.domain.repository

import com.sifu.mylocation.domain.model.Atm

interface AtmRepository {
    suspend fun getAtms(): Result<List<Atm>>
}