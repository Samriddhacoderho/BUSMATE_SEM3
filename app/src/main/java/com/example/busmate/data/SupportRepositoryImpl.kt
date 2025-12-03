package com.example.busmate.data

import com.example.busmate.model.SupportModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SupportRepositoryImpl : SupportRepositoryInterface {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun writeSupport(
        model: SupportModel,
        callback: (String, Boolean) -> Unit
    ) {
        try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                callback("User not logged in", false)
                return
            }

            val updatedSupport=model.copy(uid = uid, typeofUser = "parent")

            firestore.collection("support")
                .document(uid)
                .set(updatedSupport.toMap())
                .await()

            callback("Support request submitted", true)
        } catch (e: Exception) {
            callback("Failed to submit: ${e.message}", false)
        }
    }
}