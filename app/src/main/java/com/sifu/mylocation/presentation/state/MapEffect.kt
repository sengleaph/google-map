package com.sifu.mylocation.presentation.state

import com.google.android.gms.maps.model.LatLng
import com.sifu.mylocation.domain.model.Atm
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker

sealed interface MapEffect {
 
    // Navigation / Camera
    data class AnimateCameraToLocation(val latLng: LatLng, val zoom: Float = 15f) : MapEffect
    data class AnimateCameraToMarker(val marker: MapMarker) : MapEffect
 
    // Permissions
    data object RequestLocationPermission : MapEffect
 
    // System
    data class ShowToast(val message: String) : MapEffect
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : MapEffect
 
    // Marker dialogs
    data class ShowAddMarkerDialog(val latLng: LatLng) : MapEffect
    data class ShowMarkerDetailsBottomSheet(val marker: MapMarker) : MapEffect
 
    // Search
    data object ClearSearchFocus : MapEffect
    data class ShowSearchResults(val results: List<MapLocation>) : MapEffect

}
