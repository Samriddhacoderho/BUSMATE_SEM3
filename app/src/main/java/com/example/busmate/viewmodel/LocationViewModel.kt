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
import com.example.busmate.model.BusModel

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

    private val _isTripRunning = MutableStateFlow(false)
    val isTripRunning: StateFlow<Boolean> = _isTripRunning

    private val _polylinePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val polylinePoints: StateFlow<List<LatLng>> = _polylinePoints
    private var trackedBusId: String? = null

    private val speedBuffer = mutableListOf<Float>()
    private val BUFFER_SIZE = 10

    private val _allBuses = MutableStateFlow<List<BusModel?>>(emptyList())
    val allBuses: StateFlow<List<BusModel?>> = _allBuses

    /**
     * Starts live tracking.
     * @param driverUid The UID of the driver currently on the trip.
     * If provided, the ViewModel will trigger the repository to find the
     * driver's assigned bus and update its 'currentLocation' in Firebase.
     */
    fun startTracking(busId: String, driverUid: String? = null) {
        this.trackedBusId = busId // Keep track of it

        // 1. Start GPS Updates (For Driver to send or Parent to see own location)
        repo.startLocationUpdates { latLng, _ ->
            _location.value = latLng

            // Sync to Firebase ONLY if driverUid is present
            driverUid?.let { uid ->
                busRepo.updateLocationByDriver(uid, latLng)
            }
        }

        busRepo.getLiveBusLocation(busId) { coords ->
            _currentBusCoordinates.value = coords
        }

        busRepo.getBusByRouteId(busId) { bus ->
            _isTripRunning.value = bus?.isTripRunning ?: false
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

        // FIX: If speed is 0, use 5.5 m/s (approx 20km/h) as a fallback
        // so parents see a realistic ETA instead of 0 or infinity.
        val effectiveSpeed = if (avgSpeedMps < 0.5f) 5.5f else avgSpeedMps

        _childEtas.value = children.map { child ->
            val results = FloatArray(1)
            Location.distanceBetween(
                busLatLng.latitude, busLatLng.longitude,
                child.pickUpLat, child.pickUpLng,
                results
            )
            val distanceInMeters = results[0]
            val etaMinutes = (distanceInMeters / (effectiveSpeed * 60)).toInt()

            ChildEtaState(childName = child.firstName, etaMinutes = etaMinutes)
        }
    }

    fun trackAllBuses() {
        busRepo.getAllBusesLive { buses ->
            _allBuses.value = buses
        }
    }

    private fun parseCoordinates(coords: String): LatLng {
        return try {
            val parts = coords.split(",")
            LatLng(parts[0].trim().toDouble(), parts[1].trim().toDouble())
        } catch (e: Exception) {
            LatLng(27.7172, 85.3240)
        }
    }

    fun fetchRoadSnappedRoute(origin: LatLng, destination: LatLng, apiKey: String) {
        busRepo.getRoadSnappedRoute(
            origin = origin,
            destination = destination,
            apiKey = apiKey,
            onSuccess = { points ->
                _polylinePoints.value = points // Fixed reference here
            },
            onFailure = { error ->
                android.util.Log.e("DirectionsAPI", "Error: $error")
            }
        )
    }
}