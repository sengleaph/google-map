package com.sifu.mylocation.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker
import com.sifu.mylocation.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray

class LocationRepositoryImpl(
    private val context: Context
) : LocationRepository {
 
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
 
    // In-memory marker storage (replace with Room DB for production)
    private val _savedMarkers = MutableStateFlow<List<MapMarker>>(emptyList())
 
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<MapLocation> {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
 
            if (location != null) {
                Result.success(
                    MapLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        title = "My Location"
                    )
                )
            } else {
                // Fallback to last known location
                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    Result.success(
                        MapLocation(
                            latitude = lastLocation.latitude,
                            longitude = lastLocation.longitude,
                            title = "My Location"
                        )
                    )
                } else {
                    Result.failure(Exception("Unable to get current location"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
 
    override fun getSavedMarkers(): Flow<List<MapMarker>> =
        _savedMarkers.asStateFlow()
 
    override suspend fun addMarker(marker: MapMarker): Result<Unit> {
        return try {
            val currentMarkers = _savedMarkers.value.toMutableList()
            currentMarkers.add(marker)
            _savedMarkers.value = currentMarkers
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
 
    override suspend fun removeMarker(markerId: String): Result<Unit> {
        return try {
            _savedMarkers.value = _savedMarkers.value.filter { it.id != markerId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
 
    override suspend fun searchLocation(query: String): Result<List<MapLocation>> {
        // Placeholder: Integrate Google Places API or Geocoder for real search
        return try {
            val geocoder = android.location.Geocoder(context)
            val addresses = geocoder.getFromLocationName(query, 5)
            val locations = addresses?.map { address ->
                MapLocation(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    title = address.featureName ?: query,
                    snippet = address.getAddressLine(0) ?: ""
                )
            } ?: emptyList()
            Result.success(locations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
//    override suspend fun getAtmLocations(): Result<List<Atm>> {
//        return try {
//            // Read JSON from assets
//            val jsonString = context.assets
//                .open("atmMockup.json")
//                .bufferedReader()
//                .use { it.readText() }
//
//            val jsonArray = JSONArray(jsonString)
//            val atmList = mutableListOf<Atm>()
//
//            for (i in 0 until jsonArray.length()) {
//                val obj = jsonArray.getJSONObject(i)
//
//                // Parse currency array
//                val currencyArray = obj.getJSONArray("currency")
//                val currencies = (0 until currencyArray.length())
//                    .map { currencyArray.getString(it) }
//
//                atmList.add(
//                    Atm(
//                        id = obj.getString("id"),
//                        name = obj.getString("name"),
//                        bank = obj.getString("bank"),
//                        address = obj.getString("address"),
//                        latitude = obj.getDouble("latitude"),
//                        longitude = obj.getDouble("longitude"),
//                        is24Hours = obj.getBoolean("is24Hours"),
//                        currency = currencies
//                    )
//                )
//            }
//            Result.success(atmList)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

}
