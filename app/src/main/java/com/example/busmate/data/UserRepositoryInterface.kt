package com.example.busmate.data

import com.example.busmate.model.CreateAccountModel
import com.example.busmate.model.UserModel

interface UserRepositoryInterface {
    fun registerUser(
        user: UserModel,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

//    fun login(email:String,password: String,callback:(String, Boolean, UserModel)->Unit)
    //sir ko changed code

    fun loginUser(
        schoolId: String,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )


    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit>

    fun createAccount(
        model: CreateAccountModel,
        callback: (String, Boolean) -> Unit
    )

    suspend fun sendPasswordResetEmail(
        email: String,
        callback: (String, Boolean) -> Unit
    )
    suspend fun updateUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        phone: String): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<UserModel>

}
