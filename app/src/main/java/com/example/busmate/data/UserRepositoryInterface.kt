package com.example.busmate.data

import com.example.busmate.model.UserModel

interface UserRepositoryInterface {
    suspend fun registerUser(
        user: UserModel,
        password: String
    ): Result<UserModel>
}
