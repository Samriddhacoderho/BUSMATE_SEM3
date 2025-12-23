package com.example.busmate.data

import androidx.lifecycle.LiveData
import com.example.busmate.model.AccelerometerModel

interface AccelerometerRepository {
    // 1. Local data (For the Driver's UI)
    val currentSpeedMps: LiveData<Float>
    val firebaseData: LiveData<AccelerometerModel>

    // Pass the DRIVER'S UID here
    fun startListening(driverUid: String)
    fun stopListening()

    fun startSyncingFromFirebase(busUid: String)
    fun stopSyncingFromFirebase()
    fun updateTripRunning(biusId: String, isRunning: Boolean)

}