package com.example.busmate.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await

class ResetPasswordRepositoryImpl : ResetPasswordRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()

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
}
