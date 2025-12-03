package com.example.busmate.model

data class UserModel(
    val uid:String="",
    val firstName:String="",
    val lastName:String="",
    val email:String="",
    val schoolId:String="",
    val phone:String="",
    val role: String = ""


) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "schoolId" to schoolId,
            "phone" to phone,
            "role" to role

        )
    }
}
