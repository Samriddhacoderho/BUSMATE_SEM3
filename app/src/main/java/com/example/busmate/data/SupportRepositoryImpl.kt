package com.example.busmate.data

import com.example.busmate.model.SupportModel

class SupportRepositoryImpl : SupportRepositoryInterface{
    override suspend fun writeSupport(
        model: SupportModel,
        callback: (String, Boolean) -> Unit
    ) {
        //to do work
    }

}