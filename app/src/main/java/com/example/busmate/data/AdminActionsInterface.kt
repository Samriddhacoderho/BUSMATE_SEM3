package com.example.busmate.data

import com.example.busmate.model.UserModel

interface AdminActionsInterface {
    fun getUserbyID(userID: String,callback: (Boolean, UserModel?) -> Unit)
    fun deactivateUser(userID: String,callback:(Boolean, String)-> Unit)
    fun deleteUser(userID: String,callback:(Boolean, String)-> Unit)
    fun reactivateUser(userID: String,callback: (Boolean, String) -> Unit)

}