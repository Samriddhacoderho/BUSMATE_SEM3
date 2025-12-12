package com.example.busmate.view.dashboard

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateGreen
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.view.AddChildActivity

@Composable
fun HomeScreen() {

    val context = LocalContext.current
    val activity = context as Activity

    var model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    val navigateToAddChild: () -> Unit = {
        val intent = Intent(context, AddChildActivity::class.java)
        context.startActivity(intent)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background   // ✔ Dark mode support
    ) { paddingValues ->

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {

                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver")
                    WelcomeCardScreen(model?.firstName + " " + model?.lastName)
                else
                    WelcomeCardAdmin(model?.firstName + " " + model?.lastName)

                MyChildrenHeaderScreen(model, onAddChildClick = navigateToAddChild)

                ChildTrackingCardScreen(
                    childName =
                        if (model?.typeofUser == "Parent") "Swikrit Ghimire"
                        else if (model?.typeofUser == "Driver") "Bus No: 1522"
                        else "Harwinder Singh",

                    statusText =
                        if (model?.typeofUser == "Parent") "Reached School"
                        else if (model?.typeofUser == "Driver") "Duty Completed"
                        else "Reached School",

                    subText =
                        if (model?.typeofUser == "Parent") "Bus No: 1511\n2 min ago"
                        else if (model?.typeofUser == "Driver") "Helper Name: Sandip"
                        else "Bus No: 1511\n9812668800",

                    statusColor = BusMateGreen,
                    imageResource =
                        if (model?.typeofUser == "Parent") R.drawable.boy
                        else if (model?.typeofUser == "Driver") R.drawable.schoolbus
                        else R.drawable.driver,

                    mapImageResource = R.drawable.school
                )

                ChildTrackingCardScreen(
                    childName =
                        if (model?.typeofUser == "Parent") "Shahana Katwal"
                        else if (model?.typeofUser == "Driver") "Bus No: 1543"
                        else "Ramesh Pathak",

                    statusText =
                        if (model?.typeofUser == "Parent") "In Bus"
                        else if (model?.typeofUser == "Driver") "Duty on 2:00 PM"
                        else "Driving",

                    subText =
                        if (model?.typeofUser == "Parent") "Bus No: 1533\n8 min ago"
                        else if (model?.typeofUser == "Driver") "Helper Name: Raju"
                        else "Bus No: 1533\n9800112236",

                    statusColor = BusMateOrange,
                    imageResource =
                        if (model?.typeofUser == "Parent") R.drawable.girl
                        else if (model?.typeofUser == "Driver") R.drawable.schoolbus
                        else R.drawable.driver,

                    mapImageResource = R.drawable.map
                )

                if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver")
                    NotificationsAlertHeaderScreen()
                else
                    NotificationsAlertHeaderAdmin()
            }
        }
    }
}

@Composable
fun WelcomeCardScreen(parentName: String?) {
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
fun NotificationsAlertHeaderAdmin() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 5.dp),
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
            onClick = {},
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary // ✔
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(35.dp)
        ) {

            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Create Notification", fontSize = 14.sp)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    NotificationItemScreen(
        initial = "S",
        message = "School closed on Friday",
        indicatorColor = BusMateOrange
    )
}
