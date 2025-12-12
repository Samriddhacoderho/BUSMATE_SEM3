package com.example.busmate.data

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
        // schoolId is now absent from the bus object

        if (busNumber.isBlank() || licensePlate.isBlank()) {
            callback("Missing required fields (Bus Number or License Plate)", false)
            return
        }

        try {
            // 1. Define the collection reference: TOP-LEVEL 'buses' COLLECTION
            val busCollectionRef = firestore.collection("buses")

            // --- 2. Check for existing Bus Number (UNIQUE GLOBALLY) ---
            val existingBusNumberQuery = busCollectionRef
                .whereEqualTo("busNumber", busNumber)
                .get()
                .await()

            if (!existingBusNumberQuery.isEmpty) {
                callback("Bus Number '$busNumber' already exists.", false)
                return
            }

            // --- 3. Check for existing License Plate (UNIQUE GLOBALLY) ---
            val existingLicenseQuery = busCollectionRef
                .whereEqualTo("licensePlate", licensePlate)
                .get()
                .await()

            if (!existingLicenseQuery.isEmpty) {
                callback("License Plate '$licensePlate' is already assigned to another bus.", false)
                return
            }

            // 4. Save the new bus record using Firestore's auto-generated ID (.add())
            // The document ID will be a simple unique string (e.g., 'u7gHkP2mJ')
            busCollectionRef.add(bus.toMap()).await()

            callback("Bus $busNumber registered successfully!", true)

        } catch (e: Exception) {
            callback("Failed to register bus: ${e.message}", false)
        }
    }
}