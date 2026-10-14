package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    // Clock state
    var liveClockTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            liveClockTime = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    val homePrefs = remember { context.getSharedPreferences("home_ui_state", android.content.Context.MODE_PRIVATE) }
    var showWhatsNew by remember { mutableStateOf(!homePrefs.getBoolean("whats_new_dismissed", false)) }

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
            val skyColors = when (hour) {
                in 5..7 -> listOf(Color(0xFF273B67), Color(0xFFDF8C68), Color(0xFFFFD6A0))
                in 8..15 -> listOf(Color(0xFF1976A8), Color(0xFF55B6D8), Color(0xFFB6E4F2))
                in 16..18 -> listOf(Color(0xFF49366F), Color(0xFFCE765E), Color(0xFFFFC078))
                in 19..23, in 0..4 -> listOf(Color(0xFF101A36), Color(0xFF263B63), Color(0xFF46577A))
                else -> listOf(Color(0xFF1976A8), Color(0xFF55B6D8), Color(0xFFB6E4F2))
            }
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
                    .height(320.dp)
                    .background(Brush.verticalGradient(skyColors))
            ) {
                // Subtle atmospheric glow; the background palette follows the local time.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Black.copy(alpha = 0.22f))
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable { onNavigateToFeature("MORE_MENU") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.GridView, contentDescription = "القائمة", tint = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("أقم صلاتك", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
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
                            Icon(Icons.Filled.Settings, contentDescription = "الإعدادات", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Text("الصلاة القادمة", color = Color.White.copy(alpha = 0.88f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        nextPrayerArabic,
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (nextTime.isNotBlank()) "موعدها $nextTime" else "جارٍ حساب وقت الصلاة",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF101827).copy(alpha = 0.30f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(22.dp))
                            .padding(horizontal = 26.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الوقت المتبقي", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
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
                            .background(Color.Black.copy(alpha = 0.20f))
                            .clickable { onNavigateToFeature("PRAYER_TIMES") }
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "الموقع", tint = Color.White, modifier = Modifier.size(16.dp))
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
                            tint = Color(0xFF4A90E2),
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

        // --- NEW INNOVATIVE: INTERACTIVE DAILY PRAYER ESTABLISHMENT TRACKER ---
        item {
            val loggedPrayers by viewModel.loggedPrayers.collectAsState()
            
            // Calculate completed prayers
            val prayersList = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            val completedCount = prayersList.count { key ->
                val status = loggedPrayers[key] ?: "NOT_YET"
                status == "PRAYED_ON_TIME" || status == "PRAYED_LATE"
            }
            val percentage = completedCount * 20
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "سجل إقامة الصلاة اليومية" else "Daily Prayer Tracker",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Small Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$completedCount / 5",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Progress HUD Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Motivational text based on progress
                        Text(
                            text = when (completedCount) {
                                5 -> if (lang == "ar") "ما شاء الله! أتممت صلواتك اليوم كاملة 🌟" else "Masha'Allah! All prayers completed today! 🌟"
                                4 -> if (lang == "ar") "رائع! صلاة واحدة تفصلك عن اليوم الكامل 💪" else "Great! Just one prayer left today! 💪"
                                0 -> if (lang == "ar") "حافظ على صلاتك لتنير يومك وحياتك 🌱" else "Keep your prayers to illuminate your day! 🌱"
                                else -> if (lang == "ar") "أقم صلاتك في وقتها تنل رضا ربك ✨" else "Establish your prayers on time for Allah's pleasure! ✨"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = if (lang == "ar") TextAlign.Right else TextAlign.Left
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Circular Progress Indicator with centered text
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(54.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { percentage / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF0D6B4B),
                                strokeWidth = 4.dp,
                                trackColor = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "$percentage%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D6B4B)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // 5 Prayer Interactive Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prayersMap = listOf(
                            Triple("Fajr", if (lang == "ar") "الفجر" else "Fajr", Color(0xFF1E88E5)),
                            Triple("Dhuhr", if (lang == "ar") "الظهر" else "Dhuhr", Color(0xFFFFB300)),
                            Triple("Asr", if (lang == "ar") "العصر" else "Asr", Color(0xFFF4511E)),
                            Triple("Maghrib", if (lang == "ar") "المغرب" else "Maghrib", Color(0xFF43A047)),
                            Triple("Isha", if (lang == "ar") "العشاء" else "Isha", Color(0xFF5E35B1))
                        )
                        
                        prayersMap.forEach { (key, name, baseColor) ->
                            val status = loggedPrayers[key] ?: "NOT_YET"
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.togglePrayerStatus(key, status) }
                                    .testTag("interactive_tracker_$key")
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6C757D)
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Beautiful circular status button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (status) {
                                                "PRAYED_ON_TIME" -> Color(0xFFE8F5E9)
                                                "PRAYED_LATE" -> Color(0xFFFFF8E1)
                                                "MISSED" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFF1F3F5)
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when (status) {
                                                "PRAYED_ON_TIME" -> Color(0xFF4CAF50)
                                                "PRAYED_LATE" -> Color(0xFFFFB300)
                                                "MISSED" -> Color(0xFFF44336)
                                                else -> Color(0xFFCED4DA)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (status) {
                                            "PRAYED_ON_TIME" -> Icons.Filled.Check
                                            "PRAYED_LATE" -> Icons.Filled.Schedule
                                            "MISSED" -> Icons.Filled.Close
                                            else -> Icons.Filled.Add
                                        },
                                        contentDescription = status,
                                        tint = when (status) {
                                            "PRAYED_ON_TIME" -> Color(0xFF4CAF50)
                                            "PRAYED_LATE" -> Color(0xFFFFB300)
                                            "MISSED" -> Color(0xFFF44336)
                                            else -> Color(0xFFADB5BD)
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Label text under the icon
                                Text(
                                    text = when (status) {
                                        "PRAYED_ON_TIME" -> if (lang == "ar") "في وقتها" else "On Time"
                                        "PRAYED_LATE" -> if (lang == "ar") "قضاء" else "Late"
                                        "MISSED" -> if (lang == "ar") "فاتتني" else "Missed"
                                        else -> if (lang == "ar") "لم تسجل" else "Unlogged"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (status) {
                                        "PRAYED_ON_TIME" -> Color(0xFF2E7D32)
                                        "PRAYED_LATE" -> Color(0xFFF57F17)
                                        "MISSED" -> Color(0xFFC62828)
                                        else -> Color(0xFF6C757D)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Weekly prayer analytics ---
        item {
            val weeklyStats by viewModel.weeklyPrayerSummary.collectAsState()
            val weeklyCompleted = weeklyStats.sumOf { it.completedCount }
            val weeklyOnTime = weeklyStats.sumOf { it.onTimeCount }
            val weeklyLate = weeklyStats.sumOf { it.lateCount }
            val weeklyMissed = weeklyStats.sumOf { it.missedCount }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ملخص الصلوات خلال 7 أيام",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${weeklyCompleted} / 35",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("في وقتها: ${weeklyOnTime}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        Text("قضاء: ${weeklyLate}", fontSize = 11.sp, color = Color(0xFFB77900))
                        Text("فاتت: ${weeklyMissed}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                    if (weeklyStats.isEmpty()) {
                        Text(
                            text = "سيظهر ملخص الأسبوع بعد تسجيل الصلوات.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        weeklyStats.forEach { day ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = day.dayNameAr,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(62.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                LinearProgressIndicator(
                                    progress = { day.completedCount / 5f },
                                    modifier = Modifier.weight(1f).height(7.dp),
                                    color = when {
                                        day.completedCount == 5 -> Color(0xFF2E7D32)
                                        day.completedCount >= 3 -> Color(0xFF689F38)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = "${day.completedCount}/5",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 6. HIGH-FIDELITY 12-ICON CIRCULAR GRID ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val items = listOf(
                    Triple("HISN_AL_MUSLIM", if (lang == "ar") "حصن المسلم" else "Hisn Al-Muslim", Color(0xFF2E7D32)),
                    Triple("CALENDAR", if (lang == "ar") "التقويم" else "Calendar", Color(0xFF6A1B9A)),
                    Triple("QIBLA", if (lang == "ar") "القبلة" else "Qibla", Color(0xFF00897B)),
                    Triple("SEERAH", if (lang == "ar") "السيرة" else "Seerah", Color(0xFF795548)),
                    Triple("ADHKAR", if (lang == "ar") "الأذكار" else "Adhkar", Color(0xFF1565C0)),
                    Triple("QURAN", if (lang == "ar") "المصحف" else "Quran", Color(0xFF2E7D32)),
                    Triple("MORE_MENU", if (lang == "ar") "المزيد" else "More", Color(0xFF455A64)),
                    Triple("NAMES", if (lang == "ar") "أسماء الله" else "Names of Allah", Color(0xFF8E24AA)),
                    Triple("DAILY_DUA", if (lang == "ar") "الأدعية" else "Duas", Color(0xFF00838F)),
                    Triple("FAVORITES", if (lang == "ar") "علامات" else "Bookmarks", Color(0xFFEF6C00)),
                    Triple("ZAKAT", if (lang == "ar") "حاسبة الزكاة" else "Zakat Calculator", Color(0xFF00796B))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Render Row 1
                    items.take(6).forEach { (key, label, color) ->
                        CircularGridItem(
                            key = key,
                            label = label,
                            color = color,
                            lang = lang,
                            onNavigate = onNavigateToFeature
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Render Row 2
                    items.drop(6).forEach { (key, label, color) ->
                        CircularGridItem(
                            key = key,
                            label = label,
                            color = color,
                            lang = lang,
                            onNavigate = onNavigateToFeature
                        )
                    }
                }
            }
        }

        // --- 7. "WHAT'S NEW" INFO CARD (ZAKAT ANNOUNCEMENT) ---
        if (showWhatsNew) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3D2E)), // Midnight Dark Blue
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Top Header Row with Gift Icon & Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left close button
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { showWhatsNew = false; homePrefs.edit().putBoolean("whats_new_dismissed", true).apply() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            // Right gift icon & titles
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (lang == "ar") "ما الجديد" else "What's New",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (lang == "ar") "حاسبة الزكاة" else "Zakat Calculator",
                                        color = Color(0xFFFFD700), // Gold
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2196F3).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CardGiftcard,
                                        contentDescription = if (lang == "ar") "جديد" else "New",
                                        tint = Color(0xFFFFCA28),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Body Description
                        Text(
                            text = if (lang == "ar") {
                                "أطلقنا (حاسبة الزكاة) لمساعدتك على حساب زكاة المال والتجارة بنسبة 2.5% عند تحقق شروطها. راجع نصاب الزكاة وأحكامها الشرعية قبل الاعتماد على النتيجة."
                            } else {
                                "The Zakat Calculator helps estimate monetary and trade-wealth zakat at 2.5% when its conditions are met. Verify the current nisab and applicable rulings before relying on the result."
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp,
                            textAlign = if (lang == "ar") TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Yellow Link Button + Dots Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Link
                            Text(
                                text = if (lang == "ar") "اكتشفها الآن >>" else "Explore now >>",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onNavigateToFeature("ZAKAT") }
                            )

                            // Page dots indicators
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { dotIdx ->
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(if (dotIdx == 1) 12.dp else 5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (dotIdx == 1) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 8. "DEED OF DAY & NIGHT" GRADIENT CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2E7D5B), Color(0xFF0D6B4B))
                            )
                        )
                        .clickable { onNavigateToFeature("DAILY_DUA") }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (lang == "ar") "عمل اليوم والليلة" else "Deed of Day & Night",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (lang == "ar") "نرجو أن نكون خير معين لك في طريقك إلى الله" else "We hope to be your best helper on your path",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.WbTwilight,
                            contentDescription = "تبديل الليل والنهار",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(34.dp)
                        )
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
                        "حاسبة الزكاة", "Zakat Calc" -> Icons.Filled.Percent
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
