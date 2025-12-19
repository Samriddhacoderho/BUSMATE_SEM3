package com.example.busmate.model

import com.google.android.gms.maps.model.LatLng

data class LocationModel(
    val currentLocation: LatLng?=null,
    val fusedLocation: Boolean=true,
    val isLoading: Boolean=true
)

//testing