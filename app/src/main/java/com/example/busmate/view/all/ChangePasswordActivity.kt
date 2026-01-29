package com.example.busmate.view.all

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.PrimaryBlue
import com.example.busmate.viewmodel.UserViewModel
import kotlinx.coroutines.delay

class ChangePasswordScreen : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeChanged by remember { mutableStateOf(sharedPrefs.getInt("dark_mode_pref", 0)) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeChanged = sharedPrefs.getInt("dark_mode_pref", 0)
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeChanged) {
                com.example.busmate.ui.theme.BusMateTheme {
                    val repo = UserRepositoryImpl()
                    val viewModel = UserViewModel(repo)
                    ChangePasswordUI(viewModel)
                }
            }
        }
    }
}

@Composable
fun ChangePasswordUI(viewModel: UserViewModel) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val message by viewModel.message.collectAsState()
    val activity = context as? Activity
    val isLoading = message == "Loading..."

    // UI Validations
    val isLengthValid = newPass.length >= 8
    val hasUppercase = newPass.any { it.isUpperCase() }
    val hasLowercase = newPass.any { it.isLowerCase() }
    val hasSpecial = newPass.any { !it.isLetterOrDigit() }
    val hasNumber = newPass.any { it.isDigit() }
    val isUIValidationComplete = isLengthValid && hasUppercase && hasLowercase &&
            hasSpecial && hasNumber && oldPass.isNotEmpty() &&
            newPass.isNotEmpty() && confirmPass.isNotEmpty()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message.isNotBlank() && message != "Loading...") {
            snackbarHostState.showSnackbar(message)
            if (message == "Password successfully changed!") {
                activity?.finish()
            }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (message.contains("successfully", ignoreCase = true))
                        Color(0xFF4CAF50) else Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ---------- TOP BLUE SECTION (Exact same as Reset) ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.40f)
                    .background(BusMateBlue)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.loginscreenphoto),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.22f
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Change\nPassword",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ---------- MODERN CARD (Exact same gradient/elevation) ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-40).dp), // Slightly adjusted for longer content
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White, Color(0xFFF8F9FE))
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // OLD PASSWORD
                        PasswordInputField(
                            value = oldPass,
                            onValueChange = { oldPass = it },
                            label = "Old Password",
                            isVisible = showOld,
                            onToggleVisibility = { showOld = !showOld }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // NEW PASSWORD
                        PasswordInputField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = "New Password",
                            isVisible = showNew,
                            onToggleVisibility = { showNew = !showNew }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // REQUIREMENTS LIST
                        PasswordIndicators(password = newPass)

                        Spacer(modifier = Modifier.height(16.dp))

                        // CONFIRM PASSWORD
                        PasswordInputField(
                            value = confirmPass,
                            onValueChange = { confirmPass = it },
                            label = "Confirm New Password",
                            isVisible = showConfirm,
                            onToggleVisibility = { showConfirm = !showConfirm }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // BUTTON
                        Button(
                            onClick = { viewModel.changePassword(oldPass, newPass, confirmPass) },
                            enabled = isUIValidationComplete && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Update Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Forgot Your Password?",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                context.startActivity(Intent(context, ResetPasswordActivity::class.java))
                                activity?.finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.Medium) },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    painter = painterResource(
                        if (isVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            focusedLabelColor = PrimaryBlue,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordIndicators(password: String) {
    val requirements = listOf(
        "Minimum 8 characters" to (password.length >= 8),
        "One uppercase character" to password.any { it.isUpperCase() },
        "One lowercase character" to password.any { it.isLowerCase() },
        "One special character" to password.any { !it.isLetterOrDigit() },
        "One number" to password.any { it.isDigit() }
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        requirements.forEach { (text, passed) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Icon(
                    painter = painterResource(R.drawable.baseline_check_circle_24),
                    contentDescription = null,
                    tint = if (passed) Color(0xFF4CAF50) else Color(0xFFE57373),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, color = if (passed) Color.DarkGray else Color.Red, fontSize = 12.sp)
            }
        }
    }
}