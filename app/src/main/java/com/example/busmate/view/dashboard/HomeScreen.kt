package com.example.busmate.view.dashboard

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateGreen
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.view.parent.AddChildActivity
import com.example.busmate.view.admin.BusScreen
import com.example.busmate.view.driver.TripActivity
import com.example.busmate.viewmodel.BusViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.busmate.data.UserRepositoryImpl
import coil3.compose.AsyncImage
import com.example.busmate.util.NotificationHelper
import com.example.busmate.util.SOSPrefs
import com.example.busmate.view.admin.AdminNotificationActivity
import com.example.busmate.view.admin.AdminAddChildActivity
import com.example.busmate.view.admin.AdminAttendanceHistoryActivity
import com.example.busmate.view.admin.AdminDeactivatesActivity
import com.example.busmate.view.admin.AdminSearchChildActivity
import com.example.busmate.view.admin.BusProfileScreen
import com.example.busmate.view.admin.CreateAccountScreenActivity
import com.example.busmate.view.admin.DriverProfileScreen
import com.example.busmate.view.admin.GuideLineActivity
import com.example.busmate.view.admin.SearchBusActivity
import com.example.busmate.view.driver.AttendanceActivity
import com.example.busmate.view.parent.BusDetailsActivity
import com.example.busmate.view.parent.DriverProfileActivity
import com.example.busmate.view.parent.ParentAttendanceActivity
import com.example.busmate.view.parent.StudentIdCard
import com.example.busmate.view.parent.ChildListActivity
import com.example.busmate.view.parent.MapPickerActivity


@Composable
fun HomeScreen(
    children: List<ChildModel> = emptyList(),
    notifications: List<Map<String, String>> = emptyList(),
    onOpenLiveLocation: (busRouteId: String, studentId: String) -> Unit,
    onSetLocationClick: (() -> Unit)? = null
) {
    val busViewModel = remember { BusViewModel(BusRepositoryImpl()) }
    val userRepository = remember { UserRepositoryImpl() }

    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    /* =======================================================
       🔔 SYSTEM NOTIFICATION LOGIC (SAFE + NON-DUPLICATE)
       ======================================================= */

    var lastNotificationCount by rememberSaveable {
        mutableIntStateOf(notifications.size)
    }
    var model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }
    val userId = model?.uid

    val showSOSDialog = remember { mutableStateOf(false) }
    val sosTitle = remember { mutableStateOf("") }
    val sosMessage = remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId != null && SOSPrefs.isSOSActive(context, userId)) {
            showSOSDialog.value = true
            sosTitle.value = SOSPrefs.getSOSTitle(context, userId)
            sosMessage.value = SOSPrefs.getSOSMessage(context, userId)
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            showSOSDialog.value = SOSPrefs.isSOSActive(context, userId)
        }
    }

    LaunchedEffect(notifications.size) {
        if (notifications.size > lastNotificationCount && notifications.isNotEmpty()) {
            val latestNotification = notifications.last()
            val title = latestNotification["title"] ?: "BusMate Alert"
            val message = latestNotification["message"] ?: ""

            Log.d("NOTIF_DEBUG", "New notification received: $title")

            NotificationHelper.showNotification(
                context = context,
                title = title,
                message = message
            )
        }
        lastNotificationCount = notifications.size
    }

    /* =======================================================
       🔹 USER MODEL STATE & AUTOMATIC CHILD SYNC
       ======================================================= */
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                userRepository.getCurrentUserProfile { success, _, user ->
                    if (success && user != null) {
                        model = user
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val childrenList = remember(children, model) {
        if (children.isNotEmpty()) children
        else model?.children?.values?.toList() ?: emptyList()
    }

    /* =======================================================
       🔹 NAVIGATION CALLBACKS
       ======================================================= */

    val navigateToAddChild = {
        context.startActivity(Intent(context, AddChildActivity::class.java))
    }

    val navigateToAddBus = {
        context.startActivity(Intent(context, BusScreen::class.java))
    }

    val navigateToTrip = {
        busViewModel.getBusByDriverUid(model?.uid ?: "") { bus ->
            if (bus != null) {
                val intent = Intent(context, TripActivity::class.java).apply {
                    putExtra("EXTRA_DRIVER_UID", model?.uid)
                    putExtra("EXTRA_BUS_ID", bus.uid)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Bus not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (model?.typeofUser?.lowercase() != "driver") {
        SOSObserver(
            userId = model?.uid ?: return,
            userRole = model?.typeofUser,
            children = model?.children,
            onSOSReceived = { title, message ->
                sosTitle.value = title
                sosMessage.value = message
                showSOSDialog.value = true
            }
        )
    }

    /* =======================================================
       🔹 UI COMPOSITION - MODERN REDESIGN
       ======================================================= */

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
//                .padding(paddingValues)
            ,
            contentPadding = PaddingValues(top = 20.dp, bottom = 16.dp)
        ) {

            // MODERN GRADIENT HEADER
            item {
                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                    ModernWelcomeCard(
                        parentName = "${model?.firstName} ${model?.lastName}",
                        model = model
                    )
                } else {
                    ModernAdminWelcomeCard("${model?.firstName} ${model?.lastName}")
                }
            }

            // ADMIN: FEATURE GRID BELOW WELCOME CARD
            if (model?.typeofUser == "Admin") {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFF1A1A1A)
                    )
                }

                item {
                    AdminFeatureGrid(
                        onCreateUserClick = {
                            context.startActivity(Intent(context, CreateAccountScreenActivity::class.java))
                        },
                        onAddBusClick = {
                            context.startActivity(Intent(context, BusScreen::class.java))
                        },
                        onViewBusClick = {
                            context.startActivity(Intent(context, BusProfileScreen::class.java))
                        },
                        onViewDriverClick = {
                            context.startActivity(Intent(context, DriverProfileScreen::class.java))
                        },
                        onManageAccountClick = {
                            context.startActivity(Intent(context, AdminDeactivatesActivity::class.java))
                        },
                        onSearchChildClick = {
                            context.startActivity(Intent(context, AdminSearchChildActivity::class.java))
                        },
                        onViewAttendanceClick = {
                            context.startActivity(Intent(context, AdminAttendanceHistoryActivity::class.java))
                        },
                        onGuidelinesClick = {
                            val intent = Intent(context, GuideLineActivity::class.java).apply {
                                putExtra("typeOfUser", "Admin")
                            }
                            context.startActivity(intent)
                        },
                        onSearchBusClick = {
                            context.startActivity(Intent(context, SearchBusActivity::class.java))
                        },
                        onCreateChildClick = {
                            context.startActivity(Intent(context, AdminAddChildActivity::class.java))
                        },
                        onSetLocationClick = {
                            onSetLocationClick?.invoke()
                        }
                    )
                }
            }

            // PARENT: SECTION HEADER
            if (model?.typeofUser == "Parent") {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "My Children",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFF1A1A1A)
                    )
                }

                // CHILD CARDS
                if (childrenList.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No children added yet",
                            icon = Icons.Default.ChildCare
                        )
                    }
                } else {
                    items(childrenList) { child ->
                        ModernChildCard(
                            childName = "${child.firstName} ${child.lastName}",
                            statusText = "On Route",
                            studentId = child.studentId,
                            routeId = child.busRouteId,
                            statusColor = BusMateGreen,
                            imageUrl = child.profileImage,
                            onClick = {
                                busViewModel.getBusByRouteId(child.busRouteId) { bus ->
                                    if (bus == null) {
                                        Toast.makeText(
                                            context,
                                            "No bus linked to this route",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@getBusByRouteId
                                    }

                                    when {
                                        bus.driver == null ->
                                            Toast.makeText(
                                                context,
                                                "Driver not assigned yet",
                                                Toast.LENGTH_LONG
                                            ).show()

                                        !bus.isTripRunning ->
                                            Toast.makeText(
                                                context,
                                                "The trip has not started yet",
                                                Toast.LENGTH_LONG
                                            ).show()

                                        else -> onOpenLiveLocation(bus.uid, child.studentId)
                                    }
                                }
                            }
                        )
                    }
                }

                // PARENT: FEATURE GRID BELOW CHILD CARDS
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Services",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFF1A1A1A)
                    )
                }

                item {
                    ParentFeatureGrid(
                        onBusDetailsClick = {
                            context.startActivity(Intent(context, BusDetailsActivity::class.java))
                        },
                        onDriverProfileClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    DriverProfileActivity::class.java
                                )
                            )
                        },
                        onAttendanceClick = {
                            if (!userId.isNullOrEmpty()) {
                                val intent =
                                    Intent(context, ParentAttendanceActivity::class.java).apply {
                                        putExtra("PARENT_UID", userId)
                                    }
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        onStudentIdClick = {
                            context.startActivity(Intent(context, StudentIdCard::class.java))
                        },
                        onManageChildrenClick = {  // NEW
                            context.startActivity(Intent(context, ChildListActivity::class.java))
                        }
                    )
                }
            }

            // DRIVER: FEATURE GRID ABOVE "MY DUTIES"
            if (model?.typeofUser == "Driver") {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFF1A1A1A)
                    )
                }

                item {
                    DriverFeatureGrid(
                        onAttendanceClick = {
                            val currentDriverUid = model?.uid ?: ""
                            if (currentDriverUid.isNotEmpty()) {
                                busViewModel.getBusByDriverUid(currentDriverUid) { bus ->
                                    if (bus != null) {
                                        val intent = Intent(context, AttendanceActivity::class.java).apply {
                                            putExtra("EXTRA_DRIVER_UID", currentDriverUid)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "You are not assigned to any bus yet.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onGuidelinesClick = {
                            val intent = Intent(context, GuideLineActivity::class.java).apply {
                                putExtra("typeOfUser", "Driver")
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "My Duties",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            // NOTIFICATIONS SECTION
            item {
                Spacer(Modifier.height(8.dp))
                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                    ModernNotificationsHeader()
                } else {
                    ModernAdminNotificationsHeader(
                        onAddBusClick = navigateToAddBus
                    )
                }
            }

            val latestNotifications = notifications.takeLast(5).reversed()

            items(latestNotifications) { notification ->
                ModernNotificationCard(
                    initial = notification["title"]?.take(1) ?: "!",
                    message = notification["message"] ?: "",
                    indicatorColor = BusMateOrange
                )
            }

            if (notifications.isEmpty() &&
                (model?.typeofUser == "Parent" || model?.typeofUser == "Driver")
            ) {
                item {
                    EmptyNotificationsCard()
                }
            }

            // DRIVER BUTTONS
            if (model?.typeofUser == "Driver") {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ModernSOSButton(
                        onClick = {
                            busViewModel.triggerSOS(model?.uid ?: "")
                            Toast.makeText(context, "Emergency Alert Sent!", Toast.LENGTH_LONG).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    ModernTripButton(onClick = navigateToTrip)
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        if (showSOSDialog.value && userId != null) {
            SOSAlertDialog(
                title = sosTitle.value,
                message = sosMessage.value,
                onClose = {
                    SOSPrefs.setSOSActive(context, userId, false)
                    showSOSDialog.value = false
                }
            )
        }
    }
}

// ============================================
// MODERN FEATURE GRIDS WITH COLORFUL ICONS
// ============================================

data class FeatureItem(
    val label: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val onClick: () -> Unit
)

@Composable
fun AdminFeatureGrid(
    onCreateUserClick: () -> Unit,
    onAddBusClick: () -> Unit,
    onViewBusClick: () -> Unit,
    onViewDriverClick: () -> Unit,
    onManageAccountClick: () -> Unit,
    onSearchChildClick: () -> Unit,
    onViewAttendanceClick: () -> Unit,
    onGuidelinesClick: () -> Unit,
    onSearchBusClick: () -> Unit,
    onCreateChildClick: () -> Unit,
    onSetLocationClick: () -> Unit
) {
    val features = listOf(
        FeatureItem("Create User", Icons.Default.PersonAdd, listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), onCreateUserClick),
        FeatureItem("Add Bus", Icons.Default.DirectionsBus, listOf(Color(0xFF2567E8), Color(0xFF1D4ED8)), onAddBusClick),
        FeatureItem("View Bus", Icons.Default.DirectionsBus, listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)), onViewBusClick),
        FeatureItem("View Driver", Icons.Default.Badge, listOf(Color(0xFF10B981), Color(0xFF059669)), onViewDriverClick),
        FeatureItem("Manage Users", Icons.Default.PersonOff, listOf(Color(0xFFEF4444), Color(0xFFDC2626)), onManageAccountClick),
        FeatureItem("Search Child", Icons.Default.Search, listOf(Color(0xFFF59E0B), Color(0xFFD97706)), onSearchChildClick),
        FeatureItem("Attendance", Icons.Default.ChildCare, listOf(Color(0xFFEC4899), Color(0xFFDB2777)), onViewAttendanceClick),
        FeatureItem("Guidelines", Icons.Default.RuleFolder, listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), onGuidelinesClick),
        FeatureItem("Search Bus", Icons.Default.DirectionsBus, listOf(Color(0xFF06B6D4), Color(0xFF0891B2)), onSearchBusClick),
        FeatureItem("Create Child", Icons.Default.ManageAccounts, listOf(Color(0xFF14B8A6), Color(0xFF0D9488)), onCreateChildClick),
        FeatureItem("Set Location", Icons.Default.LocationOn, listOf(Color(0xFFF97316), Color(0xFFEA580C)), onSetLocationClick)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 600.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(features) { feature ->
            FeatureGridItem(
                label = feature.label,
                icon = feature.icon,
                gradientColors = feature.gradientColors,
                onClick = feature.onClick
            )
        }
    }
}

@Composable
fun ParentFeatureGrid(
    onBusDetailsClick: () -> Unit,
    onDriverProfileClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onStudentIdClick: () -> Unit,
    onManageChildrenClick: () -> Unit  // NEW
) {
    val features = listOf(
        FeatureItem("Bus Details", Icons.Default.DirectionsBus, listOf(Color(0xFF2567E8), Color(0xFF1D4ED8)), onBusDetailsClick),
        FeatureItem("Driver Profile", Icons.Default.Badge, listOf(Color(0xFF10B981), Color(0xFF059669)), onDriverProfileClick),
        FeatureItem("Attendance", Icons.Default.ChildCare, listOf(Color(0xFFF59E0B), Color(0xFFD97706)), onAttendanceClick),
        FeatureItem("Student ID", Icons.Default.QrCode, listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), onStudentIdClick),
        FeatureItem("Manage Children", Icons.Default.FamilyRestroom, listOf(Color(0xFFEC4899), Color(0xFFDB2777)), onManageChildrenClick)  // NEW
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp),  // Increased height for 2 rows
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(features) { feature ->
            FeatureGridItem(
                label = feature.label,
                icon = feature.icon,
                gradientColors = feature.gradientColors,
                onClick = feature.onClick
            )
        }
    }
}

@Composable
fun DriverFeatureGrid(
    onAttendanceClick: () -> Unit,
    onGuidelinesClick: () -> Unit
) {
    val features = listOf(
        FeatureItem("Attendance", Icons.Default.ChildCare, listOf(Color(0xFF2567E8), Color(0xFF1D4ED8)), onAttendanceClick),
        FeatureItem("Guidelines", Icons.Default.RuleFolder, listOf(Color(0xFF10B981), Color(0xFF059669)), onGuidelinesClick)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        features.forEach { feature ->
            FeatureGridItem(
                label = feature.label,
                icon = feature.icon,
                gradientColors = feature.gradientColors,
                onClick = feature.onClick,
                modifier = Modifier.weight(1f)
            )
        }
        // Empty spacers for alignment
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun FeatureGridItem(
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Colorful gradient icon background
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4B5563),
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            maxLines = 2
        )
    }
}

// ============================================
// MODERN UI COMPONENTS
// ============================================

@Composable
fun ModernWelcomeCard(parentName: String?, model: UserModel?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2567E8),
                            Color(0xFF1D4ED8)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WavingHand,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Welcome back!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = parentName ?: "User",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusChip(
                        icon = Icons.Default.School,
                        label = "School",
                        isActive = true,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(
                        icon = Icons.Default.MyLocation,
                        label = "Live Tracking",
                        isActive = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0xFFFFB74D) else Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Color.White else Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ModernAdminWelcomeCard(adminName: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2567E8),
                            Color(0xFF1D4ED8)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Admin Dashboard",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = adminName ?: "Administrator",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
fun ModernChildCard(
    childName: String,
    statusText: String,
    studentId: String,
    routeId: String,
    statusColor: Color,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF42A5F5),
                                Color(0xFF2567E8)
                            )
                        )
                    )
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = childName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = childName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ID: $studentId • Route: $routeId",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ModernNotificationsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF2567E8),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Recent Alerts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
fun ModernAdminNotificationsHeader(
    onAddBusClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF2567E8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun ModernNotificationCard(
    initial: String,
    message: String,
    indicatorColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(indicatorColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = indicatorColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f),
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyNotificationsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BusMateGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "All caught up! No new alerts.",
                fontSize = 15.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModernSOSButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53935)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "EMERGENCY SOS",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ModernTripButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2567E8)
        )
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Go to Trip",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================
// BACKWARD COMPATIBILITY
// ============================================

@Composable
fun NotificationItemScreen(
    initial: String,
    message: String,
    indicatorColor: Color
) {
    ModernNotificationCard(initial, message, indicatorColor)
}

@Composable
fun SOSObserver(
    userId: String,
    userRole: String?,
    children: Map<String, ChildModel>?,
    onSOSReceived: (String, String) -> Unit
) {
    val context = LocalContext.current

    if (userRole == null || userRole.lowercase() == "driver") return

    DisposableEffect(userId, userRole) {
        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance()
            .getReference("notifications")

        val lastSeenTime = SOSPrefs.getLastSeen(context, userId)

        val query = db.orderByChild("timestamp")
            .startAt(lastSeenTime.toDouble() + 1)

        val listener = object : com.google.firebase.database.ChildEventListener {
            override fun onChildAdded(
                snapshot: com.google.firebase.database.DataSnapshot,
                previousChildName: String?
            ) {
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: return
                val alertRouteId = snapshot.child("routeId").getValue(String::class.java)
                val busNo = snapshot.child("busNumber").getValue(String::class.java) ?: "N/A"

                val audience = snapshot.child("audience")
                    .getValue(
                        object : com.google.firebase.database.GenericTypeIndicator<List<String>>() {}
                    ) ?: emptyList()

                if (!audience.contains(userRole.lowercase())) return

                var shown = false
                var title = ""
                var message = ""

                when (userRole.lowercase()) {
                    "admin" -> {
                        title = "Admin: SOS ALERT"
                        message = "SOS: BUS $busNo (Route: $alertRouteId) reported an emergency"
                        NotificationHelper.showNotification(context, title, message)
                        shown = true
                    }

                    "parent" -> {
                        val hasMatchingChild =
                            children?.values?.any {
                                it.busRouteId == alertRouteId
                            } == true

                        if (hasMatchingChild) {
                            title = "Parent: SOS ALERT"
                            message = "SOS: BUS $busNo has reported an emergency"
                            NotificationHelper.showNotification(context, title, message)
                            shown = true
                        }
                    }
                }
                if (shown) {
                    SOSPrefs.setLastSeen(context, userId, timestamp)
                    SOSPrefs.setSOSActive(context, userId, true)
                    SOSPrefs.saveSOSContent(context, userId, title, message)
                    onSOSReceived(title, message)
                }
            }

            override fun onChildChanged(
                snapshot: com.google.firebase.database.DataSnapshot,
                previousChildName: String?
            ) {}

            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {}
            override fun onChildMoved(
                snapshot: com.google.firebase.database.DataSnapshot,
                previousChildName: String?
            ) {}

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }

        query.addChildEventListener(listener)

        onDispose {
            query.removeEventListener(listener)
        }
    }
}

@Composable
fun SOSAlertDialog(
    title: String,
    message: String,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Close", color = Color.White)
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 16.sp
            )
        }
    )
}