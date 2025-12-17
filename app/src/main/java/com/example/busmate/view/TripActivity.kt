package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.viewmodel.AccelerometerViewModel

class TripActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripScreen()

        }
    }
}

@Composable
fun TripScreen(viewModel: AccelerometerViewModel = viewModel()) {
    // Collect the state from the ViewModel
    val state by viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Warning about accuracy
        Text(
            text = "Press the button to start the trip",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

//        // The digital display
//        Text(
//            // Display the scaled value
//            text = "${"%.1f".format(state.speedMps)}",
//            fontSize = 80.sp,
//            fontWeight = FontWeight.Bold
//        )
//        Text(
//            // Updated text to reflect it is not true speed
//            text = "SCALED KM/H",
//            fontSize = 20.sp,
//            modifier = Modifier.padding(bottom = 48.dp)
//        )

        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isRunning) {
                Button(onClick = viewModel::stopMeasurement) {
                    Text("Stop Trip")
                }
            } else {
                Button(onClick = viewModel::startMeasurement) {
                    Text("Start Trip")
                }
            }
        }
    }

}

