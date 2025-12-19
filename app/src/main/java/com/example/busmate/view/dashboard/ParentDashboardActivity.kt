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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.data.SupportRepositoryImpl
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.view.* // Ensure all view activities are accessible
import com.example.busmate.viewmodel.ChildViewModel
import com.example.busmate.viewmodel.SupportViewModel
import com.example.busmate.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ParentDashboardScreen(
    isDarkModeEnabled: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val userViewModel = remember { UserViewModel(repository = UserRepositoryImpl()) }
    val context = LocalContext.current
    val supportViewModel = SupportViewModel(repository = SupportRepositoryImpl())
    val childViewModel = remember {
        ChildViewModel(repository = ChildRepositoryImpl())
    }
    val user by userViewModel.user.collectAsState()

    // Drawer & Navigation State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(0) }

    val children by childViewModel.children.collectAsState()

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            userViewModel.loadUserProfile(it)
            childViewModel.observeChildren(it)
        }
    }

    /* ---------- NAV ITEM MODEL ---------- */
    data class NavItem(
        val label: String,
        val icon: ImageVector
    )

    /* ---------- BOTTOM NAV ITEMS ---------- */
    val navList = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Support", Icons.Filled.SupportAgent),
        NavItem("Location", Icons.Filled.LocationOn),
        NavItem("Profile", Icons.Filled.Person)
    )

    /* ---------- DRAWER LOGIC ---------- */
    val drawerItems = when (user?.typeofUser?.lowercase()) {
        "admin" -> listOf(
            NavItem("Create Account", Icons.Default.PersonAdd),
            NavItem("Add Bus", Icons.Default.DirectionsBus),
            NavItem("View Bus", Icons.Default.DirectionsBus),
            NavItem("View Driver", Icons.Default.Badge),
            NavItem("Deactivate Account", Icons.Default.PersonOff),
            NavItem("Schedules", Icons.Default.Event),
            NavItem("Reports", Icons.Default.Assessment),
            NavItem("Logs", Icons.Default.History),
            NavItem("About Us", Icons.Default.Info)
        )
        "driver" -> listOf(
            NavItem("My Trips", Icons.Default.Route),
            NavItem("About Us", Icons.Default.Info)
        )
        else -> listOf(
            NavItem("About Us", Icons.Default.Info)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(user?.firstName ?: "Loading...", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(user?.typeofUser ?: "", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                // Navigation logic for Admin activities
                                when(item.label) {
                                    "Create Account" -> context.startActivity(Intent(context, CreateAccountScreenActivity::class.java))
                                    "Add Bus" -> context.startActivity(Intent(context, BusScreen::class.java))
                                    "View Bus" -> context.startActivity(Intent(context, BusProfileScreen::class.java))
                                    "View Driver" -> context.startActivity(Intent(context, DriverProfileScreen::class.java))
                                    "Deactivate Account" -> context.startActivity(Intent(context, AdminDeactivatesActivity::class.java))
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,

            /* ---------- UPDATED TOP BAR ---------- */
            topBar = {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Menu Button (Drawer Trigger)
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 2. Centered Logo (2x Size & Cropped)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "BusMate Logo",
                                contentScale = ContentScale.Fit,
                                colorFilter = if (isDarkModeEnabled)
                                    ColorFilter.tint(PlaceholderBusColor)
                                else null,
                                modifier = Modifier
                                    .height(70.dp) // Increased size per your request
                                    .fillMaxWidth()
                            )
                        }

                        // 3. Notification Button
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
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
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
                    0 -> HomeScreen(children = children)
                    1 -> SupportScreen(viewModel = supportViewModel)
                    2 -> LiveLocationScreen()
                    3 -> ProfileEditScreen()
                }
            }
        }
    }
}