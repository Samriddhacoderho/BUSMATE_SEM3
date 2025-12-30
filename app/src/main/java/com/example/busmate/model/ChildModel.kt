package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChildModel (
    val firstName: String = "",
    val lastName: String = "",
    val studentId: String = "", // This must remain the unique identifier
    val busRouteId: String = "",
    val pickUpLocation: String = "",
    val dropOffLocation: String = "",
    // 'parentUid' is removed as it's implicit in the Parent's document path
    val pickUpLat: Double = 0.0,
    val pickUpLng: Double = 0.0,
    val dropOffLat: Double = 0.0,
    val dropOffLng: Double = 0.0,
    val profileImage: String = ""
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "studentId" to studentId,
            "busRouteId" to busRouteId,
            "pickUpLocation" to pickUpLocation,
            "dropOffLocation" to dropOffLocation,
            "pickUpLat" to pickUpLat,
            "pickUpLng" to pickUpLng,
            "dropOffLat" to dropOffLat,
            "dropOffLng" to dropOffLng,
            "profileImage" to profileImage

        )
    }
}