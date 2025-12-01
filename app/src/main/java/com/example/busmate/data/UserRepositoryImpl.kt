package com.example.busmate.data

import com.example.busmate.model.UserModel
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
}