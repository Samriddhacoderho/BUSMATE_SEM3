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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.CreateAccountViewModel
import kotlinx.coroutines.launch

class CreateAccountScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = UserRepositoryImpl()
        val viewModel = CreateAccountViewModel(repo)

        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
            var themeChanged by remember { mutableStateOf(sharedPrefs.getInt("dark_mode_pref", 0)) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") { themeChanged = sharedPrefs.getInt("dark_mode_pref", 0) }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeChanged) {
                BusMateTheme {
                    CreateAccountScreen(viewModel = viewModel, onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(viewModel: CreateAccountViewModel, onBack: () -> Unit) {
    val busMateBlue = Color(0xFF2854D8)
    var selectedRole by remember { mutableStateOf("Select Role") }
    var userId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val roles = listOf("Parent", "Driver")
    val isRoleSelected = selectedRole != "Select Role"

    LaunchedEffect(message) {
        if (message == "Created Account Successful") {
            if (selectedRole == "Parent") {
                val intent = Intent(context, AdminAddChildActivity::class.java).apply {
                    putExtra("PARENT_ID", userId)
                }
                context.startActivity(intent)
                (context as Activity).finish()
            } else {
                snackbarHostState.showSnackbar("Account created successfully")
            }
        } else if (message.isNotEmpty() && message != "Loading...") {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create User Account", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
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
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp),
                colorFilter = ColorFilter.tint(busMateBlue)
            )

            Spacer(Modifier.height(16.dp))

            Text("Register New User", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Assign a role and unique school ID", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(32.dp))

            // ROLE SELECTOR
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = if (isRoleSelected) selectedRole else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("User Role") },
                    placeholder = { Text("Select Role") },
                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = busMateBlue) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(expanded, { expanded = false }) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                selectedRole = role
                                expanded = false
                            }
                        )
                    }
                }
            }

            // USER ID FIELD
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("School ID or User ID") },
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = busMateBlue) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (selectedRole == "Select Role" || userId.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please select a role and enter User ID") }
                    } else {
                        viewModel.createAccountWithMinimalData(selectedRole, userId)
                    }
                },
                enabled = message != "Loading...",
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue)
            ) {
                if (message == "Loading...") {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}