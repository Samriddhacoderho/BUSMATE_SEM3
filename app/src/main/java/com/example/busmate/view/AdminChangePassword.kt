package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.ui.theme.PrimaryBlue


class AdminChangePassword : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminChangeUI()

        }
    }
}
@Composable
fun AdminChangeUI() {

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔵 BLUE TOP SECTION (same as login screen)
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
                painter = painterResource(com.example.busmate.R.drawable.logo),
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

        // ⚪ WHITE CARD THAT OVERLAPS (same shape as login)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-30).dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
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
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
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
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(35.dp))

                // 🔵 BUTTON (same as login page)
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Change Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Forgot Password
                Text(
                    text = "Forgot Your Password?",
                    color = PrimaryBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(25.dp))
            }
        }
    }
}

@Composable
fun PasswordIndicators(password: String) {
    val requirements = listOf(
        "Minimum 12 characters" to { it: String -> it.length >= 12 },
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

@Composable
fun Requirement(text: String, passed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_check_circle_24),
            contentDescription = null,
            tint = if (passed) Color.Gray else Color.Red,
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
