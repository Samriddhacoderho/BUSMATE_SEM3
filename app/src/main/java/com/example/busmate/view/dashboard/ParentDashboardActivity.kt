package com.example.busmate.view.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.busmate.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import com.example.busmate.service.TripMonitoringService
import coil3.compose.AsyncImage
import com.example.busmate.ui.theme.BusMateOrange
import com.google.firebase.database.ValueEventListener
import com.example.busmate.service.ETAMonitoringService
import com.example.busmate.view.admin.AdminNotificationActivity
import com.example.busmate.view.parent.MapPickerActivity
import com.example.busmate.view.parent.ChatScreen
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


data class NavItem(val label: String, val icon: ImageVector)

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
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    /* ---------- VIEW MODELS ---------- */
    val userViewModel = remember { UserViewModel(UserRepositoryImpl()) }
    val childViewModel = remember { ChildViewModel(ChildRepositoryImpl()) }
    val locationViewModel = remember { LocationViewModel(LocationImpl(context)) }
    val application = context.applicationContext as android.app.Application
    val accelViewModel = remember { AccelRecieverViewModel(application) }
    var selectedBusRouteId by remember { mutableStateOf<String?>(null) }
    var selectedChildId by remember { mutableStateOf<String?>(null) }
    val busViewModel = remember { BusViewModel(BusRepositoryImpl()) }
    val user by userViewModel.user.collectAsState()
    val children by childViewModel.children.collectAsState()
    val chatViewModel = remember { ChatViewModel() }

    var showChatDialog by remember { mutableStateOf(false) }

    val userState by userViewModel.user.collectAsState()
    var dynamicNotifications by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    val busIds = remember(userState) {
        userState?.children?.values?.map { it.busRouteId }?.filter { it.isNotEmpty() }?.distinct() ?: emptyList()
    }

    /* ---------- DRAWER STATE ---------- */
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(0) }
    var isViewingBusDetails by remember { mutableStateOf(false) }
    var showNotificationOverlay by remember { mutableStateOf(false) }
    var lastSeenNotificationCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permission denied. Notifications disabled.", Toast.LENGTH_SHORT).show()
        }
    }

    // MapPicker launcher for school location
    val mapPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            val address = result.data?.getStringExtra("address") ?: ""

            if (lat != 0.0 && lng != 0.0) {
                busViewModel.saveSchoolLocation(lat, lng, address)
                Toast.makeText(context, "School location updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.loadUserProfile(userId)

        UserRepositoryImpl().updateFcmToken { success ->
            Log.d("FCM", "Token update: $success")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(busIds) {
        if (busIds.isEmpty()) {
            Log.d("TripMonitor", "Bus IDs are empty, waiting for data...")
            return@LaunchedEffect
        }

        Log.d("TripMonitor", "Starting Service with: $busIds")

        val serviceIntent = Intent(context, TripMonitoringService::class.java).apply {
            putStringArrayListExtra("BUS_ROUTE_IDS", ArrayList(busIds))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    LaunchedEffect(user?.typeofUser) {
        if (user?.typeofUser == "Parent") {
            val etaServiceIntent = Intent(context, ETAMonitoringService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(etaServiceIntent)
            } else {
                context.startService(etaServiceIntent)
            }

            Log.d("ParentDashboard", "ETA Monitoring Service started")
        }
    }

    DisposableEffect(user?.typeofUser) {
        onDispose {
            if (user?.typeofUser == "Parent") {
                val etaServiceIntent = Intent(context, ETAMonitoringService::class.java)
                context.stopService(etaServiceIntent)
                Log.d("ParentDashboard", "ETA Monitoring Service stopped")
            }
        }
    }

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            userViewModel.loadUserProfile(it)
            childViewModel.observeChildren(it)
        }
    }

    DisposableEffect(user?.typeofUser) {
        val database = FirebaseDatabase.getInstance()
        val currentUserType = user?.typeofUser?.lowercase()

        val notifRef = if (currentUserType == "admin") {
            database.getReference("notifications").child("admin")
        } else {
            database.getReference("notifications").child(userId)
        }

        val query = notifRef.limitToLast(10)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, String>>()
                for (notifSnapshot in snapshot.children) {
                    val title = notifSnapshot.child("title").getValue(String::class.java) ?: ""
                    val message = notifSnapshot.child("message").getValue(String::class.java) ?: ""
                    list.add(mapOf("title" to title, "message" to message))
                }
                dynamicNotifications = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        query.addValueEventListener(listener)
        onDispose { query.removeEventListener(listener) }
    }

    val navList = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Location", Icons.Filled.LocationOn),
        NavItem("Profile", Icons.Filled.Person)
    )

    // Minimal drawer items
    val drawerItems = when (user?.typeofUser?.lowercase()) {
        "admin" -> emptyList() // Admin has everything in grid
        "driver" -> listOf(
            NavItem("My Trips", Icons.Default.Route)
        )
        else -> listOf(
            NavItem("About Us", Icons.Default.Info)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModernDrawerContent(
                user = user,
                drawerItems = drawerItems,
                onItemClick = { label ->
                    scope.launch {
                        drawerState.close()
                        when (label) {
                            "About Us" -> {
                                Toast.makeText(context, "About Us - Coming Soon", Toast.LENGTH_SHORT).show()
                            }
                            "My Trips" -> {
                                Toast.makeText(context, "My Trips - Coming Soon", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFFF8F9FA),
            topBar = {
                ModernTopBar(
                    isDarkModeEnabled = isDarkModeEnabled,
                    notificationCount = dynamicNotifications.size - lastSeenNotificationCount,
                    showNotificationOverlay = showNotificationOverlay,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onLogoClick = {
                        selectedItem = 0
                        isViewingBusDetails = false
                        selectedBusRouteId = null
                        selectedChildId = null
                    },
                    onNotificationClick = {
                        showNotificationOverlay = !showNotificationOverlay
                        lastSeenNotificationCount = dynamicNotifications.size
                    }
                )
            },
            bottomBar = {
                ModernBottomNavBar(
                    navList = navList,
                    selectedItem = selectedItem,
                    onItemClick = { index ->
                        selectedItem = index
                        isViewingBusDetails = false
                    }
                )
            },
            floatingActionButton = {
                when (user?.typeofUser) {
                    "Admin" -> {
                        FloatingActionButton(
                            onClick = {
                                context.startActivity(Intent(context, AdminNotificationActivity::class.java))
                            },
                            containerColor = Color(0xFF2567E8),
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = "Broadcast",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    "Parent" -> {
                        FloatingActionButton(
                            onClick = { showChatDialog = true },
                            containerColor = BusMateOrange,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "Chat AI")
                        }
                    }
                }
            }
        ) { padding ->

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedItem) {
                    0 -> HomeScreen(
                        children = children,
                        notifications = dynamicNotifications,
                        onOpenLiveLocation = { busId, studentId ->
                            // FIXED: Reset trip type when navigating to location screen
                            locationViewModel.resetTripType()
                            selectedBusRouteId = busId
                            selectedChildId = studentId
                            selectedItem = 1
                        },
                        onSetLocationClick = {
                            val intent = Intent(context, MapPickerActivity::class.java)
                            mapPickerLauncher.launch(intent)
                        }
                    )
                    1 -> {
                        val userRole = user?.typeofUser?.lowercase()

                        if (userRole == "driver") {
                            DriverLocationScreen(
                                viewModel = locationViewModel,
                                driverUid = userId,
                                busId = user?.schoolId ?: ""
                            )
                        } else {
                            LiveLocationScreen(
                                viewModel = locationViewModel,
                                childViewModel = childViewModel,
                                accelViewModel = accelViewModel,
                                busId = selectedBusRouteId ?: "",
                                selectedChildId = selectedChildId
                            )
                        }
                    }
                    2 -> ProfileEditScreen()
                }
            }
        }

        // Notification Overlay
        AnimatedVisibility(
            visible = showNotificationOverlay,
            enter = expandVertically(animationSpec = tween(400), expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300), shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            ModernNotificationOverlay(
                notifications = dynamicNotifications,
                onClose = { showNotificationOverlay = false }
            )
        }

        // Chat Dialog
        if (showChatDialog) {
            Dialog(
                onDismissRequest = { showChatDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                ChatScreen(
                    viewModel = chatViewModel,
                    user = user,
                    onClose = { showChatDialog = false }
                )
            }
        }
    }
}

// ============================================
// MODERN UI COMPONENTS
// ============================================

@Composable
fun ModernTopBar(
    isDarkModeEnabled: Boolean,
    notificationCount: Int,
    showNotificationOverlay: Boolean,
    onMenuClick: () -> Unit,
    onLogoClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF2567E8)
                )
            }

            Box(
                Modifier.weight(1f),
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
                        .height(50.dp)
                        .clickable(onClick = onLogoClick)
                )
            }

            IconButton(onClick = onNotificationClick) {
                val displayCount = if (notificationCount > 5) 5 else notificationCount

                if (displayCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Color(0xFFE53935),
                                contentColor = Color.White
                            ) {
                                Text(
                                    displayCount.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (showNotificationOverlay)
                                Color(0xFF2567E8)
                            else
                                Color(0xFF6B7280)
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = if (showNotificationOverlay)
                            Color(0xFF2567E8)
                        else
                            Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBottomNavBar(
    navList: List<NavItem>,
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navList.forEachIndexed { index, item ->
                    BottomNavItem(
                        icon = item.icon,
                        selected = selectedItem == index,
                        onClick = { onItemClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color(0xFF2567E8) else Color(0xFFB0B0B0),
                modifier = Modifier.size(26.dp)
            )

            if (selected) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2567E8))
                )
            } else {
                Spacer(Modifier.height(7.dp))
            }
        }
    }
}

@Composable
fun ModernDrawerContent(
    user: com.example.busmate.model.UserModel?,
    drawerItems: List<NavItem>,
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2567E8),
                            Color(0xFF1D4ED8)
                        )
                    )
                )
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.profileImage.isNullOrEmpty()) {
                        AsyncImage(
                            model = user?.profileImage,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user?.firstName?.take(1)?.uppercase() ?: "U",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "${user?.firstName ?: "User"} ${user?.lastName ?: ""}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = user?.typeofUser?.uppercase() ?: "ROLE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        drawerItems.forEach { item ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = Color(0xFF2567E8)
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = false,
                onClick = { onItemClick(item.label) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun ModernNotificationOverlay(
    notifications: List<Map<String, String>>,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onClose() }
            .padding(top = 70.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) { }
                .animateContentSize(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Alerts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                val displayList = notifications.takeLast(5).reversed()

                if (displayList.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "No notifications",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    displayList.forEach { notification ->
                        NotificationItemScreen(
                            initial = notification["title"]?.take(1) ?: "!",
                            message = notification["message"] ?: "",
                            indicatorColor = BusMateOrange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}