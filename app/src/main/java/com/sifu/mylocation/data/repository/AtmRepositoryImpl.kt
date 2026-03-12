package com.sifu.mylocation.data.repository

import com.sifu.mylocation.data.local.LocalAtmDataSource
import com.sifu.mylocation.domain.model.Atm
import com.sifu.mylocation.domain.repository.AtmRepository

class AtmRepositoryImpl(
    private val localDataSource: LocalAtmDataSource
) : AtmRepository {

    override suspend fun getAtms(): Result<List<Atm>> = runCatching {
        localDataSource.getAtms().map { dto ->
            Atm(
                id = dto.id,
                name = dto.name,
                bank = dto.bank,
                address = dto.address,
                latitude = dto.latitude,
                longitude = dto.longitude,
                is24Hours = dto.is24Hours,
                currency = dto.currency
            )
        }
    }
}