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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoField
import java.time.chrono.HijrahDate
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

private fun nextHijriDate(start: LocalDate, month: Int, day: Int): LocalDate =
    (0L..400L).map { start.plusDays(it) }.firstOrNull {
        val h = HijrahDate.from(it); h.get(ChronoField.MONTH_OF_YEAR) == month && h.get(ChronoField.DAY_OF_MONTH) == day
    } ?: start.plusDays(400)

@Composable
fun IslamicEventsFeatureScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val zoneId by viewModel.timeZoneId.collectAsState()
    val zone = remember(zoneId) { runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault()) }
    var adjustment by remember { mutableIntStateOf(context.getSharedPreferences("islamic_calendar", Context.MODE_PRIVATE).getInt("adjustment", 0)) }
    val today = java.time.ZonedDateTime.now(zone).toLocalDate().plusDays(adjustment.toLong())
    val hijri = remember(today) { HijrahDate.from(today) }
    val seasonalMessage = when {
        hijri.get(ChronoField.MONTH_OF_YEAR) == 9 -> "نحن في شهر رمضان المبارك"
        hijri.get(ChronoField.MONTH_OF_YEAR) == 10 && hijri.get(ChronoField.DAY_OF_MONTH) <= 3 -> "أيام عيد الفطر المبارك"
        hijri.get(ChronoField.MONTH_OF_YEAR) == 12 && hijri.get(ChronoField.DAY_OF_MONTH) in 8..13 -> "أيام الحج وعيد الأضحى"
        else -> "تابع المناسبات الإسلامية القادمة"
    }
    val ramadan = remember(today) { nextHijriDate(today, 9, 1) }
    val fitr = remember(today) { nextHijriDate(today, 10, 1) }
    val arafah = remember(today) { nextHijriDate(today, 12, 9) }
    val adha = remember(today) { nextHijriDate(today, 12, 10) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FeatureIntroCard(Icons.Filled.CalendarMonth, "رمضان والأعياد", seasonalMessage) }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("التاريخ الهجري التقريبي", color = featureGold, fontWeight = FontWeight.Bold)
                    Text(hijri.get(ChronoField.DAY_OF_MONTH).toString() + " " + listOf("محرم","صفر","ربيع الأول","ربيع الآخر","جمادى الأولى","جمادى الآخرة","رجب","شعبان","رمضان","شوال","ذو القعدة","ذو الحجة")[hijri.get(ChronoField.MONTH_OF_YEAR) - 1] + " " + hijri.get(ChronoField.YEAR_OF_ERA).toString() + " هـ", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("ضبط التاريخ: " + adjustment + " يوم")
                    Slider(value = adjustment.toFloat(), onValueChange = {
                        adjustment = it.toInt()
                        context.getSharedPreferences("islamic_calendar", Context.MODE_PRIVATE).edit().putInt("adjustment", adjustment).apply()
                    }, valueRange = -2f..2f, steps = 3)
                    Text("يمكن ضبط التاريخ من يومين قبلًا أو بعدًا ليتوافق مع إعلان بلدك ورؤية الهلال.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { EventDateCard("رمضان القادم", ramadan, today) }
        item { EventDateCard("عيد الفطر", fitr, today) }
        item { EventDateCard("يوم عرفة", arafah, today) }
        item { EventDateCard("عيد الأضحى", adha, today) }
        item {
            FeatureInfoCard("رمضان", "أحكام الصيام ومبطلاته، دعاء الإفطار، قيام الليل، العشر الأواخر، تحري ليلة القدر، زكاة الفطر، ومتابعة ختم القرآن.")
            FeatureInfoCard("عيد الفطر", "صلاة العيد، التكبير، الفطر قبل الصلاة، وزكاة الفطر وأحكامها.")
            FeatureInfoCard("عيد الأضحى", "صلاة العيد، الأضحية، أيام التشريق، والتكبير المطلق والمقيد.")
            FeatureInfoCard("مناسبات أخرى", "العشر الأوائل من ذي الحجة، يوم عرفة، وعاشوراء.")
        }
        item { Text("التواريخ حسابية وقد تختلف عن الإعلان الرسمي ورؤية الهلال في بلدك بيوم أو يومين.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun EventDateCard(title: String, date: LocalDate, today: LocalDate) {
    val days = ChronoUnit.DAYS.between(today, date)
    FeatureInfoCard(title, if (days == 0L) "اليوم" else "متبقي " + days + " يومًا • " + date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale("ar"))))
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
