package com.example.busmate.data

import com.example.busmate.model.SupportModel
import javax.security.auth.callback.Callback

interface SupportRepositoryInterface {
    fun writeSupport(
        model: SupportModel,
        callback: (Boolean, String) -> Unit
    )

    fun fetchSupportMessages(
        callback: (Boolean, String, List<SupportModel>) -> Unit
    )

    fun replyToSupport(
        supportId: String,
        replyMessage: String,
        callback: (Boolean, String) -> Unit
    )


}