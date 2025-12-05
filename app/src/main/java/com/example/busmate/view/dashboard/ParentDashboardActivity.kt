package com.example.busmate.view.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.busmate.R
import com.example.busmate.data.SupportRepositoryImpl
import com.example.busmate.ui.theme.BackgroundLightGray
import com.example.busmate.viewmodel.SupportViewModel


class ParentDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentDashboardScreen()
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ParentDashboardScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

//    val userModel = activity?.intent?.getParcelableExtra<UserModel>("model")
//    val parentName = userModel?.firstName ?: "Parent"
    val supportViewModel= SupportViewModel(repository = SupportRepositoryImpl())

    data class NavItem(val label: String, val icon: Int)

    var selectedItem by remember { mutableStateOf(0) }

    val navList = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Support", R.drawable.baseline_support_24),
        NavItem("Location", R.drawable.baseline_location_on_24),
        NavItem("Setting", R.drawable.baseline_settings_24),
    )


    Scaffold(
        containerColor = BackgroundLightGray,

        // ---------------- TOP BAR FIXED ----------------
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
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
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },

        // ---------------- BOTTOM NAVIGATION ----------------
        bottomBar = {
            NavigationBar {
                navList.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
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
                2 -> LocationScreen()
                3 -> SettingScreen()
                else -> HomeScreen()
            }
        }
    }
}