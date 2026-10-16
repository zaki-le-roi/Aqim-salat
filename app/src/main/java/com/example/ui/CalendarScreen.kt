package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerCalculator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: (() -> Unit)? = null
) {

    val today = remember { Date() }
    val dateInputFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.US) }
    val todayParts = remember { dateInputFormat.format(today).split("-") }

    var gregDay by remember { mutableStateOf(todayParts.getOrNull(0) ?: "01") }
    var gregMonth by remember { mutableStateOf(todayParts.getOrNull(1) ?: "01") }
    var gregYear by remember { mutableStateOf(todayParts.getOrNull(2) ?: "2026") }
    var convertedHijriResult by remember { mutableStateOf("") }

    val currentHijri = PrayerCalculator.getHijriDate(today, "ar")
    val isHijriLeapYear = ((11 * currentHijri.year + 14) % 30) < 11
    val currentHijriMonthLength = when {
        currentHijri.month == 12 && isHijriLeapYear -> 30
        currentHijri.month % 2 == 1 -> 30
        else -> 29
    }
    val daysInCurrentHijriMonth = (1..currentHijriMonthLength).toList()

    val islamicEvents = listOf(
        Triple("بداية شهر رمضان", "بداية صيام شهر رمضان المبارك", "١ رمضان"),
        Triple("ليلة القدر", "ليلة القدر المباركة في العشر الأواخر", "٢٧ رمضان"),
        Triple("عيد الفطر", "عيد الفطر المبارك (١ شوال)", "١ شوال"),
        Triple("يوم عرفة", "يوم عرفة المبارك (٩ ذو الحجة)", "٩ ذو الحجة"),
        Triple("عيد الأضحى", "عيد الأضحى المبارك (١٠ ذو الحجة)", "١٠ ذو الحجة"),
        Triple("رأس السنة الهجرية", "بداية العام الهجري الجديد (١ محرم)", "١ محرم")
    )

    fun performConversion() {
        val d = gregDay.toIntOrNull() ?: 1
        val m = gregMonth.toIntOrNull() ?: 1
        val y = gregYear.toIntOrNull() ?: 2026

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, d)
        calendar.set(Calendar.MONTH, m - 1)
        calendar.set(Calendar.YEAR, y)

        val hijri = PrayerCalculator.getHijriDate(calendar.time, "ar")
        convertedHijriResult = "${hijri.day} ${hijri.monthName} ${hijri.year} هـ"
    }

    LaunchedEffect(gregDay, gregMonth, gregYear) {
        performConversion()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calendar_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "التقويم الهجري",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Current Date Hero Card
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
                    Text(
                        text = "تاريخ اليوم الهجري",
                        fontSize = 13.sp,
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${currentHijri.day} ${currentHijri.monthName} ${currentHijri.year} هـ",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 22.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = SimpleDateFormat("EEEE، dd MMMM yyyy", Locale("ar")).format(Date()),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Current Hijri month grid — derived from the live Hijri date, not hard-coded sample data.
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "شهر ${currentHijri.monthName} ${currentHijri.year} هـ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Weekdays headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
                        days.forEach { d ->
                            Text(
                                text = d,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Render the current Hijri month as a real 30-day calendar.
                    daysInCurrentHijriMonth.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { cellNum ->
                                val isToday = cellNum == currentHijri.day
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isToday) Color(0xFFD4AF37)
                                            else Color.Transparent
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cellNum.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            repeat(7 - week.size) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Converter Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "محول التاريخ الميلادي إلى الهجري",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "أدخل التاريخ الميلادي وسيُحسب التاريخ الهجري تلقائيًا",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = gregDay,
                            onValueChange = { gregDay = it },
                            label = { Text("اليوم") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                        )
                        TextField(
                            value = gregMonth,
                            onValueChange = { gregMonth = it },
                            label = { Text("الشهر") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                        )
                        TextField(
                            value = gregYear,
                            onValueChange = { gregYear = it },
                            label = { Text("السنة") },
                            modifier = Modifier.weight(1.5f),
                            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                        )
                    }

                    if (convertedHijriResult.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD4AF37).copy(alpha = 0.12f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = convertedHijriResult,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 18.sp,
                                lineHeight = 26.sp,
                                color = Color(0xFF8B6B15),
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // Key Islamic Occasions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "المناسبات الإسلامية الهامة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    islamicEvents.forEach { (title, description, dateStr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD4AF37).copy(alpha = 0.22f))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = dateStr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF76570C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
