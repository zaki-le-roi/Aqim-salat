package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
    val loggedInMember by viewModel.loggedInMember.collectAsState()
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
    var showOnboarding by remember { mutableStateOf(!homePrefs.getBoolean("onboarding_dismissed", false) && loggedInMember == null) }
    var showWhatsNew by remember { mutableStateOf(!homePrefs.getBoolean("whats_new_dismissed", false)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8F6)),
        contentPadding = PaddingValues(top = 0.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. IMMERSIVE TOP SKY SECTION WITH INTEGRATED CLOCK & BANNER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                // Mosque sky background
                Image(
                    painter = painterResource(id = R.drawable.img_home_banner_1783073765742),
                    contentDescription = if (lang == "ar") "خلفية المسجد" else "Mosque background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Beautiful sky overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x334A90E2),
                                    Color.Transparent,
                                    Color(0x22000000)
                                )
                            )
                        )
                )

                // Sky HUD Content Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Custom Header HUD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Grid button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .clickable { onNavigateToFeature("MORE_MENU") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GridView,
                                    contentDescription = if (lang == "ar") "القائمة" else "Menu",
                                    tint = Color(0xFF333333),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Avatar button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (loggedInMember != null) Color(0xFFD4AF37).copy(alpha = 0.15f) else Color.White)
                                    .border(1.5.dp, Color(0xFFD4AF37), CircleShape)
                                    .clickable { onNavigateToFeature("AUTH") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (loggedInMember != null) Icons.Filled.AccountCircle else Icons.Filled.Person,
                                    contentDescription = if (lang == "ar") "الملف الشخصي" else "Profile",
                                    tint = if (loggedInMember != null) Color(0xFF1B5E20) else Color(0xFF666666),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Branded Aqim Salah Premium Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF0B3D2E))
                                .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(20.dp))
                                .clickable { onNavigateToFeature("PREMIUM") }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Crown",
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == "ar") "أقم صلاتك المميز" else "Aqim Salah Premium",
                                    color = Color(0xFFD4AF37),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Notification icon with orange badge
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .clickable { onNavigateToFeature("SETTINGS") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.VolumeUp else Icons.Filled.Notifications,
                                    contentDescription = if (lang == "ar") "الإشعارات" else "Notifications",
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // "Asr since" or active prayer info
                    val currentPrayerName = when (nextName.lowercase()) {
                        "fajr" -> if (lang == "ar") "العشاء" else "Isha"
                        "shuruq", "dhuhr" -> if (lang == "ar") "الفجر" else "Fajr"
                        "asr" -> if (lang == "ar") "الظهر" else "Dhuhr"
                        "maghrib" -> if (lang == "ar") "العصر" else "Asr"
                        else -> if (lang == "ar") "المغرب" else "Maghrib"
                    }
                    Text(
                        text = if (lang == "ar") "$currentPrayerName منذ" else "$currentPrayerName since",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Large Digital Clock
                    Text(
                        text = liveClockTime,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location Pill button with GPS pin
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable { onNavigateToFeature("SETTINGS") }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$locationName +",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = if (lang == "ar") "الموقع" else "Location",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.5f))
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        color = Color(0xFF173C2E),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = if (lang == "ar") "مشاركة" else "Share",
                        tint = Color(0xFF333333),
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

        // --- 3. PREMIUM PRAYER TIMES CARD WITH HIGHLIGHTED ACTIVE CELL ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // 6 Prayer Columns
                    val activePrayer = nextName.lowercase()
                    val columns = listOf(
                        Triple("fajr", if (lang == "ar") "الفجر" else "Fajr", times?.fajr ?: "--:--"),
                        Triple("shuruq", if (lang == "ar") "الشروق" else "Shuruq", times?.sunrise ?: "--:--"),
                        Triple("dhuhr", if (lang == "ar") "الظهر" else "Dhuhr", times?.dhuhr ?: "--:--"),
                        Triple("asr", if (lang == "ar") "العصر" else "Asr", times?.asr ?: "--:--"),
                        Triple("maghrib", if (lang == "ar") "المغرب" else "Maghrib", times?.maghrib ?: "--:--"),
                        Triple("isha", if (lang == "ar") "العشاء" else "Isha", times?.isha ?: "--:--")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        columns.forEach { (key, title, timeStr) ->
                            // Determine if this column is the active/current prayer
                            // In real-world, active is the one currently elapsed.
                            // Let's make "Asr" or the one right before nextName active.
                            val isActive = when (activePrayer) {
                                "fajr" -> key == "isha"
                                "shuruq" -> key == "fajr"
                                "dhuhr" -> key == "shuruq"
                                "asr" -> key == "dhuhr"
                                "maghrib" -> key == "asr"
                                else -> key == "maghrib"
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isActive) Color(0xFFF0EBF8) else Color.Transparent)
                                    .border(
                                        width = if (isActive) 1.dp else 0.dp,
                                        color = if (isActive) Color(0xFF0D6B4B).copy(alpha = 0.15f) else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isActive) Color(0xFF0D6B4B) else Color(0xFF666666)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Icon(
                                        imageVector = when (key) {
                                            "fajr" -> Icons.Filled.NightsStay
                                            "shuruq" -> Icons.Filled.WbTwilight
                                            "dhuhr" -> Icons.Filled.WbSunny
                                            "asr" -> Icons.Filled.WbCloudy
                                            "maghrib" -> Icons.Filled.WbTwilight
                                            else -> Icons.Filled.NightsStay
                                        },
                                        contentDescription = title,
                                        tint = if (isActive) Color(0xFF0D6B4B) else Color(0xFFFFB300),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = timeStr,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isActive) Color(0xFF0D6B4B) else Color(0xFF333333)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Midnight and Last Third indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Midnight
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            val midnightTime = times?.midnight ?: "—"
                            Text(
                                text = if (lang == "ar") "منتصف الليل : $midnightTime" else "Midnight: $midnightTime",
                                color = Color(0xFF1E88E5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Vertical separator divider
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp)
                                .background(Color.LightGray)
                        )

                        // Last Third
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE0F7FA))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            val lastThirdTime = times?.lastThird ?: "—"
                            Text(
                                text = if (lang == "ar") "الثلث الأخير : $lastThirdTime" else "Last Third: $lastThirdTime",
                                color = Color(0xFF00ACC1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                color = Color(0xFF173C2E)
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
                            .background(Color(0xFFF8F9FA))
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
                            color = Color(0xFF495057),
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

        // --- 4. PROFILE COMPLETION ONBOARDING CARD ---
        if (showOnboarding) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToFeature("AUTH") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Close 'X' Button
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                                .clickable { showOnboarding = false; homePrefs.edit().putBoolean("onboarding_dismissed", true).apply() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = if (lang == "ar") "إغلاق" else "Close",
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Text and progress bar
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "ar") "إعداد العضوية والبيانات الشخصية" else "Set up your membership and profile",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = if (lang == "ar") "سجّل الدخول أو أكمل بياناتك الشخصية الآن" else "Sign in or complete your personal details now",
                                fontSize = 12.sp,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (lang == "ar") "يمكنك تسجيل الدخول الآن، أو متابعة استخدام التطبيق دون عضوية." else "You can sign in now, or continue using the app without membership.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
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
                                    contentDescription = "Close",
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
                            contentDescription = "Day/Night",
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
            color = Color(0xFF333333),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
