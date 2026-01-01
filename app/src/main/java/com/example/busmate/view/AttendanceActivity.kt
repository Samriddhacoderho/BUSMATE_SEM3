package com.example.busmate.view

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.model.ChildModel
import com.example.busmate.viewmodel.AttendanceViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.Executors

class AttendanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val driverUid = intent.getStringExtra("EXTRA_DRIVER_UID") ?: ""

        setContent {
            AttendanceScreen(
                driverUid = driverUid,
                onBackClick = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    driverUid: String,
    onBackClick: () -> Unit,
    viewModel: AttendanceViewModel = viewModel()
) {
    val children by viewModel.children.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val checkedStudents = remember { mutableStateListOf<ChildModel>() }
    var isScannerOpen by remember { mutableStateOf(false) }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) isScannerOpen = true
        else Toast.makeText(context, "Camera permission is required to scan IDs", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(driverUid) {
        if (driverUid.isNotEmpty()) {
            viewModel.loadAttendanceList(driverUid)
        }
    }
    LaunchedEffect(children) {
        if (children.isNotEmpty()) {
            checkedStudents.clear() // Clear old state
            children.forEach { child ->
                // Check if this child was marked present in the pre-loaded map
                if (viewModel.getInitialStatus(child.studentId)) {
                    checkedStudents.add(child)
                }
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // QR Scanner Toggle Button
                    IconButton(onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = {
                        viewModel.submitAttendance(driverUid, checkedStudents.toList()) { success ->
                            if (success) {
                                Toast.makeText(context, "Attendance submitted!", Toast.LENGTH_LONG).show()
                                onBackClick()
                            } else {
                                Toast.makeText(context, "Submission failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Attendance (${checkedStudents.size})", fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(children) { child ->
                        val isChecked = checkedStudents.any { it.studentId == child.studentId }
                        StudentAttendanceCard(
                            child = child,
                            isSelected = isChecked,
                            onCheckChanged = { checked ->
                                if (checked) {
                                    if (!checkedStudents.any { it.studentId == child.studentId }) {
                                        checkedStudents.add(child)
                                    }
                                } else {
                                    checkedStudents.removeAll { it.studentId == child.studentId }
                                }
                            }
                        )
                    }
                }
            }

            // QR Scanner Overlay
            if (isScannerOpen) {
                QRScannerOverlay(
                    onDismiss = { isScannerOpen = false },
                    onIdScanned = { scannedId ->
                        val student = children.find { it.studentId == scannedId }
                        if (student != null) {
                            if (!checkedStudents.any { it.studentId == scannedId }) {
                                checkedStudents.add(student)
                                Toast.makeText(context, "Scanned: ${student.firstName}", Toast.LENGTH_SHORT).show()
                            }
                            isScannerOpen = false // Close after scan
                        } else {
                            Toast.makeText(context, "Student ID $scannedId not found on this route!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerOverlay(onDismiss: () -> Unit, onIdScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Background dimming and scanner dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Scan Student ID Card") },
        text = {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val barcodeScanner = BarcodeScanning.getClient()
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                barcode.rawValue?.let { rawValue ->
                                                    try {
                                                        val json = JSONObject(rawValue)
                                                        val id = json.getString("studentId")
                                                        onIdScanned(id)
                                                    } catch (e: Exception) {
                                                        Log.e("Scanner", "Invalid JSON QR")
                                                    }
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("Scanner", "Camera binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}

@Composable
fun StudentAttendanceCard(
    child: ChildModel,
    isSelected: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = child.firstName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("${child.firstName} ${child.lastName}", fontWeight = FontWeight.Bold)
                Text("ID: ${child.studentId}", fontSize = 12.sp, color = Color.Gray)
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onCheckChanged(it) }
            )
        }
    }
}