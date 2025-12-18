package com.example.busmate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.busmate.data.AccelerometerRepository
import com.example.busmate.data.AccelerometerRepositoryImpl
import com.example.busmate.model.AccelerometerModel

class AccelRecieverViewModel(application: Application): AndroidViewModel(application) {
    private val repository: AccelerometerRepository =
        AccelerometerRepositoryImpl(application.applicationContext)

    // Expose the Firebase LiveData to the UI
    val firebaseReading: LiveData<AccelerometerModel> = repository.firebaseData

    init {
        // Start listening to Firebase immediately
        repository.startSyncingFromFirebase()
    }

    override fun onCleared() {
        super.onCleared()
        // Stop listener to prevent memory leaks
        repository.stopSyncingFromFirebase()
    }
}