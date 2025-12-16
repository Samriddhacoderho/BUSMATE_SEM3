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

                    auth.createUserWithEmailAndPassword(user.email, password)
                        .addOnCompleteListener { task ->

                            if (!task.isSuccessful) {
                                callback(false, task.exception?.message ?: "Registration failed", null)
                                return@addOnCompleteListener
                            }

                            val firebaseUser = task.result.user!!

                            val updatedUser = user.copy(
                                uid = firebaseUser.uid,
                                typeofUser = role
                            )

                            usersRef.child(firebaseUser.uid)
                                .setValue(updatedUser)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        callback(true, "Registration Successful", updatedUser)
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


    override fun loginUser(
        schoolId: String,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        usersRef
            .orderByChild("schoolId")
            .equalTo(schoolId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.exists()) {
                        callback(false, "No such user found", null)
                        return
                    }

                    val userSnap = snapshot.children.first()
                    val user = userSnap.getValue(UserModel::class.java)
                    if (user == null) {
                        callback(false, "Failed to read user data", null)
                        return
                    }

                    if (user.status != "active") {
                        callback(false, "Your account is deactivated.", null)
                        return
                    }

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



//    override suspend fun changePassword(
//        oldPassword: String,
//        newPassword: String
//    ): Result<Unit> {
//        return try {
//            val user =
//                auth.currentUser ?: return Result.failure(Exception("No user currently logged in."))
//
//            val credential = EmailAuthProvider.getCredential(
//                user.email ?: throw Exception("User email is missing for re-authentication."),
//                oldPassword
//            )
//
//            user.reauthenticate(credential).await()
//
//            user.updatePassword(newPassword).await()
//
//            Result.success(Unit)
//
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    override fun changePassword(
        oldPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "No user currently logged in.")
            return
        }

        val email = user.email
        if (email.isNullOrEmpty()) {
            callback(false, "User email not found.")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, oldPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    callback(false, "Old password is incorrect.")
                    return@addOnCompleteListener
                }

                user.updatePassword(newPassword)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            callback(true, "Password successfully changed!")
                        } else {
                            callback(
                                false,
                                updateTask.exception?.message ?: "Password update failed."
                            )
                        }
                    }
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



    override fun sendPasswordResetEmail(
        email: String,
        callback: (String, Boolean) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback("A reset link has been sent to your email.", true)
                } else {
                    val msg = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                        else -> task.exception?.message ?: "Failed to send reset email."
                    }
                    callback(msg, false)
                }
            }
    }


    override fun getUserProfile(
        uid: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        usersRef.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(false, "User not found", null)
                        return
                    }

                    val user = snapshot.getValue(UserModel::class.java)
                    if (user == null) {
                        callback(false, "Failed to parse user data", null)
                    } else {
                        callback(true, "Profile Loaded", user)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }




    override fun updateUserProfile(
        uid: String,
        firstName: String,
        lastName: String,
        phone: String,
        callback: (Boolean, String) -> Unit
    ) {
        val updates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone
        )

        usersRef.child(uid)
            .updateChildren(updates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Profile Updated Successfully!")
                } else {
                    callback(false, it.exception?.message ?: "Failed to update profile.")
                }
            }
    }


}
