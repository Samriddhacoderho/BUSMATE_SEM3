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


    override fun adminPreAddStudentId(studentId: String, callback: (String, Boolean) -> Unit) {
        // 1. First, check if this ID already exists in the studentIdIndex node
        studentIndexRef.child(studentId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // 2. If it exists, check its status to give a specific error message
                val status = snapshot.child("status").getValue(String::class.java)
                if (status == "used") {
                    callback("Error: ID $studentId is already registered to a parent.", false)
                } else {
                    callback("Error: ID $studentId is already added and available.", false)
                }
            } else {
                // 3. If it doesn't exist, only then add it as "available"
                val data = mapOf("status" to "available")
                studentIndexRef.child(studentId).setValue(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback("Student ID $studentId added successfully", true)
                        } else {
                            callback("Failed to add ID: ${task.exception?.message}", false)
                        }
                    }
            }
        }.addOnFailureListener {
            callback("Database error: ${it.message}", false)
        }
    }

    override fun addChild(model: ChildModel, callback: (String, Boolean) -> Unit) {
        val parentUid = auth.currentUser?.uid ?: return callback("User not logged in", false)

        studentIndexRef.child(model.studentId).get().addOnSuccessListener { snapshot ->
            // Check if the Admin actually pre-added this ID
            if (!snapshot.exists()) {
                callback("Invalid Student ID. Please contact school admin.", false)
                return@addOnSuccessListener
            }

            // Check if the ID is already used
            val status = snapshot.child("status").getValue(String::class.java)
            if (status == "used") {
                callback("This Student ID is already registered to another account.", false)
                return@addOnSuccessListener
            }

            // Proceed with update
            val updates = hashMapOf<String, Any>()
            updates["/users/$parentUid/children/${model.studentId}"] = model.toMap()
            updates["/studentIdIndex/${model.studentId}/status"] = "used" // Mark as used
            updates["/studentIdIndex/${model.studentId}/parentUid"] = parentUid

            db.reference.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) callback("Child added successfully", true)
                else callback("Database error", false)
            }
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


    override fun uploadChildImage(
        context: android.content.Context,
        imageUri: android.net.Uri,
        callback: (String?) -> Unit
    ) {
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

                val uploadResponse = cloudinary.uploader()
                    .upload(inputStream, com.cloudinary.utils.ObjectUtils.emptyMap())
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
    // Inside ChildRepositoryImpl.kt

    override fun adminAddChildToParent(
        parentSchoolId: String,
        child: ChildModel,
        callback: (String, Boolean) -> Unit
    ) {
        // 1. Try to find an existing registered user
        usersRef.orderByChild("schoolId").equalTo(parentSchoolId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // SCENARIO A: Parent is already registered. Add directly.
                        val parentUid = snapshot.children.first().key!!
                        usersRef.child(parentUid).child("children")
                            .child(child.studentId).setValue(child)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    studentIndexRef.child(child.studentId).setValue(
                                        mapOf("status" to "used", "parentUid" to parentUid)
                                    )
                                    callback("Child added to Parent!", true)
                                } else {
                                    callback("Failed to save data", false)
                                }
                            }
                    } else {
                        // SCENARIO B: Parent has NOT registered yet.
                        // Store in "user/{schoolId}/pendingChildren"
                        val adminRef = FirebaseDatabase.getInstance().getReference("user")

                        adminRef.child(parentSchoolId).child("pendingChildren")
                            .child(child.studentId).setValue(child)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Mark as used, but no parentUid yet
                                    studentIndexRef.child(child.studentId).setValue(
                                        mapOf("status" to "pending", "reservedFor" to parentSchoolId)
                                    )
                                    callback("Child saved! Will appear when Parent registers.", true)
                                } else {
                                    callback("Failed to save pending child.", false)
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(error.message, false)
                }
            })
    }

    // Add this to ChildRepositoryImpl.kt
    // In ChildRepositoryImpl.kt
    override fun verifyParentExists(parentSchoolId: String, callback: (Boolean) -> Unit) {
        // 1. Check registered users node first
        usersRef.orderByChild("schoolId").equalTo(parentSchoolId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Check if the registered user is a parent
                        val userSnap = snapshot.children.firstOrNull()
                        val type = userSnap?.child("typeofUser")?.getValue(String::class.java)
                        callback(type == "Parent")
                    } else {
                        // 2. Check the pre-registered 'user' node
                        val adminRef = FirebaseDatabase.getInstance().getReference("user")
                        adminRef.child(parentSchoolId).get().addOnSuccessListener { adminSnap ->
                            if (adminSnap.exists()) {
                                val role = adminSnap.child("role").getValue(String::class.java)
                                // STRICT CHECK: Only allow if role is exactly "Parent"
                                callback(role == "Parent")
                            } else {
                                callback(false)
                            }
                        }.addOnFailureListener { callback(false) }
                    }
                }
                override fun onCancelled(error: DatabaseError) { callback(false) }
            })
    }
}

