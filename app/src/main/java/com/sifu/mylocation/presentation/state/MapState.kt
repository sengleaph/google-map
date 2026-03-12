package com.sifu.mylocation.presentation.state

import com.google.android.gms.maps.model.LatLng
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.model.MapType

data class MapState(
    val isLoading: Boolean = false,
    val currentLocation: MapLocation? = null,
    val markers: List<MapMarker> = emptyList(),
    val selectedMarker: MapMarker? = null,
    val mapType: MapType = MapType.NORMAL,
    val cameraPosition: LatLng = LatLng(0.0, 0.0),
    val zoomLevel: Float = 12f,
    val isLocationPermissionGranted: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<MapLocation> = emptyList(),
    val isSearchVisible: Boolean = false,
    val isBottomSheetVisible: Boolean = false,
    val errorMessage: String? = null
)