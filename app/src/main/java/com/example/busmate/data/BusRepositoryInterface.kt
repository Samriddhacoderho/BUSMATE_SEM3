package com.example.busmate.data

import com.example.busmate.model.BusModel

interface BusRepositoryInterface {
    suspend fun registerBus(
        bus: BusModel,
        callback: (String, Boolean) -> Unit
    )
}