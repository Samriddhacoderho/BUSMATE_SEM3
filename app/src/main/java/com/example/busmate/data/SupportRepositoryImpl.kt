package com.example.busmate.data

import com.example.busmate.model.SupportModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SupportRepositoryImpl : SupportRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val supportRef = database.getReference("support")

    override suspend fun writeSupport(
        model: SupportModel,
        callback: (String, Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback("User not logged in", false)
            return
        }

        // 1. Generate the unique key for this specific support ticket
        val supportId = supportRef.push().key
        if (supportId == null) {
            callback("Failed to generate support ID", false)
            return
        }

        // 2. IMPORTANT: Copy the supportId into the model so the Admin knows which ID to reply to
        val updatedSupport = model.copy(uid = uid, supportId = supportId)

        supportRef.child(supportId)
            .setValue(updatedSupport)
            .addOnSuccessListener {
                callback("Support request submitted", true)
            }
            .addOnFailureListener {
                callback(it.message ?: "Failed to submit support", false)
            }
    }

    override suspend fun fetchSupportMessages(
        callback: (List<SupportModel>) -> Unit
    ) {
        supportRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    // 3. Retrieve the model and ensure the ID from the database key is mapped
                    child.getValue(SupportModel::class.java)?.copy(supportId = child.key ?: "")
                }
                callback(list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    override suspend fun replyToSupport(
        supportId: String,
        replyMessage: String,
        callback: (String, Boolean) -> Unit
    ) {
        // 4. This now correctly targets the unique push key node instead of the User UID
        supportRef.child(supportId)
            .child("reply")
            .setValue(replyMessage)
            .addOnSuccessListener {
                callback("Reply sent successfully", true)
            }
            .addOnFailureListener {
                callback(it.message ?: "Failed to send reply", false)
            }
    }
}