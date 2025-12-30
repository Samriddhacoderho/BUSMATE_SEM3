package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var uid: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var schoolId: String = "",
    var phone: String = "",
    var typeofUser: String = "",
    var children: Map<String, ChildModel> = emptyMap(),
    var status: String = "active",
    var fcmToken: String = "",

    // Developer branch
    var profileImage: String = ""
) : Parcelable {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "schoolId" to schoolId,
            "phone" to phone,
            "typeofUser" to typeofUser,
            "children" to children,
            "status" to status,
            "profileImage" to profileImage,
            "fcmToken" to fcmToken
        )
    }
}
