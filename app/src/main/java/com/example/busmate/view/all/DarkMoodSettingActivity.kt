package com.example.busmate.view.all

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.ui.theme.BusMateTheme

// Constants for SharedPreferences
const val PREFS_NAME = "settings"
const val THEME_KEY = "dark_mode_pref"

// Mapping logic: 0 = System, 1 = OFF (Light), 2 = ON (Dark)
class DarkMoodSettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // We wrap this screen in the theme too so it looks correct
            BusMateTheme {
                DarkModeSettingsScreen()
            }
        }
    }
}

@Composable
fun DarkModeSettingsScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // Default to 1 (OFF) if no value is saved yet
    var selectedValue by remember { mutableIntStateOf(sharedPrefs.getInt(THEME_KEY, 1)) }

    Scaffold(
        topBar = { DarkModeTopBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Option: OFF (Light Mode)
            DarkModeOptionItem(
                title = "Off",
                description = "Always use light appearance.",
                isSelected = selectedValue == 1,
                onModeSelected = {
                    selectedValue = 1
                    sharedPrefs.edit().putInt(THEME_KEY, 1).apply()
                    (context as? Activity)?.recreate() // Refresh UI immediately
                }
            )

            // Option: ON (Dark Mode)
            DarkModeOptionItem(
                title = "On",
                description = "Always use dark appearance.",
                isSelected = selectedValue == 2,
                onModeSelected = {
                    selectedValue = 2
                    sharedPrefs.edit().putInt(THEME_KEY, 2).apply()
                    (context as? Activity)?.recreate()
                }
            )

            // Option: SYSTEM
            DarkModeOptionItem(
                title = "System",
                description = "We'll adjust your appearance based on your device's system settings.",
                isSelected = selectedValue == 0,
                onModeSelected = {
                    selectedValue = 0
                    sharedPrefs.edit().putInt(THEME_KEY, 0).apply()
                    (context as? Activity)?.recreate()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkModeTopBar() {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Dark Mode",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 40.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { (context as Activity).finish() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun DarkModeOptionItem(
    title: String,
    description: String?,
    isSelected: Boolean,
    onModeSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onModeSelected() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            description?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        RadioButton(
            selected = isSelected,
            onClick = { onModeSelected() },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF4285F4)
            )
        )
    }
}