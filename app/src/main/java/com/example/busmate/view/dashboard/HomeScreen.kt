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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun HomeScreen(
    children: List<ChildModel> = emptyList(),
    onOpenLiveLocation: (busRouteId: String) -> Unit   // ✅ NEW
)
 {
    val busViewModel = remember { BusViewModel(BusRepositoryImpl()) }
    val context = LocalContext.current
    val activity = context as Activity

    var model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

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
                     // USE bus.uid because that is the name of the folder in Firebase
                     putExtra("EXTRA_BUS_ID", bus.uid)
                 }
                 context.startActivity(intent)
             } else {
                 Toast.makeText(context, "Bus not found", Toast.LENGTH_SHORT).show()
             }
         }
     }

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
                            imageResource = R.drawable.boy,
                            mapImageResource = R.drawable.map,
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

                                    val isDriverMissing = bus.driver == null
                                    val isTripRunning = bus.speed > 1.0

                                    when {
                                        isDriverMissing -> {
                                            Toast.makeText(
                                                context,
                                                "Driver not assigned yet",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        !isTripRunning -> {
                                            Toast.makeText(
                                                context,
                                                "Trip has not started yet",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        else -> {
                                            onOpenLiveLocation(bus.uid)
                                        }
                                    }
                                }

                            }

                        )
                    }
                }
            }

            // FOOTER (NOTIFICATIONS)
            item {
                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                    NotificationsAlertHeaderScreen()
                } else {
                    NotificationsAlertHeaderAdmin(onAddBusClick = navigateToAddBus)
                }
            }

            // DRIVER → GO TO TRIP
            if (model?.typeofUser == "Driver") {
                item {
                    Spacer(modifier = Modifier.height(30.dp))

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
    imageResource: Int,
    mapImageResource: Int,
    onClick: () -> Unit        // ✅ ADD THIS
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
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = childName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = statusText,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Image(
                painter = painterResource(id = mapImageResource),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
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
            .padding(16.dp)
            .height(70.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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

            Text(message, fontSize = 14.sp)
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
