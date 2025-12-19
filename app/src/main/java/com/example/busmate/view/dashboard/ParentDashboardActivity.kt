package com.example.busmate.view.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
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



    /* ---------- NAV ITEMS ---------- */
    val navList = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Support", Icons.Filled.SupportAgent),
        NavItem("Location", Icons.Filled.LocationOn),
        NavItem("Profile", Icons.Filled.Person)
    )

    /* ---------- DRAWER ITEM MODEL ---------- */


    // Dynamic Logic for all 3 types of users
    val drawerItems = when (user?.typeofUser?.lowercase()) {
        "admin" -> listOf(
            NavItem("Create Account", Icons.Default.PersonAdd),
            NavItem("View Buses", Icons.Default.DirectionsBus),
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
        else -> listOf( // Default for Parent
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
                            scope.launch { drawerState.close() }
                            // Handle drawer navigation here
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        /* ---------- TOP BAR ---------- */
        topBar = {
            Surface(
                tonalElevation = 3.dp, // Adds a slight shadow/separation from content
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Ensures it doesn't hide under the clock/battery icons
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. MENU BUTTON (3 Lines)
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 2. CENTERED LOGO (Fixed & Cropped)
                    // We use a Box with weight(1f) to force the logo to stay exactly in the middle
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = "BusMate Logo",
                            // Use ContentScale.Crop and a fixed height to "cut" the white space
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            colorFilter = if (isDarkModeEnabled)
                                ColorFilter.tint(PlaceholderBusColor)
                            else null,
                            modifier = Modifier
                                .height(35.dp) // Keeps it small and professional
                                .wrapContentWidth()
                        )
                    }

                    // 3. NOTIFICATION BUTTON
                    IconButton(onClick = { /* Handle Notifications click here */ }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.error // Standard red for notifications
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
                0 -> HomeScreen(
                    children = children
                )
                1 -> SupportScreen(viewModel = supportViewModel)
                2 -> LiveLocationScreen()
                3 -> ProfileEditScreen()
            }
        }
    }
}
    }
