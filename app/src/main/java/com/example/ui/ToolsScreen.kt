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
import androidx.compose.foundation.verticalScroll
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
import com.example.data.TasbihCounter
import android.net.Uri
import android.content.Context
import android.content.Intent
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: AppViewModel,
    lang: String,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf<String?>(null) } // "TASBIH", "QIBLA", "ADHKAR", "HISN_AL_MUSLIM"

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
                    Triple("HISN_AL_MUSLIM", if (lang == "ar") "حصن المسلم" else if (lang == "tr") "Hisnul Muslim" else "Hisn Al Muslim", Icons.Filled.Book),
                )

                items(toolItems) { (toolKey, title, icon) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeTool = toolKey }
                            .testTag("tool_card_$toolKey")
                            ,
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
    var sensorAccuracyGood by remember { mutableStateOf(false) }

    DisposableEffect(context, latitude, longitude) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val canUseFallback = rotationSensor == null && accelerometer != null && magneticSensor != null
        sensorAvailable = rotationSensor != null || canUseFallback
        sensorAccuracyGood = false

        if (sensorManager == null || (rotationSensor == null && !canUseFallback)) {
            onDispose { }
        } else {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val gravityValues = FloatArray(3)
            val magneticValues = FloatArray(3)

            fun updateAzimuth() {
                val matrixReady = SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues)
                if (!matrixReady) return
                SensorManager.getOrientation(rotationMatrix, orientation)
                val magneticAzimuth = ((Math.toDegrees(orientation[0].toDouble()).toFloat() % 360f) + 360f) % 360f
                val declination = GeomagneticField(latitude.toFloat(), longitude.toFloat(), 0f, System.currentTimeMillis()).declination
                deviceAzimuth = ((magneticAzimuth + declination) % 360f + 360f) % 360f
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (rotationSensor != null) {
                        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR || event.values.size < 3) return
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val magneticAzimuth = ((Math.toDegrees(orientation[0].toDouble()).toFloat() % 360f) + 360f) % 360f
                        val declination = GeomagneticField(latitude.toFloat(), longitude.toFloat(), 0f, System.currentTimeMillis()).declination
                        deviceAzimuth = ((magneticAzimuth + declination) % 360f + 360f) % 360f
                    } else {
                        if (event.values.size < 3) return
                        when (event.sensor.type) {
                            Sensor.TYPE_ACCELEROMETER -> event.values.copyInto(gravityValues)
                            Sensor.TYPE_MAGNETIC_FIELD -> event.values.copyInto(magneticValues)
                            else -> return
                        }
                        updateAzimuth()
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    sensorAccuracyGood = accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                }
            }

            if (rotationSensor != null) {
                sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_UI)
            }
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val compassRotation = ((qiblaAngle.toFloat() - deviceAzimuth) + 360f) % 360f
    val validLocation = latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0
    val directionName = when {
        qiblaAngle >= 337.5 || qiblaAngle < 22.5 -> if (lang == "ar") "الشمال" else "North"
        qiblaAngle < 67.5 -> if (lang == "ar") "الشمال الشرقي" else "North-east"
        qiblaAngle < 112.5 -> if (lang == "ar") "الشرق" else "East"
        qiblaAngle < 157.5 -> if (lang == "ar") "الجنوب الشرقي" else "South-east"
        qiblaAngle < 202.5 -> if (lang == "ar") "الجنوب" else "South"
        qiblaAngle < 247.5 -> if (lang == "ar") "الجنوب الغربي" else "South-west"
        qiblaAngle < 292.5 -> if (lang == "ar") "الغرب" else "West"
        else -> if (lang == "ar") "الشمال الغربي" else "North-west"
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (lang == "ar") "اتجاه القبلة" else "Qibla direction",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (sensorAvailable) {
                if (lang == "ar") "حرّك الهاتف ببطء حتى يستقر المؤشر، وابتعد عن المعادن والأجهزة المغناطيسية."
                else "Move your phone slowly until the indicator settles. Keep away from metal and magnetic devices."
            } else {
                if (lang == "ar") "تظهر البوصلة دائمًا؛ عند غياب مستشعر الاتجاه يبقى المؤشر ثابتًا ولا يتبع حركة الهاتف."
                else "The compass is always visible; without an orientation sensor, the pointer stays fixed and cannot track the phone."
            },
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier.size(280.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF1C6849), Color(0xFF0B382C), Color(0xFF061F18))))
                .border(3.dp, Color(0xFFD4AF37), CircleShape)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = -deviceAzimuth }) {
                val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.455f
                drawCircle(Color(0xFFD4AF37).copy(alpha = 0.85f), radius, center, style = Stroke(1.5.dp.toPx()))
                drawCircle(Color(0xFFD4AF37).copy(alpha = 0.45f), radius * 0.78f, center, style = Stroke(1.dp.toPx()))
                for (tick in 0 until 72) {
                    val angle = Math.toRadians(tick * 5.0 - 90.0)
                    val major = tick % 6 == 0
                    val inner = radius * if (major) 0.83f else 0.89f
                    val outer = radius * 0.96f
                    val from = androidx.compose.ui.geometry.Offset(center.x + kotlin.math.cos(angle).toFloat() * inner, center.y + kotlin.math.sin(angle).toFloat() * inner)
                    val to = androidx.compose.ui.geometry.Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer)
                    drawLine(if (major) Color(0xFFFFD978) else Color(0xFFB99443).copy(alpha = 0.75f), from, to, strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx())
                }
                for (mark in 0 until 8) {
                    val angle = Math.toRadians(mark * 45.0 - 90.0)
                    val point = androidx.compose.ui.geometry.Offset(center.x + kotlin.math.cos(angle).toFloat() * radius * 0.70f, center.y + kotlin.math.sin(angle).toFloat() * radius * 0.70f)
                    drawCircle(Color(0xFFD4AF37), radius = 2.2.dp.toPx(), center = point)
                }
            }
            Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = -deviceAzimuth }) {
                Text("شمال", Modifier.align(Alignment.TopCenter).padding(top = 25.dp), color = Color(0xFFFFD978), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("شرق", Modifier.align(Alignment.CenterEnd).padding(end = 22.dp), color = Color(0xFFFFD978), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("جنوب", Modifier.align(Alignment.BottomCenter).padding(bottom = 25.dp), color = Color(0xFFFFD978), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("غرب", Modifier.align(Alignment.CenterStart).padding(start = 22.dp), color = Color(0xFFFFD978), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Icon(
                Icons.Filled.Navigation,
                contentDescription = if (lang == "ar") "مؤشر اتجاه القبلة" else "Qibla direction indicator",
                tint = Color(0xFFFFD978),
                modifier = Modifier.size(82.dp).graphicsLayer { rotationZ = compassRotation }
            )
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF071F19))
                    .border(2.dp, Color(0xFFFFD978), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Home, contentDescription = if (lang == "ar") "الكعبة المشرفة" else "Kaaba", tint = Color(0xFFFFD978), modifier = Modifier.size(23.dp))
            }
        }

        if (sensorAvailable && !sensorAccuracyGood) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (lang == "ar") "تحقّق من استقرار المؤشر قبل الاعتماد عليه." else "Wait for the indicator to stabilize before relying on it.",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B382C)),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.8f))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (lang == "ar") "اتجاه القبلة بالنسبة إلى الشمال الحقيقي" else "Qibla bearing from true north", fontSize = 13.sp, color = Color(0xFFE8D8A8), textAlign = TextAlign.Center)
                Text(String.format(Locale.US, "%.1f°", qiblaAngle), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD978))
                Text(directionName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(
                    if (sensorAvailable) {
                        if (lang == "ar") "المؤشر يتبع اتجاه الهاتف." else "The pointer follows the phone's heading."
                    } else {
                        if (lang == "ar") "المؤشر ثابت ولا يتبع حركة الهاتف لعدم توفر مستشعر اتجاه." else "The pointer is static because no orientation sensor is available."
                    },
                    fontSize = 11.sp, textAlign = TextAlign.Center, color = Color(0xFFE8D8A8)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (lang == "ar") "المسافة إلى الكعبة المشرفة" else "Distance to the Kaaba", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    DisplayNumberFormatter.westernDigits(String.format(Locale.US, if (lang == "ar") "%,.1f كم" else "%,.1f km", distance)),
                    fontSize = 23.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = {
                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=21.4225%2C39.8262&travelmode=walking")
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (lang == "ar") "فتح الخريطة إلى الكعبة" else "Open map to the Kaaba")
        }
        Text(
            if (lang == "ar") "تعرض الخريطة طريقًا إلى الكعبة، وقد لا يكون خط السير هو اتجاه القبلة المباشر. إذا تغيّر موقعك، حدّث الموقع من إعدادات التطبيق."
            else "The map shows a route to the Kaaba, which may differ from the direct Qibla bearing. Update your saved location if you have moved.",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
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
