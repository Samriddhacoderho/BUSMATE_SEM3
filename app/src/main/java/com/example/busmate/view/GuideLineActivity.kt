package com.example.busmate.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.GuideLinesImpl
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.viewmodel.GuideLineViewModel
import com.example.busmate.viewmodel.UserViewModel

class GuideLineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // LOGGING: Check what was received
        val typeOfUser = intent.getStringExtra("typeOfUser")
        Log.d("BUSMATE_DEBUG", "GuideLineActivity started")
        Log.d("BUSMATE_DEBUG", "Received typeOfUser: '$typeOfUser'")

        enableEdgeToEdge()
        setContent {
            // Use the standard theme name
            BusMateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        // Use a safe check. If it's null, we default to Driver to prevent crash
                        when {
                            typeOfUser == "Admin" -> {
                                Log.d("BUSMATE_DEBUG", "Showing Admin Screen")
                                AdminGuidelineScreen()
                            }
                            typeOfUser == "Driver" || typeOfUser == "Parent" -> {
                                Log.d("BUSMATE_DEBUG", "Showing Driver/View Screen for: $typeOfUser")
                                DriverGuidelineScreen()
                            }
                            else -> {
                                Log.e("BUSMATE_DEBUG", "Unknown or Null user type: $typeOfUser")
                                // Fallback UI so it doesn't just show a blank screen or crash
                                Text("Error: User type not recognized. Received: $typeOfUser", Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun AdminGuidelineScreen() {
    // Note: In a real app, use a ViewModel Factory, but keeping your style:
    val viewModel = remember { GuideLineViewModel(GuideLinesImpl()) }
    val currentGuidelines by viewModel.guidelines.collectAsState()
    val message by viewModel.message.collectAsState()
    var textState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    // Update textState only when currentGuidelines changes from Firebase
    LaunchedEffect(currentGuidelines) {
        if (currentGuidelines.isNotEmpty()) {
            textState = currentGuidelines
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Admin Editor", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Write Safety Guidelines...") },
            modifier = Modifier.fillMaxWidth().height(300.dp),
            minLines = 10
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.postGuidelines(textState) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (message == "Posting...") "Saving..." else "Update Guidelines")
        }

        if (message.isNotEmpty() && message != "Posting...") {
            Text(text = message, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun DriverGuidelineScreen() {
    val viewModel = remember { GuideLineViewModel(GuideLinesImpl()) }
    val guidelines by viewModel.guidelines.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Safety Guidelines", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = guidelines.ifEmpty { "Waiting for admin to post guidelines..." },
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}