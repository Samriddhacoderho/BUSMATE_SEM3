package com.example.busmate.data

import com.example.busmate.model.ChildModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChildRepositoryImpl : ChildRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    private val usersRef = db.getReference("users")
    private val studentIndexRef = db.getReference("studentIdIndex")

    override fun addChild(
        model: ChildModel,
        callback: (String, Boolean) -> Unit
    ) {

        val parentUid = auth.currentUser?.uid
        if (parentUid == null) {
            callback("User not logged in", false)
            return
        }

        // 1️⃣ Check uniqueness of Student ID
        studentIndexRef.child(model.studentId)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {
                    callback(
                        "Registration failed: Student ID ${model.studentId} already exists",
                        false
                    )
                    return@addOnSuccessListener
                }

                // 2️⃣ Atomic multi-path update
                val updates = hashMapOf<String, Any>(
                    "/users/$parentUid/children/${model.studentId}" to model,
                    "/studentIdIndex/${model.studentId}" to mapOf(
                        "parentUid" to parentUid,
                        "timestamp" to System.currentTimeMillis()
                    )
                )

                db.reference.updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(
                                "Child ${model.firstName} successfully added",
                                true
                            )
                        } else {
                            callback(
                                "Failed to add child: ${task.exception?.message}",
                                false
                            )
                        }
                    }
            }
            .addOnFailureListener {
                callback("Failed to verify Student ID", false)
            }
    }
}
