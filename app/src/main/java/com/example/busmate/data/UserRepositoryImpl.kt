package com.example.busmate.data

import com.example.busmate.model.UserModel
import com.google.firebase.auth.FirebaseAuth
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

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(updatedUser.toMap())
                .await()

            Result.success(updatedUser)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
