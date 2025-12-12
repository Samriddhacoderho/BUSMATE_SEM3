package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.AdminActionsInterface
import com.example.busmate.model.UserModel

class AdminActionsViewModel(val repo: AdminActionsInterface) : ViewModel() {
    fun getUserbyID(userID: String, callback: (Boolean, UserModel?) -> Unit) {
        repo.getUserbyID(userID, callback)
    }

    fun deactivateAccount(userID: String,callback: (Boolean, String) -> Unit){
        repo.deactivateUser(userID,callback)
    }

    fun deleteAccount(userID: String,callback: (Boolean, String) -> Unit){
        repo.deleteUser(userID,callback)
    }

}