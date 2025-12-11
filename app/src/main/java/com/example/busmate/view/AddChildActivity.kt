package com.example.busmate.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.viewmodel.ChildViewModel
// ASSUMPTION: You must import your concrete repository implementation here
import com.example.busmate.data.ChildRepositoryImpl // <-- Crucial Import
import androidx.lifecycle.ViewModel // Import needed for ChildViewModel signature


// --- New Activity Definition ---
class AddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // No changes needed here
            AddChildScreenUI()
        }
    }
}

// --- Main Composable UI (Debugged to avoid Factory crash) ---
@Composable
fun AddChildScreenUI() {
    // 1. Manually Instantiate ViewModel and Repository (Bypassing ViewModelProvider)
    // This is the fix for avoiding the Factory crash
    val repository = remember { ChildRepositoryImpl() } // Assuming ChildRepositoryImpl exists and is instantiable
    val viewModel: ChildViewModel = remember { ChildViewModel(repository) }

    // 2. Context and State Setup
    val context = LocalContext.current
    val activity = context as Activity
    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var busRouteId by remember { mutableStateOf("") }
    var pickUpLocation by remember { mutableStateOf("") }
    var dropOffLocation by remember { mutableStateOf("") }
    val isLoading = message == "Loading"

    // 3. Side Effect for Toast Messages and Success Action (FINISHING ACTIVITY)
    LaunchedEffect(message, isSuccess) {
        if (message.isNotBlank() && !isLoading) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            if (isSuccess) {
                // Success action: Close the activity and return to the previous screen
                activity.finish()
            }

            // Clear the message state after displaying the Toast (whether success or failure)
            // This prevents the toast from reappearing on configuration changes
            viewModel.clearMessage()
        }
    }

    // 4. UI Implementation (Card Stack Design) - Scrollable Content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF0F0F0))
    ) {
        // **HEADER SECTION: Card Stack Design**
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(BusMateBlue)
                .padding(top = 16.dp),
        ) {
            // Back Button
            IconButton(
                onClick = { activity.finish() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back",
                    tint = Color.White
                )
            }

            // Center Content (Icon and Title)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enter Child Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // **CONTENT CARD: Overlapping and Elevated**
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Input Fields
                AddChildInputField(firstName, { firstName = it }, "First Name", Icons.Default.Person)
                AddChildInputField(lastName, { lastName = it }, "Last Name", Icons.Default.Person, isOptional = true)
                AddChildInputField(studentId, { studentId = it }, "Student ID", Icons.Default.Badge, keyboardType = KeyboardType.Number)
                AddChildInputField(busRouteId, { busRouteId = it }, "Bus Route ID", Icons.Default.DirectionsBus)

                Divider(Modifier.padding(vertical = 16.dp))
                Text("Pickup/Dropoff Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                AddChildInputField(pickUpLocation, { pickUpLocation = it }, "Pickup Location", Icons.Default.PinDrop)
                AddChildInputField(dropOffLocation, { dropOffLocation = it }, "Dropoff Location", Icons.Default.PinDrop)

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        // Correctly call the ViewModel function with individual parameters
                        viewModel.addChild(
                            firstName = firstName,
                            lastName = lastName,
                            studentId = studentId,
                            busRouteId = busRouteId,
                            pickUpLocation = pickUpLocation,
                            dropOffLocation = dropOffLocation
                        )
                    },
                    enabled = !isLoading && firstName.isNotBlank() && studentId.isNotBlank() && busRouteId.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isLoading) "Adding..." else "ADD CHILD",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        // Additional spacer to compensate for the card offset
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Reusable Composable for Input Field (kept the same)
@Composable
fun AddChildInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isOptional: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val BusMateBlue = Color(0xFF1976D2)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label + if (!isOptional) " *" else "") },
        leadingIcon = { Icon(icon, contentDescription = null, tint = BusMateBlue) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BusMateBlue,
            focusedLabelColor = BusMateBlue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}