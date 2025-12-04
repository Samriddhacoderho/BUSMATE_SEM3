package com.example.busmate.data

interface ResetPasswordRepositoryInterface {
    suspend fun sendPasswordResetEmail(
        email: String,
        callback: (String, Boolean) -> Unit
    )
}
