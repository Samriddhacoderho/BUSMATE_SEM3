package com.example.busmate.view.dashboard

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.busmate.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging

class ParentDashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

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
    val application = context.applicationContext as android.app.Application
    val accelViewModel = remember { AccelRecieverViewModel(application) }
    var selectedBusRouteId by remember { mutableStateOf<String?>(null) }
    var selectedChildId by remember { mutableStateOf<String?>(null) }
    val busViewModel = remember { BusViewModel(BusRepositoryImpl()) }
    val user by userViewModel.user.collectAsState()
    val children by childViewModel.children.collectAsState()

//    val busId = "-OgeXRJhRkVNMonROQYL" // TODO: replace with real bus id

    /* ---------- DRAWER STATE ---------- */
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(0) }
    var isViewingBusDetails by remember { mutableStateOf(false) }


    /* ---------- 1. NOTIFICATION LISTENER ---------- */
    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        val nodePath = if (user?.typeofUser?.lowercase() == "admin") "admin" else uid
        val notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(nodePath)

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val title = snapshot.child("title").getValue(String::class.java) ?: "Bus Update"
                val message = snapshot.child("message").getValue(String::class.java) ?: ""

                if (message.isNotEmpty()) {
                    NotificationHelper.showNotification(context, title, message)
                    snapshot.ref.removeValue() // Delete after showing
                }
            }
            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    /* ---------- 2. FCM TOKEN REGISTRATION ---------- */
    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid).child("fcmToken").setValue(token)
            }
        }
    }

    /* ---------- 3. DATA LOADING ---------- */
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
            NavItem("Digital Student ID", Icons.Default.QrCode),
            NavItem("Driver Profile", Icons.Default.Badge)
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
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Black
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
                when (selectedItem) {
                    0 -> HomeScreen(children = children, onOpenLiveLocation = { busId, studentId ->
                        selectedBusRouteId = busId
                        selectedChildId = studentId
                        selectedItem = 2
                    })

                    1 -> SupportScreen(supportViewModel)
                    2 -> LiveLocationScreen(
                        viewModel = locationViewModel,
                        childViewModel = childViewModel,
                        accelViewModel = accelViewModel,
                        busId = selectedBusRouteId ?: "",
                        selectedChildId = selectedChildId // Correctly passed now
                    )
                    3 -> ProfileEditScreen()
                }
            }
        }
    }
}
//fixed bugs

