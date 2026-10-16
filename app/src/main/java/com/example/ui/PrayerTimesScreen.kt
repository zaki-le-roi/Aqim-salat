package com.example.ui

import android.Manifest
import android.location.Geocoder
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.startLocationTracking()
        }
    }

    val times by viewModel.prayerTimes.collectAsState()
    val nextName by viewModel.nextPrayerName.collectAsState()
    val nextTime by viewModel.nextPrayerTime.collectAsState()
    val countdown by viewModel.countdownText.collectAsState()
    val currentName by viewModel.currentPrayerName.collectAsState()
    val hijriDate by viewModel.hijriDateString.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val locationConfigured by viewModel.locationConfigured.collectAsState()
    val isTrackingLocation by viewModel.isTrackingLocation.collectAsState()
    
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val calcMethodStr by viewModel.calcMethod.collectAsState()
    val madhabStr by viewModel.madhab.collectAsState()

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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF101F2B), Color(0xFF0B3329), Color(0xFF071F1B))
                )
            )
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
                        color = Color(0xFFD4AF37)
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
                                text = if (lang == "ar") "مواقيت الشهر" else if (lang == "tr") "Aylık Vakitler" else "Monthly Timetable",
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
            // --- Luxury emerald and gold prayer hero ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(470.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF172B3A),
                                        Color(0xFF123A36),
                                        Color(0xFF071F1B)
                                    )
                                )
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val sky = Color(0xFF172B3A)
                            // Crescent and quiet stars.
                            drawCircle(Color(0xFFFFE7A3), radius = w * 0.055f, center = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.17f))
                            drawCircle(sky, radius = w * 0.050f, center = androidx.compose.ui.geometry.Offset(w * 0.805f, h * 0.145f))
                            listOf(
                                androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.15f),
                                androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.22f),
                                androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.10f),
                                androidx.compose.ui.geometry.Offset(w * 0.88f, h * 0.31f),
                                androidx.compose.ui.geometry.Offset(w * 0.12f, h * 0.32f)
                            ).forEach { drawCircle(Color(0xFFFFE7A3).copy(alpha = 0.75f), radius = 2.dp.toPx(), center = it) }
                            // Mosque silhouette: minarets, domes and the central prayer hall.
                            val silhouette = Color(0xFF041713).copy(alpha = 0.76f)
                            drawRect(silhouette, topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.79f), size = androidx.compose.ui.geometry.Size(w, h * 0.21f))
                            drawRect(silhouette, topLeft = androidx.compose.ui.geometry.Offset(w * 0.12f, h * 0.52f), size = androidx.compose.ui.geometry.Size(w * 0.055f, h * 0.36f))
                            drawCircle(silhouette, radius = w * 0.028f, center = androidx.compose.ui.geometry.Offset(w * 0.147f, h * 0.51f))
                            drawRect(silhouette, topLeft = androidx.compose.ui.geometry.Offset(w * 0.83f, h * 0.47f), size = androidx.compose.ui.geometry.Size(w * 0.055f, h * 0.41f))
                            drawCircle(silhouette, radius = w * 0.028f, center = androidx.compose.ui.geometry.Offset(w * 0.858f, h * 0.46f))
                            val dome = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.20f, h * 0.78f)
                                quadraticTo(w * 0.50f, h * 0.35f, w * 0.80f, h * 0.78f)
                                close()
                            }
                            drawPath(dome, silhouette)
                            drawRect(silhouette, topLeft = androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.77f), size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.18f))
                            val smallDome = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.06f, h * 0.80f)
                                quadraticTo(w * 0.17f, h * 0.62f, w * 0.28f, h * 0.80f)
                                close()
                            }
                            drawPath(smallDome, silhouette)
                            val rightDome = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.72f, h * 0.80f)
                                quadraticTo(w * 0.83f, h * 0.63f, w * 0.94f, h * 0.80f)
                                close()
                            }
                            drawPath(rightDome, silhouette)
                            // Fine gold horizon.
                            drawLine(Color(0xFFD4AF37).copy(alpha = 0.45f), androidx.compose.ui.geometry.Offset(0f, h * 0.955f), androidx.compose.ui.geometry.Offset(w, h * 0.955f), strokeWidth = 1.dp.toPx())
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF061B19).copy(alpha = 0.18f),
                                            Color(0xFF061B19).copy(alpha = 0.34f),
                                            Color(0xFF061B19).copy(alpha = 0.58f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (lang == "ar") "الصلاة القادمة" else "NEXT PRAYER",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF4E5B2),
                                    letterSpacing = if (lang == "ar") 0.sp else 1.8.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Translations.get(nextName.lowercase(), lang),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    color = Color(0xFFD4AF37),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 52.sp
                                )
                                Text(
                                    text = if (nextTime.isNotBlank()) "موعدها $nextTime" else if (lang == "ar") "جارٍ حساب وقت الصلاة" else "Calculating prayer time",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.94f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(12.dp, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFF115E3F).copy(alpha = 0.85f))
                                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.95f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (lang == "ar") "الوقت المتبقي" else "TIME REMAINING",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF4E5B2),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = countdown,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    onClick = { showLocationDialog = true },
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.10f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.65f)),
                                    modifier = Modifier.testTag("change_location_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = if (lang == "ar") "الموقع" else "Location", tint = Color(0xFFD4AF37), modifier = Modifier.size(17.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (locationConfigured) locationName else if (lang == "ar") "حدّد موقعك" else "Set your location",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Filled.Edit, contentDescription = if (lang == "ar") "تعديل الموقع" else "Edit location", tint = Color(0xFFF4E5B2), modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(
                                    text = hijriDate,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    color = Color(0xFFF4E5B2),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
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
        var isSearchingCity by remember { mutableStateOf(false) }
        var citySearchFilter by remember { mutableStateOf("") }
        val locationScope = rememberCoroutineScope()

        var geocodedCityResults by remember { mutableStateOf<List<Triple<String, Double, Double>>>(emptyList()) }
        val presetCities = remember {
            val algerianWilayas = listOf(
                Triple("أدرار",27.8743,-0.2939), Triple("الشلف",36.1653,1.3345), Triple("الأغواط",33.8000,2.8651),
                Triple("أم البواقي",35.8754,7.1135), Triple("باتنة",35.5559,6.1741), Triple("بجاية",36.7509,5.0567),
                Triple("بسكرة",34.8504,5.7281), Triple("بشار",31.6167,-2.2167), Triple("البليدة",36.4700,2.8277),
                Triple("البويرة",36.3749,3.9020), Triple("تمنراست",22.7850,5.5228), Triple("تبسة",35.4042,8.1242),
                Triple("تلمسان",34.8783,-1.3150), Triple("تيارت",35.3710,1.3170), Triple("تيزي وزو",36.7118,4.0459),
                Triple("الجزائر العاصمة",36.7538,3.0588), Triple("الجلفة",34.6704,3.2504), Triple("جيجل",36.8206,5.7667),
                Triple("سطيف",36.1911,5.4137), Triple("سعيدة",34.8303,0.1517), Triple("سكيكدة",36.8667,6.9000),
                Triple("سيدي بلعباس",35.1899,-0.6309), Triple("عنابة",36.9000,7.7667), Triple("قالمة",36.4621,7.4261),
                Triple("قسنطينة",36.3650,6.6147), Triple("المدية",36.2674,2.7500), Triple("مستغانم",35.9311,0.0892),
                Triple("المسيلة",35.7058,4.5419), Triple("معسكر",35.3966,0.1403), Triple("ورقلة",31.9520,5.3330),
                Triple("وهران",35.6987,-0.6349), Triple("البيض",33.6832,1.0193), Triple("إليزي",26.4833,8.4667),
                Triple("برج بوعريريج",36.0732,4.7611), Triple("بومرداس",36.7564,3.4772), Triple("الطارف",36.7672,8.3138),
                Triple("تندوف",27.6711,-8.1474), Triple("تيسمسيلت",35.6072,1.8108), Triple("الوادي",33.3561,6.8632),
                Triple("خنشلة",35.4358,7.1433), Triple("سوق أهراس",36.2864,7.9511), Triple("تيبازة",36.5897,2.4475),
                Triple("ميلة",36.4503,6.2644), Triple("عين الدفلى",36.2641,1.9679), Triple("النعامة",33.2667,-0.3167),
                Triple("عين تموشنت",35.2975,-1.1404), Triple("غرداية",32.4909,3.6735), Triple("غليزان",35.7373,0.5559),
                Triple("تيميمون",29.2639,0.2306), Triple("برج باجي مختار",21.3270,0.9517), Triple("أولاد جلال",34.4333,5.0667),
                Triple("بني عباس",30.1333,-2.1667), Triple("عين صالح",27.1975,2.4800), Triple("عين قزام",19.5667,5.7667),
                Triple("تقرت",33.1000,6.0667), Triple("جانت",24.5541,9.4840), Triple("المغير",33.9500,5.9167),
                Triple("المنيعة",30.5833,2.8833)
            )
            val worldCities = listOf(
                Triple("مكة المكرمة",21.4225,39.8262), Triple("المدينة المنورة",24.4672,39.6111),
                Triple("القدس الشريف",31.7683,35.2137), Triple("الرياض",24.7136,46.6753), Triple("جدة",21.5433,39.1728),
                Triple("القاهرة",30.0444,31.2357), Triple("الإسكندرية",31.2001,29.9187), Triple("تونس",36.8065,10.1815),
                Triple("صفاقس",34.7406,10.7603), Triple("سوسة",35.8256,10.6084), Triple("الرباط",34.0209,-6.8416),
                Triple("الدار البيضاء",33.5731,-7.5898), Triple("فاس",34.0181,-5.0078), Triple("مراكش",31.6295,-7.9811),
                Triple("طنجة",35.7595,-5.8340), Triple("طرابلس",32.8872,13.1913), Triple("بنغازي",32.1167,20.0667),
                Triple("الخرطوم",15.5007,32.5599), Triple("دمشق",33.5138,36.2765), Triple("حلب",36.2021,37.1343),
                Triple("بغداد",33.3152,44.3661), Triple("البصرة",30.5085,47.7804), Triple("عمّان",31.9454,35.9284),
                Triple("بيروت",33.8938,35.5018), Triple("الكويت",29.3759,47.9774), Triple("الدوحة",25.2854,51.5310),
                Triple("أبوظبي",24.4539,54.3773), Triple("دبي",25.2048,55.2708), Triple("مسقط",23.5880,58.3829),
                Triple("المنامة",26.2285,50.5860), Triple("إسطنبول",41.0082,28.9784), Triple("أنقرة",39.9334,32.8597),
                Triple("جاكرتا",-6.2088,106.8456), Triple("كوالالمبور",3.1390,101.6869), Triple("باريس",48.8566,2.3522),
                Triple("لندن",51.5074,-0.1278), Triple("مدريد",40.4168,-3.7038), Triple("روما",41.9028,12.4964),
                Triple("برلين",52.5200,13.4050), Triple("نيويورك",40.7128,-74.0060), Triple("تورونتو",43.6532,-79.3832),
                Triple("لوس أنجلوس",34.0522,-118.2437), Triple("طوكيو",35.6762,139.6503), Triple("سيول",37.5665,126.9780),
                Triple("دلهي",28.6139,77.2090), Triple("إسلام آباد",33.6844,73.0479), Triple("كراتشي",24.8607,67.0011),
                Triple("داكار",14.7167,-17.4677), Triple("نيروبي",-1.2921,36.8219), Triple("كيب تاون",-33.9249,18.4241)
            )
            val collator = java.text.Collator.getInstance(Locale("ar"))
            (algerianWilayas + worldCities).distinctBy { it.first }
                .sortedWith(Comparator { a, b -> collator.compare(a.first, b.first) })
        }
        val filteredPresetCities = remember(citySearchFilter, geocodedCityResults, presetCities) {
            val query = citySearchFilter.trim()
            val matchingPresets = if (query.isBlank()) presetCities else presetCities.filter {
                it.first.contains(query, ignoreCase = true)
            }
            val collator = java.text.Collator.getInstance(Locale("ar"))
            (matchingPresets + geocodedCityResults).distinctBy { it.first.lowercase(Locale.ROOT) }
                .sortedWith(Comparator { a, b -> collator.compare(a.first, b.first) })
        }
        LaunchedEffect(citySearchFilter) {
            val query = citySearchFilter.trim()
            if (query.isBlank()) {
                geocodedCityResults = emptyList()
            } else {
                kotlinx.coroutines.delay(350)
                geocodedCityResults = withContext(Dispatchers.IO) {
                    runCatching {
                        if (!Geocoder.isPresent()) emptyList()
                        else {
                            @Suppress("DEPRECATION")
                            Geocoder(context, Locale("ar")).getFromLocationName(query, 15).orEmpty().mapNotNull { address ->
                                if (!address.hasLatitude() || !address.hasLongitude()) null
                                else {
                                    val place = address.locality ?: address.subAdminArea ?: address.adminArea ?: query
                                    val country = address.countryName.orEmpty()
                                    val name = listOf(place, country).filter { it.isNotBlank() }.distinct().joinToString("، ")
                                    Triple(name.ifBlank { query }, address.latitude, address.longitude)
                                }
                            }
                        }
                    }.getOrDefault(emptyList())
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            containerColor = Color(0xFFFAF8F5),
            title = {
                Text(
                    text = if (lang == "ar") "تحديد المدينة أو الموقع" else "Select City or Location",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 540.dp)
                ) {
                    // Quick City Selection section
                    item {
                        Text(
                            text = "اختيار مباشر من المدن الشائعة (بدون GPS):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = citySearchFilter,
                            onValueChange = { citySearchFilter = it },
                            placeholder = { Text("ابحث في قائمة المدن…", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("filter_preset_cities_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (citySearchFilter.isNotEmpty()) {
                                    IconButton(onClick = { citySearchFilter = "" }) {
                                        Icon(Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        )
                    }

                    // Scrollable manual list; worldwide Geocoder matches are added while typing.
                    item {
                        Text(
                            text = if (lang == "ar") "ولايات الجزائر ومدن العالم" else "Algerian wilayas and cities worldwide",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F4C3A),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp, max = 230.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredPresetCities, key = { it.first }) { city ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (locationName == city.first) Color(0xFF0F4C3A) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DDC8)),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.setManualCity(city.first, city.second, city.third)
                                        showLocationDialog = false
                                    }.testTag("city_chip_${city.first}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.LocationCity,
                                            contentDescription = null,
                                            tint = if (locationName == city.first) Color.White else Color(0xFF0F4C3A),
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(9.dp))
                                        Text(
                                            text = city.first,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (locationName == city.first) Color.White else Color(0xFF25251F),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Divider
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    // GPS auto-detection button
                    item {
                        Button(
                            onClick = {
                                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasFine || hasCoarse) {
                                    viewModel.startLocationTracking()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("gps_auto_detect_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MyLocation, contentDescription = if (lang == "ar") "الموقع الجغرافي" else "GPS", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ar") "تحديد تلقائي عبر GPS" else "Auto Detect (GPS)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = if (isTrackingLocation) "جارٍ استقبال إحداثيات GPS من الهاتف…" else "استخدام GPS اختياري؛ يحتفظ التطبيق بموقعك المحفوظ ولا يطلب تفعيل GPS في كل مرة.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("فتح إعدادات الموقع بالهاتف", fontSize = 12.sp)
                        }
                    }

                    // City search uses the device geocoder instead of a fixed list of city coordinates.
                    item {
                        Text(
                            text = "ابحث عن أي مدينة لتحديد إحداثياتها الحقيقية",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
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

                    // City name input and real geocoder lookup
                    item {
                        OutlinedTextField(
                            value = customCityName,
                            onValueChange = { customCityName = it; locationError = "" },
                            label = { Text("اسم المدينة أو المنطقة") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_city_input"),
                            singleLine = true
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                val query = customCityName.trim()
                                if (query.isBlank()) {
                                    locationError = "اكتب اسم المدينة أولًا"
                                } else {
                                    isSearchingCity = true
                                    locationError = ""
                                    locationScope.launch {
                                        @Suppress("DEPRECATION")
                                        fun lookupCity(): List<android.location.Address> {
                                            if (!Geocoder.isPresent()) return emptyList()
                                            return Geocoder(context, Locale("ar")).getFromLocationName(query, 1).orEmpty()
                                        }
                                        val matches = withContext(Dispatchers.IO) {
                                            runCatching { lookupCity() }.getOrDefault(emptyList())
                                        }
                                        val address = matches.firstOrNull()
                                        if (address != null && address.hasLatitude() && address.hasLongitude()) {
                                            customLatitude = address.latitude.toString()
                                            customLongitude = address.longitude.toString()
                                            customCityName = listOfNotNull(
                                                address.locality ?: address.subAdminArea ?: address.adminArea,
                                                address.countryName
                                            ).joinToString("، ").ifBlank { query }
                                            locationError = "تم العثور على إحداثيات المدينة. اضغط حفظ لتطبيقها."
                                        } else {
                                            locationError = "تعذر العثور على المدينة. تحقق من الإنترنت أو أدخل الإحداثيات يدويًا."
                                        }
                                        isSearchingCity = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSearchingCity
                        ) {
                            Text(if (isSearchingCity) "جارٍ البحث عن المدينة…" else "البحث عن المدينة وإحضار إحداثياتها")
                        }
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
