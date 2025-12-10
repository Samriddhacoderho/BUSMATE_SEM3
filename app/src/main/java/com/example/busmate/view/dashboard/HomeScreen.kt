package com.example.busmate.view.dashboard

import android.app.Activity
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

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val activity = context as Activity

    var model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {

                WelcomeCardScreen(model?.firstName + " " + model?.lastName, model)

                MyChildrenHeaderScreen(model)

                ChildTrackingCardScreen(
                    childName = if (model?.typeofUser == "Parent") "Swikrit Ghimire" else "Bus No: 1522",
                    statusText = if (model?.typeofUser == "Parent") "Reached School" else "Duty Completed",
                    subText = if (model?.typeofUser == "Parent") "Bus No: 1511\n2 min ago" else "Helper Name:Sandip",
                    statusColor = BusMateGreen,
                    imageResource = if (model?.typeofUser == "Parent") R.drawable.boy else R.drawable.schoolbus,
                    mapImageResource = R.drawable.school
                )

                ChildTrackingCardScreen(
                    childName = if (model?.typeofUser == "Parent") "Shahana Katwal" else "Bus No: 1543",
                    statusText = if (model?.typeofUser == "Parent") "In Bus" else "Duty on 2:00 PM",
                    subText = if (model?.typeofUser == "Parent") "Bus No: 1533\n8 min ago" else "Helper Name:Raju",
                    statusColor = BusMateOrange,
                    imageResource = if (model?.typeofUser == "Parent") R.drawable.girl else R.drawable.schoolbus,
                    mapImageResource = R.drawable.map
                )

                NotificationsAlertHeaderScreen()
            }
        }
    }
}


@Composable
fun WelcomeCardScreen(parentName: String?, model: UserModel?) {

    Column(
        Modifier
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
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
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
                Icon(
                    Icons.Filled.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "School",
                    color = MaterialTheme.colorScheme.onPrimary,
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
                    tint = MaterialTheme.colorScheme.onSurface
                )
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
fun MyChildrenHeaderScreen(model: UserModel?) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = if (model?.typeofUser == "Parent") "My Children" else "My Duties",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (model?.typeofUser == "Parent")
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = ButtonDefaults.outlinedButtonBorder,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(35.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Child",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    "Add Child",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
    }
}


@Composable
fun ChildTrackingCardScreen(
    childName: String,
    statusText: String,
    subText: String,
    statusColor: androidx.compose.ui.graphics.Color,
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
            containerColor = MaterialTheme.colorScheme.surface
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
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {

                    Text(
                        text = childName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Row(Modifier.weight(0.2f)) {
                Image(
                    painter = painterResource(id = mapImageResource),
                    contentDescription = "Map or School Visual",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {}
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
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    NotificationItemScreen(
        initial = "S",
        message = "School Closed on Friday",
        indicatorColor = BusMateOrange
    )
}


@Composable
fun NotificationItemScreen(
    initial: String,
    message: String,
    indicatorColor: androidx.compose.ui.graphics.Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        .background(
                            indicatorColor.copy(alpha = 0.1f)
                        )
                        .border(
                            1.dp,
                            indicatorColor.copy(alpha = 0.4f),
                            CircleShape
                        ),
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.outline_arrow_forward_ios_24),
                contentDescription = "View notification",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
