package com.example.busmate.view.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.GuideLinesImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.viewmodel.GuideLineViewModel

class GuideLineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val typeOfUser = intent.getStringExtra("typeOfUser")
        Log.d("BUSMATE_DEBUG", "GuideLineActivity: $typeOfUser")

        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", MODE_PRIVATE)
            }

            var themeChanged by remember {
                mutableIntStateOf(sharedPrefs.getInt("dark_mode_pref", 0))
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
                BusMateTheme(darkTheme = isDarkMode()) {
                    when (typeOfUser) {
                        "Admin" -> AdminGuidelineScreen(onBackClick = { finish() })
                        "Driver", "Parent" -> DriverGuidelineScreen(onBackClick = { finish() })
                        else -> ErrorScreen(typeOfUser)
                    }
                }
            }
        }
    }
}

/* ============================================================
   ADMIN SCREEN (BusProfile-aligned UI)
   ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGuidelineScreen(onBackClick: () -> Unit) {

    val viewModel = remember { GuideLineViewModel(GuideLinesImpl()) }
    val guidelines by viewModel.guidelines.collectAsState()
    val message by viewModel.message.collectAsState()

    var textState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    LaunchedEffect(guidelines) {
        if (guidelines.isNotEmpty()) textState = guidelines
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guidelines", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BusMateBlue)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* 🔵 HEADER (same as BusProfile) */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(BusMateBlue),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Safety Guidelines",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Create & manage instructions",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))


            /* ⚪ CENTER CARD (bigger + centered) */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-70).dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Edit Guidelines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BusMateBlue
                    )

                    HorizontalDivider(Modifier.padding(vertical = 12.dp))

                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 280.dp),
                        placeholder = { Text("Enter safety guidelines here…") },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BusMateBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            cursorColor = BusMateBlue
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.postGuidelines(textState) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue)
                    ) {
                        Text(
                            text = if (message == "Posting...") "Saving…" else "Save Guidelines",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (message.isNotEmpty() && message != "Posting...") {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

/* ============================================================
   DRIVER / PARENT SCREEN
   ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverGuidelineScreen(onBackClick: () -> Unit) {

    val viewModel = remember { GuideLineViewModel(GuideLinesImpl()) }
    val guidelines by viewModel.guidelines.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Guidelines", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BusMateBlue)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(BusMateBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please Follow Rules",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-70).dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Official Rules",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BusMateBlue
                    )

                    HorizontalDivider(Modifier.padding(vertical = 12.dp))

                    Text(
                        text = guidelines.ifEmpty {
                            "Guidelines will appear once the admin publishes them."
                        },
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
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
            color = Color.Red
        )
    }
}
