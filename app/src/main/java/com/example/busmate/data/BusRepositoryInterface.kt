package com.example.busmate.data

import com.example.busmate.model.BusModel

interface BusRepositoryInterface {

    fun registerBus(
        bus: BusModel,
        callback: (String, Boolean) -> Unit
    )
}
