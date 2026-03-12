package com.sifu.mylocation.presentation.state

sealed class AtmEvent {
    data object LoadAtms : AtmEvent()
    data class OnAtmMarkerClicked(val atmId: String) : AtmEvent()
    data object OnDismissBottomSheet : AtmEvent()
}