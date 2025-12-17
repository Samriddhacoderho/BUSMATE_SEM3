package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.BusModel
import com.google.firebase.database.*

class BusRepositoryImpl : BusRepositoryInterface {

    private val db = FirebaseDatabase.getInstance()
    private val busesRef = db.getReference("buses")

    override fun registerBus(
        bus: BusModel,
        callback: (String, Boolean) -> Unit
    ) {
        val busNumber = bus.busNumber.trim()
        val licensePlate = bus.licensePlate.trim()

        if (busNumber.isBlank() || licensePlate.isBlank()) {
            callback("Missing required fields (Bus Number or License Plate)", false)
            return
        }

        // 1️⃣ Check unique Bus Number
        busesRef.orderByChild("busNumber").equalTo(busNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(busNumberSnap: DataSnapshot) {

                    if (busNumberSnap.exists()) {
                        callback("Bus Number '$busNumber' already exists.", false)
                        return
                    }

                    // 2️⃣ Check unique License Plate
                    busesRef.orderByChild("licensePlate").equalTo(licensePlate)
                        .addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onDataChange(licenseSnap: DataSnapshot) {

                                if (licenseSnap.exists()) {
                                    callback(
                                        "License Plate '$licensePlate' is already assigned to another bus.",
                                        false
                                    )
                                    return
                                }

                                // 3️⃣ Generate Firebase UID
                                val newBusRef = busesRef.push()
                                val busUid = newBusRef.key!!

                                val updatedBus = bus.copy(uid = busUid, driver = null)

                                // 4️⃣ Save bus
                                newBusRef.setValue(updatedBus.toMap())
                                    .addOnCompleteListener {

                                        if (it.isSuccessful) {
                                            Log.d(
                                                "BusRepo",
                                                "Bus registered successfully: $busUid"
                                            )
                                            callback(
                                                "Bus $busNumber registered successfully!",
                                                true
                                            )
                                        } else {
                                            callback(
                                                it.exception?.message
                                                    ?: "Failed to register bus",
                                                false
                                            )
                                        }
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(error.message, false)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(error.message, false)
                }
            })
    }
}
