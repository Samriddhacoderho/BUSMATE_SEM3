package com.example.busmate.view.dashboard
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateGreen
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.view.AddChildActivity
import com.example.busmate.view.BusScreen
import com.example.busmate.view.TripActivity
import com.example.busmate.viewmodel.BusViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.busmate.data.UserRepositoryImpl
import coil3.compose.AsyncImage
import com.example.busmate.util.NotificationHelper
import com.example.busmate.util.SOSPrefs


@Composable
fun HomeScreen(
    children: List<ChildModel> = emptyList(),
    notifications: List<Map<String, String>> = emptyList(),
    onOpenLiveLocation: (busRouteId: String, studentId: String) -> Unit
) {
    val busViewModel = remember { BusViewModel(BusRepositoryImpl()) }
    val userRepository = remember { UserRepositoryImpl() }

    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    /* =======================================================
       🔔 SYSTEM NOTIFICATION LOGIC (SAFE + NON-DUPLICATE)
       ======================================================= */

    // Remember last processed notification count
    var lastNotificationCount by rememberSaveable {
        mutableIntStateOf(notifications.size)
    }
    var model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }
    val userId = model?.uid

    val showSOSDialog = remember {
        mutableStateOf(false)
    }
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
        // Only trigger if a NEW notification arrives
        if (notifications.size > lastNotificationCount && notifications.isNotEmpty()) {

            val latestNotification = notifications.last()
            val title = latestNotification["title"] ?: "BusMate Alert"
            val message = latestNotification["message"] ?: ""

            Log.d("NOTIF_DEBUG", "New notification received: $title")

            // 🔔 Show system notification
            NotificationHelper.showNotification(
                context = context,
                title = title,
                message = message
            )
        }

        // Update count to prevent duplicates
        lastNotificationCount = notifications.size
    }

    /* =======================================================
       🔹 USER MODEL STATE (UNCHANGED)
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

    /* =======================================================
       🔹 NAVIGATION CALLBACKS (UNCHANGED)
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
       🔹 YOUR ORIGINAL UI BELOW (UNCHANGED)
       ======================================================= */

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // HEADER
            item {
                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                    WelcomeCardScreen(
                        parentName = "${model?.firstName} ${model?.lastName}",
                        model = model
                    )
                } else {
                    WelcomeCardAdmin("${model?.firstName} ${model?.lastName}")
                }

                MyChildrenHeaderScreen(
                    model = model,
                    onAddChildClick = navigateToAddChild
                )
            }

            // CHILD LIST
            if (model?.typeofUser == "Parent") {
                val childrenList =
                    if (children.isNotEmpty()) children
                    else model?.children?.values?.toList().orEmpty()

                if (childrenList.isEmpty()) {
                    item {
                        Text(
                            text = "No children added yet",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(childrenList) { child ->
                        ChildTrackingCardScreen(
                            childName = "${child.firstName} ${child.lastName}",
                            statusText = "On Route",
                            subText = "Student ID: ${child.studentId}\nRoute: ${child.busRouteId}",
                            statusColor = BusMateGreen,
                            imageUrl = child.profileImage,
                            imageResource = R.drawable.boy,
                            mapImageResource = R.drawable.map,
                            onClick = {
                                busViewModel.getBusByRouteId(child.busRouteId) { bus ->
                                    if (bus == null) {
                                        Toast.makeText(context, "No bus linked to this route", Toast.LENGTH_SHORT).show()
                                        return@getBusByRouteId
                                    }

                                    when {
                                        bus.driver == null ->
                                            Toast.makeText(context, "Driver not assigned yet", Toast.LENGTH_LONG).show()

                                        bus.speed <= 0.1 ->
                                            Toast.makeText(context, "Trip has not started yet", Toast.LENGTH_LONG).show()

                                        else -> onOpenLiveLocation(bus.uid, child.studentId)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // FOOTER
            item {
                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                    NotificationsAlertHeaderScreen()
                } else {
                    NotificationsAlertHeaderAdmin(onAddBusClick = navigateToAddBus)
                }
            }

            val latestNotifications = notifications.takeLast(5).reversed()

            // 🔔 DYNAMIC NOTIFICATIONS (UNCHANGED)
            items(latestNotifications) { notification ->
                NotificationItemScreen(
                    initial = notification["title"]?.take(1) ?: "!",
                    message = notification["message"] ?: "",
                    indicatorColor = BusMateOrange
                )
            }

            if (notifications.isEmpty() &&
                (model?.typeofUser == "Parent" || model?.typeofUser == "Driver")
            ) {
                item {
                    Text(
                        text = "No new alerts",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // DRIVER BUTTONS (UNCHANGED)
            if (model?.typeofUser == "Driver") {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            busViewModel.triggerSOS(model?.uid ?: "")
                            Toast.makeText(context, "Emergency Alert Sent!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(65.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("EMERGENCY SOS", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            // DRIVER BUTTONS (SOS + GO TO TRIP)
            if (model?.typeofUser == "Driver") {

                item {
                    Spacer(modifier = Modifier.height(30.dp))

                    // ✅ GO TO TRIP BUTTON (RESTORED)
                    Button(
                        onClick = navigateToTrip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Go to Trip",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
@Composable
fun WelcomeCardScreen(parentName: String?, model: UserModel?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome, $parentName!",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .height(40.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f)
                    .background(BusMateOrange),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text(
                    "School",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text(
                    "Tracking Live",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun MyChildrenHeaderScreen(model: UserModel?, onAddChildClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (model?.typeofUser) {
                "Parent" -> "My Children"
                "Driver" -> "My Duties"
                else -> "View Buses"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (model?.typeofUser == "Parent") {
            OutlinedButton(
                onClick = onAddChildClick,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(35.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Child",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Child", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ChildTrackingCardScreen(
    childName: String,
    statusText: String,
    subText: String,
    statusColor: Color,
    imageUrl: String?,      // Added
    imageResource: Int,
    mapImageResource: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- PHOTO SECTION UPDATED ---
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
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
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(text = childName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.8f))) {
                        Text(text = statusText, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = subText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Image(
                painter = painterResource(id = mapImageResource),
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun NotificationsAlertHeaderScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Notifications & Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }

    NotificationItemScreen(
        initial = "S",
        message = "School closed on Friday",
        indicatorColor = BusMateOrange
    )
}


@Composable
fun NotificationItemScreen(
    initial: String,
    message: String,
    indicatorColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Reduced padding so they stack nicely
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(indicatorColor.copy(alpha = 0.1f))
                    .border(1.dp, indicatorColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Removed fixed height from the Row and Card to allow text wrapping
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 18.sp, // Better readability for multi-line text
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun WelcomeCardAdmin(adminName: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome, $adminName!",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    }
}
@Composable
fun NotificationsAlertHeaderAdmin(onAddBusClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notifications & Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedButton(onClick = onAddBusClick) {
            Text("Add Bus")
        }
    }
    NotificationItemScreen(
        initial = "S",
        message = "School closed on Friday",
        indicatorColor = BusMateOrange
    )
}
@Composable
fun SOSObserver(
    userId: String,                     // 🔥 ADD THIS
    userRole: String?,
    children: Map<String, ChildModel>?,
    onSOSReceived: (String, String) -> Unit

)
{
    val context = LocalContext.current

    if (userRole == null || userRole.lowercase() == "driver") return

    DisposableEffect(userId, userRole) {

        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance()
            .getReference("notifications")

        // 🔥 PER-USER last seen time
        val lastSeenTime = SOSPrefs.getLastSeen(context, userId)

        // 🔥 Listen ONLY to unseen SOS for THIS USER
        val query = db.orderByChild("timestamp")
            .startAt(lastSeenTime.toDouble() + 1)

        val listener = object : com.google.firebase.database.ChildEventListener {

            override fun onChildAdded(
                snapshot: com.google.firebase.database.DataSnapshot,
                previousChildName: String?
            ) {
                val timestamp =
                    snapshot.child("timestamp").getValue(Long::class.java) ?: return

                val alertRouteId =
                    snapshot.child("routeId").getValue(String::class.java)

                val busNo =
                    snapshot.child("busNumber").getValue(String::class.java) ?: "N/A"

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
                // ✅ Mark this SOS as seen FOR THIS USER ONLY
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
        onDismissRequest = {}, // ❌ prevent outside dismiss
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






















