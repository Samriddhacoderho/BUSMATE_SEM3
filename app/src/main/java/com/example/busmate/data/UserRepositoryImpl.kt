package com.example.busmate.data

import com.example.busmate.model.CreateAccountModel
import com.example.busmate.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val db = FirebaseDatabase.getInstance()

    private val adminRef = db.getReference("user")   // pre-created by admin
    private val usersRef = db.getReference("users")  // registered users

    override fun registerUser(
        user: UserModel,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        adminRef.child(user.schoolId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.exists()) {
                        callback(false, "Invalid User ID. Contact school admin.", null)
                        return
                    }

                    val role = snapshot.child("role").getValue(String::class.java) ?: ""

                    // 2️⃣ Check already registered
                    usersRef.child(user.schoolId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnap: DataSnapshot) {

                                if (userSnap.exists()) {
                                    callback(false, "This User ID is already registered.", null)
                                    return
                                }

                                // 3️⃣ Create Auth account
                                auth.createUserWithEmailAndPassword(user.email, password)
                                    .addOnCompleteListener { task ->

                                        if (!task.isSuccessful) {
                                            callback(
                                                false,
                                                task.exception?.message ?: "Registration failed",
                                                null
                                            )
                                            return@addOnCompleteListener
                                        }

                                        val firebaseUser = task.result.user!!

                                        val updatedUser = user.copy(
                                            uid = firebaseUser.uid,
                                            typeofUser = role
                                        )

                                        // 4️⃣ Save user in Realtime DB
                                        usersRef.child(user.schoolId)
                                            .setValue(updatedUser)
                                            .addOnCompleteListener {

                                                if (it.isSuccessful) {
                                                    callback(
                                                        true,
                                                        "Registration Successful",
                                                        updatedUser
                                                    )
                                                } else {
                                                    callback(false, "Failed to save user", null)
                                                }
                                            }
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, error.message, null)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

//override suspend fun loginUser(userID: String, password: String): Result<UserModel> {
//    return try {
//        val snapshot = firestore.collection("users")
//            .whereEqualTo("schoolId", userID)
//            .get()
//            .await()
//
//        if (snapshot.isEmpty) {
//            return Result.failure(Exception("No such user found"))
//        }
//
//        val document = snapshot.documents.first()
//
//        // Check status field
//        val status = document.getString("status") ?: "deactivated"
//        if (status != "active") {
//            return Result.failure(Exception("Your account is deactivated. Please contact administration."))
//        }
//
//        val email = document.getString("email")
//            ?: return Result.failure(Exception("Email not found for this user"))
//
//        val signInResult = auth.signInWithEmailAndPassword(email, password).await()
//        if (signInResult.user == null) {
//            return Result.failure(Exception("Invalid Email ID or Password"))
//        }
//
//        val userModel: UserModel = document.toObject(UserModel::class.java)!!
//
//        Result.success(userModel)
//
//    } catch (e: Exception) {
//        val message = when (e) {
//            is FirebaseAuthInvalidCredentialsException -> "Invalid Email ID or Password"
//            else -> "Login failed. Please try again."
//        }
//        Result.failure(Exception(message))
//    }
//}


    override fun loginUser(
        schoolId: String,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        usersRef.child(schoolId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.exists()) {
                        callback(false, "No such user found", null)
                        return
                    }

                    val user = snapshot.getValue(UserModel::class.java)
                    if (user == null) {
                        callback(false, "Failed to read user data", null)
                        return
                    }

                    // Check account status
                    if (user.status != "active") {
                        callback(
                            false,
                            "Your account is deactivated. Please contact administration.",
                            null
                        )
                        return
                    }

                    // Login using email from DB
                    auth.signInWithEmailAndPassword(user.email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Successful Login", user)
                            } else {
                                callback(false, "Invalid Email ID or Password", null)
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }



    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val user =
                auth.currentUser ?: return Result.failure(Exception("No user currently logged in."))

            val credential = EmailAuthProvider.getCredential(
                user.email ?: throw Exception("User email is missing for re-authentication."),
                oldPassword
            )

            user.reauthenticate(credential).await()

            user.updatePassword(newPassword).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun createAccount(
        model: CreateAccountModel,
        callback: (String, Boolean) -> Unit
    ) {
        adminRef.child(model.schoolId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        callback("This User ID already exists.", false)
                        return
                    }

                    adminRef.child(model.schoolId)
                        .setValue(model)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                callback("Created Account Successful", true)
                            } else {
                                callback("Failed to Create Account", false)
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(error.message, false)
                }
            })
    }


    override suspend fun sendPasswordResetEmail(
        email: String,
        callback: (String, Boolean) -> Unit
    ) {
        try {
            auth.sendPasswordResetEmail(email).await()
            callback("A reset link has been sent to your email.", true)

        } catch (e: FirebaseAuthInvalidUserException) {
            callback("No account found with this email.", false)

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            callback("Email format is invalid.", false)

        } catch (e: Exception) {
            callback("Error: ${e.message}", false)
        }
    }

    override suspend fun getUserProfile(userId: String): Result<UserModel> {
        return try {
            val documentSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!documentSnapshot.exists()) {
                return Result.failure(Exception("User not found"))
            }

            // Convert document to UserModel
            val userModel = documentSnapshot.toObject(UserModel::class.java)
                ?: return Result.failure(Exception("Error converting document to UserModel"))

            Result.success(userModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<Unit> {
        return try {
            // Access the 'users' collection and update the document
            firestore.collection("users")
                .document(userId) // Use userId to locate the document
                .update(
                    "firstName", firstName,
                    "lastName", lastName,
                    "phone", phone
                )
                .await() // Await the result of the update operation

            Result.success(Unit) // If update is successful
        } catch (e: Exception) {
            Result.failure(e) // If there's an error, return failure
        }
    }
}