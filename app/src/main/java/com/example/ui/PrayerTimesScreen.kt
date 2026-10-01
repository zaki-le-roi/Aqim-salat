package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerCalculator
import java.text.SimpleDateFormat
import java.util.*

data class QazaItem(
    val key: String,
    val name: String,
    val count: Int,
    val onPlus: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: (() -> Unit)? = null
) {
    val times by viewModel.prayerTimes.collectAsState()
    val nextName by viewModel.nextPrayerName.collectAsState()
    val nextTime by viewModel.nextPrayerTime.collectAsState()
    val countdown by viewModel.countdownText.collectAsState()
    val currentName by viewModel.currentPrayerName.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val calcMethodStr by viewModel.calcMethod.collectAsState()
    val madhabStr by viewModel.madhab.collectAsState()
    val nearbyMosques by viewModel.nearbyRealMosques.collectAsState()
    val nearestMosque by viewModel.nearestRealMosque.collectAsState()

    var showQazaTracker by remember { mutableStateOf(false) }
    var showMonthlyTimes by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    // Qaza counts are persisted by AppViewModel so they survive screen recreation.
    var qazaFajr by remember { mutableIntStateOf(viewModel.getQazaCount("Fajr")) }
    var qazaDhuhr by remember { mutableIntStateOf(viewModel.getQazaCount("Dhuhr")) }
    var qazaAsr by remember { mutableIntStateOf(viewModel.getQazaCount("Asr")) }
    var qazaMaghrib by remember { mutableIntStateOf(viewModel.getQazaCount("Maghrib")) }
    var qazaIsha by remember { mutableIntStateOf(viewModel.getQazaCount("Isha")) }

    // Local theme colors to prevent @Composable Canvas compile failures
    val baseOnSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("prayer_back_button")) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = if (lang == "ar") "رجوع" else "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = Translations.get("prayer_times", lang),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(
                        onClick = { showQazaTracker = !showQazaTracker; showMonthlyTimes = false },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (showQazaTracker) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                    ) {
                        Icon(Icons.Filled.EventRepeat, contentDescription = if (lang == "ar") "سجل القضاء" else "Qaza Tracker", tint = Color(0xFFD4AF37))
                    }
                    IconButton(
                        onClick = { showMonthlyTimes = !showMonthlyTimes; showQazaTracker = false },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (showMonthlyTimes) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = if (lang == "ar") "شهري" else "Monthly", tint = Color(0xFFD4AF37))
                    }
                }
            }
        }

        // --- Nearby Mosques ---
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (lang == "ar") "المساجد القريبة" else "Nearby Mosques", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Filled.Mosque, contentDescription = null, tint = Color(0xFFD4AF37))
                    }
                    nearestMosque?.let { mosque ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = if (lang == "ar") "أقرب مسجد: ${mosque.nameAr}" else "Nearest: ${mosque.nameEn}", fontWeight = FontWeight.Bold)
                        Text(text = String.format(Locale.US, "%.2f كم", mosque.distanceKm), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.openMosqueNavigation(mosque) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Navigation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (lang == "ar") "الملاحة إلى أقرب مسجد" else "Navigate to nearest mosque")
                        }
                    }
                    if (nearbyMosques.isEmpty()) {
                        Text(text = if (lang == "ar") "لم يتم العثور على مساجد مسجلة حول موقعك بعد." else "No mapped mosques found around your location yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), modifier = Modifier.padding(top = 10.dp))
                    } else {
                        nearbyMosques.take(5).forEach { mosque ->
                            ListItem(headlineContent = { Text(if (lang == "ar") mosque.nameAr else mosque.nameEn, fontWeight = FontWeight.Medium) }, supportingContent = { Text(String.format(Locale.US, "%.2f كم", mosque.distanceKm)) }, leadingContent = { Icon(Icons.Filled.Place, contentDescription = null) }, trailingContent = { IconButton(onClick = { viewModel.openMosqueNavigation(mosque) }) { Icon(Icons.Filled.Navigation, contentDescription = if (lang == "ar") "الملاحة" else "Navigate") } })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        if (showQazaTracker) {
            // --- Qaza (Missed) Prayer Tracker Ledger ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ar") "سجل قضاء الصلوات الفائتة" else if (lang == "tr") "Kaza Namazı Çetelesi" else "Qaza Missed Prayers Ledger",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(Icons.Filled.History, contentDescription = if (lang == "ar") "القضاء" else "Qaza", tint = Color(0xFFD4AF37))
                        }
                        Text(
                            text = if (lang == "ar") "تتبع صلواتك الفائتة التي تعتزم قضاءها واجعل ذمتك تبرأ تدريجياً." else "Keep track of outstanding makeup prayers to systematically complete them.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val qazas = listOf(
                            QazaItem("Fajr", Translations.get("fajr", lang), qazaFajr, { qazaFajr = (qazaFajr + 1) }),
                            QazaItem("Dhuhr", Translations.get("dhuhr", lang), qazaDhuhr, { qazaDhuhr = (qazaDhuhr + 1) }),
                            QazaItem("Asr", Translations.get("asr", lang), qazaAsr, { qazaAsr = (qazaAsr + 1) }),
                            QazaItem("Maghrib", Translations.get("maghrib", lang), qazaMaghrib, { qazaMaghrib = (qazaMaghrib + 1) }),
                            QazaItem("Isha", Translations.get("isha", lang), qazaIsha, { qazaIsha = (qazaIsha + 1) })
                        )

                        qazas.forEach { qaza ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(qaza.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            when (qaza.key) {
                                                "Fajr" -> { if (qazaFajr > 0) qazaFajr--; viewModel.changeQazaCount("Fajr", -1) }
                                                "Dhuhr" -> { if (qazaDhuhr > 0) qazaDhuhr--; viewModel.changeQazaCount("Dhuhr", -1) }
                                                "Asr" -> { if (qazaAsr > 0) qazaAsr--; viewModel.changeQazaCount("Asr", -1) }
                                                "Maghrib" -> { if (qazaMaghrib > 0) qazaMaghrib--; viewModel.changeQazaCount("Maghrib", -1) }
                                                "Isha" -> { if (qazaIsha > 0) qazaIsha--; viewModel.changeQazaCount("Isha", -1) }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = if (lang == "ar") "إنقاص" else "Minus", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    Text(
                                        text = qaza.count.toString(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            qaza.onPlus()
                                            viewModel.changeQazaCount(qaza.key, 1)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = if (lang == "ar") "إضافة" else "Plus", tint = Color(0xFFD4AF37))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (showMonthlyTimes) {
            // --- Monthly Prayer Times Calendar Grid ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ar") "مواقيت الشهر الفضيل" else if (lang == "tr") "Aylık Vakitler" else "Monthly Timetable",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(Icons.Filled.CalendarMonth, contentDescription = if (lang == "ar") "الشهر" else "Month", tint = Color(0xFFD4AF37))
                        }
                        Text(
                            text = if (lang == "ar") "مواقيت الصلاة المقدرة لشهر رمضان والأيام القادمة بموقعك الحالي." else "Prayer timetables predicted for Ramadan and coming weeks at your current location.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                if (lang == "ar") "اليوم" else "Day",
                                if (lang == "ar") "الفجر" else "Fajr",
                                if (lang == "ar") "الظهر" else "Dhuhr",
                                if (lang == "ar") "العصر" else "Asr",
                                if (lang == "ar") "المغرب" else "Maghrib",
                                if (lang == "ar") "العشاء" else "Isha"
                            ).forEach { label ->
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Generate 30 days of times based on current calculations
                        val calendar = Calendar.getInstance()
                        val sdfDay = SimpleDateFormat("dd MMM", Locale.getDefault())
                        val calculationMethod = try {
                            PrayerCalculator.CalculationMethod.valueOf(calcMethodStr)
                        } catch (e: Exception) {
                            PrayerCalculator.CalculationMethod.MWL
                        }
                        val calculationMadhab = try {
                            PrayerCalculator.Madhab.valueOf(madhabStr)
                        } catch (e: Exception) {
                            PrayerCalculator.Madhab.STANDARD
                        }
                        
                        // Find timezone offset
                        val tzOffset = calendar.timeZone.getOffset(System.currentTimeMillis()) / 3600000.0

                        for (i in 0..29) {
                            val targetDate = calendar.time
                            val dayStr = sdfDay.format(targetDate)

                            // Compute actual times for that specific day.
                            // If calculation fails, show an unavailable value rather than fabricated times.
                            val t = try {
                                PrayerCalculator.calculateTimes(
                                    latitude = lat,
                                    longitude = lng,
                                    timezoneOffset = tzOffset,
                                    date = targetDate,
                                    method = calculationMethod,
                                    madhab = calculationMadhab
                                )
                            } catch (_: Exception) {
                                null
                            }
                            
                            val dayTimes = listOf(
                                dayStr,
                                t?.fajr ?: "--:--",
                                t?.dhuhr ?: "--:--",
                                t?.asr ?: "--:--",
                                t?.maghrib ?: "--:--",
                                t?.isha ?: "--:--"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                dayTimes.forEachIndexed { index, timeVal ->
                                    Text(
                                        text = timeVal,
                                        fontSize = 11.sp,
                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (index == 0) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                }
            }
        } else {
            // --- Primary Active Countdown Dial Gauge Card ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (lang == "ar") "الصلاة القادمة" else "NEXT PRAYER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = Translations.get(nextName.lowercase(), lang) + " ($nextTime)",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large Dynamic Circular Ring Gauge
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Dial Canvas (using external safe color parameter)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = baseOnSurfaceColor,
                                    radius = size.width / 2f,
                                    style = Stroke(width = 12.dp.toPx())
                                )
                                drawArc(
                                    color = Color(0xFFD4AF37),
                                    startAngle = -90f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx())
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = countdown,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = Translations.get("remaining", lang),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                onClick = { showLocationDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .testTag("change_location_button")
                                    .clickable { showLocationDialog = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = if (lang == "ar") "الموقع" else "Location", tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = locationName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Filled.Edit, contentDescription = if (lang == "ar") "تعديل الموقع" else "Edit Location", tint = Color(0xFFD4AF37), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Today's list
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (lang == "ar") "أوقات صلاة اليوم" else if (lang == "tr") "Bugünkü Vakitler" else "Today's Prayer Times",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val list = listOf("imsak", "fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha", "midnight", "lastThird")
                        list.forEach { pKey ->
                            val pTime = when (pKey) {
                                "imsak" -> times?.imsak ?: "--:--"
                                "fajr" -> times?.fajr ?: "--:--"
                                "sunrise" -> times?.sunrise ?: "--:--"
                                "dhuhr" -> times?.dhuhr ?: "--:--"
                                "asr" -> times?.asr ?: "--:--"
                                "maghrib" -> times?.maghrib ?: "--:--"
                                "isha" -> times?.isha ?: "--:--"
                                "midnight" -> times?.midnight ?: "--:--"
                                else -> times?.lastThird ?: "--:--"
                            }

                            val isCurrent = pKey.equals(currentName, ignoreCase = true)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isCurrent) Color(0xFFD4AF37).copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (pKey) {
                                            "imsak" -> Icons.Filled.WbTwilight
                                            "fajr" -> Icons.Filled.WbTwilight
                                            "sunrise" -> Icons.Filled.WbSunny
                                            "dhuhr" -> Icons.Filled.WbSunny
                                            "asr" -> Icons.Filled.WbCloudy
                                            "maghrib" -> Icons.Filled.WbTwilight
                                            "isha" -> Icons.Filled.NightsStay
                                            "midnight" -> Icons.Filled.NightsStay
                                            else -> Icons.Filled.Star
                                        },
                                        contentDescription = pKey,
                                        tint = if (isCurrent) Color(0xFFC59B27) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = Translations.get(pKey, lang),
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = if (isCurrent) Color(0xFFC59B27) else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = pTime,
                                    fontSize = 16.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color(0xFFC59B27) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        var customCityName by remember { mutableStateOf("") }
        var customLatitude by remember { mutableStateOf("") }
        var customLongitude by remember { mutableStateOf("") }
        var locationError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = {
                Text(
                    text = if (lang == "ar") "تحديد الموقع" else if (lang == "tr") "Konum Seçimi" else "Select Location",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // GPS auto-detection button
                    item {
                        Button(
                            onClick = {
                                viewModel.startLocationTracking()
                                showLocationDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("gps_auto_detect_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MyLocation, contentDescription = if (lang == "ar") "الموقع الجغرافي" else "GPS", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ar") "تحديد تلقائي عبر GPS" else if (lang == "tr") "Otomatik GPS Konumu" else "Auto Detect (GPS)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Preset cities divider
                    item {
                        Text(
                            text = if (lang == "ar") "المدن المقترحة" else if (lang == "tr") "Önerilen Şehirler" else "Suggested Cities",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Preset cities list (Algeria & global)
                    val presetCities = listOf(
                        Triple("Algiers", 36.7538, 3.0588),
                        Triple("Oran", 35.6971, -0.6308),
                        Triple("Constantine", 36.3650, 6.6147),
                        Triple("Annaba", 36.9000, 7.7667),
                        Triple("Mecca", 21.4225, 39.8262),
                        Triple("Medina", 24.4673, 39.6111),
                        Triple("Cairo", 30.0444, 31.2357),
                        Triple("Istanbul", 41.0082, 28.9784),
                        Triple("London", 51.5074, -0.1278),
                        Triple("Paris", 48.8566, 2.3522),
                        Triple("New York", 40.7128, -74.0060)
                    )

                    items(presetCities) { (name, latVal, lngVal) ->
                        val displayName = when (name) {
                            "Algiers" -> if (lang == "ar") "الجزائر العاصمة (Alger)" else "Algiers"
                            "Oran" -> if (lang == "ar") "وهران (Oran)" else "Oran"
                            "Constantine" -> if (lang == "ar") "قسنطينة (Constantine)" else "Constantine"
                            "Annaba" -> if (lang == "ar") "عنابة (Annaba)" else "Annaba"
                            "Mecca" -> if (lang == "ar") "مكة المكرمة" else "Mecca"
                            "Medina" -> if (lang == "ar") "المدينة المنورة" else "Medina"
                            "Cairo" -> if (lang == "ar") "القاهرة" else "Cairo"
                            "Istanbul" -> if (lang == "ar") "إسطنبول" else "Istanbul"
                            "London" -> if (lang == "ar") "لندن" else "London"
                            "Paris" -> if (lang == "ar") "باريس" else "Paris"
                            "New York" -> if (lang == "ar") "نيويورك" else "New York"
                            else -> name
                        }
                        
                        Surface(
                            onClick = {
                                viewModel.setManualCity(displayName, latVal, lngVal)
                                showLocationDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationCity, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(displayName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            }
                        }
                    }

                    // Custom Coordinates section divider
                    item {
                        Text(
                            text = if (lang == "ar") "إحداثيات مخصصة" else if (lang == "tr") "Özel Koordinatlar" else "Custom Coordinates",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Custom City Name Input
                    item {
                        OutlinedTextField(
                            value = customCityName,
                            onValueChange = { customCityName = it },
                            label = { Text(if (lang == "ar") "اسم المدينة" else "City Name") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_city_input"),
                            singleLine = true
                        )
                    }

                    // Custom Latitude Input
                    item {
                        OutlinedTextField(
                            value = customLatitude,
                            onValueChange = { customLatitude = it },
                            label = { Text(if (lang == "ar") "خط العرض (Latitude)" else "Latitude") },
                            placeholder = { Text(if (lang == "ar") "مثال: 36.75" else "e.g. 36.75") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_lat_input"),
                            singleLine = true
                        )
                    }

                    // Custom Longitude Input
                    item {
                        OutlinedTextField(
                            value = customLongitude,
                            onValueChange = { customLongitude = it },
                            label = { Text(if (lang == "ar") "خط الطول (Longitude)" else "Longitude") },
                            placeholder = { Text(if (lang == "ar") "مثال: 3.05" else "e.g. 3.05") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_lng_input"),
                            singleLine = true
                        )
                    }

                    if (locationError.isNotEmpty()) {
                        item {
                            Text(locationError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val latVal = customLatitude.toDoubleOrNull()
                        val lngVal = customLongitude.toDoubleOrNull()
                        val nameVal = customCityName.trim()

                        if (nameVal.isEmpty()) {
                            locationError = if (lang == "ar") "يرجى إدخال اسم المدينة" else "Please enter city name"
                        } else if (latVal == null || latVal < -90.0 || latVal > 90.0) {
                            locationError = if (lang == "ar") "يرجى إدخال خط عرض صحيح (-90 إلى 90)" else "Please enter valid latitude (-90 to 90)"
                        } else if (lngVal == null || lngVal < -180.0 || lngVal > 180.0) {
                            locationError = if (lang == "ar") "يرجى إدخال خط طول صحيح (-180 إلى 180)" else "Please enter valid longitude (-180 to 180)"
                        } else {
                            viewModel.setManualCity(nameVal, latVal, lngVal)
                            showLocationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("save_custom_location_button")
                ) {
                    Text(if (lang == "ar") "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text(if (lang == "ar") "إلغاء" else "Cancel")
                }
            }
        )
    }
}
