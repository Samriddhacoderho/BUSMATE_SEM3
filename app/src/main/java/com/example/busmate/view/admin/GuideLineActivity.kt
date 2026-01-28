package com.example.busmate.view.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.GuideLinesImpl
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.viewmodel.GuideLineViewModel

class GuideLineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typeOfUser = intent.getStringExtra("typeOfUser")
        Log.d("BUSMATE_DEBUG", "GuideLineActivity: $typeOfUser")

        enableEdgeToEdge()
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current

            // Standardizing the theme refresh logic to match your other activities
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeUpdateTrigger by remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeUpdateTrigger++
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeUpdateTrigger) {
                BusMateTheme(darkTheme = isDarkMode()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            when (typeOfUser) {
                                "Admin" -> AdminGuidelineScreen()
                                "Driver", "Parent" -> DriverGuidelineScreen()
                                else -> ErrorScreen(typeOfUser)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(type: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Unknown user type: $type",
            color = Color.Red,
            fontSize = 16.sp
        )
    }
}

/* ============================================================
   ADMIN UI
   ============================================================ */
@Composable
fun AdminGuidelineScreen() {
    // FORCE BLUE COLOR
    val busMateBlue = Color(0xFF2567E8)

    val viewModel = remember { GuideLineViewModel(GuideLinesImpl()) }
    val guidelines by viewModel.guidelines.collectAsState()
    val message by viewModel.message.collectAsState()
    var textState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    LaunchedEffect(guidelines) {
        if (guidelines.isNotEmpty()) textState = guidelines
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // TOP BLUE HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(busMateBlue), // Applied Blue
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Admin Guidelines",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manage safety instructions",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        // CARD SECTION
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-32).dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text("Write Safety Guidelines") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    minLines = 10,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = busMateBlue,
                        focusedLabelColor = busMateBlue
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.postGuidelines(textState) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = busMateBlue, // Applied Blue
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (message == "Posting...") "Saving..." else "Update Guidelines",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (message.isNotEmpty() && message != "Posting...") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/* ============================================================
   DRIVER / PARENT UI
   ============================================================ */
@Composable
fun DriverGuidelineScreen() {
    val busMateBlue = Color(0xFF2567E8)
    val viewModel = remember { GuideLineViewModel(GuideLinesImpl()) }
    val guidelines by viewModel.guidelines.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    Box(modifier = Modifier.fillMaxSize()) {
        // TOP BLUE HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(busMateBlue), // Applied Blue
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Safety Guidelines",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please follow these instructions",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        // CONTENT CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-32).dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = guidelines.ifEmpty {
                        "Waiting for admin to post guidelines..."
                    },
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}