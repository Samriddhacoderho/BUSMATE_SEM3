package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DriverModel(
    val driverId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val licenseNumber: String = "",
) : Parcelable {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "driverId" to driverId,
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "licenseNumber" to licenseNumber
        )
    }
}