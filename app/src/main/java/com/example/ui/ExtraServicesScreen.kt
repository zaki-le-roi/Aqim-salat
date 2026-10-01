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
import com.example.data.PrayerCalculator
import java.util.Date

@Composable
fun ZakatCalculatorScreen(lang: String, onBack: () -> Unit) {
    var cash by remember { mutableStateOf("") }
    var gold by remember { mutableStateOf("") }
    var silver by remember { mutableStateOf("") }
    var trade by remember { mutableStateOf("") }
    var debts by remember { mutableStateOf("") }
    var nisab by remember { mutableStateOf("") }
    fun value(s: String) = s.replace(',', '.').toDoubleOrNull() ?: 0.0
    val net = (value(cash) + value(gold) + value(silver) + value(trade) - value(debts)).coerceAtLeast(0.0)
    val threshold = value(nisab)
    val zakat = if (threshold > 0 && net >= threshold) net * 0.025 else 0.0

    SimpleExtraScaffold(if (lang == "ar") "حاسبة الزكاة" else "Zakat Calculator", onBack) {
        Text("أدخل القيم بالعملة التي تستخدمها، واضبط النصاب وفق المرجع الشرعي الذي تتبعه.", fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        ExtraNumberField("النقد والمدخرات", cash) { cash = it }
        ExtraNumberField("قيمة الذهب", gold) { gold = it }
        ExtraNumberField("قيمة الفضة", silver) { silver = it }
        ExtraNumberField("عروض التجارة", trade) { trade = it }
        ExtraNumberField("الديون المستحقة القابلة للخصم", debts) { debts = it }
        ExtraNumberField("قيمة النصاب", nisab) { nisab = it }
        Spacer(Modifier.height(12.dp))
        ResultCard("صافي المال", String.format("%.2f", net))
        ResultCard("الزكاة المقدرة 2.5%", String.format("%.2f", zakat))
        if (threshold <= 0) Text("أدخل قيمة النصاب لإظهار النتيجة.", color = MaterialTheme.colorScheme.error)
        else if (net < threshold) Text("المبلغ المدخل دون النصاب المحدد.")
    }
}

@Composable
fun DailyAccountabilityScreen(lang: String, onBack: () -> Unit, challenge: Boolean = false) {
    val items = remember { listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء", "قراءة القرآن", "أذكار اليوم") }
    val checked = remember { mutableStateMapOf<String, Boolean>() }
    val done = items.count { checked[it] == true }

    SimpleExtraScaffold(if (challenge) "تحدي الطاعات" else "ورد المحاسبة", onBack) {
        Text(if (challenge) "سجّل إنجازك اليومي وحافظ على الاستمرارية." else "راجع أعمال يومك وسجّل ما أتممته.", fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(progress = { done.toFloat() / items.size }, modifier = Modifier.fillMaxWidth())
        Text("$done / ${items.size}", modifier = Modifier.padding(vertical = 8.dp), fontWeight = FontWeight.Bold)
        items.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked[item] == true, onCheckedChange = { checked[item] = it })
                    Text(item, Modifier.weight(1f), fontSize = 15.sp)
                    if (checked[item] == true) Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}

data class WorldCity(val name: String, val lat: Double, val lon: Double, val tz: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldPrayerTimesScreen(lang: String, onBack: () -> Unit) {
    val cities = remember {
        listOf(
            WorldCity("الجزائر", 36.7538, 3.0588, 1.0),
            WorldCity("العلمة", 36.1528, 5.6902, 1.0),
            WorldCity("مكة المكرمة", 21.4225, 39.8262, 3.0),
            WorldCity("المدينة المنورة", 24.4672, 39.6024, 3.0),
            WorldCity("القاهرة", 30.0444, 31.2357, 2.0),
            WorldCity("إسطنبول", 41.0082, 28.9784, 3.0),
            WorldCity("باريس", 48.8566, 2.3522, 2.0),
            WorldCity("لندن", 51.5074, -0.1278, 1.0)
        )
    }
    var selected by remember { mutableStateOf(cities.first()) }
    var expanded by remember { mutableStateOf(false) }
    val times = remember(selected) {
        PrayerCalculator.calculateTimes(selected.lat, selected.lon, selected.tz, Date(), PrayerCalculator.CalculationMethod.MWL, PrayerCalculator.Madhab.STANDARD)
    }

    SimpleExtraScaffold(if (lang == "ar") "الصلاة حول العالم" else "Prayer Times Around the World", onBack) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selected.name, onValueChange = {}, readOnly = true,
                label = { Text("المدينة") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                cities.forEach { city ->
                    DropdownMenuItem(text = { Text(city.name) }, onClick = { selected = city; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        listOf("الفجر" to times.fajr, "الشروق" to times.sunrise, "الظهر" to times.dhuhr, "العصر" to times.asr, "المغرب" to times.maghrib, "العشاء" to times.isha)
            .forEach { (name, time) -> ResultCard(name, time) }
    }
}

@Composable
fun PremiumFeaturesScreen(lang: String, onBack: () -> Unit) {
    SimpleExtraScaffold(if (lang == "ar") "أقم صلاتك المميز" else "Aqim Salah Premium", onBack) {
        Icon(Icons.Filled.Star, null, tint = Color(0xFFD4AF37), modifier = Modifier.size(56.dp))
        Text("ميزات حصرية", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        listOf("تجربة أكثر تخصيصاً", "خيارات موسعة للتذكيرات", "أولوية للميزات الجديدة").forEach {
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(it, Modifier.padding(16.dp)) }
        }
        Text("هذه الشاشة تعريفية حالياً ولا تنفذ دفعاً أو اشتراكاً تلقائياً.", fontSize = 12.sp)
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
