package com.example.busmate.model

data class SupportModel(
    val uid: String="",
    val name: String="",
    val typeofUser:String="",
    val title:String="",
    val message:String=""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "typeofUser" to typeofUser,
            "title" to title,
            "message" to message
        )
    }
}