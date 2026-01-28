package com.example.busmate.model

data class SupportModel(
    val supportId: String = "",
    val uid: String ="",
    val name: String ="",
    val typeofUser: String? ="",
    val title: String ="",
    val message: String ="",
    val reply: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(

            "supportId" to supportId,
            "uid" to uid,
            "name" to name,
            "typeofUser" to typeofUser,
            "title" to title,
            "message" to message,
            "reply" to reply
        ) as Map<String, Any>
    }
}