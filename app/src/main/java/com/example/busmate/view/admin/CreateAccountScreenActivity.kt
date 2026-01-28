package com.example.busmate.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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
            // 🌙 Dark mode observer
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeChanged by remember {
                mutableStateOf(sharedPrefs.getInt("dark_mode_pref", 0))
            }

            DisposableEffect(Unit) {
                val listener =
                    android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
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
                BusMateTheme {
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
    val isRoleSelected = selectedRole != "Select Role"
    val cardAlpha by animateFloatAsState(targetValue = 1f)

    // 🚦 Navigation + snackbar logic
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // 🔵 Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2567E8), Color(0xFF1D4ED8))
                        )
                    )
                    .shadow(6.dp)
                    .padding(vertical = 28.dp, horizontal = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Admin",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Create User ID",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // 🧾 Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer { alpha = cardAlpha },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(100.dp),
                        colorFilter = ColorFilter.tint(Color(0xFF2567E8))
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Register Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Choose a role and assign an ID",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(24.dp))

                    // 🔽 Role Dropdown
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
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, null)
                            },
                            trailingIcon = {
                                Icon(Icons.Default.KeyboardArrowDown, null)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
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

                    Spacer(Modifier.height(16.dp))

                    // 🆔 User ID
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("School ID or User ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(28.dp))

                    // 🚀 Button
                    Button(
                        onClick = {
                            if (selectedRole == "Select Role" || userId.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Please select a role and enter User ID"
                                    )
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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2567E8)
                        )
                    ) {
                        if (message == "Loading...") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Creating...")
                        } else {
                            Text(
                                text = "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }
        }
    }
}
