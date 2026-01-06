//package com.example.busmate.viewmodel
//
//import androidx.lifecycle.ViewModel
//import com.example.busmate.data.AdminActionsInterface
//import com.example.busmate.model.BusModel
//import com.example.busmate.model.UserModel
//
//class AdminActionsViewModel(val repo: AdminActionsInterface) : ViewModel() {
//    fun getUserbyID(userID: String, callback: (Boolean, UserModel?) -> Unit) {
//        repo.getUserbyID(userID, callback)
//    }
//
//    fun deactivateAccount(userID: String,callback: (Boolean, String) -> Unit){
//        repo.deactivateUser(userID,callback)
//    }
//
//    fun deleteAccount(userID: String,callback: (Boolean, String) -> Unit){
//        repo.deleteUser(userID,callback)
//    }
//
//    fun reactivateAccount(userID: String,callback: (Boolean, String) -> Unit){
//        repo.reactivateUser(userID,callback)
//    }
//
//    fun getAllBus(callback: (Boolean, List<BusModel>?) -> Unit){
//        repo.getAllBus(callback)
//    }
//
//    fun getAllDrivers(callback: (Boolean, List<UserModel>?) -> Unit){
//        repo.getAllDrivers(callback)
//    }
//
//    fun assignBusToDriver(busId:String,driverId:String,callback: (Boolean, String) -> Unit) {
//        repo.assignBusToDriver(busId, driverId,callback)
//    }
//}

package com.example.busmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.busmate.data.AdminActionsInterface
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.BusModel
import com.example.busmate.model.UserModel

class AdminActionsViewModel(
    private val repo: AdminActionsInterface,
    private val userRepo: UserRepositoryInterface
) : ViewModel() {

    fun getUserbyID(
        userID: String,
        callback: (Boolean, UserModel?) -> Unit
    ) {
        repo.getUserbyID(userID, callback)
    }

    fun deactivateAccount(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deactivateUser(userID, callback)
    }

    fun reactivateAccount(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.reactivateUser(userID, callback)
    }

    fun deleteAccount(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteUser(userID, callback)
    }

    fun getAllBus(callback: (Boolean, List<BusModel>?) -> Unit) {
        repo.getAllBus { success, list ->
            Log.d("DEBUG", "getAllBus result: success=$success, listSize=${list?.size ?: "null"}")
            callback(success, list)
        }
    }


    fun getAllDrivers(
        callback: (Boolean, List<UserModel>?) -> Unit
    ) {
        repo.getAllDrivers(callback)
    }

    fun assignBusToDriver(
        busId: String,
        driverId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.assignBusToDriver(busId, driverId, callback)
    }

    // AdminActionsViewModel.kt

    // Add this function
    fun verifyAdminPassword(password: String, callback: (Boolean, String) -> Unit) {
        userRepo.verifyPassword(password, callback)
    }
}
