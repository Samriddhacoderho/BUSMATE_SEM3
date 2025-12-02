package com.example.busmate.data

import com.example.busmate.model.UserModel

interface UserRepositoryInterface {
    suspend fun registerUser(
        user: UserModel,
        password: String
    ): Result<UserModel>

//    fun login(email:String,password: String,callback:(String, Boolean, UserModel)->Unit)
    //sir ko changed code

    suspend fun loginUser(userID:String,password: String): Result<UserModel>

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit>
}
