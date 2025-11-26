package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class AdminChangePasswordActivity : ComponentActivity() {
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Title
            Text(
                text = "Change Password",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Old Password
            Text("Old Password", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = oldPass,
                onValueChange = { oldPass = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("********") },
                trailingIcon = {
                    IconButton(onClick = { showOld = !showOld }) {
                        Icon(
                            painter = painterResource(
                                if (showOld) com.example.busmate.R.drawable.baseline_visibility_off_24 else com.example.busmate.R.drawable.baseline_visibility_24
                            ),
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF007BFF),
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // New Password
            Text("New Password", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Enter new password") },
                trailingIcon = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(
                            painter = painterResource(
                                if (showNew) com.example.busmate.R.drawable.baseline_visibility_off_24 else com.example.busmate.R.drawable.baseline_visibility_24
                            ),
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF007BFF),
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
//
            // Dynamic Password Requirement Indicators
//            PasswordIndicators(password = newPass)

            Spacer(modifier = Modifier.height(25.dp))

            // Confirm Password
            Text("Confirm New Password", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Re-enter new password") },
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            painter = painterResource(
                                if (showConfirm) com.example.busmate.R.drawable.baseline_visibility_off_24 else com.example.busmate.R.drawable.baseline_visibility_24
                            ),
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF007BFF),
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007BFF)
                )
            ) {
                Text(text = "Change Password", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Forgot Password?",
                color = Color(0xFF007BFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
//
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Composable
fun PasswordIndicators(password: String) {
    val requirements = listOf(
        "Minimum 12 characters" to { it: String -> it.length >= 12 },
        "One uppercase character" to { it: String -> it.any { c -> c.isUpperCase() } },
        "One lowercase character" to { it: String -> it.any { c -> c.isLowerCase() } },
        "One special character" to { it: String -> it.any { c -> !c.isLetterOrDigit() } },
        "One number" to { it: String -> it.any { c -> c.isDigit() } }
    )

//    Column {
//        requirements.forEach { (text, rule) ->
//            Requirement(text = text, passed = rule(password))
//        }
//    }
}
//
//@Composable
//fun Requirement(text: String, passed: Boolean) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.Start,
//        modifier = Modifier.padding(vertical = 4.dp)
//    ) {
//        Icon(
//            painter = painterResource(R.drawable.baseline_check_circle_24),
//            contentDescription = null,
//            tint = if (passed) Color.Gray else Color.Red,
//            modifier = Modifier
//                .padding(end = 8.dp)
//                .height(16.dp)
//        )
//        Text(
//            text = text,
//            color = if (passed) Color.Gray else Color.Red,
//            fontSize = 14.sp
//        )
//    }
//}
//
//@Preview
//@Composable
//fun PreviewAdminChangeUI() {
//    AdminChangeUI()
//}
//
//
//
//
