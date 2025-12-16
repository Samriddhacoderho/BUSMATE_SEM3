package com.example.busmate.view.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.busmate.R
import com.example.busmate.data.SupportRepositoryImpl
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.view.EditActivity
import com.example.busmate.view.ProfileScreen
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
    val supportViewModel = SupportViewModel(repository = SupportRepositoryImpl())

    /* ---------- NAV ITEM MODEL ---------- */
    data class NavItem(
        val label: String,
        val icon: ImageVector
    )

    var selectedItem by remember { mutableStateOf(0) }

    /* ---------- NAV ITEMS ---------- */
    val navList = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Support", Icons.Filled.SupportAgent),
        NavItem("Location", Icons.Filled.LocationOn),
        NavItem("Profile", Icons.Filled.Person)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        /* ---------- TOP BAR ---------- */
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    colorFilter = if (isDarkModeEnabled)
                        ColorFilter.tint(PlaceholderBusColor)
                    else null,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.End) {
//                    IconButton(onClick = {
//                        selectedItem = 3   // Profile tab
//                    }) {
//                    Icon(
//                            imageVector = Icons.Filled.Person,
//                            contentDescription = "Profile",
//                            tint = MaterialTheme.colorScheme.onSurface
//                        )
//                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },

        /* ---------- BOTTOM NAV ---------- */
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navList.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index},
                        icon = {
                            Icon(
                                imageVector = item.icon,
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

        /* ---------- SCREEN SWITCHER ---------- */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> SupportScreen(viewModel = supportViewModel)
                2 -> LiveLocationScreen()
                3 -> ProfileEditScreen()
            }
        }
    }
}
