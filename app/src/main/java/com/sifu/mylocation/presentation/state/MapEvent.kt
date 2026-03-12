package com.sifu.mylocation.presentation.state

import com.google.android.gms.maps.model.LatLng
import com.sifu.mylocation.domain.model.Atm
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.model.MapType

sealed interface MapEvent {
 
    // Permission
    data class OnLocationPermissionResult(val isGranted: Boolean) : MapEvent
 
    // Map interactions
    data class OnMapLongClick(val latLng: LatLng) : MapEvent
    data class OnMarkerClick(val marker: MapMarker) : MapEvent
    data class OnMapClick(val latLng: LatLng) : MapEvent
    data class OnCameraMove(val latLng: LatLng, val zoom: Float) : MapEvent
 
    // My location
    data object OnMyLocationClick : MapEvent
    data object OnRequestCurrentLocation : MapEvent
 
    // Markers
    data class OnAddMarker(val latLng: LatLng, val title: String, val snippet: String) : MapEvent
    data class OnRemoveMarker(val markerId: String) : MapEvent
    data object OnDismissMarkerInfo : MapEvent
 
    // Map type
    data class OnMapTypeChange(val mapType: MapType) : MapEvent
 
    // Search
    data class OnSearchQueryChange(val query: String) : MapEvent
    data object OnSearchSubmit : MapEvent
    data class OnSearchResultClick(val location: MapLocation) : MapEvent
    data object OnToggleSearch : MapEvent
    data object OnClearSearch : MapEvent
 
    // Bottom sheet
    data object OnToggleBottomSheet : MapEvent
    data object OnDismissBottomSheet : MapEvent
 
    // Error
    data object OnDismissError : MapEvent
}
