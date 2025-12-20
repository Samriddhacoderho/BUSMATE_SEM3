package com.example.busmate.view

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

class BusScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusScreenUI(onBackClick = { finish() })
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

    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isLoading = message == "Loading"

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
        containerColor = Color.White
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Bus Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                    OutlinedTextField(value = busNumber, onValueChange = { busNumber = it }, label = { Text("Bus Number *") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    OutlinedTextField(value = licensePlate, onValueChange = { licensePlate = it }, label = { Text("License Plate *") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    OutlinedTextField(value = routeId, onValueChange = { routeId = it }, label = { Text("Route ID *") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                        label = { Text("Capacity *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val capacityInt = capacity.toIntOrNull()
                            if (busNumber.isBlank() || licensePlate.isBlank() || routeId.isBlank() || capacityInt == null || capacityInt <= 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fill all bus details correctly.")
                                }
                            } else {
                                viewModel.registerBus(
                                    busNumber = busNumber,
                                    licensePlate = licensePlate,
                                    routeId = routeId,
                                    capacity = capacityInt,
                                )
                            }
                        },
                        enabled = !isLoading,
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