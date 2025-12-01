package com.example.busmate.data

import com.example.busmate.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun registerUser(
        user: UserModel,
        password: String
    ): Result<UserModel> {

        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("User is null"))

            val updatedUser = user.copy(uid = firebaseUser.uid)
            //updatedUser=uid=2u130283u2091u9 firstname="Samriddha', lastname=Satyal ........

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

            val email = snapshot.documents.first().getString("email")
                ?: return Result.failure(Exception("Email not found for this user"))

            val signInResult = auth.signInWithEmailAndPassword(email, password).await()
            if (signInResult.user == null) {
                return Result.failure(Exception("Invalid Email ID or Password"))
            }
            val userModel: UserModel=snapshot.documents.first().toObject(UserModel::class.java)!!

            Result.success(userModel)

        } catch (e: Exception) {
            val message=when(e){
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
            // Get the currently logged-in user
            val user = auth.currentUser ?: return Result.failure(Exception("No user currently logged in."))

            // Re-authenticate user for security
            val credential = EmailAuthProvider.getCredential(
                user.email ?: throw Exception("User email is missing for re-authentication."),
                oldPassword
            )
            user.reauthenticate(credential).await()

            // Update the password
            user.updatePassword(newPassword).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }

    }


}
