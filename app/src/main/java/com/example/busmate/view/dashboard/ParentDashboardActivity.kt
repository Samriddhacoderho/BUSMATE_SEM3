package com.example.busmate.view.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Search
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import com.example.busmate.service.TripMonitoringService
import coil3.compose.AsyncImage
import com.example.busmate.ui.theme.BusMateOrange
import com.google.firebase.database.ValueEventListener
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically


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

    val userState by userViewModel.user.collectAsState()
    var dynamicNotifications by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

//    val busId = "-OgeXRJhRkVNMonROQYL" // TODO: replace with real bus id
    val busIds = remember(userState) {
        userState?.children?.values?.map { it.busRouteId }?.filter { it.isNotEmpty() }?.distinct() ?: emptyList()
    }

    /* ---------- DRAWER STATE ---------- */
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(0) }
    var isViewingBusDetails by remember { mutableStateOf(false) }
    var showNotificationOverlay by remember { mutableStateOf(false) }
    // Track the count of notifications the user has already acknowledged
    var lastSeenNotificationCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permission denied. Notifications disabled.", Toast.LENGTH_SHORT).show()
        }
    }
    // 3. Combined Logic: Load Profile, Register FCM, and Check Permissions
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

    // CHANGE: Combined logic into a single block to save battery and memory
    // Update your existing LaunchedEffect(busIds) to look like this:
    LaunchedEffect(busIds) {
        // This log is critical. If you see "Bus IDs are empty" in Logcat,
        // it means your Firebase User data hasn't loaded properly.
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

        // ... rest of your in-app listener code ...
    }
    /* ---------- 3. DATA LOADING ---------- */
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

        // We use a query to get the last 10, but we must handle them correctly
        val query = notifRef.limitToLast(10)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, String>>()
                for (notifSnapshot in snapshot.children) {
                    val title = notifSnapshot.child("title").getValue(String::class.java) ?: ""
                    val message = notifSnapshot.child("message").getValue(String::class.java) ?: ""
                    list.add(mapOf("title" to title, "message" to message))
                }
                // newest notification is now the last one in the Firebase list
                // so we reverse it for the UI LazyColumn
                dynamicNotifications = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        query.addValueEventListener(listener)
        onDispose { query.removeEventListener(listener) }
    }

    /* ---------- NAV ITEM MODEL ---------- */
    data class NavItem(val label: String, val icon: ImageVector)

    val navList = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Location", Icons.Filled.LocationOn),
        NavItem("Profile", Icons.Filled.Person)
    )
    val drawerItems = when (user?.typeofUser?.lowercase()) {
        "admin" -> listOf(
            NavItem("Create Account", Icons.Default.PersonAdd),
            NavItem("Add Bus", Icons.Default.DirectionsBus),
            NavItem("View Bus", Icons.Default.DirectionsBus),
            NavItem("View Driver", Icons.Default.Badge),
            NavItem("Manage Account", Icons.Default.PersonOff),
            NavItem("Search Child", Icons.Default.Search),
            NavItem("View Attendance", Icons.Default.ChildCare),
            NavItem("Guidelines and Rules", Icons.Default.RuleFolder)

        )

        "driver" -> listOf(
            NavItem("My Trips", Icons.Default.Route),
            NavItem("Attendance",Icons.Default.ChildCare),
            NavItem("Guidelines and Rules", Icons.Default.RuleFolder)
        )

        else -> listOf(
            NavItem("About Us", Icons.Default.Info),
            NavItem("Bus Details", Icons.Default.DirectionsBus),
            NavItem("Attendance of Children", Icons.Default.ChildCare),
            NavItem("Digital Student ID", Icons.Default.QrCode),
            NavItem("Driver Profile", Icons.Default.Badge)
        )
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, //swipe to close only
        drawerContent = {
            ModalDrawerSheet(Modifier.width(300.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column {
                        // Profile Image with Fallback Logic
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user?.profileImage.isNullOrEmpty()) {
                                // Show uploaded image from Cloudinary
                                AsyncImage(
                                    model = user?.profileImage,
                                    contentDescription = "User Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback: Show first letter of First Name if no image
                                Text(
                                    text = user?.firstName?.take(1)?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // User Name
                        Text(
                            text = "${user?.firstName ?: "Loading..."} ${user?.lastName ?: ""}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // User Role Badge
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = user?.typeofUser?.uppercase() ?: "",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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
                                    "Bus Details" -> {
                                        // ✅ Launch as Activity now
                                        context.startActivity(
                                            Intent(
                                                context,
                                                BusDetailsActivity::class.java
                                            )
                                        )
                                    }

                                    "About Us" -> { /* Handle About Us */
                                    }
                                    "Search Child" -> {
                                        context.startActivity(Intent(context, AdminSearchChildActivity::class.java))
                                    }
                                    "Digital Student ID" -> {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                StudentIdCard::class.java
                                            )
                                        )
                                    }
                                    // ... inside your drawer onClick logic ...
                                    "Driver Profile" -> {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                DriverProfileActivity::class.java
                                            )
                                        )
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

                                    // Inside ParentDashboardActivity.kt -> navigation drawer onClick logic
                                    "Attendance" -> {
                                        val currentDriverUid = user?.uid ?: ""

                                        if (currentDriverUid.isNotEmpty()) {
                                            // Use the ViewModel to check if a bus is linked to this driver UID
                                            busViewModel.getBusByDriverUid(currentDriverUid) { bus ->
                                                if (bus != null) {
                                                    // SUCCESS: Bus is assigned, proceed to Attendance
                                                    val intent = Intent(context, AttendanceActivity::class.java).apply {
                                                        putExtra("EXTRA_DRIVER_UID", currentDriverUid)
                                                    }
                                                    context.startActivity(intent)
                                                } else {
                                                    // FAILURE: No bus assigned
                                                    Toast.makeText(
                                                        context,
                                                        "You are not assigned to any bus yet.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }

                                    "View Attendance" ->context.startActivity(
                                        Intent(context, AdminAttendanceHistoryActivity::class.java)
                                    )
                                    // Inside ParentDashboardActivity
                                    "Guidelines and Rules" -> {
                                        val userRole = user?.typeofUser

                                        // LOGGING: Check if user or role is null
                                        android.util.Log.d("BUSMATE_DEBUG", "Navigating from Dashboard")
                                        android.util.Log.d("BUSMATE_DEBUG", "User Object exists: ${user != null}")
                                        android.util.Log.d("BUSMATE_DEBUG", "User Role value: '$userRole'")

                                        val intent = Intent(context, GuideLineActivity::class.java).apply {
                                            putExtra("typeOfUser", userRole) // If this is null, the other activity needs to handle it
                                        }
                                        context.startActivity(intent)
                                    }
                                    "Attendance of Children" -> {
                                        // userId is already defined at the top of your ParentDashboardScreen
                                        if (userId.isNotEmpty()) {
                                            val intent = Intent(context, ParentAttendanceActivity::class.java).apply {
                                                // ParentAttendanceActivity expects "PARENT_UID" to function
                                                putExtra("PARENT_UID", userId)
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
                                        }
                                    }

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
                                modifier = Modifier.height(70.dp).clickable(onClick = {
                                    selectedItem = 0
                                    isViewingBusDetails = false
                                    selectedBusRouteId = null
                                    selectedChildId = null
                                })
                            )
                        }
                        IconButton(onClick = {
                            showNotificationOverlay = !showNotificationOverlay
                            // Once clicked, we set the 'seen' count to match the current list size to hide the badge
                            lastSeenNotificationCount = dynamicNotifications.size
                        }) {
                            val unreadCount = dynamicNotifications.size - lastSeenNotificationCount
                            val displayCount = if (unreadCount > 5) 5 else unreadCount

                            if (displayCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White
                                        ) {
                                            Text(displayCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = if (showNotificationOverlay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (showNotificationOverlay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    navList.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = (selectedItem == index && !isViewingBusDetails),
                            onClick = {
                                selectedItem = index
                                isViewingBusDetails = false
                            },
                            icon = { Icon(item.icon, null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->

            Box(Modifier.fillMaxSize().padding(padding)) {
                Log.d("CHECK_NOTIFICATIONS_COUNT", dynamicNotifications.toString())
                when (selectedItem) {
                    0 -> HomeScreen(children = children,notifications=dynamicNotifications, onOpenLiveLocation = { busId, studentId ->
                        selectedBusRouteId = busId
                        selectedChildId = studentId
                        selectedItem = 2
                    })
                    1 ->{
                        // Logic to switch between Driver and Parent/Admin views
                        val userRole = user?.typeofUser?.lowercase()

                        if (userRole == "driver") {
                            // NEW SCREEN FOR DRIVER
                            DriverLocationScreen(
                                viewModel = locationViewModel,
                                driverUid = userId,
                                busId = user?.schoolId ?: "" // Ensure you pass the correct Bus/Route ID field for the driver
                            )
                        } else {
                            LiveLocationScreen(
                                viewModel = locationViewModel,
                                childViewModel = childViewModel,
                                accelViewModel = accelViewModel,
                                busId = selectedBusRouteId ?: "",
                                selectedChildId = selectedChildId // Correctly passed now
                            )
                        }
                    }
                    2 -> ProfileEditScreen()
                }
            }
        }
        // Add this at the end of ParentDashboardScreen, after the Scaffold

        // Remove the 'if (showNotificationOverlay)' wrapper
        AnimatedVisibility(
            visible = showNotificationOverlay,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 400),
                expandFrom = Alignment.Top
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300),
                shrinkTowards = Alignment.Top
            ) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showNotificationOverlay = false }
                    .padding(top = 80.dp, end = 16.dp, start = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .clickable(enabled = false) { }
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Recent Alerts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val displayList = dynamicNotifications.takeLast(5).reversed()

                        if (displayList.isEmpty()) {
                            Text("No notifications", color = Color.Gray, fontSize = 14.sp)
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

                        TextButton(
                            onClick = { showNotificationOverlay = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
