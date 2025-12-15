package com.example.busmate.data

import com.google.android.gms.maps.model.LatLng

interface LocationInterface {
    fun startLocationUpdates(
        callback: (LatLng, Boolean) -> Unit // location, isFused
    )

    fun stopLocationUpdates()
}