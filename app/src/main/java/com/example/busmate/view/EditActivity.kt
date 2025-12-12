package com.example.busmate.view

import android.app.Activity
import android.content.Intent
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
import com.example.busmate.view.ui.theme.BUSMATETheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colors and Theme (Simplified placeholders based on the image's black and white style) ---
// Using MaterialTheme.colorScheme for better practices, but keeping a simple palette.
val PrimaryBlack = Color(0xFF1E1E1E)
val BackgroundWhite = Color(0xFFFFFFFF)
val ButtonGray = Color(0xFFEEEEEE)
val TextGray = Color(0xFF6A6A6A)
val IconGray = Color(0xFF4A4A4A)

class EditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDarkModeEnabled = intent.getBooleanExtra("isDarkModeEnabled", false)
        enableEdgeToEdge()
        setContent {
            ProfileScreen()

        }
    }
}
@Composable
fun ProfileScreen() {
    // This state would typically be managed by a ViewModel
    var fullName by remember { mutableStateOf("Parves Ahamad") }
    var username by remember { mutableStateOf("@parvesahamad") }
    val context = LocalContext.current

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
                // 1. App Bar
                ProfileScreenHeader(title = "Profile")

                // 2. Profile Info Section
                ProfileInfoCard(
                    fullName = fullName,
                    username = username,
                    onEditClick = {   val intent = Intent(context, EditProfileActivity::class.java)
                        context.startActivity(intent) }
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 3. Menu Items
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = { /* Handle click */ }
                )
//                ProfileMenuItem(
//                    icon = Icons.Default.ShoppingBag,
//                    label = "My Orders",
//                    onClick = { /* Handle click */ }
//                )
                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    onClick = { /* Handle click */ }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    label = "Change Password",
                    onClick = { /* Handle click */ }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 5.dp),
                    color = Color(0xFFF5F5F5) // Very light divider
                )

                // 4. Footer Menu Items
                ProfileMenuItem(
                    icon = Icons.Default.HelpOutline,
                    label = "Help & Support",
                    showArrow = true,
                    onClick = { /* Handle click */ }
                )
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Logout, // Using Logout icon
                    label = "Log out",
                    showArrow = false,
                    onClick = { /* Handle click */ }
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ProfileScreenHeader(title: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {  (context as? Activity)?.onBackPressed() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryBlack,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = PrimaryBlack
        )
    }
}

@Composable
fun ProfileInfoCard(fullName: String, username: String, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder for user avatar with an edit badge
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle, // Placeholder Image
                contentDescription = "User Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(ButtonGray),
                tint = Color.LightGray
            )
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
        Text(
            text = fullName,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = PrimaryBlack
        )
        Text(
            text = username,
            fontSize = 14.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEditClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlack,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp)
        ) {
            Text("Edit Profile", fontSize = 14.sp)
        }
    }
}

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
@Preview(showBackground = true, name = "Profile Screen Preview")
@Composable
fun PreviewProfileScreen() {
    Surface(color = BackgroundWhite) {
        ProfileScreen()
    }
}


