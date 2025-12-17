package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.model.AccelRecieverViewModel
import com.example.busmate.model.AccelerometerModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.viewmodel.AccelerometerViewModel

class ChildTripDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChildTripDetailsScreen()

        }
    }
}

@Composable
fun ChildTripDetailsScreen(viewModel: AccelRecieverViewModel = viewModel()) {
    // Convert LiveData to Compose State
    val remoteData by viewModel.firebaseReading.observeAsState(AccelerometerModel())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Remote Real-time Data",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "${"%.1f".format(remoteData.speedMps)}",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "SCALED KM/H",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Last Updated: ${remoteData.timestamp}",
            fontSize = 12.sp
        )
    }
}