package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // New Import
import androidx.compose.foundation.interaction.collectIsFocusedAsState // New Import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.view.ui.theme.BUSMATETheme

// --- Custom Colors ---
private val PrimaryBlue = Color(0xFF2567E8)
private val DarkBlueBackground = Color(0xFF2854D8) // A slightly darker shade for the top background
private val PlaceholderBusColor = Color(0xFFFFB74D) // The orange/yellow color for the bus logo

class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BUSMATETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreenUI(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreenUI(modifier: Modifier = Modifier) {
    // State variables for input fields and checkbox (required for TextField components)
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    // InteractionSource to track focus state of the password field
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isPasswordFocused by passwordInteractionSource.collectIsFocusedAsState()

    // Main screen structure uses Box for layering the blue background and the white card
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Top Blue Background Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.40f)
                .background(DarkBlueBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Top padding

            // Bus Logo
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_search),
                contentDescription = "Bus Mate Logo",
                colorFilter = ColorFilter.tint(PlaceholderBusColor),
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Bus Mate Title
            Text(
                text = "Bus Mate",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Log in title
            Text(
                text = "Log in to your\nAccount",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter your ID and password to log in",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }





    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreenUI()
}