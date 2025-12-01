package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.LightGrayBackground
import com.example.busmate.ui.theme.PrimaryDarkTeal

class SupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrievanceFormUI()

        }
    }
}
@Composable
fun GrievanceFormUI() {
    var titleText by remember { mutableStateOf("") }
    var explainText by remember { mutableStateOf("") }

    // Custom TextField colors to match the image's light gray background and rounded corners
    val customTextFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedContainerColor = LightGrayBackground,
        unfocusedContainerColor = LightGrayBackground,
        disabledContainerColor = LightGrayBackground,
        focusedPlaceholderColor = Color.LightGray,
        unfocusedPlaceholderColor = Color.LightGray,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header Text ---
            Text(
                text = "If you are experiencing any issues, please\nlet us know. We will try to solve them as\nsoon as possible.",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
            )

            // --- Title Field ---
            Text(
                text = "Title",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            TextField(
                value = titleText,
                onValueChange = { titleText = it },
                placeholder = { Text("Add you grievance title here") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(10.dp),
                colors = customTextFieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Explain the problem Field ---
            Text(
                text = "Explain the problem",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
//testing ui themes
            TextField(
                value = explainText,
                onValueChange = { explainText = it },
                placeholder = { Text("Type your query here") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // Height to accommodate multi-line text
                shape = RoundedCornerShape(10.dp),
                colors = customTextFieldColors,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- SUBMIT Button ---
            Button(
                onClick = { /* Handle submission */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BusMateBlue,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
            ) {
                Text(
                    text = "SUBMIT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- Contact Number ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "You can contact us on this number ",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "1234567892",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                        //testing colors
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGrievanceFormUI() {
    GrievanceFormUI()
}



