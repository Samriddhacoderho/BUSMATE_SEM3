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
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "BusMate Logo",
                        colorFilter = ColorFilter.tint(PlaceholderBusColor),
                        modifier = Modifier.size(100.dp)
                    )
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
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bus Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    // --- IMAGE PICKER SECTION ---
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = BusMateBlue
                                )
                                Text("Add Photo", fontSize = 12.sp, color = BusMateBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = busNumber,
                        onValueChange = { busNumber = it },
                        label = { Text("Bus Number *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it },
                        label = { Text("License Plate *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = routeId,
                        onValueChange = { routeId = it },
                        label = { Text("Route ID *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                        label = { Text("Capacity *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Show this if the user hasn't picked an image
                        if (selectedImageUri == null) {
                            Text(
                                text = "• Please upload a bus photo",
                                color = Color.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // Show this if any text field is empty
                        if (busNumber.isBlank() || licensePlate.isBlank() || routeId.isBlank() || capacity.isBlank()) {
                            Text(
                                text = "• Please fill all required fields",
                                color = Color.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                                                busImage = imageUrl ?: "" // ✅ Now this works!
                                            )
                                        }
                                    } else {
                                        viewModel.registerBus(
                                            busNumber = busNumber,
                                            licensePlate = licensePlate,
                                            routeId = routeId,
                                            capacity = capacityInt,
                                            busImage = "" // ✅ Now this works!
                                        )
                                    }
                                }
                            }
                        },
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(text = if (!isLoading) "REGISTER NEW BUS" else "Registering...")
                    }
                }
            }
        }
    }
}
// testing add bus image