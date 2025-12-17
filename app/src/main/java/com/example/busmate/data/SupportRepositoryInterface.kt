package com.example.busmate.data

import com.example.busmate.model.SupportModel
import javax.security.auth.callback.Callback

interface SupportRepositoryInterface {
    suspend fun writeSupport(
        model: SupportModel,
        callback: (String, Boolean) -> Unit
    )

    suspend fun fetchSupportMessages(
        callback: (List<SupportModel>) -> Unit
    )

    suspend fun replyToSupport(
        supportId: String,
        replyMessage: String,
        callback: (String, Boolean) -> Unit
    )
}
