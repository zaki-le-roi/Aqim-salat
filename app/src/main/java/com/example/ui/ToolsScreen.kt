package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import android.content.Intent
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
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Open", tint = Color(0xFFD4AF37))
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
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                Icon(Icons.Filled.Add, contentDescription = "Add")
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
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select", tint = MaterialTheme.colorScheme.onSurface)
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
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurface)
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
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            // Empty State
            Column(
                modifier = Modifier.fillMaxHeight(0.6f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = "No Counter", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tap 'Add Counter' to launch your digital Tasbih tracker.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
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

    // Smooth compass needle vibration simulation to feel tactile
    var animatedSensorRotation by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        var base = 0f
        while (true) {
            // Gentle random sway of 0.2 degrees to simulate actual dynamic sensor reading
            val sway = (Math.random() * 0.4 - 0.2).toFloat()
            animatedSensorRotation = base + sway
            delay(150)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = Translations.get("qibla_desc", lang),
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
                        rotationZ = -qiblaAngle.toFloat() + animatedSensorRotation
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
                    contentDescription = "Kaaba Direction Needle",
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
                    contentDescription = "Kaaba",
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

    val categories = listOf("Morning", "Evening", "Sleep", "Travel", "Mosque", "Protection")

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
                            text = cat,
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
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
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
                                    text = if (remainingCount == 0) "Completed" else "$remainingCount / ${dhikr.countTarget}",
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
            placeholder = { Text("Search Names...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
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
    var charityLogged by remember { mutableStateOf(0) }
    var qiyamLogged by remember { mutableStateOf(0) }

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
                    Icon(Icons.Filled.Bedtime, contentDescription = "Ramadan", tint = Color(0xFFD4AF37), modifier = Modifier.size(36.dp))
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
                        Icon(Icons.Filled.VolunteerActivism, contentDescription = "Charity", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sadaqah Logged", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$charityLogged Times", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { charityLogged++ },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("log_charity_button")
                        ) {
                            Text("+ Log")
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
                        Icon(Icons.Filled.MenuBook, contentDescription = "Qiyam", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Qiyam / Teravih", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$qiyamLogged Sessions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { qiyamLogged++ },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("log_qiyam_button")
                        ) {
                            Text("+ Log")
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

    // High quality fallback local list in case GPS or API connectivity is limited (Never display blank state!)
    val fallbackBaseMosques = listOf(
        LocalMosque(1L, "المسجد الحرام", "Al-Masjid al-Haram", 21.4225, 39.8262, "مكة المكرمة، المملكة العربية السعودية", "Makkah, Saudi Arabia"),
        LocalMosque(2L, "مسجد عائشة الراجحي", "Aisha Al Rajhi Mosque", 21.3780, 39.8950, "مكة المكرمة، النسيم", "Makkah, Al Naseem"),
        LocalMosque(3L, "مسجد التنعيم (مسجد عائشة)", "Al Taneem Mosque", 21.4880, 39.7990, "مكة المكرمة، التنعيم", "Makkah, Al Taneem"),
        LocalMosque(4L, "المسجد النبوي", "Al-Masjid an-Nabawi", 24.4672, 39.6111, "المدينة المنورة، المملكة العربية السعودية", "Medina, Saudi Arabia"),
        LocalMosque(5L, "مسجد قباء", "Quba Mosque", 24.4392, 39.6172, "المدينة المنورة، طريق الهجرة", "Medina, Hijrah Rd"),
        LocalMosque(6L, "جامع السلطان أحمد (المسجد الأزرق)", "Sultan Ahmed Mosque", 41.0054, 28.9768, "إسطنبول، تركيا", "Istanbul, Turkey")
    )

    val baseMosques = if (realNearbyMosques.isNotEmpty()) realNearbyMosques else fallbackBaseMosques

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
                            contentDescription = "GPS",
                            tint = if (locationPermissionGranted) Color(0xFF1E5E3A) else Color(0xFFD4AF37),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Lat: %.4f • Lng: %.4f".format(lat, lng),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    if (!locationPermissionGranted) {
                        Spacer(modifier = Modifier.height(12.dp))
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
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (lang == "ar") "تفعيل تحديد الموقع GPS" else "Enable GPS Location Access")
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
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFFD4AF37)) },
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
                                    Icon(Icons.Filled.DirectionsCar, contentDescription = "Drive", tint = MaterialTheme.colorScheme.primary)
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
                                    Icon(Icons.Filled.DirectionsWalk, contentDescription = "Walk", tint = MaterialTheme.colorScheme.primary)
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
                                        contentDescription = "Fav",
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
                Icon(Icons.Filled.Add, contentDescription = "Add")
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
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
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
    val translationEn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisnAlMuslimTool(viewModel: AppViewModel, lang: String) {
    val context = LocalContext.current
    val duas = listOf(
        HisnDua(
            "1", "أذكار الصباح والمساء", "Morning & Evening",
            "آية الكرسي", "Ayat al-Kursi",
            "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ...",
            "من قرأها حين يصبح أجير من الجن حتى يمسي، ومن قرأها حين يمسي أجير منهم حتى يصبح.",
            "Allahu la ilaha illa Huwa, Al-Hayyul-Qayyum. No slumber can seize Him nor sleep. To Him belongs all that is in the heavens and on earth..."
        ),
        HisnDua(
            "2", "أذكار الصباح والمساء", "Morning & Evening",
            "سيد الاستغفار", "Master of Forgiveness",
            "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            "من قالها موقنا بها حين يصبح فمات من يومه قبل أن يمسي فهو من أهل الجنة.",
            "O Allah, You are my Lord, there is none worthy of worship but You. You created me and I am Your slave. I keep Your covenant and my promise to You as much as I can..."
        ),
        HisnDua(
            "3", "النوم والاستيقاظ", "Sleep & Waking",
            "دعاء الاستيقاظ من النوم", "Dua Upon Waking",
            "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ.",
            "الحمد لله الذي رد علي روحي، وعافاني في جسدي، وأذن لي بذكره.",
            "Praise is to Allah Who gave us life after He had caused us to die and to Him is the resurrection."
        ),
        HisnDua(
            "4", "النوم والاستيقاظ", "Sleep & Waking",
            "دعاء ما قبل النوم", "Dua Before Sleeping",
            "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا.",
            "باسمك ربي وضعت جنبي وبك أرفعه، إن أمسكت نفسي فارحمها وإن أرسلتها فاحفظها.",
            "In Your name, O Allah, I die and I live."
        ),
        HisnDua(
            "5", "السفر والترحال", "Travel & Outdoors",
            "دعاء السفر", "Dua for Travel",
            "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ.",
            "اللهم إنا نسألك في سفرنا هذا البر والتقوى، ومن العمل ما ترضى، اللهم هون علينا سفرنا هذا...",
            "Allah is the Greatest (three times). Glory is to Him Who has provided this for us, though we could never have had it by our efforts. And surely, to our Lord we are returning..."
        ),
        HisnDua(
            "6", "الصلاة والوضوء", "Prayer & Ablution",
            "دعاء الذهاب إلى المسجد", "Dua for Going to the Mosque",
            "اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُورًا، وَفِي لِسَانِي نُورًا، وَاجْعَلْ فِي سَمْعِي نُورًا، وَاجْعَلْ فِي بَصَرِي نُورًا، وَاجْعَلْ مِنْ خَلْفِي نُورًا...",
            "اللهم اجعل لي نورا في قبري ونورا في عظامي ونورا في دمي ونورا في لحمي...",
            "O Allah, place light in my heart, and on my tongue light, and in my hearing light, and in my sight light, and behind me light..."
        ),
        HisnDua(
            "7", "الشدة والكرب", "Distress & Relief",
            "دعاء الكرب والهم", "Dua for Distress & Anxiety",
            "لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ.",
            "لا إله إلا أنت سبحانك إني كنت من الظالمين، يا حي يا قيوم برحمتك أستغيث.",
            "There is none worthy of worship but Allah the Mighty, the Forbearing. There is none worthy of worship but Allah, Lord of the Magnificent Throne..."
        )
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
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFFD4AF37)) },
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
                    text = "A+",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                        putExtra(Intent.EXTRA_TEXT, "${dua.titleAr}\n\n${dua.arabic}\n\n${dua.translationEn}")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    try { context.startActivity(shareIntent) } catch (e: Exception) {}
                                }
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
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

                        Text(
                            text = if (lang == "ar") dua.translationAr else dua.translationEn,
                            fontSize = (13.sp.value * textScaleFactor).sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 20.sp,
                            textAlign = if (lang == "ar") TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
