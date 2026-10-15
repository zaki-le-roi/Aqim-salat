package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.data.PrayerCalculator
import java.util.Date



@Composable
fun DailyAccountabilityScreen(lang: String, onBack: () -> Unit, challenge: Boolean = false) {
    val items = remember(lang) { if (lang == "ar") listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء", "قراءة القرآن", "أذكار اليوم") else listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Quran reading", "Daily adhkar") }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("daily_accountability", android.content.Context.MODE_PRIVATE) }
    val dayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val checked = remember(dayKey, lang, challenge) {
        mutableStateMapOf<String, Boolean>().apply {
            items.forEach { item ->
                if (prefs.getBoolean("${if (challenge) "challenge" else "accountability"}_${dayKey}_${item}", false)) {
                    this[item] = true
                }
            }
        }
    }
    val done = items.count { checked[it] == true }

    SimpleExtraScaffold(if (challenge) "تحدي الطاعات" else "ورد المحاسبة", onBack) {
        Text(if (lang == "ar") { if (challenge) "سجّل إنجازك اليومي وحافظ على الاستمرارية." else "راجع أعمال يومك وسجّل ما أتممته." } else { if (challenge) "Track your daily progress and keep your streak." else "Review your daily deeds and record what you completed." }, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(progress = { done.toFloat() / items.size }, modifier = Modifier.fillMaxWidth())
        Text("$done / ${items.size}", modifier = Modifier.padding(vertical = 8.dp), fontWeight = FontWeight.Bold)
        items.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked[item] == true,
                        onCheckedChange = { value ->
                            checked[item] = value
                            prefs.edit().putBoolean(
                                "${if (challenge) "challenge" else "accountability"}_${dayKey}_${item}",
                                value
                            ).apply()
                        }
                    )
                    Text(item, Modifier.weight(1f), fontSize = 15.sp)
                    if (checked[item] == true) Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}

data class WorldCity(val nameAr: String, val nameEn: String, val lat: Double, val lon: Double, val timeZoneId: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldPrayerTimesScreen(lang: String, onBack: () -> Unit) {
    val cities = remember {
        listOf(
            WorldCity("الجزائر", "Algiers", 36.7538, 3.0588, "Africa/Algiers"),
            WorldCity("العلمة", "El Eulma", 36.1528, 5.6902, "Africa/Algiers"),
            WorldCity("مكة المكرمة", "Makkah", 21.4225, 39.8262, "Asia/Riyadh"),
            WorldCity("المدينة المنورة", "Madinah", 24.4672, 39.6024, "Asia/Riyadh"),
            WorldCity("القاهرة", "Cairo", 30.0444, 31.2357, "Africa/Cairo"),
            WorldCity("إسطنبول", "Istanbul", 41.0082, 28.9784, "Europe/Istanbul"),
            WorldCity("باريس", "Paris", 48.8566, 2.3522, "Europe/Paris"),
            WorldCity("لندن", "London", 51.5074, -0.1278, "Europe/London")
        )
    }
    var selected by remember { mutableStateOf(cities.first()) }
    var expanded by remember { mutableStateOf(false) }
    val times = remember(selected) {
        val now = Date()
        val cityTimeZone = java.util.TimeZone.getTimeZone(selected.timeZoneId)
        val offsetHours = cityTimeZone.getOffset(now.time) / 3600000.0
        PrayerCalculator.calculateTimes(
            selected.lat, selected.lon, offsetHours, now,
            PrayerCalculator.CalculationMethod.MWL,
            PrayerCalculator.Madhab.STANDARD,
            cityTimeZone
        )
    }

    SimpleExtraScaffold(if (lang == "ar") "الصلاة حول العالم" else "Prayer Times Around the World", onBack) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = if (lang == "ar") selected.nameAr else selected.nameEn, onValueChange = {}, readOnly = true,
                label = { Text(if (lang == "ar") "المدينة" else "City") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                cities.forEach { city ->
                    DropdownMenuItem(text = { Text(if (lang == "ar") city.nameAr else city.nameEn) }, onClick = { selected = city; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        listOf("الفجر" to times.fajr, "الشروق" to times.sunrise, "الظهر" to times.dhuhr, "العصر" to times.asr, "المغرب" to times.maghrib, "العشاء" to times.isha)
            .forEach { (name, time) -> ResultCard(name, time) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleExtraScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "رجوع") }
        })
    }) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { item { Column(content = content) } }
    }
}

@Composable
private fun ExtraNumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ResultCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}
