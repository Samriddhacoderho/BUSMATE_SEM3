package com.example.busmate.view.all

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// --- Custom Colors for Dark Theme UI Matching the Image ---
val DarkBackground = Color(0xFF1C1C1E) // Deep dark gray/black
val PrimaryText = Color.White
val SecondaryText = Color(0xFFAFAFAF) // Lighter gray for secondary text
val SelectedBlue = Color(0xFF4285F4) // Google Blue for selected radio button

// --- Data Class for Dark Mode Options ---
enum class ThemeMode { ON, OFF, SYSTEM }

class DarkMoodSettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DarkModeSettingsScreen()

        }
    }
}
@Composable
fun DarkModeSettingsScreen() {
    // State to hold the currently selected theme mode
    var selectedMode by remember { mutableStateOf(ThemeMode.ON) }

    Scaffold(
        topBar = {
            DarkModeTopBar()
        },
        containerColor = Color.White// Set the entire Scaffold background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Replicating the structure shown in the image with the three options
            DarkModeOptionItem(
                title = "On",
                description = null,
                mode = ThemeMode.ON,
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it }
            )
            DarkModeOptionItem(
                title = "Off",
                description = null,
                mode = ThemeMode.OFF,
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it }
            )
            DarkModeOptionItem(
                title = "System",
                description = "We'll adjust your appearance based on your device's system settings.",
                mode = ThemeMode.SYSTEM,
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it }
            )

            // The image shows two sections, repeating the options.
            // We will replicate the visual structure of the second, simpler group,
            // which appears to only have "Off" and "System" or similar options without a header.
            Spacer(modifier = Modifier.height(32.dp))

            // Second Visual Group (Looks like a placeholder or a bug in the original screenshot,
            // but we'll replicate the visible list items.)

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkModeTopBar() {
    val context = LocalContext.current
    TopAppBar(
        title = {
            // Center the title visually
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Dark Mode",
                    fontSize = 18.sp,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    // Offset the title back slightly because of the back button padding
                    modifier = Modifier.padding(end = 40.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { (context as Activity).finish()}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White // Match the screen background color
        )
    )
}

@Composable
fun DarkModeOptionItem(
    title: String,
    description: String?,
    mode: ThemeMode,
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val isSelected = mode == selectedMode

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Make the entire row clickable to select the option
            .clickable { onModeSelected(mode) }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color =Color.Black,
                style = MaterialTheme.typography.bodyLarge
            )
            description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color =Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Radio Button
        RadioButton(
            selected = isSelected,
            onClick = { onModeSelected(mode) },
            colors = RadioButtonDefaults.colors(
                selectedColor = SelectedBlue,
                unselectedColor = Color.Black,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DarkModeScreenPreview() {
    Column(modifier = Modifier.background(DarkBackground).fillMaxSize()) {
        DarkModeSettingsScreen()
    }
}