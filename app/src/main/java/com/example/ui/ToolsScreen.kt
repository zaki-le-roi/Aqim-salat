package com.example.ui

import com.example.data.HisnAlMuslimData
import com.example.data.HisnDua

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdhkarData
import com.example.data.NamesOfAllahData
import com.example.data.TasbihCounter
import com.example.data.FavoriteMosque
import com.example.data.Khatmah
import android.net.Uri
import android.content.Context
import android.content.Intent
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.annotation.SuppressLint
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.util.Locale

// WebView and GPS permission launchers
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: AppViewModel,
    lang: String,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf<String?>(null) } // "TASBIH", "QIBLA", "ADHKAR", "NAMES"

    AnimatedContent(
        targetState = activeTool,
        transitionSpec = {
            slideInHorizontally { width -> if (targetState != null) width else -width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> if (targetState == null) width else -width } + fadeOut()
        },
        label = "ToolTransition"
    ) { tool ->
        if (tool == null) {
            // --- Primary Tools Panel Browser ---
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = Translations.get("tools", lang),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Grid/List of Premium Tools
                val toolItems = listOf(
                    Triple("TASBIH", Translations.get("tasbih", lang), Icons.Filled.AddCircle),
                    Triple("QIBLA", Translations.get("qibla", lang), Icons.Filled.Explore),
                    Triple("ADHKAR", Translations.get("adhkar", lang), Icons.Filled.SelfImprovement),
                    Triple("NAMES", Translations.get("names_of_allah", lang), Icons.Filled.AutoAwesome),
                    Triple("MOSQUES", if (lang == "ar") "المساجد القريبة" else if (lang == "tr") "Yakındaki Camiler" else "Nearby Mosques", Icons.Filled.LocationOn),
                    Triple("KHATMAH", if (lang == "ar") "خاتمة القرآن" else if (lang == "tr") "Kuran Hatim" else "Ramadan Khatmah", Icons.Filled.LibraryBooks),
                    Triple("HISN_AL_MUSLIM", if (lang == "ar") "حصن المسلم" else if (lang == "tr") "Hisnul Muslim" else "Hisn Al Muslim", Icons.Filled.Book),
                )

                items(toolItems) { (toolKey, title, icon) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeTool = toolKey }
                            .testTag("tool_card_$toolKey"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = title, tint = Color(0xFFD4AF37), modifier = Modifier.size(26.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Icon(Icons.Filled.ArrowForward, contentDescription = "فتح", tint = Color(0xFFD4AF37))
                        }
                    }
                }
            }
        } else {
            // --- Render Selected Active Tool ---
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (tool) {
                                    "TASBIH" -> Translations.get("tasbih", lang)
                                    "QIBLA" -> Translations.get("qibla", lang)
                                    "ADHKAR" -> Translations.get("adhkar", lang)
                                    "NAMES" -> Translations.get("names_of_allah", lang)
                                    "MOSQUES" -> if (lang == "ar") "المساجد القريبة" else if (lang == "tr") "Yakındaki Camiler" else "Nearby Mosques"
                                    "KHATMAH" -> if (lang == "ar") "خاتمة القرآن الكريم" else if (lang == "tr") "Kuran Hatim" else "Ramadan Khatmah"
                                    "HISN_AL_MUSLIM" -> if (lang == "ar") "حصن المسلم" else if (lang == "tr") "Hisnul Muslim" else "Hisn Al Muslim"
                                    else -> ""
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { activeTool = null }, modifier = Modifier.testTag("tool_back_button")) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (tool) {
                        "TASBIH" -> TasbihTool(viewModel, lang)
                        "QIBLA" -> QiblaTool(viewModel, lang)
                        "ADHKAR" -> AdhkarTool(viewModel, lang)
                        "NAMES" -> NamesOfAllahTool(viewModel, lang)
                        "MOSQUES" -> MosquesTool(viewModel, lang)
                        "KHATMAH" -> KhatmahTool(viewModel, lang)
                        "HISN_AL_MUSLIM" -> HisnAlMuslimTool(viewModel, lang)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. TASBIH TOOL (Digital Tasbih Counter)
// ==========================================
// ==========================================
// 1. TASBIH TOOL (Digital Finger Ring Tally Counter)
// ==========================================
@Composable
fun TasbihTool(viewModel: AppViewModel, lang: String) {
    val counters by viewModel.tasbihCounters.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    var selectedCounter by remember { mutableStateOf<TasbihCounter?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isBacklightOn by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newGoalStr by remember { mutableStateOf("33") }

    LaunchedEffect(counters) {
        if (counters.isNotEmpty()) {
            if (selectedCounter == null || counters.none { it.id == selectedCounter?.id }) {
                selectedCounter = counters.first()
            } else {
                selectedCounter = counters.find { it.id == selectedCounter?.id }
            }
        }
    }

    val propheticPresets = remember {
        listOf(
            "سُبْحَانَ اللَّهِ" to 33,
            "الْحَمْدُ لِلَّهِ" to 33,
            "اللَّهُ أَكْبَرُ" to 34,
            "لَا إِلَهَ إِلَّا اللَّهُ" to 100,
            "أَسْتَغْفِرُ اللَّهَ" to 100,
            "الصَّلَاةُ عَلَى النَّبِيِّ" to 100,
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ" to 100
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Dhikr Preset Pills Row
        Text(
            text = "اختر صيغة الذكر:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            propheticPresets.forEach { (dhikrName, goal) ->
                val isSelected = selectedCounter?.name?.trim() == dhikrName.trim()
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val existing = counters.find { it.name.trim() == dhikrName.trim() }
                        if (existing != null) {
                            selectedCounter = existing
                        } else {
                            viewModel.createTasbih(dhikrName, goal)
                        }
                    },
                    label = { Text(dhikrName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0F4C3A),
                        selectedLabelColor = Color.White
                    )
                )
            }
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("tasbih_custom_add_chip")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة ذكر مخصص", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الاهتزاز عند الضغط",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (hapticsEnabled) "مفعّل" else "متوقف",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { viewModel.setHapticsEnabled(it) },
                    modifier = Modifier.testTag("tasbih_haptics_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedCounter != null) {
            val counter = selectedCounter!!
            val laps = if (counter.goal > 0) counter.count / counter.goal else 0
            val formattedCount = String.format(java.util.Locale.US, "%05d", counter.count)

            // --- Physical Finger Tally Counter Body Simulation ---
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(390.dp),
                contentAlignment = Alignment.Center
            ) {
                // Silicone Strap Loop Visual at Top & Bottom
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(380.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF1E2824))
                )

                // Main Ergonomic Counter Shell (Pebble Case)
                Card(
                    modifier = Modifier
                        .width(240.dp)
                        .height(340.dp),
                    shape = RoundedCornerShape(70.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D3B2E)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF135341),
                                        Color(0xFF0D3B2E),
                                        Color(0xFF07231B)
                                    )
                                )
                            )
                            .border(2.dp, Color(0xFF2E7D63).copy(alpha = 0.5f), RoundedCornerShape(70.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Digital LCD Window
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(82.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isBacklightOn) Color(0xFF8EEAD3) else Color(0xFF9EAA96))
                                .border(3.dp, Color(0xFF1A2722), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top Micro Indicators
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الدورات: $laps",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBacklightOn) Color(0xFF063326) else Color(0xFF283625)
                                    )
                                    Text(
                                        text = "الهدف: ${counter.goal}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBacklightOn) Color(0xFF063326) else Color(0xFF283625)
                                    )
                                }

                                // 5-Digit Segment Display Text
                                Text(
                                    text = formattedCount,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isBacklightOn) Color(0xFF03241B) else Color(0xFF1C271A),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 4.sp,
                                    modifier = Modifier.fillMaxWidth().testTag("tasbih_digital_display")
                                )
                            }
                        }

                        // Dhikr Name Display on Device
                        Text(
                            text = counter.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF3E7C4),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        // Auxiliary Control Buttons: Undo (تراجع) & Reset (تصفير) & LED
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // UNDO Button (تراجع)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFF4A5568), Color(0xFF2D3748))
                                            )
                                        )
                                        .border(1.dp, Color(0xFF718096), CircleShape)
                                        .clickable {
                                            viewModel.decrementTasbih(counter)
                                        }
                                        .testTag("tasbih_undo_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Undo,
                                        contentDescription = "تراجع",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text("تراجع", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }

                            // LED Backlight Toggle
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isBacklightOn) Color(0xFF8EEAD3) else Color(0xFF2D3748))
                                        .border(1.dp, Color(0xFF718096), CircleShape)
                                        .clickable { isBacklightOn = !isBacklightOn }
                                        .testTag("tasbih_led_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lightbulb,
                                        contentDescription = "إضاءة",
                                        tint = if (isBacklightOn) Color(0xFF063326) else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text("LED", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }

                            // RESET Button (تصفير)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFFE53E3E), Color(0xFF9B2C2C))
                                            )
                                        )
                                        .border(1.dp, Color(0xFFFEB2B2), CircleShape)
                                        .clickable {
                                            viewModel.resetTasbih(counter)
                                        }
                                        .testTag("tasbih_reset_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "تصفير",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text("تصفير", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        // Large Main Count Press Button (زر العد الكبير)
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .graphicsLayer {
                                    scaleX = if (isPressed) 0.94f else 1.0f
                                    scaleY = if (isPressed) 0.94f else 1.0f
                                }
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFD4AF37),
                                            Color(0xFFAA8014),
                                            Color(0xFF735406)
                                        )
                                    )
                                )
                                .border(3.dp, Color(0xFFF9E79F), CircleShape)
                                .clickable {
                                    isPressed = true
                                    viewModel.incrementTasbih(counter)
                                }
                                .testTag("tasbih_click_zone"),
                            contentAlignment = Alignment.Center
                        ) {
                            LaunchedEffect(isPressed) {
                                if (isPressed) {
                                    kotlinx.coroutines.delay(100)
                                    isPressed = false
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = "تسبيح",
                                    tint = Color(0xFF1E2824),
                                    modifier = Modifier.size(34.dp)
                                )
                                Text(
                                    text = "اضغط",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E2824)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Loading or fallback state
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة ذكر أو تسبيح مخصص", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("صيغة الذكر") },
                        placeholder = { Text("مثال: لا إله إلا أنت سبحانك") },
                        modifier = Modifier.fillMaxWidth().testTag("tasbih_input_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newGoalStr,
                        onValueChange = { newGoalStr = it },
                        label = { Text("الهدف (العدد المستهدف)") },
                        modifier = Modifier.fillMaxWidth().testTag("tasbih_input_goal"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = newGoalStr.toIntOrNull() ?: 33
                        if (newName.isNotBlank()) {
                            viewModel.createTasbih(newName.trim(), g)
                            newName = ""
                            newGoalStr = "33"
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("tasbih_confirm_save")
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// ==========================================
// 2. QIBLA COMPASS TOOL
// ==========================================
@Composable
fun QiblaTool(viewModel: AppViewModel, lang: String) {
    val qiblaAngle by viewModel.qiblaAngle.collectAsState()
    val distance by viewModel.distanceToKaaba.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()

    val context = LocalContext.current
    var deviceAzimuth by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(false) }

    DisposableEffect(context, latitude, longitude) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorAvailable = rotationSensor != null

        if (sensorManager == null || rotationSensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val orientation = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var magneticAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    magneticAzimuth = ((magneticAzimuth % 360f) + 360f) % 360f
                    // Qibla bearings are measured from true north; correct the magnetic sensor heading.
                    val declination = GeomagneticField(
                        latitude.toFloat(),
                        longitude.toFloat(),
                        0f,
                        System.currentTimeMillis()
                    ).declination
                    deviceAzimuth = ((magneticAzimuth + declination) % 360f + 360f) % 360f
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val compassRotation = ((qiblaAngle.toFloat() - deviceAzimuth) + 360f) % 360f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (sensorAvailable) Translations.get("qibla_desc", lang)
            else if (lang == "ar") "لا يتوفر مستشعر اتجاه الجهاز. ستظهر جهة القبلة محسوبة من موقعك دون تدوير حي مع حركة الهاتف."
            else "A device orientation sensor is unavailable. The Qibla bearing is calculated from your location without live phone rotation.",

            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Dynamic Compass Dial Canvas Layer
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(4.dp, Color(0xFFD4AF37), CircleShape)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Compass Frame
            // Point of Qibla is relative to phone's top (heading angle)
            // Simulating dial orientation at heading 0 pointing North up.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = compassRotation
                    },
                contentAlignment = Alignment.Center
            ) {
                // Compass markings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw Cardinal points
                    // Standard markings (N, E, S, W)
                }

                // Needle pointing directly to Kaaba
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = "مؤشر اتجاه القبلة",
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(0f) // standard icon points up
                )
            }

            // Beautiful Central Kaaba Badge Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.dp, Color(0xFFD4AF37), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "الكعبة المشرفة",
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Distance Indicator
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Translations.get("kaaba_distance", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = String.format(Locale.US, "%,.1f km", distance),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = String.format(Locale.US, "Qibla Heading: %.1f°", qiblaAngle),
                    fontSize = 11.sp,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 3. ADHKAR & INVOCATIONS RECITER
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarTool(viewModel: AppViewModel, lang: String) {
    var selectedCategory by remember { mutableStateOf("Morning") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("Morning", "Evening", "Sleep", "Travel", "Mosque", "Protection", "Food", "Healing")
    val categoryLabels = mapOf(
        "Morning" to if (lang == "ar") "أذكار الصباح" else "Morning",
        "Evening" to if (lang == "ar") "أذكار المساء" else "Evening",
        "Sleep" to if (lang == "ar") "أذكار النوم" else "Sleep",
        "Travel" to if (lang == "ar") "أذكار السفر" else "Travel",
        "Mosque" to if (lang == "ar") "أذكار المسجد" else "Mosque",
        "Protection" to if (lang == "ar") "أذكار الحفظ" else "Protection",
        "Food" to if (lang == "ar") "أذكار الطعام" else "Food",
        "Healing" to if (lang == "ar") "أدعية الشفاء" else "Healing"
    )

    // Dynamic reactive counters for interactive recitation
    val activeCounters = remember { mutableStateMapOf<String, Int>() }

    // Filter adhkar
    val filteredAdhkar = remember(selectedCategory, searchQuery) {
        AdhkarData.adhkar.filter {
            it.category == selectedCategory && (
                    searchQuery.isBlank() ||
                            it.text.contains(searchQuery) ||
                            it.translation.contains(searchQuery, ignoreCase = true)
                    )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Horizontal Scrollable Category row
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth().testTag("adhkar_category_row")
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    text = {
                        Text(
                            text = categoryLabels[cat] ?: cat,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Dhikr
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("adhkar_search_bar"),
            placeholder = { Text(Translations.get("search_dhikr", lang)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = if (lang == "ar") "بحث" else "Search") },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Adhkar Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredAdhkar, key = { it.id }) { dhikr ->
                val remainingCount = activeCounters.getOrPut(dhikr.id) { dhikr.countTarget }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dhikr_card_${dhikr.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (remainingCount == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dhikr.reference,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontWeight = FontWeight.SemiBold
                            )

                            // Interactive Repeat Counter Button
                            Button(
                                onClick = {
                                    if (remainingCount > 0) {
                                        activeCounters[dhikr.id] = remainingCount - 1
                                    } else {
                                        // Reset
                                        activeCounters[dhikr.id] = dhikr.countTarget
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (remainingCount == 0) Color(0xFF2E7D32) else Color(0xFFD4AF37)
                                ),
                                modifier = Modifier.testTag("dhikr_counter_${dhikr.id}")
                            ) {
                                Text(
                                    text = if (remainingCount == 0) { if (lang == "ar") "تم" else "Completed" } else "$remainingCount / ${dhikr.countTarget}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Arabic Script
                        Text(
                            text = dhikr.text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // English Translation
                        Text(
                            text = dhikr.translation,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. 99 NAMES OF ALLAH TOOL
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesOfAllahTool(viewModel: AppViewModel, lang: String) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredNames = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            NamesOfAllahData.names
        } else {
            NamesOfAllahData.names.filter {
                it.transliteration.contains(searchQuery, ignoreCase = true) ||
                        it.translation.contains(searchQuery, ignoreCase = true) ||
                        it.arabic.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Search
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp)
                .testTag("names_search_bar"),
            placeholder = { Text(if (lang == "ar") "ابحث في أسماء الله الحسنى..." else "Search Names...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "بحث") },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // 99 Names Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredNames) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.testTag("name_cell_${item.number}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.number.toString(),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.arabic,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.transliteration,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.translation,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. MOSQUES TOOL (Nearby Mosques Finder)
// ==========================================
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveGoogleMap(
    lat: Double,
    lng: Double,
    nearbyMosques: List<LocalMosque>,
    lang: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
            }
        },
        update = { webView ->
            val html = buildString {
                append("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                        <style>
                            html, body, #map { height: 100%; margin: 0; padding: 0; background-color: #121214; }
                            .leaflet-popup-content { font-family: sans-serif; font-size: 13px; line-height: 1.4; color: #ffffff !important; }
                            .leaflet-popup-content-wrapper { background: #1a1a1c !important; border: 1px solid #D4AF37; }
                            .leaflet-popup-tip { background: #1a1a1c !important; }
                            .leaflet-container { background: #121214; }
                        </style>
                    </head>
                    <body>
                        <div id="map"></div>
                        <script>
                            var map = L.map('map', { zoomControl: false }).setView([$lat, $lng], 14);
                            
                            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                                maxZoom: 19
                            }).addTo(map);
                            
                            L.control.zoom({ position: 'bottomright' }).addTo(map);
            
                            var userIcon = L.divIcon({
                                className: 'user-pin',
                                html: "<div style='background-color:#D4AF37; width:12px; height:12px; border:3px solid #ffffff; border-radius:50%; box-shadow: 0 0 10px #D4AF37;'></div>",
                                iconSize: [18, 18],
                                iconAnchor: [9, 9]
                            });
                            L.marker([$lat, $lng], { icon: userIcon }).addTo(map)
                                .bindPopup("<b>Your Active GPS</b>").openPopup();
                """)
                
                nearbyMosques.forEach { mosque ->
                    // Quote untrusted values as JavaScript literals and neutralize HTML script terminators.
                    val mName = org.json.JSONObject.quote(if (lang == "ar") mosque.nameAr else mosque.nameEn)
                        .replace("<", "\\u003C").replace(">", "\\u003E").replace("&", "\\u0026")
                        .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
                    val mAddr = org.json.JSONObject.quote(if (lang == "ar") mosque.addressAr else mosque.addressEn)
                        .replace("<", "\\u003C").replace(">", "\\u003E").replace("&", "\\u0026")
                        .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
                    val markerLat = mosque.lat
                    val markerLng = mosque.lng
                    append("""
                        var mosqueIcon = L.divIcon({
                            className: 'mosque-pin',
                            html: "<div style='background-color:#1E5E3A; width:14px; height:14px; border:2.5px solid #D4AF37; border-radius:50%; box-shadow: 0 0 8px rgba(0,0,0,0.4);'></div>",
                            iconSize: [20, 20],
                            iconAnchor: [10, 10]
                        });
                        var popupContent = document.createElement('div');
                        var popupTitle = document.createElement('b');
                        popupTitle.textContent = $mName;
                        popupContent.appendChild(popupTitle);
                        popupContent.appendChild(document.createElement('br'));
                        var popupAddress = document.createElement('span');
                        popupAddress.style.color = '#e0e0e0';
                        popupAddress.textContent = $mAddr;
                        popupContent.appendChild(popupAddress);
                        L.marker([$markerLat, $markerLng], { icon: mosqueIcon }).addTo(map)
                            .bindPopup(popupContent);
                    """)
                }
                
                append("""
                        </script>
                    </body>
                    </html>
                """)
            }
            webView.loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}


@Composable
fun MosquesTool(viewModel: AppViewModel, lang: String) {
    val context = LocalContext.current
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val locName by viewModel.locationName.collectAsState()

    val preferredId by viewModel.prefMosqueId.collectAsState()
    val favorites by viewModel.favoriteMosques.collectAsState()
    val realNearbyMosques by viewModel.nearbyRealMosques.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDistanceFilter by remember { mutableStateOf(15.0) } // Max distance in km

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.startLocationTracking()
        } else {
            // Keep the app on device/GPS location only. The user can retry permission from the app.
        }
    }

    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val locationPermissionGranted = hasFine || hasCoarse

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            viewModel.startLocationTracking()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationTracking()
        }
    }

    val baseMosques = realNearbyMosques

    // Calculate distance helper
    fun calculateDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
        return r * c
    }

    val processedMosques = baseMosques.map { mosque ->
        val distance = calculateDist(lat, lng, mosque.lat, mosque.lng)
        Pair(mosque, distance)
    }.filter { (_, distance) ->
        distance <= selectedDistanceFilter || selectedDistanceFilter >= 100.0
    }.sortedBy { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // Live GPS indicator card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (lang == "ar") "موقعك الحالي النشط" else "Active GPS Coordinates",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = locName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "الموقع الجغرافي",
                            tint = if (locationPermissionGranted) Color(0xFF1E5E3A) else Color(0xFFD4AF37),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == "ar") "خط العرض: %.4f • خط الطول: %.4f".format(lat, lng) else "Lat: %.4f • Lng: %.4f".format(lat, lng),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!locationPermissionGranted) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (lang == "ar") "تفعيل الموقع" else "Enable location")
                            }
                        }
                        OutlinedButton(
                            onClick = { viewModel.fetchRealNearbyMosques(lat, lng) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (lang == "ar") "تحديث المساجد" else "Refresh mosques")
                        }
                    }
                }
            }
        }

        // Live Google Map / OpenStreetMap Interactive Map View!
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                InteractiveGoogleMap(
                    lat = lat,
                    lng = lng,
                    nearbyMosques = baseMosques,
                    lang = lang,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Search inputs
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (lang == "ar") "ابحث عن مسجد بالاسم..." else "Search mosques by name...") },
                modifier = Modifier.fillMaxWidth().testTag("mosque_search_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "بحث", tint = Color(0xFFD4AF37)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                singleLine = true
            )
        }

        // Distance Filter Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "ar") "نطاق البحث" else "Search Radius",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (selectedDistanceFilter >= 100.0) {
                                if (lang == "ar") "عرض الكل" else "Show All"
                            } else {
                                "%.0f km".format(selectedDistanceFilter)
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                    Slider(
                        value = selectedDistanceFilter.toFloat(),
                        onValueChange = { selectedDistanceFilter = it.toDouble() },
                        valueRange = 5.0f..105.0f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFD4AF37),
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Mosques list
        val finalFiltered = processedMosques.filter { (mosque, _) ->
            searchQuery.isBlank() || mosque.nameEn.contains(searchQuery, ignoreCase = true) || mosque.nameAr.contains(searchQuery)
        }

        if (finalFiltered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "ar") "لم يتم العثور على مساجد في هذا النطاق" else "No mosques found in this search radius",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(finalFiltered.size) { idx ->
                val (mosque, distance) = finalFiltered[idx]
                val isPreferred = preferredId == mosque.id.toString()
                val isFav = favorites.any { it.id == mosque.id }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("mosque_card_${mosque.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = if (isPreferred) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD4AF37)) else null
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == "ar") mosque.nameAr else mosque.nameEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (lang == "ar") mosque.addressAr else mosque.addressEn,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFD4AF37).copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (distance >= 1000.0) "%.0f km".format(distance) else "%.1f km".format(distance),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC59B27),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${mosque.lat},${mosque.lng}&mode=d"))
                                        try { context.startActivity(intent) } catch (e: Exception) {}
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                ) {
                                    Icon(Icons.Filled.DirectionsCar, contentDescription = "القيادة", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${mosque.lat},${mosque.lng}&mode=w"))
                                        try { context.startActivity(intent) } catch (e: Exception) {}
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                ) {
                                    Icon(Icons.Filled.DirectionsWalk, contentDescription = "المشي", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(
                                    onClick = {
                                        if (isFav) {
                                            viewModel.deleteFavoriteMosque(mosque.id)
                                        } else {
                                            viewModel.insertFavoriteMosque(
                                                FavoriteMosque(
                                                    id = mosque.id,
                                                    name = mosque.nameEn,
                                                    latitude = mosque.lat,
                                                    longitude = mosque.lng,
                                                    address = mosque.addressEn
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = "المفضلة",
                                        tint = Color(0xFFD4AF37)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.setPreferredMosque(
                                        id = mosque.id.toString(),
                                        name = mosque.nameEn,
                                        lat = mosque.lat,
                                        lng = mosque.lng,
                                        addr = mosque.addressEn
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPreferred) Color(0xFFD4AF37) else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isPreferred) {
                                        if (lang == "ar") "المسجد المختار" else "Selected Preferred"
                                    } else {
                                        if (lang == "ar") "اختر كمسجد مفضل" else "Select Preferred"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. RAMADAN KHATMAH TOOL (Quran Completion Planner)
// ==========================================
@Composable
fun KhatmahTool(viewModel: AppViewModel, lang: String) {
    val khatmahs by viewModel.allKhatmahs.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    var newKhatmahName by remember { mutableStateOf("") }
    var targetDaysStr by remember { mutableStateOf("30") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lang == "ar") "خطط الختم الفعالة" else "Active Khatmah Plans",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Button(
                onClick = {
                    newKhatmahName = if (lang == "ar") "ختمتي الرمضانية" else "My Ramadan Khatmah"
                    showCreateDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("create_khatmah_btn")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة")
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (lang == "ar") "إنشاء خطة" else "Create Plan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (khatmahs.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (lang == "ar") "ابدأ بالتخطيط لختمة جديدة لتنظيم قراءتك اليومية" else "Create a Khatmah plan to organize your daily Quran completions.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(khatmahs.size) { idx ->
                    val kh = khatmahs[idx]
                    val progressPercent = (kh.currentProgressPages.toFloat() / kh.totalPages.toFloat() * 100).coerceIn(0f, 100f)
                    
                    val daysElapsed = ((System.currentTimeMillis() - kh.lastReadTime) / (1000 * 60 * 60 * 24)).coerceAtLeast(0L)
                    val remainingDays = (kh.targetDays - daysElapsed).coerceAtLeast(1L)
                    val remainingPages = (kh.totalPages - kh.currentProgressPages).coerceAtLeast(0)
                    val dailyTargetPages = (remainingPages.toFloat() / remainingDays.toFloat()).coerceAtLeast(1f)

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("khatmah_card_${kh.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(kh.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = if (lang == "ar") "الهدف: الختم في ${kh.targetDays} يومًا" else "Goal: Complete in ${kh.targetDays} days",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteKhatmah(kh) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.6f))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (lang == "ar") "الصفحة ${kh.currentProgressPages} من ${kh.totalPages}" else "Page ${kh.currentProgressPages} of ${kh.totalPages}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%.0f%%".format(progressPercent),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth().clip(CircleShape).height(10.dp),
                                color = Color(0xFFD4AF37),
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == "ar") "القراءة اليومية المقترحة" else "Suggested Daily Reading",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == "ar") "اقرأ %.1f صفحة يوميًا لإنهاء الختمة بالوقت المتبقي (%d أيام)".format(dailyTargetPages, remainingDays)
                                               else "Read %.1f pages daily to finish on-time (%d days left)".format(dailyTargetPages, remainingDays),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            var showProgressInput by remember { mutableStateOf(false) }
                            var addPagesStr by remember { mutableStateOf("10") }

                            if (!showProgressInput) {
                                Button(
                                    onClick = { showProgressInput = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (lang == "ar") "تسجيل التقدم اليومي" else "Log Daily Progress")
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = addPagesStr,
                                        onValueChange = { addPagesStr = it },
                                        label = { Text(if (lang == "ar") "عدد الصفحات المنجزة" else "Pages read") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            val extraPages = addPagesStr.toIntOrNull() ?: 0
                                            val nextProg = (kh.currentProgressPages + extraPages).coerceAtMost(kh.totalPages)
                                            viewModel.updateKhatmah(
                                                kh.copy(
                                                    currentProgressPages = nextProg,
                                                    lastReadTime = System.currentTimeMillis()
                                                )
                                            )
                                            showProgressInput = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(if (lang == "ar") "حفظ" else "Save")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (lang == "ar") "إنشاء خطة ختم جديدة" else "Create New Khatmah Plan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newKhatmahName,
                        onValueChange = { newKhatmahName = it },
                        label = { Text(if (lang == "ar") "اسم الخطة" else "Plan Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetDaysStr,
                        onValueChange = { targetDaysStr = it },
                        label = { Text(if (lang == "ar") "المدة المستهدفة (أيام)" else "Target duration (days)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val d = targetDaysStr.toIntOrNull() ?: 30
                        viewModel.insertKhatmah(
                            Khatmah(
                                name = newKhatmahName,
                                targetDays = d,
                                currentProgressPages = 0,
                                lastReadTime = System.currentTimeMillis()
                            )
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (lang == "ar") "إنشاء" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(if (lang == "ar") "إلغاء" else "Cancel")
                }
            }
        )
    }
}

// ==========================================
// 8. HISN AL MUSLIM TOOL (Authentic Duas Library)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisnAlMuslimTool(viewModel: AppViewModel, lang: String) {
    val context = LocalContext.current
    // بيانات حصن المسلم الكاملة: 133 بابًا و302 ذكر، من مجموعة MIT مرخصة.
    val duas = HisnAlMuslimData.duas

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var textScaleFactor by remember { mutableFloatStateOf(1.0f) }

    val categoriesList = duas.map { if (lang == "ar") it.categoryAr else it.categoryEn }.distinct()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (lang == "ar") "ابحث في الأدعية..." else "Search Duas...") },
                modifier = Modifier.weight(1f).testTag("hisn_search_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "بحث", tint = Color(0xFFD4AF37)) },
                singleLine = true
            )

            IconButton(
                onClick = {
                    textScaleFactor = if (textScaleFactor >= 1.4f) 1.0f else textScaleFactor + 0.15f
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "تكبير",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text(if (lang == "ar") "الكل" else "All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD4AF37),
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("hisn_chip_all")
                )

                categoriesList.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD4AF37),
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("hisn_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        val filteredDuas = duas.filter { dua ->
            val matchQuery = searchQuery.isBlank() ||
                    dua.titleAr.contains(searchQuery) ||
                    dua.titleEn.contains(searchQuery, ignoreCase = true) ||
                    dua.arabic.contains(searchQuery)

            val matchCategory = selectedCategory == null ||
                    (if (lang == "ar") dua.categoryAr else dua.categoryEn) == selectedCategory

            matchQuery && matchCategory
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredDuas.size) { idx ->
                val dua = filteredDuas[idx]

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("hisn_dua_card_${dua.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ar") dua.titleAr else dua.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "${dua.titleAr}\n\n${dua.arabic}\n\n${dua.translationAr}")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    try { context.startActivity(shareIntent) } catch (e: Exception) {}
                                }
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "مشاركة", tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = dua.arabic,
                            fontSize = (21.sp.value * textScaleFactor).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (dua.repeatCount > 1) {
                            Text(
                                text = "عدد التكرار: ${dua.repeatCount}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (dua.translationAr.isNotBlank()) {
                            Text(
                                text = dua.translationAr,
                                fontSize = (13.sp.value * textScaleFactor).sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (dua.reference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "المصدر: ${dua.reference}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
