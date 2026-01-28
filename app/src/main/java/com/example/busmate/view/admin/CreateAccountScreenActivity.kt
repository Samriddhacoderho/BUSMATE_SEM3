package com.example.busmate.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.viewmodel.CreateAccountViewModel
import kotlinx.coroutines.launch

class CreateAccountScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = UserRepositoryImpl()
        val viewModel = CreateAccountViewModel(repo)

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
                    CreateAccountScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(viewModel: CreateAccountViewModel) {
    var selectedRole by remember { mutableStateOf("Select Role") }
    var userId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val roles = listOf("Parent", "Driver")

    // AUTOMATIC NAVIGATION LOG
    LaunchedEffect(message) {
        if (message == "Created Account Successful") {

            if (selectedRole == "Parent") {
                // Navigate ONLY for Parent
                val intent = Intent(context, AdminAddChildActivity::class.java).apply {
                    putExtra("PARENT_ID", userId)
                }
                context.startActivity(intent)
                (context as Activity).finish()
            } else {
                // For Driver (or others), just show success and stay / finish
                scope.launch {
                    snackbarHostState.showSnackbar("Account created successfully")
                }
            }

        } else if (message.isNotEmpty() && message != "Loading...") {
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create User ID", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BusMateBlue)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp),
                        colorFilter = ColorFilter.tint(BusMateBlue)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Register Account ID",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = BusMateBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Assign a role and unique ID to the user",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ROLE SELECTION DROPDOWN
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("User Role") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BusMateBlue,
                                focusedLabelColor = BusMateBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(text = role) },
                                    onClick = {
                                        selectedRole = role
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // USER ID INPUT
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("Enter School/User ID") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BusMateBlue,
                            focusedLabelColor = BusMateBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // CREATE ACCOUNT BUTTON
                    Button(
                        onClick = {
                            if (selectedRole == "Select Role" || userId.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please select a role and enter User ID")
                                }
                            } else {
                                viewModel.createAccountWithMinimalData(
                                    role = selectedRole,
                                    schoolId = userId
                                )
                            }
                        },
                        enabled = message != "Loading...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue)
                    ) {
                        Text(
                            text = if (message != "Loading...") "Create Account" else "Creating...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}