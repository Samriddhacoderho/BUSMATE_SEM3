package com.example.busmate.view.dashboard

import android.R
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.view.all.ChangePasswordScreen
import com.example.busmate.view.all.EditProfileActivity
import com.example.busmate.view.auth.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.busmate.view.all.DarkMoodSettingActivity
import coil3.compose.AsyncImage
import com.example.busmate.view.all.HelpAndSupportActivity


// --- Colors and Theme (Simplified placeholders based on the image's black and white style) ---
// Using MaterialTheme.colorScheme for better practices, but keeping a simple palette.
val PrimaryBlack = Color(0xFF1E1E1E)
val BackgroundWhite = Color(0xFFFFFFFF)
val ButtonGray = Color(0xFFEEEEEE)
val TextGray = Color(0xFF6A6A6A)
val IconGray = Color(0xFF4A4A4A)

@Composable
fun ProfileEditScreen(userRepository: UserRepositoryImpl = UserRepositoryImpl()) {
    // This state would typically be managed by a ViewModel

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
//    var isDarkModeEnabled by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                userRepository.getCurrentUserProfile { success, _, user ->
                    if (success && user != null) {
                        firstName = user.firstName ?: ""
                        lastName = user.lastName ?: ""
                        profileImageUrl = user.profileImage
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = BackgroundWhite,
//        bottomBar = { AppBottomNavigationBar() } // Assuming a bottom navigation bar is present
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundWhite)
                .padding(horizontal = 20.dp)
        ) {
            item {
                // 2. Profile Info Section
                ProfileInfoCard(
                    fullName = firstName,
                    lastName = lastName,
                    profileImageUrl = profileImageUrl,
                    onEditClick = {
                        val intent = Intent(context, EditProfileActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))
                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    onClick = { /* Handle click */ }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    label = "Change Password",
                    onClick = { val intent = Intent(context, ChangePasswordScreen::class.java)
                        context.startActivity(intent) }
                )
                ProfileMenuItem(
                    icon = Icons.Default.DarkMode,
                    label = "Dark Mode",
                    onClick = {
                        val intent = Intent(context, DarkMoodSettingActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                // 4. Footer Menu Items
                ProfileMenuItem(
                    icon = Icons.Default.HelpOutline,
                    label = "Help & Support",
                    showArrow = true,
                    onClick = {
                        val intent = Intent(context, HelpAndSupportActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "Log out",
                    showArrow = false,
                    onClick = { showLogoutDialog = true }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
    if (showLogoutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            ),
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
                Divider()
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        // Cancel
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

                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                        )

                        // Log out (SAME LOGIC AS SETTINGS)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val sharedPreferences =
                                        context.getSharedPreferences("User", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().clear().apply()

                                    FirebaseAuth.getInstance().signOut()

                                    val intent =
                                        Intent(context, LoginScreen::class.java)
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
@Composable
fun ProfileInfoCard(
    fullName: String,
    lastName: String,
    profileImageUrl: String?,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Section
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // FIX: Use an if-else block so only ONE image is drawn
            if (!profileImageUrl.isNullOrEmpty()) {
                // Use AsyncImage directly after importing it
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                // Default placeholder only if URL is null
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(ButtonGray),
                    tint = Color.LightGray
                )
            }

            // Edit badge (Camera/Edit Icon)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile Picture",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Name Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = fullName,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = PrimaryBlack
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = lastName,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = PrimaryBlack
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onEditClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2854D8),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp)
        ) {
            Text("Edit Profile", fontSize = 14.sp,fontWeight = FontWeight.SemiBold)
        }
    }
}
//testing the profile screen
@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, showArrow: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IconGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = PrimaryBlack
            )
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go to $label",
                tint = IconGray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
//testing show image