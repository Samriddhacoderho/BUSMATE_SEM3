package com.example.busmate.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.viewmodel.BusViewModel
import com.example.busmate.R
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import com.example.busmate.data.UserRepositoryImpl

class BusScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observe SharedPreferences changes for dark mode
            val context = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeChanged by remember { mutableStateOf(sharedPrefs.getInt("dark_mode_pref", 0)) }

            androidx.compose.runtime.DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeChanged = sharedPrefs.getInt("dark_mode_pref", 0)
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)

                onDispose {
                    sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            key(themeChanged) {
                com.example.busmate.ui.theme.BusMateTheme {
                    BusScreenUI(onBackClick = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScreenUI(
    onBackClick: () -> Unit,
    viewModel: BusViewModel = viewModel()
) {
    var busNumber by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var routeId by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val userRepo = UserRepositoryImpl() // To use uploadImage
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isLoading = message == "Loading"

    val isFormValid = busNumber.isNotBlank() &&
            licensePlate.isNotBlank() &&
            routeId.isNotBlank() && // Removed the duplicate routeId check
            capacity.isNotBlank() &&
            selectedImageUri != null // Added this to ensure button grays out if no photo

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading") {
            scope.launch { snackbarHostState.showSnackbar(message) }
            if (message.contains("success", ignoreCase = true)) {
                busNumber = ""; licensePlate = ""; routeId = ""; capacity = "";
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            //  --- FIXED TOP LOGO SECTION ---
            // We use a Box here so we can use .align(Alignment.TopStart)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(BusMateBlue)
            ) {
                // 1. BACK BUTTON
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 8.dp)
                        .align(Alignment.TopStart) // Now this works inside the Box!
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // 2. LOGO AND TITLE (Centered in the Box)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "New Bus Registration",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            //  --- CARD SECTION ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.70f)
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-16).dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFF8F9FE)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bus Details",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Fill in all required information",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        // --- IMAGE PICKER SECTION ---
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F3F5))
                                .border(
                                    width = 2.dp,
                                    color = if (selectedImageUri != null) Color(0xFF2567E8) else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected Bus Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = Color(0xFF2567E8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Add Photo",
                                        fontSize = 14.sp,
                                        color = Color(0xFF2567E8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = busNumber,
                            onValueChange = { busNumber = it },
                            label = { Text("Bus Number", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("e.g., BUS-001", color = Color(0xFF9E9E9E)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2567E8),
                                focusedLabelColor = Color(0xFF2567E8),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = licensePlate,
                            onValueChange = { licensePlate = it },
                            label = { Text("License Plate", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("e.g., BA-1-PA-1234", color = Color(0xFF9E9E9E)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2567E8),
                                focusedLabelColor = Color(0xFF2567E8),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = routeId,
                            onValueChange = { routeId = it },
                            label = { Text("Route ID", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("e.g., ROUTE-A1", color = Color(0xFF9E9E9E)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2567E8),
                                focusedLabelColor = Color(0xFF2567E8),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                            label = { Text("Capacity", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("e.g., 40", color = Color(0xFF9E9E9E)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2567E8),
                                focusedLabelColor = Color(0xFF2567E8),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Validation Messages
                        if (!isFormValid) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE57373).copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    if (selectedImageUri == null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "•",
                                                color = Color(0xFFE57373),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = "Please upload a bus photo",
                                                color = Color(0xFFE57373),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    if (busNumber.isBlank() || licensePlate.isBlank() || routeId.isBlank() || capacity.isBlank()) {
                                        if (selectedImageUri == null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "•",
                                                color = Color(0xFFE57373),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = "Please fill all required fields",
                                                color = Color(0xFFE57373),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Button(
                            onClick = {
                                submitted = true
                                if (isFormValid){

                                    val capacityInt = capacity.toIntOrNull()
                                    if (busNumber.isBlank() || licensePlate.isBlank() || routeId.isBlank() || routeId.isBlank()  || capacityInt == null || capacityInt <= 0) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Please fill all bus details correctly.")
                                        }
                                    } else {
                                        // Check if an image is selected
                                        // Inside BusScreen.kt Button onClick
                                        if (selectedImageUri != null) {
                                            userRepo.uploadImage(context, selectedImageUri!!) { imageUrl ->
                                                viewModel.registerBus(
                                                    busNumber = busNumber,
                                                    licensePlate = licensePlate,
                                                    routeId = routeId,
                                                    capacity = capacityInt,
                                                    busImage = imageUrl ?: ""
                                                )
                                            }
                                        } else {
                                            viewModel.registerBus(
                                                busNumber = busNumber,
                                                licensePlate = licensePlate,
                                                routeId = routeId,
                                                capacity = capacityInt,
                                                busImage = ""
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2567E8),
                                disabledContainerColor = Color(0xFFBDBDBD),
                                disabledContentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                            Text(
                                text = if (!isLoading) "REGISTER NEW BUS" else "Registering...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
// testing add bus image