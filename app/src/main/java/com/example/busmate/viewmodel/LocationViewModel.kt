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
import android.content.pm.PackageManager
import android.app.Application
import androidx.lifecycle.AndroidViewModel // Change to AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class LocationViewModel(
    application: Application,
    private val repo: LocationInterface,
    private val busRepo: BusRepositoryInterface = BusRepositoryImpl()
) : AndroidViewModel(application) {

    data class ChildEtaState(
        val childName: String,
        val etaMinutes: Int
    )

    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location
    private val _currentBusCoordinates = MutableStateFlow("Fetching...")
    val currentBusCoordinates: StateFlow<String> = _currentBusCoordinates

    private val _currentBusSpeed = MutableStateFlow(0f)
    val currentBusSpeed: StateFlow<Float> = _currentBusSpeed

    private val _childEtas = MutableStateFlow<List<ChildEtaState>>(emptyList())
    val childEtas: StateFlow<List<ChildEtaState>> = _childEtas

    private val _isTripRunning = MutableStateFlow(false)
    val isTripRunning: StateFlow<Boolean> = _isTripRunning
    private var trackedBusId: String? = null

    private val speedBuffer = mutableListOf<Float>()
    private val BUFFER_SIZE = 10

    private val _allBuses = MutableStateFlow<List<BusModel?>>(emptyList())
    val allBuses: StateFlow<List<BusModel?>> = _allBuses

    private val _roadPathPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val roadPathPoints: StateFlow<List<LatLng>> = _roadPathPoints

    /**
     * Starts live tracking.
     * @param driverUid The UID of the driver currently on the trip.
     * If provided, the ViewModel will trigger the repository to find the
     * driver's assigned bus and update its 'currentLocation' in Firebase.
     */
    fun startTracking(busId: String, driverUid: String? = null) {
        this.trackedBusId = busId

        // 1. Start GPS Updates (For Driver)
        repo.startLocationUpdates { latLng, _ ->
            _location.value = latLng
            driverUid?.let { uid ->
                busRepo.updateLocationByDriver(uid, latLng)
            }
        }

        // 2. Observe Live Data from Firebase (For Parent/Admin)
        // We update the listener to handle the speed as well
        busRepo.getBusByRouteId(busId) { bus ->
            bus?.let {
                _currentBusCoordinates.value = it.currentLocation
                _currentBusSpeed.value = it.speed.toFloat() // Capture the live speed!
                _isTripRunning.value = it.isTripRunning
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

    fun fetchRoadRoute(origin: LatLng, waypoints: List<LatLng>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Get the Context from the Application
                val context = getApplication<Application>().applicationContext

                // 2. Automatically pull the API KEY from the Manifest
                val appInfo = context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
                val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY")

                if (apiKey.isNullOrEmpty()) return@launch

                // 3. Prepare the URL for the Directions API
                val destination = waypoints.last()
                val waypointsString = waypoints.dropLast(1).joinToString("|") {
                    "${it.latitude},${it.longitude}"
                }

                val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&waypoints=$waypointsString" +
                        "&key=$apiKey"

                // 4. Fetch and Decode
                val response = URL(url).readText()
                val json = JSONObject(response)
                val routes = json.getJSONArray("routes")

                if (routes.length() > 0) {
                    val encodedPolyline = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    _roadPathPoints.value = PolyUtil.decode(encodedPolyline)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
