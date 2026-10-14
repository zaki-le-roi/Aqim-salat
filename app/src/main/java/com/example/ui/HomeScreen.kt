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

        // --- Compact daily prayer tracker with a centered progress ring ---
        item {
            val loggedPrayers by viewModel.loggedPrayers.collectAsState()
            val prayersList = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            val completedCount = prayersList.count { key ->
                val status = loggedPrayers[key] ?: "NOT_YET"
                status == "PRAYED_ON_TIME" || status == "PRAYED_LATE" || status == "CONGREGATION"
            }
            val percentage = completedCount * 20

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = Color(0xFF0D6B4B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "متابعة صلوات اليوم" else "Today's Prayer Tracker",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (lang == "ar") "الإحصائيات التفصيلية ←" else "Detailed statistics →",
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigateToFeature("PRAYER_STATS") }
                                .padding(horizontal = 7.dp, vertical = 8.dp)
                                .testTag("open_prayer_statistics"),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier.size(112.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { percentage / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF0D6B4B),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$percentage%",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (lang == "ar") "$completedCount/5 صلوات" else "$completedCount/5 prayers",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (completedCount) {
                            5 -> if (lang == "ar") "ما شاء الله، أتممت صلوات اليوم" else "All five prayers recorded"
                            0 -> if (lang == "ar") "سجّل صلاتك بلمسة واحدة" else "Record a prayer with one tap"
                            else -> if (lang == "ar") "تقبّل الله طاعتك" else "May your prayers be accepted"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
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
                            val isCompleted = status == "PRAYED_ON_TIME" || status == "PRAYED_LATE" || status == "CONGREGATION"
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.togglePrayerStatus(key, status) }
                                    .testTag("interactive_tracker_$key")
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(7.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) Color(0xFFE4F3E9) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCompleted) Color(0xFF4B9A6A) else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.Add,
                                        contentDescription = if (lang == "ar") "تسجيل $name" else "Record $name",
                                        tint = if (isCompleted) Color(0xFF267348) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isCompleted) {
                                        if (lang == "ar") "تمت" else "Done"
                                    } else {
                                        if (lang == "ar") "تسجيل" else "Log"
                                    },
                                    fontSize = 9.sp,
                                    color = if (isCompleted) Color(0xFF267348) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 6. BENTO SERVICES GRID ---
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
                    Text(
                        text = if (lang == "ar") "كل الخدمات" else "All services",
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToFeature("MORE_MENU") }
                            .testTag("home_all_services")
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
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
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFFE7D6A4),
                                modifier = Modifier.size(58.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(0.72f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = if (lang == "ar") "القرآن الكريم" else "The Holy Quran",
                                color = Color.White,
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

                // Three calm pastel cards replace the old crowded circular icon rows.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(126.dp)
                            .clickable { onNavigateToFeature("ADHKAR") }
                            .testTag("home_service_adhkar"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F2E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFD2E6D5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Spa, contentDescription = null, tint = Color(0xFF356B48), modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text = if (lang == "ar") "الأذكار" else "Adhkar",
                                color = Color(0xFF244C35),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(126.dp)
                            .clickable { onNavigateToFeature("QIBLA") }
                            .testTag("home_service_qibla"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FA)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFD5E4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Explore, contentDescription = null, tint = Color(0xFF355F91), modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text = if (lang == "ar") "اتجاه القبلة" else "Qibla direction",
                                color = Color(0xFF29496F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(126.dp)
                            .clickable { onNavigateToFeature("HISN_AL_MUSLIM") }
                            .testTag("home_service_hisn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5ECDF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFEBDDCA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF86663D), modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text = if (lang == "ar") "حصن المسلم" else "Hisn al-Muslim",
                                color = Color(0xFF6B512F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onNavigateToFeature("MORE_MENU") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_all_services_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (lang == "ar") "استكشف كل الخدمات" else "Explore all services", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
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
