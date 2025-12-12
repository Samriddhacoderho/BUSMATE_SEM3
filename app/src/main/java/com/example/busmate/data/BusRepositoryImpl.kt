package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.BusModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BusRepositoryImpl : BusRepositoryInterface {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun registerBus(
        bus: BusModel,
        callback: (String, Boolean) -> Unit
    ) {
        val busNumber = bus.busNumber.trim()
        val licensePlate = bus.licensePlate.trim()

        if (busNumber.isBlank() || licensePlate.isBlank()) {
            callback("Missing required fields (Bus Number or License Plate)", false)
            return
        }

        try {
            val busCollectionRef = firestore.collection("buses")

            // --- Check unique bus number ---
            val existingBusNumberQuery = busCollectionRef
                .whereEqualTo("busNumber", busNumber)
                .get()
                .await()

            if (!existingBusNumberQuery.isEmpty) {
                callback("Bus Number '$busNumber' already exists.", false)
                return
            }

            // --- Check unique license plate ---
            val existingLicenseQuery = busCollectionRef
                .whereEqualTo("licensePlate", licensePlate)
                .get()
                .await()

            if (!existingLicenseQuery.isEmpty) {
                callback("License Plate '$licensePlate' is already assigned to another bus.", false)
                return
            }

            // ----------------------------------------------------
            // ✅ FIX: Correctly generate and use Firestore document ID
            // ----------------------------------------------------
            val newDocRef = busCollectionRef.document()   // Generate the ID FIRST

            val updatedBus = bus.copy(uid = newDocRef.id)

            // Save the bus at EXACTLY that document ID
            newDocRef.set(updatedBus.toMap()).await()

            Log.d("BusRepo", "Successfully registered bus: ${updatedBus.uid}")

            callback("Bus $busNumber registered successfully!", true)

        } catch (e: Exception) {
            callback("Failed to register bus: ${e.message}", false)
        }
    }
}
