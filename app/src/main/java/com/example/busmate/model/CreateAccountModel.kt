package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateAccountModel(

    val schoolId:String="",
    val role:String="",
): Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(

            "schoolId" to schoolId,
            "role" to role
        )
    }
}