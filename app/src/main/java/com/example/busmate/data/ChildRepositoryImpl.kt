package com.example.busmate.data

import com.example.busmate.model.ChildModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

    override fun observeChildren(
        parentUid: String,
        callback: (List<ChildModel>) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(parentUid)
            .child("children")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children.mapNotNull {
                        it.getValue(ChildModel::class.java)
                    }
                    callback(children)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
    // ChildRepositoryImpl.kt
    override fun observeAllChildren(callback: (List<ChildModel>) -> Unit) {
        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allChildren = mutableListOf<ChildModel>()
                    // Loop through every user to find their 'children' node
                    for (userSnapshot in snapshot.children) {
                        val childrenNode = userSnapshot.child("children")
                        for (childSnapshot in childrenNode.children) {
                            childSnapshot.getValue(ChildModel::class.java)?.let {
                                allChildren.add(it)
                            }
                        }
                    }
                    callback(allChildren)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    override fun updateChild(model: ChildModel, callback: (String, Boolean) -> Unit) {
        // 1. Find which parent this student belongs to using the index
        studentIndexRef.child(model.studentId).get().addOnSuccessListener { snapshot ->
            val parentUid = snapshot.child("parentUid").getValue(String::class.java)

            if (parentUid != null) {
                // 2. Update the student details in the correct path
                usersRef.child(parentUid).child("children").child(model.studentId)
                    .setValue(model)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback("Student updated successfully", true)
                        } else {
                            callback("Update failed: ${task.exception?.message}", false)
                        }
                    }
            } else {
                callback("Error: Student index not found", false)
            }
        }.addOnFailureListener {
            callback("Database error: ${it.message}", false)
        }
    }
}

