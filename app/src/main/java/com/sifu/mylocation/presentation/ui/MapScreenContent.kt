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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.sifu.mylocation.R
import com.sifu.mylocation.data.mapper.toMapMarker
import com.sifu.mylocation.domain.model.MapType
import com.sifu.mylocation.presentation.state.MapEffect
import com.sifu.mylocation.presentation.state.MapEvent
import com.sifu.mylocation.presentation.viewmodel.MapViewModel
import com.sifu.mylocation.util.bitmapDescriptorFromDrawable
import kotlinx.coroutines.launch
import com.google.maps.android.compose.MapType as GoogleMapType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(viewModel: MapViewModel) {

    val state             by viewModel.state.collectAsState()
    val context           = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager      = LocalFocusManager.current
    val scope             = rememberCoroutineScope()

    val searchFocusRequester             = remember { FocusRequester() }
    var showAddMarkerDialog by remember  { mutableStateOf(false) }
    var pendingMarkerLatLng by remember { mutableStateOf<LatLng?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                state.cameraPosition.latitude,
                state.cameraPosition.longitude
            ),
            state.zoomLevel
        )
    }

    // ── Custom icon ───────────────────────────────────────────────────────────
    // produceState runs AFTER composition → Maps SDK is ready → no NullPointerException
    val officeIcon by produceState<BitmapDescriptor?>(initialValue = null) {
        value = context.bitmapDescriptorFromDrawable(
            resId  = R.drawable.ic_map_office,
            sizeDp = 44
        )
    }

    // ── Permission launcher ───────────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onEvent(MapEvent.OnLocationPermissionResult(granted))
    }

    // ── Effect consumer ───────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MapEffect.AnimateCameraToLocation ->
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(effect.latLng, effect.zoom)
                    )
                is MapEffect.AnimateCameraToMarker ->
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                effect.marker.location.latitude,
                                effect.marker.location.longitude
                            ),
                            15f
                        )
                    )
                is MapEffect.RequestLocationPermission ->
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                is MapEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is MapEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message, effect.actionLabel)
                is MapEffect.ShowAddMarkerDialog -> {
                    pendingMarkerLatLng = effect.latLng
                    showAddMarkerDialog = true
                }
                is MapEffect.ShowMarkerDetailsBottomSheet ->
                    scope.launch { bottomSheetState.show() }
                is MapEffect.ClearSearchFocus ->
                    focusManager.clearFocus()
                is MapEffect.ShowSearchResults -> { /* driven by state */ }
            }
        }
    }

    // ── Auto-request permissions on first launch ──────────────────────────────
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ── Map type mapping ──────────────────────────────────────────────────────
    val googleMapType = when (state.mapType) {
        MapType.NORMAL    -> GoogleMapType.NORMAL
        MapType.SATELLITE -> GoogleMapType.SATELLITE
        MapType.HYBRID    -> GoogleMapType.HYBRID
        MapType.TERRAIN   -> GoogleMapType.TERRAIN
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ── Google Map ────────────────────────────────────────────────────
            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties          = MapProperties(
                    mapType             = googleMapType,
                    isMyLocationEnabled = state.isMyLocationEnabled
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled     = false,
                    compassEnabled          = true
                ),
                onMapLongClick = { viewModel.onEvent(MapEvent.OnMapLongClick(it)) },
                onMapClick     = { viewModel.onEvent(MapEvent.OnMapClick(it)) }
            ) {

                // ── User-saved markers ────────────────────────────────────────
                state.markers.forEach { marker ->
                    Marker(
                        state   = MarkerState(
                            position = LatLng(
                                marker.location.latitude,
                                marker.location.longitude
                            )
                        ),
                        title   = marker.title,
                        snippet = marker.snippet,
                        icon    = if (marker.id == state.selectedMarker?.id)
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_AZURE
                            )
                        else
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                            ),
                        onClick = {
                            viewModel.onEvent(MapEvent.OnMarkerClick(marker))
                            false
                        }
                    )
                }

                // ── Branch / ATM markers pinned from JSON lat & lng ───────────
                // RULE: always emit Marker() — never wrap in ?.let
                //       vary `icon` with ?: fallback to keep node count stable
                state.branchMarkers.forEach { branch ->
                    Marker(
                        state   = MarkerState(
                            // lat and lng already Double after mapper conversion
                            position = LatLng(branch.lat, branch.lng)
                        ),
                        title   = branch.name,
                        snippet = buildString {
                            append(branch.address)
                            if (branch.tel.isNotBlank())
                                append("\n☎ ${branch.tel}")
                            if (branch.hour.isNotBlank())
                                append("\n⏰ ${branch.hour}")
                            if (branch.status.isNotBlank())
                                append("\n● ${branch.status}")
                        },
                        // officeIcon is null for ~1 frame only → cyan fallback
                        icon    = officeIcon
                            ?: BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_CYAN
                            ),
                        // dim closed branches
                        alpha   = if (branch.status.equals("Closed", ignoreCase = true))
                            0.45f else 1.0f,
                        onClick = {
                            viewModel.onEvent(
                                MapEvent.OnMarkerClick(branch.toMapMarker())
                            )
                            false
                        }
                    )
                }

                // ── My location dot ───────────────────────────────────────────
                state.currentLocation?.let { loc ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(loc.latitude, loc.longitude)
                        ),
                        title = "My Location",
                        icon  = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_BLUE
                        )
                    )
                }
            }

            // ── Loading indicator ─────────────────────────────────────────────
            if (state.isLoading || state.isBranchMarkersLoading) {
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

            // ── Search bar ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SearchBar(
                    query          = state.searchQuery,
                    isVisible      = state.isSearchVisible,
                    onQueryChange  = { viewModel.onEvent(MapEvent.OnSearchQueryChange(it)) },
                    onSearch       = { viewModel.onEvent(MapEvent.OnSearchSubmit) },
                    onToggle       = { viewModel.onEvent(MapEvent.OnToggleSearch) },
                    onClear        = { viewModel.onEvent(MapEvent.OnClearSearch) },
                    focusRequester = searchFocusRequester
                )

                AnimatedVisibility(
                    visible = state.searchResults.isNotEmpty(),
                    enter   = fadeIn() + slideInVertically(),
                    exit    = fadeOut() + slideOutVertically()
                ) {
                    SearchResultsList(
                        results       = state.searchResults,
                        onResultClick = {
                            viewModel.onEvent(MapEvent.OnSearchResultClick(it))
                        }
                    )
                }
            }

            // ── FABs ──────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapFab(
                    onClick = { viewModel.onEvent(MapEvent.OnMyLocationClick) },
                    icon    = {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                )
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

            // ── Map type chip ─────────────────────────────────────────────────
            Surface(
                modifier       = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = 16.dp),
                shape          = RoundedCornerShape(50),
                color          = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Text(
                    text     = state.mapType.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // ── Branch count chip ─────────────────────────────────────────────
            if (state.branchMarkers.isNotEmpty()) {
                Surface(
                    modifier       = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 16.dp),
                    shape          = RoundedCornerShape(50),
                    color          = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            horizontal = 12.dp, vertical = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint     = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text  = "${state.branchMarkers.size} branches",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

}
