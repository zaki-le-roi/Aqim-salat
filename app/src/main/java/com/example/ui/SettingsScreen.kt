package com.example.ui

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.core.content.ContextCompat
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    lang: String,
    modifier: Modifier = Modifier,
    initialDialog: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val madhabVal by viewModel.madhab.collectAsState()
    val methodVal by viewModel.calcMethod.collectAsState()
    val locationVal by viewModel.locationName.collectAsState()
    val notificationsVal by viewModel.notificationsEnabled.collectAsState()
    val preAlertEnabled by viewModel.prePrayerAlertEnabled.collectAsState()
    val preAlertMinutes by viewModel.prePrayerAlertMinutes.collectAsState()
    val autoSilentEnabled by viewModel.autoSilentEnabled.collectAsState()
    val activeThemeVal by viewModel.themeMode.collectAsState()
    val defaultReciterVal by viewModel.defaultReciter.collectAsState()
    val defaultMuadhinVal by viewModel.defaultMuadhin.collectAsState()
    val hapticsVal by viewModel.hapticsEnabled.collectAsState()
    val fontScaleVal by viewModel.fontScale.collectAsState()

    var floatingAdhkarEnabled by remember { mutableStateOf(context.getSharedPreferences("floating_adhkar", android.content.Context.MODE_PRIVATE).getBoolean("enabled", false)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val prefs = context.getSharedPreferences("floating_adhkar", android.content.Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("enabled", false)
                floatingAdhkarEnabled = enabled
                if (enabled && AndroidSettings.canDrawOverlays(context)) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, com.example.AdhkarOverlayService::class.java).setAction(com.example.AdhkarOverlayService.ACTION_SHOW)
                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Dialog state controllers
    var activeDialog by remember(initialDialog) { mutableStateOf<String?>(initialDialog) } // Dialog opened from the global app menu

    // Supported prayer-time and theme choices
    val themes = listOf(
        "AUTO" to if (lang == "ar") "تلقائي حسب الجهاز" else "Follow device",
        "LIGHT" to if (lang == "ar") "فاتح" else "Light",
        "DARK" to if (lang == "ar") "داكن" else "Dark",
        "AMOLED" to "أسود عميق موفّر للطاقة",
        "SUNRISE_SUNSET" to if (lang == "ar") "حسب وقت الشروق والغروب" else "Sunrise and sunset",
        "PRAYER_ADAPTIVE" to "ألوان تتغير مع الصلاة القادمة"
    )
    val methods = listOf("ALGERIA", "UMM_AL_QURA", "EGYPT", "MWL", "KARACHI", "ISNA", "TURKEY", "AWQAF")
    val madhabs = listOf("STANDARD", "HANAFI")
    val recitersList = listOf("مشاري العفاسي", "عبد الباسط عبد الصمد", "سعد الغامدي")
    val muadhinOptions = listOf(
        "MAKKAH" to "أذان مكة المكرمة",
        "MADINAH" to "أذان المدينة المنورة",
        "ALAFASY" to "أذان مشاري العفاسي",
        "FAJR_SPECIAL" to "أذان الفجر مع الدعاء"
    )
    val selectedMuadhinLabel = muadhinOptions.firstOrNull { it.first == defaultMuadhinVal }?.second ?: muadhinOptions.first().second

    // Accessibility state
    var fontSizeZoom by remember(fontScaleVal) { mutableFloatStateOf(fontScaleVal) }
    var hapticsEnabled by remember(hapticsVal) { mutableStateOf(hapticsVal) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = Translations.get("settings", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 1. Theme Configuration
        item {
            SettingsItemCard(
                icon = Icons.Filled.Palette,
                title = if (lang == "ar") "سمة التطبيق" else "Theme Configuration",
                subtitle = if (lang == "ar") "الوضع الحالي: " + when(activeThemeVal) { "LIGHT" -> "فاتح"; "DARK" -> "داكن"; "AMOLED" -> "أسود عميق"; else -> "تلقائي" } else "Current: $activeThemeVal",
                tag = "setting_theme",
                onClick = { activeDialog = "THEME" }
            )
        }



        // Floating adhkar overlay (shown over other apps only after explicit permission).
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("setting_floating_adhkar"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "الأذكار العائمة", tint = Color(0xFFD4AF37), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("الأذكار العائمة", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                if (floatingAdhkarEnabled) "تظهر فوق التطبيقات الأخرى" else "ذكر ظاهر على شاشة الهاتف مع إمكانية تغييره",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                    Switch(
                        checked = floatingAdhkarEnabled,
                        onCheckedChange = { enabled ->
                            val prefs = context.getSharedPreferences("floating_adhkar", android.content.Context.MODE_PRIVATE)
                            if (enabled && !AndroidSettings.canDrawOverlays(context)) {
                                // Persist the user's choice before opening Android settings.
                                // ON_RESUME will start the overlay as soon as permission is granted.
                                prefs.edit().putBoolean("enabled", true).commit()
                                floatingAdhkarEnabled = true
                                Toast.makeText(context, "اسمح بالظهور فوق التطبيقات الأخرى، ثم ارجع إلى التطبيق.", Toast.LENGTH_LONG).show()
                                context.startActivity(
                                    Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            } else {
                                floatingAdhkarEnabled = enabled
                                prefs.edit().putBoolean("enabled", enabled).commit()
                                val serviceIntent = Intent(context, com.example.AdhkarOverlayService::class.java).apply {
                                    action = if (enabled) com.example.AdhkarOverlayService.ACTION_SHOW else com.example.AdhkarOverlayService.ACTION_STOP
                                }
                                if (enabled) ContextCompat.startForegroundService(context, serviceIntent)
                                else context.startService(serviceIntent)
                            }
                        }
                    )
                }
            }
        }

        // Floating adhkar appearance and schedule controls.
        item {
            val prefs = context.getSharedPreferences("floating_adhkar", android.content.Context.MODE_PRIVATE)
            var overlayOpacity by remember { mutableFloatStateOf(prefs.getFloat("opacity", 0.94f)) }
            var overlayScale by remember { mutableFloatStateOf(prefs.getFloat("scale", 1.0f)) }
            var autoRotateAdhkar by remember { mutableStateOf(prefs.getBoolean("auto_rotate", true)) }
            var adhkarInterval by remember { mutableIntStateOf(prefs.getInt("interval_minutes", 30)) }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("setting_floating_adhkar_controls"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تخصيص الأذكار العائمة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text("بطاقة محادثة رفيعة 💬؛ اضغط عليها لإظهار الذكر ومصدره. اسحبها من أعلاها لنقلها.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("شفافية البطاقة", fontSize = 14.sp)
                        Text((overlayOpacity * 100).toInt().toString() + "%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = overlayOpacity,
                        onValueChange = {
                            overlayOpacity = it
                            prefs.edit().putFloat("opacity", it).apply()
                            if (floatingAdhkarEnabled) ContextCompat.startForegroundService(context, Intent(context, com.example.AdhkarOverlayService::class.java).setAction(com.example.AdhkarOverlayService.ACTION_REFRESH))
                        },
                        valueRange = 0.55f..1.0f
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("حجم البطاقة", fontSize = 14.sp)
                        Text((overlayScale * 100).toInt().toString() + "%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = overlayScale,
                        onValueChange = {
                            overlayScale = it
                            prefs.edit().putFloat("scale", it).apply()
                            if (floatingAdhkarEnabled) ContextCompat.startForegroundService(context, Intent(context, com.example.AdhkarOverlayService::class.java).setAction(com.example.AdhkarOverlayService.ACTION_REFRESH))
                        },
                        valueRange = 0.8f..1.25f
                    )
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تغيير الذكر تلقائيًا", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("أذكار الصباح والمساء والنوم والتسبيح بحسب الوقت.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        }
                        Switch(
                            checked = autoRotateAdhkar,
                            onCheckedChange = {
                                autoRotateAdhkar = it
                                prefs.edit().putBoolean("auto_rotate", it).apply()
                                if (floatingAdhkarEnabled) ContextCompat.startForegroundService(context, Intent(context, com.example.AdhkarOverlayService::class.java).setAction(com.example.AdhkarOverlayService.ACTION_REFRESH))
                            }
                        )
                    }
                    if (autoRotateAdhkar) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("الفاصل بين الأذكار", fontSize = 14.sp)
                            Text(adhkarInterval.toString() + " دقيقة", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = adhkarInterval.toFloat(),
                            onValueChange = {
                                val minutes = (((it / 5f) + 0.5f).toInt() * 5).coerceIn(5, 60)
                                adhkarInterval = minutes
                                prefs.edit().putInt("interval_minutes", minutes).commit()
                            },
                            onValueChangeFinished = {
                                if (floatingAdhkarEnabled) ContextCompat.startForegroundService(context, Intent(context, com.example.AdhkarOverlayService::class.java).setAction(com.example.AdhkarOverlayService.ACTION_REFRESH))
                            },
                            valueRange = 5f..60f,
                            steps = 10
                        )
                    }
                    Text("مصدر كل ذكر يظهر أسفله، مع الاعتماد على الأحاديث الصحيحة أو الحسنة الموثقة.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }

        // 3. Notifications Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("setting_notifications"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFD4AF37).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = if (lang == "ar") "التنبيهات" else "Alerts", tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (lang == "ar") "التنبيهات والأذان" else "Notifications & Alerts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (lang == "ar") { if (notificationsVal) "تشغيل أذان التذكير مفعّل" else "التذكيرات مكتومة" } else { if (notificationsVal) "Athan reminders enabled" else "Reminders muted" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Switch(
                        checked = notificationsVal,
                        onCheckedChange = { viewModel.toggleNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFD4AF37),
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().testTag("setting_pre_prayer_alert"), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (lang == "ar") "التنبيه قبل الأذان" else "Pre-prayer reminder", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(if (lang == "ar") { if (preAlertEnabled) "سيظهر تذكير قبل الصلاة بـ $preAlertMinutes دقيقة" else "تذكير اختياري للاستعداد للصلاة" } else { if (preAlertEnabled) "A reminder will appear $preAlertMinutes minutes before prayer" else "Optional reminder to prepare for prayer" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        }
                        Switch(checked = preAlertEnabled, onCheckedChange = { viewModel.setPrePrayerAlert(it, preAlertMinutes) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(selected = preAlertMinutes == 10, onClick = { viewModel.setPrePrayerAlert(preAlertEnabled, 10) }, label = { Text(if (lang == "ar") "10 دقائق" else "10 minutes") }, enabled = preAlertEnabled)
                        FilterChip(selected = preAlertMinutes == 15, onClick = { viewModel.setPrePrayerAlert(preAlertEnabled, 15) }, label = { Text(if (lang == "ar") "15 دقيقة" else "15 minutes") }, enabled = preAlertEnabled)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("setting_auto_silent"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.VolumeOff,
                                contentDescription = if (lang == "ar") "الوضع الصامت التلقائي" else "Automatic silent mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (lang == "ar") "الوضع الصامت وقت الصلاة" else "Silent mode at prayer",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    if (lang == "ar") {
                                        if (autoSilentEnabled) "يكتم الرنين 10 دقائق ثم يعيد وضعه السابق" else "اختياري؛ يحتاج صلاحية عدم الإزعاج"
                                    } else {
                                        if (autoSilentEnabled) "Mutes ringing for 10 minutes, then restores the previous mode" else "Optional; requires Do Not Disturb access"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }
                        Switch(
                            checked = autoSilentEnabled,
                            onCheckedChange = { requested ->
                                val accepted = viewModel.setAutoSilentEnabled(requested)
                                if (requested && !accepted) {
                                    Toast.makeText(
                                        context,
                                        if (lang == "ar") "امنح التطبيق صلاحية عدم الإزعاج، ثم فعّل الخيار مرة أخرى" else "Grant Do Not Disturb access, then enable this option again",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // 4. Prayer Calculation
        item {
            SettingsItemCard(
                icon = Icons.Filled.SettingsInputComponent,
                title = if (lang == "ar") "حساب المواقيت والمذهب" else "Prayer Calculation",
                subtitle = if (lang == "ar") "الطريقة: " + when (methodVal) { "ALGERIA" -> "الجزائر"; "UMM_AL_QURA" -> "أم القرى"; "EGYPT" -> "الهيئة المصرية للمساحة"; "MWL" -> "رابطة العالم الإسلامي"; "KARACHI" -> "جامعة العلوم الإسلامية بكراتشي"; "ISNA" -> "الجمعية الإسلامية لأمريكا الشمالية"; "TURKEY" -> "الشؤون الدينية التركية"; "AWQAF" -> "الأوقاف"; else -> methodVal } + " • المذهب: " + if (madhabVal == "HANAFI") "حنفي" else "الشافعي والمالكي والحنبلي" else "Method: $methodVal • Madhab: $madhabVal",
                tag = "setting_calculation",
                onClick = { activeDialog = "CALC" }
            )
        }

        // Muadhin selector, separate from Quran recitation.
        item {
            SettingsItemCard(
                icon = Icons.Filled.RecordVoiceOver,
                title = if (lang == "ar") "اختيار المؤذن" else "Choose Muadhin",
                subtitle = if (lang == "ar") "تخصيص صوت مستقل لكل صلاة" else "Set a separate voice for each prayer",
                tag = "setting_muadhin",
                onClick = { activeDialog = "MUADHIN" }
            )
        }

        // 5. Audio Playback (Default Reciter stream)
        item {
            SettingsItemCard(
                icon = Icons.Filled.RecordVoiceOver,
                title = if (lang == "ar") "قارئ القرآن الافتراضي" else "Audio Settings",
                subtitle = if (lang == "ar") "اختر إعدادات الاستماع إلى التلاوة" else "Choose recitation audio preferences",
                tag = "setting_audio",
                onClick = { activeDialog = "AUDIO" }
            )
        }

        // 6. Data Backup & Cloud Sync
        item {
            SettingsItemCard(
                icon = Icons.Filled.Backup,
                title = if (lang == "ar") "النسخ الاحتياطي والمزامنة" else "Backup & Sync",
                subtitle = if (lang == "ar") "حماية الإحصاءات والسجلات والمحفوظات" else "Secure local statistics, logs & bookmarks",
                tag = "setting_backup",
                onClick = { activeDialog = "BACKUP" }
            )
        }

        // 8. Privacy & Device Permissions
        item {
            SettingsItemCard(
                icon = Icons.Filled.Security,
                title = if (lang == "ar") "الخصوصية والأذونات" else "Privacy & Security",
                subtitle = if (lang == "ar") "أذونات الموقع وشروط الاستخدام" else "Location permissions and terms",
                tag = "setting_privacy",
                onClick = { activeDialog = "PRIVACY" }
            )
        }

        // 9. Accessibility
        item {
            SettingsItemCard(
                icon = Icons.Filled.Accessibility,
                title = if (lang == "ar") "سهولة الاستخدام" else "Accessibility",
                subtitle = if (lang == "ar") "تخصيص حجم الخط والاهتزاز والتكبير" else "Customize font size, haptics and zoom",
                tag = "setting_accessibility",
                onClick = { activeDialog = "ACCESSIBILITY" }
            )
        }
    }

    // --- Interactive Dialogue Handlers ---

    // A. Theme Selector Dialog
    if (activeDialog == "THEME") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "اختر سمة التطبيق" else "Select Theme Mode", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { (themeCode, themeLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(themeCode)
                                    activeDialog = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = themeCode == activeThemeVal, onClick = {
                                viewModel.setThemeMode(themeCode)
                                activeDialog = null
                            })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(themeLabel, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }



    // C. Calculation Method & Madhab Settings Dialog
    if (activeDialog == "CALC") {
        var currentMadhab by remember { mutableStateOf(madhabVal) }
        var currentMethod by remember { mutableStateOf(methodVal) }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "معايير حساب الصلاة" else "Calculation Criteria", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text(if (lang == "ar") "الهيئة الفلكية" else "Astronomical Authority", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD4AF37))
                    }
                    items(methods.size) { i ->
                        val m = methods[i]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentMethod = m }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = m == currentMethod, onClick = { currentMethod = m })
                            Spacer(modifier = Modifier.width(12.dp))
                            val methodLabel = when (m) {
                                "ALGERIA" -> if (lang == "ar") "وزارة الشؤون الدينية والأوقاف (الجزائر)" else "Algerian Ministry of Religious Affairs & Wakfs"
                                "UMM_AL_QURA" -> if (lang == "ar") "جامعة أم القرى (مكة المكرمة)" else "Umm Al-Qura University, Makkah"
                                "EGYPT" -> if (lang == "ar") "الهيئة المصرية العامة للمساحة" else "Egyptian General Authority of Survey"
                                "MWL" -> if (lang == "ar") "رابطة العالم الإسلامي" else "Muslim World League"
                                "KARACHI" -> if (lang == "ar") "جامعة العلوم الإسلامية بكراتشي" else "University of Islamic Sciences, Karachi"
                                "ISNA" -> if (lang == "ar") "الجمعية الإسلامية لأمريكا الشمالية (ISNA)" else "Islamic Society of North America (ISNA)"
                                "TURKEY" -> if (lang == "ar") "رئاسة الشؤون الدينية التركية (Diyanet)" else "Diyanet (Turkey)"
                                "AWQAF" -> if (lang == "ar") "وزارة الأوقاف والشؤون الإسلامية" else "Ministry of Awqaf (General)"
                                else -> m
                            }
                            Text(methodLabel, fontSize = 14.sp)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(if (lang == "ar") "المذهب الفقهي (العصر)" else "Asr Jurisprudential Madhab", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD4AF37))
                    }
                    items(madhabs.size) { i ->
                        val md = madhabs[i]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentMadhab = md }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = md == currentMadhab, onClick = { currentMadhab = md })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (lang == "ar") { if (md == "STANDARD") "الشافعي والمالكي والحنبلي" else "الحنفي" } else { if (md == "STANDARD") "Standard (Shafi'i, Maliki, Hanbali)" else "Hanafi" }, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCalculationMethod(currentMethod)
                        viewModel.setMadhab(currentMadhab)
                        activeDialog = null
                        Toast.makeText(context, if (lang == "ar") "تم حفظ إعدادات المواقيت بنجاح" else "Calculation rules saved successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (lang == "ar") "حفظ" else "Save Rules")
                }
            }
        )
    }

    // D. Per-prayer muadhin selector.
    if (activeDialog == "MUADHIN") {
        val prayerChoices = listOf(
            "Fajr" to "الفجر", "Dhuhr" to "الظهر", "Asr" to "العصر",
            "Maghrib" to "المغرب", "Isha" to "العشاء"
        )
        val audioPrefs = remember { context.getSharedPreferences("audio_preferences", android.content.Context.MODE_PRIVATE) }
        var selectedPrayer by remember { mutableStateOf("Fajr") }
        var selectedMuadhin by remember { mutableStateOf(audioPrefs.getString("muadhin_Fajr", defaultMuadhinVal) ?: defaultMuadhinVal) }
        var previewingCode by remember { mutableStateOf<String?>(null) }
        val previewPlayer = remember { arrayOfNulls<MediaPlayer>(1) }
        DisposableEffect(Unit) {
            onDispose {
                previewPlayer[0]?.runCatching { stop() }
                previewPlayer[0]?.release()
                previewPlayer[0] = null
            }
        }
        fun stopAdhanPreview() {
            previewPlayer[0]?.runCatching { stop() }
            previewPlayer[0]?.release()
            previewPlayer[0] = null
            previewingCode = null
        }
        fun previewAdhan(code: String) {
            stopAdhanPreview()
            val url = when (code) {
                "MADINAH" -> "https://www.islamcan.com/audio/adhan/azan2.mp3"
                "ALAFASY" -> "https://www.islamcan.com/audio/adhan/azan8.mp3"
                "FAJR_SPECIAL" -> "https://download.tvquran.com/download/selections/180/58b0dac02106f.mp3"
                else -> "https://www.islamcan.com/audio/adhan/azan1.mp3"
            }
            previewingCode = code
            runCatching {
                MediaPlayer().apply {
                    previewPlayer[0] = this
                    setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    setDataSource(url)
                    setOnPreparedListener { player -> if (previewPlayer[0] === player) player.start() }
                    setOnCompletionListener { player ->
                        player.release()
                        if (previewPlayer[0] === player) { previewPlayer[0] = null; previewingCode = null }
                    }
                    setOnErrorListener { player, _, _ ->
                        player.release()
                        if (previewPlayer[0] === player) { previewPlayer[0] = null; previewingCode = null }
                        Toast.makeText(context, if (lang == "ar") "تعذر تشغيل المعاينة. تحقق من الإنترنت." else "Preview failed. Check your internet connection.", Toast.LENGTH_LONG).show()
                        true
                    }
                    prepareAsync()
                }
            }.onFailure {
                stopAdhanPreview()
                Toast.makeText(context, if (lang == "ar") "تعذر تشغيل المعاينة. تحقق من الإنترنت." else "Preview failed. Check your internet connection.", Toast.LENGTH_LONG).show()
            }
        }
        AlertDialog(
            onDismissRequest = { stopAdhanPreview(); activeDialog = null },
            title = { Text(if (lang == "ar") "مؤذّن مستقل لكل صلاة" else "Choose a voice for each prayer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (lang == "ar") "اختر الصلاة أولًا، ثم اختر صوت المؤذن الخاص بها." else "Select a prayer, then its adhan voice.", fontSize = 13.sp)
                    prayerChoices.forEach { (key, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                stopAdhanPreview()
                                selectedPrayer = key
                                selectedMuadhin = audioPrefs.getString("muadhin_$key", audioPrefs.getString("default_muadhin", defaultMuadhinVal)) ?: defaultMuadhinVal
                            }.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedPrayer == key, onClick = {
                                stopAdhanPreview()
                                selectedPrayer = key
                                selectedMuadhin = audioPrefs.getString("muadhin_$key", audioPrefs.getString("default_muadhin", defaultMuadhinVal)) ?: defaultMuadhinVal
                            })
                            Text(label, fontSize = 14.sp)
                        }
                    }
                    Divider()
                    muadhinOptions.forEach { (code, label) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = code == selectedMuadhin, onClick = { selectedMuadhin = code })
                            Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f).clickable { selectedMuadhin = code })
                            TextButton(onClick = { if (previewingCode == code) stopAdhanPreview() else previewAdhan(code) }) {
                                Icon(imageVector = if (previewingCode == code) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(if (previewingCode == code) (if (lang == "ar") "إيقاف" else "Stop") else (if (lang == "ar") "استماع" else "Listen"))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    stopAdhanPreview()
                    audioPrefs.edit().putString("muadhin_$selectedPrayer", selectedMuadhin).apply()
                    activeDialog = null
                    val prayerLabel = prayerChoices.firstOrNull { it.first == selectedPrayer }?.second ?: selectedPrayer
                    val voiceLabel = muadhinOptions.firstOrNull { it.first == selectedMuadhin }?.second ?: selectedMuadhin
                    Toast.makeText(context, if (lang == "ar") "تم حفظ $voiceLabel لصلاة $prayerLabel" else "Saved $voiceLabel for $selectedPrayer", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text(if (lang == "ar") "حفظ هذه الصلاة" else "Save this prayer")
                }
            },
            dismissButton = { TextButton(onClick = { stopAdhanPreview(); activeDialog = null }) { Text(if (lang == "ar") "إغلاق" else "Close") } }
        )
    }

    // E. Audio Reciter Settings Dialog
    if (activeDialog == "AUDIO") {
        var selectedReciter by remember(defaultReciterVal) { mutableStateOf(defaultReciterVal) }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "إعدادات الصوت والمقرئ" else "Recitation Voice Preferences", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    recitersList.forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReciter = rec }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = rec == selectedReciter, onClick = { selectedReciter = rec })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(rec, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setDefaultReciter(selectedReciter)
                        activeDialog = null
                        Toast.makeText(context, if (lang == "ar") "تم حفظ المقرئ الافتراضي: $selectedReciter" else "Default reciter saved: $selectedReciter", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (lang == "ar") "موافق" else "OK")
                }
            }
        )
    }

    // E. Backup Dialog
    if (activeDialog == "BACKUP") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "النسخ الاحتياطي والمزامنة" else "Backup & Sync", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (lang == "ar") "الحالة: البيانات المحلية محفوظة على هذا الجهاز"
                        else "Status: Data is stored locally on this device",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        if (lang == "ar") "النسخ الاحتياطي السحابي غير مفعّل حاليًا. لا يدّعي التطبيق تنفيذ مزامنة خارج الجهاز."
                        else "Cloud backup is not currently connected. The app does not claim to synchronize data outside this device.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(if (lang == "ar") "إغلاق" else "Close")
                }
            }
        )
    }

    // G. Privacy Dialog
    if (activeDialog == "PRIVACY") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "شروط الخدمة والخصوصية" else "Privacy sandbox & Location", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(if (lang == "ar") "1. إحداثيات الجهاز" else "1. Device Coordinates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (lang == "ar") "يستخدم التطبيق موقع الجهاز لحساب المواقيت والقبلة والمسافة إلى المساجد. لا يستبدل موقع الجهاز بتحديد الموقع عبر عنوان IP، وقد تُرسل الإحداثيات إلى خدمات الخرائط العامة فقط عند طلب البحث عن المساجد القريبة. لا يرفع التطبيق الإحداثيات إلى خادم التطبيق الخاص به." else "The app uses your device location to calculate prayer times, Qibla direction, and mosque distances. It does not replace device location with IP geolocation. Nearby-mosque searches may send coordinates to the selected map service. The app does not upload your coordinates to its own application server.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(if (lang == "ar") "2. التشغيل المحلي" else "2. Offline Sandbox Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (lang == "ar") "عند إنشاء عضوية، يطلب التطبيق بيانات مثل الاسم والبريد الإلكتروني والبلد والمدينة لحساب العضوية. كما تستخدم بعض الخدمات اتصالًا بالإنترنت، وتُنفذ الحسابات الأساسية مثل مواقيت الصلاة على الجهاز." else "When you create a membership, the app requests details such as name, email, country, and city for the membership profile. Some services also use network connections, while core calculations such as prayer times run on the device.", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { activeDialog = null }) {
                    Text(if (lang == "ar") "فهمت" else "Acknowledge")
                }
            }
        )
    }

    // H. Accessibility Settings Dialog
    if (activeDialog == "ACCESSIBILITY") {
        var currentHaptics by remember { mutableStateOf(hapticsEnabled) }
        var zoomScale by remember { mutableFloatStateOf(fontSizeZoom) }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "تخصيص سهولة الاستخدام" else "Accessibility settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(if (lang == "ar") "تكبير الخط: ${"%.1f".format(zoomScale)}×" else "Text Size Multiplier: ${"%.1f".format(zoomScale)}x", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Slider(
                            value = zoomScale,
                            onValueChange = { zoomScale = it },
                            valueRange = 0.8f..1.6f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFD4AF37), activeTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (lang == "ar") "اهتزاز عند اللمس" else "Haptic Feedback on Tap", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(if (lang == "ar") "اهتزاز خفيف عند تسجيل الأعمال والضغط على السبحة" else "Gentle vibration when logging tasks & clicking tasbih beads", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = currentHaptics,
                            onCheckedChange = { currentHaptics = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD4AF37))
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        fontSizeZoom = zoomScale
                        viewModel.setFontScale(zoomScale)
                        hapticsEnabled = currentHaptics
                        viewModel.setHapticsEnabled(currentHaptics)
                        activeDialog = null
                        Toast.makeText(context, if (lang == "ar") "تم تطبيق إعدادات سهولة الاستخدام" else "Accessibility changes applied", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (lang == "ar") "تطبيق" else "Apply")
                }
            }
        )
    }
}

@Composable
fun SettingsItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFD4AF37).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = title, tint = Color(0xFFD4AF37))
        }
    }
}
