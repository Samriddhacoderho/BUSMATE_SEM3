package com.example.busmate.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.busmate.R // Assuming your drawables are accessible here
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScreen(
    schoolId: String, // Assume this is passed from the admin login session
    viewModel: BusViewModel = viewModel()
) {
    // --- 1. LOCAL STATE FOR ALL FORM FIELDS (Mimicking CreateAccountScreen) ---
    var busNumber by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var routeId by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    var driverId by remember { mutableStateOf("") }
    var driverFirstName by remember { mutableStateOf("") }
    var driverLastName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var driverLicenseNumber by remember { mutableStateOf("") }

    // --- 2. VIEWMODEL STATE & SNACKBAR SETUP ---
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isLoading = message == "Loading"

    // Show Snackbar for messages
    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading") {
            scope.launch { snackbarHostState.showSnackbar(message) }
            // Optional: Clear fields on SUCCESS
            if (message.contains("success", ignoreCase = true)) {
                busNumber = ""; licensePlate = ""; routeId = ""; capacity = "";
                driverId = ""; driverFirstName = ""; driverLastName = ""; driverPhone = ""; driverLicenseNumber = "";
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
            //  TOP LOGO SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.30f) // Reduced height for more form space
                    .background(BusMateBlue),
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
                    text = "Bus & Driver Registration",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            //  CARD SECTION
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f) // Card takes up majority of the space
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()), // Make it scrollable
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Bus Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    OutlinedTextField(value = busNumber, onValueChange = { busNumber = it }, label = { Text("Bus Number *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = licensePlate, onValueChange = { licensePlate = it }, label = { Text("License Plate *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = routeId, onValueChange = { routeId = it }, label = { Text("Route ID *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                        label = { Text("Capacity *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Driver Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    OutlinedTextField(value = driverId, onValueChange = { driverId = it }, label = { Text("Driver ID (UID) *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = driverFirstName, onValueChange = { driverFirstName = it }, label = { Text("First Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = driverLastName, onValueChange = { driverLastName = it }, label = { Text("Last Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = driverPhone,
                        onValueChange = { driverPhone = it.filter { c -> c.isDigit() } },
                        label = { Text("Phone Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(value = driverLicenseNumber, onValueChange = { driverLicenseNumber = it }, label = { Text("License Number *") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(20.dp))

                    // REGISTER BUTTON WITH VALIDATION
                    Button(
                        onClick = {
                            if (busNumber.isBlank() || licensePlate.isBlank() || routeId.isBlank() || capacity.toIntOrNull() == null ||
                                driverId.isBlank() || driverFirstName.isBlank() || driverLastName.isBlank()
                            ) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fill all fields marked with *")
                                }
                            } else {
                                viewModel.registerBus(
                                    schoolId = schoolId,
                                    busNumber = busNumber,
                                    licensePlate = licensePlate,
                                    routeId = routeId,
                                    capacity = capacity.toInt(),
                                    driverId = driverId,
                                    driverFirstName = driverFirstName,
                                    driverLastName = driverLastName,
                                    driverPhone = driverPhone,
                                    driverLicenseNumber = driverLicenseNumber
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(text = if (!isLoading) "REGISTER BUS" else "Registering...")
                    }
                }
            }
        }
    }
}