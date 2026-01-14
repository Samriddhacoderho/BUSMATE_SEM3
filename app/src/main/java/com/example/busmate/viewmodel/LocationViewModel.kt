package com.example.busmate.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.busmate.data.AttendanceRepositoryImpl
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.data.LocationInterface
import com.example.busmate.model.BusModel
import com.example.busmate.model.ChildModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel(
    private val repo: LocationInterface,
    private val busRepo: BusRepositoryInterface = BusRepositoryImpl()
) : ViewModel() {

    data class ChildEtaState(
        val childName: String,
        val etaMinutes: Int
    )

    private val attendanceRepo = AttendanceRepositoryImpl()

    private val _driverStudents = MutableStateFlow<List<ChildModel>>(emptyList())
    val driverStudents: StateFlow<List<ChildModel>> = _driverStudents
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

    private var currentRouteDistanceMeters: Int = 0
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
        val effectiveSpeed = if (avgSpeedMps < 0.5f) 5.5f else avgSpeedMps

        _childEtas.value = children.map { child ->
            // Logic: Use route distance if available (more accurate), else use aerial
            val distanceInMeters = if (currentRouteDistanceMeters > 0) {
                currentRouteDistanceMeters.toFloat()
            } else {
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    busLatLng.latitude, busLatLng.longitude,
                    child.pickUpLat, child.pickUpLng,
                    results
                )
                results[0]
            }

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
            onSuccess = { points, distanceMeters ->
                _polylinePoints.value = points
                // Store the road distance for ETA calculation
                currentRouteDistanceMeters = distanceMeters
            },
            onFailure = { error ->
                android.util.Log.e("DirectionsAPI", "Error: $error")
            }
        )
    }

    // Add this to LocationViewModel.kt
    fun fetchDriverRouteWithWaypoints(
        origin: LatLng,
        schoolLocation: LatLng, // Added school location parameter
        students: List<ChildModel>,
        tripType: String,        // Added tripType parameter ("Pickup" or "Drop-off")
        apiKey: String
    ) {
        if (students.isEmpty()) return

        val waypoints: List<LatLng>
        val finalDestination: LatLng

        if (tripType == "Pickup") {
            // Sequence: Current -> All Student Pickups -> School
            waypoints = students.map { LatLng(it.pickUpLat, it.pickUpLng) }
            finalDestination = schoolLocation
        } else {
            // Sequence: Current -> School -> Student Drop-offs -> Last Student
            // 1. First stop is always the school to pick up the kids
            val firstStop = schoolLocation

            // 2. Middle stops are the student drop-off locations (except the last one)
            val studentDropOffs = students.map { LatLng(it.dropOffLat, it.dropOffLng) }

            // Combine School with all drop-offs except the very last one
            waypoints = listOf(firstStop) + studentDropOffs.dropLast(1)

            // 3. Final destination is the last student's house
            finalDestination = studentDropOffs.last()
        }

        busRepo.getRoadSnappedRoute(
            origin = origin,
            destination = finalDestination,
            apiKey = apiKey,
            waypoints = waypoints,
            onSuccess = { points, distance ->
                _polylinePoints.value = points
                currentRouteDistanceMeters = distance
            },
            onFailure = { error ->
                android.util.Log.e("DirectionsAPI", "Driver Route Error: $error")
            }
        )
    }

    // Inside LocationViewModel.kt
    fun fetchStudentsForRoute(driverSchoolId: String) {
        val busRef = FirebaseDatabase.getInstance().getReference("buses")

        busRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var actualRouteId = ""
                for (busSnap in snapshot.children) {
                    val bus = busSnap.getValue(BusModel::class.java)
                    // Comparing Driver Login ID to find the correct bus
                    if (bus?.driver?.schoolId == driverSchoolId) {
                        actualRouteId = bus.routeId
                        break
                    }
                }

                if (actualRouteId.isNotEmpty()) {
                    attendanceRepo.getChildrenByRouteId(actualRouteId) { students ->
                        _driverStudents.value = students
                        Log.d("DRIVER_DEBUG", "Success! Found ${students.size} students for Route: $actualRouteId")
                    }
                } else {
                    Log.e("DRIVER_DEBUG", "No bus found where driver schoolId matches: $driverSchoolId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DRIVER_DEBUG", "Database error: ${error.message}")
            }
        })
    }
}