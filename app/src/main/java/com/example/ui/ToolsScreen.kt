package com.example.ui

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
    var activeTool by remember { mutableStateOf<String?>(null) } // "TASBIH", "QIBLA", "ADHKAR", "NAMES", "RAMADAN"

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
                    Triple("RAMADAN", Translations.get("ramadan", lang), Icons.Filled.Bedtime),
                    Triple("MOSQUES", if (lang == "ar") "المساجد القريبة" else if (lang == "tr") "Yakındaki Camiler" else "Nearby Mosques", Icons.Filled.LocationOn),
                    Triple("KHATMAH", if (lang == "ar") "خاتمة القرآن" else if (lang == "tr") "Kuran Hatim" else "Ramadan Khatmah", Icons.Filled.LibraryBooks),
                    Triple("HISN_AL_MUSLIM", if (lang == "ar") "حصن المسلم" else if (lang == "tr") "Hisnul Muslim" else "Hisn Al Muslim", Icons.Filled.Book),
                    Triple("HALAL_FOOD", if (lang == "ar") "المطاعم الحلال" else "Nearby Halal Food", Icons.Filled.Restaurant)
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
                                    "RAMADAN" -> Translations.get("ramadan", lang)
                                    "MOSQUES" -> if (lang == "ar") "المساجد القريبة" else if (lang == "tr") "Yakındaki Camiler" else "Nearby Mosques"
                                    "KHATMAH" -> if (lang == "ar") "خاتمة القرآن الكريم" else if (lang == "tr") "Kuran Hatim" else "Ramadan Khatmah"
                                    "HISN_AL_MUSLIM" -> if (lang == "ar") "حصن المسلم" else if (lang == "tr") "Hisnul Muslim" else "Hisn Al Muslim"
                                    "HALAL_FOOD" -> if (lang == "ar") "المطاعم الحلال القريبة" else "Nearby Halal Food"
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
                        "RAMADAN" -> RamadanTool(viewModel, lang)
                        "MOSQUES" -> MosquesTool(viewModel, lang)
                        "KHATMAH" -> KhatmahTool(viewModel, lang)
                        "HISN_AL_MUSLIM" -> HisnAlMuslimTool(viewModel, lang)
                        "HALAL_FOOD" -> HalalFoodTool(viewModel, lang)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. TASBIH TOOL (Digital Tasbih Counter)
// ==========================================
@Composable
fun TasbihTool(viewModel: AppViewModel, lang: String) {
    val counters by viewModel.tasbihCounters.collectAsState()
    var selectedCounter by remember { mutableStateOf<TasbihCounter?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newGoalStr by remember { mutableStateOf("33") }

    LaunchedEffect(counters) {
        if (selectedCounter == null && counters.isNotEmpty()) {
            selectedCounter = counters.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_tasbih_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة")
                Spacer(modifier = Modifier.width(4.dp))
                Text(Translations.get("add_counter", lang))
            }

            if (counters.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = selectedCounter?.name ?: "Select",
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "اختيار", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        counters.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedCounter = c
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (selectedCounter != null) {
            val counter = selectedCounter!!
            val progress = remember(counter.count, counter.goal) {
                if (counter.goal > 0) counter.count.toFloat() / counter.goal else 0f
            }

            // Big Interactive Counter Circle (Glassmorphic Ripple Button)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), CircleShape)
                    .clickable {
                        viewModel.incrementTasbih(counter)
                        selectedCounter = counters.find { it.id == counter.id }?.copy(count = counter.count + 1)
                    }
                    .testTag("tasbih_click_zone"),
                contentAlignment = Alignment.Center
            ) {
                // Circular progress arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFFD4AF37),
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx())
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = counter.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = counter.count.toString(),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${Translations.get("tasbih_goal", lang)}: ${counter.goal}",
                        fontSize = 14.sp,
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons Row (Reset / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        viewModel.resetTasbih(counter)
                        selectedCounter = counter.copy(count = 0)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tasbih_reset_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "إعادة الضبط", tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Translations.get("tasbih_reset", lang), color = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = {
                        viewModel.deleteTasbih(counter)
                        selectedCounter = null
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .testTag("tasbih_delete_button")
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            // Empty State
            Column(
                modifier = Modifier.fillMaxHeight(0.6f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = "لا يوجد عداد", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (lang == "ar") "اضغط على إضافة سبحة لبدء عداد التسبيح." else "Tap Add Counter to launch your digital Tasbih tracker.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(Translations.get("add_counter", lang)) },
            text = {
                Column {
                    TextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(Translations.get("name", lang)) },
                        modifier = Modifier.fillMaxWidth().testTag("tasbih_input_name")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = newGoalStr,
                        onValueChange = { newGoalStr = it },
                        label = { Text(Translations.get("tasbih_goal", lang)) },
                        modifier = Modifier.fillMaxWidth().testTag("tasbih_input_goal")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val g = newGoalStr.toIntOrNull() ?: 33
                        if (newName.isNotBlank()) {
                            viewModel.createTasbih(newName, g)
                            newName = ""
                            newGoalStr = "33"
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("tasbih_confirm_save")
                ) {
                    Text(Translations.get("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(Translations.get("cancel", lang))
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
// 5. RAMADAN TRACKER TOOL
// ==========================================
@Composable
fun RamadanTool(viewModel: AppViewModel, lang: String) {
    val remainingDays by viewModel.ramadanDaysRemaining.collectAsState()
    val isFasting by viewModel.isFastingToday.collectAsState()

    // Charity & prayers logged
    val context = LocalContext.current
    val ramadanPrefs = remember { context.getSharedPreferences("ramadan_progress", android.content.Context.MODE_PRIVATE) }
    var charityLogged by remember { mutableStateOf(ramadanPrefs.getInt("charity_logged", 0)) }
    var qiyamLogged by remember { mutableStateOf(ramadanPrefs.getInt("qiyam_logged", 0)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Countdown Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Bedtime, contentDescription = "رمضان", tint = Color(0xFFD4AF37), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Translations.get("ramadan_countdown", lang),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$remainingDays ${Translations.get("days", lang)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )
                }
            }
        }

        // Interactive Fasting Logger Checkbox
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleFastingToday() }
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Translations.get("fasting_tracker", lang),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Translations.get("fast_today", lang),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = isFasting,
                        onCheckedChange = { viewModel.toggleFastingToday() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD4AF37), checkedTrackColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("fasting_switch")
                    )
                }
            }
        }

        // Qiyam and Charity loggers (interactive, real statistics)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Charity Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.VolunteerActivism, contentDescription = "الصدقة", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (lang == "ar") "الصدقات المسجلة" else "Sadaqah Logged", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(if (lang == "ar") "$charityLogged مرة" else "$charityLogged Times", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                charityLogged++
                                ramadanPrefs.edit().putInt("charity_logged", charityLogged).apply()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("log_charity_button")
                        ) {
                            Text(if (lang == "ar") "+ تسجيل" else "+ Log")
                        }
                    }
                }

                // Qiyam (Night prayers) Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.MenuBook, contentDescription = "قيام الليل", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (lang == "ar") "قيام الليل / التراويح" else "Qiyam / Tarawih", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(if (lang == "ar") "$qiyamLogged جلسة" else "$qiyamLogged Sessions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                qiyamLogged++
                                ramadanPrefs.edit().putInt("qiyam_logged", qiyamLogged).apply()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("log_qiyam_button")
                        ) {
                            Text("+ تسجيل")
                        }
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
                settings.domStorageEnabled = true
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
                    val mName = if (lang == "ar") mosque.nameAr.replace("'", "\\'") else mosque.nameEn.replace("'", "\\'")
                    val mAddr = if (lang == "ar") mosque.addressAr.replace("'", "\\'") else mosque.addressEn.replace("'", "\\'")
                    append("""
                        var mosqueIcon = L.divIcon({
                            className: 'mosque-pin',
                            html: "<div style='background-color:#1E5E3A; width:14px; height:14px; border:2.5px solid #D4AF37; border-radius:50%; box-shadow: 0 0 8px rgba(0,0,0,0.4);'></div>",
                            iconSize: [20, 20],
                            iconAnchor: [10, 10]
                        });
                        L.marker([${mosque.lat}, ${mosque.lng}], { icon: mosqueIcon }).addTo(map)
                            .bindPopup("<b>$mName</b><br/><span style='color:#e0e0e0;'>$mAddr</span>");
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
data class HisnDua(
    val id: String,
    val categoryAr: String,
    val categoryEn: String,
    val titleAr: String,
    val titleEn: String,
    val arabic: String,
    val translationAr: String,
    val translationEn: String,
    val reference: String = "",
    val repeatCount: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisnAlMuslimTool(viewModel: AppViewModel, lang: String) {
    val context = LocalContext.current
    // بيانات حصن المسلم الكاملة: 133 بابًا و302 ذكر، من مجموعة MIT مرخصة.
    val duas = listOf(
        HisnDua("1", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 1", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 1", "الحَمْدُ لِلَّهِ الذِي أَحْيَانَا بَعْدَمَا أَمَاتَنَا، وَإِلَيْهِ النُّشُّورُ", "", "", "البخاري مع الفتح 11/ 113 ومسلم 4/ 2083", 1),
        HisnDua("2", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 2", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 2", "الحَمْدُ لِلَّهِ الذِي عَافَانِي فِي جَسَدِي، وَرَدَّ عَلَيَّ رُوحِي، وَأَذِنَ لِي بِذِكْرِهِ", "", "", "التّرمذي 5/ 473 وانظر صحيح التّرمذي 3/ 144", 1),
        HisnDua("3", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 3", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 3", "(لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، سُبْحَانَ اللَّهِ، وَالحَمْدُ لِلَّهِ، وَلَا إِلَهَ إِلَّا اللَّه، وَاللَّه أَكْبَرُ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ العَلِّي العَظِيمِ، رَبِّ اغْفِرْ لِي). أَوْ دَعَا اسْتُجِيبَ لَهُ فَإِنْ تَوَضَّأَ وَصَلَّى قُبِلَتْ صَلَاتَهُ", "", "", "البخاري مع الفتح 3/ 144 وغيرها واللفظ لابن ماجه انظر صحيح ابن ماجه 2/ 335", 1),
        HisnDua("4", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 4", "أَذْكَارُ الِاسْتِيقَاظِ مِنَ النَّوْمِ — الذكر 4", "{إِنَّ فِي خَلْقِ السَّمَاوَاتِ وَالْأَرْضِ وَاخْتِلَافِ اللَّيْلِ وَالنَّهَارِ لَآيَاتٍ لِأُولِي الْأَلْبَابِ (190) الَّذِينَ يَذْكُرُونَ اللَّهَ قِيَامًا وَقُعُودًا وَعَلَى جُنُوبِهِمْ وَيَتَفَكَّرُونَ فِي خَلْقِ السَّمَاوَاتِ وَالْأَرْضِ رَبَّنَا مَا خَلَقْتَ هَذَا بَاطِلًا سُبْحَانَكَ فَقِنَا عَذَابَ النَّارِ (191) رَبَّنَا إِنَّكَ مَنْ تُدْخِلِ النَّارَ فَقَدْ أَخْزَيْتَهُ وَمَا لِلظَّالِمِينَ مِنْ أَنْصَارٍ (192) رَبَّنَا إِنَّنَا سَمِعْنَا مُنَادِيًا يُنَادِي لِلْإِيمَانِ أَنْ آمِنُوا بِرَبِّكُمْ فَآمَنَّا رَبَّنَا فَاغْفِرْ لَنَا ذُنُوبَنَا وَكَفِّرْ عَنَّا سَيِّئَاتِنَا وَتَوَفَّنَا مَعَ الْأَبْرَارِ (193) رَبَّنَا وَآتِنَا مَا وَعَدْتَنَا عَلَى رُسُلِكَ وَلَا تُخْزِنَا يَوْمَ الْقِيَامَةِ إِنَّكَ لَا تُخْلِفُ الْمِيعَادَ (194) فَاسْتَجَابَ لَهُمْ رَبُّهُمْ أَنِّي لَا أُضِيعُ عَمَلَ عَامِلٍ مِنْكُمْ مِنْ ذَكَرٍ أَوْ أُنْثَى بَعْضُكُمْ مِنْ بَعْضٍ فَالَّذِينَ هَاجَرُوا وَأُخْرِجُوا مِنْ دِيَارِهِمْ وَأُوذُوا فِي سَبِيلِي وَقَاتَلُوا وَقُتِلُوا لَأُكَفِّرَنَّ عَنْهُمْ سَيِّئَاتِهِمْ وَلَأُدْخِلَنَّهُمْ جَنَّاتٍ تَجْرِي مِنْ تَحْتِهَا الْأَنْهَارُ ثَوَابًا مِنْ عِنْدِ اللَّهِ وَاللَّهُ عِنْدَهُ حُسْنُ الثَّوَابِ (195) لَا يَغُرَّنَّكَ تَقَلُّبُ الَّذِينَ كَفَرُوا فِي الْبِلَادِ (196) مَتَاعٌ قَلِيلٌ ثُمَّ مَأْوَاهُمْ جَهَنَّمُ وَبِئْسَ الْمِهَادُ (197) لَكِنِ الَّذِينَ اتَّقَوْا رَبَّهُمْ لَهُمْ جَنَّاتٌ تَجْرِي مِنْ تَحْتِهَا الْأَنْهَارُ خَالِدِينَ فِيهَا نُزُلًا مِنْ عِنْدِ اللَّهِ وَمَا عِنْدَ اللَّهِ خَيْرٌ لِلْأَبْرَارِ (198) وَإِنَّ مِنْ أَهْلِ الْكِتَابِ لَمَنْ يُؤْمِنُ بِاللَّهِ وَمَا أُنْزِلَ إِلَيْكُمْ وَمَا أُنْزِلَ إِلَيْهِمْ خَاشِعِينَ لِلَّهِ لَا يَشْتَرُونَ بِآيَاتِ اللَّهِ ثَمَنًا قَلِيلًا أُولَئِكَ لَهُمْ أَجْرُهُمْ عِنْدَ رَبِّهِمْ إِنَّ اللَّهَ سَرِيعُ الْحِسَابِ (199) يَا أَيُّهَا الَّذِينَ آمَنُوا اصْبِرُوا وَصَابِرُوا وَرَابِطُوا وَاتَّقُوا اللَّهَ لَعَلَّكُمْ تُفْلِحُونَ (200)}\r\nسورة آل عمران، 190 – 200", "", "", "البخاري مع الفتح 8/ 238 ومسلم 1/ 530", 1),
        HisnDua("5", "دُعَاءُ لُبْسِ الثَّوْبِ", "دُعَاءُ لُبْسِ الثَّوْبِ", "دُعَاءُ لُبْسِ الثَّوْبِ — الذكر 5", "دُعَاءُ لُبْسِ الثَّوْبِ — الذكر 5", "الحَمْدُ لِلَّهِ الذِي كَسَانِي هَذَا ( الثَّوْبَ ) وَرَزَقَنِيه مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ", "", "", "أخرجه أهل السنن إلا النسائي انظر إرواء الغليل 7/ 47", 1),
        HisnDua("6", "دُعَاءُ لُبْسِ الثَّوْبِ الجَدِيدِ", "دُعَاءُ لُبْسِ الثَّوْبِ الجَدِيدِ", "دُعَاءُ لُبْسِ الثَّوْبِ الجَدِيدِ — الذكر 6", "دُعَاءُ لُبْسِ الثَّوْبِ الجَدِيدِ — الذكر 6", "اللَّهُمَّ لَكَ الحَمْدُ أَنْتَ كَسَوتَنِيه، أسْألك مِنْ خَيرِهِ وخَيْرِ مَا صُنِعَ لَهُ، وَأَعُوذُ بِكَ مِنْ شرِّه وشَرِّ مَا صُنِعَ لَهُ", "", "", "أبو داود والتّرمذي والبغوي وانظر مختصر شمائل التّرمذي للألباني ص 47", 1),
        HisnDua("7", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا — الذكر 7", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا — الذكر 7", "إِلْبَسْ جَدِيدًا وَعِشْ حَمِيدًا وَمُتْ شَهِيدًا", "", "", "ابن ماجه 2/ 1178 والبغوي 12/ 41 وانظر صحيح ابن ماجة 2/ 275", 1),
        HisnDua("8", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا — الذكر 8", "الدُّعَاءُ لِمَنْ لَبِسَ ثَوْبًـا جَدِيدًا — الذكر 8", "تُبْلِي وَيُخْلِفُ اللَّهُ تَعَالَى", "", "", "أخرجه أبو داود 4/ 41، وانظر صحيح أبي داود 2/ 760", 1),
        HisnDua("9", "مَا يَقُولُ إِذَا وَضَعَ ثَوْبَهُ", "مَا يَقُولُ إِذَا وَضَعَ ثَوْبَهُ", "مَا يَقُولُ إِذَا وَضَعَ ثَوْبَهُ — الذكر 9", "مَا يَقُولُ إِذَا وَضَعَ ثَوْبَهُ — الذكر 9", "بِسْمِ اللَّهِ", "", "", "التّرمذي 2/ 505 وغيره وانظر الإرواء برقم 49 صحيح الجامع 3/ 203", 1),
        HisnDua("10", "دُعَاءُ دُخُولِ الخَلَاءِ", "دُعَاءُ دُخُولِ الخَلَاءِ", "دُعَاءُ دُخُولِ الخَلَاءِ — الذكر 10", "دُعَاءُ دُخُولِ الخَلَاءِ — الذكر 10", "[بِسْمِ اللَّه]، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الخُبْثِ وَالخَبَائِثِ", "", "", "أخرجه البخاري 1/ 45 ومسلم 1/ 283 وزيادة (بسم الله) في أوله، أخرجها سعيد بن منصور، أنظر فتح الباري 1/ 244", 1),
        HisnDua("11", "دُعَاءُ الخُرُوجِ مِنَ الخَلَاءِ", "دُعَاءُ الخُرُوجِ مِنَ الخَلَاءِ", "دُعَاءُ الخُرُوجِ مِنَ الخَلَاءِ — الذكر 11", "دُعَاءُ الخُرُوجِ مِنَ الخَلَاءِ — الذكر 11", "غُفْرَانَكَ", "", "", "أخرجه أصحاب السنن إلا النسائي، أخرجه في عمل اليوم والليلة انظر تخريج زاد المعاد 2/ 387", 1),
        HisnDua("12", "الذِّكْرُ قَبْلَ الوُضُوءِ", "الذِّكْرُ قَبْلَ الوُضُوءِ", "الذِّكْرُ قَبْلَ الوُضُوءِ — الذكر 12", "الذِّكْرُ قَبْلَ الوُضُوءِ — الذكر 12", "بِسْمِ اللَّه", "", "", "أبو داود وابن ماجه وأحمد وانظر إرواء الغليل 1/ 122", 1),
        HisnDua("13", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ — الذكر 13", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ — الذكر 13", "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "", "", "رواه مسلم 1/ 209", 1),
        HisnDua("14", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ — الذكر 14", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ — الذكر 14", "اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ المُتَطَهِّرِينَ", "", "", "التّرمذي 1/ 78 وانظر صحيح التّرمذي 1/ 18", 1),
        HisnDua("15", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ — الذكر 15", "الذِّكْرُ بَعْدَ الفَرَاغِ مِنَ الوُضُوءِ — الذكر 15", "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ", "", "", "النسائي في عمل اليوم والليلة ص 173 وانظر  إرواء الغليل 1/ 135 و 2/  94", 1),
        HisnDua("16", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ — الذكر 16", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ — الذكر 16", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أَضِلَّ أَوْ أُضَلَّ أَوْ أَزِلَّ، أو أُزَلَّ، أَوْ أَظْلِم أَوْ أُظْلَمَ، أَوْ أَجْهَلَ أَوْ يُجْهَلَ عَلَيَّ", "", "", "أهل السنن وانظر صحيح التّرمذي 3/ 152 وصحيح ابن ماجه 2/ 336", 1),
        HisnDua("17", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ — الذكر 17", "الذِّكْرُ عِنْدَ الخُرُوجِ مِنَ الـمَنْزِلِ — الذكر 17", "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "", "", "أبو داود 4/ 325 والتّرمذي 5/ 490 وانظر صحيح التّرمذي 3/ 151", 1),
        HisnDua("18", "الذِّكْرُ عِنْدَ دُخُولِ الـمَنْزِلِ", "الذِّكْرُ عِنْدَ دُخُولِ الـمَنْزِلِ", "الذِّكْرُ عِنْدَ دُخُولِ الـمَنْزِلِ — الذكر 18", "الذِّكْرُ عِنْدَ دُخُولِ الـمَنْزِلِ — الذكر 18", "بِسْمِ اللًّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا. ثُمَّ لِيُسَلِّمْ عَلَى أَهْلِهِ", "", "", "أخرجه أبو داود 4/ 325، وحسن إسناده العلامة ابن باز في تحفة الأخيار ص 28، وفي الصحيح (إذا دخل الرجل بيته فذكر بيته فذكر الله عند دخوله وعند طعامه قال الشيطان لا مبيت لكم ولا عشاء) مسلم برقم 2018", 1),
        HisnDua("19", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 19", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 19", "اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُورًا، وَفِي لِسَانِي نُورًا، وَفِي سَمْعِي نُورًا، وَفِي بَصَرِي نُورًا، وَمِنْ فَوْقِي نُورًا، وَمِنْ تَحْتِي نُورًا، وَعَنْ يَمِينِي نُورًا، وَعَنْ شِمَالِي نُورا، وَمِنْ أَمَامِي نُورًا، وَمِنْ خَلْفِي نُورًا، وَاجْعَلْ فِي نَفْسِي نُورًا، وَأَعْظِمْ لِي نُورًا، وَعَظِّمْ لِي نُورًا، وَاجْعَلْ لِي نُورًا، وَاجْعَلْنِي نُورًا. اللَّهُمَّ أَعْطِنِي نُورًا، وَاجْعَلْ فِي عَصَبِي نُورًا، وَفِي لَحْمِي نُورًا، وَفِي دَمِي نُورًا، وَفِي شَعْرِي نُورًا، وَفِي بَشَرِي نُورًا", "", "", "جميع هذه الخصال في البخاري 11/ 116 برقم 6316 ومسلم 1/ 526، 529، 530 برقم 763", 1),
        HisnDua("20", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 20", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 20", "اللَّهُمَّ اجْعَلْ لِي نُورًا فِي قَبْرِي .. وَنُورًا فِي عِظَامِي", "", "", "التّرمذي برقم 3419، 5/ 483", 1),
        HisnDua("21", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 21", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 21", "وَزِدْنِي نُورًا، وَزِدْنِي نُورًا، وَزِدْنِي نُورًا", "", "", "أخرجه البخاري في الأدب المفرد برقم 695، ص 258 وصحح إسناده الألباني في صحيح الأدب المفرد برقم 536", 1),
        HisnDua("22", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 22", "دُعَاءُ الذَّهَابِ إِلَى الـمَسْجِدِ — الذكر 22", "وَهَبْ لِي نُورًا عَلَى نُورٍ", "", "", "ذكره ابن حجر في فتح الباري وعزاه إلى ابن أبي عاصم في كتاب الدعاء، انظر الفتح 11/ 118 وقال: فاجتمع من اختلاف الروايات خمس وعشرون خصلة", 1),
        HisnDua("23", "دُعَاءُ دُخُولِ الـمَسْجِدِ", "دُعَاءُ دُخُولِ الـمَسْجِدِ", "دُعَاءُ دُخُولِ الـمَسْجِدِ — الذكر 23", "دُعَاءُ دُخُولِ الـمَسْجِدِ — الذكر 23", "أَعُوذُ بِاللَّهِ العَظِيمِ، وَبِوَجْهِهِ الكَرِيمِ، وَسُلْطَانِهِ القَدِيمِ، مِنَ الشَّيْطَانِ الرَّجِيمِ", "", "", "أبو داود وانظر صحيح الجامع برقم 4591", 1),
        HisnDua("24", "دُعَاءُ دُخُولِ الـمَسْجِدِ", "دُعَاءُ دُخُولِ الـمَسْجِدِ", "دُعَاءُ دُخُولِ الـمَسْجِدِ — الذكر 24", "دُعَاءُ دُخُولِ الـمَسْجِدِ — الذكر 24", "بِسْمِ اللَّهِ، وَالصَّلَاةُ* وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ** اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ***", "", "", "* رواه ابن السني برقم 88 وحسنه الألباني\r\n** أبو داود 1/ 126 وانظر صحيح الجامع 1/ 528\r\n*** رواه مسلم 1/ 494. وفي سنن ابن ماجه من حديث فاطمة رضي الله عنها (اللهم اغفر لي ذنوبي وافتح لي أبواب رحمتك) وصححه الألباني لشواهده، انظر صحيح ابن ماجه 1/ 128 - 129", 1),
        HisnDua("25", "دُعَاءُ الخُرُوجِ مِنَ الـمَسْجِدِ", "دُعَاءُ الخُرُوجِ مِنَ الـمَسْجِدِ", "دُعَاءُ الخُرُوجِ مِنَ الـمَسْجِدِ — الذكر 25", "دُعَاءُ الخُرُوجِ مِنَ الـمَسْجِدِ — الذكر 25", "بِسْمِ اللَّهِ وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ، اللَّهُمَّ اعْصِمْنِي مِنَ الشَّيْطَانِ الرَّجِيمِ", "", "", "انظر تخريج روايات الحديث السابق رقم 20 وزيادة (اللهم اعصمني من الشيطان الرجيم) لابن ماجه. انظر صحيح ابن ماجه 1/ 129", 1),
        HisnDua("26", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ — الذكر 26", "أَذْكَارُ الأَذَانِ — الذكر 26", "يَقُولُ مِثْلَ مَا يَقُولُ المُؤَذِّنُ إِلَّا فِي (حَيَّ عَلَى الصَّلَاةِ) وَ (حَيَّ عَلَى الفَلَاحِ) فَيَقُولُ (لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ)", "", "", "البخاري 1/ 152 ومسلم 1/ 288", 1),
        HisnDua("27", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ — الذكر 27", "أَذْكَارُ الأَذَانِ — الذكر 27", "وَأَنَا أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ، وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، رَضِيتُ بِاللَّهِ رَبًّا، وَبِمُحَمَّدٍ رَسُولًا وَبِالإِسْلَامِ دِينًا", "", "", "مسلم 1/ 290. يقول ذلك عقب تشهد المؤذن (ابن خزيمة 1/ 220)", 1),
        HisnDua("28", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ — الذكر 28", "أَذْكَارُ الأَذَانِ — الذكر 28", "يُصَلِّي عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ فَرَاغِهِ مِنَ إِجَابَةِ المُؤَذِّنِ", "", "", "مسلم 1/ 288", 1),
        HisnDua("29", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ — الذكر 29", "أَذْكَارُ الأَذَانِ — الذكر 29", "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ القَائِمَةِ، آتِ مُحَمَّدًا الوَسِيلَةَ وَالفَضِيلَةَ، وَأبْعَثْهُ مَقَامًا مَحْمُودًا الذِي وَعَدْتَهُ، [إِنَّكَ لَا تُخْلِفُ المِيعَادَ]", "", "", "البخاري 1/ 152 وما بين المعكوفين للبيهقي 1/ 410 وحسن إسناده العلامة عبد العزيز بن باز في تحفة الأخيار ص 38", 1),
        HisnDua("30", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ", "أَذْكَارُ الأَذَانِ — الذكر 30", "أَذْكَارُ الأَذَانِ — الذكر 30", "يَدْعُو لِنَفْسِهِ بَيْنَ الأَذَانِ وَالإِقَامَةِ فَإِنَّ الدُّعَاءَ حِينَئِذٍ لَا يُرَدُّ", "", "", "التّرمذي وأبو داود وأحمد وانظر إرواء الغليل 1/ 262", 1),
        HisnDua("31", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 31", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 31", "اللَّهُمَّ بَاعِدْ بَيْنِي وَبَيْنَ خَطَايَايَ كَمَا بَاعَدْتَ بَيْنَ المَشْرِقِ وَالمَغْرِبِ، اللَّهُمَّ نَقِّنِي مِنْ خَطَايَايَ كَمَا يُنَقَّى الثَّوْبُ الأَبْيَضُ مِنَ الدَّنَسِ اللَّهُمَّ اغْسِلْنِي مِنْ خَطَايَايَ بِالمَاءِ وَالثَّلْجِ وَالبَرَدِ", "", "", "البخاري 1/ 181 ومسلم 1/ 419", 1),
        HisnDua("32", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 32", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 32", "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَى جَدُّكْ، وَلَا إِلَهَ غَيْرُكَ", "", "", "أخرجه أصحاب السنن الأربعة وانظر صحيح التّرمذي 1/ 77 وصحيح ابن ماجه 1/ 135", 1),
        HisnDua("33", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 33", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 33", "وَجَّهْتُ وَجْهِيَ لِلَّذِي فَطَرَ السَّمَوَاتِ وَالأَرْضَ حَنِيفًا وَمَا أَنَا مِنَ المُشْرِكِينَ، إِنَّ صَلَاتِي وَنُسُكِي وَمَحْيَايَ وَمَمَاتِي لِلَّهِ رَبِّ العَالَمِينْ، لَا شَرِيكَ لَهُ وَبِذَلِكَ أُمِرْتُ وَأَنَا مِنَ المُسْلِمِينَ، اللَّهُمَّ أَنْتَ المَلِكُ لَا إِلَهَ إِلَّا أَنْتَ، أَنْتَ رَبِّي وَأَنَا عَبْدُكَ، ظَلَمْتُ نَفْسِي، وَاعْتَرَفْتُ بِذَنْبِي، فَاغْفِرْ لِي ذُنُوبِي جَمِيعًا، إِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، وَاهْدِنِي لِأَحْسَنِ الأَخْلَاقِ، لَا يَهْدِي لِأَحْسَنِهَا إِلَّا أَنْتَ، وَاصْرِفْ عَنِّي سَيِّئَهَا، لَا يَصْرِفُ عَنِّي سَيِّئَهَا إِلَّا أَنْتَ، لَبَّيْكَ وَسَعْدَيْكَ، وَالخَيْرُ كُلُّهُ بِيَدَيْكَ، وَالشَّرُّ لَيْسَ إِلَيْكَ، أَنَا بِكَ وَإِلَيْكَ، تَبَارَكْتَ وَتَعَالَيْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ", "", "", "رواه مسلم 1/ 534", 1),
        HisnDua("34", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 34", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 34", "اللَّهُمَّ رَبَّ جِبْرَائِيلَ وَمِيكَائِيلَ وَإِسْرَافِيلَ، فَاطِرَ السَّمَوَاتِ وَالأَرْضِ، عَالِمَ الغَيْبِ وَالشَّهَادَةِ، أَنْتَ تَحْكُمُ بَيْنَ عِبَادِكَ فِيمَا كَانُوا فِيهِ يَخْتَلِفُونَ. اهْدِنِي لِمَا اخْتُلِفَ فِيهِ مِنَ الحَّقِّ بِإِذْنِكَ إِنَّكَ تَهْدِي مَنْ تَشَاءُ إِلَى صِرَاطٍ مُسْتَقِيمٍ", "", "", "رواه مسلم 1/ 534", 1),
        HisnDua("35", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 35", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 35", "اللَّهُ أَكْبَرُ كَبِيرًا، اللَّهُ أَكْبَرُ كَبِيرًا، اللَّهُ أَكْبَرُ كَبِيرًا، وَالحَمْدُ لِلَّهِ كَثِيرًا، وَالحَمْدُ لِلَّهِ كَثِيرًا، وَالحَمْدُ لِلَّهِ كَثِيرًا، وَسُبْحَانَ اللَّهِ بُكْرَةً وَأَصِيلًا (ثَلَاثًا). أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ: مِنْ نَفْخِهِ، وَنَفْثِهِ، وَ هَمْزِهِ", "", "", "أخرجه أبو داود 1/ 203 وابن ماجه 1/ 265 وأحمد 4/ 85 وأخرجه مسلم عن ابن عمر رضي الله عنهما بنحوه وفيه قصة 1/ 420", 1),
        HisnDua("36", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 36", "دُعَاءُ الاسْتِفْتَاحِ — الذكر 36", "اللَّهُمَّ لَكَ الحَمْدُ أَنْتَ نُورُ السَّمَوَاتِ وَالأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الحَمْدُ أَنْتَ قَيِّمُ السَّمَوَاتِ وَالأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الحَمْدُ أَنْتَ رَبُّ السَّمَوَاتِ وَالأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الحَمْدُ لَكَ مُلْكُ السَّمَوَاتِ وَالأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الحَمْدُ أَنْتَ مَلِكُ السَّمَوَاتِ وَالأَرْضِ، وَلَكَ الحَمْدُ أَنْتَ الحَقُّ، وَوَعْدُكَ الحَقُّ، وَقَوْلُكَ الحَقُّ، وَلِقَاؤُكَ الحَقُّ، وَالجَنَّةُ حَقٌّ، وَالنَّارُ حَقٌّ، وَالنَّبِيُّونَ حَقٌّ، وَمُحَمَّدٌ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ حَقٌّ وَالسَّاعَةُ حَقٌّ اللَّهُمَّ لَكَ أَسْلَمْتُ، وَعَلَيْكَ تَوَكَّلْتُ، وَبِكَ آمَنْتُ، وَإِلَيْكَ أَنَبْتُ، وَبِكَ خَاصَمْتُ، وَإِلَيْكَ حَاكَمْتُ، فَاغْفِرْ لِي مَا قَدَّمْتُ وَمَا أَخَّرْتُ، وَمَا أَسْرَرْتُ، وَمَا أَعْلَنْتُ أَنْتَ المُقَدِّمُ، وَأَنْتَ المُؤَخِّرُ لَا إِلَهَ إِلَّا أَنْتَ، أَنْتَ إِلَهِي لَا إِلَهَ إِلَّا أَنْتَ", "", "", "البخاري مع الفتح 3/ 3 و 11/ 116 و 13/ 371، 423، 465 ومسلم مختصرًا بنحوه 1/ 532", 1),
        HisnDua("37", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ — الذكر 37", "دُعَاءُ الرُّكُوعِ — الذكر 37", "سُبْحَانَ رَبِّيَ العَظِيمِ", "", "", "أخرجه أهل السنن وأحمد وانظر صحيح التّرمذي 1/ 83", 3),
        HisnDua("38", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ — الذكر 38", "دُعَاءُ الرُّكُوعِ — الذكر 38", "سُبْحَانَكَ اللَّهُمَّ رَبَّنَا وَبِحَمْدِكَ، اللَّهُمَّ اغْفِرْ لِي", "", "", "البخاري 1/ 199 ومسلم 1/ 350", 1),
        HisnDua("39", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ — الذكر 39", "دُعَاءُ الرُّكُوعِ — الذكر 39", "سُبُّوحٌ، قُدُّوسٌ، رَبُّ المَلَائِكَةِ وَالرُّوحِ", "", "", "مسلم 1/ 353 وأبو داود 1/ 230", 1),
        HisnDua("40", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ — الذكر 40", "دُعَاءُ الرُّكُوعِ — الذكر 40", "اللَّهُمَّ لَكَ رَكَعْتُ، وَبِكَ آمَنْتُ، وَلَكَ أَسْلَمْتُ، خَشَعَ لَكَ سَمْعِي وَبَصَرِي وَمُخِّي وَعَظْمِي وَعَصَبِي، وَمَا اسْتَقَلَّ بِهِ قَدَمِي", "", "", "مسلم 1/ 534 والأربعة إلا ابن ماجه", 1),
        HisnDua("41", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ", "دُعَاءُ الرُّكُوعِ — الذكر 41", "دُعَاءُ الرُّكُوعِ — الذكر 41", "سُبْحَانَ ذِي الجَبَرُوتِ وَالمَلَكُوتِ وَالكِبْرِيَاءِ وَالعَظَمَةِ", "", "", "أبو داود 1/ 230 والنسائي وأحمد وإسناده حسن", 1),
        HisnDua("42", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ — الذكر 42", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ — الذكر 42", "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ", "", "", "البخاري مع الفتح 2/ 282", 1),
        HisnDua("43", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ — الذكر 43", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ — الذكر 43", "رَبَّنَا وَلَكَ الحَمْدُ، حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ", "", "", "البخاري مع الفتح 2/ 284", 1),
        HisnDua("44", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ — الذكر 44", "دُعَاءُ الرَّفْعِ مِنَ الرُّكُوعِ — الذكر 44", "مِلْءَ السَّمَوَاتِ وَمِلْءَ الأَرْضِ وَمَا بَيْنَهُمَا، وَمِلْءَ مَا شِئْتَ مِنْ شَيْءٍ بَعْدُ. أَهْلَ الثَّنَاءِ وَالمَجْدِ، أَحَقُّ مَا قَالَ العَبْدُ، وَكُلُّنَا لَكَ عَبْدٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ وَلَا مُعْطِيَ لِمَا مَنَعْتَ وَلَا يَنْفَعُ ذَا الجَدِّ مِنْكَ الجَدُّ", "", "", "مسلم 1/ 346", 1),
        HisnDua("45", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ — الذكر 45", "دُعَاءُ السُّجُودِ — الذكر 45", "سُبْحَانَ رَبِّيَ الأَعْلَى", "", "", "أخرجه أهل السنن وأحمد وانظر صحيح التّرمذي 1/ 83", 3),
        HisnDua("46", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ — الذكر 46", "دُعَاءُ السُّجُودِ — الذكر 46", "سُبْحَانَكَ اللَّهُمَّ رَبَّنَا وَبِحَمْدِكَ، اللَّهُمَّ اغْفِرْ لِي", "", "", "البخاري ومسلم وتقدم تخريجه برقم 34", 1),
        HisnDua("47", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ — الذكر 47", "دُعَاءُ السُّجُودِ — الذكر 47", "سُبُّوحٌ، قُدُّوسٌ، رَبُّ المَلَائِكَةِ وَالرُّوحِ", "", "", "مسلم 1/ 533 وتقدم برقم 35", 1),
        HisnDua("48", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ — الذكر 48", "دُعَاءُ السُّجُودِ — الذكر 48", "اللَّهُمَّ لَكَ سَجَدْتُ وَبِكَ آمَنْتُ، وَلَكَ أَسْلَمْتُ، سَجَدَ وَجْهِيَ لِلَّذِي خَلَقَهُ، وَصَوَّرَهُ، وَشَقَّ سَمْعَهُ، وَبَصَرَهُ، تَبَارَكَ اللَّهُ أَحْسَنُ الخَالِقِينَ", "", "", "مسلم 1/ 534 وغيره", 1),
        HisnDua("49", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ — الذكر 49", "دُعَاءُ السُّجُودِ — الذكر 49", "سُبْحَانَ ذِي الجَبَرُوتِ وَالمَلَكُوتِ، وَالكِبْرِيَاءِ وَالعَظَمَةِ", "", "", "أبو داود 1/ 230 وأحمد، والنسائي، وإسناده حسن", 1),
        HisnDua("50", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ — الذكر 50", "دُعَاءُ السُّجُودِ — الذكر 50", "اللَّهُمَّ اغْفِرْ لِي ذَنْبِي كُلَّهُ، دِقَّهُ وَجِلَّهُ، وَأَوَّلَهُ وَآخِرَهُ، وَعَلَانِيَّتَهُ وَسِرَّهُ", "", "", "مسلم 1/ 350", 1),
        HisnDua("51", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ", "دُعَاءُ السُّجُودِ — الذكر 51", "دُعَاءُ السُّجُودِ — الذكر 51", "اللَّهُمَّ إِنِّي أَعُوذُ بِرِضَاكَ مِنْ سَخَطِكَ، وَبِمُعَافَاتِكَ مِنْ عُقُوبَتِكَ، وَأَعُوذُ بِكَ مِنْكَ، لَا أُحْصِي ثَنَاءً عَلَيْكَ أَنْتَ كَمَا أَثْنَيْتَ عَلَى نَفْسِكَ", "", "", "مسلم 1/ 352", 1),
        HisnDua("52", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ — الذكر 52", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ — الذكر 52", "رَبِّ اغْفِرْ لِي، رَبِّ اغْفِرْ لِي", "", "", "أبو داود 1/ 231 وانظر  صحيح ابن ماجة 1/ 148", 1),
        HisnDua("53", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ — الذكر 53", "دُعَاءُ الجَلْسَةِ بَيْنَ السَّجْدَتَيْنِ — الذكر 53", "اللَّهُمَّ اغْفِرْ لِي، وَارْحَمْنِي، وَاهْدِنِي، وَاجْبُرْنِي، وَعَافِنِي، وَارْزُقْنِي، وَارْفَعْنِي", "", "", "أخرجه أصحاب السنن إلا النسائي وانظر صحيح التّرمذي 1/ 90 وصحيح ابن ماجه 1/ 148", 1),
        HisnDua("54", "دُعَاءُ سُجُودِ التِّلَاوَةِ", "دُعَاءُ سُجُودِ التِّلَاوَةِ", "دُعَاءُ سُجُودِ التِّلَاوَةِ — الذكر 54", "دُعَاءُ سُجُودِ التِّلَاوَةِ — الذكر 54", "سَجَدَ وَجْهِيَ لِلَّذِي خَلَقَهُ، وَشَقَّ سَمْعَهُ وَبَصَرَهُ بِحَوْلِهِ وَقُوَّتِهِ {فَتَبَارَكَ اللَّهُ أَحْسَنُ الخَالِقِينَ}", "", "", "التّرمذي 2/ 474 وأحمد 6/ 30 والحاكم وصححه ووافقه الذهبي 1/ 220 والزيادة له", 1),
        HisnDua("55", "دُعَاءُ سُجُودِ التِّلَاوَةِ", "دُعَاءُ سُجُودِ التِّلَاوَةِ", "دُعَاءُ سُجُودِ التِّلَاوَةِ — الذكر 55", "دُعَاءُ سُجُودِ التِّلَاوَةِ — الذكر 55", "اللَّهُمَّ أُكْتُبْ لِي بِهَا عِنْدَكَ أَجْرًا، وَضَعْ عَنِّي بِهَا وِزْرًا، وَاجْعَلْهَا لِي عِنْدَكَ ذُخْرًا، وَتَقَبَّلْهَا مِنِّي كَمَا تَقَبَّلْتَهَا مِنْ عَبْدِكَ دَاوُدْ", "", "", "التّرمذي 2/ 473 والحاكم وصححه ووافقه الذهبي 1/ 219", 1),
        HisnDua("56", "التَّشَهُّدُ", "التَّشَهُّدُ", "التَّشَهُّدُ — الذكر 56", "التَّشَهُّدُ — الذكر 56", "التَّحِيَّاتُ لِلَّهِ، وَالصَّلَوَاتُ، وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "", "", "البخاري مع الفتح 1/ 13 ومسلم 1/ 301", 1),
        HisnDua("57", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ — الذكر 57", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ — الذكر 57", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى  إِبْرَاهِيمَ، وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ، وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ", "", "", "البخاري مع الفتح 6/ 408", 1),
        HisnDua("58", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ — الذكر 58", "الصَّلَاةُ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بَعْدَ التَّشَهُّدِ — الذكر 58", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى أَزْوَاجِهِ وَذُرِّيَتِهِ، كَمَا صَلَّيْتَ عَلَى آلِ إِبْرَاهِيمَ، وَبَارِكْ عَلَى مُحَمَّدٍ وَعَلَى أَزْوَاجِهِ وَذُرِّيَتِهِ كَمَا بَارَكْتَ عَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ", "", "", "البخاري مع الفتح 6/ 407 ومسلم 1/ 306 واللفظ له", 1),
        HisnDua("59", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 59", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 59", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ القَبْرِ، وَمِنْ عَذَابِ جَهَنَّمَ، وَمِنْ فِتْنَةِ المَحْيَا وَالمَمَاتِ، وَمِنْ شَرِّ فِتْنَةِ المَسِيحِ الدَّجَّالِ", "", "", "البخاري 2/ 102 ومسلم 1/ 412 واللفظ لمسلم", 1),
        HisnDua("60", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 60", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 60", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ القَبْرِ، وَأَعُوذُ بِكَ مِنْ فِتْنَةِ المَسِيحِ الدَّجَّالِ، وَأَعُوذُ بِكَ مِنْ فِتْنَةِ المَحْيَا وَالمَمَاتِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ المَأْثَمِ وَالمَغْرَمِ", "", "", "البخاري 1/ 202 ومسلم 1/ 412", 1),
        HisnDua("61", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 61", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 61", "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا، وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّكَ أَنْتَ الغَفُورُ الرَّحِيمُ", "", "", "البخاري 8/ 168 ومسلم 4/ 2078", 1),
        HisnDua("62", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 62", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 62", "اللَّهُمَّ اغْفِرْ لِي مَا قَدَّمْتُ، وَمَا أَخَّرْتُ، وَمَا أَسْرَرْتُ، وَمَا أَعْلَنْتُ، وَمَا أَسْرَفْتُ، وَمَا أَنْتَ أَعْلَمُ بِهِ مِنِّي. أَنْتَ المُقَدِّمُ، وَأَنْتَ المُؤَخِّرُ لَا إِلَهَ إِلَّا أَنْتَ", "", "", "مسلم 1/ 534", 1),
        HisnDua("63", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 63", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 63", "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ", "", "", "أبو داود 2/ 86 والنسائي 3/ 53 وصححه الألباني في صحيح أبو داود 1/ 284", 1),
        HisnDua("64", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 64", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 64", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ البُخْلِ وَأَعُوذُ بِكَ مِنَ الجُبْنِ، وَأَعُوذُ بِكَ مِنْ أَنْ أُرَدَّ إِلَى أَرْذَلِ العُمُرِ، وَأَعُوذُ بِكَ مِنْ فِتْنَةِ الدُّنْيَا وَعَذَابِ القَبْرِ", "", "", "البخاري مع الفتح 6/ 35", 1),
        HisnDua("65", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 65", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 65", "اللَّهُمَّ إِنِّي أَسْأَلُكَ الجَنَّةَ وَأَعُوذُ بِكَ مِنَ النَّارِ", "", "", "أبو داود وانظر صحيح ابن ماجه 2/ 328", 1),
        HisnDua("66", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 66", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 66", "اللَّهُمَّ بِعِلْمِكَ الغَيْبَ وَقُدْرَتِكَ عَلَى الخَلْقِ أَحْيِنِي مَا عَلِمْتَ الحَيَاةَ خَيْرًا لِي وَتَوَفَّنِي إِذَا عَلِمْتَ الوَفَاةَ خَيْرًا لِي، اللَّهُمَّ إِنِّي أَسْأَلُكَ خَشْيَتَكَ فِي الغَيْبِ وَالشَّهَادَةِ، وَأَسْأَلُكَ كَلِمَةَ الحَقِّ فِي الرِّضَا وَالغَضَبِ، وَأَسْأَلُكَ القَصْدَ فِي الغِنَى وَالفَقْرِ، وَأَسْأَلُكَ نَعِيمًا لَا يَنْفَدُ، وَأْسْأَلُكَ قُرَّةَ عَيْنٍ لَا تَنْقَطِعُ، وَأَسْأَلُكَ الرِّضَا بَعْدَ القَضَاءِ، وَأَسْأَلُكَ بَرْدَ العَيْشِ بَعْدَ المَوْتِ، وَأَسْأَلُكَ لَذَّةَ النَّظَرِ إِلَى وَجْهِكَ وَالشَّوْقَ إِلَى لِقَائِكَ فِي غَيْرِ ضَرَّاءَ مُضِرَّةٍ وَلَا فِتْنَةٍ مُضِلَّةٍ، اللَّهُمَّ زَيِّنَا بِزِينَةِ الإِيمَانِ، وَاجْعَلْنَا هُدَاةً مُهْتَدِينَ", "", "", "النسائي 4/ 54، 55 وأحمد 4/ 364 وصححه الألباني في صحيح النسائي 1/ 281", 1),
        HisnDua("67", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 67", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 67", "اللَّهُمَّ إِنِّي أَسْأَلُكَ يَا اللَّهُ بِأَنَّكَ الوَاحِدُ الأَحَدُ الصَّمَدُ الذِي لَمْ يَلِدْ وَلَمْ يُولَدْ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدْ أَنْ تَغْفِرَ لِي ذُنُوبِي إِنَّكَ أَنْتَ الغَفُورُ الرَّحِيمُ", "", "", "أخرجه النسائي بلفظه 3/ 52 وأحمد 4/ 338 وصححه الألباني في صحيح النسائي 1/ 280", 1),
        HisnDua("68", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 68", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 68", "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِأَنَّ لَكَ الحَمْدَ، لَا إِلَهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ، المَنَّانُ، يَا بَدِيعَ السَّمَوَاتِ وَالأَرْضِ يَا ذَا الجَلَالِ وَالإِكْرَامِ، يَا حَيُّ يَا قَيُّومُ إِنِّي أَسْأَلُكَ الجَنَّةَ وَأَعُوذُ بِكَ مِنَ النَّارِ", "", "", "رواه أهل السنن وانظر صحيح ابن ماجه 2/ 329", 1),
        HisnDua("69", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 69", "الدُّعَاءُ بَعْدَ التَّشَهُّدِ الأَخِيرِ وَقَبْلَ السَّلَامِ — الذكر 69", "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِأَنِّي أَشْهَدُ أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ، الأَحَدُ الصَّمَدُ الذِي لَمْ يَلِدْ وَلَمْ يُولَدْ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدْ", "", "", "أبو داود 2/ 62 والتّرمذي 5/ 515 وابن ماجه 2/ 1267 وأحمد 5/ 360 وانظر صحيح ابن ماجه 2/ 329 وصحيح التّرمذي 3/ 163", 1),
        HisnDua("70", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 70", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 70", "أَسْتَغْفِرُ اللَّهَ (ثَلَاثًا)، اللَّهُمَّ أَنْتَ السَّلَامُ، وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الجَلَالِ وَالإِكْرَامِ", "", "", "مسلم 1/ 414", 1),
        HisnDua("71", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 71", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 71", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ وَلَا مُعْطِيَ لِمَا مَنَعْتَ وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ", "", "", "البخاري 1/ 255 ومسلم 1/ 414", 1),
        HisnDua("72", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 72", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 72", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ، لَا إِلَهَ إِلَّا اللَّهُ، وَلَا نَعْبُدُ إِلَّا إِيَّاهُ، لَهُ النِّعْمَةُ وَلَهُ الفَضْلُ، وَلَهُ الثَّنَاءُ الحَسَنُ، لَا إِلَهَ إِلَّا اللَّهُ مُخْلِصِينَ لَهُ الدِّينَ وَلَوْ كَرِهَ الكَافِرُونَ", "", "", "مسلم 1/ 415", 1),
        HisnDua("73", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 73", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 73", "سُبْحَانَ اللَّهُ، وَالحَمْدُ لِلَّهِ، وَاللَّهُ أَكْبَرُ (ثَلَاثًا وَثَلَاثِينَ مَرَّةً)\r\nلَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ (مَرَّةً وَاحِدَةً)", "", "", "\"من قال ذلك دبر كل صلاة غفرت خطاياه وإن كانت مثل زبد البحر\"مسلم 1/ 418", 1),
        HisnDua("74", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 74", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 74", "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ * مِن شَرِّ مَا خَلَقَ * وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ * وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ * وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ أَعُوذُ بِرَبِّ النَّاسِ * مَلِكِ النَّاسِ * إِلَهِ النَّاسِ * مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ * الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ * مِنَ الْجِنَّةِ وَالنَّاسِ}", "", "", "أبو داود 2/ 86 والنسائي 3/ 68 وانظر صحيح التّرمذي 2/ 8 والسور الثلاث يقال لها المعوذات وانظر فتح الباري 9/ 62", 1),
        HisnDua("75", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 75", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 75", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\r\n{اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ}", "", "", "من قرأها دبر كل صلاة لم يمنعه من دخول الجنة إلا أن يموت . والنسائي في عمل اليوم والليلة برقم 100، وابن السني برقم 121 وصححه الجامع 5/ 339 وسلسلة الأحاديث الصحيحة 2/ 697 برقم 972", 1),
        HisnDua("76", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 76", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 76", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ\r\n(بَعْدَ صَلَاةِ المَغْرِبِ وَالصُّبْحِ)", "", "", "رواه التّرمذي 5/ 515 وأحمد 4/ 227 وانظر تخريجه في زاد المعاد 1/ 300", 10),
        HisnDua("77", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 77", "الأَذْكَارُ بَعْدَ السَّلَامِ مِنَ الصَّلَاةِ — الذكر 77", "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وعَمَلًا مُتَقَبَّلًا\r\n(بَعْدَ السَّلَامِ مِنْ صَلَاةِ الفَجْرِ)", "", "", "ابن ماجه وغيره وانظر صحيح ابن ماجة 1/ 152 ومجمع الزوائد 10/ 111", 1),
        HisnDua("78", "دُعَاءُ صَلَاةِ الاسْتِخَارَةِ", "دُعَاءُ صَلَاةِ الاسْتِخَارَةِ", "دُعَاءُ صَلَاةِ الاسْتِخَارَةِ — الذكر 78", "دُعَاءُ صَلَاةِ الاسْتِخَارَةِ — الذكر 78", "قَالَ جَابِرٌ بْنُ عَبْدِ اللَّهِ رَضِيَ اللَّهُ عِنْهُمَا : كَانَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ، يُعَلِّمُنَا الاسْتِخَارَةَ فِي الأُمُورِ كُلِّهَا كَمَا يُعَلِّمُنَا السُّورَةَ مِنَ القُرْآنِ، يَقُولُ : إِذَا هَمَّ أَحَدُكُمْ بِالأَمْرِ فَلْيَرْكَعْ رَكْعَتَيْنِ مِنْ غَيْرِ الفَرِيضَةِ، ثُمَّ لِيَقُلْ: (اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ العَظِيمِ فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الغُيُوبِ، اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الأَمْرَ - وَيُسَمِّي حَاجَتَهُ - خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي - أَوْ قَالَ : عَاجِلِهِ وَآجِلِهِ - فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي، ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي - أَوْ قَالَ : عَاجِلِهِ وَآجِلِهِ - فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ، وَاقْدُرْ لِيَ الخَيْرَ حَيْثُ كَانَ، ثُمَّ أَرْضِنِي بِهِ)", "", "", "البخاري 7/ 162", 1),
        HisnDua("79", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 79", "أَذْكَارُ الصَّبَاحِ — الذكر 79", "الحَمْدُ لِلَّهِ وَحْدَهُ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى مَنْ لَا نَبِيَّ بَعْدَهُ", "", "", "عن أنس يرفعه \"لأن أقعد مع قوم يذكرون الله تعالى من صلاة الغداة حتى تطلع الشمس أحب إلي من أن أعتق أربعة من ولد إسماعيل، ولأن أقعد مع قوم يذكرون الله من صلاة العصر إلى أن تغرب الشمس أحب إلي من أن أعتق أربعة\"أبو داود برقم 3667، وحسنه الألباني، صحيح أبو داود 2/ 698", 1),
        HisnDua("80", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 80", "أَذْكَارُ الصَّبَاحِ — الذكر 80", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\r\n{اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ}", "", "", "سورة البقرة، آية 255، \"من قالها حين يصبح أجير من الجن حتى يمسي، ومن قالها حين يمسي أجير من الجن حتى يصبح\"أخرجه الحاكم 1/ 562 وصححه الألباني في صحيح الترغيب والترهيب 1/ 273، وعزاه إلى النسائي والطبراني وقال : وإسناد الطبراني جيد", 1),
        HisnDua("81", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 81", "أَذْكَارُ الصَّبَاحِ — الذكر 81", "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ * مِن شَرِّ مَا خَلَقَ * وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ * وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ * وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ أَعُوذُ بِرَبِّ النَّاسِ * مَلِكِ النَّاسِ * إِلَهِ النَّاسِ * مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ * الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ * مِنَ الْجِنَّةِ وَالنَّاسِ}", "", "", "من قالها ثلاث مرات حين يصبح وحين يمسي كفته من كل شيء. أبو داود 4/ 322 والتّرمذي 5/ 567 وانظر صحيح التّرمذي 3/ 182", 3),
        HisnDua("82", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 82", "أَذْكَارُ الصَّبَاحِ — الذكر 82", "أَصْبَحْنَا وَأَصْبَحَ المُلْكُ لِلَّهِ، وَالحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا اليَوْمِ وَخَيْرَ مَا بَعْدَهُ وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا اليَوْمِ وَشَرِّ مَا بَعْدَهُ رَبِّ أَعُوذُ بِكَ مِنَ الكَسَلِ وَسُوءِ الكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي القَبْرِ", "", "", "مسلم 4/ 2088", 1),
        HisnDua("83", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 83", "أَذْكَارُ الصَّبَاحِ — الذكر 83", "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ", "", "", "التّرمذي 5/ 466 وانظر صحيح التّرمذي 3/ 142", 1),
        HisnDua("84", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 84", "أَذْكَارُ الصَّبَاحِ — الذكر 84", "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُك، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي، فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ", "", "", "من قالها موقنًا بها حين يمسي فمات من ليلته دخل الجنة، وكذلك إذا أصبح أخرجه البخاري 7/ 150", 1),
        HisnDua("85", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 85", "أَذْكَارُ الصَّبَاحِ — الذكر 85", "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ", "", "", "من قالها حين يصبح وحين يمسي أربع مرات أعتقه الله من النار . أخرجه أبو داود 4/ 317 والبخاري في الأدب المفرد برقم 1201 والنسائي في عمل اليوم والليلة برقم 9، وابن السني برقم 70 وحسن سماحة الشيخ ابن باز إسناد النسائي وأبي داود في تحفة الأخيار ص 23", 4),
        HisnDua("86", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 86", "أَذْكَارُ الصَّبَاحِ — الذكر 86", "اللَّهُمَّ مَا أَصْبَحَ بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ، فَلَكَ الحَمْدُ وَلَكَ الشُّكْرُ", "", "", "من قالها حين يصبح فقد أدي شكر يومه، ومن قالها حين يمسى فقد أدى شكر ليلته. أخرجه أبو داود 4/ 318، والنسائي في عمل اليوم والليلة برقم 7 وابن السني برقم 41 وابن حبان \"موارد\"رقم 2361 وحسن ابن باز إسناده في تحفة الأخيار ص 24", 1),
        HisnDua("87", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 87", "أَذْكَارُ الصَّبَاحِ — الذكر 87", "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَه إِلَّا أَنْتَ. اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الكُفْرِ، وَالفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ القَبْرِ، لَا إِلَهَ إِلَّا أَنْتَ", "", "", "أبو داود 4/ 324، وأحمد 5/ 42 والنسائي في عمل اليوم والليلة برقم 22 وابن السني برقم 69 والبخاري في الأدب المفرد، وحسن العلامة ابن باز إسناده في تحفة الأخيار ص 26", 3),
        HisnDua("88", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 88", "أَذْكَارُ الصَّبَاحِ — الذكر 88", "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ العَرْشِ العَظِيمِ", "", "", "من قالها حين يصبح ويمسي سبع مرات كفاه الله ما أهمه من أمر الدنيا والآخرة. أخرجه ابن السني برقم 71 مرفوعًا وأبو موقوف 4/ 321، وصحح إسناده شعيب وعبد القادر الأرناؤوط. انظر زاد المعاد 2/ 376", 7),
        HisnDua("89", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 89", "أَذْكَارُ الصَّبَاحِ — الذكر 89", "اللَّهُمَّ إِنِّي أَسْأَلُكَ العَفْوَ وَالعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ العَفْوَ وَالعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي، اللَّهُمَّ احْفَظْنِي مِنْ بَيْنِ يَدَيَّ وَمِنْ خَلْفِي وَعَنْ يَمِينِي وَعَنْ شِمَالِي وَمِنْ فَوْقِي وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي", "", "", "أبو داود وابن ماجه وانظر صحيح ابن ماجه 2/ 332", 1),
        HisnDua("90", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 90", "أَذْكَارُ الصَّبَاحِ — الذكر 90", "اللَّهُمَّ عَالِمَ الغَيْبِ وَالشَّهَادَةِ، فَاطِرَ السَّمَوَاتِ وَالأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي وَمِنْ شَرِّ الشَّيْطَانِ وَشِرْكِهِ وَأَنْ أَقْتَرِفَ عَلَى نَفْسِي سُوءًا أَوْ أَجُرَّهُ إِلَى مُسْلِمٍ", "", "", "التّرمذي وأبو داود .انظر :صحيح التّرمذي 3/ 142", 1),
        HisnDua("91", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 91", "أَذْكَارُ الصَّبَاحِ — الذكر 91", "بِسْمِ اللَّهِ الذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ العَلِيمُ", "", "", "من قالها ثلاثًا إذا أصبح وثلاثًا إذا أمسى لم يضره شيء أخرجه أبو داود 4/ 323 والتّرمذي 5/ 465 وابن ماجه وأحمد. انظر: صحيح ابن ماجه 2/ 332 وحسن إسناده العلامة ابن باز في تحفة الأخيار ص 39", 3),
        HisnDua("92", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 92", "أَذْكَارُ الصَّبَاحِ — الذكر 92", "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلَامِ دِينًا وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا", "", "", "من قالها ثلاثًا حين يصبح وحين يمسي كان حقًا على الله أن يرضيه يوم القيامة. أحمد 4/ 337 والنسائي في عمل اليوم والليلة برقم 4 وابن السني برقم 68 وأبو داود 4/ 418 والتّرمذي 5/ 465 وحسنه ابن باز في تحفة الأخيار ص 39", 3),
        HisnDua("93", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 93", "أَذْكَارُ الصَّبَاحِ — الذكر 93", "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ", "", "", "الحاكم وصححه ووافقه الذهبي 1/ 545 وانظر صحيح الترغيب والترهيب 1/ 273", 1),
        HisnDua("94", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 94", "أَذْكَارُ الصَّبَاحِ — الذكر 94", "أَصْبَحْنَا وَأَصْبَحَ المُلْكُ لِلَّهِ رَبِّ العَالَمِينَ، اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ هَذَا اليَوْمِ: فَتْحَهُ، وَنَصْرَهُ، وَنُورَهُ، وَبَرَكَتَهُ، وَهُدَاهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِيهِ وَشَرِّ مَا بَعْدَهُ", "", "", "أبو داود 4/ 322 وحسن إسناده شعيب وعبد القادر الأرناؤوط في تحقيق زاد المعاد 2/ 273", 1),
        HisnDua("95", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 95", "أَذْكَارُ الصَّبَاحِ — الذكر 95", "أَصْبَحْنَا عَلَى فِطْرَةِ الإِسْلَامِ وَعَلَى كَلِمَةِ الإِخْلَاصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ، وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ المُشْرِكِينَ", "", "", "أحمد 3/ 406 و 407 وابن السني في عمل اليوم والليلة برقم 34 وانظر: صحيح الجامع 4/ 209", 1),
        HisnDua("96", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 96", "أَذْكَارُ الصَّبَاحِ — الذكر 96", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "", "", "من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما جاء به إلا أحد قال مثل ما قال أو زاد رواه مسلم 4/ 2071", 100),
        HisnDua("97", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 97", "أَذْكَارُ الصَّبَاحِ — الذكر 97", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "", "", "عَشرَ مَرَّاتٍ:\r\n النسائي في عمل اليوم والليلة برقم 24 وانظر :صحيح الترغيب والترهيب 1/ 272، وتحفة الأخيار لابن باز ص 44 وانظر فضلها في ص 146، حديث رقم 255\r\n\r\nأو مرة واحدة عند الكسل\r\nأبو داود 4/ 319 وابن ماجه وأحمد 4/ 60 وانظر :صحيح الترغيب والترهيب 1/ 270، صحيح أبو داود 3/ 957، وصحيح ابن ماجه 2/ 331 وزاد المعاد 2/ 377", 10),
        HisnDua("98", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 98", "أَذْكَارُ الصَّبَاحِ — الذكر 98", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "", "", "من قالها مائة مرة في يوم كانت له عدل عشر رقاب، وكتب له مائة حسنة، ومحيت عنه مائة سيئة، وكانت حرزًا من الشيطان يومه ذلك حتى يمسي، ولم يأت أحد بأفضل مما جاء به إلا أحد عمل أكثر من ذلك. البخاري 4/ 95، ومسلم 4/ 2071", 100),
        HisnDua("99", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 99", "أَذْكَارُ الصَّبَاحِ — الذكر 99", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ وَرِضَا نَفْسِهِ وَزِنَةَ عَرْشِهِ وَمِدَادَ كَلِمَاتِهِ", "", "", "مسلم 4/ 2090", 3),
        HisnDua("100", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 100", "أَذْكَارُ الصَّبَاحِ — الذكر 100", "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا", "", "", "أخرجه ابن السني في عمل اليوم والليلة برقم 54 وابن ماجه برقم 925 وحسن إسناده عبد القادر وشعيب الأرناؤوط في تحقيق زاد المعاد 2/ 375", 1),
        HisnDua("101", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 101", "أَذْكَارُ الصَّبَاحِ — الذكر 101", "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "", "", "البخاري مع الفتح 11/ 101، ومسلم 4/ 2075", 100),
        HisnDua("102", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ", "أَذْكَارُ الصَّبَاحِ — الذكر 102", "أَذْكَارُ الصَّبَاحِ — الذكر 102", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", "", "", "من صلى علي حين يُصبح عشرًا، وحين يُمسي عشرًا أدركتهُ شفاعتي يوم القيامة\r\nأخرجه الطبراني بإسنادين أحدهما جيد، انظر: الزوائد 10/ 120 وصحيح الترغيب والترهيب 1/ 273", 10),
        HisnDua("103", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 103", "أَذْكَارُ المَسَاءِ — الذكر 103", "الحَمْدُ لِلَّهِ وَحْدَهُ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى مَنْ لَا نَبِيَّ بَعْدَهُ", "", "", "عن أنس يرفعه \"لأن أقعد مع قوم يذكرون الله تعالى من صلاة الغداة حتى تطلع الشمس أحب إلي من أن أعتق أربعة من ولد إسماعيل، ولأن أقعد مع قوم يذكرون الله من صلاة العصر إلى أن تغرب الشمس أحب إلي من أن أعتق أربعة\"أبو داود برقم 3667، وحسنه الألباني، صحيح أبو داود 2/ 698", 1),
        HisnDua("104", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 104", "أَذْكَارُ المَسَاءِ — الذكر 104", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\r\n{اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ}", "", "", "سورة البقرة، آية 255، \"من قالها حين يصبح أجير من الجن حتى يمسي، ومن قالها حين يمسي أجير من الجن حتى يصبح\"أخرجه الحاكم 1/ 562 وصححه الألباني في صحيح الترغيب والترهيب 1/ 273، وعزاه إلى النسائي والطبراني وقال : وإسناد الطبراني جيد", 1),
        HisnDua("105", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 105", "أَذْكَارُ المَسَاءِ — الذكر 105", "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n{قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ * مِن شَرِّ مَا خَلَقَ * وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ * وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ * وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\r\n(قُلْ أَعُوذُ بِرَبِّ النَّاسِ * مَلِكِ النَّاسِ * إِلَهِ النَّاسِ * مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ * الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ * مِنَ الْجِنَّةِ وَالنَّاسِ)", "", "", "من قالها ثلاث مرات حين يصبح وحين يمسي كفته من كل شيء. أبو داود 4/ 322 والتّرمذي 5/ 567 وانظر صحيح التّرمذي 3/ 182", 3),
        HisnDua("106", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 106", "أَذْكَارُ المَسَاءِ — الذكر 106", "أَمْسَيْنَا وَأَمْسَى المُلْكُ لِلَّهِ، وَالحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرَّ مَا بَعْدَهَا رَبِّ أَعُوذُ بِكَ مِنَ الكَسَلِ وَسُوءِ الكِبَرِ، رَبَّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي القَبْرِ", "", "", "مسلم 4/ 2088", 1),
        HisnDua("107", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 107", "أَذْكَارُ المَسَاءِ — الذكر 107", "اللَّهُمَّ بِكَ أَمْسَيْنَا وَبِكَ أَصْبَحْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ المَصِيرُ", "", "", "التّرمذي 5/ 466 وانظر صحيح التّرمذي 3/ 142", 1),
        HisnDua("108", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 108", "أَذْكَارُ المَسَاءِ — الذكر 108", "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُك، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي، فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ", "", "", "من قالها موقنًا بها حين يمسي فمات من ليلته دخل الجنة، وكذلك إذا أصبح أخرجه البخاري 7/ 150", 1),
        HisnDua("109", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 109", "أَذْكَارُ المَسَاءِ — الذكر 109", "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ", "", "", "من قالها حين يصبح وحين يمسي أربع مرات أعتقه الله من النار . أخرجه أبو داود 4/ 317 والبخاري في الأدب المفرد برقم 1201 والنسائي في عمل اليوم والليلة برقم 9، وابن السني برقم 70 وحسن سماحة الشيخ ابن باز إسناد النسائي وأبي داود في تحفة الأخيار ص 23", 4),
        HisnDua("110", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 110", "أَذْكَارُ المَسَاءِ — الذكر 110", "اللَّهُمَّ مَا أَمْسَى بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ، فَلَكَ الحَمْدُ وَلَكَ الشُّكْرُ", "", "", "من قالها حين يصبح فقد أدي شكر يومه، ومن قالها حين يمسى فقد أدى شكر ليلته. أخرجه أبو داود 4/ 318، والنسائي في عمل اليوم والليلة برقم 7 وابن السني برقم 41 وابن حبان \"موارد\"رقم 2361 وحسن ابن باز إسناده في تحفة الأخيار ص 24", 1),
        HisnDua("111", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 111", "أَذْكَارُ المَسَاءِ — الذكر 111", "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَه إِلَّا أَنْتَ. اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الكُفْرِ، وَالفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ القَبْرِ، لَا إِلَهَ إِلَّا أَنْتَ", "", "", "أبو داود 4/ 324، وأحمد 5/ 42 والنسائي في عمل اليوم والليلة برقم 22 وابن السني برقم 69 والبخاري في الأدب المفرد، وحسن العلامة ابن باز إسناده في تحفة الأخيار ص 26", 3),
        HisnDua("112", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 112", "أَذْكَارُ المَسَاءِ — الذكر 112", "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ العَرْشِ العَظِيمِ", "", "", "من قالها حين يصبح ويمسي سبع مرات كفاه الله ما أهمه من أمر الدنيا والآخرة. أخرجه ابن السني برقم 71 مرفوعًا وأبو موقوف 4/ 321، وصحح إسناده شعيب وعبد القادر الأرناؤوط. انظر زاد المعاد 2/ 376", 7),
        HisnDua("113", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 113", "أَذْكَارُ المَسَاءِ — الذكر 113", "اللَّهُمَّ إِنِّي أَسْأَلُكَ العَفْوَ وَالعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ العَفْوَ وَالعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي، اللَّهُمَّ احْفَظْنِي مِنْ بَيْنِ يَدَيَّ وَمِنْ خَلْفِي وَعَنْ يَمِينِي وَعَنْ شِمَالِي وَمِنْ فَوْقِي وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي", "", "", "أبو داود وابن ماجه وانظر صحيح ابن ماجه 2/ 332", 1),
        HisnDua("114", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 114", "أَذْكَارُ المَسَاءِ — الذكر 114", "اللَّهُمَّ عَالِمَ الغَيْبِ وَالشَّهَادَةِ، فَاطِرَ السَّمَوَاتِ وَالأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي وَمِنْ شَرِّ الشَّيْطَانِ وَشِرْكِهِ وَأَنْ أَقْتَرِفَ عَلَى نَفْسِي سُوءًا أَوْ أَجُرَّهُ إِلَى مُسْلِمٍ", "", "", "التّرمذي وأبو داود .انظر :صحيح التّرمذي 3/ 142", 1),
        HisnDua("115", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 115", "أَذْكَارُ المَسَاءِ — الذكر 115", "بِسْمِ اللَّهِ الذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ العَلِيمُ", "", "", "من قالها ثلاثًا إذا أصبح وثلاثًا إذا أمسى لم يضره شيء أخرجه أبو داود 4/ 323 والتّرمذي 5/ 465 وابن ماجه وأحمد. انظر: صحيح ابن ماجه 2/ 332 وحسن إسناده العلامة ابن باز في تحفة الأخيار ص 39", 3),
        HisnDua("116", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 116", "أَذْكَارُ المَسَاءِ — الذكر 116", "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلَامِ دِينًا وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا", "", "", "من قالها ثلاثًا حين يصبح وحين يمسي كان حقًا على الله أن يرضيه يوم القيامة. أحمد 4/ 337 والنسائي في عمل اليوم والليلة برقم 4 وابن السني برقم 68 وأبو داود 4/ 418 والتّرمذي 5/ 465 وحسنه ابن باز في تحفة الأخيار ص 39", 3),
        HisnDua("117", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 117", "أَذْكَارُ المَسَاءِ — الذكر 117", "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ", "", "", "الحاكم وصححه ووافقه الذهبي 1/ 545 وانظر صحيح الترغيب والترهيب 1/ 273", 1),
        HisnDua("118", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 118", "أَذْكَارُ المَسَاءِ — الذكر 118", "أَمْسَيْنَا وَأَمْسَى المُلْكُ لِلَّهِ رَبِّ العَالَمِينَ، اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ هَذِهِ اللَّيْلَةِ: فَتْحَهَا، وَنَصْرَهَا، وَنُورَهَا، وَبَرَكَتَهَا، وَهُدَاهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِيهَا وَشَرِّ مَا بَعْدَهَا", "", "", "أبو داود 4/ 322 وحسن إسناده شعيب وعبد القادر الأرناؤوط في تحقيق زاد المعاد 2/ 273", 1),
        HisnDua("119", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 119", "أَذْكَارُ المَسَاءِ — الذكر 119", "أَمْسَيْنَا عَلَى فِطْرَةِ الإِسْلَامِ وَعَلَى كَلِمَةِ الإِخْلَاصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ، وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ المُشْرِكِينَ", "", "", "أحمد 3/ 406 و 407 وابن السني في عمل اليوم والليلة برقم 34 وانظر: صحيح الجامع 4/ 209", 1),
        HisnDua("120", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 120", "أَذْكَارُ المَسَاءِ — الذكر 120", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "", "", "من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما جاء به إلا أحد قال مثل ما قال أو زاد رواه مسلم 4/ 2071", 100),
        HisnDua("121", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 121", "أَذْكَارُ المَسَاءِ — الذكر 121", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "", "", "عَشرَ مَرَّاتٍ:\r\n النسائي في عمل اليوم والليلة برقم 24 وانظر :صحيح الترغيب والترهيب 1/ 272، وتحفة الأخيار لابن باز ص 44 وانظر فضلها في ص 146، حديث رقم 255\r\n\r\nأو مرة واحدة عند الكسل\r\nأبو داود 4/ 319 وابن ماجه وأحمد 4/ 60 وانظر :صحيح الترغيب والترهيب 1/ 270، صحيح أبو داود 3/ 957، وصحيح ابن ماجه 2/ 331 وزاد المعاد 2/ 377", 10),
        HisnDua("122", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 122", "أَذْكَارُ المَسَاءِ — الذكر 122", "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "", "", "البخاري مع الفتح 11/ 101، ومسلم 4/ 2075", 100),
        HisnDua("123", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 123", "أَذْكَارُ المَسَاءِ — الذكر 123", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ", "", "", "من قالها حين يمسي ثلاث مرات لم تضره حمة تلك الليلة أخرجه أحمد 2/ 290 والنسائي في عمل اليوم والليلة برقم 590 وابن السني برقم 68 وانظر: صحيح التّرمذي 3/ 187، وصحيح ابن ماجه 2/ 266 وتحفة الأخيار ص 45", 3),
        HisnDua("124", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ", "أَذْكَارُ المَسَاءِ — الذكر 124", "أَذْكَارُ المَسَاءِ — الذكر 124", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", "", "", "من صلى علي حين يُصبح عشرًا، وحين يُمسي عشرًا أدركتهُ شفاعتي يوم القيامة\r\nأخرجه الطبراني بإسنادين أحدهما جيد، انظر: الزوائد 10/ 120 وصحيح الترغيب والترهيب 1/ 273", 10),
        HisnDua("125", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 125", "أَذْكَارُ النَّوْمِ — الذكر 125", "يَجْمَعُ كَفَّيْهِ ثُمَّ يَنْفُثُ فِيهِمَا، فَيَقْرَأُ فِيهِمَا:\r\n\r\nبِسْمِ اللَّهِ الرَّحْمِنِ الرَّحِيمِ\r\n{قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمِنِ الرَّحِيمِ\r\n{قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ * مِن شَرِّ مَا خَلَقَ * وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ * وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ * وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ}\r\n\r\nبِسْمِ اللَّهِ الرَّحْمِنِ الرَّحِيمِ\r\n{قُلْ أَعُوذُ بِرَبِّ النَّاسِ * مَلِكِ النَّاسِ * إِلَهِ النَّاسِ * مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ * الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ * مِنَ الْجِنَّةِ وَالنَّاسِ}\r\n\r\nثُمَّ يَمْسَحُ بِهِمَا مَا اسْتَطَاعَ مِنْ جَسَدِهِ يَبْدَأُ بِهِمَا عَلَى رَأْسِهِ وَوَجْهِهِ وَمَا أَقْبَلَ مِنْ جَسَدِهِ", "", "", "البخاري مع الفتح 9/ 62 ومسلم 4/ 1723", 3),
        HisnDua("126", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 126", "أَذْكَارُ النَّوْمِ — الذكر 126", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\r\n{اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ}", "", "", "من قرأها إذا أوى إلى فراشه فإنه لن يزال عليه من الله حافظ ولا يقربه شيطان حتى يصبح البخاري مع الفتح 4/ 487", 1),
        HisnDua("127", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 127", "أَذْكَارُ النَّوْمِ — الذكر 127", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\r\n{آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ كُلٌّ آمَنَ بِاللّهِ وَمَلآئِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لاَ نُفَرِّقُ بَيْنَ أَحَدٍ مِّن رُّسُلِهِ وَقَالُواْ سَمِعْنَا وَأَطَعْنَا غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ * لاَ يُكَلِّفُ اللّهُ نَفْسًا إِلاَّ وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ رَبَّنَا لاَ تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا رَبَّنَا وَلاَ تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا رَبَّنَا وَلاَ تُحَمِّلْنَا مَا لاَ طَاقَةَ لَنَا بِهِ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا أَنتَ مَوْلاَنَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ}", "", "", "من قرأهما في ليلة كفتاه، البخاري مع الفتح 9/ 94 ومسلم 1/ 554، والآيتان من سورة البقرة، 285 – 286", 1),
        HisnDua("128", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 128", "أَذْكَارُ النَّوْمِ — الذكر 128", "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ", "", "", "إذا قام أحدكم من فراشه ثم رجع إليه فلينفض بصنفة  إزاره ثلاث مرات وليسم الله  فإنه لا يدري ماذا خلفه عليه بعده وإذا اضطجع فليقل ... (الحديث)\r\n\r\n البخاري 11/ 126. ومسلم 4/ 2084", 1),
        HisnDua("129", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 129", "أَذْكَارُ النَّوْمِ — الذكر 129", "اللَّهُمَّ إِنَّكَ خَلَقْتَ نَفْسِي وَأَنْتَ تَوَفَّاهَا، لَكَ مَمَاتُهَا وَمَحْيَاهَا، إِنْ أَحْيَيْتَهَا فَاحْفَظْهَا، وَإِنْ أَمَتَّهَا فَاغْفِرْ لَهَا، اللَّهُمَّ إِنِّي أَسْأَلُكَ العَافِيَةَ", "", "", "أخرجه مسلم 4/ 2083 وأحمد بلفظه 2/ 79", 1),
        HisnDua("130", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 130", "أَذْكَارُ النَّوْمِ — الذكر 130", "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ", "", "", "كان رسول الله صلى الله عليه وسلم إذا أراد أن يرقد وضع يده اليمنى تحت خده ثم يقول ... (الحديث)\r\n\r\nأبو داود بلفظه 4/ 311 وانظر صحيح التّرمذي 3/ 143", 3),
        HisnDua("131", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 131", "أَذْكَارُ النَّوْمِ — الذكر 131", "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", "", "", "البخاري مع الفتح 11/ 113 ومسلم 4/ 2083", 1),
        HisnDua("132", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 132", "أَذْكَارُ النَّوْمِ — الذكر 132", "سُبْحَانَ اللَّهِ", "", "", "من قال ذلك عندما يأوي إلى فراشه كان خيرًا له من خادم. البخاري مع الفتح 7/ 71 ومسلم 4/ 2091", 33),
        HisnDua("133", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 133", "أَذْكَارُ النَّوْمِ — الذكر 133", "الحَمْدُ لِلَّهِ", "", "", "من قال ذلك عندما يأوي إلى فراشه كان خيرًا له من خادم. البخاري مع الفتح 7/ 71 ومسلم 4/ 2091", 33),
        HisnDua("134", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 134", "أَذْكَارُ النَّوْمِ — الذكر 134", "اللَّهُ أَكْبَرُ", "", "", "من قال ذلك عندما يأوي إلى فراشه كان خيرًا له من خادم. البخاري مع الفتح 7/ 71 ومسلم 4/ 2091", 34),
        HisnDua("135", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 135", "أَذْكَارُ النَّوْمِ — الذكر 135", "اللَّهُمَّ رَبَّ السَّمَوَاتِ السَّبْعِ وَرَبَّ الأَرْضِ، وَرَبَّ العَرْشِ العَظِيمِ، رَبَّنَا وَرَبَّ كُلَّ شَيْءٍ، فَالِقَ الحَبِّ وَالنَّوَى، وَمُنْزِلَ التَّوْرَاةِ وَالِإنْجِيلِ، وَالفُرْقَانِ، أَعُوذُ بِكَ مِنْ شَرِّ كُلِّ شَيْءٍ أَنْتَ آخِذٌ بِنَاصِيَتِهِ، اللَّهُمَّ أَنْتَ الأَوَّلُ فَلَيْسَ قَبْلَكَ شَيْءٌ، وَأَنْتَ الآخِرُ فَلَيْسَ بَعْدَكَ شَيْءٌ، وَأَنْتَ الظَّاهِرُ فَلَيْسَ فَوْقَكَ شَيْءٌ، وَأَنْتَ البَاطِنُ فَلَيْسَ دُونَكَ شَيْءٌ، اقْضِ عَنَّا الدَّيْنَ وَاغْنِنَا مِنَ الفَقْرِ", "", "", "مسلم 4/ 2084", 1),
        HisnDua("136", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 136", "أَذْكَارُ النَّوْمِ — الذكر 136", "الحَمْدُ لِلَّهِ الذِي أَطْعَمَنَا وَسَقَانَا وَكَفَانَا وَآوَانَا فَكَمْ مِمَّنْ لَا كَافِيَ لَهُ وَلَا مُؤْوِي", "", "", "مسلم 4/ 2085", 1),
        HisnDua("137", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 137", "أَذْكَارُ النَّوْمِ — الذكر 137", "اللَّهُمَّ عَالِمَ الغَيْبِ وَالشَّهَادَةِ، فَاطِرَ السَّمَوَاتِ وَالأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي، وَمِنْ شَرِّ الشَّيْطَانِ وَشَرَكِهِ، وَأَنْ أَقْتَرِفَ عَلَى نَفْسِي سُوءًا أَوْ أَجُرَّه إِلَى مُسْلِمٍ", "", "", "أبو داود 4/ 317 وانظر صحيح سنن التّرمذي 3/ 142", 1),
        HisnDua("138", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 138", "أَذْكَارُ النَّوْمِ — الذكر 138", "يَقْرَأُ {ألم * تَنْزِيل ...} (سُورَةُ السَّجْدَة)\r\nوَ{تَبَارَكَ الذِي بِيَدِهِ المُلْكُ ...} (سُورَةُ المُلْكِ)", "", "", "التّرمذي والنسائي وانظر صحيح الجامع 4/ 255", 1),
        HisnDua("139", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ", "أَذْكَارُ النَّوْمِ — الذكر 139", "أَذْكَارُ النَّوْمِ — الذكر 139", "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَوَجَّهْتُ وَجْهِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، رَهْبَةً وَرَغْبَةً إِلَيْكَ، لَا مَلْجَأَ وَلَا مَنْجَا مِنْكَ إِلَّا إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الذِي أَنْزَلْتَ وَبِنَبِيِّكَ الذِي أَرْسَلْتَ", "", "", "إذا أخذت مضجعك فتوضأ وضوءك للصلاة ثم اضطجع على شقك الأيمن، وقل: ... (الحديث)  قال صلى الله عليه وسلم لمن قال ذلك: فإن متَّ، متَّ على الفطرة البخاري مع الفتح 11/ 113 ومسلم 4/ 2081", 1),
        HisnDua("140", "الدُّعَاءُ إِذَا تَقَلَّبَ لَيْلًا", "الدُّعَاءُ إِذَا تَقَلَّبَ لَيْلًا", "الدُّعَاءُ إِذَا تَقَلَّبَ لَيْلًا — الذكر 140", "الدُّعَاءُ إِذَا تَقَلَّبَ لَيْلًا — الذكر 140", "لَا إِلَهِ إِلَّا اللَّهُ الوَاحِدُ القَهَّارُ، رَبُّ السَّمَوَاتِ وَالأَرْضِ وَمَا بَيْنَهُمَا العَزِيزُ الغَفَّارُ", "", "", "يقول ذلك إذا تقلّب من جنب إلى جنب. أخرجه الحاكم وصححه ووافقه الذّهبي 1/ 540 والنّسائي في عمل اليوم والّيلة، وابن السّني، وانظر صحيح الجامع 4/ 213", 1),
        HisnDua("141", "دُعَاءُ الفَزَعِ فِي النَّوْمِ وَمَنْ بُلِيَ بِالوَحْشَةِ", "دُعَاءُ الفَزَعِ فِي النَّوْمِ وَمَنْ بُلِيَ بِالوَحْشَةِ", "دُعَاءُ الفَزَعِ فِي النَّوْمِ وَمَنْ بُلِيَ بِالوَحْشَةِ — الذكر 141", "دُعَاءُ الفَزَعِ فِي النَّوْمِ وَمَنْ بُلِيَ بِالوَحْشَةِ — الذكر 141", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ غَضَبِهِ وَعِقَابِهِ، وَشَرِّ عِبَادِهِ، وَمِنْ هَمَزَاتِ الشَّيَاطِينِ وَأَنْ يَحْضُرُونِ", "", "", "أبو داود 4/ 12 وانظر صحيح التّرمذي 3/ 171", 1),
        HisnDua("142", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 142", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 142", "يَنْفُثُ عَنْ يَسَارِهِ", "", "", "مسلم 4/ 1772", 3),
        HisnDua("143", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 143", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 143", "يَسْتَعِيذُ بِاللَّهِ مِنَ الشَّيْطَانِ وَمِنْ شَرِّ مَا رَأَى", "", "", "مسلم 4/ 1772، 1773", 3),
        HisnDua("144", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 144", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 144", "لَا يُحَدِّثُ بِهَا أَحَدًا", "", "", "مسلم 4/ 1772", 1),
        HisnDua("145", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 145", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 145", "يَتَحَوَّلَ عَنْ جَنْبِهِ الذِي كَانَ عَلَيْهِ", "", "", "مسلم 4/ 1773", 1),
        HisnDua("146", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 146", "مَا يَفْعَلُ مَنْ رَأَى الرُّؤْيَا أَوِ الحُلُمَ — الذكر 146", "يَقُومُ يُصَلِّي إِنْ أَرَادَ ذَلِكَ", "", "", "مسلم 4/ 1773", 1),
        HisnDua("147", "دُعَاءُ قُنُوتِ الوِتْرِ", "دُعَاءُ قُنُوتِ الوِتْرِ", "دُعَاءُ قُنُوتِ الوِتْرِ — الذكر 147", "دُعَاءُ قُنُوتِ الوِتْرِ — الذكر 147", "اللَّهُمَّ اهْدِنِي فِيمَنْ هَدَيْتَ، وَعَافِنِي فِيمَنْ عَافَيْتَ، وَتَوَلَّنِي فِيمَنْ تَوَلَّيْتَ، وَبَارِكْ لِي فِيمَا أَعْطَيْتَ، وَقِنِي شَرَّ مَا قَضَيْتَ، فَإِنَّكَ تَقْضِي وَلَا يُقْضَى عَلَيْكَ، إِنَّهُ لَا يَذِلُّ مَنْ وَاَلَيْتَ، [وَلَا يَعِزُّ مَنْ عَادَيْتَ]، تَبَارَكْتَ رَبَّنَا وَتَعَالَيْتَ", "", "", "أخرجه أصحاب السّنن الأربعة، وأحمد والدّارمي والحاكم والبيهقيّ، ومابين المعكوفين للبيهقي، وانظر صحيح التّرمذي 1/ 144 وصحيح ابن ماجة 1/ 194 وإرواء الغليل للألباني 2/ 172", 1),
        HisnDua("148", "دُعَاءُ قُنُوتِ الوِتْرِ", "دُعَاءُ قُنُوتِ الوِتْرِ", "دُعَاءُ قُنُوتِ الوِتْرِ — الذكر 148", "دُعَاءُ قُنُوتِ الوِتْرِ — الذكر 148", "اللَّهُمَّ إِنِّي أَعُوذُ بِرِضَاكَ مِنْ سَخَطِكَ، وَبِمُعَافَاتِكَ مِنْ عُقُوبَتِكَ، وأَعُوذُ بِكَ مِنْكَ، لَا أُحْصِي ثَنَاءً عَلَيْكَ، أَنْتَ كَمَا أَثْنَيْتَ عَلَى نَفْسِكَ", "", "", "أخرجه أصحاب السّنن الأربعة، وأحمد. انظر صحيح التّرمذي 3/ 180 وصحيح ابن ماجة 1/ 194، والإرواء 2/ 175", 1),
        HisnDua("149", "دُعَاءُ قُنُوتِ الوِتْرِ", "دُعَاءُ قُنُوتِ الوِتْرِ", "دُعَاءُ قُنُوتِ الوِتْرِ — الذكر 149", "دُعَاءُ قُنُوتِ الوِتْرِ — الذكر 149", "اللَّهُمَّ إِيَّاكَ نَعْبُدُ، وَلَكَ نُصَلِّي وَنَسْجُدُ، وَإِلَيْكَ نَسْعَى وَنَحْفِدُ، نَرْجُو رَحْمَتَكَ وَنَخْشَى عَذَابَكَ، إِنَّ عَذَابَكَ بِالكَافِرِينَ مُلْحَقٌ. اللَّهُمَّ إِنَّا نَسْتَعِينُكَ، وَنَسْتَغْفِرُكَ، وَنُثْنِي عَلَيْكَ الخَيْرَ، وَلَا نَكْفُرُكَ، وَنُؤْمِنُ بِكَ، وَنَخْضَعُ لَكَ، وَنَخْلَعُ مَنْ يَكْفُرُكَ", "", "", "أخرجه البيهقيّ في السّنن الكبرى وصحح إسناده 2/ 211، وقال الشّيخ الألباني في إرواء الغليل: وهذا إسناده صحيح 2/ 170. وهو موقوف على عمر", 1),
        HisnDua("150", "الذِّكْرُ عَقِبَ السَّلَامِ مِنَ الوِتْرِ", "الذِّكْرُ عَقِبَ السَّلَامِ مِنَ الوِتْرِ", "الذِّكْرُ عَقِبَ السَّلَامِ مِنَ الوِتْرِ — الذكر 150", "الذِّكْرُ عَقِبَ السَّلَامِ مِنَ الوِتْرِ — الذكر 150", "سُبْحَانَ المَلِكِ القُدُّوسِ. وَالثَّالِثَةُ يَجْهَرُ بِهَا وَيَمُدُّ بِهَا صَوْتَهُ يَقُولُ: [رَبِّ المَلَائِكَةِ وَالرُّوحِ]", "", "", "رواه النّسائي 3/ 244 والدّاراقطني وغيرهما، وما بين المعكوفين زيادة للدّاراقطني 2/ 31 وإسناده صحيح. انظر زاد المعاد بتحقيق شعيب الأرناؤوط و عبد القادر الأرناؤوط 1/ 337", 3),
        HisnDua("151", "دُعَاءُ الهَمِّ والحُزْنِ", "دُعَاءُ الهَمِّ والحُزْنِ", "دُعَاءُ الهَمِّ والحُزْنِ — الذكر 151", "دُعَاءُ الهَمِّ والحُزْنِ — الذكر 151", "اللَّهُمَّ إِنِّي عَبْدُكَ، اِبْنُ عَبْدِكَ، اِبْنُ أَمَتِكَ، نَاصِيَتِي بِيَدِكَ، مَاضٍ فِيَّ حُكْمُكَ، عَدْلٌ فِيَّ قَضَاؤُكَ، أَسْأَلُكَ بِكُلِّ اِسْمٍ هُوَ لَكَ، سَمَّيْتَ بِهِ نَفْسَكَ، أَوْ أَنْزَلْتَهُ فِي كِتَابِكَ، أَوْ عَلَّمْتَهُ أَحَدًا مِنْ خَلْقِكَ، أَوْ اِسْتَأْثَرْتَ بِهِ فِي عِلْمِ الغَيْبِ عِنْدَكَ، أَنْ تَجْعَلَ القُرْآنَ رَبِيعَ قَلْبِي، وَنُورَ صَدْرِي، وَجَلَاءَ حُزْنِي، وَذَهَابَ هَمِّي", "", "", "أحمد 1/ 391 وصحّحه الألبانيّ", 1),
        HisnDua("152", "دُعَاءُ الهَمِّ والحُزْنِ", "دُعَاءُ الهَمِّ والحُزْنِ", "دُعَاءُ الهَمِّ والحُزْنِ — الذكر 152", "دُعَاءُ الهَمِّ والحُزْنِ — الذكر 152", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الهَمِّ وَالحَزَنِ، وَالعَجْزِ وَالكَسَلِ، وَالبُخْلِ وَالجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ", "", "", "البخاري 7/ 158، كان رسول اللّه صلّى اللّه عليه وسلّم يُكثِر من هذا الدّعاء. انظر البخاري مع الفتح 11/ 173", 1),
        HisnDua("153", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ — الذكر 153", "دُعَاءُ الكَرْبِ — الذكر 153", "لَا إِلَهَ إِلَّا اللَّهُ العَظِيمُ الحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ العَرْشِ العَظِيمِ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الأَرْضِ وَرَبُّ العَرْشِ الكَرِيمِ", "", "", "البخاري 7/ 154 ومسلم 4/ 2092", 1),
        HisnDua("154", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ — الذكر 154", "دُعَاءُ الكَرْبِ — الذكر 154", "اللَّهُمَّ رَحْمَتَكَ أَرْجُو فَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ، وَأَصْلِحْ لِي شَأْنِي كُلَّهُ، لَاإِلَهَ إِلَّا أَنْتَ", "", "", "أبو داود 4/ 324 وأحمد 5/ 42 وحسّنه الألبانيّ في صحيح أبي داود 3/ 959", 1),
        HisnDua("155", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ — الذكر 155", "دُعَاءُ الكَرْبِ — الذكر 155", "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ", "", "", "التّرمذي 5/ 529، والحاكم، وصحّحه ووافقه الذّهبيّ 1/ 505، وانظر صحيح التّرمذي 3/ 168", 1),
        HisnDua("156", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ", "دُعَاءُ الكَرْبِ — الذكر 156", "دُعَاءُ الكَرْبِ — الذكر 156", "اللَّهُ اللَّهُ رَبِّي لَا أُشْرِكُ بِهِ شَيْئًا", "", "", "أخرجه أبو داود 2/ 87 وانظر صحيح ابن ماجة 2/ 335", 1),
        HisnDua("157", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ — الذكر 157", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ — الذكر 157", "اللَّهُمَّ إِنَّا نَجْعَلُكَ فِي نُحُورِهِمُ، وَنَعُوذُ بِكَ مِنْ شُرُورِهِمُ", "", "", "أبو داود 2/ 89، وصحّحه الحاكم ووافقه الذّهبيّ 2/ 142", 1),
        HisnDua("158", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ — الذكر 158", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ — الذكر 158", "اللَّهُمَّ أَنْتَ عَضُدِي، وَأَنْتَ نَصِيرِي، بِكَ أَجُولُ، وَبِكَ أَصُولُ، وَبِكَ أُقَاتِلُ", "", "", "أبو داود 3/ 42 والتّرمذي 5/ 572، وانظر صحيح التّرمذي 3/ 183", 1),
        HisnDua("159", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ — الذكر 159", "دُعَاءُ لِقَاءِ العَدُّوِّ وَذِي السُّلْطَانِ — الذكر 159", "حَسْبُنَا اللَّهُ وَنِعْمَ الوَكِيلُ", "", "", "البخاري 5/ 172", 1),
        HisnDua("160", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ — الذكر 160", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ — الذكر 160", "اللَّهُمَّ رَبَّ السَّمَاوَاتِ السَّبْعِ، وَرَبَّ العَرْشِ العَظِيمِ، كُنْ لِي جَارًا مِنْ فُلَانٍ بْنِ فُلَانٍ، وَأَحْزَابِهِ مِنْ خَلَائِقِكَ، أَنْ يَفْرُطَ عَلَيَّ أَحَدٌ مِنْهُمْ أَوْ يَطْغَى، عَزَّ جَارُكَ، وَجَلَّ ثَنَاؤُكَ، وَلَا إِلَهَ إِلَّا أَنْتَ", "", "", "البخاري في الأدب المفرد برقم 707 وصحّحه الألبانيّ في صحيح الأدب المفرد برقم 545", 1),
        HisnDua("161", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ — الذكر 161", "دُعَاءُ مَنْ خَافَ ظُلْمَ السُّلْطَانِ — الذكر 161", "اللَّهُ أَكْبَرُ، اللَّهُ أَعَزُّ مِنْ خَلْقِهِ جَمِيعًا، اللَّهُ أَعَزُّ مِمَّا أَخَافُ وَأَحْذَرُ، أَعُوذُ بِاللَّهِ الذِي لَا إِلَهَ إِلَّا هُوَ، المُمْسِكِ السَّمَاوَاتِ السَّبْعِ أَنْ يَقَعْنَ عَلَى الأَرْضِ إِلَّا بِإِذْنِهِ، مِنْ شَرِّ عَبْدِكَ فُلَانٍ، وَجُنُودِهِ وَأَتْبَاعِهِ وَأَشْيَاعِهِ، مِنَّ الجِنِّ وَالإِنْسِ، اللَّهُمَّ كُنْ لِي جَارًا مِنْ شَرِّهِمْ، جَلَّ ثَنَاؤُكَ وَعَزَّ جَارُكَ، وَتَبَارَكَ اِسْمُكَ، وَلَا إِلَهَ غَيْرُكَ", "", "", "البخاري في الأدب المفرد برقم 708 وصحّحه الألباني في صحيح الأدب المفرد برقم 546", 3),
        HisnDua("162", "الدُّعَاءُ عَلَى العَدُوِّ", "الدُّعَاءُ عَلَى العَدُوِّ", "الدُّعَاءُ عَلَى العَدُوِّ — الذكر 162", "الدُّعَاءُ عَلَى العَدُوِّ — الذكر 162", "اللَّهُمَّ مُنْزِلَ الكِتَابِ، سَرِيعَ الحِسَابِ، اِهْزِمْ الأَحْزَابَ، اللَّهُمَّ اِهْزِمْهُمْ وَزَلْزِلْهُمْ", "", "", "مسلم 3/ 1362", 1),
        HisnDua("163", "مَا يَقُولُ مَنْ خَافَ قَوْمًا", "مَا يَقُولُ مَنْ خَافَ قَوْمًا", "مَا يَقُولُ مَنْ خَافَ قَوْمًا — الذكر 163", "مَا يَقُولُ مَنْ خَافَ قَوْمًا — الذكر 163", "اللَّهُمَّ اِكْفِنِيهُمْ بِمَا شِئْتَ", "", "", "مسلم 4/ 2300", 1),
        HisnDua("164", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 164", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 164", "يَسْتَعِيذُ بِاللَّهِ", "", "", "البخاري مع الفتح 6/ 336 ومسلم 1/ 120", 1),
        HisnDua("165", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 165", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 165", "يَنْتَهِي عَمَّا شَكَّ فِيهِ", "", "", "البخاري مع الفتح 6/ 336 ومسلم 1/ 120", 1),
        HisnDua("166", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 166", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 166", "يَقُولُ: آمَنْتُ بِاللَّهِ وَرُسُلِهِ", "", "", "مسلم 1/ 119 - 120", 1),
        HisnDua("167", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 167", "دُعَاءُ مَنْ أَصَابَهُ شَكٌّ فِي الِإيمَانِ — الذكر 167", "يَقْرَأُ قَوْلَهُ تَعَالَى: {هُوَ الْأَوَّلُ وَالْآخِرُ وَالظَّاهِرُ وَالْبَاطِنُ وَهُوَ بِكُلِّ شَيْءٍ عَلِيمٌ}", "", "", "سورة الحديد، الآية: 3. أبو داود 4/ 329 وحسّنه الألبانيّ في صحيح أبي داود 3/ 962", 1),
        HisnDua("168", "دُعَاءُ قَضَاءِ الدَّيْنِ", "دُعَاءُ قَضَاءِ الدَّيْنِ", "دُعَاءُ قَضَاءِ الدَّيْنِ — الذكر 168", "دُعَاءُ قَضَاءِ الدَّيْنِ — الذكر 168", "اللَّهُمَّ اِكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ", "", "", "التّرمذي 5/ 560 وانظر صحيح التّرمذي 3/ 180", 1),
        HisnDua("169", "دُعَاءُ قَضَاءِ الدَّيْنِ", "دُعَاءُ قَضَاءِ الدَّيْنِ", "دُعَاءُ قَضَاءِ الدَّيْنِ — الذكر 169", "دُعَاءُ قَضَاءِ الدَّيْنِ — الذكر 169", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الهَمِّ وَالحَزَنِ، وَالعَجْزِ وَالكَسَلِ، وَالبُخْلِ وَالجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ", "", "", "البخاري 7/ 158", 1),
        HisnDua("170", "دُعَاءُ الوَسْوَسَةِ فِي الصَّلَاةِ وَالقِرَاءَةِ", "دُعَاءُ الوَسْوَسَةِ فِي الصَّلَاةِ وَالقِرَاءَةِ", "دُعَاءُ الوَسْوَسَةِ فِي الصَّلَاةِ وَالقِرَاءَةِ — الذكر 170", "دُعَاءُ الوَسْوَسَةِ فِي الصَّلَاةِ وَالقِرَاءَةِ — الذكر 170", "يَقُولُ: (أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ)، وَيَتْفُلْ عَلَى يَسَارِهِ", "", "", "مسلم 4/ 1729 من حديث عثمان بن أبي العاص رضي اللّه عنه، وفيه: ففعلت ذلك فأذهبه اللّه عنّي", 3),
        HisnDua("171", "دُعاءُ مَنِ اسْتَصْعَبَ عَلَيْهِ أَمْرٌ", "دُعاءُ مَنِ اسْتَصْعَبَ عَلَيْهِ أَمْرٌ", "دُعاءُ مَنِ اسْتَصْعَبَ عَلَيْهِ أَمْرٌ — الذكر 171", "دُعاءُ مَنِ اسْتَصْعَبَ عَلَيْهِ أَمْرٌ — الذكر 171", "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا وَأَنْتَ تَجْعَلُ الحَزْنَ إِذَا شِئْتَ سَهْلًا", "", "", "رواه ابن حبان في صحيحه برقم 2427 (موارد) وابن السّنّي برقم 351 وقال الحافظ: هذا حديث صحيح، وصحّحه عبدالقادر الأرناؤوط في تخريج الأذكار للنّووي ص 106", 1),
        HisnDua("172", "مَا يَقُولُ وَيَفْعَلُ مَنْ أَذْنَبَ ذَنْبًا", "مَا يَقُولُ وَيَفْعَلُ مَنْ أَذْنَبَ ذَنْبًا", "مَا يَقُولُ وَيَفْعَلُ مَنْ أَذْنَبَ ذَنْبًا — الذكر 172", "مَا يَقُولُ وَيَفْعَلُ مَنْ أَذْنَبَ ذَنْبًا — الذكر 172", "مَا مِنْ عَبْدٍ يُذْنِبُ ذَنْبًا فَيُحْسِنُ الطُّهُورَ، ثُمَّ يَقُومُ فَيُصَلِّي رَكْعَتَيْنِ، ثُمَّ يَسْتَغْفِرُ اللَّهَ إِلَّا غَفَرَ اللَّهُ لَهُ", "", "", "أبو داود 2/ 86 والتّرمذي 2/ 257 وصحّحه الألبانيّ في صحيح أبي داود 1/ 283", 1),
        HisnDua("173", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ — الذكر 173", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ — الذكر 173", "الاِسْتِعَاذَةُ بِاللَّهِ مِنْهُ", "", "", "أبو داود 1/ 206، والتّرمذي، وانظر صحيح التّرمذي 1/ 77، وانظر سورة المؤمنون آية 98 - 99", 1),
        HisnDua("174", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ — الذكر 174", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ — الذكر 174", "الأَذَانُ", "", "", "مسلم 1/ 291 والبخاري 1/ 151", 1),
        HisnDua("175", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ — الذكر 175", "دُعَاءُ طَرْدِ الشَّيْطَانِ وَوَسَاوِسِهِ — الذكر 175", "الأَذْكَارُ وَقِرَاءَةُ القُرْآنِ", "", "", "{لاتجعلوا بيوتكم مقابر، إنّ الشّيطان ينفر من البيت الذي تقرأ فيه سورة البقرة} رواه مسلم 1/ 539. ومما يطرد الشّيطان أذكار الصّباح والمساء، والنّوم والاستيقاظ، وغير ذلك من الأذكار المشروعة مثل قراءة آية الكرسيّ عند النّوم، والآيتين الأخيرتين من سورة البقرة، ومن قال: لا إله إلّا اللّه وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير مائة مرة كانت له حِرْزًا من الشّيطان يومه كلّه. وكذا الأذان يطرد الشّيطان", 1),
        HisnDua("176", "الدُّعَاءُ حِينَمَا يَقَعُ مَا لَا يَرْضَاهُ أَوْ غُلِبَ عَلَى أَمْرِهِ", "الدُّعَاءُ حِينَمَا يَقَعُ مَا لَا يَرْضَاهُ أَوْ غُلِبَ عَلَى أَمْرِهِ", "الدُّعَاءُ حِينَمَا يَقَعُ مَا لَا يَرْضَاهُ أَوْ غُلِبَ عَلَى أَمْرِهِ — الذكر 176", "الدُّعَاءُ حِينَمَا يَقَعُ مَا لَا يَرْضَاهُ أَوْ غُلِبَ عَلَى أَمْرِهِ — الذكر 176", "قَدَرُ اللَّهِ. وَمَا شَاءَ فَعَلْ", "", "", "{المؤمن القويّ خير وأحبّ إلى اللّه من المؤمن الضعيف وفي كلّ خير، احرص على ما ينفعك واستعن باللّه ولا تعجز، وإن أصابك شيء فلا تقل لو أنّي فعلت كذا وكذا، ولكن قل: قَدَرُ اللّه. وما شاء فعل، فإنّ لو تفتح عمل الشّيطان} مسلم 4/ 2052", 1),
        HisnDua("177", "تَهْنِئَةُ المَوْلُودِ لَهُ وَجَوَابُهُ", "تَهْنِئَةُ المَوْلُودِ لَهُ وَجَوَابُهُ", "تَهْنِئَةُ المَوْلُودِ لَهُ وَجَوَابُهُ — الذكر 177", "تَهْنِئَةُ المَوْلُودِ لَهُ وَجَوَابُهُ — الذكر 177", "(بَارَكَ اللَّهُ لَكَ فِي المَوْهُوبِ لَكَ، وَشَكَرْتَ الوَاهِبَ، وَبَلَغَ أَشُدَّهُ، وَرُزِقْتَ بِرَّهُ) وَيَرُدُّ عَلَيْهِ المُهَنَّأُ فَيَقُولُ: (بَارَكَ اللَّهُ لَكَ وَبَارَكَ عَلَيْكَ، وَجَزَاكَ اللَّهُ خَيْرًا، وَرَزَقَكَ اللَّهُ مِثْلَهُ، وَأَجْزَلَ ثَوَابَكَ)", "", "", "انظر الأذكار للنّووي ص 349 وصحيح الأذكار للنّووي لسليم الهلالي 2/ 713", 1),
        HisnDua("178", "مَا يُعَوَّذُ بِهِ الأَوْلَادُ", "مَا يُعَوَّذُ بِهِ الأَوْلَادُ", "مَا يُعَوَّذُ بِهِ الأَوْلَادُ — الذكر 178", "مَا يُعَوَّذُ بِهِ الأَوْلَادُ — الذكر 178", "كَانَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يُعَوِّذُ الحَسَنَ وَالحُسَيْنَ: (أُعِيذُكُمَا بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ}", "", "", "البخاري 4/ 119 من حديث ابن عبّاس رضي اللّه عنهما", 1),
        HisnDua("179", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ — الذكر 179", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ — الذكر 179", "لَا بَأْسَ، طَهُورٌ إِنْ شَاءَ اللَّهُ", "", "", "البخاري مع الفتح 10/ 118", 1),
        HisnDua("180", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ — الذكر 180", "الدُّعَاءُ لِلْمَرِيضِ فِي عِيَادَتِهِ — الذكر 180", "أَسْأَلُ اللَّهَ العَظِيمَ رَبَّ العَرْشِ العَظِيمِ أَنْ يَشْفِيَكَ", "", "", "ما من عبد مسلم يعود مريضا لم يحضر أجله فيقول سبع مرّات الحيدث إلّا عوفيَ. أخرجه التّرمذي وأبو داود وانظر صحيح التّرمذي 2/ 210 وصحيح الجامع 5/ 180", 7),
        HisnDua("181", "فَضْلُ عِيَادَةِ المَرِيضِ", "فَضْلُ عِيَادَةِ المَرِيضِ", "فَضْلُ عِيَادَةِ المَرِيضِ — الذكر 181", "فَضْلُ عِيَادَةِ المَرِيضِ — الذكر 181", "قَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (إِذَا عَادَ الرَّجُلُ أَخَاهُ المُسْلِمَ مَشَى فِي خِرَافَةِ الجَنَّةِ حَتَّى يَجْلِسَ فَإِذَا جَلَسَ غَمَرَتْهُ الرَّحْمَةُ، فَإِنْ كَانَ غُدْوَةً صَلَّى عَلَيْهِ سَبْعُونَ أَلْفَ مَلَكٍ حَتَّى يُمْسِي، وَإِنْ كَانَ مَسَاءً صَلَّى عَلَيْهِ سَبْعُونَ أَلْفَ مَلَكٍ حَتَّى يُصْبِحَ)", "", "", "رواه التّرمذي وابن ماجة وأحمد وانظر صحيح ابن ماجة 1/ 244 وصحيح التّرمذي 1/ 286 وصحّحه أيضًا أحمد شاكر", 1),
        HisnDua("182", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ — الذكر 182", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ — الذكر 182", "اللَّهُمَّ اِغْفِرْ لِي وَاِرْحَمْنِي وَأَلْحِقْنِي بِالرَّفِيقِ الأَعْلَى", "", "", "البخاري 7/ 10 ومسلم 4/ 1893", 1),
        HisnDua("183", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ — الذكر 183", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ — الذكر 183", "جَعَلَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ عِنْدَ مَوْتِهِ يُدْخِلُ يَدَيْهِ فِي المَاءِ فَيَمْسَحُ بِهِمَا وَجْهَهُ وَيَقُولُ: لَا إِلَهَ إِلَّا اللَّهُ إِنَّ لِلْمَوْتِ لَسَكَرَاتٌ", "", "", "البخاري مع الفتح 8/ 144 وفي الحديث السّواك", 1),
        HisnDua("184", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ — الذكر 184", "دُعَاءُ المَرِيضِ الذِي يَئِسَ مِنْ حَيَاتِهِ — الذكر 184", "لَا إِلَهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ، لَاإِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَا إِلَهَ إِلَّا اللَّهُ لَهُ المُلْكُ وَلَهُ الحَمْدُ، لَا إِلَهَ إِلَّا اللَّهُ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "", "", "أخرجه التّرمذي وابن ماجة وصحّحه الألبانيّ، انظر صحيح التّرمذي 3/ 152 وصحيح ابن ماجة 2/ 317", 1),
        HisnDua("185", "تَلْقِينُ المُحْتَضِرِ", "تَلْقِينُ المُحْتَضِرِ", "تَلْقِينُ المُحْتَضِرِ — الذكر 185", "تَلْقِينُ المُحْتَضِرِ — الذكر 185", "مَنْ كَانَ آخِرُ كَلَامِهِ (لَا إِلَهَ إِلَّا اللَّهُ) دَخَلَ الجَنَّةَ", "", "", "أبو داود 3/ 190 وانظر صحيح الجامع 5/ 342", 1),
        HisnDua("186", "دُعَاءُ مَنْ أُصِيبَ بِمُصِيبَةٍ", "دُعَاءُ مَنْ أُصِيبَ بِمُصِيبَةٍ", "دُعَاءُ مَنْ أُصِيبَ بِمُصِيبَةٍ — الذكر 186", "دُعَاءُ مَنْ أُصِيبَ بِمُصِيبَةٍ — الذكر 186", "إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ، اللَّهُمَّ أْجُرْنِي فِي مُصِيبَتِي وَأَخْلِفْ لِي خَيْرًا مِنْهَا", "", "", "مسلم 2/ 632", 1),
        HisnDua("187", "الدُّعَاءُ عِنْدَ إِغْمَاضِ المَيِّتِ", "الدُّعَاءُ عِنْدَ إِغْمَاضِ المَيِّتِ", "الدُّعَاءُ عِنْدَ إِغْمَاضِ المَيِّتِ — الذكر 187", "الدُّعَاءُ عِنْدَ إِغْمَاضِ المَيِّتِ — الذكر 187", "اللَّهُمَّ اِغْفِرْ لِفُلَانٍ - بِاِسْمِهِ - وَاِرْفَعْ دَرَجَتَهُ فِي المَهْدِيِّينَ، وَاخْلُفْهُ فِي عَقِبِهِ فِي الغَابِرِينَ، وَاِغْفِرْ لَنَا وَلَهُ يَا رَبَّ العَالَمِينَ، وَاِفْسَحْ لَهُ فِي قَبْرِهِ وَنَوِّرْ لَهُ فِيهِ", "", "", "مسلم 2/ 634", 1),
        HisnDua("188", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 188", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 188", "اللَّهُمَّ اِغْفِرْ لَهُ وَاِرْحَمْهُ، وَعَافِهِ، وَاعْفُ عَنْهُ، وَأَكْرِمْ نُزُلَهُ، وَوَسِّعْ مُدْخَلَهُ، وَاغْسِلْهُ بِالمَاءِ وَالثَّلْجِ وَالبَرَدِ، وَنَقِّهِ مِنْ الخَطَايَا كَمَا نَقَّيْتَ الثَّوْبُ الأَبْيَضُ مِنَ الدَّنَسِ، وَأَبْدِلْهُ دَارًا خَيْرًا مِنْ دَارِهِ، وَأَهْلًا خَيْرًا مِنْ أَهْلِهِ، وَزَوْجًا خَيْرًا مِنْ زَوْجِهِ، وَأَدْخِلْهُ الجَنَّةَ، وَأَعِذْهُ مِنْ عَذَابِ القَبْرِ [وَعَذَابِ النَّارِ]", "", "", "مسلم 2/ 663", 1),
        HisnDua("189", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 189", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 189", "اللَّهُمَّ اِغْفِرْ لِحَيِّنَا، وَمَيِّتِنَا، وَشَاهِدِنَا، وَغَائِبِنَا، وَصَغِيرِنَا وَكَبِيرِنَا، وَذَكَرِنَا وَأُنْثَانَا. اللَّهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الإِسْلَامِ، وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الإِيمَانِ، اللَّهُمَّ لَا تَحْرِمْنَا أَجْرَهُ وَلَا تُضِلَّنَا بَعْدَهُ", "", "", "ابن ماجة 1/ 480 وأحمد 2/ 368 وانظر صحيح ابن ماجة 1/ 251", 1),
        HisnDua("190", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 190", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 190", "اللَّهُمَّ إِنَّ فُلَانَ بْنَ فُلَانٍ فِي ذِمَّتِكَ، وَحَبْلِ جِوَارِكَ، فَقِهِ مِنْ فِتْنَةِ القَبْرِ وَعَذَابِ النَّارِ، وَأَنْتَ أَهْلُ الوَفَاءِ وَالحَقِّ، فَاغْفِرْ لَهُ وَاِرْحَمْهُ إِنَّكَ أَنْتَ الغَفُورُ الرَّحِيمُ", "", "", "أخرجه ابن ماجة، انظر صحيح ابن ماجة 1/ 251 ورواه أبو داود 3/ 211", 1),
        HisnDua("191", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 191", "الدُّعَاءُ لِلْمَيِّتِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 191", "اللَّهُمَّ عَبْدُكَ وَاِبْنُ أَمَتِكَ اِحْتَاجَ إِلَى رَحْمَتِكَ، وَأَنْتَ غَنِيٌّ عَنْ عَذَابِهِ، إِنْ كَانَ مُحْسِنًا فَزِدْ فِي حَسَنَاتِهِ، وَإِنْ كَانَ مُسِيئًا فَتَجَاوَزْ عَنْهُ", "", "", "أخرجه الحاكم وصحّحه ووافقه الذّهبيّ 1/ 359 وانظر أحكام الجنائز للألبانيّ ص 125", 1),
        HisnDua("192", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 192", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 192", "اللَّهُمَّ أَعِذْهُ مِنْ عَذَابِ القَبْرِ", "", "", "قال سعيد بن المسيّب صلّيت وراء أبي هريرة على صبيّ لم يعمل خطيئة قطّ فسمعته يقول: 'الحديث'. أخرجه مالك في الموطّأ 1/ 288 وابن أبي شيبة في المصنّف 3/ 217 والبيهقي 4/ 9، وصحّح إسناده شعيب الأرناؤوط في تحقيقه لشرح السّنّة للبغوي 5/ 357", 1),
        HisnDua("193", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 193", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 193", "وَإِنْ قَالَ: (اللَّهُمَّ اِجْعَلْهُ فَرَطًا وَذُخْرًا لِوَالِدَيْهِ، وَشَفِيعًا مُجَابًا. اللَّهُمَّ ثَقِّلْ بِهِ مَوَازِينَهُمَا وَأَعْظِمْ بِهِ أُجُورَهُمَا وَأَلْحِقْهُ بِصَالِحِ المُؤْمِنِينَ، وَاِجْعَلْهُ فِي كَفَالَةِ إِبْرَاهِيمَ، وَقِهِ بِرَحْمَتِكَ عَذَابَ الجَحِيمِ، وَأَبْدِلْهُ دَارًا خَيْرًا مِنْ دَارِهِ، وَأَهْلًا خَيْرًا مِنْ أَهْلِهِ، اللَّهُمَّ اِغْفِرْ لِأَسْلَافِنَا وَأَفْرَاطِنَا، وَمَنْ سَبَقَنَا بِالإِيمَانِ) فَحَسَنٌ", "", "", "انظر: المغني لابن قدامة 3/ 416 والدّروس المهمّة لعامة الأمة للشيخ عبد العزيز بن عبداللّه بن باز رحمه اللّه ص 15", 1),
        HisnDua("194", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 194", "الدُّعَاءُ لِلْفَرَطِ فِي الصَّلَاةِ عَلَيْهِ — الذكر 194", "اللَّهُمَّ اِجْعَلْهُ لَنَا فَرَطًا، وَسَلَفًا، وَأَجْرًا", "", "", "كان الحَسَن يقرأ على الطّفل بفاتحة الكتاب ويقول: 'الحديث'. أخرجه البغوي في شرح السّنّة 5/ 357، وعبد الرزّاق برقم 6588، وعلّقه البخاري في كتاب الجنائز 65 باب قراءة فاتحة الكتاب على الجنازة 2/ 113", 1),
        HisnDua("195", "دُعَاءُ التَّعْزِيَةِ", "دُعَاءُ التَّعْزِيَةِ", "دُعَاءُ التَّعْزِيَةِ — الذكر 195", "دُعَاءُ التَّعْزِيَةِ — الذكر 195", "إِنَّ لِلَّهِ مَا أَخَذَ، وَلَهُ مَا أَعْطَى، وَكُلُّ شَيْءٍ عِنْدَهُ بِأَجَلٍ مُسَمًّى ... فَلْتَصْبِرْ وَلْتَحْتَسِبْ", "", "", "البخاري 2/ 80 ومسلم 2/ 636", 1),
        HisnDua("196", "دُعَاءُ التَّعْزِيَةِ", "دُعَاءُ التَّعْزِيَةِ", "دُعَاءُ التَّعْزِيَةِ — الذكر 196", "دُعَاءُ التَّعْزِيَةِ — الذكر 196", "وَإِنْ قَالَ: (أَعْظَمَ اللَّهُ أَجْرَكَ، وَأَحْسَنَ عَزَاءَكَ وَغَفَرَ لِمَيِّتِكَ) فَحَسَنٌ", "", "", "الأذكار للنّووي ص 126", 1),
        HisnDua("197", "الدُّعَاءُ عِنْدَ إِدْخَالِ المَيِّتِ القَبْرَ", "الدُّعَاءُ عِنْدَ إِدْخَالِ المَيِّتِ القَبْرَ", "الدُّعَاءُ عِنْدَ إِدْخَالِ المَيِّتِ القَبْرَ — الذكر 197", "الدُّعَاءُ عِنْدَ إِدْخَالِ المَيِّتِ القَبْرَ — الذكر 197", "بِسْمِ اللَّهِ وَعَلَى سُنَّةِ رَسُولِ اللَّهِ", "", "", "أبو داود 3/ 314 بسند صحيح وأحمد بلفظ: بسم اللّه وعلى ملّة رسول اللّه. وسنده صحيح", 1),
        HisnDua("198", "الدُّعَاءُ بَعْدَ دَفْنِ المَيِّتِ", "الدُّعَاءُ بَعْدَ دَفْنِ المَيِّتِ", "الدُّعَاءُ بَعْدَ دَفْنِ المَيِّتِ — الذكر 198", "الدُّعَاءُ بَعْدَ دَفْنِ المَيِّتِ — الذكر 198", "اللَّهُمَّ اِغْفِرْ لَهُ، اللَّهُمَّ ثَبِّتْهُ", "", "", "كان النّبيّ صلّى اللّه عليه وسلّم إذا فرغ من دفن الميّت وقف عليه وقال: (استغفروا لأخيكم وسلوا له التّثبيت، فإنّه الآن يُسأل). أبو داود 3/ 315، والحاكم وصحّحه ووافقه الذّهبيّ 1/ 370", 1),
        HisnDua("199", "دُعَاءُ زِيَارَةِ القُبُورِ", "دُعَاءُ زِيَارَةِ القُبُورِ", "دُعَاءُ زِيَارَةِ القُبُورِ — الذكر 199", "دُعَاءُ زِيَارَةِ القُبُورِ — الذكر 199", "السَّلَامُ عَلَيْكُمْ أَهْلَ الدِّيَارِ، مِنَ المُؤْمِنِينَ وَالمُسْلِمِينَ، وَإِنَّا إِنْ شَاءَ اللَّهُ بِكُمْ لَاحِقُونَ [وَيَرْحَمُ اللَّهُ المُسْتَقْدِمِينَ مِنَّا وَالمُسْتَأْخِرِينَ] أَسْأَلُ اللَّهَ لَنَا وَلَكُمُ العَافِيَةَ", "", "", "مسلم 2/ 671، وابن ماجة، واللفظ له 1/ 494 عن بريدة رضي اللّه عنه، وما بين المعكوفين من حديث عائشة رضي اللّه عنها عند مسلم 2/ 671", 1),
        HisnDua("200", "دُعَاءُ الرِّيحِ", "دُعَاءُ الرِّيحِ", "دُعَاءُ الرِّيحِ — الذكر 200", "دُعَاءُ الرِّيحِ — الذكر 200", "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّهَا", "", "", "أخرجه أبو داود 4/ 326 وابن ماجة 2/ 1228 وانظر صحيح ابن ماجة 2/ 305", 1),
        HisnDua("201", "دُعَاءُ الرِّيحِ", "دُعَاءُ الرِّيحِ", "دُعَاءُ الرِّيحِ — الذكر 201", "دُعَاءُ الرِّيحِ — الذكر 201", "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا، وَخَيْرَ مَا فِيهَا، وَخَيْرَ مَا أُرْسِلَتْ بِهِ، وَأَعُوذُ بِكَ مِنْ شَرِّهَا، وَشَرِّ مَا فِيهَا، وَشَرِّ مَا أُرْسِلَتْ بِهِ", "", "", "مسلم 2/ 616 والبخاري 4/ 76", 1),
        HisnDua("202", "دُعَاءُ الرَّعْدِ", "دُعَاءُ الرَّعْدِ", "دُعَاءُ الرَّعْدِ — الذكر 202", "دُعَاءُ الرَّعْدِ — الذكر 202", "سُبْحَانَ الذِي يُسَبِّحُ الرَّعْدُ بِحَمْدِهِ وَالمَلَائِكَةُ مِنْ خِيفَتِهِ", "", "", "كان عبداللّه بن الزّبير رضي اللّه عنهما إذا سَمِعَ الرّعد ترك الحديث وقال: '...'، الموطّأ 2/ 992 وقال الألبانيّ: صحيح الإسناد موقوفا", 1),
        HisnDua("203", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ — الذكر 203", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ — الذكر 203", "اللَّهُمَّ اَسْقِنَا غَيْثًا مُغِيثًا مَرِيئًا مَرِيعًا، نَافِعًا غَيْرَ ضَارٍّ، عَاجِلًا غَيْرَ آجِلٍ", "", "", "أبو داود 1/ 303 وصحّحه الألباني في صحيح أبي داود 1/ 216", 1),
        HisnDua("204", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ — الذكر 204", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ — الذكر 204", "اللَّهُمَّ أَغِثْنَا، اللَّهُمَّ أَغِثْنَا، اللَّهُمَّ أَغِثْنَا", "", "", "البخاري 1/ 224 ومسلم 2/ 613", 1),
        HisnDua("205", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ — الذكر 205", "مِنْ أَدْعِيَةِ الاسْتِسْقَاءِ — الذكر 205", "اللَّهُمَّ اسْقِ عِبَادَكَ، وَبَهَائِمَكَ، وَانْشُرْ رَحْمَتَكَ، وَأَحْيِي بَلَدَكَ المَيِّتَ", "", "", "أبو داود 1/ 305 وحسّنه الألباني في صحيح أبي داود 1/ 218", 1),
        HisnDua("206", "الدُّعَاءُ إِذَا نَزَلَ المَطَرُ", "الدُّعَاءُ إِذَا نَزَلَ المَطَرُ", "الدُّعَاءُ إِذَا نَزَلَ المَطَرُ — الذكر 206", "الدُّعَاءُ إِذَا نَزَلَ المَطَرُ — الذكر 206", "اللَّهُمَّ صَيِّبًا نَافِعًا", "", "", "البخاري مع الفتح 2/ 518", 1),
        HisnDua("207", "الذِّكْرُ بَعْدَ نُزُولِ المَطَرِ", "الذِّكْرُ بَعْدَ نُزُولِ المَطَرِ", "الذِّكْرُ بَعْدَ نُزُولِ المَطَرِ — الذكر 207", "الذِّكْرُ بَعْدَ نُزُولِ المَطَرِ — الذكر 207", "مُطِرْنَا بِفَضْلِ اللَّهِ وَرَحْمَتِهِ", "", "", "البخاري 1/ 205 ومسلم 1/ 83", 1),
        HisnDua("208", "مِنْ أَدْعِيَةِ الاسْتِصْحَاءِ", "مِنْ أَدْعِيَةِ الاسْتِصْحَاءِ", "مِنْ أَدْعِيَةِ الاسْتِصْحَاءِ — الذكر 208", "مِنْ أَدْعِيَةِ الاسْتِصْحَاءِ — الذكر 208", "اللَّهُمَّ حَوَالَيْنَا وَلَا عَلَيْنَا، اللَّهُمَّ عَلَى الآكَامِ وَالظِّرَابِ، وَبُطُونِ الأَوْدِيَةِ، وَمَنَابِتِ الشَّجَرِ", "", "", "البخاري 1/ 224 ومسلم 2/ 614", 1),
        HisnDua("209", "دُعَاءُ رُؤْيَةِ الهِلَالِ", "دُعَاءُ رُؤْيَةِ الهِلَالِ", "دُعَاءُ رُؤْيَةِ الهِلَالِ — الذكر 209", "دُعَاءُ رُؤْيَةِ الهِلَالِ — الذكر 209", "اللَّهُ أَكْبَرُ. اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالأَمْنِ وَالإِيمَانِ، وَالسَّلَامَةِ وَالإِسْلَامِ، وَالتَّوْفِيقِ لِمَا تُحِبُّ رَبَّنَا وَتَرْضَى. رَبُّنَا وَرَبُّكَ اللَّهُ", "", "", "التّرمذي 5/ 504 والدّارمي بلفظه 1/ 336 وانظر صحيح التّرمذي 3/ 157", 1),
        HisnDua("210", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ — الذكر 210", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ — الذكر 210", "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ العُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ", "", "", "أخرجه أبو داود 2/ 306 وغيره، وانظر صحيح الجامع 4/ 209", 1),
        HisnDua("211", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ — الذكر 211", "الدُّعَاءُ عِنْدَ إِفْطَارِ الصَّائِمِ — الذكر 211", "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ التِي وَسِعَتْ كُلَّ شَيْءٍ أَنْ تَغْفِرَ لِي", "", "", "أخرجه ابن ماجة 1/ 557 من دعاء عبد اللّه بن عمرو رضي اللّه عنهما، وحسّنه الحافظ في تخريج الأذكار انظر شرح الأذكار 4/ 342", 1),
        HisnDua("212", "الدُّعَاءُ قَبْلَ الطَّعَامِ", "الدُّعَاءُ قَبْلَ الطَّعَامِ", "الدُّعَاءُ قَبْلَ الطَّعَامِ — الذكر 212", "الدُّعَاءُ قَبْلَ الطَّعَامِ — الذكر 212", "إِذَا أَكَلَ أَحَدُكُمْ طَعَامًا فَلْيَقُلْ: (بِسْمِ اللَّهِ)، فَإِنْ نَسِيَ فِي أَوَّلِهِ فَلْيَقُلْ: (بِسْمِ اللَّهِ فِي أَوَّلِهِ وَآخِرِهِ)", "", "", "أخرجه أبو داود 3/ 347 والتّرمذي 4/ 288 وانظر صحيح التّرمذي 2/ 167", 1),
        HisnDua("213", "الدُّعَاءُ قَبْلَ الطَّعَامِ", "الدُّعَاءُ قَبْلَ الطَّعَامِ", "الدُّعَاءُ قَبْلَ الطَّعَامِ — الذكر 213", "الدُّعَاءُ قَبْلَ الطَّعَامِ — الذكر 213", "مَنْ أَطْعَمَهُ اللَّهُ الطَّعَامَ فَلْيَقُلْ: (اللَّهُمَّ بَارِكْ لَنَا فِيهِ وَأَطْعِمْنَا خَيْرًا مِنْهُ)، وَمَنْ سَقَاهُ اللَّهُ لَبَنًا فَلْيَقُلْ: (اللَّهُمَّ بَارِكْ لَنَا فِيهِ وَزِدْنَا مِنْهُ)", "", "", "التّرمذي 5/ 506 وانظر صحيح التّرمذي 3/ 158", 1),
        HisnDua("214", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ — الذكر 214", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ — الذكر 214", "الحَمْدُ لِلَّهِ الذِي أَطْعَمَنِي هَذَا، وَرَزَقَنِيهِ، مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ", "", "", "أخرجه أصحاب السّنن إلّا النّسائي، وانظر صحيح التّرمذي 3/ 159", 1),
        HisnDua("215", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ — الذكر 215", "الدُّعَاءُ عِنْدَ الفَرَاغِ مِنَ الطَّعَامِ — الذكر 215", "الحَمْدُ لِلَّهِ حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ، غَيْرَ [مَكْفِيٍّ وَلَا] مُوَدَّعٍ، وَلَا مُسْتَغْنًى عَنْهُ رَبَّنَا", "", "", "البخاري 6/ 214 والتّرمذي بلفظه 5/ 507", 1),
        HisnDua("216", "دُعَاءُ الضَّيْفِ لِصَاحِبِ الطَّعَامِ", "دُعَاءُ الضَّيْفِ لِصَاحِبِ الطَّعَامِ", "دُعَاءُ الضَّيْفِ لِصَاحِبِ الطَّعَامِ — الذكر 216", "دُعَاءُ الضَّيْفِ لِصَاحِبِ الطَّعَامِ — الذكر 216", "اللَّهُمَّ بَارِكْ لَهُمْ فِيمَا رَزَقْتَهُمْ، وَاغْفِرْ لَهُمْ وَارْحَمْهُمْ", "", "", "مسلم 3/ 1615", 1),
        HisnDua("217", "الدُّعَاءُ لِمَنْ سَقَاهُ أَوْ إِذَا أَرَادَ ذَلِكَ", "الدُّعَاءُ لِمَنْ سَقَاهُ أَوْ إِذَا أَرَادَ ذَلِكَ", "الدُّعَاءُ لِمَنْ سَقَاهُ أَوْ إِذَا أَرَادَ ذَلِكَ — الذكر 217", "الدُّعَاءُ لِمَنْ سَقَاهُ أَوْ إِذَا أَرَادَ ذَلِكَ — الذكر 217", "اللَّهُمَّ أَطْعِمْ مَنْ أَطْعَمَنِي وَاسْقِ مَنْ سَقَانِي", "", "", "مسلم 3/ 126", 1),
        HisnDua("218", "الدُّعَاءُ إِذَا أَفْطَرَ عِنْدَ أَهْلِ بَيْتٍ", "الدُّعَاءُ إِذَا أَفْطَرَ عِنْدَ أَهْلِ بَيْتٍ", "الدُّعَاءُ إِذَا أَفْطَرَ عِنْدَ أَهْلِ بَيْتٍ — الذكر 218", "الدُّعَاءُ إِذَا أَفْطَرَ عِنْدَ أَهْلِ بَيْتٍ — الذكر 218", "أَفْطَرَ عِنْدَكُمُ الصَّائِمُونَ، وَأَكَلَ طَعَامَكُمُ الأَبْرَارُ، وَصَلَّتْ عَلَيْكُمُ المَلَائِكَةُ", "", "", "سنن أبي داود 3/ 367، وابن ماجة 1/ 556، والنّسائي في عمل اليوم واللّيلة برقم: 296 - 298، ونصّ على أنّه صلّى اللّه عليه وسلّم يقوله إذا أفطر عند أهل بيت، وصحّحه الألباني في صحيح أبي داود 2/ 730", 1),
        HisnDua("219", "دُعَاءُ الصَّائِمِ إِذَا حَضَرَ الطَّعَامُ وَلَمْ يُفْطِرْ", "دُعَاءُ الصَّائِمِ إِذَا حَضَرَ الطَّعَامُ وَلَمْ يُفْطِرْ", "دُعَاءُ الصَّائِمِ إِذَا حَضَرَ الطَّعَامُ وَلَمْ يُفْطِرْ — الذكر 219", "دُعَاءُ الصَّائِمِ إِذَا حَضَرَ الطَّعَامُ وَلَمْ يُفْطِرْ — الذكر 219", "(إِذَا دُعِيَ أَحَدُكُمْ فَلْيُجِبْ، فَإِنْ كَانَ صَائِمًا فَلْيُصَلِّ وَإِنْ كَانَ مُفْطِرًا فَلْيَطْعَمْ).\r\nوَمَعْنَى فَلْيُصَلِّ أَيْ: فَلْيَدْعُ", "", "", "مسلم 2/ 1054", 1),
        HisnDua("220", "مَا يَقُولُ الصَّائِمُ إِذَا سَابَّهُ أَحَدٌ", "مَا يَقُولُ الصَّائِمُ إِذَا سَابَّهُ أَحَدٌ", "مَا يَقُولُ الصَّائِمُ إِذَا سَابَّهُ أَحَدٌ — الذكر 220", "مَا يَقُولُ الصَّائِمُ إِذَا سَابَّهُ أَحَدٌ — الذكر 220", "إِنِّي صَائِمٌ، إِنِّي صَائِمٌ", "", "", "البخاري مع الفتح 4/ 103، ومسلم 2/ 806", 1),
        HisnDua("221", "الدُّعَاءُ عِنْدَ رُؤْيَةِ بَاكُورَةِ الثَّمَرِ", "الدُّعَاءُ عِنْدَ رُؤْيَةِ بَاكُورَةِ الثَّمَرِ", "الدُّعَاءُ عِنْدَ رُؤْيَةِ بَاكُورَةِ الثَّمَرِ — الذكر 221", "الدُّعَاءُ عِنْدَ رُؤْيَةِ بَاكُورَةِ الثَّمَرِ — الذكر 221", "اللَّهُمَّ بَارِكْ لَنَا فِي ثَمَرِنَا، وبَارِكْ لَنَا فِي مَدِينَتِنَا وبَارِكْ لَنَا فِي صَاعِنَا، وبَارِكْ لَنَا فِي مُدِّنَا", "", "", "مسلم 2/ 1000", 1),
        HisnDua("222", "دُعَاءُ العُطَاسِ", "دُعَاءُ العُطَاسِ", "دُعَاءُ العُطَاسِ — الذكر 222", "دُعَاءُ العُطَاسِ — الذكر 222", "إِذَا عَطَسَ أَحَدُكُمْ فَلْيَقُلْ: (الحَمْدُ لِلَّهِ)، وَلْيَقُلْ لَهُ أَخُوهُ أَوْ صَاحِبُهُ: (يَرْحَمُكَ اللَّهُ)، فَإِذَا قَالَ لَهُ: (يَرْحَمُكَ اللَّهُ)، فَلْيَقُلْ: (يَهْدِيكُمُ اللَّهُ وَيُصْلِحُ بَالَكُمُ)", "", "", "البخاري 7/ 125", 1),
        HisnDua("223", "مَا يُقَالُ لِلْكَافِرِ إِذَا عَطَسَ فَحَمِدَ اللَّهَ", "مَا يُقَالُ لِلْكَافِرِ إِذَا عَطَسَ فَحَمِدَ اللَّهَ", "مَا يُقَالُ لِلْكَافِرِ إِذَا عَطَسَ فَحَمِدَ اللَّهَ — الذكر 223", "مَا يُقَالُ لِلْكَافِرِ إِذَا عَطَسَ فَحَمِدَ اللَّهَ — الذكر 223", "يَهْدِيكُمْ اللَّهُ وَيُصْلِحُ بَالَكُمْ", "", "", "التّرمذي 5/ 82 وأحمد 4/ 400 وأبو داود 4/ 308، وانظر صحيح التّرمذي 2/ 354", 1),
        HisnDua("224", "الدُّعَاءُ لِلْمُتَزَوِّجِ", "الدُّعَاءُ لِلْمُتَزَوِّجِ", "الدُّعَاءُ لِلْمُتَزَوِّجِ — الذكر 224", "الدُّعَاءُ لِلْمُتَزَوِّجِ — الذكر 224", "بَارَكَ اللَّهُ لَكَ، وَبَارَكَ عَلَيْكَ، وَجَمَعَ بَيْنَكُمَا فِي خَيْرٍ", "", "", "أخرجه أصحاب السّنن إلّا النّسائي وانظر صحيح التّرمذي 1/ 316", 1),
        HisnDua("225", "دُعَاءُ المُتَزَوِّجِ وَشِرَاءِ الدَّابَّةِ", "دُعَاءُ المُتَزَوِّجِ وَشِرَاءِ الدَّابَّةِ", "دُعَاءُ المُتَزَوِّجِ وَشِرَاءِ الدَّابَّةِ — الذكر 225", "دُعَاءُ المُتَزَوِّجِ وَشِرَاءِ الدَّابَّةِ — الذكر 225", "إِذَا تَزَوَّجَ أَحَدُكُمْ اِمْرَأَةً، أَوْ إِذَا اِشْتَرَى خَادِمًا فَلْيَقُلْ: (اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا وَخَيْرَ مَا جَبَلْتَهَا عَلَيْهِ وَأَعُوذُ بِكَ مِنْ شَرِّهَا وَشَرِّ مَا جَبَلْتَهَا عَلَيْهِ) وَإِذَا اِشْتَرَى بَعِيرًا فَلْيَأْخُذْ بِذِرْوَةِ سَنَامِهِ وَلْيَقُلْ مِثْلَ ذَلِكَ", "", "", "أبو داود 2/ 248 وابن ماجة 1/ 617 وانظر صحيح ابن ماجة 1/ 324", 1),
        HisnDua("226", "الدُّعَاءُ قَبْلَ إِتُيَانِ الزَّوْجَةِ", "الدُّعَاءُ قَبْلَ إِتُيَانِ الزَّوْجَةِ", "الدُّعَاءُ قَبْلَ إِتُيَانِ الزَّوْجَةِ — الذكر 226", "الدُّعَاءُ قَبْلَ إِتُيَانِ الزَّوْجَةِ — الذكر 226", "بِسْمِ اللَّهِ، اللَّهُمَّ جَنِّبْنَا الشَّيْطَانَ وَجَنِّبْ الشَّيْطَانَ مَا رَزَقْتَنَا", "", "", "البخاري 6/ 141 ومسلم 2/ 1028", 1),
        HisnDua("227", "دُعَاءُ الغَضَبِ", "دُعَاءُ الغَضَبِ", "دُعَاءُ الغَضَبِ — الذكر 227", "دُعَاءُ الغَضَبِ — الذكر 227", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ", "", "", "البخاري 7/ 99 ومسلم 4/ 2015", 1),
        HisnDua("228", "دُعَاءُ مَنْ رَأَى مُبْتَلًى", "دُعَاءُ مَنْ رَأَى مُبْتَلًى", "دُعَاءُ مَنْ رَأَى مُبْتَلًى — الذكر 228", "دُعَاءُ مَنْ رَأَى مُبْتَلًى — الذكر 228", "الحَمْدُ لِلَّهِ الذِي عَافَانِي مِمَّا اِبْتَلَاكَ بِهِ وَفَضَّلَنِي عَلَى كَثِيرٍ مِمَّنْ خَلَقَ تَفْضِيلًا", "", "", "التّرمذي 5/ 493 و 494 وانظر صحيح التّرمذي 3/ 153", 1),
        HisnDua("229", "مَا يُقَالُ فِي المَجْلِسِ", "مَا يُقَالُ فِي المَجْلِسِ", "مَا يُقَالُ فِي المَجْلِسِ — الذكر 229", "مَا يُقَالُ فِي المَجْلِسِ — الذكر 229", "عَنِ اِبْنِ عُمَرَ قَالَ: كَانَ يُعَدُّ لِرَسُولِ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ فِي المَجْلِسِ الوَاحِدِ مِائَةُ مَرَّةٍ مِنْ قَبْلِ أَنْ يَقُومَ: (رَبِّ اِغْفِرْ لِي وَتُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الغَفُورُ)", "", "", "التّرمذي وغيره وانظر صحيح التّرمذي 3/ 153 وصحيح ابن ماجة 2/ 321 ولفظه للتّرمذي", 100),
        HisnDua("230", "كَفَّارَةُ المَجْلِسِ", "كَفَّارَةُ المَجْلِسِ", "كَفَّارَةُ المَجْلِسِ — الذكر 230", "كَفَّارَةُ المَجْلِسِ — الذكر 230", "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ", "", "", "أخرجه أصحاب السّنن وانظر صحيح التّرمذي 3/ 153، وقد ثبت أنّ عائشة رضي اللّه عنها قالت: ما جلس رسول اللّه صلّى اللّه عليه وسلّم مجلسا، ولا تلى قرآنا، ولا صلّى صلاة إلّا ختم ذلك بكلمات: '...'، أخرجه النّسائي في عمل اليوم واللّيلة برقم 308، وأحمد 6/ 77 وصحّحه الدّكتور فاروق حمادة في تحقيقه لعمل اليوم واللّيلة للنّسائي ص 273", 1),
        HisnDua("231", "الدُّعَاءُ لِمَنْ قَالَ غَفَرَ اللَّهُ لَكَ", "الدُّعَاءُ لِمَنْ قَالَ غَفَرَ اللَّهُ لَكَ", "الدُّعَاءُ لِمَنْ قَالَ غَفَرَ اللَّهُ لَكَ — الذكر 231", "الدُّعَاءُ لِمَنْ قَالَ غَفَرَ اللَّهُ لَكَ — الذكر 231", "وَلَكَ", "", "", "أحمد 5/ 82 والنّسائي في عمل اليوم واللّيلة ص 218 برقم 421 تحقيق الدّكتور فاروق حمادة", 1),
        HisnDua("232", "الدُّعَاءُ لِمَنْ صَنَعَ إِلَيْكَ مَعْرُوفًا", "الدُّعَاءُ لِمَنْ صَنَعَ إِلَيْكَ مَعْرُوفًا", "الدُّعَاءُ لِمَنْ صَنَعَ إِلَيْكَ مَعْرُوفًا — الذكر 232", "الدُّعَاءُ لِمَنْ صَنَعَ إِلَيْكَ مَعْرُوفًا — الذكر 232", "جَزَاكَ اللَّهُ خَيْرًا", "", "", "أخرجه التّرمذي رقم 2035 وانظر صحيح الجامع 6244 وصحيح التّرمذي 2/ 200", 1),
        HisnDua("233", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ — الذكر 233", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ — الذكر 233", "مَنْ حَفِظَ عَشْرَ آيَاتٍ مِنْ أَوَّلِ سُورَةِ الكَهْفِ عُصِمَ مِنَ الدَّجَّالِ", "", "", "مسلم 1/ 555، وفي رواية من آخر الكهف 1/ 556", 1),
        HisnDua("234", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ — الذكر 234", "مَا يَعْصِمُ اللَّهُ بِهِ مِنَ الدَّجَّالِ — الذكر 234", "وَالاِسْتِعَاذَةُ بِاللَّهِ مِنْ فِتْنَتِهِ عَقِبَ التَّشَهُّدِ الأَخِيرِ مِنْ كُلِّ صَلَاةٍ", "", "", "انظر حديث رقم 55 وحديث رقم 56 من هذا الكتاب", 1),
        HisnDua("235", "الدُّعَاءُ لِمَنْ قَالَ إِنِّي أُحِبُّكَ فِي اللَّهِ", "الدُّعَاءُ لِمَنْ قَالَ إِنِّي أُحِبُّكَ فِي اللَّهِ", "الدُّعَاءُ لِمَنْ قَالَ إِنِّي أُحِبُّكَ فِي اللَّهِ — الذكر 235", "الدُّعَاءُ لِمَنْ قَالَ إِنِّي أُحِبُّكَ فِي اللَّهِ — الذكر 235", "أَحَبَّكَ الذِي أَحْبَبْتَنِي لَهُ", "", "", "أخرجه أبو داود 4/ 333، وحسّنه الألباني في صحيح سنن أبي داود 3/ 965", 1),
        HisnDua("236", "الدُّعَاءُ لِمَنْ عَرَضَ عَلَيْكَ مَالَهُ", "الدُّعَاءُ لِمَنْ عَرَضَ عَلَيْكَ مَالَهُ", "الدُّعَاءُ لِمَنْ عَرَضَ عَلَيْكَ مَالَهُ — الذكر 236", "الدُّعَاءُ لِمَنْ عَرَضَ عَلَيْكَ مَالَهُ — الذكر 236", "بَارَكَ اللَّهُ لَكَ فِي أَهْلِكَ وَمَالِكَ", "", "", "البخاري مع الفتح 4/ 88", 1),
        HisnDua("237", "الدُّعَاءُ لِمَنْ أَقْرَضَ عِنْدَ القَضَاءِ", "الدُّعَاءُ لِمَنْ أَقْرَضَ عِنْدَ القَضَاءِ", "الدُّعَاءُ لِمَنْ أَقْرَضَ عِنْدَ القَضَاءِ — الذكر 237", "الدُّعَاءُ لِمَنْ أَقْرَضَ عِنْدَ القَضَاءِ — الذكر 237", "بَارَكَ اللَّهُ لَكَ فِي أَهْلِكَ وَمَالِكَ، إِنَّمَا جَزَاءُ السَّلَفِ الحَمْدُ وَالأَدَاءُ", "", "", "أخرجه النّسائي في عمل اليوم واللّيلة ص 300 وابن ماجة 2/ 809 وانظر صحيح ابن ماجة 2/ 55", 1),
        HisnDua("238", "دُعَاءُ الخَوْفِ مِنَ الشِّرْكِ", "دُعَاءُ الخَوْفِ مِنَ الشِّرْكِ", "دُعَاءُ الخَوْفِ مِنَ الشِّرْكِ — الذكر 238", "دُعَاءُ الخَوْفِ مِنَ الشِّرْكِ — الذكر 238", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أُشْرِكَ بِكَ وَأَنَا أَعْلَمُ، وَأَسْتَغْفِرُكَ لِمَا لَا أَعْلَمُ", "", "", "أحمد 4/ 403 وغيره وانظر صحيح الجامع 3/ 233 وصحيح التّرغيب والتّرهيب للألباني 1/ 19", 1),
        HisnDua("239", "الدُّعَاءُ لِمَنْ قَالَ بَارَكَ اللَّهُ فِيكَ", "الدُّعَاءُ لِمَنْ قَالَ بَارَكَ اللَّهُ فِيكَ", "الدُّعَاءُ لِمَنْ قَالَ بَارَكَ اللَّهُ فِيكَ — الذكر 239", "الدُّعَاءُ لِمَنْ قَالَ بَارَكَ اللَّهُ فِيكَ — الذكر 239", "وَفِيكَ بَارَكَ اللَّهُ", "", "", "أخرجه ابن السّني ص 138 برقم 278 وانظر الوابل الصّيب لابن القيّم ص 304 تحقيق بشير محمد عيون", 1),
        HisnDua("240", "دُعَاءُ كَرَاهِيَّةِ الطِّيرَةِ", "دُعَاءُ كَرَاهِيَّةِ الطِّيرَةِ", "دُعَاءُ كَرَاهِيَّةِ الطِّيرَةِ — الذكر 240", "دُعَاءُ كَرَاهِيَّةِ الطِّيرَةِ — الذكر 240", "اللَّهُمَّ لَا طَيْرَ إِلَّا طَيْرُكَ، وَلَا خَيْرَ إِلَّا خَيْرُكَ، وَلَا إِلَهَ غَيْرُكَ", "", "", "أحمد 2/ 220، وابن السني برقم 292، وصححه الألباني في الأحاديث الصحيحة 3/ 54، رقم 1065، أما الفأل فكان يعجب النّبيّ صلّى اللّه عليه وسلّم ولهذا سمع من رجل كلمة طيّبة فأعجبته فقال: (أخذنا فألك من فيك) أبو داود وأحمد، وصححه الألباني في الصحيحة 2/ 363 عند أبي الشيخ في أخلاق النّبيّ صلّى اللّه عليه وسلّم ص 270", 1),
        HisnDua("241", "دُعَاءُ الرُّكُوبِ", "دُعَاءُ الرُّكُوبِ", "دُعَاءُ الرُّكُوبِ — الذكر 241", "دُعَاءُ الرُّكُوبِ — الذكر 241", "بِسْمِ اللَّهِ، وَالحَمْدُ لِلَّهِ، {سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ * وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ} الحَمْدُ لِلَّهِ، الحَمْدُ لِلَّهِ، الحَمْدُ لِلَّهِ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، سُبْحَانَكَ اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي فَاِغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ", "", "", "أبو داود 3/ 34 والتّرمذي 5/ 501 وانظر صحيح التّرمذي 3/ 156", 1),
        HisnDua("242", "دُعَاءُ السَّفَرِ", "دُعَاءُ السَّفَرِ", "دُعَاءُ السَّفَرِ — الذكر 242", "دُعَاءُ السَّفَرِ — الذكر 242", "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، {سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ * وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ} اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا البِرَّ وَالتَّقْوَى، وَمِنَ العَمَلِ مَا تَرْضَى، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَذَا وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ، وَالخَلِيفَةُ فِي الأَهْلِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ وَعْثَاءِ السَّفَرِ، وَكَآبَةِ المَنْظَرِ، وَسُوءِ المُنْقَلَبِ فِي المَالِ وَالأَهْلِ. وَإِذَا رَجَعَ قَالَهُنَّ وَزَادَ فِيهِنَّ: (آيِبُونَ، تَائِبُونَ، عَابِدُونَ، لِرَبِّنَا حَامِدُونَ)", "", "", "مسلم 2/ 998", 1),
        HisnDua("243", "دُعَاءُ دُخُولِ القَرْيَةِ أَو البَلْدَةِ", "دُعَاءُ دُخُولِ القَرْيَةِ أَو البَلْدَةِ", "دُعَاءُ دُخُولِ القَرْيَةِ أَو البَلْدَةِ — الذكر 243", "دُعَاءُ دُخُولِ القَرْيَةِ أَو البَلْدَةِ — الذكر 243", "اللَّهُمَّ رَبَّ السَّمَوَاتِ السَّبْعِ وَمَا أَظْلَلْنَ، وَرَبَّ الأَرَضِينِ السَّبْعِ وَمَا أَقْلَلْنَ، وَرَبَّ الشَّيَاطِينِ وَمَا أَضْلَلْنَ، وَرَبَّ الرِّيَاحِ وَمَا ذَرَيْنَ، أَسْأَلُكَ خَيْرَ هَذِهِ القَرْيَةِ وَخَيْرَ أَهْلِهَا، وَخَيْرَ مَا فِيهَا، وَأَعُوذُ بِكَ مِنْ شَرِّهَا، وَشَرِّ أَهْلِهَا، وَشَرِّ مَا فِيهَا", "", "", "الحاكم وصحّحه ووافقه الذّهبي 2/ 100 وابن السّني برقم 524 وحسّنه الحافظ في تخريج الأذكار 5/ 154، قال ابن باز: ورواه النّسائي بإسناد حسن. انظر تحفة الأخيار ص 37", 1),
        HisnDua("244", "دُعَاءُ دُخُولِ السُّوقِ", "دُعَاءُ دُخُولِ السُّوقِ", "دُعَاءُ دُخُولِ السُّوقِ — الذكر 244", "دُعَاءُ دُخُولِ السُّوقِ — الذكر 244", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ، بِيَدِهِ الخَيْرُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "", "", "التّرمذي 5/ 491 والحاكم 1/ 538 وحسّنه الألباني في صحيح ابن ماجة 2/ 21 وفي صحيح التّرمذي 3/ 152", 1),
        HisnDua("245", "الدُّعَاءُ إِذَا تَعِسَ المَرْكُوبُ", "الدُّعَاءُ إِذَا تَعِسَ المَرْكُوبُ", "الدُّعَاءُ إِذَا تَعِسَ المَرْكُوبُ — الذكر 245", "الدُّعَاءُ إِذَا تَعِسَ المَرْكُوبُ — الذكر 245", "بِسْمِ اللَّهِ", "", "", "أبو داود 4/ 296 وصحّحه الألباني في صحيح أبي داود 3/ 941", 1),
        HisnDua("246", "دُعَاءُ المُسَافِرِ لِلْمُقِيمِ", "دُعَاءُ المُسَافِرِ لِلْمُقِيمِ", "دُعَاءُ المُسَافِرِ لِلْمُقِيمِ — الذكر 246", "دُعَاءُ المُسَافِرِ لِلْمُقِيمِ — الذكر 246", "أَسْتَوْدِعُكُمُ اللَّهَ الذِي لَا تَضِيعُ وَدَائِعُهُ", "", "", "أحمد 2/ 403 وابن ماجة 2/ 943 وانظر صحيح ابن ماجة 2/ 133", 1),
        HisnDua("247", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ — الذكر 247", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ — الذكر 247", "أَسْتَوْدِعُ اللَّهَ دِينَكَ، وَأَمَانَتَكَ، وَخَوَاتِيمَ عَمَلِكَ", "", "", "أحمد 2/ 7 والتّرمذي 5/ 499 وانظر صحيح التّرمذي 2/ 155", 1),
        HisnDua("248", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ — الذكر 248", "دُعَاءُ المُقِيمِ لِلْمُسَافِرِ — الذكر 248", "زَوَّدَكَ اللَّهُ التَّقْوَى، وَغَفَرَ ذَنْبَكَ، وَيَسَّرَ لَكَ الخَيْرَ حَيْثُ مَا كُنْتَ", "", "", "التّرمذي وانظر صحيح التّرمذي 3/ 155", 1),
        HisnDua("249", "التَّكْبِيرُ وَالتَّسْبِيحُ فِي سَيْرِ السَّفَرِ", "التَّكْبِيرُ وَالتَّسْبِيحُ فِي سَيْرِ السَّفَرِ", "التَّكْبِيرُ وَالتَّسْبِيحُ فِي سَيْرِ السَّفَرِ — الذكر 249", "التَّكْبِيرُ وَالتَّسْبِيحُ فِي سَيْرِ السَّفَرِ — الذكر 249", "قَالَ جَابِرٌ رَضِيَ اللَّهُ عَنْهُ: (كُنَّا إِذَا صَعَدْنَا كَبَّرْنَا، وَإِذَا نَزَلْنَا سَبَّحْنَا)", "", "", "البخاري مع الفتح 6/ 135", 1),
        HisnDua("250", "دُعَاءُ المُسَافِرِ إِذَا أَسْحَرَ", "دُعَاءُ المُسَافِرِ إِذَا أَسْحَرَ", "دُعَاءُ المُسَافِرِ إِذَا أَسْحَرَ — الذكر 250", "دُعَاءُ المُسَافِرِ إِذَا أَسْحَرَ — الذكر 250", "سَمَّـِعَ سَامِعٌ بِحَمْدِ اللَّهِ، وَحُسْنِ بَلَائِهِ عَلَيْنَا. رَبَّنَا صَاحِبْنَا، وَأَفْضِلْ عَلَيْنَا عَائِذًا بِاللَّهِ مِنَ النَّارِ", "", "", "مسلم 4/ 2086 ومعنى سمِع سامع: أي شهد شاهد على حمدنا للّه تعالى على نعمه وحسن بلائه. ومعنى سمَّع سامع: بلّغ سامع قولي هذا لغيره وقال مثله تنبيها على الذِّكر في السّحر والدعاء. وقوله: ربّنا صاحبنا وأفضل علينا: أي احفظنا وحطنا واكلأنا وأفضل علينا بجزيل نعمك واصرف عنّا كلّ مكروه، شرح النّووي 17/ 39", 1),
        HisnDua("251", "الدُّعَاءُ إِذَا نَزَلَ مَنْزِلًا فِي سَفَرٍ أَوْ غَيْرِهِ", "الدُّعَاءُ إِذَا نَزَلَ مَنْزِلًا فِي سَفَرٍ أَوْ غَيْرِهِ", "الدُّعَاءُ إِذَا نَزَلَ مَنْزِلًا فِي سَفَرٍ أَوْ غَيْرِهِ — الذكر 251", "الدُّعَاءُ إِذَا نَزَلَ مَنْزِلًا فِي سَفَرٍ أَوْ غَيْرِهِ — الذكر 251", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَاتِ مِنْ شَرِّ مَا خَلَقَ", "", "", "مسلم 4/ 2080", 1),
        HisnDua("252", "ذِكْرُ الرُّجُوعِ مِنَ السَّفَرِ", "ذِكْرُ الرُّجُوعِ مِنَ السَّفَرِ", "ذِكْرُ الرُّجُوعِ مِنَ السَّفَرِ — الذكر 252", "ذِكْرُ الرُّجُوعِ مِنَ السَّفَرِ — الذكر 252", "يُكِبِّرُ عَلَى كُلِّ شَرَفٍ ثَلَاثَ تَكْبِيرَاتٍ ثُمَّ يَقُولُ: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ، وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، آيِبُونَ، تَائِبُونَ، عَابِدُونَ، لِرَبِّنَا حَامِدُونَ، صَدَقَ اللَّهُ وَعْدَهُ، وَنَصَرَ عَبْدَهُ، وَهَزَمَ الأَحْزَابَ وَحْدَهُ", "", "", "كان النّبيّ صلّى اللّه عليه وسلّم يقوله إذا قفل من غزو أو حجّ، البخاري 7/ 163 ومسلم 2/ 980", 1),
        HisnDua("253", "مَا يَقُولُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ أَوْ يَكْرَهُهُ", "مَا يَقُولُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ أَوْ يَكْرَهُهُ", "مَا يَقُولُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ أَوْ يَكْرَهُهُ — الذكر 253", "مَا يَقُولُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ أَوْ يَكْرَهُهُ — الذكر 253", "كَانَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ إِذَا أَتَاهُ الأَمْرُ يَسُرُّهُ قَالَ: (الحَمْدُ لِلَّهِ الذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ) وَإِذَا أَتَاهُ الأَمْرُ يَكْرَهُهُ قَالَ: (الحَمْدُ لِلَّهِ عَلَى كُلِّ حَالٍ)", "", "", "أخرجه ابن السّني في عمل اليوم واللّيلة، والحاكم وصحّحه 1/ 499 وصحّحه الألباني رحمه اللّه في صحيح الجامع 4/ 201", 1),
        HisnDua("254", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 254", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 254", "قَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (مَنْ صَلَّى عَلَيَّ صَلَاةً صَلَّى اللَّهُ عَلَيْهِ بِهَا عَشْرًا)", "", "", "أخرجه مسلم 1/ 288", 1),
        HisnDua("255", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 255", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 255", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (لَا تَجْعَلُوا قَبْرِي عِيدًا وَصَلُّوا عَلَيَّ، فَإِنَّ صَلَاتَكُمْ تَبْلُغُنِي حَيْثُ كُنْتُمْ)", "", "", "أبو داود 2/ 218 وأحمد 2/ 367 وصحّحه الألباني في صحيح أبي داود 2/ 383", 1),
        HisnDua("256", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 256", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 256", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (البَخِيلُ مَنْ ذُكِرْتُ عِنْدَهُ فَلَمْ يُصَلِّ عَلَيَّ)", "", "", "التّرمذي 5/ 551 وغيره، وانظر صحيح الجامع 3/ 25 وصحيح التّرمذي 3/ 177", 1),
        HisnDua("257", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 257", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 257", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (إِنَّ لِلَّهِ مَلَائِكَةً سَيَّاحِينَ فِي الأَرْضِ يُبَلِّغُونِي مِنْ أُمَّتِيَ السَّلاَمَ)", "", "", "النّسائي، والحاكم 2/ 421، وصحّحه الألباني في صحيح النّسائي 1/ 274", 1),
        HisnDua("258", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 258", "فَضْلُ الصَّلَاةِ عَلَى النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ — الذكر 258", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (مَا مِنْ أَحَدٍ يُسَلِّمُ عَلَيَّ إِلَّا رَدَّ اللَّهُ عَلَيَّ رُوحِيَ حَتَّى أَرُدَّ عَلَيْهِ السَّلَامَ)", "", "", "أبو داود برقم 2041 وحسّنه الألباني في صحيح أبي داود 1/ 383", 1),
        HisnDua("259", "إِفْشَاءُ السَّلَامِ", "إِفْشَاءُ السَّلَامِ", "إِفْشَاءُ السَّلَامِ — الذكر 259", "إِفْشَاءُ السَّلَامِ — الذكر 259", "قَالَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (لَاتَدْخُلُوا الجَنَّةَ حَتَّى تُؤْمِنُوا، وَلَا تُؤْمِنُوا حَتَّى تَحَابُّوا، أَوَلَا أَدُلُّكُمُ عَلَى شَيْءٍ إِذَا فَعَلْتُمُوهُ تَحَابَبَتُمْ؟ أَفْشُوا السَّلَامَ بَيْنَكُمْ)", "", "", "مسلم 1/ 74 وغيره", 1),
        HisnDua("260", "إِفْشَاءُ السَّلَامِ", "إِفْشَاءُ السَّلَامِ", "إِفْشَاءُ السَّلَامِ — الذكر 260", "إِفْشَاءُ السَّلَامِ — الذكر 260", "ثَلَاثٌ مَنْ جَمَعَهُنَّ فَقَدْ جَمَعَ الإِيمَانَ: (الإِنْصَافُ مِنْ نَفْسِكَ، وَبَذْلُ السَّلَامِ لِلْعَالَمِ، وَالإِنْفَاقُ مِنَ الإِقْتَارِ)", "", "", "البخاري مع الفتح 1/ 82 عن عمّار رضي اللّه عنه موقوفا معلّقا", 1),
        HisnDua("261", "إِفْشَاءُ السَّلَامِ", "إِفْشَاءُ السَّلَامِ", "إِفْشَاءُ السَّلَامِ — الذكر 261", "إِفْشَاءُ السَّلَامِ — الذكر 261", "وَعَنْ عَبْدِاللَّهِ بْنِ عُمَرَ رَضِيَ اللَّهُ عَنْهُمَا: أَنَّ رَجُلًا سَأَلَ النَّبِيَّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: أَيُّ الإِسْلَامِ خَيْرٌ؟ قَالَ: (تُطْعِمُ الطَّعَامَ، وَتَقْرَأُ السَّلَامَ عَلَى مَنْ عَرَفْتَ وَمَنْ لَمْ تَعْرِفْ)", "", "", "البخاري مع الفتح 1/ 55 ومسلم 1/ 65", 1),
        HisnDua("262", "كَيْفَ يَرُدُّ السَّلَامُ عَلَى الكَافِرِ إِذَا سَلَّمَ", "كَيْفَ يَرُدُّ السَّلَامُ عَلَى الكَافِرِ إِذَا سَلَّمَ", "كَيْفَ يَرُدُّ السَّلَامُ عَلَى الكَافِرِ إِذَا سَلَّمَ — الذكر 262", "كَيْفَ يَرُدُّ السَّلَامُ عَلَى الكَافِرِ إِذَا سَلَّمَ — الذكر 262", "إِذَا سَلَّمَ عَلَيْكُمْ أَهْلُ الكِتَابِ فَقُولُوا: (وَعَلَيْكُمْ)", "", "", "البخاري مع الفتح 11/ 42، ومسلم 4/ 1705", 1),
        HisnDua("263", "الدُّعَاءُ عِنْدَ صِيَاحِ الدِّيكِ وَنَهِيقِ الحِمَارِ", "الدُّعَاءُ عِنْدَ صِيَاحِ الدِّيكِ وَنَهِيقِ الحِمَارِ", "الدُّعَاءُ عِنْدَ صِيَاحِ الدِّيكِ وَنَهِيقِ الحِمَارِ — الذكر 263", "الدُّعَاءُ عِنْدَ صِيَاحِ الدِّيكِ وَنَهِيقِ الحِمَارِ — الذكر 263", "إِذَا سَمِعْتُمْ صِيَاحَ الدِّيَكَةِ فَاسْأَلُوا اللَّهَ مِنْ فَضْلِهِ، فَإِنَّهَا رَأَتْ مَلَكًا. وَإِذَا سَمِعْتُمْ نَهِيقَ الحِمَارَ فَتَعَوَّذُوا بِاللَّهِ مِنَ الشَّيْطَانِ، فَإِنَّهُ رَأَى شَيْطَانًا", "", "", "البخاري مع الفتح 6/ 350 ومسلم 4/ 2092", 1),
        HisnDua("264", "الدُّعَاءُ عِنْدَ سَمَاعِ نُبَاحِ الكِلَابِ بِاللَّيْلِ", "الدُّعَاءُ عِنْدَ سَمَاعِ نُبَاحِ الكِلَابِ بِاللَّيْلِ", "الدُّعَاءُ عِنْدَ سَمَاعِ نُبَاحِ الكِلَابِ بِاللَّيْلِ — الذكر 264", "الدُّعَاءُ عِنْدَ سَمَاعِ نُبَاحِ الكِلَابِ بِاللَّيْلِ — الذكر 264", "إِذَا سَمِعْتُمْ نُبَاحَ الكِلَابِ وَنَهِيقَ الحَمِيرِ بِاللَّيْلِ فَتَعَوَّذُوا بِاللَّهِ مِنْهُمْ، فَإِنَّهُنَّ يَرَيْنَ مَا لَا تَرَوْنَ", "", "", "أبو داود 4/ 327 وأحمد 3/ 306 وصحّحه الألباني في صحيح أبي داود 3/ 961", 1),
        HisnDua("265", "الدُّعَاءُ لِمَنْ سَبَبْتَهُ", "الدُّعَاءُ لِمَنْ سَبَبْتَهُ", "الدُّعَاءُ لِمَنْ سَبَبْتَهُ — الذكر 265", "الدُّعَاءُ لِمَنْ سَبَبْتَهُ — الذكر 265", "قَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (اللَّهُمَّ فَأَيُّمَا مُؤْمِنٍ سَبَبْتُهُ فَاجْعَلْ ذَلِكَ لَهُ قُرْبَةً إِلَيْكَ يَوْمَ القِيَامَةِ)", "", "", "البخاري مع الفتح 11/ 171 ومسلم 4/ 2007 ولفظه: فاجعلها له زكاة ورحمة", 1),
        HisnDua("266", "مَا يَقُولُ المُسْلِمُ إِذَا مَدَحَ المُسْلِمَ", "مَا يَقُولُ المُسْلِمُ إِذَا مَدَحَ المُسْلِمَ", "مَا يَقُولُ المُسْلِمُ إِذَا مَدَحَ المُسْلِمَ — الذكر 266", "مَا يَقُولُ المُسْلِمُ إِذَا مَدَحَ المُسْلِمَ — الذكر 266", "قَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (إِذَا كَانَ أَحَدُكُمُ مَادِحًا صَاحِبَهُ لَا مَحَالَةَ، فَلْيَقُلْ: أَحْسَبُ فُلَانًا وَاللَّهُ حَسِيبُهُ، وَلَا أُزَكِّي عَلَى اللَّهِ أَحَدًا، أَحْسِبُهُ - إِنْ كَانَ يَعْلَمُ ذَاكَ - كَذَا وَكَذَا)", "", "", "رواه مسلم 4/ 2296", 1),
        HisnDua("267", "مَا يَقُولُ المُسْلِمُ إِذَا زُكِّيَ", "مَا يَقُولُ المُسْلِمُ إِذَا زُكِّيَ", "مَا يَقُولُ المُسْلِمُ إِذَا زُكِّيَ — الذكر 267", "مَا يَقُولُ المُسْلِمُ إِذَا زُكِّيَ — الذكر 267", "اللَّهُمَّ لَا تُؤَاخِذْنِي بِمَا يَقُولُونَ، وَاِغْفِرْ لِي مَا لَا يَعْلَمُونَ [وَاِجْعَلْنِي خَيْرًا مِمَّا يَظُنُّونَ]", "", "", "البخاري في الأدب المفرد برقم 761، وصحّح إسناده الألباني في صحيح الأدب المفرد برقم 585، وما بين المعكوفين زيادة للبيهقي في شعب الإيمان 4/ 228 من طريق آخر.", 1),
        HisnDua("268", "كَيْفَ يُلَبِّي المُحْرِمُ فِي الحَجِّ أَوْ العُمْرَةِ", "كَيْفَ يُلَبِّي المُحْرِمُ فِي الحَجِّ أَوْ العُمْرَةِ", "كَيْفَ يُلَبِّي المُحْرِمُ فِي الحَجِّ أَوْ العُمْرَةِ — الذكر 268", "كَيْفَ يُلَبِّي المُحْرِمُ فِي الحَجِّ أَوْ العُمْرَةِ — الذكر 268", "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الحَمْدَ، وَالنِّعْمَةَ، لَكَ وَالمُلْكُ، لَا شَرِيكَ لَكَ", "", "", "البخاري مع الفتح 3/ 408، ومسلم 2/ 841", 1),
        HisnDua("269", "التَّكْبِيرُ إِذَا أَتَى الرُّكْنَ الأَسْوَدَ", "التَّكْبِيرُ إِذَا أَتَى الرُّكْنَ الأَسْوَدَ", "التَّكْبِيرُ إِذَا أَتَى الرُّكْنَ الأَسْوَدَ — الذكر 269", "التَّكْبِيرُ إِذَا أَتَى الرُّكْنَ الأَسْوَدَ — الذكر 269", "طَافَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ بِالبَيْتِ عَلَى بَعِيرٍ كُلَّمَا أَتَى الرُّكْنَ أَشَارَ إِلَيْهِ بِشَيْءٍ عِنْدَهُ وَكَبَّرَ", "", "", "البخاري مع الفتح 3/ 476 والمراد بالشيء: المحجن. انظر: البخاري مع الفتح 3/ 472", 1),
        HisnDua("270", "الدُّعَاءُ بَيْنَ الرُّكْنِ اليَمَانِي وَالحَجَرِ الأَسْوَدِ", "الدُّعَاءُ بَيْنَ الرُّكْنِ اليَمَانِي وَالحَجَرِ الأَسْوَدِ", "الدُّعَاءُ بَيْنَ الرُّكْنِ اليَمَانِي وَالحَجَرِ الأَسْوَدِ — الذكر 270", "الدُّعَاءُ بَيْنَ الرُّكْنِ اليَمَانِي وَالحَجَرِ الأَسْوَدِ — الذكر 270", "{رَبَّنَا ءَاتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ}", "", "", "أبو داود 2/ 179، وأحمد 3/ 411 والبغوي في شرح السّنّة 7/ 128، وحسّنه الألباني في صحيح أبي داود 1/ 354، والآية من سورة البقرة: 201", 1),
        HisnDua("271", "دُعَاءُ الوُقُوفِ عَلَى الصَّفَا وَالمَرْوَةِ", "دُعَاءُ الوُقُوفِ عَلَى الصَّفَا وَالمَرْوَةِ", "دُعَاءُ الوُقُوفِ عَلَى الصَّفَا وَالمَرْوَةِ — الذكر 271", "دُعَاءُ الوُقُوفِ عَلَى الصَّفَا وَالمَرْوَةِ — الذكر 271", "لَمَّا دَنَا صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ مِنَ الصَّفَا قَرَأَ {إِنَّ الصَّفَا وَالْمَرْوَةَ مِنْ شَعَآئِرِ اللَّهِ} (أَبْدَأُ بِمَا بَدَأَ اللَّهُ بِهِ) فَبَدَأَ بِالصَّفَا فَرَقِيَ عَلَيْهِ حَتَّى رَأَى البَيْتَ فَاسْتَقْبَلَ القِبْلَةَ فَوَحَّدَ اللَّهَ وَكَبَّرَهُ وَقَالَ: (لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ، أَنْجَزَ وَعْدَهُ، وَنَصَرَ عَبْدَهُ، وَهَزَمَ الأَحْزَابَ وَحْدَهُ)، ثُمَّ دَعَا بَيْنَ ذَلِكَ. قَالَ مِثْلَ هَذَا ثَلَاثَ مَرَّاتٍ (الحَدِيثْ). وَفِيهِ: (فَفَعَلَ عَلَى المَرْوَةِ كَمَا فَعَلَ عَلَى الصَّفَا)", "", "", "مسلم 2/ 888، والآية من سورة البقرة: 158", 1),
        HisnDua("272", "الدُّعَاءُ يَوْمَ عَرَفَةَ", "الدُّعَاءُ يَوْمَ عَرَفَةَ", "الدُّعَاءُ يَوْمَ عَرَفَةَ — الذكر 272", "الدُّعَاءُ يَوْمَ عَرَفَةَ — الذكر 272", "خَيْرُ الدُّعَاءِ دُعَاءُ يَوْمِ عَرَفَةَ، وَخَيْرُ مَا قُلْتُ أَنَا وَالنَّبِيُّونَ مِنْ قَبْلِي: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "", "", "التّرمذي وحسّنه الألباني في صحيح التّرمذي 3/ 184، وفي الأحاديث الصّحيحة 4/ 6", 1),
        HisnDua("273", "الذِّكْرُ عِنْدَ المَشْعَرِ الحَرَامِ", "الذِّكْرُ عِنْدَ المَشْعَرِ الحَرَامِ", "الذِّكْرُ عِنْدَ المَشْعَرِ الحَرَامِ — الذكر 273", "الذِّكْرُ عِنْدَ المَشْعَرِ الحَرَامِ — الذكر 273", "رَكِبَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ القَصْوَاءَ حَتَّى أَتَى المَشْعَرَ الحَرَامَ، فَاسْتَقْبَلَ القِبْلَةَ (فَدَعَاهُ، وَكَبَّرَهُ، وَهَلَّلَهُ، وَوَحَّدَهُ) فَلَمْ يَزَلْ وَاقِفًا حَتَّى أَسْفَرَ جِدًّا، فَدَفَعَ قَبْلَ أَنْ تَطْلُعَ الشَّمْسُ", "", "", "مسلم 2/ 891", 1),
        HisnDua("274", "التَّكْبِيرُ عِنْدَ رَمْيِ الجِمَارِ مَعَ كُلِّ حَصَاةٍ", "التَّكْبِيرُ عِنْدَ رَمْيِ الجِمَارِ مَعَ كُلِّ حَصَاةٍ", "التَّكْبِيرُ عِنْدَ رَمْيِ الجِمَارِ مَعَ كُلِّ حَصَاةٍ — الذكر 274", "التَّكْبِيرُ عِنْدَ رَمْيِ الجِمَارِ مَعَ كُلِّ حَصَاةٍ — الذكر 274", "يُكَبِّرُ كُلَّمَا رَمَى بِحَصَاةٍ عِنْدَ الجِمَارِ الثَّلَاثِ، ثُمَّ يَتَقَدَّمُ، وَيَقِفُ يَدْعُو مُسْتَقْبِلَ القِبْلَةِ، رَافِعًا يَدَيْهِ بَعْدَ الجَمْرَةِ الأُولَى وَالثَّانِيَةِ. أَمَّا جَمْرَةُ العَقَبَةِ فَيَرْمِيهَا وَيُكَبِّرُ عِنْدَ كُلِّ حَصَاةٍ وَيَنْصَرِفُ وَلَا يَقِفُ عِنْدَهَا", "", "", "البخاري مع الفتح 3/ 583 و 3/ 584، وانظر لفظه هناك. والبخاري مع الفتح 3/ 581 ورواه مسلم أيضا.", 1),
        HisnDua("275", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ — الذكر 275", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ — الذكر 275", "سُبْحَانَ اللَّهِ", "", "", "البخاري مع الفتح 1/ 210 و 390 و 414 ومسلم 4/ 1857", 1),
        HisnDua("276", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ — الذكر 276", "دُعَاءُ التَّعَجُّبِ وَالأَمْرِ السَّارِ — الذكر 276", "اللَّهُ أَكْبَرُ", "", "", "البخاري مع الفتح 8/ 441 وانظر صحيح التّرمذي 2/ 103 و 2/ 235 ومسند أحمد 5/ 218", 1),
        HisnDua("277", "مَا يَفْعَلُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ", "مَا يَفْعَلُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ", "مَا يَفْعَلُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ — الذكر 277", "مَا يَفْعَلُ مَنْ أَتَاهُ أَمْرٌ يَسُرُّهُ — الذكر 277", "كَانَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ إِذَا أَتَاهُ أَمْرٌ يَسُرُّهُ أَوْ يُسَرُّ بِهِ خَرَّ سَاجِدًا شُكْرًا لِلَّهِ تَبَارَكَ وَتَعَالَى", "", "", "رواه أهل السّنن إلّا النّسائي. انظر صحيح ابن ماجة 1/ 233 وإرواء الغليل 2/ 226", 1),
        HisnDua("278", "مَا يَقُولُ مَنْ أَحَسَّ وَجَعًا فِي جَسَدِهِ", "مَا يَقُولُ مَنْ أَحَسَّ وَجَعًا فِي جَسَدِهِ", "مَا يَقُولُ مَنْ أَحَسَّ وَجَعًا فِي جَسَدِهِ — الذكر 278", "مَا يَقُولُ مَنْ أَحَسَّ وَجَعًا فِي جَسَدِهِ — الذكر 278", "ضَعْ يَدَكَ عَلَى الذِي تَأَلَّمَ مِنْ جَسَدِكَ وَقُلْ: (بِسْمِ اللَّهِ) ثَلَاثًا. وَقُلْ سَبْعَ مَرَّاتٍ: (أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ)", "", "", "مسلم 4/ 1728", 1),
        HisnDua("279", "دُعَاءُ مَنْ خَشِيَ أَنْ يُصِيبَ شَيْئًا بِعَيْنِهِ", "دُعَاءُ مَنْ خَشِيَ أَنْ يُصِيبَ شَيْئًا بِعَيْنِهِ", "دُعَاءُ مَنْ خَشِيَ أَنْ يُصِيبَ شَيْئًا بِعَيْنِهِ — الذكر 279", "دُعَاءُ مَنْ خَشِيَ أَنْ يُصِيبَ شَيْئًا بِعَيْنِهِ — الذكر 279", "إِذَا رَأَى أَحَدُكُمْ مِنْ أَخِيهِ، أَوْ مِنْ نَفْسِهِ، أَوْ مِنْ مَالِهِ مَا يُعْجِبُهُ [فَلْيَدْعُ لَهُ بِالبَرَكَةِ] فَإِنَّ العَيْنَ حَقٌّ", "", "", "مسند أحمد 4/ 447 وابن ماجة، ومالك، وصحّحه الألباني في صحيح الجامع 1/ 212 وانظر تحقيق زاد المعاد للأرناؤوط 4/ 170", 1),
        HisnDua("280", "مَا يُقَالُ عِنْدَ الفَزَعِ", "مَا يُقَالُ عِنْدَ الفَزَعِ", "مَا يُقَالُ عِنْدَ الفَزَعِ — الذكر 280", "مَا يُقَالُ عِنْدَ الفَزَعِ — الذكر 280", "لَا إِلَهَ إِلَّا اللَّهُ", "", "", "البخاري مع الفتح 6/ 181، ومسلم 4/ 2208", 1),
        HisnDua("281", "مَا يَقُولُ عِنْدَ الذَّبْحِ أَوْ النَّحْرِ", "مَا يَقُولُ عِنْدَ الذَّبْحِ أَوْ النَّحْرِ", "مَا يَقُولُ عِنْدَ الذَّبْحِ أَوْ النَّحْرِ — الذكر 281", "مَا يَقُولُ عِنْدَ الذَّبْحِ أَوْ النَّحْرِ — الذكر 281", "بِسْمِ اللَّهِ وَاللَّهُ أَكْبَرُ، [اللَّهُمَّ مِنْكَ وَلَكَ]، اللَّهُمَّ تَقَبَّلْ مِنِّي", "", "", "مسلم 3/ 1557، والبيهقي 9/ 287/ ومابين المعكوفين للبيهقي 9/ 287، وغيره والجملة الأخيرة سقتها بالمعنى من رواية مسلم", 1),
        HisnDua("282", "مَا يَقُولُ لِرَدِّ كَيْدِ مَرَدَةِ الشَّيَاطِينِ", "مَا يَقُولُ لِرَدِّ كَيْدِ مَرَدَةِ الشَّيَاطِينِ", "مَا يَقُولُ لِرَدِّ كَيْدِ مَرَدَةِ الشَّيَاطِينِ — الذكر 282", "مَا يَقُولُ لِرَدِّ كَيْدِ مَرَدَةِ الشَّيَاطِينِ — الذكر 282", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ التِي لَا يُجَاوِزُهُنَّ بَرٌّ وَلَا فَاجِرٌ مِنْ شَرِّ مَا خَلَقَ، وَبَرَأَ وَذَرَأَ، وَمِنْ شَرِّ مَا يَنْزِلُ مِنَ السَّمَاءِ، وَمِنْ شَرِّ مَا يَعْرُجُ فِيهَا، وَمِنْ شَرِّ مَا ذَرَأَ فِي الأَرْضِ، وَمِنْ شَرِّ مَا يَخْرُجُ مِنْهَا، وَمِنْ شَرِّ فِتَنِ اللَّيْلِ وَالنَّهَارِ، وَمِنْ شَرِّ كُلِّ طَارِقٍ إِلَّا طَارِقًا يَطْرُقُ بِخَيْرً يَا رَحْمَانُ", "", "", "أحمد 3/ 419 بإسناد صحيح وابن السّني برقم 637 وصحّح إسناده الأرناؤوط في تخريجه للطّحاوية ص 133 وانظر مجمع الزوائد 10/ 127", 1),
        HisnDua("283", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 283", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 283", "قَالَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (وَاللَّهِ إِنِّي لَأَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ فِي اليَوْمِ أَكْثَرَ مِنْ سَبْعِينَ مَرَّةٍ)", "", "", "البخاري مع الفتح 11/ 101", 1),
        HisnDua("284", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 284", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 284", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (يَا أَيُّهَا النَّاسُ تُوبُوا إِلَى اللَّهِ فَإِنِّي أَتُوبُ فِي اليَوْمِ إِلَيْهِ مِائَةَ مَرَّةٍ)", "", "", "مسلم 4/ 2076", 1),
        HisnDua("285", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 285", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 285", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (مَنْ قَالَ أَسْتَغْفِرُ اللَّهَ العَظِيمَ الذِي لَا إِلَهَ إِلَّا هُوَ الحَيُّ القَيُّومُ وَأَتُوبُ إِلَيْهِ، غَفَرَ اللَّهُ لَهُ وَإِنْ كَانَ فَرَّ مِنَ الزَّحْفِ)", "", "", "أخرجه أبو داود 2/ 85 والتّرمذي 5/ 569 والحاكم وصحّحه ووافقه الذّهبي 1/ 511 وصحّحه الألباني. انظر صحيح الترمذي 3/ 182 وجامع الأصول لأحاديث الرسول صلّى اللّه عليه وسلّم 4/ 389 - 390 بتحقيق الأرناؤوط", 1),
        HisnDua("286", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 286", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 286", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (أَقْرَبُ مَا يَكُونُ الرَّبُّ مِنَ العَبْدِ فِي جَوْفِ اللَّيْلِ الآخِرِ، فَإِنْ اِسْتَطَعْتَ أَنْ تَكُونَ مِمَّنْ يَذْكُرُ اللَّهَ فِي تِلْكَ السَّاعَةِ فَكُنْ)", "", "", "أخرجه الترمذي والنسائي 1/ 279، والحاكم وانظر صحيح الترمذي 3/ 183 وجامع الأصول بتحقيق الأرناؤوط 4/ 144", 1),
        HisnDua("287", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 287", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 287", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (أَقْرَبُ مَا يَكُونُ العَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ فَأَكْثِرُوا الدُّعَاءَ)", "", "", "مسلم 1/ 350", 1),
        HisnDua("288", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 288", "الاسْتِغْفَارُ وَالتَّوْبَةُ — الذكر 288", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (إِنَّهُ لَيُغَانُ عَلَى قَلْبِي وَإِنِّي لَأَسْتَغْفِرُ اللَّهَ فِي اليَوْمِ مِائَةَ مَرَّةٍ)", "", "", "أخرجه مسلم 4/ 2075 قال ابن الأثير: (ليغان على قلبي) أي ليغطى ويغشى والمراد به: السهو؛ لأنه كان صلّى اللّه عليه وسلّم لا يزال في مزيد من الذكر والقربة ودوام المراقبة فإذا سها عن شيء منها في بعض الأوقات، أو نسي، عدّه ذنبا على نفسه ففزع إلى الاستغفار. انظر جامع الأصول 4/ 386", 1),
        HisnDua("289", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 289", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 289", "قَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (مَنْ قَالَ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ فِي يَوْمٍ مِائَةَ مَرَّةٍ حُطَّتْ خَطَايَاهُ وَلَوْ كَانَتْ مِثْلَ زَبَدِ البَحْرِ)", "", "", "البخاري 7/ 168 ومسلم 4/ 2071، وانظر: فضل من قالها مائة مرّة إذا أصبح وإذا أمسى ص 31 من هذا الكتاب.", 100),
        HisnDua("290", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 290", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 290", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (مَنْ قَالَ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ المُلْكُ، وَلَهُ الحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، عَشْرَ مِرَارٍ، كَانَ كَمَنْ أَعْتَقَ أَرْبَعَةَ أَنْفُسٍ مِنْ وَلَدِ إِسْمَاعِيلَ)", "", "", "البخاري 7/ 67 ومسلم بلفظه 4/ 2071، وانظر فضل من قالها في اليوم مائة مرّة ص 33 من هذا الكتاب", 10),
        HisnDua("291", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 291", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 291", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي المِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَانِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ العَظِيمِ)", "", "", "البخاري 7/ 168 ومسلم 4/ 2072", 1),
        HisnDua("292", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 292", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 292", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (لَأَنْ أَقُولَ سُبْحَانَ اللَّهِ، وَالحَمْدُ لِلَّهِ، وَلَا إِلَهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، أَحَبُّ إِلَيَّ مِمَّا طَلَعَتْ عَلَيْهِ الشَّمْسُ)", "", "", "مسلم 4/ 2072", 1),
        HisnDua("293", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 293", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 293", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (أَيَعْجِزُ أَحَدُكُمْ أَنْ يَكْسِبَ كُلَّ يَوْمٍ أَلْفَ حَسَنَةٍ؟) فَسَأَلَهُ سَائِلٌ مِنْ جُلَسَائِهِ: كَيْفَ يَكْسِبُ أَحَدُنَا أَلْفَ حَسَنَةٍ؟ قَالَ: (يُسَبِّحُ مِائَةَ تَسْبِيحَةٍ، فَيُكْتَبُ لَهُ أَلْفُ حَسَنَةٍ أَوْ يُحَطُّ عَنْهُ أَلْفُ خَطِيئَةٍ)", "", "", "مسلم 4/ 2073", 1),
        HisnDua("294", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 294", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 294", "مَنْ قَالَ: سُبْحَانَ اللَّهِ العَظِيمِ وَبِحَمْدِهِ غُرِسَتْ لَهُ نَخْلَةٌ فِي الجَنَّةِ", "", "", "أخرجه الترمذي 5/ 511 والحاكم 1/ 501 وصحّحه ووافقه الذهبي، وانظر صحيح الجامع 5/ 531 وصحيح الترمذي 3/ 160", 1),
        HisnDua("295", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 295", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 295", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (يَا عَبْدَ اللَّهِ بْنَ قَيْسٍ أَلَا أَدُلُّكَ عَلَى كَنْزٍ مِنْ كُنُوزِ الجَنَّةِ؟) فَقُلْتُ: بَلَى يَا رَسُولَ اللَّهِ، قَالَ: (قُلْ: لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ)", "", "", "البخاري مع الفتح 11/ 213 ومسلم 4/ 2076", 1),
        HisnDua("296", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 296", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 296", "وَقَالَ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: (أَحَبُّ الكَلَامِ إِلَى اللَّهِ أَرْبَعٌ: سُبْحَانَ اللَّهِ، وَالحَمْدُ لِلَّهِ، وَلَا إِلَهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، لَا يَضُرُّكَ بِأَيِّهِنَّ بَدَأْتَ)", "", "", "مسلم 3/ 1685", 1),
        HisnDua("297", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 297", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 297", "جَاءَ أَعْرَابِيٌّ إِلَى رَسُولِ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ فَقَالَ: عَلِّمْنِي كَلَامًا أَقُولُهُ، قَالَ: (قُلْ: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، اللَّهُ أَكْبَرُ كَبِيرًا، وَالحَمْدُ لِلَّهِ كَثِيرًا، سُبْحَانَ اللَّهِ رَبِّ العَالَمِينَ، لَاحَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ العَزِيزِ الحَكِيمِ) قَالَ: فَهَؤُلَاءِ لِرَبِّي فَمَا لِي؟ قَالَ: (قُلْ: اللَّهُمَّ اِغْفِرْ لِي، وَاِرْحَمْنِي، وَاِهْدِنِي، وَارْزُقْنِي)", "", "", "مسلم 4/ 2072 وزَادَ أبو داود: فلمّا ولّى الأعرابي قال النّبي صلّى اللّه عليه وسلّم: (لقد ملأ يديه من الخير) 1/ 220", 1),
        HisnDua("298", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 298", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 298", "كَانَ الرَّجُلُ إِذَا أَسْلَمَ عَلَّمَهُ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ الصَّلَاةَ ثُمَّ أَمَرَهُ أَنْ يَدْعُو بِهَؤُلَاءِ الكَلِمَاتِ: (اللَّهُمَّ اغْفِرْ لِي، وَارْحَمْنِي، وَاهْدِنِي، وَعَافِنِي وَارْزُقْنِي)", "", "", "مسلم 4/ 2073 في رواية مسلم: \"فإنّ هؤلاء تجمع لك دنياك وآخرتك\"", 1),
        HisnDua("299", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 299", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 299", "إِنَّ أَفْضَلَ الدُّعَاءِ: الحَمْدُ لِلَّهِ، وَأَفْضَلَ الذِّكْرِ: لَا إِلَهَ إِلَّا اللَّهُ", "", "", "الترمذي 5/ 462 وابن ماجة 2/ 1249 والحاكم 1/ 503 وصحّحه ووافقه الذهبي وانظر صحيح الجامع 1/ 362", 1),
        HisnDua("300", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 300", "فَضْلُ التَّسْبِيحِ وَالتَّحْمِيدِ، وَالتَّهْلِيلِ، وَالتَّكْبِيرِ — الذكر 300", "البَاقِيَاتُ الصَّالِحَاتُ: سُبْحَانَ اللَّهِ، وَالحَمْدُ لِلَّهِ، وَلَا إِلَهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "", "", "أحمد برقم 513 بترتيب أحمد شاكر وإسناده صحيح وانظر مجمع الزوائد 1/ 297، وعزاه ابن حجر في بلوغ المرام من رواية أبي سعيد إلى النّسائي وقال: صحّحه ابن حبان والحاكم", 1),
        HisnDua("301", "كَيْفَ كَانَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يُسَبِّحُ؟", "كَيْفَ كَانَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يُسَبِّحُ؟", "كَيْفَ كَانَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يُسَبِّحُ؟ — الذكر 301", "كَيْفَ كَانَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يُسَبِّحُ؟ — الذكر 301", "عَنْ عَبْدِ اللَّهِ بْنِ عَمْرٍو رَضِيَ اللَّهُ عَنْهُمَا قَالَ: (رَأَيْتُ النَّبِيَّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يَعْقِدُ التَّسْبِيحَ بِيَمِينِهِ)", "", "", "أخرجه أبو داود بلفظه 2/ 81 والترمذي 5/ 521 وانظر صحيح الجامع 4/ 271 برقم 4865", 1),
        HisnDua("302", "مِنْ أَنْوَاعِ الخَيْرِ وَالآدَابِ الجَامِعَةِ", "مِنْ أَنْوَاعِ الخَيْرِ وَالآدَابِ الجَامِعَةِ", "مِنْ أَنْوَاعِ الخَيْرِ وَالآدَابِ الجَامِعَةِ — الذكر 302", "مِنْ أَنْوَاعِ الخَيْرِ وَالآدَابِ الجَامِعَةِ — الذكر 302", "إِذَا كَانَ جُنْحُ اللَّيْلِ - أَوْ أَمْسَيْتُمْ - فَكُفُّوا صِبْيَانَكُمْ؛ فَإِنَّ الشَّيَاطِينَ تَنْتَشِرُ حِينَئِذٍ، فَإِذَا ذَهَبَ سَاعَةٌ مِنَ اللَّيْلِ فَخَلُّوهُمْ، وَأَغْلِقُوا الأَبْوَابَ وَاذْكُرُوا اِسْمَ اللَّهِ، فَإِنَّ الشَّيْطَانَ لَا يَفْتَحُ بَابًا مُغْلَقًا، وَأَوْكُوا قِرَبَكُمْ وَاذْكُرُوا اِسْمَ اللَّهِ، وَخَمِّرُوا آنِيَتَكُمْ وَاذْكُرُوا اِسْمَ اللَّهِ، وَلَوْ أَنْ تَعْرُضُوا عَلَيْهَا شَيْئًا، وَأَطْفِئُوا مَصَابِيحَكُمْ\r\nوَصَلَّى اللَّهُ وَسَلَّمَ وَبَارَكَ عَلَى نَبِيِّنَا مُحَمَّدٍ وَعَلَى آلِهِ وَأَصْحَابِهِ أَجْمَعِينَ", "", "", "البخاري مع الفتح 10/ 88 ومسلم 3/ 1595", 1)
    )

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
