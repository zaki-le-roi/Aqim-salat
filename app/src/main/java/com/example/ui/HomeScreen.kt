package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.sp
import com.example.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    lang: String,
    onNavigateToFeature: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val times by viewModel.prayerTimes.collectAsState()
    val nextName by viewModel.nextPrayerName.collectAsState()
    val nextTime by viewModel.nextPrayerTime.collectAsState()
    val countdown by viewModel.countdownText.collectAsState()
    val moonName by viewModel.moonPhaseName.collectAsState()
    val hijriDate by viewModel.hijriDateString.collectAsState()
    val isPlaying by viewModel.isAthanPlaying.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val locationConfigured by viewModel.locationConfigured.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var appMenuExpanded by remember { mutableStateOf(false) }
    var appMenuSection by remember { mutableStateOf("SERVICES") }

    // Clock state
    var liveClockTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            liveClockTime = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }



    androidx.compose.runtime.CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = FontFamily.Serif)
    ) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 0.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. DYNAMIC SKY HEADER WITH NEXT PRAYER COUNTDOWN ---
        item {
            val hour = liveClockTime.take(2).toIntOrNull() ?: java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val nextPrayerArabic = when (nextName.lowercase()) {
                "fajr" -> "الفجر"
                "dhuhr" -> "الظهر"
                "asr" -> "العصر"
                "maghrib" -> "المغرب"
                "isha" -> "العشاء"
                else -> "الصلاة القادمة"
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF172B3A), Color(0xFF16483D), Color(0xFF071F1B))))
            ) {
                // Drawn night sky, crescent and mosque silhouette; existing icons remain unchanged.
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val sky = Color(0xFF172B3A)
                    drawCircle(Color(0xFFFFE7A3), radius = w * 0.065f, center = androidx.compose.ui.geometry.Offset(w * 0.80f, h * 0.18f))
                    drawCircle(sky, radius = w * 0.058f, center = androidx.compose.ui.geometry.Offset(w * 0.825f, h * 0.15f))
                    listOf(
                        androidx.compose.ui.geometry.Offset(w * 0.16f, h * 0.13f),
                        androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.22f),
                        androidx.compose.ui.geometry.Offset(w * 0.57f, h * 0.11f),
                        androidx.compose.ui.geometry.Offset(w * 0.91f, h * 0.30f),
                        androidx.compose.ui.geometry.Offset(w * 0.10f, h * 0.34f)
                    ).forEach { drawCircle(Color(0xFFFFE7A3).copy(alpha = 0.8f), radius = 1.8.dp.toPx(), center = it) }
                    val mosque = Color(0xFF041713).copy(alpha = 0.86f)
                    drawRect(mosque, topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.82f), size = androidx.compose.ui.geometry.Size(w, h * 0.18f))
                    drawRect(mosque, topLeft = androidx.compose.ui.geometry.Offset(w * 0.13f, h * 0.52f), size = androidx.compose.ui.geometry.Size(w * 0.045f, h * 0.39f))
                    drawCircle(mosque, radius = w * 0.024f, center = androidx.compose.ui.geometry.Offset(w * 0.153f, h * 0.51f))
                    drawRect(mosque, topLeft = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.47f), size = androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.44f))
                    drawCircle(mosque, radius = w * 0.026f, center = androidx.compose.ui.geometry.Offset(w * 0.845f, h * 0.46f))
                    val dome = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.20f, h * 0.81f)
                        quadraticTo(w * 0.50f, h * 0.40f, w * 0.80f, h * 0.81f)
                        close()
                    }
                    drawPath(dome, mosque)
                    drawRect(mosque, topLeft = androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.80f), size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.20f))
                    drawLine(Color(0xFFD4AF37).copy(alpha = 0.55f), androidx.compose.ui.geometry.Offset(0f, h * 0.96f), androidx.compose.ui.geometry.Offset(w, h * 0.96f), strokeWidth = 1.dp.toPx())
                }
                // Dark overlay preserves text contrast.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF061B19).copy(alpha = 0.14f), Color(0xFF061B19).copy(alpha = 0.12f), Color(0xFF061B19).copy(alpha = 0.38f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { appMenuExpanded = true }
                                    .testTag("home_app_menu_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.GridView, contentDescription = "القائمة الرئيسية", tint = Color(0xFFFFE7A3))
                            }
                            DropdownMenu(
                                expanded = appMenuExpanded,
                                onDismissRequest = { appMenuExpanded = false },
                                modifier = Modifier
                                    .widthIn(min = 248.dp, max = 292.dp)
                                    .heightIn(max = 520.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = "القائمة الرئيسية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = appMenuSection == "SERVICES",
                                        onClick = { appMenuSection = "SERVICES" },
                                        label = { Text("الخدمات", maxLines = 1) },
                                        leadingIcon = { Icon(Icons.Filled.Apps, contentDescription = null) }
                                    )
                                    FilterChip(
                                        selected = appMenuSection == "SETTINGS",
                                        onClick = { appMenuSection = "SETTINGS" },
                                        label = { Text("خصائص التطبيق", maxLines = 1) },
                                        leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null) }
                                    )
                                }
                                HorizontalDivider()
                                val menuItems = if (appMenuSection == "SERVICES") {
                                    listOf(
                                        Triple("QURAN", "القرآن الكريم", Icons.Filled.MenuBook),
                                        Triple("TASBIH", "السبحة الإلكترونية", Icons.Filled.AddCircle),
                                        Triple("HADITH", "كتب الأحاديث", Icons.Filled.LibraryBooks),
                                        Triple("ZAD_ALMUSLIM", "زاد المسلم", Icons.Filled.MenuBook),
                                        Triple("HISN_AL_MUSLIM", "حصن المسلم", Icons.Filled.Shield),
                                        Triple("QIBLA", "القبلة", Icons.Filled.Explore)
                                    )
                                } else {
                                    listOf(
                                        Triple("SETTING_THEME", "سمة التطبيق", Icons.Filled.Palette),
                                        Triple("SETTING_CALC", "حساب مواقيت الصلاة والمذهب", Icons.Filled.AccessTime),
                                        Triple("SETTING_AUDIO", "القارئ والصوت", Icons.Filled.VolumeUp),
                                        Triple("SETTING_BACKUP", "النسخ الاحتياطي والمزامنة", Icons.Filled.Cloud),
                                        Triple("SETTING_PRIVACY", "الخصوصية وأذونات الموقع", Icons.Filled.Security),
                                        Triple("SETTING_ACCESSIBILITY", "حجم الخط والاهتزاز", Icons.Filled.Accessibility)
                                    )
                                }
                                menuItems.forEach { (route, title, icon) ->
                                    DropdownMenuItem(
                                        text = { Text(title, fontWeight = FontWeight.Medium, maxLines = 2) },
                                        leadingIcon = {
                                            Icon(icon, contentDescription = null, tint = Color(0xFF0D5E34))
                                        },
                                        onClick = {
                                            appMenuExpanded = false
                                            onNavigateToFeature(route)
                                        },
                                        modifier = Modifier.testTag("home_menu_item_$route")
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("أقم صلاتك", color = Color(0xFFFFE7A3), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text(locationName, color = Color.White.copy(alpha = 0.86f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable { onNavigateToFeature("SETTINGS") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = "الإعدادات", tint = Color(0xFFFFE7A3))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Text("الصلاة القادمة", color = Color(0xFFFFE7A3), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        nextPrayerArabic,
                        color = Color(0xFFFFD76A),
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (nextTime.isNotBlank()) "موعدها $nextTime" else "جارٍ حساب وقت الصلاة",
                        color = Color.White.copy(alpha = 0.96f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF0D4F3A))
                            .border(2.dp, Color(0xFFB89A52), RoundedCornerShape(22.dp))
                            .padding(horizontal = 26.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الوقت المتبقي", color = Color(0xFFE8D7A8), fontSize = 12.sp)
                        Text(
                            countdown,
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0D4F3A))
                            .border(1.dp, Color(0xFFB89A52), RoundedCornerShape(18.dp))
                            .clickable { onNavigateToFeature("PRAYER_TIMES") }
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "الموقع", tint = Color(0xFFE8D7A8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            if (locationConfigured) locationName else "حدّد موقعك لعرض المواقيت الصحيحة",
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- 2. FLOATING DATE CAPSULE CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-10).dp), // Slight overlap for depth
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = if (lang == "ar") "التقويم" else "Calendar",
                            tint = Color(0xFFB89A52),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onNavigateToFeature("CALENDAR") }
                        )
                    }

                    // Centered Gregorian and Hijri Date
                    Text(
                        text = hijriDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = if (lang == "ar") "مشاركة" else "Share",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        if (lang == "ar") "التاريخ الهجري اليوم: $hijriDate" else "Today's Hijri date: $hijriDate"
                                    )
                                }
                                runCatching {
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, null))
                                }
                            }
                    )
                }
            }
        }

        // --- 3. HORIZONTAL PRAYER TIME SLIDER ---
        item {
            val prayerCards = listOf(
                Triple("fajr", "الفجر", times?.fajr ?: "--:--"),
                Triple("dhuhr", "الظهر", times?.dhuhr ?: "--:--"),
                Triple("asr", "العصر", times?.asr ?: "--:--"),
                Triple("maghrib", "المغرب", times?.maghrib ?: "--:--"),
                Triple("isha", "العشاء", times?.isha ?: "--:--")
            )
            val nextKey = nextName.lowercase()
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "مواقيت الصلوات",
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 8.dp),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    prayerCards.forEach { (key, title, timeText) ->
                        val isNext = key == nextKey
                        Card(
                            modifier = Modifier
                                .width(100.dp)
                                .then(
                                    if (isNext) Modifier.border(2.dp, Color(0xFFB99A55), RoundedCornerShape(18.dp))
                                    else Modifier
                                )
                                .testTag("prayer_time_$key"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNext) Color(0xFFF4EBDD) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                if (isNext) {
                                    Text("القادمة", color = Color(0xFF755B25), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                                Icon(
                                    imageVector = when (key) {
                                        "fajr" -> Icons.Filled.NightsStay
                                        "dhuhr" -> Icons.Filled.WbSunny
                                        "asr" -> Icons.Filled.WbCloudy
                                        "maghrib" -> Icons.Filled.WbTwilight
                                        else -> Icons.Filled.NightsStay
                                    },
                                    contentDescription = title,
                                    tint = if (isNext) Color(0xFF755B25) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(title, color = if (isNext) Color(0xFF4D3C1B) else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(timeText, color = if (isNext) Color(0xFF4D3C1B) else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 4. BENTO SERVICES GRID ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (lang == "ar") "خدماتك اليومية" else "Your daily services",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Premium full-width Quran card.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .clickable { onNavigateToFeature("QURAN") }
                        .testTag("home_service_quran"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF123D35)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF123D35), Color(0xFF1D6250), Color(0xFF2D7660))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(104.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.White.copy(alpha = 0.07f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IslamicServiceIcon(type = "QURAN", modifier = Modifier.size(86.dp))
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(0.72f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = if (lang == "ar") "القرآن الكريم" else "The Holy Quran",
                                color = Color(0xFFD4AF37),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (lang == "ar") "وردك اليومي بين يديك" else "Your daily recitation, always close",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = if (lang == "ar") "افتح المصحف" else "Open Quran",
                                    color = Color(0xFFF0DDA8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFFF0DDA8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // "زاد المسلم" — coordinated emerald, deep green and gold library card.
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 118.dp)
                        .clickable { onNavigateToFeature("ZAD_ALMUSLIM") }
                        .testTag("home_service_zad_al_muslim"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0B2F26), Color(0xFF17634A), Color(0xFF234B3A))))
                    ) {
                        Box(
                            modifier = Modifier.align(Alignment.CenterEnd).size(150.dp)
                                .offset(x = 28.dp, y = 18.dp).clip(CircleShape)
                                .background(Color(0xFFD7B65D).copy(alpha = 0.08f))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 17.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(62.dp).clip(RoundedCornerShape(18.dp))
                                    .background(Brush.verticalGradient(listOf(Color(0xFFF1D995), Color(0xFFB88A2C)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color(0xFF173D30), modifier = Modifier.size(36.dp))
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("زاد المسلم", color = Color(0xFFFFE6A0), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Text("مكتبة كتب إسلامية في تخصصات متعددة", color = Color.White, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = Color(0xFFFFD979), modifier = Modifier.size(15.dp))
                                    Text("نزّل كتبك واقرأها دون إنترنت", color = Color(0xFFFFD979), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color(0xFFFFE6A0), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Row 1: Tasbih and Adhkar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 132.dp)
                            .clickable { onNavigateToFeature("TASBIH") }
                            .testTag("home_service_tasbih"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF115E3F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IslamicServiceIcon(type = "TASBIH", modifier = Modifier.size(38.dp))
                            }
                            Text(
                                text = if (lang == "ar") "السبحة الإلكترونية" else "Digital Tasbih",
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (lang == "ar") "عداد رقمي ذكي" else "Smart Counter",
                                color = Color(0xFFE6C687),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 132.dp)
                            .clickable { onNavigateToFeature("ADHKAR") }
                            .testTag("home_service_adhkar"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF115E3F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Spa, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(22.dp))
                            }
                            Text(
                                text = if (lang == "ar") "الأذكار اليومية" else "Daily Adhkar",
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (lang == "ar") "صباح ومساء ونوم" else "Morning & Evening",
                                color = Color(0xFFE6C687),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Row 2: Qibla, Hisn al-Muslim, Hadith Books
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 132.dp)
                            .clickable { onNavigateToFeature("QIBLA") }
                            .testTag("home_service_qibla"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF115E3F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 6.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IslamicServiceIcon(type = "QIBLA", modifier = Modifier.size(36.dp))
                            }
                            Text(
                                text = if (lang == "ar") "القبلة" else "Qibla",
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (lang == "ar") "بوصلة دقيقة" else "Accurate Compass",
                                color = Color(0xFFE6C687),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 132.dp)
                            .clickable { onNavigateToFeature("HISN_AL_MUSLIM") }
                            .testTag("home_service_hisn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF115E3F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 6.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IslamicServiceIcon(type = "HISN", modifier = Modifier.size(36.dp))
                            }
                            Text(
                                text = if (lang == "ar") "حصن المسلم" else "Hisn Muslim",
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (lang == "ar") "أدعية جامعة" else "Fortress of Dua",
                                color = Color(0xFFE6C687),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 132.dp)
                            .clickable { onNavigateToFeature("HADITH") }
                            .testTag("home_service_hadith"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF115E3F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 6.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IslamicServiceIcon(type = "HADITH", modifier = Modifier.size(36.dp))
                            }
                            Text(
                                text = if (lang == "ar") "كتب الحديث" else "Hadith Books",
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (lang == "ar") "أمهات الكتب" else "Core Texts",
                                color = Color(0xFFE6C687),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

            }
        }





    }
    }
}

// Helper Composable for the circular grid items
@Composable
fun CircularGridItem(
    key: String,
    label: String,
    color: Color,
    lang: String,
    onNavigate: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(58.dp)
            .clickable { onNavigate(key) }
            .testTag("circular_feature_$key")
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Main Colored Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (label) {
                        "المصحف", "Mushaf" -> Icons.Filled.MenuBook
                        "الأذكار", "Adhkar" -> Icons.Filled.SelfImprovement
                        "السبحة", "Tasbih" -> Icons.Filled.FormatListNumbered
                        "القبلة", "Qibla" -> Icons.Filled.Explore
                        "التقويم", "Calendar" -> Icons.Filled.DateRange
                        "حصن المسلم", "Hisn Al Muslim" -> Icons.Filled.Shield
                        "كتب الحديث", "Hadith Books" -> Icons.Filled.MenuBook
                        "طاعاتك", "Obedience" -> Icons.Filled.WorkspacePremium
                        "الدعاء", "Dua" -> Icons.Filled.Favorite
                        "الختمة", "Khatmah" -> Icons.Filled.LibraryBooks
                        "أسماء الله", "Allah Names" -> Icons.Filled.AutoAwesome
                        else -> Icons.Filled.Widgets
                    },
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Small red star badge on specific key features as shown in the screenshot
            if (label in listOf("المصحف", "Mushaf", "الأذكار", "Adhkar", "الأجر بالنشر", "Publish Reward", "الختمة", "Khatmah")) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE51C23))
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
