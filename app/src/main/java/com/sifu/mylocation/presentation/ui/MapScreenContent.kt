package com.sifu.mylocation.presentation.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sifu.mylocation.domain.model.MapType
import com.sifu.mylocation.presentation.state.MapEffect
import com.sifu.mylocation.presentation.state.MapEvent
import kotlinx.coroutines.launch
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.presentation.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import com.google.maps.android.compose.MapType as GoogleMapType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(viewModel: MapViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(state.cameraPosition.latitude, state.cameraPosition.longitude),
            state.zoomLevel
        )
    }
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var showAddMarkerDialog by remember { mutableStateOf(false) }
    var pendingMarkerLatLng by remember { mutableStateOf<LatLng?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Location Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onEvent(MapEvent.OnLocationPermissionResult(isGranted))
    }

    // ── Effect Consumer ──────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MapEffect.AnimateCameraToLocation -> {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(effect.latLng, effect.zoom)
                    )
                }
                is MapEffect.AnimateCameraToMarker -> {
                    val latLng = LatLng(
                        effect.marker.location.latitude,
                        effect.marker.location.longitude
                    )
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                }
                is MapEffect.RequestLocationPermission -> {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
                is MapEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is MapEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel
                    )
                }
                is MapEffect.ShowAddMarkerDialog -> {
                    pendingMarkerLatLng = effect.latLng
                    showAddMarkerDialog = true
                }
                is MapEffect.ShowMarkerDetailsBottomSheet -> {
                    scope.launch { bottomSheetState.show() }
                }
                is MapEffect.ClearSearchFocus -> focusManager.clearFocus()
                is MapEffect.ShowSearchResults -> { /* results shown in state */ }
            }
        }
    }

    // ── Request permissions on first launch ─────────────────────────────────
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ── Map type conversion ──────────────────────────────────────────────────
    val googleMapType = when (state.mapType) {
        MapType.NORMAL    -> GoogleMapType.NORMAL
        MapType.SATELLITE -> GoogleMapType.SATELLITE
        MapType.HYBRID    -> GoogleMapType.HYBRID
        MapType.TERRAIN   -> GoogleMapType.TERRAIN
    }

    // ── Scaffold ─────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Google Map ───────────────────────────────────────────────────
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = googleMapType,
                    isMyLocationEnabled = state.isMyLocationEnabled
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = true
                ),
                onMapLongClick = { latLng ->
                    viewModel.onEvent(MapEvent.OnMapLongClick(latLng))
                },
                onMapClick = { latLng ->
                    viewModel.onEvent(MapEvent.OnMapClick(latLng))
                }
            ) {
                // Custom markers
                state.markers.forEach { marker ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                marker.location.latitude,
                                marker.location.longitude
                            )
                        ),
                        title = marker.title,
                        snippet = marker.snippet,
                        icon = if (marker.id == state.selectedMarker?.id)
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        else
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            viewModel.onEvent(MapEvent.OnMarkerClick(marker))
                            false
                        }
                    )
                }

                // Current location marker (if no system dot)
                state.currentLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        title = "My Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }

            // ── Loading Indicator ────────────────────────────────────────────
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                        .padding(8.dp)
                )
            }

            // ── Top Search Bar ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SearchBar(
                    query = state.searchQuery,
                    isVisible = state.isSearchVisible,
                    onQueryChange = { viewModel.onEvent(MapEvent.OnSearchQueryChange(it)) },
                    onSearch = { viewModel.onEvent(MapEvent.OnSearchSubmit) },
                    onToggle = { viewModel.onEvent(MapEvent.OnToggleSearch) },
                    onClear = { viewModel.onEvent(MapEvent.OnClearSearch) },
                    focusRequester = searchFocusRequester
                )

                // Search Results Dropdown
                AnimatedVisibility(
                    visible = state.searchResults.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    SearchResultsList(
                        results = state.searchResults,
                        onResultClick = { location ->
                            viewModel.onEvent(MapEvent.OnSearchResultClick(location))
                        }
                    )
                }
            }

            // ── Right FABs ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // My Location
                MapFab(
                    onClick = { viewModel.onEvent(MapEvent.OnMyLocationClick) },
                    icon = { Icon(Icons.Default.MyLocation, contentDescription = "My Location") }
                )
                // Map Type: toggle NORMAL → SATELLITE → HYBRID → TERRAIN
                MapFab(
                    onClick = {
                        val next = when (state.mapType) {
                            MapType.NORMAL    -> MapType.SATELLITE
                            MapType.SATELLITE -> MapType.HYBRID
                            MapType.HYBRID    -> MapType.TERRAIN
                            MapType.TERRAIN   -> MapType.NORMAL
                        }
                        viewModel.onEvent(MapEvent.OnMapTypeChange(next))
                    },
                    icon = {
                        Icon(
                            imageVector = when (state.mapType) {
                                MapType.NORMAL    -> Icons.Default.Map
                                MapType.SATELLITE -> Icons.Default.LayersClear
                                MapType.HYBRID    -> Icons.Default.LayersClear
                                MapType.TERRAIN   -> Icons.Default.Terrain
                            },
                            contentDescription = "Map Type"
                        )
                    }
                )
            }

            // ── Map type label chip ──────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = state.mapType.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // ── Marker count chip ────────────────────────────────────────────
            if (state.markers.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.markers.size} marker${if (state.markers.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    // ── Add Marker Dialog ────────────────────────────────────────────────────
    if (showAddMarkerDialog && pendingMarkerLatLng != null) {
        AddMarkerDialog(
            latLng = pendingMarkerLatLng!!,
            onConfirm = { title, snippet ->
                viewModel.onEvent(MapEvent.OnAddMarker(pendingMarkerLatLng!!, title, snippet))
                showAddMarkerDialog = false
                pendingMarkerLatLng = null
            },
            onDismiss = {
                showAddMarkerDialog = false
                pendingMarkerLatLng = null
            }
        )
    }

    // ── Marker Details Bottom Sheet ──────────────────────────────────────────
    if (state.isBottomSheetVisible && state.selectedMarker != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(MapEvent.OnDismissBottomSheet) },
            sheetState = bottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            MarkerDetailsSheet(
                marker = state.selectedMarker!!,
                onRemove = { viewModel.onEvent(MapEvent.OnRemoveMarker(state.selectedMarker!!.id)) },
                onNavigate = {
                    viewModel.onEvent(MapEvent.OnDismissBottomSheet)
                    val latLng = LatLng(
                        state.selectedMarker!!.location.latitude,
                        state.selectedMarker!!.location.longitude
                    )
                    viewModel.onEvent(
                        MapEvent.OnCameraMove(latLng, state.zoomLevel)
                    )
                }
            )
        }
    }
}
