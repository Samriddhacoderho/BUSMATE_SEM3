package com.example.busmate.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.BusViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp


class EditBusActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bus = intent.getParcelableExtra<BusModel>("bus_data")
        val viewModel = BusViewModel(BusRepositoryImpl())
        setContent {
            BusMateTheme {
                if (bus != null) {
                    EditBusScreen(bus, viewModel) { finish() }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bus data not found")
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBusScreen(
    bus: BusModel,
    viewModel: BusViewModel,
    onBack: () -> Unit
) {
    val busMateBlue = Color(0xFF2567E8) // Exact blue from EditStudentActivity
    val context = LocalContext.current

    var busNumber by remember { mutableStateOf(bus.busNumber) }
    var licensePlate by remember { mutableStateOf(bus.licensePlate) }
    var routeId by remember { mutableStateOf(bus.routeId) }
    var capacity by remember { mutableStateOf(bus.capacity.toString()) }

    // 🖼️ IMAGE STATE (Keeping your logic)
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf(bus.busImage) }
    var isUploading by remember { mutableStateOf(false) }

    // ✅ REAL IMAGE PICKER (Keeping your logic)
    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Bus Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = busMateBlue)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA)) // Matching EditStudentActivity background
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🖼️ CLICKABLE BUS IMAGE (Updated UI to match Student Profile style)
            Text("Bus Profile Photo", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 120.dp) // Rectangular aspect ratio
                    .clip(RoundedCornerShape(16.dp))        // Changed from CircleShape to Rounded
                    .background(Color(0xFFE9ECEF))
                    .border(2.dp, busMateBlue, RoundedCornerShape(16.dp))// Matching the student border
                    .clickable {
                        imagePicker.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedImageUri ?: imageUrl,
                    contentDescription = "Bus Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = busMateBlue
                    )
                }
            }

            Text(
                "Tap image to change",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Text Fields using your EditBusField logic but with Student padding
            EditBusField("Bus Number", busNumber) { busNumber = it }
            EditBusField("License Plate", licensePlate) { licensePlate = it }
            EditBusField("Route ID", routeId) { routeId = it }
            EditBusField("Capacity", capacity) { capacity = it }

            Spacer(modifier = Modifier.height(30.dp))

            // Using your original Save Logic inside the new UI button style
            Button(
                onClick = {
                    if (selectedImageUri != null) {
                        isUploading = true
                        viewModel.uploadBusImage(
                            context,
                            selectedImageUri!!
                        ) { uploadedUrl ->
                            isUploading = false
                            if (uploadedUrl != null) {
                                imageUrl = uploadedUrl
                                saveBus(viewModel, bus, busNumber, licensePlate, routeId, capacity, imageUrl)
                                onBack()
                            } else {
                                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        saveBus(viewModel, bus, busNumber, licensePlate, routeId, capacity, imageUrl)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue),
                enabled = !isUploading
            )
            {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // DRIVER SECTION (Added to match the "Parent Contact" section in EditStudentActivity)
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Driver Information",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val driverName = bus.driver?.let { "${it.firstName} ${it.lastName}" } ?: "Not Assigned"
            val driverPhone = bus.driver?.phone ?: "Not Found"

            // Using your existing ReadOnlyField
            ReadOnlyField(label = "Driver Name", value = driverName, icon = Icons.Default.Person)
            ReadOnlyField(label = "Driver Phone", value = driverPhone, icon = Icons.Default.Phone)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )
        }
    }
}

// Logic kept exactly as provided
private fun saveBus(
    viewModel: BusViewModel,
    bus: BusModel,
    busNumber: String,
    licensePlate: String,
    routeId: String,
    capacity: String,
    imageUrl: String
) {
    val updatedBus = bus.copy(
        busNumber = busNumber.trim(),
        licensePlate = licensePlate.trim().uppercase(),
        routeId = routeId.trim(),
        capacity = capacity.toIntOrNull() ?: bus.capacity,
        busImage = imageUrl
    )
    viewModel.updateBus(updatedBus)
}

// UI updated to match EditField padding and shapes
@Composable
fun EditBusField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Matched padding
        shape = RoundedCornerShape(12.dp)
    )
}
//testing edit bus activity
