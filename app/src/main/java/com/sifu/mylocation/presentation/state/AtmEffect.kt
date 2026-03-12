package com.sifu.mylocation.presentation.state

import com.sifu.mylocation.domain.model.Atm

sealed class AtmEffect {
    data class ShowAtmDetail(val atm: Atm) : AtmEffect()
    data class ShowError(val message: String) : AtmEffect()
    data class MoveCamera(val latitude: Double, val longitude: Double) : AtmEffect()
}