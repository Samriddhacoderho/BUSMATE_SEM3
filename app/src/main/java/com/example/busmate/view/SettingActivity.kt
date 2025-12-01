package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.* // Import all filled icons for the menu items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector // Needed for Icon with ImageVector
import androidx.compose.ui.unit.dp

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettingsScreen()

        }
    }
}
@Composable
fun SettingsScreen() {
    // State to manage the Notification Switch position
    var isNotificationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            SettingsTopBar(onBackClick = { /* TODO: Implement navigation back */ })
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            // 🔔 Notification Toggle Item
            SettingsMenuItem(
                title = "Notification",
                imageVector = Icons.Filled.Notifications,
                onClick = { isNotificationEnabled = !isNotificationEnabled } // Clicking the row toggles the switch
            ) {
                // Trailing content for Notification item is a Switch
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = { isNotificationEnabled = it }
                )
            }


            SettingsMenuItem(
                title = "Dark Mode",
                imageVector = Icons.Filled.DarkMode,
                onClick = { /* TODO: Implement Dark Mode toggle/navigation */ }
            )


            SettingsMenuItem(
                title = "Rate App",
                imageVector = Icons.Filled.Star,
                onClick = { /* TODO: Implement launching rating prompt */ }
            )


            SettingsMenuItem(
                title = "Share App",
                imageVector = Icons.Filled.Share,
                onClick = { /* TODO: Implement share intent */ }
            )


            SettingsMenuItem(
                title = "Privacy Policy",
                imageVector = Icons.Filled.Lock,
                onClick = { /* TODO: Implement navigation to policy screen */ }
            )



            SettingsMenuItem(
                title = "Terms and Conditions",
                imageVector = Icons.Filled.Description,
                onClick = { /* TODO: Implement navigation to terms screen */ }
            )


            SettingsMenuItem(
                title = "Cookies Policy",
                imageVector = Icons.Filled.Cookie,
                onClick = { /* TODO: Implement navigation to cookies policy screen */ }
            )


            SettingsMenuItem(
                title = "Contact",
                imageVector = Icons.Filled.Mail,
                onClick = { /* TODO: Implement navigation to contact screen */ }
            )


            SettingsMenuItem(
                title = "Feedback",
                imageVector = Icons.Filled.Feedback,
                onClick = { /* TODO: Implement navigation to feedback screen */ }
            )


            SettingsMenuItem(
                title = "Logout",
                imageVector = Icons.Filled.Logout,
                onClick = { /* TODO: Implement logout logic */ }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text("Settings")
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            // Use surface for a clean, white background as shown in the image
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun SettingsMenuItem(
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = imageVector,
            contentDescription = null, // decorative
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))


        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // Pushes trailing content to the end
        )


        trailingContent()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}


