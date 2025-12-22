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
import com.example.busmate.data.*
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.view.*
import com.example.busmate.viewmodel.*
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
    val context = LocalContext.current

    /* ---------- VIEW MODELS ---------- */
    val userViewModel = remember { UserViewModel(UserRepositoryImpl()) }
    val supportViewModel = remember { SupportViewModel(SupportRepositoryImpl()) }
    val childViewModel = remember { ChildViewModel(ChildRepositoryImpl()) }
    val locationViewModel = remember { LocationViewModel(LocationImpl(context)) }
    var selectedBusRouteId by remember { mutableStateOf<String?>(null) }


    val user by userViewModel.user.collectAsState()
    val children by childViewModel.children.collectAsState()


    /* ---------- DRAWER STATE ---------- */
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(0) }


    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            userViewModel.loadUserProfile(it)
            childViewModel.observeChildren(it)
        }
    }

    /* ---------- NAV ITEM MODEL ---------- */
    data class NavItem(val label: String, val icon: ImageVector)

    val navList = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Support", Icons.Filled.SupportAgent),
        NavItem("Location", Icons.Filled.LocationOn),
        NavItem("Profile", Icons.Filled.Person)
    )

    val drawerItems = when (user?.typeofUser?.lowercase()) {
        "admin" -> listOf(
            NavItem("Create Account", Icons.Default.PersonAdd),
            NavItem("Add Bus", Icons.Default.DirectionsBus),
            NavItem("View Bus", Icons.Default.DirectionsBus),
            NavItem("View Driver", Icons.Default.Badge),
            NavItem("Manage Account", Icons.Default.PersonOff)
        )

        "driver" -> listOf(
            NavItem("My Trips", Icons.Default.Route)
        )

        else -> listOf(
            NavItem("Digital Student ID", Icons.Default.QrCode),
            NavItem("About Us", Icons.Default.Info)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(Modifier.width(300.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            user?.firstName ?: "Loading...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            user?.typeofUser ?: "",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.label) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                when (item.label) {
                                    "Digital Student ID" -> {
                                        context.startActivity(Intent(context, StudentIdCard::class.java))
                                    }

                                    "Create Account" -> context.startActivity(
                                        Intent(
                                            context,
                                            CreateAccountScreenActivity::class.java
                                        )
                                    )

                                    "Add Bus" -> context.startActivity(
                                        Intent(
                                            context,
                                            BusScreen::class.java
                                        )
                                    )

                                    "View Bus" -> context.startActivity(
                                        Intent(
                                            context,
                                            BusProfileScreen::class.java
                                        )
                                    )

                                    "View Driver" -> context.startActivity(
                                        Intent(
                                            context,
                                            DriverProfileScreen::class.java
                                        )
                                    )

                                    "Manage Account" -> context.startActivity(
                                        Intent(
                                            context,
                                            AdminDeactivatesActivity::class.java
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 4.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }

                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                colorFilter = if (isDarkModeEnabled)
                                    ColorFilter.tint(PlaceholderBusColor)
                                else null,
                                modifier = Modifier.height(70.dp)
                            )
                        }

                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },

            bottomBar = {
                NavigationBar {
                    navList.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index

                            },
                            icon = { Icon(item.icon, null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                    when (selectedItem) {
                        0 -> HomeScreen(
                            children = children,
                            onOpenLiveLocation = { routeId ->
                                selectedBusRouteId = routeId
                                selectedItem = 2   // ✅ Switch to Location tab
                            }
                        )

                        1 -> SupportScreen(supportViewModel)
                        2 -> LiveLocationScreen(
                            viewModel = locationViewModel,
                            busId = selectedBusRouteId ?: "No bus selected"
                        )

                        3 -> ProfileEditScreen()
                    }
                }
            }
        }
    }
