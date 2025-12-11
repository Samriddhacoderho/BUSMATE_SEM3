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
        val schoolId = bus.schoolId.trim()
        val busNumber = bus.busNumber.trim()
        val licensePlate = bus.licensePlate.trim()

        if (schoolId.isBlank() || busNumber.isBlank() || licensePlate.isBlank()) {
            callback("Missing required IDs (School, Bus Number, or License Plate)", false)
            return
        }

        try {
            // 1. Define the document reference path: schools/{schoolId}/buses/{busNumber}
            val schoolBusCollectionRef = firestore.collection("schools")
                .document(schoolId)
                .collection("buses")

            val busRef = schoolBusCollectionRef.document(busNumber)

            // 2. Check for existing Bus Number (unique document ID) in this school
            val existingBus = busRef.get().await()
            if (existingBus.exists()) {
                callback("Bus Number '$busNumber' already exists for this school. Use a unique Bus Number.", false)
                return
            }

            // 3. Check for existing License Plate within this school
            val existingLicense = schoolBusCollectionRef
                .whereEqualTo("licensePlate", licensePlate)
                .get()
                .await()

            if (!existingLicense.isEmpty) {
                callback("License Plate '$licensePlate' is already assigned to another bus in this school.", false)
                return
            }


            // 4. Save the new bus record
            // bus.toMap() handles nesting the DriverModel object correctly.
            busRef.set(bus.toMap()).await()

            callback("Bus $busNumber registered successfully!", true)

        } catch (e: Exception) {
            callback("Failed to register bus: ${e.message}", false)
        }
    }
}