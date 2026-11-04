package com.example.ui

import android.provider.Settings
import androidx.compose.ui.platform.LocalContext

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
private fun HomeServiceCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    colors: CardColors = CardDefaults.cardColors(containerColor = Color(0xFF115E3F)),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        content = content
    )
}


/** Reuses the countdown card's restrained emerald-and-raised-gold frame on compact home cards. */
private fun Modifier.premiumIslamicFrame(shape: RoundedCornerShape): Modifier =
    this
        .shadow(8.dp, shape)
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(Color(0xFF176447), Color(0xFF0B3B2D), Color(0xFF062B22))
            )
        )
        .border(1.5.dp, Color(0xFFD4AF37), shape)
        .drawWithCache {
            val gold = Color(0xFFD4AF37)
            val highlight = Color(0xFFFFE8A0)
            onDrawBehind {
                val inset = 5.dp.toPx()
                val corner = 13.dp.toPx()
                drawRoundRect(
                    color = gold.copy(alpha = 0.82f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                    style = Stroke(width = 0.8.dp.toPx())
                )
                drawLine(
                    highlight.copy(alpha = 0.75f),
                    Offset(inset + corner, inset),
                    Offset(size.width - inset - corner, inset),
                    1.dp.toPx()
                )
                val arm = 9.dp.toPx()
                val points = listOf(
                    Offset(inset + corner, inset + corner),
                    Offset(size.width - inset - corner, inset + corner),
                    Offset(inset + corner, size.height - inset - corner),
                    Offset(size.width - inset - corner, size.height - inset - corner)
                )
                points.forEachIndexed { index, point ->
                    val sx = if (index % 2 == 0) 1f else -1f
                    val sy = if (index < 2) 1f else -1f
                    drawLine(gold, point, Offset(point.x + arm * sx, point.y), 1.2.dp.toPx())
                    drawLine(gold, point, Offset(point.x, point.y + arm * sy), 1.2.dp.toPx())
                    drawLine(
                        highlight.copy(alpha = 0.8f),
                        Offset(point.x + arm * sx * 0.45f, point.y),
                        Offset(point.x, point.y + arm * sy * 0.45f),
                        1.dp.toPx()
                    )
                }
            }
        }

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
            val nextPrayerArabic = when (nextName.lowercase()) {
                "fajr" -> "الفجر"
                "dhuhr" -> "الظهر"
                "asr" -> "العصر"
                "maghrib" -> "المغرب"
                "isha" -> "العشاء"
                else -> "الصلاة القادمة"
            }
            var skyPeriod by remember { mutableStateOf(SkyPeriod.NIGHT) }
            LaunchedEffect(times) {
                val prayerTimes = times ?: return@LaunchedEffect
                fun seconds(clock: String): Int {
                    val parts = clock.split(":")
                    return if (parts.size >= 2) parts[0].toIntOrNull()?.times(3600)?.plus(parts[1].toIntOrNull()?.times(60) ?: 0) ?: 0 else 0
                }
                val fajr = seconds(prayerTimes.fajr)
                val sunrise = seconds(prayerTimes.sunrise)
                val dhuhr = seconds(prayerTimes.dhuhr)
                val asr = seconds(prayerTimes.asr)
                val maghrib = seconds(prayerTimes.maghrib)
                val isha = seconds(prayerTimes.isha)
                while (true) {
                    val now = java.util.Calendar.getInstance()
                    val nowSeconds = now.get(java.util.Calendar.HOUR_OF_DAY) * 3600 +
                        now.get(java.util.Calendar.MINUTE) * 60 + now.get(java.util.Calendar.SECOND)
                    val boundaries = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
                    val nextBoundary = boundaries.firstOrNull { it > nowSeconds } ?: (fajr + 86400)
                    skyPeriod = skyPeriodFor(nowSeconds, fajr, sunrise, dhuhr, asr, maghrib, isha)
                    kotlinx.coroutines.delay(((nextBoundary - nowSeconds) * 1000L).coerceAtLeast(1000L))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
            ) {
                PrayerSkyBackground(period = skyPeriod)
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
                        Spacer(modifier = Modifier.size(40.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("أقم صلاتك", color = Color(0xFFFFE7A3), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text(locationName, color = Color.White.copy(alpha = 0.86f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable { onNavigateToFeature("APP_MENU") }
                                .testTag("home_settings_menu_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "القائمة والإعدادات", tint = Color(0xFFFFE7A3))
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
                    Box(
                        modifier = Modifier
                            .shadow(10.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF176447), Color(0xFF0B3B2D), Color(0xFF062B22))
                                )
                            )
                            .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(22.dp))
                            .drawWithCache {
                                val gold = Color(0xFFD4AF37)
                                val highlight = Color(0xFFFFE8A0)
                                onDrawBehind {
                                    val inset = 5.dp.toPx()
                                    val corner = 13.dp.toPx()
                                    // Double engraved frame with a subtle raised-gold highlight.
                                    drawRoundRect(
                                        color = gold.copy(alpha = 0.82f),
                                        topLeft = Offset(inset, inset),
                                        size = Size(size.width - inset * 2, size.height - inset * 2),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                                        style = Stroke(width = 0.8.dp.toPx())
                                    )
                                    drawLine(highlight.copy(alpha = 0.75f), Offset(inset + corner, inset), Offset(size.width - inset - corner, inset), 1.dp.toPx())
                                    // Restrained Islamic-inspired arabesque corner flourishes.
                                    val arm = 9.dp.toPx()
                                    val points = listOf(
                                        Offset(inset + corner, inset + corner),
                                        Offset(size.width - inset - corner, inset + corner),
                                        Offset(inset + corner, size.height - inset - corner),
                                        Offset(size.width - inset - corner, size.height - inset - corner)
                                    )
                                    points.forEachIndexed { index, p ->
                                        val sx = if (index % 2 == 0) 1f else -1f
                                        val sy = if (index < 2) 1f else -1f
                                        drawLine(gold, p, Offset(p.x + arm * sx, p.y), 1.2.dp.toPx())
                                        drawLine(gold, p, Offset(p.x, p.y + arm * sy), 1.2.dp.toPx())
                                        drawLine(highlight.copy(alpha = 0.8f), Offset(p.x + arm * sx * 0.45f, p.y), Offset(p.x, p.y + arm * sy * 0.45f), 1.dp.toPx())
                                    }
                                }
                            }
                            .padding(horizontal = 28.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "۞ الْوَقْتُ الْمُتَبَقِّي ۞",
                                color = Color(0xFFFFE7A3),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                countdown,
                                color = Color(0xFFFFD76A),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .width(54.dp)
                                    .height(1.dp)
                                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFFFE7A3), Color.Transparent)))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .premiumIslamicFrame(RoundedCornerShape(18.dp))
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
                    .offset(y = (-10).dp) // Slight overlap for depth
                    .premiumIslamicFrame(RoundedCornerShape(30.dp))
                    .testTag("home_hijri_date_card"),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            tint = Color(0xFFFFE8A0),
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
                        color = Color(0xFFFFE8A0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = if (lang == "ar") "مشاركة" else "Share",
                        tint = Color(0xFFD4AF37),
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
                        val prayerDrawable = when (key) {
                            "fajr" -> R.drawable.prayer_card_fajr
                            "dhuhr" -> R.drawable.prayer_card_dhuhr
                            "asr" -> R.drawable.prayer_card_asr
                            "maghrib" -> R.drawable.prayer_card_maghrib
                            else -> R.drawable.prayer_card_isha
                        }
                        Card(
                            modifier = Modifier
                                .width(100.dp)
                                .height(128.dp)
                                .then(
                                    if (isNext) Modifier.border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(18.dp))
                                    else Modifier
                                )
                                .testTag("prayer_time_$key"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNext) Color(0xFF134233) else Color(0xFF0F362A)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(id = prayerDrawable),
                                    contentDescription = title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                if (isNext) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFD4AF37))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "القادمة",
                                            color = Color(0xFF0E3125),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.38f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = timeText,
                                        color = Color(0xFFFFD979),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. BENTO SERVICES GRID ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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

                // Mishkat: independent conversational Islamic reference, separate from Zad Al-Muslim.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(108.dp)
                        .clickable { onNavigateToFeature("MISHKAT") }
                        .testTag("home_service_mishkat"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.card_1_mishkat),
                            contentDescription = if (lang == "ar") "مشكاة" else "Mishkat",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Premium full-width Quran card.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(132.dp)
                        .clickable { onNavigateToFeature("QURAN") }
                        .testTag("home_service_quran"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.card_2_quran),
                            contentDescription = if (lang == "ar") "القرآن الكريم" else "The Holy Quran",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // "زاد المسلم" — coordinated emerald, deep green and gold library card.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(118.dp)
                        .clickable { onNavigateToFeature("ZAD_ALMUSLIM") }
                        .testTag("home_service_zad_al_muslim"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.card_3_zad_almuslim),
                            contentDescription = if (lang == "ar") "زاد المسلم" else "Zad Al-Muslim",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Row 1: Tasbih and Adhkar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeServiceCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable { onNavigateToFeature("TASBIH") }
                            .testTag("home_service_tasbih"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_4_sebha),
                                contentDescription = if (lang == "ar") "السبحة الإلكترونية" else "Digital Tasbih",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    HomeServiceCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable { onNavigateToFeature("ADHKAR") }
                            .testTag("home_service_adhkar"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_5_azkar),
                                contentDescription = if (lang == "ar") "الأذكار اليومية" else "Daily Adhkar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // Row 2: Qibla, Hisn al-Muslim, Hadith Books
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeServiceCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(158.dp)
                            .clickable { onNavigateToFeature("QIBLA") }
                            .testTag("home_service_qibla"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_6_qibla),
                                contentDescription = if (lang == "ar") "القبلة" else "Qibla",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    HomeServiceCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(158.dp)
                            .clickable { onNavigateToFeature("HISN_AL_MUSLIM") }
                            .testTag("home_service_hisn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_7_hisn_almuslim),
                                contentDescription = if (lang == "ar") "حصن المسلم" else "Hisn Muslim",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    HomeServiceCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(158.dp)
                            .clickable { onNavigateToFeature("HADITH") }
                            .testTag("home_service_hadith"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_8_hadith_books),
                                contentDescription = if (lang == "ar") "كتب الحديث" else "Hadith Books",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

            }
        }


        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "© 2026 Djenba Zakaria. جميع الحقوق محفوظة.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("home_copyright"),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
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

enum class SkyPeriod { NIGHT, DAWN, MORNING, MIDDAY, AFTERNOON, SUNSET }

/** Pure time-of-day classifier. All arguments are seconds after local midnight. */
fun skyPeriodFor(
    nowSeconds: Int,
    fajrSeconds: Int,
    sunriseSeconds: Int,
    dhuhrSeconds: Int,
    asrSeconds: Int,
    maghribSeconds: Int,
    ishaSeconds: Int
): SkyPeriod {
    val now = ((nowSeconds % 86400) + 86400) % 86400
    return when {
        now < fajrSeconds || now >= ishaSeconds -> SkyPeriod.NIGHT
        now < sunriseSeconds -> SkyPeriod.DAWN
        now < dhuhrSeconds -> SkyPeriod.MORNING
        now < asrSeconds -> SkyPeriod.MIDDAY
        now < maghribSeconds -> SkyPeriod.AFTERNOON
        else -> SkyPeriod.SUNSET
    }
}

@Composable
fun PrayerSkyBackground(period: SkyPeriod, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val animationDurationScale = remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)
    }
    val transitionDuration = if (animationDurationScale <= 0f) 0 else 900
    val colors = when (period) {
        SkyPeriod.NIGHT -> listOf(Color(0xFF102C3B), Color(0xFF17483F), Color(0xFF071F1B))
        SkyPeriod.DAWN -> listOf(Color(0xFF4D7F8A), Color(0xFF6E9B91), Color(0xFF294F45))
        SkyPeriod.MORNING -> listOf(Color(0xFF72B9C1), Color(0xFF9CCDBB), Color(0xFF3F8171))
        SkyPeriod.MIDDAY -> listOf(Color(0xFF5BA8C0), Color(0xFF9BD0D0), Color(0xFF438D78))
        SkyPeriod.AFTERNOON -> listOf(Color(0xFFB9A06B), Color(0xFFD8A06C), Color(0xFF7C6850))
        SkyPeriod.SUNSET -> listOf(Color(0xFFC85A3C), Color(0xFFE78350), Color(0xFF603C36))
    }
    val top by animateColorAsState(colors[0], animationSpec = tween(transitionDuration), label = "sky-top")
    val middle by animateColorAsState(colors[1], animationSpec = tween(transitionDuration), label = "sky-middle")
    val bottom by animateColorAsState(colors[2], animationSpec = tween(transitionDuration), label = "sky-bottom")
    Box(modifier = modifier.background(Brush.verticalGradient(listOf(top, middle, bottom)))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gold = Color(0xFFFFE7A3)
            val nightAlpha = when (period) {
                SkyPeriod.NIGHT -> 0.95f
                SkyPeriod.DAWN -> 0.32f
                else -> 0f
            }
            val sunAlpha = when (period) {
                SkyPeriod.MORNING, SkyPeriod.MIDDAY, SkyPeriod.AFTERNOON, SkyPeriod.SUNSET -> 1f
                else -> 0f
            }
            val sunY = when (period) {
                SkyPeriod.MORNING -> 0.22f
                SkyPeriod.MIDDAY -> 0.18f
                SkyPeriod.AFTERNOON -> 0.27f
                SkyPeriod.SUNSET -> 0.39f
                else -> 0.18f
            }
            if (sunAlpha > 0f) {
                drawCircle(Color(0xFFFFC36B).copy(alpha = 0.14f), radius = w * 0.12f, center = Offset(w * 0.76f, h * sunY))
                drawCircle(Color(0xFFFFD18A).copy(alpha = 0.28f), radius = w * 0.085f, center = Offset(w * 0.76f, h * sunY))
                drawCircle(Color(0xFFFFE2A0).copy(alpha = sunAlpha), radius = w * 0.052f, center = Offset(w * 0.76f, h * sunY))
            }
            if (nightAlpha > 0f) {
                drawCircle(gold.copy(alpha = nightAlpha), radius = w * 0.064f, center = Offset(w * 0.80f, h * 0.18f))
                drawCircle(colors[0].copy(alpha = nightAlpha), radius = w * 0.058f, center = Offset(w * 0.825f, h * 0.15f))
                listOf(
                    Offset(w * 0.16f, h * 0.13f),
                    Offset(w * 0.30f, h * 0.22f),
                    Offset(w * 0.57f, h * 0.11f),
                    Offset(w * 0.91f, h * 0.30f),
                    Offset(w * 0.10f, h * 0.34f)
                ).forEach { drawCircle(gold.copy(alpha = 0.8f * nightAlpha), radius = 1.8.dp.toPx(), center = it) }
            }
            val mosque = Color(0xFF041713).copy(alpha = 0.86f)
            drawRect(mosque, topLeft = Offset(0f, h * 0.82f), size = Size(w, h * 0.18f))
            drawRect(mosque, topLeft = Offset(w * 0.13f, h * 0.52f), size = Size(w * 0.045f, h * 0.39f))
            drawCircle(mosque, radius = w * 0.024f, center = Offset(w * 0.153f, h * 0.51f))
            drawRect(mosque, topLeft = Offset(w * 0.82f, h * 0.47f), size = Size(w * 0.05f, h * 0.44f))
            drawCircle(mosque, radius = w * 0.026f, center = Offset(w * 0.845f, h * 0.46f))
            val dome = Path().apply {
                moveTo(w * 0.20f, h * 0.81f)
                quadraticTo(w * 0.50f, h * 0.40f, w * 0.80f, h * 0.81f)
                close()
            }
            drawPath(dome, mosque)
            drawRect(mosque, topLeft = Offset(w * 0.20f, h * 0.80f), size = Size(w * 0.60f, h * 0.20f))
            drawLine(Color(0xFFD4AF37).copy(alpha = 0.55f), Offset(0f, h * 0.96f), Offset(w, h * 0.96f), strokeWidth = 1.dp.toPx())
        }
    }
}
