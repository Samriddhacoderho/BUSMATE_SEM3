package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var uid:String="",
    var firstName:String="",
    var lastName:String="",
    var email:String="",
    var schoolId:String="",
    var phone:String="",
    var typeofUser:String="",
    var children: Map<String, ChildModel> = emptyMap(),
    var status:String="active",
    var profileImageUrl: String? = null,
): Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "schoolId" to schoolId,
            "phone" to phone,
            "typeofUser" to typeofUser,
            "status" to status,
            "profileImageUrl" to (profileImageUrl ?: ""),
            "children" to children
        )
    }
}