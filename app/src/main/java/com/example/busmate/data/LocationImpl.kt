package com.example.busmate.data

import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class LocationImpl(private val context: Context): LocationInterface {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private var fusedCallback: LocationCallback? = null

    override fun startLocationUpdates(callback: (LatLng, Boolean) -> Unit) {
        fusedCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                callback(
                    LatLng(loc.latitude, loc.longitude),
                    true
                )
            }
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()
        fusedClient.requestLocationUpdates(
            request,
            fusedCallback!!,
            Looper.getMainLooper()
        )
    }

    override fun stopLocationUpdates() {
        fusedCallback?.let {
            fusedClient.removeLocationUpdates(it)
        }
        fusedCallback = null
    }
}
