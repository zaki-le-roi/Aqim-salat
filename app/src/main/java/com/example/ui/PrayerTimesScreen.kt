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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerCalculator
import com.example.data.PrayerTimeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.ZoneId


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
    
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val selectedTimeZoneId by viewModel.timeZoneId.collectAsState()
    val calcMethodStr by viewModel.calcMethod.collectAsState()
    val madhabStr by viewModel.madhab.collectAsState()

    var showMonthlyTimes by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }


    // Local theme colors to prevent @Composable Canvas compile failures
    val baseOnSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                        onClick = { showMonthlyTimes = !showMonthlyTimes },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (showMonthlyTimes) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = if (lang == "ar") "شهري" else "Monthly", tint = Color(0xFFD4AF37))
                    }
                }
            }
        }

        if (showMonthlyTimes) {
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

                        val zone = PrayerTimeEngine.zoneIdOrDevice(selectedTimeZoneId)
                        val startDate = PrayerTimeEngine.localDate(zone)
                        val dayFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
                        val calculationMethod = runCatching { PrayerCalculator.CalculationMethod.valueOf(calcMethodStr) }
                            .getOrDefault(PrayerCalculator.CalculationMethod.MWL)
                        val calculationMadhab = runCatching { PrayerCalculator.Madhab.valueOf(madhabStr) }
                            .getOrDefault(PrayerCalculator.Madhab.STANDARD)
                        for (i in 0..29) {
                            val targetDate = startDate.plusDays(i.toLong())
                            val offset = zone.rules.getOffset(targetDate.atTime(12, 0).atZone(zone).toInstant())
                            val dayStr = targetDate.format(dayFormatter) + "\nUTC" + offset.id.replace("Z", "+00:00")
                            val t = runCatching {
                                PrayerCalculator.calculateTimes(lat, lng, targetDate, zone, calculationMethod, calculationMadhab)
                            }.getOrNull()
                            val dayTimes = listOf(dayStr, t?.fajr ?: "--:--", t?.dhuhr ?: "--:--",
                                t?.asr ?: "--:--", t?.maghrib ?: "--:--", t?.isha ?: "--:--")
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly) {
                                dayTimes.forEachIndexed { index, timeVal ->
                                    Text(text = timeVal, fontSize = 11.sp,
                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (index == 0) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                }
                            }
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
        var citySearchQuery by remember { mutableStateOf("") }
        var customLatitude by remember { mutableStateOf("") }
        var customLongitude by remember { mutableStateOf("") }
        var locationError by remember { mutableStateOf("") }
        var isSearchingCity by remember { mutableStateOf(false) }
        var showManualCoordinates by remember { mutableStateOf(false) }
        var geocodedCityResults by remember { mutableStateOf<List<Triple<String, Double, Double>>>(emptyList()) }

        LaunchedEffect(citySearchQuery) {
            val query = citySearchQuery.trim()
            if (query.isBlank()) {
                geocodedCityResults = emptyList()
                isSearchingCity = false
            } else {
                isSearchingCity = true
                kotlinx.coroutines.delay(450)
                geocodedCityResults = withContext(Dispatchers.IO) {
                    runCatching {
                        if (!Geocoder.isPresent()) emptyList()
                        else {
                            @Suppress("DEPRECATION")
                            Geocoder(context, Locale("ar")).getFromLocationName(query, 8).orEmpty()
                                .mapNotNull { address ->
                                    if (!address.hasLatitude() || !address.hasLongitude()) null
                                    else {
                                        val place = address.locality ?: address.subAdminArea ?: address.adminArea ?: query
                                        val country = address.countryName.orEmpty()
                                        val name = listOf(place, country).filter { it.isNotBlank() }
                                            .distinct().joinToString("، ")
                                        Triple(name.ifBlank { query }, address.latitude, address.longitude)
                                    }
                                }.distinctBy { "${it.first}:${it.second}:${it.third}" }
                        }
                    }.getOrDefault(emptyList())
                }
                isSearchingCity = false
            }
        }

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            containerColor = Color(0xFFFAF8F5),
            title = {
                Text(
                    text = if (lang == "ar") "تحديد الموقع" else "Choose location",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = if (lang == "ar")
                                "ابحث عن أي مدينة أو اختر موقع الهاتف الحالي."
                            else "Search for any city or use your phone's current location.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = citySearchQuery,
                            onValueChange = {
                                citySearchQuery = it
                                locationError = ""
                            },
                            modifier = Modifier.fillMaxWidth().testTag("filter_preset_cities_input"),
                            singleLine = true,
                            placeholder = {
                                Text(if (lang == "ar") "اسم المدينة أو الدولة…" else "City or country…")
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(19.dp))
                            },
                            trailingIcon = {
                                if (citySearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { citySearchQuery = "" }) {
                                        Icon(Icons.Filled.Clear, contentDescription = if (lang == "ar") "مسح" else "Clear")
                                    }
                                }
                            }
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                val hasFine = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasFine || hasCoarse) {
                                    viewModel.startLocationTracking()
                                    showLocationDialog = false
                                } else {
                                    showLocationDialog = false
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("gps_auto_detect_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF115E3F))
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (lang == "ar") "استخدام موقعي الحالي (GPS)" else "Use current location (GPS)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isSearchingCity) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF115E3F)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (lang == "ar") "جارٍ البحث عن المدينة…" else "Searching cities…", fontSize = 12.sp)
                            }
                        }
                    }
                    if (citySearchQuery.isNotBlank() && !isSearchingCity && geocodedCityResults.isEmpty()) {
                        item {
                            Text(
                                if (lang == "ar") "لم نعثر على نتائج. جرّب كتابة اسم المدينة مع الدولة."
                                else "No results. Try the city name together with its country.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    items(
                        geocodedCityResults,
                        key = { "${it.first}:${it.second}:${it.third}" }
                    ) { city ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (locationName == city.first) Color(0xFFE8F0E9) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (locationName == city.first) Color(0xFF115E3F) else Color(0xFFE8DDC8)
                            ),
                            modifier = Modifier.fillMaxWidth().clickable {
                                viewModel.setManualCity(city.first, city.second, city.third)
                                showLocationDialog = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.LocationCity,
                                    contentDescription = null,
                                    tint = Color(0xFF115E3F),
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        city.first,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF25251F),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${"%.4f".format(Locale.US, city.second)}, ${"%.4f".format(Locale.US, city.third)}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = Color(0xFFB08A2E))
                            }
                        }
                    }
                    item {
                        HorizontalDivider(color = Color(0xFFE8DDC8))
                        TextButton(
                            onClick = { showManualCoordinates = !showManualCoordinates },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (showManualCoordinates)
                                    (if (lang == "ar") "إخفاء الإحداثيات اليدوية" else "Hide manual coordinates")
                                else
                                    (if (lang == "ar") "إدخال الإحداثيات يدويًا" else "Enter coordinates manually"),
                                color = Color(0xFF115E3F),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (showManualCoordinates) {
                        item {
                            OutlinedTextField(
                                value = citySearchQuery,
                                onValueChange = { citySearchQuery = it; locationError = "" },
                                label = { Text(if (lang == "ar") "اسم المدينة أو الموقع" else "City or place name") },
                                modifier = Modifier.fillMaxWidth().testTag("custom_city_input"),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = customLatitude,
                                onValueChange = { customLatitude = it; locationError = "" },
                                label = { Text(if (lang == "ar") "خط العرض" else "Latitude") },
                                placeholder = { Text("36.75") },
                                modifier = Modifier.fillMaxWidth().testTag("custom_lat_input"),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = customLongitude,
                                onValueChange = { customLongitude = it; locationError = "" },
                                label = { Text(if (lang == "ar") "خط الطول" else "Longitude") },
                                placeholder = { Text("3.05") },
                                modifier = Modifier.fillMaxWidth().testTag("custom_lng_input"),
                                singleLine = true
                            )
                        }
                        item {
                            Button(
                                onClick = {
                                    val name = citySearchQuery.trim()
                                    val lat = customLatitude.toDoubleOrNull()
                                    val lng = customLongitude.toDoubleOrNull()
                                    locationError = when {
                                        name.isBlank() -> if (lang == "ar") "أدخل اسم الموقع." else "Enter a place name."
                                        lat == null || lat !in -90.0..90.0 -> if (lang == "ar") "خط العرض غير صحيح." else "Invalid latitude."
                                        lng == null || lng !in -180.0..180.0 -> if (lang == "ar") "خط الطول غير صحيح." else "Invalid longitude."
                                        else -> ""
                                    }
                                    if (locationError.isEmpty()) {
                                        viewModel.setManualCity(name, lat!!, lng!!)
                                        showLocationDialog = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF115E3F))
                            ) {
                                Text(if (lang == "ar") "حفظ الموقع" else "Save location", color = Color.White)
                            }
                        }
                        if (locationError.isNotEmpty()) {
                            item {
                                Text(locationError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text(if (lang == "ar") "إغلاق" else "Close")
                }
            }
        )
    }
}
