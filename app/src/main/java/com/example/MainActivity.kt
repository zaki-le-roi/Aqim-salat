package com.example

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import com.example.adhan.AdhanScheduler
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val granted = it[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                it[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                locationViewModel?.startLocationTracking()
            }
        }

    private var locationViewModel: AppViewModel? = null
    private var apkUpdateChecker: ApkUpdateChecker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        locationViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        apkUpdateChecker = ApkUpdateChecker(this).also { it.checkForUpdate() }
        requestNotificationAndAlarmAccess()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            locationViewModel?.startLocationTracking()
        }
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            AdhanScheduler.schedule(applicationContext)
        }
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val activeLanguage = "ar" // التطبيق عربي فقط وباتجاه من اليمين إلى اليسار
            val activeThemeMode by appViewModel.themeMode.collectAsState()
            val nextPrayerName by appViewModel.nextPrayerName.collectAsState()
            val fontScale by appViewModel.fontScale.collectAsState()

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> appViewModel.startLocationTracking()
                        Lifecycle.Event.ON_STOP -> appViewModel.stopLocationTracking()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            MyApplicationTheme(themeMode = activeThemeMode, nextPrayer = nextPrayerName) {
                val layoutDirection = if (Translations.isRtl(activeLanguage)) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }

                val baseDensity = LocalDensity.current
                val scaledDensity = Density(
                    density = baseDensity.density,
                    fontScale = baseDensity.fontScale * fontScale
                )

                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection,
                    LocalDensity provides scaledDensity
                ) {
                    AqimSalahApp(viewModel = appViewModel, lang = activeLanguage)
                }
            }
        }
    }

    private fun requestNotificationAndAlarmAccess() {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Exact-alarm access is optional. AdhanScheduler uses an inexact idle-safe fallback
        // when Android has not granted exact-alarm access, so the app does not trap users in Settings.
    }

    override fun onResume() {
        super.onResume()
        apkUpdateChecker?.installPendingUpdateIfAllowed()
    }

    override fun onDestroy() {
        apkUpdateChecker?.dispose()
        apkUpdateChecker = null
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AqimSalahApp(
    viewModel: AppViewModel,
    lang: String
) {
    var selectedTab by remember { mutableStateOf("HOME") }
    var isQuranReaderOpen by remember { mutableStateOf(false) }
    
    // Dynamic detail screens controllers
    var activeDetailScreen by remember { mutableStateOf<String?>(null) }
    var initialToolParam by remember { mutableStateOf<String?>(null) }
    var initialSurahIdParam by remember { mutableStateOf<Int?>(null) }
    var initialSettingsDialog by remember { mutableStateOf<String?>(null) }

    // Dynamic routing callback across all dashboard grid cards
    val handleFeatureNavigation: (String) -> Unit = { routeKey ->
        when (routeKey) {
            "HOME" -> {
                selectedTab = "HOME"
                activeDetailScreen = null
            }
            "PRAYER_TIMES" -> {
                selectedTab = "PRAYER_TIMES"
                activeDetailScreen = null
            }
            "QURAN" -> {
                selectedTab = "QURAN"
                initialSurahIdParam = null
                activeDetailScreen = null
            }
            "ADHKAR" -> {
                selectedTab = "ADHKAR"
                activeDetailScreen = null
            }
            "QIBLA" -> {
                selectedTab = "QIBLA"
                activeDetailScreen = null
            }
            "SETTING_THEME", "SETTING_CALC", "SETTING_AUDIO", "SETTING_BACKUP", "SETTING_PRIVACY", "SETTING_ACCESSIBILITY" -> {
                initialSettingsDialog = when (routeKey) {
                    "SETTING_THEME" -> "THEME"
                    "SETTING_CALC" -> "CALC"
                    "SETTING_AUDIO" -> "AUDIO"
                    "SETTING_BACKUP" -> "BACKUP"
                    "SETTING_PRIVACY" -> "PRIVACY"
                    else -> "ACCESSIBILITY"
                }
                activeDetailScreen = "SETTINGS"
            }
            "TOOLS_MENU", "TOOLS" -> {
                activeDetailScreen = "TOOLS"
                initialToolParam = null
            }
            "TASBIH", "NAMES", "KHATMAH", "HISN_AL_MUSLIM" -> {
                activeDetailScreen = "TOOLS"
                initialToolParam = routeKey
            }
            "MOSQUES", "MOSQUE_LOCATOR" -> {
                activeDetailScreen = "TOOLS"
                initialToolParam = "MOSQUES"
            }
            "CALENDAR", "FAVORITES", "SEARCH", "QURAN_AUDIO", "HADITH", "SETTINGS", "SEERAH",
            "SCHOLARS_LIBRARY", "ZAD_ALMUSLIM", "MISHKAT" -> {
                activeDetailScreen = routeKey
            }
        }
    }

    androidx.activity.compose.BackHandler(enabled = activeDetailScreen != null) {
        activeDetailScreen = null
    }

    androidx.activity.compose.BackHandler(enabled = activeDetailScreen == null && selectedTab != "HOME") {
        selectedTab = "HOME"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Floating, softly translucent pill navigation with five primary destinations.
            if (activeDetailScreen == null && !(selectedTab == "QURAN" && isQuranReaderOpen)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("floating_navigation_container")
                ) {
                    NavigationBar(
                        containerColor = Color(0xFFFAF8F5),
                        tonalElevation = 0.dp,
                        windowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFAF8F5))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f), CircleShape)
                            .shadow(12.dp, CircleShape)
                            .testTag("app_navigation_bar")
                    ) {
                        val tabs = listOf(
                            Triple("HOME", if (lang == "ar") "الرئيسية" else "Home", Icons.Filled.Home),
                            Triple("QURAN", if (lang == "ar") "القرآن" else "Quran", Icons.Filled.MenuBook),
                            Triple("PRAYER_TIMES", if (lang == "ar") "المواقيت والقبلة" else "Prayer & Qibla", Icons.Filled.Explore),
                            Triple("ADHKAR", if (lang == "ar") "الأذكار" else "Adhkar", Icons.Filled.Spa),
                        )

                        tabs.forEach { (route, label, icon) ->
                            val isSelected = selectedTab == route || (route == "PRAYER_TIMES" && selectedTab == "QIBLA")
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    selectedTab = route
                                    activeDetailScreen = null
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = Color(0xFFD4AF37),
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontFamily = FontFamily.Serif,
                                         fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD4AF37)
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                     indicatorColor = Color(0xFF115E3F),
                                     selectedIconColor = Color(0xFFD4AF37),
                                     selectedTextColor = Color(0xFFD4AF37),
                                     unselectedIconColor = Color(0xFF115E3F),
                                     unselectedTextColor = Color(0xFF115E3F)
                                 ),
                                modifier = Modifier.testTag("nav_item_" + route)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val activeWallpaper by viewModel.wallpaper.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    if (activeDetailScreen == null && !(selectedTab == "QURAN" && isQuranReaderOpen)) innerPadding
                    else PaddingValues(0.dp)
                )
        ) {
            ThemeBackground(wallpaper = activeWallpaper)

            if (activeDetailScreen != null) {
                // Overlay detail views with pristine back navigation support
                when (activeDetailScreen) {
                    "CALENDAR" -> CalendarScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "FAVORITES" -> FavoritesScreen(
                        viewModel = viewModel, 
                        lang = lang, 
                        onNavigateToQuran = { surahId ->
                            selectedTab = "QURAN"
                            initialSurahIdParam = surahId
                            activeDetailScreen = null
                        },
                        onBack = { activeDetailScreen = null }
                    )
                    "SEARCH" -> SearchScreen(
                        viewModel = viewModel,
                        lang = lang,
                        onNavigateToQuran = { surahId ->
                            selectedTab = "QURAN"
                            initialSurahIdParam = surahId
                            activeDetailScreen = null
                        },
                        onNavigateToAdhkar = {
                            activeDetailScreen = "TOOLS"
                            initialToolParam = "ADHKAR"
                        },
                        onBack = { activeDetailScreen = null }
                    )
                    "QURAN_AUDIO" -> QuranAudioScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "HADITH" -> HadithScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "SETTINGS" -> Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(if (lang == "ar") "الإعدادات" else "Settings", fontWeight = FontWeight.Bold) },
                                navigationIcon = {
                                    IconButton(onClick = { activeDetailScreen = null }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                                    }
                                }
                            )
                        }
                    ) { p ->
                        SettingsScreen(viewModel = viewModel, lang = lang, modifier = Modifier.padding(p), initialDialog = initialSettingsDialog)
                    }
                    "SEERAH" -> SeerahScreen(lang = lang, onBack = { activeDetailScreen = null })
                    "SCHOLARS_LIBRARY" -> ScholarsLibraryScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "ZAD_ALMUSLIM" -> ZadAlMuslimScreen(onBack = { activeDetailScreen = null })
                    "MISHKAT" -> MishkatScreen(onBack = { activeDetailScreen = null })

                    "TOOLS" -> {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = when (initialToolParam) {
                                                "TASBIH" -> Translations.get("tasbih", lang)
                                                "ADHKAR" -> Translations.get("adhkar", lang)
                                                "NAMES" -> Translations.get("names_of_allah", lang)
                                                "MOSQUES" -> if (lang == "ar") "المساجد القريبة" else "Nearby Mosques"
                                                "KHATMAH" -> if (lang == "ar") "خاتمة القرآن" else "Ramadan Khatmah"
                                                "HISN_AL_MUSLIM" -> if (lang == "ar") "حصن المسلم" else "Hisn Al Muslim"
                                                else -> if (lang == "ar") "الخدمات الإسلامية" else "Islamic Services"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { activeDetailScreen = null }) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                                        }
                                    }
                                )
                            }
                        ) { p ->
                            Box(modifier = Modifier.padding(p)) {
                                when (initialToolParam) {
                                    "TASBIH" -> TasbihTool(viewModel, lang)
                                    "ADHKAR" -> AdhkarTool(viewModel, lang)
                                    "NAMES" -> NamesOfAllahTool(viewModel, lang)
                                    "MOSQUES" -> MosquesTool(viewModel, lang)
                                    "KHATMAH" -> KhatmahTool(viewModel, lang)
                                    "HISN_AL_MUSLIM" -> HisnAlMuslimTool(viewModel, lang)
                                    else -> ToolsScreen(viewModel = viewModel, lang = lang)
                                }
                            }
                        }
                    }
                }
            } else {
                // Render the primary bottom navigation screens
                when (selectedTab) {
                    "HOME" -> HomeScreen(
                        viewModel = viewModel, 
                        lang = lang, 
                        onNavigateToFeature = handleFeatureNavigation
                    )
                    "QURAN" -> QuranScreen(
                        viewModel = viewModel, 
                        lang = lang, 
                        initialSurahId = initialSurahIdParam,
                        onReadingModeChange = { isQuranReaderOpen = it }
                    )
                    "ADHKAR" -> AdhkarTool(viewModel, lang)
                    "PRAYER_TIMES" -> PrayerTimesScreen(
                        viewModel = viewModel, 
                        lang = lang
                    )
                    "QIBLA" -> Scaffold(
                        topBar = {
                            TopAppBar(title = { Text(Translations.get("qibla", lang), fontWeight = FontWeight.Bold) })
                        }
                    ) { p ->
                        Box(modifier = Modifier.padding(p)) {
                            QiblaTool(viewModel, lang)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun ThemeBackground(
    wallpaper: String,
    modifier: Modifier = Modifier,
    islamicPatternEnabled: Boolean = true
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = Color(0x1AD4AF37)

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (islamicPatternEnabled) {
            // Warm ivory base and a low-alpha repeating eight-point star lattice.
            drawRect(color = Color(0xFFFAF8F2))
            val spacing = 112.dp.toPx()
            val outerRadius = 15.dp.toPx()
            val innerRadius = 6.2.dp.toPx()
            val gold = Color(0xFFC5A059).copy(alpha = 0.055f)
            val beige = Color(0xFF9A8058).copy(alpha = 0.035f)
            val stroke = 0.75.dp.toPx()
            var row = -1
            var y = -spacing
            while (y <= height + spacing) {
                var col = -1
                var x = -spacing
                while (x <= width + spacing) {
                    val star = androidx.compose.ui.graphics.Path()
                    for (i in 0 until 16) {
                        val angle = Math.PI * i / 8.0 - Math.PI / 2.0
                        val radius = if (i % 2 == 0) outerRadius else innerRadius
                        val px = x + kotlin.math.cos(angle).toFloat() * radius
                        val py = y + kotlin.math.sin(angle).toFloat() * radius
                        if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
                    }
                    star.close()
                    drawPath(
                        path = star,
                        color = if ((row + col) % 2 == 0) gold else beige,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                    )
                    // Sparse diagonal connectors form a repeating Islamic geometric tile.
                    drawLine(
                        color = beige,
                        start = androidx.compose.ui.geometry.Offset(x + outerRadius * 0.72f, y + outerRadius * 0.72f),
                        end = androidx.compose.ui.geometry.Offset(x + spacing / 2f, y + spacing / 2f),
                        strokeWidth = stroke
                    )
                    x += spacing
                    col++
                }
                y += spacing
                row++
            }
        }
        
        when (wallpaper) {
            "MOSQUE SILHOUETTE" -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, height - 120f)
                    quadraticTo(width * 0.15f, height - 200f, width * 0.3f, height - 120f)
                    lineTo(width * 0.35f, height - 120f)
                    lineTo(width * 0.35f, height - 350f)
                    lineTo(width * 0.4f, height - 420f)
                    lineTo(width * 0.45f, height - 350f)
                    lineTo(width * 0.45f, height - 120f)
                    quadraticTo(width * 0.65f, height - 280f, width * 0.85f, height - 120f)
                    lineTo(width, height - 120f)
                    lineTo(width, height)
                    close()
                }
                drawPath(path, color = primaryColor.copy(alpha = 0.06f))
            }
            "STARRY UNIVERSE" -> {
                val starPositions = listOf(
                    Pair(0.1f, 0.15f), Pair(0.3f, 0.08f), Pair(0.85f, 0.12f),
                    Pair(0.9f, 0.3f), Pair(0.15f, 0.45f), Pair(0.7f, 0.5f),
                    Pair(0.5f, 0.25f), Pair(0.25f, 0.7f), Pair(0.8f, 0.75f)
                )
                starPositions.forEach { (x, y) ->
                    drawCircle(
                        color = Color(0x33D4AF37),
                        radius = 8f,
                        center = androidx.compose.ui.geometry.Offset(width * x, height * y)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = 4f,
                        center = androidx.compose.ui.geometry.Offset(width * x, height * y)
                    )
                }
            }
            "GEOMETRIC ARABESQUE" -> {
                val spacing = 180f
                val strokeWidth = 1.5f
                for (x in 0..(width / spacing).toInt() + 1) {
                    for (y in 0..(height / spacing).toInt() + 1) {
                        val cx = x * spacing
                        val cy = y * spacing
                        drawLine(
                            color = goldColor,
                            start = androidx.compose.ui.geometry.Offset(cx - spacing, cy - spacing),
                            end = androidx.compose.ui.geometry.Offset(cx + spacing, cy + spacing),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = goldColor,
                            start = androidx.compose.ui.geometry.Offset(cx + spacing, cy - spacing),
                            end = androidx.compose.ui.geometry.Offset(cx - spacing, cy + spacing),
                            strokeWidth = strokeWidth
                        )
                        drawCircle(
                            color = goldColor,
                            radius = spacing * 0.4f,
                            center = androidx.compose.ui.geometry.Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                    }
                }
            }
        }
    }
}
