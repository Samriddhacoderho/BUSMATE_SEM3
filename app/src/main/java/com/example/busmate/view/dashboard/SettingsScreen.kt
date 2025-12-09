package com.example.busmate.view.dashboard

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.busmate.view.SettingsMenuItem
import android.content.Intent
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.example.busmate.view.LoginScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingScreen() {
    val context = LocalContext.current
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isDarkModeEnabled by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SettingsTopBarScreen(onBackClick = { /* TODO */ })
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            item {
                SettingsMenuItemScreen(
                    title = "Notification",
                    imageVector = Icons.Filled.Notifications,
                    onClick = { isNotificationEnabled = !isNotificationEnabled }
                ) {
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { isNotificationEnabled = it }
                    )
                }

                SettingsMenuItem(
                    title = "Dark Mode",
                    imageVector = Icons.Filled.DarkMode,
                    onClick = { isDarkModeEnabled = !isDarkModeEnabled }
                ) {
                    Switch(
                        checked = isDarkModeEnabled,
                        onCheckedChange = { isDarkModeEnabled = it }
                    )
                }

                SettingsMenuItem(
                    title = "Rate App",
                    imageVector = Icons.Filled.Star,
                    onClick = { /* TODO */ }
                )

                SettingsMenuItem(
                    title = "Share App",
                    imageVector = Icons.Filled.Share,
                    onClick = { /* TODO */ }
                )

                SettingsMenuItem(
                    title = "Privacy Policy",
                    imageVector = Icons.Filled.Lock,
                    onClick = { /* TODO */ }
                )

                SettingsMenuItem(
                    title = "Terms and Conditions",
                    imageVector = Icons.Filled.Description,
                    onClick = { /* TODO */ }
                )

                SettingsMenuItem(
                    title = "Cookies Policy",
                    imageVector = Icons.Filled.Cookie,
                    onClick = { /* TODO */ }
                )

                SettingsMenuItem(
                    title = "Contact",
                    imageVector = Icons.Filled.Mail,
                    onClick = { /* TODO */ }
                )

                SettingsMenuItem(
                    title = "Feedback",
                    imageVector = Icons.Filled.Feedback,
                    onClick = { /* TODO */ }
                )

                SettingsMenuItem(
                    title = "Logout",
                    imageVector = Icons.Filled.Logout,
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),

            modifier = Modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight(),

            title = {
                Text(
                    text = "Log out of your account?",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Divider()   // Divider under title
            },

            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        // Cancel button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showLogoutDialog = false }
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Cancel",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Vertical divider
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                        )

                        // Log out button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    FirebaseAuth.getInstance().signOut()
                                    val intent = Intent(context, LoginScreen::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    showLogoutDialog = false
                                }
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Log out",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            },

            dismissButton = {}
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBarScreen(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun SettingsMenuItemScreen(
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
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        trailingContent()
    }
}
