package com.example.busmate.view.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.busmate.R
import com.example.busmate.data.SupportRepositoryImpl
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.viewmodel.SupportViewModel

class ParentDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkModeEnabled by remember { mutableStateOf(false) }

            BusMateTheme(darkTheme = isDarkModeEnabled) {
                ParentDashboardScreen(
                    isDarkModeEnabled = isDarkModeEnabled,
                    onThemeChange = { isDarkModeEnabled = it }
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ParentDashboardScreen(
    isDarkModeEnabled: Boolean,
    onThemeChange: (Boolean) -> Unit
) {

    val context = LocalContext.current
    val activity = context as? Activity
    val supportViewModel = SupportViewModel(repository = SupportRepositoryImpl())

    data class NavItem(val label: String, val icon: Int)

    var selectedItem by remember { mutableStateOf(0) }

    val navList = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Support", R.drawable.baseline_support_24),
        NavItem("Location", R.drawable.baseline_location_on_24),
        NavItem("Setting", R.drawable.baseline_settings_24),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,   // FIX ✔

        // ---------------- TOP BAR FIXED ----------------
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)  // FIX ✔
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    colorFilter = if (isDarkModeEnabled) ColorFilter.tint(PlaceholderBusColor) else null,
                    modifier = Modifier.weight(0.5f)
                )

                Row(
                    Modifier.weight(0.5f),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface // FIX ✔
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error      // FIX ✔
                        )
                    }
                }
            }
        },

        // ---------------- BOTTOM NAVIGATION ----------------
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface   // FIX ✔
            ) {
                navList.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label,
                                tint = if (selectedItem == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                color = if (selectedItem == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        // ---------------- SCREEN SWITCHER ----------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> SupportScreen(viewModel = supportViewModel)
                2 -> LiveLocationScreen()
                3 -> SettingScreen(
                    isDarkModeEnabled = isDarkModeEnabled,
                    onThemeChange = onThemeChange
                )
                else -> HomeScreen()
            }
        }
    }
}
