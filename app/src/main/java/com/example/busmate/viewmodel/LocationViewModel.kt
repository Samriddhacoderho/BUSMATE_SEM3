package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.LocationInterface
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel (private val repo: LocationInterface): ViewModel(){
    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location

    fun startTracking(callback: (LatLng, Boolean) -> Unit) {
        repo.startLocationUpdates { latLng, fusedWorking ->
            _location.value = latLng
        }
    }

    fun stopLocationUpdates(){
        repo.stopLocationUpdates()
    }
}