package com.example.busmate.view.admin

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.viewmodel.BusViewModel

class EditBusActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bus = intent.getParcelableExtra<BusModel>("bus_data")
        val viewModel = BusViewModel(BusRepositoryImpl())

        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            }
            var themeUpdateTrigger by remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeUpdateTrigger++
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeUpdateTrigger) {
                BusMateTheme(darkTheme = isDarkMode()) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBusScreen(
    bus: BusModel,
    viewModel: BusViewModel,
    onBack: () -> Unit
) {
    // FIXED: Explicit Blue Color to remove the Purple
    val busMateBlue = Color(0xFF2567E8)
    val context = LocalContext.current

    var busNumber by remember { mutableStateOf(bus.busNumber) }
    var licensePlate by remember { mutableStateOf(bus.licensePlate) }
    var routeId by remember { mutableStateOf(bus.routeId) }
    var capacity by remember { mutableStateOf(bus.capacity.toString()) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf(bus.busImage) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedImageUri = uri
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bus Profile Photo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, busMateBlue, RoundedCornerShape(16.dp))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedImageUri ?: imageUrl,
                    contentDescription = "Bus Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = busMateBlue)
                }
            }

            Text("Tap image to change", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(24.dp))

            EditBusField("Bus Number", busNumber) { busNumber = it }
            EditBusField("License Plate", licensePlate) { licensePlate = it }
            EditBusField("Route ID", routeId) { routeId = it }
            EditBusField("Capacity", capacity) { capacity = it }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (selectedImageUri != null) {
                        isUploading = true
                        viewModel.uploadBusImage(context, selectedImageUri!!) { uploadedUrl ->
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
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

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

@Composable
fun EditBusField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    )
}