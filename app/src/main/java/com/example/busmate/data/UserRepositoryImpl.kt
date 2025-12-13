package com.example.busmate.data

import com.example.busmate.model.CreateAccountModel
import com.example.busmate.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun registerUser(
        user: UserModel,
        password: String,
    ): Result<UserModel> {

        return try {

            // 1️⃣ CHECK IF ADMIN CREATED THIS SCHOOL ID
            val adminDoc = firestore.collection("user")
                .document(user.schoolId)
                .get()
                .await()

            if (!adminDoc.exists()) {
                return Result.failure(Exception("Invalid User ID. Contact school admin."))
            }

            // ADMIN ROLE
            val adminRole = adminDoc.getString("role") ?: ""

            // 2️⃣ CHECK IF THIS USER ID IS ALREADY REGISTERED
            val existingUser = firestore.collection("users")
                .whereEqualTo("schoolId", user.schoolId)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                return Result.failure(Exception("This User ID is already registered."))
            }

            // 3️⃣ USER ROLE MUST MATCH ADMIN ROLE
            if (adminRole.isNotEmpty()) {
                // replace user's role with admin role (user has no role field in your model)
                // so you do not store role inside user, but we enforce the logic here
            }

            // 4️⃣ CREATE AUTH ACCOUNT
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("User is null"))

            val updatedUser = user.copy(uid = firebaseUser.uid, typeofUser = adminRole)

            // 5️⃣ SAVE USER DATA
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(updatedUser.toMap())
                .await()

            Result.success(updatedUser)

        } catch (e: Exception) {
            val message = when (e) {
                is FirebaseAuthUserCollisionException -> "This email is already registered."
                is FirebaseAuthWeakPasswordException -> "Password is too weak."
                is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                else -> "Registration failed. Please try again."
            }
            Result.failure(Exception(message))
        }
    }

    override suspend fun loginUser(userID: String, password: String): Result<UserModel> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("schoolId", userID)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("No such user found"))
            }

            val document = snapshot.documents.first()

            // Check status field
            val status = document.getString("status") ?: "deactivated"
            if (status != "active") {
                return Result.failure(Exception("Your account is deactivated. Please contact administration."))
            }

            val email = document.getString("email")
                ?: return Result.failure(Exception("Email not found for this user"))

            val signInResult = auth.signInWithEmailAndPassword(email, password).await()
            if (signInResult.user == null) {
                return Result.failure(Exception("Invalid Email ID or Password"))
            }

            val userModel: UserModel = document.toObject(UserModel::class.java)!!

            Result.success(userModel)

        } catch (e: Exception) {
            val message = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Invalid Email ID or Password"
                else -> "Login failed. Please try again."
            }
            Result.failure(Exception(message))
        }
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

    override suspend fun createAccount(
        model: CreateAccountModel,
        callback: (String, Boolean) -> Unit
    ) {
        try {

            // 1️⃣ CHECK IF SCHOOL ID ALREADY EXISTS
            val existing = firestore.collection("user")
                .document(model.schoolId)
                .get()
                .await()

            if (existing.exists()) {
                callback("This User ID already exists.", false)
                return
            }

            // 2️⃣ CREATE NEW ACCOUNT
            firestore.collection("user")
                .document(model.schoolId)
                .set(model.toMap())
                .await()

            callback("Created Account Successful", true)

        } catch (e: Exception) {
            callback("Failed to Create Account: ${e.message}", false)
        }
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

    override suspend fun updateUserProfile(userId: String, firstName: String, lastName: String, phone: String): Result<Unit> {
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