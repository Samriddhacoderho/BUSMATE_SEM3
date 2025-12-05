package com.example.busmate.data

import com.example.busmate.model.CreateAccountModel
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

    suspend fun createAccount(
        model: CreateAccountModel,callback:(String,Boolean)->Unit
    )




}
