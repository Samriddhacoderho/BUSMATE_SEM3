package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.data.LocationInterface
import com.example.busmate.model.ChildModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.location.Location

class LocationViewModel(
    private val repo: LocationInterface,
    private val busRepo: BusRepositoryInterface = BusRepositoryImpl()
) : ViewModel() {

    data class ChildEtaState(
        val childName: String,
        val etaMinutes: Int
    )

    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location
    private val _currentBusCoordinates = MutableStateFlow("Fetching...")
    val currentBusCoordinates: StateFlow<String> = _currentBusCoordinates

    private val _childEtas = MutableStateFlow<List<ChildEtaState>>(emptyList())
    val childEtas: StateFlow<List<ChildEtaState>> = _childEtas

    private val speedBuffer = mutableListOf<Float>()
    private val BUFFER_SIZE = 10

    /**
     * Starts live tracking.
     * @param driverUid The UID of the driver currently on the trip.
     * If provided, the ViewModel will trigger the repository to find the
     * driver's assigned bus and update its 'currentLocation' in Firebase.
     */
    fun startTracking(driverUid: String? = null) {
        repo.startLocationUpdates { latLng, _ ->
            // Update the local state flow for the UI (Map/Speedometer)
            _location.value = latLng

            // Sync to Firebase if a driver is active on a trip
            driverUid?.let { uid ->
                busRepo.updateLocationByDriver(uid, latLng)
            }
        }
    }

    /**
     * Stops GPS updates and halts Firebase synchronization.
     */
    fun stopLocationUpdates() {
        repo.stopLocationUpdates()
    }

    // In LocationViewModel.kt
    fun fetchBusLocation(busId: String) {
        busRepo.getLiveBusLocation(busId) { coords ->
            _currentBusCoordinates.value = coords
        }
    }

    fun updateChildEtas(children: List<ChildModel>, currentCoords: String, rawSpeedMps: Float) {
        if (speedBuffer.size >= BUFFER_SIZE) speedBuffer.removeAt(0)
        speedBuffer.add(rawSpeedMps)

        val avgSpeedMps = if (speedBuffer.isNotEmpty()) speedBuffer.average().toFloat() else 0f
        val busLatLng = parseCoordinates(currentCoords)
        val effectiveSpeed = if (avgSpeedMps < 1.0f) 5.5f else avgSpeedMps

        _childEtas.value = children.map { child ->
            val results = FloatArray(1)

            // UPDATED: Changed pLat to pickUpLat and pLng to pickUpLng to match your model
            android.location.Location.distanceBetween(
                busLatLng.latitude, busLatLng.longitude,
                child.pickUpLat, child.pickUpLng,
                results
            )

            val distanceInMeters = results[0]
            val etaMinutes = (distanceInMeters / (effectiveSpeed * 60)).toInt()

            ChildEtaState(
                childName = child.firstName,
                etaMinutes = etaMinutes
            )
        }
    }
    private fun parseCoordinates(coords: String): LatLng {
        return try {
            val parts = coords.split(",")
            LatLng(parts[0].trim().toDouble(), parts[1].trim().toDouble())
        } catch (e: Exception) {
            LatLng(27.7172, 85.3240) // Kathmandu default
        }
    }
}
