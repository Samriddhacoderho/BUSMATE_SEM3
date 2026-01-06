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
    private val executor = java.util.concurrent.Executors.newSingleThreadExecutor()

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
    // Inside ChildRepositoryImpl.kt


    fun uploadChildImage(context: android.content.Context, imageUri: android.net.Uri, callback: (String?) -> Unit) {
        executor.execute {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)

                // IMPORTANT: Put your real credentials here!
                val cloudinary = com.cloudinary.Cloudinary(
                    mapOf(
                        "cloud_name" to "dithceay5",
                        "api_key" to "242833732537939",
                        "api_secret" to "qQvbql8xsRUmWuyP2xR-rutoxx0"
                    )
                )

                val uploadResponse = cloudinary.uploader().upload(inputStream, com.cloudinary.utils.ObjectUtils.emptyMap())
                var imageUrl = uploadResponse["url"] as String?

                // Convert to https to avoid security issues on Android
                imageUrl = imageUrl?.replace("http://", "https://")

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(imageUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace() // This helps you see the error in Logcat
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }
    // Add this function to your ChildRepositoryImpl
    override fun getAllAvailableRoutes(callback: (List<String>) -> Unit) {
        val busesRef = FirebaseDatabase.getInstance().getReference("buses")
        busesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = mutableListOf<String>()
                for (busInSnap in snapshot.children) {
                    val routeId = busInSnap.child("routeId").getValue(String::class.java)
                    if (routeId != null) {
                        routes.add(routeId)
                    }
                }
                callback(routes.distinct()) // Use distinct to avoid duplicates
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}

