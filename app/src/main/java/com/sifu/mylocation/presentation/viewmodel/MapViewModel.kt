package com.sifu.mylocation.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.google.android.gms.maps.model.LatLng
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.model.MapType
import com.sifu.mylocation.domain.usecasae.AddMarkerUseCase
import com.sifu.mylocation.domain.usecasae.GetCurrentLocationUseCase
import com.sifu.mylocation.domain.usecasae.GetSavedMarkersUseCase
import com.sifu.mylocation.domain.usecasae.RemoveMarkerUseCase
import com.sifu.mylocation.domain.usecasae.SearchLocationUseCase
import com.sifu.mylocation.presentation.state.MapEffect
import com.sifu.mylocation.presentation.state.MapEvent
import com.sifu.mylocation.presentation.state.MapState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
 
class MapViewModel(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getSavedMarkersUseCase: GetSavedMarkersUseCase,
    private val addMarkerUseCase: AddMarkerUseCase,
    private val removeMarkerUseCase: RemoveMarkerUseCase,
    private val searchLocationUseCase: SearchLocationUseCase
) : ScreenModel {
 
    // ── State ────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()
 
    // ── Effects (one-shot channel) ───────────────────────────────────────────
    private val _effect = Channel<MapEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
 
    init {
        observeMarkers()
    }
 
    // ── Event Handler ────────────────────────────────────────────────────────
    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.OnLocationPermissionResult -> handlePermissionResult(event.isGranted)
            is MapEvent.OnMapLongClick             -> handleMapLongClick(event.latLng)
            is MapEvent.OnMarkerClick              -> handleMarkerClick(event.marker)
            is MapEvent.OnMapClick                 -> handleMapClick(event.latLng)
            is MapEvent.OnCameraMove               -> handleCameraMove(event.latLng, event.zoom)
            is MapEvent.OnMyLocationClick          -> handleMyLocationClick()
            is MapEvent.OnRequestCurrentLocation   -> fetchCurrentLocation()
            is MapEvent.OnAddMarker                -> addMarker(event.latLng, event.title, event.snippet)
            is MapEvent.OnRemoveMarker             -> removeMarker(event.markerId)
            is MapEvent.OnDismissMarkerInfo        -> dismissMarkerInfo()
            is MapEvent.OnMapTypeChange            -> changeMapType(event.mapType)
            is MapEvent.OnSearchQueryChange        -> updateSearchQuery(event.query)
            is MapEvent.OnSearchSubmit             -> performSearch()
            is MapEvent.OnSearchResultClick        -> handleSearchResultClick(event.location)
            is MapEvent.OnToggleSearch             -> toggleSearch()
            is MapEvent.OnClearSearch              -> clearSearch()
            is MapEvent.OnToggleBottomSheet        -> toggleBottomSheet()
            is MapEvent.OnDismissBottomSheet       -> dismissBottomSheet()
            is MapEvent.OnDismissError             -> dismissError()
        }
    }
 
    // ── Observers ────────────────────────────────────────────────────────────
    private fun observeMarkers() {
        getSavedMarkersUseCase()
            .onEach { markers ->
                _state.update { it.copy(markers = markers) }
            }
            .launchIn(screenModelScope)
    }
 
    // ── Handlers ─────────────────────────────────────────────────────────────
    private fun handlePermissionResult(isGranted: Boolean) {
        _state.update { it.copy(isLocationPermissionGranted = isGranted, isMyLocationEnabled = isGranted) }
        if (isGranted) {
            fetchCurrentLocation()
        } else {
            sendEffect(MapEffect.ShowSnackbar("Location permission denied. Some features are limited."))
        }
    }
 
    private fun handleMapLongClick(latLng: LatLng) {
        sendEffect(MapEffect.ShowAddMarkerDialog(latLng))
    }
 
    private fun handleMarkerClick(marker: MapMarker) {
        _state.update { it.copy(selectedMarker = marker) }
        sendEffect(MapEffect.ShowMarkerDetailsBottomSheet(marker))
    }
 
    private fun handleMapClick(latLng: LatLng) {
        if (_state.value.selectedMarker != null) {
            _state.update { it.copy(selectedMarker = null, isBottomSheetVisible = false) }
        }
    }
 
    private fun handleCameraMove(latLng: LatLng, zoom: Float) {
        _state.update { it.copy(cameraPosition = latLng, zoomLevel = zoom) }
    }
 
    private fun handleMyLocationClick() {
        if (_state.value.isLocationPermissionGranted) {
            fetchCurrentLocation()
        } else {
            sendEffect(MapEffect.RequestLocationPermission)
        }
    }
 
    private fun fetchCurrentLocation() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getCurrentLocationUseCase()
                .onSuccess { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentLocation = location,
                            cameraPosition = latLng
                        )
                    }
                    sendEffect(MapEffect.AnimateCameraToLocation(latLng, zoom = 15f))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                    sendEffect(MapEffect.ShowSnackbar("Failed to get location: ${error.message}"))
                }
        }
    }
 
    private fun addMarker(latLng: LatLng, title: String, snippet: String) {
        screenModelScope.launch {
            val marker = MapMarker(
                id = UUID.randomUUID().toString(),
                location = MapLocation(
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                ),
                title = title,
                snippet = snippet
            )
            addMarkerUseCase(marker)
                .onSuccess {
                    sendEffect(MapEffect.ShowToast("Marker '$title' added!"))
                }
                .onFailure { error ->
                    sendEffect(MapEffect.ShowSnackbar("Failed to add marker: ${error.message}"))
                }
        }
    }
 
    private fun removeMarker(markerId: String) {
        screenModelScope.launch {
            removeMarkerUseCase(markerId)
                .onSuccess {
                    _state.update { it.copy(selectedMarker = null, isBottomSheetVisible = false) }
                    sendEffect(MapEffect.ShowToast("Marker removed"))
                }
                .onFailure { error ->
                    sendEffect(MapEffect.ShowSnackbar("Failed to remove marker: ${error.message}"))
                }
        }
    }
 
    private fun dismissMarkerInfo() {
        _state.update { it.copy(selectedMarker = null, isBottomSheetVisible = false) }
    }
 
    private fun changeMapType(mapType: MapType) {
        _state.update { it.copy(mapType = mapType) }
        sendEffect(MapEffect.ShowToast("Map type: ${mapType.name}"))
    }
 
    private fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
 
    private fun performSearch() {
        val query = _state.value.searchQuery.trim()
        if (query.isEmpty()) return
 
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            searchLocationUseCase(query)
                .onSuccess { results ->
                    _state.update { it.copy(isLoading = false, searchResults = results) }
                    if (results.isEmpty()) {
                        sendEffect(MapEffect.ShowSnackbar("No results found for '$query'"))
                    } else {
                        sendEffect(MapEffect.ShowSearchResults(results))
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    sendEffect(MapEffect.ShowSnackbar("Search failed: ${error.message}"))
                }
        }
        sendEffect(MapEffect.ClearSearchFocus)
    }
 
    private fun handleSearchResultClick(location: MapLocation) {
        val latLng = LatLng(location.latitude, location.longitude)
        _state.update { it.copy(cameraPosition = latLng, isSearchVisible = false, searchResults = emptyList()) }
        sendEffect(MapEffect.AnimateCameraToLocation(latLng, zoom = 15f))
    }
 
    private fun toggleSearch() {
        _state.update { it.copy(isSearchVisible = !it.isSearchVisible, searchResults = emptyList()) }
    }
 
    private fun clearSearch() {
        _state.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }
 
    private fun toggleBottomSheet() {
        _state.update { it.copy(isBottomSheetVisible = !it.isBottomSheetVisible) }
    }
 
    private fun dismissBottomSheet() {
        _state.update { it.copy(isBottomSheetVisible = false, selectedMarker = null) }
    }
 
    private fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }
 
    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun sendEffect(effect: MapEffect) {
        screenModelScope.launch {
            _effect.send(effect)
        }
    }
}
