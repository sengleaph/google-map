package com.sifu.mylocation.presentation.state

import com.google.android.gms.maps.model.LatLng
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.model.MapType

sealed class MapEvent {
    data class  OnLocationPermissionResult(val isGranted: Boolean)   : MapEvent()
    data class  OnMapLongClick(val latLng: LatLng)                   : MapEvent()
    data class  OnMarkerClick(val marker: MapMarker)                 : MapEvent()
    data class  OnMapClick(val latLng: LatLng)                       : MapEvent()
    data class  OnCameraMove(val latLng: LatLng, val zoom: Float)    : MapEvent()
    data class  OnMapTypeChange(val mapType: MapType)                : MapEvent()
    data class  OnSearchQueryChange(val query: String)               : MapEvent()
    data class  OnSearchResultClick(val location: MapLocation)       : MapEvent()
    data class  OnAddMarker(
        val latLng: LatLng,
        val title: String,
        val snippet: String
    ) : MapEvent()
    data class  OnRemoveMarker(val markerId: String)                 : MapEvent()
    object      OnMyLocationClick                                    : MapEvent()
    object      OnRequestCurrentLocation                             : MapEvent()
    object      OnDismissMarkerInfo                                  : MapEvent()
    object      OnSearchSubmit                                       : MapEvent()
    object      OnToggleSearch                                       : MapEvent()
    object      OnClearSearch                                        : MapEvent()
    object      OnToggleBottomSheet                                  : MapEvent()
    object      OnDismissBottomSheet                                 : MapEvent()
    object      OnDismissError                                       : MapEvent()
}