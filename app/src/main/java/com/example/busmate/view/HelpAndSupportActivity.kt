package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.busmate.data.SupportRepositoryImpl
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.view.dashboard.SupportScreen
import com.example.busmate.viewmodel.SupportViewModel
import com.example.busmate.viewmodel.UserViewModel

class HelpAndSupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val supportViewModel = remember { SupportViewModel(SupportRepositoryImpl()) }
            val userViewModel = remember { UserViewModel(UserRepositoryImpl()) } // Initialize this

            BusMateTheme {
                SupportScreen(
                    viewModel = supportViewModel,
                    userViewModel = userViewModel // Pass it here
                )
            }
        }
    }
}