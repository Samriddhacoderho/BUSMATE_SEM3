package com.example.busmate.model

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusModel(
    var uid: String = "",
    var busNumber: String = "",
    var licensePlate: String = "",
    var routeId: String = "",
    var capacity: Int = 0,
    var maintenanceStatus: String = "Good",
    var currentLocation: String = "Depot",
    var speed: Double = 0.0,
    var driver: UserModel? = null, // nullable for Realtime DB safety
    @get:PropertyName("isTripRunning")
    @set:PropertyName("isTripRunning")
    @get:JvmName("isTripRunning")
    var isTripRunning:Boolean = false
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "busNumber" to busNumber,
            "licensePlate" to licensePlate,
            "routeId" to routeId,
            "capacity" to capacity,
            "maintenanceStatus" to maintenanceStatus,
            "currentLocation" to currentLocation,
            "speed" to speed,
            "driver" to driver?.toMap(),
            "isTripRunning" to isTripRunning
        )
    }
}
