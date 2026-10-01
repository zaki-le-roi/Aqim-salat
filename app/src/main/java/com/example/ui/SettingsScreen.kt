package com.example.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    lang: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val madhabVal by viewModel.madhab.collectAsState()
    val methodVal by viewModel.calcMethod.collectAsState()
    val locationVal by viewModel.locationName.collectAsState()
    val notificationsVal by viewModel.notificationsEnabled.collectAsState()
    val activeThemeVal by viewModel.themeMode.collectAsState()
    val defaultReciterVal by viewModel.defaultReciter.collectAsState()

    // Dialog state controllers
    var activeDialog by remember { mutableStateOf<String?>(null) } // "THEME", "LANG", "CALC", "AUDIO", "DOWNLOADS", "BACKUP", "PRIVACY", "ACCESSIBILITY"

    // Supported lists
    val languages = listOf(
        Pair("ar", "العربية (Arabic)"),
        Pair("en", "English"),
        Pair("fr", "Français (French)"),
        Pair("tr", "Türkçe (Turkish)")
    )
    val themes = listOf("LIGHT", "DARK", "AMOLED", "AUTO")
    val methods = listOf("ALGERIA", "UMM_AL_QURA", "EGYPT", "MWL", "KARACHI", "ISNA", "TURKEY", "AWQAF")
    val madhabs = listOf("STANDARD", "HANAFI")
    val recitersList = listOf("Mishary Al-Afasy", "Abdul Basit Abdus Samad", "Saad Al-Ghamdi")

    // Accessibility state
    var fontSizeZoom by remember { mutableFloatStateOf(1.0f) }
    var hapticsEnabled by remember { mutableStateOf(true) }

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



        // 2. Language
        item {
            SettingsItemCard(
                icon = Icons.Filled.Language,
                title = Translations.get("language", lang),
                subtitle = languages.firstOrNull { it.first == lang }?.second ?: "العربية",
                tag = "setting_language",
                onClick = { activeDialog = "LANG" }
            )
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

        // 4. Prayer Calculation
        item {
            SettingsItemCard(
                icon = Icons.Filled.SettingsInputComponent,
                title = if (lang == "ar") "حساب المواقيت والمذهب" else "Prayer Calculation",
                subtitle = if (lang == "ar") "الطريقة: $methodVal • المذهب: $madhabVal" else "Method: $methodVal • Madhab: $madhabVal",
                tag = "setting_calculation",
                onClick = { activeDialog = "CALC" }
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

    // B. Language Selector
    if (activeDialog == "LANG") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "لغة التطبيق" else "App language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppLanguage(code)
                                    activeDialog = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = lang == code, onClick = {
                                viewModel.setAppLanguage(code)
                                activeDialog = null
                            })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) { Text(if (lang == "ar") "إغلاق" else "Close") }
            }
        )
    }

    // A. Theme Selector Dialog
    if (activeDialog == "THEME") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(if (lang == "ar") "اختر سمة التطبيق" else "Select Theme Mode", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { th ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(th)
                                    activeDialog = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = th == activeThemeVal, onClick = {
                                viewModel.setThemeMode(th)
                                activeDialog = null
                            })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(th, fontSize = 16.sp)
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
                            Text(if (md == "STANDARD") "Standard (Shafi'i, Maliki, Hanbali)" else "Hanafi", fontSize = 14.sp)
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

    // D. Audio Reciter Settings Dialog
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
                    Text(if (lang == "ar") "يستخدم التطبيق موقع الجهاز محليًا لحساب المواقيت والقبلة والمسافة إلى المساجد، ولا يرفع إحداثيات موقعك إلى خادم التطبيق." else "The application requests fine & coarse GPS locations locally to calculate astronomical prayer timings, compass bearings, and distance to mosques. Location coordinates are strictly private and NEVER uploaded.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(if (lang == "ar") "2. التشغيل المحلي" else "2. Offline Sandbox Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (lang == "ar") "لا يجمع التطبيق معلوماتك الشخصية لأغراض غير ضرورية، وتُنفذ الحسابات الأساسية على الجهاز." else "No personal information is harvested. The application values user security, strictly executing computations client-side.", fontSize = 12.sp, color = Color.Gray)
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
                        hapticsEnabled = currentHaptics
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
