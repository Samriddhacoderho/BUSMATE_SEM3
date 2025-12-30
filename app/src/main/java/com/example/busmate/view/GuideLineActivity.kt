package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.GuideLinesImpl
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.viewmodel.GuideLineViewModel
import com.example.busmate.viewmodel.UserViewModel

class GuideLineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //kam baki aba roll anusar dekhaune UI.
        }
    }
}

@Composable
fun AdminGuidelineScreen() {
    val viewModel= remember{ GuideLineViewModel(GuideLinesImpl()) }
    val currentGuidelines by viewModel.guidelines.collectAsState()
    var textState by remember { mutableStateOf("") }

    // Load existing data on start
    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    // Sync local state when data loads
    LaunchedEffect(currentGuidelines) { textState = currentGuidelines }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Safety Guidelines") },
            modifier = Modifier.fillMaxWidth().height(250.dp),
            minLines = 10
        )
        Button(
            onClick = { viewModel.postGuidelines(textState) },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Post/Update Guidelines")
        }
    }
}

@Composable
fun DriverGuidelineScreen() {
    val viewModel= remember{ GuideLineViewModel(GuideLinesImpl()) }
    val guidelines by viewModel.guidelines.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadGuidelines() }

    Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Safety Guidelines", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = guidelines.ifEmpty { "No guidelines posted yet." })
        }
    }
}