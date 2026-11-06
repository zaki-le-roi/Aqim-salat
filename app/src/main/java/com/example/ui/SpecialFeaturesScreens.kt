package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.FridayReminderScheduler
import com.example.IslamicCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale

private val featureGold = androidx.compose.ui.graphics.Color(0xFFD4AF37)

@Composable
fun FridayFeatureScreen(viewModel: AppViewModel, lang: String) {
    val context = LocalContext.current
    val zoneId by viewModel.timeZoneId.collectAsState()
    var enabled by remember { mutableStateOf(context.getSharedPreferences("friday_reminder", Context.MODE_PRIVATE).getBoolean("enabled", false)) }
    val zone = remember(zoneId) { runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault()) }
    val fridayNow = java.time.ZonedDateTime.now(zone).dayOfWeek == java.time.DayOfWeek.FRIDAY
    LaunchedEffect(zoneId, enabled) {
        if (enabled) FridayReminderScheduler.schedule(context, zone.id)
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FeatureIntroCard(Icons.Filled.Event, "فضل يوم الجمعة", if (fridayNow) "اليوم يوم الجمعة — تقبّل الله طاعتكم" else "فضل الجمعة وسننها وآدابها") }
        item {
            FeatureInfoCard("فضل الجمعة", "قال الله تعالى: ﴿يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا نُودِيَ لِلصَّلَاةِ مِن يَوْمِ الْجُمُعَةِ فَاسْعَوْا إِلَىٰ ذِكْرِ اللَّهِ وَذَرُوا الْبَيْعَ﴾ [الجمعة: 9].")
            FeatureInfoCard("فضل اليوم", "يوم الجمعة خير أيام الأسبوع، وفيه ساعة لا يوافقها عبد مسلم يسأل الله خيرًا إلا أعطاه إياه. (صحيح مسلم، كتاب الجمعة)")
            FeatureInfoCard("سنن وآداب الجمعة", "الاغتسال، والسواك، والتطيب، ولبس أحسن الثياب، والتبكير إلى الصلاة، والإنصات للخطبة، وعدم تخطي رقاب الناس.")
            FeatureInfoCard("سورة الكهف", "يُستحب قراءة سورة الكهف يوم الجمعة. وردت في فضلها أحاديث حسّنها وصححها عدد من أهل العلم، مع اختلافهم في بعض طرقها.")
            FeatureInfoCard("الصلاة على النبي ﷺ", "أكثروا من الصلاة والسلام على النبي محمد صلى الله عليه وسلم يوم الجمعة وليلتها.")
            FeatureInfoCard("ساعة الإجابة", "تحرَّ ساعة الإجابة وأكثر من الدعاء، ومن الأقوال المعتبرة أنها آخر ساعة بعد العصر.")
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("تذكير الجمعة الأسبوعي", fontWeight = FontWeight.Bold)
                        Text("تنبيه اختياري صباح كل جمعة وفق توقيت المدينة المختارة.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = enabled, onCheckedChange = { value ->
                        val allowed = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        if (value && !allowed) {
                            android.widget.Toast.makeText(context, "فعّل إذن الإشعارات من إعدادات الهاتف أولًا.", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            enabled = value
                            if (value) FridayReminderScheduler.schedule(context, zone.id) else FridayReminderScheduler.cancel(context)
                        }
                    })
                }
            }
        }
        item { Text("المراجع: القرآن الكريم، سورة الجمعة؛ صحيح مسلم، كتاب الجمعة؛ صحيح البخاري، كتاب الجمعة. تُعرض المسائل المختلف في درجتها بصياغة غير جازمة.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

private data class WeatherSnapshot(val temperature: Double, val feels: Double, val humidity: Int, val wind: Double, val rain: Int, val high: Double, val low: Double, val condition: String) {
    fun json() = JSONObject().put("temperature", temperature).put("feels", feels).put("humidity", humidity).put("wind", wind).put("rain", rain).put("high", high).put("low", low).put("condition", condition)
    companion object {
        fun parse(j: JSONObject) = WeatherSnapshot(j.getDouble("temperature"), j.getDouble("feels"), j.getInt("humidity"), j.getDouble("wind"), j.getInt("rain"), j.getDouble("high"), j.getDouble("low"), j.getString("condition"))
    }
}

private fun loadWeather(lat: Double, lon: Double): WeatherSnapshot {
    val url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max&forecast_days=1&timezone=auto"
    val conn = URL(url).openConnection() as HttpURLConnection
    try {
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.setRequestProperty("Accept", "application/json")
        if (conn.responseCode !in 200..299) error("Weather service unavailable")
        val root = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        val current = root.getJSONObject("current")
        val daily = root.getJSONObject("daily")
        val code = current.getInt("weather_code")
        val label = when (code) {
            0 -> "صحو"; 1, 2 -> "غائم جزئيًا"; 3 -> "غائم"; 45, 48 -> "ضباب"
            in 51..67 -> "رذاذ أو أمطار"; in 71..77 -> "ثلوج"; in 80..82 -> "زخات مطر"
            in 85..86 -> "زخات ثلجية"; in 95..99 -> "عواصف رعدية"; else -> "حالة جوية متغيرة"
        }
        return WeatherSnapshot(current.getDouble("temperature_2m"), current.getDouble("apparent_temperature"),
            current.getInt("relative_humidity_2m"), current.getDouble("wind_speed_10m"),
            daily.getJSONArray("precipitation_probability_max").optInt(0),
            daily.getJSONArray("temperature_2m_max").getDouble(0), daily.getJSONArray("temperature_2m_min").getDouble(0), label)
    } finally { conn.disconnect() }
}

@Composable
fun WeatherFeatureScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val lat by viewModel.latitude.collectAsState()
    val lon by viewModel.longitude.collectAsState()
    val city by viewModel.locationName.collectAsState()
    var data by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var stamp by remember { mutableStateOf<String?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(lat, lon, refreshTick) {
        val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
        val locationKey = lat.toString() + "," + lon.toString()
        data = null
        stamp = null
        if (prefs.getString("location_key", null) == locationKey) {
            data = prefs.getString("payload", null)?.let { runCatching { WeatherSnapshot.parse(JSONObject(it)) }.getOrNull() }
            stamp = prefs.getString("updated_at", null)
        }
        busy = true
        error = null
        try {
            val result = withContext(Dispatchers.IO) { loadWeather(lat, lon) }
            data = result
            stamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(java.util.Date())
            prefs.edit().putString("payload", result.json().toString()).putString("updated_at", stamp).putString("location_key", locationKey).apply()
        } catch (_: Exception) { error = "تعذر تحديث بيانات الطقس؛ تحقق من الإنترنت." }
        finally { busy = false }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FeatureIntroCard(Icons.Filled.Cloud, "الطقس", city) }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (busy && data == null) CircularProgressIndicator()
                    if (data != null) {
                        Text(data!!.condition, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(data!!.temperature.toInt().toString() + "°", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        Text("المحسوسة " + data!!.feels.toInt() + "°")
                        Text("العظمى " + data!!.high.toInt() + "°  •  الصغرى " + data!!.low.toInt() + "°")
                        Text("الرطوبة " + data!!.humidity + "%  •  الرياح " + data!!.wind.toInt() + " كم/س")
                        Text("احتمال المطر " + data!!.rain + "%")
                    } else if (!busy) Text(error ?: "لا تتوفر بيانات الطقس")
                    if (stamp != null) Text("آخر تحديث: " + stamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (error != null && data != null) Text("تعذر التحديث؛ المعروض آخر بيانات محفوظة.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        item {
            OutlinedButton(onClick = { refreshTick++ }, modifier = Modifier.fillMaxWidth(), enabled = !busy) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("تحديث الطقس")
            }
            Text("مصدر البيانات: Open-Meteo. التوقعات تقريبية، وقد تختلف عن الحالة الفعلية.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun IslamicEventsFeatureScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val zoneId by viewModel.timeZoneId.collectAsState()
    val zone = remember(zoneId) { runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault()) }
    val prefs = remember { context.getSharedPreferences("islamic_calendar", Context.MODE_PRIVATE) }
    var adjustment by remember { mutableIntStateOf(prefs.getInt("adjustment", 0).coerceIn(-2, 2)) }
    val today = java.time.ZonedDateTime.now(zone).toLocalDate().plusDays(adjustment.toLong())
    val hijri = remember(today) { IslamicCalendar.hijriDate(today) }
    val hijriYear = hijri.get(java.time.temporal.ChronoField.YEAR_OF_ERA)
    val hijriMonth = hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
    val hijriDay = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    val isRamadan = hijriMonth == 9
    val ramadanYear = if (hijriMonth > 9) hijriYear + 1 else hijriYear
    val ramadanLength = remember(ramadanYear) { IslamicCalendar.ramadanLength(ramadanYear) }
    val fastKey = "fasted_days_$ramadanYear"
    var fastedDays by remember(ramadanYear) {
        mutableStateOf(prefs.getStringSet(fastKey, emptySet())?.toSet() ?: emptySet())
    }
    val juzKey = "quran_juz_$ramadanYear"
    var completedJuz by remember(ramadanYear) { mutableIntStateOf(prefs.getInt(juzKey, 0).coerceIn(0, 30)) }
    val events = remember(today) { IslamicCalendar.upcomingEvents(today) }
    val monthName = IslamicCalendar.monthNames[hijriMonth - 1]
    val seasonTitle: String
    val seasonBody: String
    when {
        hijriMonth == 9 -> {
            seasonTitle = "موسم رمضان"
            seasonBody = "أيام الصيام والقيام وتلاوة القرآن. حافظ على السحور، وتعجيل الفطر، والصلوات، والصدقة، واجعل لك وردًا يوميًا من القرآن."
        }
        hijriMonth == 10 && hijriDay <= 3 -> {
            seasonTitle = "عيد الفطر المبارك"
            seasonBody = "من أعمال العيد: التكبير، وصلاة العيد، وإظهار الفرح المباح، وصلة الرحم. تذكّر إخراج زكاة الفطر قبل صلاة العيد."
        }
        hijriMonth == 12 && hijriDay <= 13 -> {
            seasonTitle = "العشر من ذي الحجة وعيد الأضحى"
            seasonBody = "أكثر من التكبير والذكر والعمل الصالح. يوم عرفة لغير الحاج يوم صيام عظيم، ثم يأتي عيد الأضحى وأيام التشريق."
        }
        hijriMonth == 1 && hijriDay <= 10 -> {
            seasonTitle = "المحرم وعاشوراء"
            seasonBody = "شهر الله المحرم من الأشهر الحرم، ويُستحب صيام عاشوراء مع يوم قبله أو بعده."
        }
        else -> {
            seasonTitle = "المناسبات الإسلامية"
            seasonBody = "تابع المناسبات القادمة واستعد لها بالعبادة والعمل الصالح، مع مراعاة إعلان رؤية الهلال في بلدك."
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FeatureIntroCard(Icons.Filled.CalendarMonth, "رمضان والأعياد", "قسم دائم للمناسبات والتقويم والمتابعة الموسمية") }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("التقويم الهجري", color = featureGold, fontWeight = FontWeight.Bold)
                    Text("$hijriDay $monthName $hijriYear هـ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(today.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("ar"))), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("ضبط التاريخ")
                        Text(if (adjustment > 0) "+$adjustment يوم" else "$adjustment يوم", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Slider(value = adjustment.toFloat(), onValueChange = {
                        adjustment = it.toInt()
                        prefs.edit().putInt("adjustment", adjustment).apply()
                    }, valueRange = -2f..2f, steps = 3)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("تقويم أم القرى الحسابي", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = {
                            adjustment = 0
                            prefs.edit().putInt("adjustment", 0).apply()
                        }, enabled = adjustment != 0) { Text("إعادة الضبط") }
                    }
                    Text("يمكن تقديم التاريخ أو تأخيره يومين لمطابقة إعلان بلدك ورؤية الهلال. يبقى التاريخ المعروض حسابيًا وليس إعلانًا رسميًا.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                }
            }
        }
        item { Text("المناسبات القادمة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        items(events.size) { index ->
            val event = events[index]
            val remaining = ChronoUnit.DAYS.between(today, event.gregorianDate)
            EventDateCard(event.title, event.gregorianDate, remaining, event.hijriLabel, event.note)
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(seasonTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text(seasonBody, fontSize = 13.sp, lineHeight = 21.sp)
                    when {
                        isRamadan -> Text("اليوم $hijriDay من رمضان — تقبّل الله صيامكم وقيامكم.", color = featureGold, fontWeight = FontWeight.SemiBold)
                        hijriMonth == 10 && hijriDay <= 3 -> Text("تقبّل الله الطاعات وكل عام وأنتم بخير.", color = featureGold, fontWeight = FontWeight.SemiBold)
                        hijriMonth == 12 && hijriDay in 8..13 -> Text("أيام الحج والعيد؛ أكثر من الذكر والتكبير.", color = featureGold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("متابعة الصيام", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text("سجّل الأيام التي صمتها في رمضان $ramadanYear هـ. عدد الأيام المسجلة: ${fastedDays.size} من $ramadanLength.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    (1..ramadanLength).toList().chunked(6).forEach { rowDays ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            rowDays.forEach { day ->
                                val key = day.toString()
                                val checked = key in fastedDays
                                FilterChip(
                                    selected = checked,
                                    onClick = {
                                        val updated = if (checked) fastedDays - key else fastedDays + key
                                        fastedDays = updated
                                        prefs.edit().putStringSet(fastKey, updated).apply()
                                    },
                                    enabled = isRamadan && day <= hijriDay,
                                    label = { Text(day.toString(), fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(6 - rowDays.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    if (!isRamadan) Text("تفعيل تسجيل أيام الصيام متاح خلال شهر رمضان، ويمكنك الاطلاع على سجل رمضان السابق عند دخول الشهر.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("متابعة ختم القرآن", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text("الأجزاء المكتملة في خطة رمضان $ramadanYear هـ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$completedJuz من 30 جزءًا", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = featureGold)
                    LinearProgressIndicator(progress = { completedJuz / 30f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = featureGold)
                    Slider(value = completedJuz.toFloat(), onValueChange = {
                        completedJuz = it.toInt()
                        prefs.edit().putInt(juzKey, completedJuz).apply()
                    }, valueRange = 0f..30f, steps = 29)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = {
                            completedJuz = 0
                            prefs.edit().putInt(juzKey, 0).apply()
                        }, enabled = completedJuz > 0) { Text("تصفير المتابعة") }
                        Text("الهدف: ختمة واحدة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Text("ملاحظة: التواريخ الهجرية المحسوبة قد تختلف عن إعلان بلدك بيوم أو يومين، خصوصًا بدايات رمضان والعيدين. اعتمد الإعلان الرسمي للصيام والعيد.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp) }
    }
}

@Composable
private fun EventDateCard(title: String, date: LocalDate, remaining: Long, hijriLabel: String, note: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(hijriLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (remaining == 0L) "اليوم" else "متبقي $remaining يومًا • " + date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("ar"))), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AppUpdateFeatureScreen(onCheckForUpdate: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FeatureIntroCard(Icons.Filled.SystemUpdate, "التحديث", "معلومات إصدار تطبيق أقم صلاتك") }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الإصدار المثبت", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(BuildConfig.VERSION_NAME, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("رقم البناء: " + BuildConfig.VERSION_CODE, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("يتحقق التطبيق تلقائيًا عند فتحه. يمكنك أيضًا إجراء تحقق يدوي.")
                    Button(onClick = onCheckForUpdate, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Refresh, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("التحقق من وجود تحديث")
                    }
                }
            }
        }
        item { FeatureInfoCard("التثبيت الآمن", "عند توفر تحديث، يعرض التطبيق تفاصيله ثم ينزّل APK ويفتح مثبت Android. لا يتم التثبيت دون موافقتك، ويُرفض الملف إذا لم يطابق توقيع التطبيق.") }
    }
}

@Composable
private fun FeatureIntroCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = featureGold, modifier = Modifier.size(34.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = androidx.compose.ui.graphics.Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FeatureInfoCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(body, fontSize = 13.sp, lineHeight = 21.sp)
        }
    }
}
