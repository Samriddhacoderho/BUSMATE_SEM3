package com.example.busmate.view.all

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.ui.theme.PrimaryBlue
import com.example.busmate.viewmodel.UserViewModel


class ChangePasswordScreen : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
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
                    val repo = UserRepositoryImpl()
                    val viewModel = UserViewModel(repo)
                    ChangePasswordScreen(viewModel = viewModel)
                }
            }
        }
    }
}
@Composable
fun ChangePasswordScreen(viewModel: UserViewModel) {

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val message by viewModel.message.collectAsState()
    val activity=context as Activity

    val isLengthValid = newPass.length >= 8
    val hasUppercase = newPass.any { it.isUpperCase() }
    val hasLowercase = newPass.any { it.isLowerCase() }
    val hasSpecial = newPass.any { !it.isLetterOrDigit() }
    val hasNumber = newPass.any { it.isDigit() }
    val passwordsMatch = newPass == confirmPass && newPass.isNotEmpty()

    // The button will only be clickable if all these are true
    val isUIValidationComplete = isLengthValid && hasUppercase && hasLowercase &&
            hasSpecial && hasNumber && oldPass.isNotEmpty() &&
            newPass.isNotEmpty() && confirmPass.isNotEmpty()
    val snackbarHostState = remember { SnackbarHostState() }

    fun handleChangePassword() {
        viewModel.changePassword(oldPass, newPass, confirmPass)
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading...") {
            snackbarHostState.showSnackbar(message)
            // Add this line below to reset the message in the ViewModel
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    // Direct state check inside the callback
                    containerColor = if (message == "Password successfully changed!") Color(0xFF4CAF50) else Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Respect scaffold padding
        ) {

            //  BLUE TOP SECTION (same as login screen)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .background(BusMateBlue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Spacer(modifier = Modifier.height(45.dp))

                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo",
                    colorFilter = ColorFilter.tint(PlaceholderBusColor),
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Change Password",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = {
                    // This triggers the standard activity back navigation
                    (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                },
                modifier = Modifier
                    .padding(top = 40.dp, start = 12.dp) // Adjust padding to avoid status bar
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_arrow_back_24),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            // ⚪ WHITE CARD THAT OVERLAPS (same shape as login)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-30).dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                ) {

                    // 🔹 OLD PASSWORD
                    Text("Old Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        placeholder = { Text("********") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showOld = !showOld }) {
                                Icon(
                                    painter = painterResource(
                                        if (showOld) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ),
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔹 NEW PASSWORD
                    Text("New Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        placeholder = { Text("Enter new password") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showNew = !showNew }) {
                                Icon(
                                    painter = painterResource(
                                        if (showNew) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ),
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔸 Password Requirements
                    PasswordIndicators(password = newPass)

                    Spacer(modifier = Modifier.height(25.dp))

                    // 🔹 CONFIRM PASSWORD
                    Text("Confirm New Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        placeholder = { Text("Re-enter new password") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(
                                    painter = painterResource(
                                        if (showConfirm) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ),
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(35.dp))


                    Button(
                        onClick = { handleChangePassword() },
                        enabled = isUIValidationComplete, // Button stays disabled until UI rules pass
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            disabledContainerColor = Color.Gray // Visually grey out the button
                        )
                    ) {
                        Text(
                            text = "Change Password",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            // Optional: make text lighter when disabled
                            color = if (isUIValidationComplete) Color.White else Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Forgot Password
                    Text(
                        text = "Forgot Your Password?",
                        color = PrimaryBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().clickable(onClick = {
                            val intent= Intent(context, ResetPasswordActivity::class.java)
                            context.startActivity(intent)
                            activity.finish()
                        })
                    )

                    Spacer(modifier = Modifier.height(25.dp))
                }
            }
        }
    }

}
@Composable
fun Requirement(text: String, passed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_check_circle_24),
            contentDescription = null,
            tint = if (passed) Color.Blue else Color.Red,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(16.dp)
        )
        Text(
            text = text,
            color = if (passed) Color.Gray else Color.Red,
            fontSize = 14.sp
        )
    }
}
@Composable
fun PasswordIndicators(password: String) {
    val requirements = listOf(
        "Minimum 8 characters" to { it: String -> it.length >= 8 },
        "One uppercase character" to { it: String -> it.any(Char::isUpperCase) },
        "One lowercase character" to { it: String -> it.any(Char::isLowerCase) },
        "One special character" to { it: String -> it.any { c -> !c.isLetterOrDigit() } },
        "One number" to { it: String -> it.any(Char::isDigit) }
    )

    Column {
        requirements.forEach { (text, rule) ->
            Requirement(text = text, passed = rule(password))
        }
    }
}
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PreviewAdminChangeUI() {
    ChangePasswordScreen(viewModel = UserViewModel(repository = UserRepositoryImpl()))

}