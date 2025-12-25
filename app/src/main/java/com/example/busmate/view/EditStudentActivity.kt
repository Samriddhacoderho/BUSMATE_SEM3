package com.example.busmate.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.ChildViewModel
import com.google.firebase.database.FirebaseDatabase

class EditStudentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Get the student object passed from the Search screen
        // Note: use 'getParcelableExtra' for your ChildModel
        val child = intent.getParcelableExtra<ChildModel>("student_data")

        // 2. Initialize the ViewModel
        val viewModel = ChildViewModel(ChildRepositoryImpl())
        setContent {
            BusMateTheme {
                if (child != null) {
                    EditStudentScreen(child, viewModel, onBack = { finish() })
                } else {
                    // Fallback if data is missing
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: Student data not found")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStudentScreen(child: ChildModel, viewModel: ChildViewModel, onBack: () -> Unit) {
    val busMateBlue = Color(0xFF2567E8)
    val context = LocalContext.current

    var parentName by remember { mutableStateOf("Fetching...") }
    var parentPhone by remember { mutableStateOf("Fetching...") }

    // Local state for editable fields
    var firstName by remember { mutableStateOf(child.firstName) }
    var lastName by remember { mutableStateOf(child.lastName) }
    var routeId by remember { mutableStateOf(child.busRouteId) }
    var pickUp by remember { mutableStateOf(child.pickUpLocation) }
    var dropOff by remember { mutableStateOf(child.dropOffLocation) }

    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(child.studentId) {
        val db = FirebaseDatabase.getInstance()
        // Find parent UID from index
        db.getReference("studentIdIndex").child(child.studentId).get().addOnSuccessListener { index ->
            val parentUid = index.child("parentUid").getValue(String::class.java)
            if (parentUid != null) {
                // Fetch parent details from users node
                db.getReference("users").child(parentUid).get().addOnSuccessListener { user ->
                    val fName = user.child("firstName").value ?: ""
                    val lName = user.child("lastName").value ?: ""
                    parentName = "$fName $lName"
                    parentPhone = user.child("phone").value.toString()
                }
            } else {
                parentName = "Not Found"
                parentPhone = "Not Found"
            }
        }
    }

    // Show toast and go back when update is successful
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Student Updated!", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Student Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = busMateBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Parent Contact Information", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            ReadOnlyField(label = "Parent Name", value = parentName, icon = Icons.Default.Person)
            ReadOnlyField(label = "Parent Phone", value = parentPhone, icon = Icons.Default.Phone)

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), thickness = 1.dp, color = Color.LightGray)

            Text("Updating ID: ${child.studentId}", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))

            // Text Fields
            EditField("First Name", firstName) { firstName = it }
            EditField("Last Name", lastName) { lastName = it }
            EditField("Route ID", routeId) { routeId = it }
            EditField("Pick-up Point", pickUp) { pickUp = it }
            EditField("Drop-off Point", dropOff) { dropOff = it }

            Spacer(modifier = Modifier.height(30.dp))

            if (message.isNotEmpty() && !isSuccess) {
                Text(message, color = Color.Red, modifier = Modifier.padding(bottom = 10.dp))
            }

            Button(
                onClick = {
                    val updatedChild = child.copy(
                        firstName = firstName,
                        lastName = lastName,
                        busRouteId = routeId,
                        pickUpLocation = pickUp,
                        dropOffLocation = dropOff
                    )
                    // You need to implement updateChild in your ViewModel/Repo
                    viewModel.updateChild(updatedChild)
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue)
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ReadOnlyField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true, // This makes it non-editable
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(icon, null, tint = Color.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE9ECEF), // Greyed out look
            unfocusedContainerColor = Color(0xFFE9ECEF),
            disabledTextColor = Color.Black
        )
    )
}
@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    )
}
//testing parent information in the child information