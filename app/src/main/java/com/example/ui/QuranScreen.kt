package com.example.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuranApiClient
import com.example.data.QuranData
import com.example.data.QuranFontLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 114 Surah Start Pages in Standard 604-page Madinah Mushaf
val SURAH_START_PAGES = intArrayOf(
    1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262, 267, 282, 293, 305, 312,
    322, 332, 342, 350, 359, 367, 377, 385, 396, 404, 411, 415, 418, 428, 434, 440, 446, 453, 458, 467,
    477, 483, 489, 496, 499, 502, 507, 511, 515, 518, 520, 523, 526, 528, 531, 534, 537, 542, 545, 549,
    551, 553, 554, 556, 558, 560, 562, 564, 566, 568, 570, 572, 574, 575, 577, 578, 580, 582, 583, 585,
    586, 587, 587, 589, 590, 591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597, 598, 598, 599, 599,
    600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604, 604, 604
)

// Mapping each Juz (1..30) to the starting Surah ID
val JUZ_START_SURAHS = intArrayOf(
    1,   // Juz 1 -> Al-Fatihah
    2,   // Juz 2 -> Al-Baqarah
    2,   // Juz 3 -> Al-Baqarah
    3,   // Juz 4 -> Ali 'Imran
    4,   // Juz 5 -> An-Nisa
    4,   // Juz 6 -> An-Nisa
    5,   // Juz 7 -> Al-Ma'idah
    6,   // Juz 8 -> Al-An'am
    7,   // Juz 9 -> Al-A'raf
    8,   // Juz 10 -> Al-Anfal
    9,   // Juz 11 -> At-Tawbah
    11,  // Juz 12 -> Hud
    12,  // Juz 13 -> Yusuf
    15,  // Juz 14 -> Al-Hijr
    17,  // Juz 15 -> Al-Isra
    18,  // Juz 16 -> Al-Kahf
    21,  // Juz 17 -> Al-Anbiya
    23,  // Juz 18 -> Al-Mu'minun
    25,  // Juz 19 -> Al-Furqan
    27,  // Juz 20 -> An-Naml
    29,  // Juz 21 -> Al-'Ankabut
    33,  // Juz 22 -> Al-Ahzab
    36,  // Juz 23 -> Ya-Sin
    39,  // Juz 24 -> Az-Zumar
    41,  // Juz 25 -> Fussilat
    46,  // Juz 26 -> Al-Ahqaf
    51,  // Juz 27 -> Adh-Dhariyat
    58,  // Juz 28 -> Al-Mujadilah
    67,  // Juz 29 -> Al-Mulk
    78   // Juz 30 -> An-Naba
)

// Helper to calculate Juz number based on standard distributions
fun getJuzNumber(surahId: Int, ayahNum: Int): Int {
    if (surahId == 1) return 1
    if (surahId == 2) {
        if (ayahNum <= 141) return 1
        if (ayahNum <= 252) return 2
        return 3
    }
    if (surahId == 3) {
        if (ayahNum <= 92) return 3
        return 4
    }
    if (surahId == 4) {
        if (ayahNum <= 23) return 4
        if (ayahNum <= 147) return 5
        return 6
    }
    if (surahId == 5) {
        if (ayahNum <= 81) return 6
        return 7
    }
    if (surahId == 6) {
        if (ayahNum <= 110) return 7
        return 8
    }
    if (surahId == 7) {
        if (ayahNum <= 87) return 8
        return 9
    }
    if (surahId == 8) {
        if (ayahNum <= 40) return 9
        return 10
    }
    if (surahId == 9) {
        if (ayahNum <= 92) return 10
        return 11
    }
    if (surahId <= 11) return 12
    if (surahId <= 14) return 13
    if (surahId <= 16) return 14
    if (surahId <= 18) return 15
    if (surahId <= 20) return 16
    if (surahId <= 22) return 17
    if (surahId <= 25) return 18
    if (surahId <= 27) return 19
    if (surahId <= 29) return 20
    if (surahId <= 32) return 21
    if (surahId <= 36) return 22
    if (surahId <= 38) return 23
    if (surahId <= 41) return 24
    if (surahId <= 45) return 25
    if (surahId <= 50) return 26
    if (surahId <= 57) return 27
    if (surahId <= 66) return 28
    if (surahId <= 77) return 29
    return 30
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: AppViewModel,
    lang: String,
    modifier: Modifier = Modifier,
    initialSurahId: Int? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var quranFontFamily by remember { mutableStateOf<FontFamily?>(null) }
    LaunchedEffect(Unit) {
        quranFontFamily = QuranFontLoader.load(context)
    }
    val history by viewModel.quranHistory.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("ALL") } // ALL, MECCAN, MEDINAN, BOOKMARKS
    var selectedSurah by remember { mutableStateOf<QuranData.Surah?>(null) }
    
    val lazyListState = rememberLazyListState()

    // Direct routing support from bookmarks/external triggers
    LaunchedEffect(initialSurahId) {
        if (initialSurahId != null) {
            val s = QuranData.surahs.find { it.id == initialSurahId }
            if (s != null) {
                selectedSurah = s
            }
        }
    }

    // Filter bookmarked surahs
    val bookmarkedSurahIds = remember(bookmarks) {
        bookmarks.filter { it.type == "QURAN" }.mapNotNull {
            val parts = it.referenceId.split(":")
            parts.getOrNull(0)?.toIntOrNull()
        }.toSet()
    }

    // Filter surahs based on query & selected category
    val filteredSurahs = remember(searchQuery, selectedTab, bookmarkedSurahIds) {
        var list = QuranData.surahs
        
        if (searchQuery.isNotBlank()) {
            val trimmed = searchQuery.trim()
            list = list.filter {
                it.name.contains(trimmed) || 
                it.id.toString() == trimmed || 
                it.englishName.contains(trimmed, ignoreCase = true)
            }
        }
        
        when (selectedTab) {
            "MECCAN" -> list.filter { it.revelationType.equals("Meccan", ignoreCase = true) }
            "MEDINAN" -> list.filter { it.revelationType.equals("Medinan", ignoreCase = true) }
            "BOOKMARKS" -> list.filter { it.id in bookmarkedSurahIds }
            else -> list
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (lang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl) {
        if (selectedSurah == null) {
            // --- Surah List Browsing Mode ---
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFFEFDF9)) // Warm elegant ivory background
            ) {
                // --- Islamic Rich Header ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(Color(0xFF042B1D)) // Deep Emerald Green
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "en") "﴿  The Holy Quran  ﴾" else "﴿  القرآن الكريم  ﴾",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "en") "The Holy Quran" else "القرآن الكريم",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "كِتَابٌ أَنزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِّيَدَّبَّرُوا آيَاتِهِ",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Continue Reading Banner (Linked directly to Room History)
                    val lastRead = history.firstOrNull()
                    if (lastRead != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val originalSurah = QuranData.surahs.find { it.id == lastRead.surahId }
                                    if (originalSurah != null) {
                                        selectedSurah = originalSurah
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF042B1D).copy(alpha = 0.05f)),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE2E2E2))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD4AF37).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = null,
                                            tint = Color(0xFFD4AF37),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (lang == "en") "Continue reading" else "مواصلة القراءة",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (lang == "en") "Surah ${lastRead.surahName}" else "سورة ${lastRead.surahName}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF042B1D)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Filled.ChevronLeft,
                                    contentDescription = null,
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // --- Search Bar ---
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quran_search_bar"),
                        placeholder = { Text(if (lang == "en") "Search surah name or number..." else "ابحث عن اسم السورة أو رقمها...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = if (lang == "en") "Search" else "بحث", tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = if (lang == "en") "Clear" else "مسح", tint = Color.Gray)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color(0xFFE2E2E2),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Category Tabs ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            "ALL" to if (lang == "en") "All" else "كل السور",
                            "MECCAN" to if (lang == "en") "Meccan" else "مكية",
                            "MEDINAN" to if (lang == "en") "Medinan" else "مدنية",
                            "BOOKMARKS" to if (lang == "en") "Bookmarks" else "المفضلة"
                        )
                        tabs.forEach { (key, title) ->
                            val isSelected = selectedTab == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedTab = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color(0xFF042B1D) else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Juz Quick Jump Index ---
                    Text(
                        text = if (lang == "en") "Quick access by Juz" else "الوصول السريع بالأجزاء",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF042B1D),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(30) { index ->
                            val juzNum = index + 1
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF042B1D).copy(alpha = 0.06f))
                                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val startSurahId = JUZ_START_SURAHS.getOrNull(index) ?: 1
                                        val scrollIndex = filteredSurahs.indexOfFirst { it.id == startSurahId }
                                        if (scrollIndex != -1) {
                                            coroutineScope.launch {
                                                lazyListState.animateScrollToItem(scrollIndex)
                                            }
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (lang == "en") "Juz $juzNum" else "جزء $juzNum",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF042B1D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Surah Scrollable List ---
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (filteredSurahs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (lang == "en") "No matching results" else "لا توجد نتائج مطابقة لبحثك",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(filteredSurahs, key = { it.id }) { surah ->
                                val isMeccan = surah.revelationType.equals("Meccan", ignoreCase = true)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSurah = surah }
                                        .testTag("surah_card_${surah.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE2E2E2))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Star Number Badge
                                            IslamicStarBadge(number = surah.id)

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column {
                                                Text(
                                                    text = surah.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = Color(0xFF042B1D)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Type indicator
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(
                                                                if (isMeccan) Color(0xFFFF9800).copy(alpha = 0.1f)
                                                                else Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (lang == "en") if (isMeccan) "Meccan" else "Medinan" else if (isMeccan) "مكية" else "مدنية",
                                                            fontSize = 10.sp,
                                                            color = if (isMeccan) Color(0xFFE65100) else Color(0xFF2E7D32),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = if (lang == "en") "Ayahs: ${surah.totalAyahs}" else "عدد الآيات: ${surah.totalAyahs}",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }

                                        // Offline availability indicator
                                        val isOffline = surah.id in QuranData.localAyahs.keys
                                        if (isOffline) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (lang == "en") "Available offline" else "متاحة دون اتصال",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF4CAF50)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.OfflinePin,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowBack, // Rotated directionally in RTL automatically
                                                contentDescription = null,
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // --- Surah Reader Mode ---
            SurahReader(
                viewModel = viewModel,
                surah = selectedSurah!!,
                lang = lang,
                onBack = { selectedSurah = null }
            )
        }
    }
}

@Composable
fun IslamicStarBadge(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.dp.toPx()
            val color = Color(0xFFD4AF37) // Golden
            val sizePx = size.minDimension
            val radius = sizePx / 2
            val center = center

            val path = androidx.compose.ui.graphics.Path()
            for (i in 0..4) {
                val angle = Math.toRadians((i * 90).toDouble())
                val x = center.x + radius * Math.cos(angle)
                val y = center.y + radius * Math.sin(angle)
                if (i == 0) path.moveTo(x.toFloat(), y.toFloat()) else path.lineTo(x.toFloat(), y.toFloat())
            }
            path.close()
            
            val path2 = androidx.compose.ui.graphics.Path()
            for (i in 0..4) {
                val angle = Math.toRadians((i * 90 + 45).toDouble())
                val x = center.x + radius * Math.cos(angle)
                val y = center.y + radius * Math.sin(angle)
                if (i == 0) path2.moveTo(x.toFloat(), y.toFloat()) else path2.lineTo(x.toFloat(), y.toFloat())
            }
            path2.close()

            drawPath(path, color = color.copy(alpha = 0.12f), style = androidx.compose.ui.graphics.drawscope.Fill)
            drawPath(path, color = color, style = Stroke(width = strokeWidth))
            
            drawPath(path2, color = color.copy(alpha = 0.12f), style = androidx.compose.ui.graphics.drawscope.Fill)
            drawPath(path2, color = color, style = Stroke(width = strokeWidth))

            drawCircle(
                color = color.copy(alpha = 0.4f),
                radius = radius * 0.7f,
                style = Stroke(width = 0.8.dp.toPx())
            )
        }
        Text(
            text = number.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF042B1D),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReader(
    viewModel: AppViewModel,
    surah: QuranData.Surah,
    lang: String,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val bookmarks by viewModel.bookmarks.collectAsState()

    // Configuration Settings
    var isMushafMode by remember { mutableStateOf(true) }
    var verticalScroll by remember { mutableStateOf(true) }
    var readingTheme by remember { mutableStateOf("WARM") } // WARM, GREEN, NIGHT
    var showReadingOptionsSheet by remember { mutableStateOf(false) }

    // Selected verse for Bottom Sheet
    var selectedAyahDetails by remember { mutableStateOf<QuranApiClient.ApiAyah?>(null) }

    // Loaded verses state
    var verses by remember { mutableStateOf<List<QuranApiClient.ApiAyah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Audio stream state
    var activeAudioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlayingIndex by remember { mutableStateOf(-1) }

    fun releaseAudio() {
        activeAudioPlayer?.release()
        activeAudioPlayer = null
        currentlyPlayingIndex = -1
    }

    DisposableEffect(surah.id) {
        viewModel.saveLastReadSurah(surah.id, 1, surah.name)

        if (QuranData.localAyahs.containsKey(surah.id)) {
            val localList = QuranData.localAyahs[surah.id]!!.map {
                val paddedSurah = String.format("%03d", surah.id)
                val paddedAyah = String.format("%03d", it.number)
                QuranApiClient.ApiAyah(
                    numberInSurah = it.number,
                    arabicText = it.text,
                    translationText = "",
                    audioUrl = "https://everyayah.com/data/Alafasy_128kbps/$paddedSurah$paddedAyah.mp3"
                )
            }
            verses = localList
            isLoading = false
        } else {
            coroutineScope.launch {
                try {
                    isLoading = true
                    errorMsg = null
                    val apiResult = QuranApiClient.fetchSurah(surah.id, lang)
                    verses = apiResult
                } catch (e: Exception) {
                    errorMsg = if (lang == "ar") {
    if (lang == "en") "This surah could not be loaded. Retry when internet is available; no surah file download is required." else "تعذر تحميل هذه السورة الآن. أعد المحاولة عند توفر الإنترنت؛ لا حاجة لتنزيل ملف السورة."
} else {
    "This surah could not be loaded right now. Retry when internet is available; no surah file download is required."
}
                } finally {
                    isLoading = false
                }
            }
        }

        onDispose {
            releaseAudio()
        }
    }

    // Function to stream recitation for a specific Ayah
    fun playRecitation(index: Int, url: String) {
        if (currentlyPlayingIndex == index) {
            activeAudioPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.start()
                }
            }
            return
        }

        releaseAudio()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    currentlyPlayingIndex = index
                }
                val mPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    prepare()
                    start()
                }
                withContext(Dispatchers.Main) {
                    activeAudioPlayer = mPlayer
                }

                mPlayer.setOnCompletionListener {
                    val nextIdx = index + 1
                    if (nextIdx < verses.size) {
                        playRecitation(nextIdx, verses[nextIdx].audioUrl)
                    } else {
                        releaseAudio()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    releaseAudio()
                }
            }
        }
    }

    val bgColor = when (readingTheme) {
        "WARM" -> Color(0xFFFAF6EE)
        "GREEN" -> Color(0xFF0F261D)
        else -> Color(0xFF121212)
    }

    val txtColor = when (readingTheme) {
        "WARM" -> Color(0xFF231C13)
        "GREEN" -> Color(0xFFE4ECD5)
        else -> Color(0xFFE0E0E0)
    }

    val goldAccent = when (readingTheme) {
        "WARM" -> Color(0xFF8E6C3F)
        "GREEN" -> Color(0xFFD4AF37)
        else -> Color(0xFFC5A059)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (lang == "en") "Surah ${surah.name}" else "سورة ${surah.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF042B1D))
                        Text(
                            text = if (lang == "en") "Ayahs: ${surah.totalAyahs} • ${if (surah.revelationType.equals("Meccan", ignoreCase = true)) "Meccan" else "Medinan"}" else "آياتها: ${surah.totalAyahs} • نزولها: ${if (surah.revelationType.equals("Meccan", ignoreCase = true)) "مكية" else "مدنية"}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }, modifier = Modifier.testTag("surah_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = if (lang == "en") "Back" else "رجوع", tint = Color(0xFF042B1D))
                    }
                },
                actions = {
                    if (currentlyPlayingIndex != -1) {
                        IconButton(onClick = { releaseAudio() }) {
                            Icon(Icons.Filled.Stop, contentDescription = if (lang == "en") "Stop" else "إيقاف", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Reading Options Sheet toggle
                    IconButton(
                        onClick = { showReadingOptionsSheet = true },
                        modifier = Modifier.testTag("open_reading_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = if (lang == "en") "Reading options" else "خيارات القراءة",
                            tint = Color(0xFF042B1D)
                        )
                    }

                    // Toggle between Mushaf and Translation list modes
                    IconButton(
                        onClick = { isMushafMode = !isMushafMode },
                        modifier = Modifier.testTag("toggle_mushaf_button")
                    ) {
                        Icon(
                            imageVector = if (isMushafMode) Icons.Filled.ViewList else Icons.Filled.MenuBook,
                            contentDescription = if (lang == "en") "Change view" else "تبديل العرض",
                            tint = Color(0xFF042B1D)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        CompositionLocalProvider(LocalLayoutDirection provides if (lang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(innerPadding)
            ) {
                // --- Quick Theme Toolbar ---
                if (isMushafMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Theme togglers
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("WARM", "GREEN", "NIGHT").forEach { th ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (th) {
                                                "WARM" -> Color(0xFFFAF6EE)
                                                "GREEN" -> Color(0xFF0F261D)
                                                else -> Color(0xFF121212)
                                            }
                                        )
                                        .clickable { readingTheme = th }
                                        .border(
                                            width = if (readingTheme == th) 2.dp else 1.dp,
                                            color = if (readingTheme == th) Color(0xFFD4AF37) else Color.LightGray,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        // Scroll orientation togglers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { verticalScroll = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapVert,
                                    contentDescription = if (lang == "en") "Vertical" else "رأسي",
                                    tint = if (verticalScroll) Color(0xFFD4AF37) else Color.Gray
                                )
                            }

                            IconButton(
                                onClick = { verticalScroll = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapHoriz,
                                    contentDescription = if (lang == "en") "Horizontal" else "أفقي",
                                    tint = if (!verticalScroll) Color(0xFFD4AF37) else Color.Gray
                                )
                            }
                        }
                    }
                }

                // --- Main Content Canvas ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFFD4AF37))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (lang == "en") "Loading verses..." else "جاري تحميل الآيات الكريمة...",
                                fontSize = 14.sp,
                                color = txtColor.copy(alpha = 0.6f)
                            )
                        }
                    } else if (errorMsg != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CloudOff, contentDescription = if (lang == "en") "Offline" else "غير متصل", modifier = Modifier.size(64.dp), tint = txtColor.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMsg!!,
                                textAlign = TextAlign.Center,
                                fontSize = 15.sp,
                                color = txtColor
                            )
                        }
                    } else {
                        if (isMushafMode) {
                            // --- MUSHAF MODE ---
                            val startPage = SURAH_START_PAGES.getOrNull(surah.id - 1) ?: 1
                            val juzNum = getJuzNumber(surah.id, 1)

                            if (verticalScroll) {
                                // 1. Continuous Vertical Scroll
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                    contentPadding = PaddingValues(bottom = 120.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    item {
                                        SurahHeaderBlock(surah = surah, lang = lang, goldColor = goldAccent, txtColor = txtColor)
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }

                                    if (surah.id != 9 && surah.id != 1) {
                                        item {
                                            Text(
                                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(bottom = 20.dp)
                                            )
                                        }
                                    }

                                    item {
                                        TextFlowContainer(
                                            verses = verses,
                                            txtColor = txtColor,
                                            goldColor = goldAccent,
                                            onVerseClick = { selectedAyahDetails = it }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (lang == "en") "Juz $juzNum" else "الجزء $juzNum",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                            Text(
                                                text = if (lang == "en") "p. $startPage" else "صـ $startPage",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                            Text(
                                                text = if (lang == "en") "Surah ${surah.name}" else "سورة ${surah.name}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                        }
                                    }
                                }
                            } else {
                                // 2. Horizontal Page Flipping Layout
                                val versesPerPage = 8
                                val pagesCount = (verses.size + versesPerPage - 1) / versesPerPage
                                val pagerState = rememberPagerState(pageCount = { pagesCount })

                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { pageIdx ->
                                    val startIdx = pageIdx * versesPerPage
                                    val endIdx = (startIdx + versesPerPage).coerceAtLeast(0).coerceAtMost(verses.size)
                                    val pageVerses = verses.subStringSafely(startIdx, endIdx)
                                    val pageNum = startPage + pageIdx

                                    MushafPageFrame(
                                        isNightMode = (readingTheme == "NIGHT"),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (lang == "en") "Juz ${getJuzNumber(surah.id, startIdx + 1)}" else "الجزء ${getJuzNumber(surah.id, startIdx + 1)}",
                                                    fontSize = 11.sp,
                                                    color = goldAccent,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (lang == "en") "Surah ${surah.name}" else "سورة ${surah.name}",
                                                    fontSize = 12.sp,
                                                    color = goldAccent,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                if (pageIdx == 0) {
                                                    SurahHeaderBlock(surah = surah, lang = lang, goldColor = goldAccent, txtColor = txtColor)
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    if (surah.id != 9 && surah.id != 1) {
                                                        Text(
                                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                                            fontSize = 22.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = goldAccent,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(bottom = 12.dp)
                                                        )
                                                    }
                                                }

                                                TextFlowContainer(
                                                    verses = pageVerses,
                                                    txtColor = txtColor,
                                                    goldColor = goldAccent,
                                                    onVerseClick = { selectedAyahDetails = it }
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .border(width = 1.dp, color = goldAccent.copy(alpha = 0.5f), shape = CircleShape)
                                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "$pageNum",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = goldAccent
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // --- TAFSIR CARD VIEW (Arabic Tafsir) ---
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (surah.id != 9 && surah.id != 1) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                items(verses) { ayah ->
                                    val refKey = "${surah.id}:${ayah.numberInSurah}"
                                    val isBookmarked = bookmarks.any { it.type == "QURAN" && it.referenceId == refKey }
                                    val isPlayingThisAyah = currentlyPlayingIndex == verses.indexOf(ayah)

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ayah_card_$refKey"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPlayingThisAyah) Color(0xFFD4AF37).copy(alpha = 0.08f) else Color.White
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2E2).copy(alpha = 0.6f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF042B1D).copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = ayah.numberInSurah.toString(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF042B1D)
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = { playRecitation(verses.indexOf(ayah), ayah.audioUrl) }) {
                                                        Icon(
                                                            imageVector = if (isPlayingThisAyah) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                                            contentDescription = if (lang == "en") "Listen" else "استماع",
                                                            tint = goldAccent
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.toggleBookmark(
                                                                type = "QURAN",
                                                                referenceId = refKey,
                                                                title = if (lang == "en") "Surah ${surah.name} • Ayah ${ayah.numberInSurah}" else "سورة ${surah.name} • آية ${ayah.numberInSurah}",
                                                                subtitle = (if (lang == "en") "Meaning: " else "تفسير: ") + ayah.translationText.take(50) + "...",
                                                                arabicText = ayah.arabicText,
                                                                translationText = ayah.translationText
                                                            )
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                            contentDescription = if (lang == "en") "Save" else "حفظ",
                                                            tint = if (isBookmarked) goldAccent else Color.Gray
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = ayah.arabicText,
                                                fontSize = 26.sp,
                                                fontFamily = quranFontFamily ?: FontFamily.Serif,
                                                color = Color(0xFF042B1D),
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth(),
                                                lineHeight = 48.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))
                                            HorizontalDivider(color = Color(0xFFEEEEEE))
                                            Spacer(modifier = Modifier.height(10.dp))

                                            if (ayah.translationText.isNotBlank()) {
                                                Text(
                                                    text = if (lang == "ar") "التفسير الميسر:" else "Tafsir / meaning:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = goldAccent
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = ayah.translationText,
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray,
                                                    lineHeight = 24.sp,
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Reading Options Sheet ---
    if (showReadingOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReadingOptionsSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (lang == "en") "Reading view" else "طريقة عرض المصحف",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF042B1D),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (verticalScroll) Color.White else Color.Transparent)
                            .clickable { verticalScroll = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "en") "Vertical Mushaf" else "المصحف الرأسي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (verticalScroll) Color(0xFFD4AF37) else Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!verticalScroll) Color.White else Color.Transparent)
                            .clickable { verticalScroll = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "en") "Horizontal Mushaf" else "المصحف الأفقي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!verticalScroll) Color(0xFFD4AF37) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isMushafMode = !isMushafMode }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (lang == "en") "Page-based Mushaf view" else "طريقة عرض المصحف بالصفحات",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    Switch(
                        checked = isMushafMode,
                        onCheckedChange = { isMushafMode = it }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                Text(
                    text = if (lang == "en") "Reading background" else "لون خلفية القراءة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Triple("WARM", Color(0xFFFAF6EE), if (lang == "en") "Warm" else "دافئ"),
                        Triple("GREEN", Color(0xFF0F261D), if (lang == "en") "Calm green night" else "ليلي هادئ"),
                        Triple("NIGHT", Color(0xFF121212), if (lang == "en") "Dark" else "داكن")
                    ).forEach { (thName, col, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { readingTheme = thName }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (readingTheme == thName) 2.dp else 1.dp,
                                        color = if (readingTheme == thName) Color(0xFFD4AF37) else Color.LightGray,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- Interactive Verse Details Sheets (Tafsir Al-Muyassar) ---
    if (selectedAyahDetails != null) {
        val details = selectedAyahDetails!!
        val refKey = "${surah.id}:${details.numberInSurah}"
        val isBookmarked = bookmarks.any { it.type == "QURAN" && it.referenceId == refKey }
        val isPlayingThisAyah = currentlyPlayingIndex == verses.indexOf(details)

        ModalBottomSheet(
            onDismissRequest = { selectedAyahDetails = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "en") "Surah ${surah.name} • Ayah ${details.numberInSurah}" else "سورة ${surah.name} • الآية ${details.numberInSurah}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFD4AF37)
                    )

                    Row {
                        IconButton(onClick = { playRecitation(verses.indexOf(details), details.audioUrl) }) {
                            Icon(
                                imageVector = if (isPlayingThisAyah) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                contentDescription = if (lang == "en") "Listen" else "استماع",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.toggleBookmark(
                                    type = "QURAN",
                                    referenceId = refKey,
                                    title = if (lang == "en") "Surah ${surah.name} • Ayah ${details.numberInSurah}" else "سورة ${surah.name} • آية ${details.numberInSurah}",
                                    subtitle = (if (lang == "en") "Meaning: " else "تفسير: ") + details.translationText.take(50) + "...",
                                    arabicText = details.arabicText,
                                    translationText = details.translationText
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (lang == "en") "Save" else "حفظ",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = details.arabicText,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF042B1D),
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (lang == "en") "Tafsir al-Muyassar:" else "التفسير الميسر للآية الكريمة:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = details.translationText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { selectedAyahDetails = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF042B1D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (lang == "en") "Close details" else "إغلاق التفاصيل", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SurahHeaderBlock(surah: QuranData.Surah, lang: String, goldColor: Color, txtColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(goldColor.copy(alpha = 0.08f))
            .border(1.5.dp, goldColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (lang == "en") "Surah ${surah.name}" else "سورة ${surah.name}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF042B1D)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (lang == "en") if (surah.revelationType.equals("Meccan", ignoreCase = true)) "Meccan" else "Medinan" else if (surah.revelationType.equals("Meccan", ignoreCase = true)) "مكية" else "مدنية",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (lang == "en") "Ayahs: ${surah.totalAyahs}" else "عدد الآيات: ${surah.totalAyahs}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = txtColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TextFlowContainer(
    verses: List<QuranApiClient.ApiAyah>,
    txtColor: Color,
    goldColor: Color,
    onVerseClick: (QuranApiClient.ApiAyah) -> Unit
) {
    val annotatedString = remember(verses) {
        buildAnnotatedString {
            verses.forEachIndexed { index, ayah ->
                pushStringAnnotation(tag = "ayah_index", annotation = index.toString())
                append(ayah.arabicText)
                append(" ")
                withStyle(style = SpanStyle(color = goldColor, fontWeight = FontWeight.Bold)) {
                    append(" ﴿${ayah.numberInSurah}﴾ ")
                }
                append("   ")
                pop()
            }
        }
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "ayah_index", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val index = annotation.item.toIntOrNull()
                    if (index != null && index in verses.indices) {
                        onVerseClick(verses[index])
                    }
                }
        },
        style = LocalTextStyle.current.copy(
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = txtColor,
            lineHeight = 44.sp,
            textAlign = TextAlign.Justify,
            fontFamily = FontFamily.Serif
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

// Utility extension for sub-string safety
fun <T> List<T>.subStringSafely(start: Int, end: Int): List<T> {
    val s = start.coerceIn(0, this.size)
    val e = end.coerceIn(s, this.size)
    return this.subList(s, e)
}

@Composable
fun MushafPageFrame(
    isNightMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val goldColor = Color(0xFFD4AF37)
    val outerGold = Color(0xFFC59B27)
    
    val paperColor = if (isNightMode) Color(0xFF121214) else Color(0xFFFAF6EE)
    val frameBorderColor = if (isNightMode) outerGold.copy(alpha = 0.5f) else outerGold
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(paperColor)
            .padding(12.dp)
            .border(width = 1.dp, color = frameBorderColor.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val inset1 = 6.dp.toPx()
            val inset2 = 10.dp.toPx()
            
            drawRect(
                color = frameBorderColor,
                topLeft = androidx.compose.ui.geometry.Offset(inset1, inset1),
                size = androidx.compose.ui.geometry.Size(w - 2 * inset1, h - 2 * inset1),
                style = Stroke(width = 1.dp.toPx())
            )
            
            drawRect(
                color = frameBorderColor,
                topLeft = androidx.compose.ui.geometry.Offset(inset2, inset2),
                size = androidx.compose.ui.geometry.Size(w - 2 * inset2, h - 2 * inset2),
                style = Stroke(width = 1.5.dp.toPx())
            )
            
            val cornerOffsets = listOf(
                androidx.compose.ui.geometry.Offset(inset2, inset2),
                androidx.compose.ui.geometry.Offset(w - inset2, inset2),
                androidx.compose.ui.geometry.Offset(inset2, h - inset2),
                androidx.compose.ui.geometry.Offset(w - inset2, h - inset2)
            )
            
            cornerOffsets.forEach { offset ->
                drawCircle(
                    color = goldColor,
                    radius = 4.dp.toPx(),
                    center = offset
                )
                drawLine(
                    color = goldColor,
                    start = androidx.compose.ui.geometry.Offset(offset.x - 6.dp.toPx(), offset.y - 6.dp.toPx()),
                    end = androidx.compose.ui.geometry.Offset(offset.x + 6.dp.toPx(), offset.y + 6.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = goldColor,
                    start = androidx.compose.ui.geometry.Offset(offset.x + 6.dp.toPx(), offset.y - 6.dp.toPx()),
                    end = androidx.compose.ui.geometry.Offset(offset.x - 6.dp.toPx(), offset.y + 6.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            content()
        }
    }
}
