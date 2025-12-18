package com.example.busmate.view.dashboard

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.busmate.model.ChildModel
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateGreen
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.view.AddChildActivity
import com.example.busmate.view.BusScreen
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen() {

    val context = LocalContext.current
    val activity = context as Activity

    var model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    val navigateToAddChild: () -> Unit = {
        context.startActivity(Intent(context, AddChildActivity::class.java))
    }

    val navigateToAddBus: () -> Unit = {
        context.startActivity(Intent(context, BusScreen::class.java))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // 🔹 HEADER
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

            // 🔹 CHILD LIST (DYNAMIC)
            if (model?.typeofUser == "Parent") {

                val childrenList: List<ChildModel> =
                    model?.children?.values?.toList().orEmpty()

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
                            mapImageResource = R.drawable.map
                        )
                    }
                }
            }

            // 🔹 FOOTER
            item {
                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                    NotificationsAlertHeaderScreen()
                } else {
                    NotificationsAlertHeaderAdmin(onAddBusClick = navigateToAddBus)
                }
            }
        }
    }
}

@Composable
fun WelcomeCardScreen(parentName: String?,model: UserModel?) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary) // ✔
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome, $parentName!",
            color = MaterialTheme.colorScheme.onPrimary, // ✔
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        Row(
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant) // ✔
                .height(40.dp)
        ) {
            Row(
                Modifier
                    .fillMaxHeight()
                    .weight(0.5f)
                    .background(BusMateOrange),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text("School",
                    color = MaterialTheme.colorScheme.onPrimary, // ✔
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }

            Row(
                Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary // ✔
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "Tracking Live",
                    color = MaterialTheme.colorScheme.onSurface, // ✔
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
            text =
                if (model?.typeofUser == "Parent") "My Children"
                else if (model?.typeofUser == "Driver") "My Duties"
                else "View Buses",

            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground // ✔
        )

        if (model?.typeofUser == "Parent")
            OutlinedButton(
                onClick = onAddChildClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary // ✔
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(35.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Child",
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Child", fontSize = 14.sp)
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
    mapImageResource: Int
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // ✔
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Row(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                Modifier.weight(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(imageResource),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape) // ✔
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {

                    Text(
                        text = childName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface // ✔
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = statusText,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // ✔
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Row(Modifier.weight(0.2f)) {
                Image(
                    painter = painterResource(id = mapImageResource),
                    contentDescription = "Map / school icon",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
fun NotificationsAlertHeaderScreen() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 5.dp)
    ) {
        Text(
            text = "Notifications & Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground // ✔
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    NotificationItemScreen(
        initial = "S",
        message = "School closed on Friday",
        indicatorColor = BusMateOrange
    )
}

@Composable
fun NotificationItemScreen(initial: String, message: String, indicatorColor: Color) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // ✔
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(indicatorColor.copy(alpha = 0.1f))
                        .border(1.dp, indicatorColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = indicatorColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = message,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface // ✔
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.outline_arrow_forward_ios_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary // ✔
            )
        }
    }
}

@Composable
fun WelcomeCardAdmin(adminName: String?) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary) // ✔
            .padding(16.dp)
    ) {

        Text(
            text = "Welcome, $adminName!",
            color = MaterialTheme.colorScheme.onPrimary, // ✔
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        Row(
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant) // ✔
                .height(40.dp)
        ) {

            Row(
                Modifier
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
                    color = MaterialTheme.colorScheme.onPrimary, // ✔
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Row(
                Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text(
                    "Tracking Live",
                    color = MaterialTheme.colorScheme.onSurface, // ✔
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
fun NotificationsAlertHeaderAdmin(onAddBusClick: () -> Unit){
    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "Notifications & Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground // ✔
        )

        OutlinedButton(
            onClick = onAddBusClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary // ✔
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(35.dp)
        ) {

            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Bus", fontSize = 14.sp)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    NotificationItemScreen(
        initial = "S",
        message = "School closed on Friday",
        indicatorColor = BusMateOrange
    )
}
