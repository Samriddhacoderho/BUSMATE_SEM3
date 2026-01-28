package com.example.busmate.view.driver

import android.Manifest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.busmate.model.ChildModel
import com.example.busmate.viewmodel.AttendanceViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.Executors

/* ---------- COLORS (ADMIN ATTENDANCE HISTORY PALETTE) ---------- */
private val PrimaryBlue = Color(0xFF2567E8)
private val SoftBlueBg = Color(0xFFF6F8FE)

class AttendanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val driverUid = intent.getStringExtra("EXTRA_DRIVER_UID") ?: ""

        setContent {
            // ---- DARK MODE PREF OBSERVER (UNCHANGED LOGIC) ----
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeChanged by remember {
                mutableIntStateOf(sharedPrefs.getInt("dark_mode_pref", 0))
            }

            DisposableEffect(Unit) {
                val listener =
                    android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                        if (key == "dark_mode_pref") {
                            themeChanged = sharedPrefs.getInt("dark_mode_pref", 0)
                        }
                    }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)

                onDispose {
                    sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            // ---- FORCE RECOMPOSITION ON THEME CHANGE ----
            key(themeChanged) {
                com.example.busmate.ui.theme.BusMateTheme {
                    AttendanceScreen(
                        driverUid = driverUid,
                        onBackClick = { finish() }
                    )
                }
            }
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

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) isScannerOpen = true
        else Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(driverUid) {
        if (driverUid.isNotEmpty()) {
            viewModel.loadAttendanceList(driverUid)
        }
    }

    LaunchedEffect(children) {
        checkedStudents.clear()
        children.forEach { child ->
            if (viewModel.getInitialStatus(child.studentId)) {
                checkedStudents.add(child)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Daily Attendance", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit (${checkedStudents.size})", fontSize = 16.sp)
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            if (isLoading) {
                CircularProgressIndicator(
                    color = PrimaryBlue,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(children) { child ->
                            val isChecked =
                                checkedStudents.any { it.studentId == child.studentId }
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
            }

            if (isScannerOpen) {
                QRScannerOverlay(
                    onDismiss = { isScannerOpen = false },
                    onIdScanned = { scannedId ->
                        val student = children.find { it.studentId == scannedId }
                        if (student != null && !checkedStudents.any { it.studentId == scannedId }) {
                            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                                .startTone(ToneGenerator.TONE_PROP_ACK, 150)
                            checkedStudents.add(student)
                            Toast.makeText(
                                context,
                                "Scanned: ${student.firstName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context, "Student not found", Toast.LENGTH_SHORT).show()
                        }
                        isScannerOpen = false
                    }
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerOverlay(
    onDismiss: () -> Unit,
    onIdScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Scan Student QR", fontWeight = FontWeight.Bold)
        },
        text = {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val providerFuture = ProcessCameraProvider.getInstance(ctx)

                        providerFuture.addListener({
                            val provider = providerFuture.get()

                            val preview = Preview.Builder().build().apply {
                                setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val scanner = BarcodeScanning.getClient()
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analysis.setAnalyzer(executor) { proxy ->
                                proxy.image?.let { mediaImage ->
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        proxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(image)
                                        .addOnSuccessListener { codes ->
                                            codes.forEach { code ->
                                                code.rawValue?.let { raw ->
                                                    try {
                                                        val id =
                                                            JSONObject(raw).getString("studentId")
                                                        onIdScanned(id)
                                                    } catch (_: Exception) {
                                                    }
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { proxy.close() }
                                }
                            }

                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                PrimaryBlue.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                if (!child.profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = child.profileImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = child.firstName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    "${child.firstName} ${child.lastName}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "ID: ${child.studentId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryBlue
                )
            )
        }
    }
}
