package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.data.LocationInterface
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel(
    private val repo: LocationInterface,
    private val busRepo: BusRepositoryInterface = BusRepositoryImpl()
) : ViewModel() {

    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location
    private val _currentBusCoordinates = MutableStateFlow("Fetching...")
    val currentBusCoordinates: StateFlow<String> = _currentBusCoordinates

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
}