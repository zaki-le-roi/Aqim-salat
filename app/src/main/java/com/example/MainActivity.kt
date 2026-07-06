package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val activeLanguage by appViewModel.language.collectAsState()
            val activeThemeMode by appViewModel.themeMode.collectAsState()
            val nextPrayerName by appViewModel.nextPrayerName.collectAsState()

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        appViewModel.stopLocationTracking()
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

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    AqimSalahApp(viewModel = appViewModel, lang = activeLanguage)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AqimSalahApp(
    viewModel: AppViewModel,
    lang: String
) {
    var selectedTab by remember { mutableStateOf("HOME") }
    
    // Dynamic detail screens controllers
    var activeDetailScreen by remember { mutableStateOf<String?>(null) }
    var initialToolParam by remember { mutableStateOf<String?>(null) }
    var initialSurahIdParam by remember { mutableStateOf<Int?>(null) }

    // Dynamic routing callback across all dashboard grid cards
    val handleFeatureNavigation: (String) -> Unit = { routeKey ->
        when (routeKey) {
            "PRAYER_TIMES" -> {
                selectedTab = "PRAYER_TIMES"
                activeDetailScreen = null
            }
            "QURAN" -> {
                selectedTab = "QURAN"
                initialSurahIdParam = null
                activeDetailScreen = null
            }
            "QIBLA" -> {
                selectedTab = "QIBLA"
                activeDetailScreen = null
            }
            "MORE", "MORE_MENU" -> {
                selectedTab = "MORE"
                activeDetailScreen = null
            }
            "TOOLS_MENU", "TOOLS" -> {
                activeDetailScreen = "TOOLS"
                initialToolParam = null
            }
            "TASBIH", "ADHKAR", "NAMES", "RAMADAN", "KHATMAH", "HISN_AL_MUSLIM", "TRAVEL", "HALAL_FOOD" -> {
                activeDetailScreen = "TOOLS"
                initialToolParam = routeKey
            }
            "MOSQUES", "MOSQUE_LOCATOR" -> {
                activeDetailScreen = "TOOLS"
                initialToolParam = "MOSQUES"
            }
            "CALENDAR", "DAILY_DUA", "FAVORITES", "SEARCH", "QURAN_AUDIO", "HADITH", "SETTINGS", "ADMIN", "DONATIONS", "AUTH",
            "COMMUNITY", "POLLS", "FAJR_LIST", "COMPETITION",
            "FAQ", "SUPPORT", "ABOUT_US", "PARTNERS", "OUR_APPS", "RATE_SHARE", "WORLD_CUP", "ON_THIS_DAY" -> {
                activeDetailScreen = routeKey
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Keep screen space beautifully uncluttered when operating in specific tools
            if (activeDetailScreen == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    val tabs = listOf(
                        Triple("HOME", Translations.get("home", lang), Icons.Filled.Home),
                        Triple("QURAN", Translations.get("quran", lang), Icons.Filled.MenuBook),
                        Triple("PRAYER_TIMES", if (lang == "ar") "المواقيت" else "Prayers", Icons.Filled.AccessTime),
                        Triple("QIBLA", Translations.get("qibla", lang), Icons.Filled.CompassCalibration),
                        Triple("MORE", if (lang == "ar") "المزيد" else "More", Icons.Filled.MoreHoriz)
                    )

                    tabs.forEach { (route, label, icon) ->
                        val isSelected = selectedTab == route
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
                                    tint = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("nav_item_$route")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val activeWallpaper by viewModel.wallpaper.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (activeDetailScreen == null) innerPadding else PaddingValues(0.dp))
        ) {
            ThemeBackground(wallpaper = activeWallpaper)

            if (activeDetailScreen != null) {
                // Overlay detail views with pristine back navigation support
                when (activeDetailScreen) {
                    "CALENDAR" -> CalendarScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "DAILY_DUA" -> DailyDuaScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "ADMIN" -> AdminScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "DONATIONS" -> DonationsScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null })
                    "AUTH" -> AuthScreen(viewModel = viewModel, lang = lang, onBack = { activeDetailScreen = null }, onNavigateToAdmin = { activeDetailScreen = "ADMIN" })
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
                                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            )
                        }
                    ) { p ->
                        SettingsScreen(viewModel = viewModel, lang = lang, modifier = Modifier.padding(p))
                    }
                    "COMMUNITY", "POLLS", "FAJR_LIST", "COMPETITION" -> {
                        CommunityPollsScreen(
                            viewModel = viewModel,
                            lang = lang,
                            initialTab = activeDetailScreen!!,
                            onBack = { activeDetailScreen = null }
                        )
                    }
                    "WORLD_CUP" -> {
                        WorldCupScreen(
                            viewModel = viewModel,
                            lang = lang,
                            onBack = { activeDetailScreen = null }
                        )
                    }
                    "ON_THIS_DAY" -> {
                        OnThisDayScreen(
                            viewModel = viewModel,
                            lang = lang,
                            onBack = { activeDetailScreen = null }
                        )
                    }
                    "FAQ", "SUPPORT", "ABOUT_US", "PARTNERS", "OUR_APPS", "RATE_SHARE" -> {
                        SupportScreen(
                            viewModel = viewModel,
                            lang = lang,
                            initialTab = activeDetailScreen!!,
                            onBack = { activeDetailScreen = null }
                        )
                    }
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
                                                "RAMADAN" -> Translations.get("ramadan", lang)
                                                "MOSQUES" -> if (lang == "ar") "المساجد القريبة" else "Nearby Mosques"
                                                "KHATMAH" -> if (lang == "ar") "خاتمة القرآن" else "Ramadan Khatmah"
                                                "HISN_AL_MUSLIM" -> if (lang == "ar") "حصن المسلم" else "Hisn Al Muslim"
                                                "TRAVEL" -> if (lang == "ar") "حقيبة المسافر" else "Travel Companion"
                                                "HALAL_FOOD" -> if (lang == "ar") "المطاعم الحلال القريبة" else "Nearby Halal Food"
                                                else -> if (lang == "ar") "الخدمات الإسلامية" else "Islamic Services"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { activeDetailScreen = null }) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                                    "RAMADAN" -> RamadanTool(viewModel, lang)
                                    "MOSQUES" -> MosquesTool(viewModel, lang)
                                    "KHATMAH" -> KhatmahTool(viewModel, lang)
                                    "HISN_AL_MUSLIM" -> HisnAlMuslimTool(viewModel, lang)
                                    "TRAVEL" -> TravelTool(viewModel, lang)
                                    "HALAL_FOOD" -> HalalFoodTool(viewModel, lang)
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
                        initialSurahId = initialSurahIdParam
                    )
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
                    "MORE" -> MoreScreen(
                        viewModel = viewModel,
                        lang = lang, 
                        onNavigateToFeature = handleFeatureNavigation
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeBackground(wallpaper: String, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = Color(0x1AD4AF37)
    
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
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
