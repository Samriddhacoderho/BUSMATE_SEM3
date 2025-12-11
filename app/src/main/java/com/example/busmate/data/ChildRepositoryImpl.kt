package com.example.busmate.data

import com.example.busmate.model.ChildModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class ChildRepositoryImpl : ChildRepositoryInterface{
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun addChild(model: ChildModel, callback: (String, Boolean) -> Unit) {
        val parentUid = auth.currentUser?.uid
        if (parentUid == null) {
            callback("User not logged in (Parent UID missing)", false)
            return
        }

        // 1. Check for GLOBAL Uniqueness of studentId
        try {
            // Replaced GLOBAL_STUDENT_ID_INDEX with "studentIdIndex"
            val uniqueIndexDoc = firestore.collection("studentIdIndex")
                .document(model.studentId)
                .get()
                .await()

            if (uniqueIndexDoc.exists()) {
                callback("Registration failed: Student ID ${model.studentId} is already registered.", false)
                return
            }

            // 2. Perform Atomic Write (Transaction)
            firestore.runTransaction { transaction ->

                // 2a. Add the child to the Parent's nested map
                // Replaced PARENT_COLLECTION with "users"
                val parentRef = firestore.collection("users").document(parentUid)

                // Assuming model.toMap() exists on ChildModel
                transaction.update(
                    parentRef,
                    "children.${model.studentId}",
                    model.toMap()
                )

                // 2b. Create the Global Uniqueness Index Record
                // Replaced GLOBAL_STUDENT_ID_INDEX with "studentIdIndex"
                val indexRef = firestore.collection("studentIdIndex").document(model.studentId)
                transaction.set(indexRef, mapOf(
                    "parentUid" to parentUid,
                    "timestamp" to FieldValue.serverTimestamp()
                ))

                null // Transaction completes successfully
            }.await()

            callback("Child ${model.firstName} successfully added", true)

        } catch (e: Exception) {
            callback("Failed to add child: ${e.message}", false)
        }
    }
}