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


    fun changePassword(
        oldPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    )

    fun createAccount(
        model: CreateAccountModel,
        callback: (String, Boolean) -> Unit
    )

    fun sendPasswordResetEmail(
        email: String,
        callback: (String, Boolean) -> Unit
    )

    fun getUserProfile(
        schoolId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )
    fun updateUserProfile(
        schoolId: String,
        firstName: String,
        lastName: String,
        phone: String,
        callback: (Boolean, String) -> Unit
    )
    fun getCurrentUserProfile(callback: (Boolean, String, UserModel?) -> Unit)


}
