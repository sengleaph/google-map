package com.sifu.mylocation.presentation.state

import com.sifu.mylocation.domain.model.Atm

data class AtmState(
    val isLoading: Boolean = false,
    val atms: List<Atm> = emptyList(),
    val selectedAtm: Atm? = null,
    val errorMessage: String? = null
)