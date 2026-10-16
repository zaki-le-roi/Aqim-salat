package com.example.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
import com.example.data.QuranOfflineData
import com.example.data.QuranPageMap
import com.example.data.QuranFontLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

private const val QURAN_CACHE_PREFS = "quran_surah_cache"

private val ArabicFontFamily = FontFamily.Serif

private fun loadCachedSurah(context: android.content.Context, surahId: Int, lang: String): List<QuranApiClient.ApiAyah>? {
    val raw = context.getSharedPreferences(QURAN_CACHE_PREFS, android.content.Context.MODE_PRIVATE)
        .getString("${surahId}_${lang}", null) ?: return null
    return runCatching {
        val array = JSONArray(raw)
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            QuranApiClient.ApiAyah(item.getInt("numberInSurah"), item.getString("arabicText"), item.optString("translationText"), item.optString("audioUrl"))
        }
    }.getOrNull()?.takeIf { it.isNotEmpty() }
}

private fun saveCachedSurah(context: android.content.Context, surahId: Int, lang: String, verses: List<QuranApiClient.ApiAyah>) {
    runCatching {
        val array = JSONArray()
        verses.forEach { verse -> array.put(JSONObject().apply { put("numberInSurah", verse.numberInSurah); put("arabicText", verse.arabicText); put("translationText", verse.translationText); put("audioUrl", verse.audioUrl) }) }
        context.getSharedPreferences(QURAN_CACHE_PREFS, android.content.Context.MODE_PRIVATE).edit().putString("${surahId}_${lang}", array.toString()).apply()
    }
}
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
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // --- Refined Quran library hero ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0F4C3A), Color(0xFF0B382C), Color(0xFF082B22))
                            )
                        )
                        .statusBarsPadding()
                        .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 6.dp)
                ) {
                    // Quiet decorative motif, kept behind the readable content.
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 2.dp)
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.035f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 25.dp, end = 19.dp)
                            .size(78.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFD7B56D).copy(alpha = 0.22f), CircleShape)
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFFE3C47D),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(11.dp))
                                Column {
                                    Text(
                                        text = "المصحف الشريف",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "رفيق تلاوتك وتدبّرك",
                                        color = Color.White.copy(alpha = 0.70f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.10f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, Color.White.copy(alpha = 0.10f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFFE3C47D),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("١١٤ سورة", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }


                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
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
                                            text = if (lang == "en") "Continue reading from last position" else "مواصلة القراءة من آخر موضع",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (lang == "en") "Surah ${lastRead.surahName} • Ayah ${lastRead.ayahId}" else "سورة ${lastRead.surahName} • الآية ${lastRead.ayahId}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        val originalSurah = QuranData.surahs.find { it.id == lastRead.surahId }
                                        if (originalSurah != null) {
                                            selectedSurah = originalSurah
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFD4AF37).copy(alpha = 0.2f))
                                ) {
                                    Text(if (lang == "en") "Resume" else "استكمال القراءة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B6B15))
                                }
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
                            focusedBorderColor = Color(0xFF0F4C3A),
                            unfocusedBorderColor = Color(0xFFD8E4DD),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Category Tabs ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
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
                                    .background(if (isSelected) Color(0xFF0F4C3A) else Color.Transparent)
                                    .clickable { selectedTab = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.primary,
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Surah Scrollable List ---
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 88.dp, top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (selectedTab == "BOOKMARKS") {
                            val quranBookmarks = bookmarks.filter { it.type == "QURAN" }
                            if (quranBookmarks.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.BookmarkBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = if (lang == "en") "No saved bookmarks yet" else "لا توجد علامات مرجعية محفوظة بعد",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (lang == "en") "Tap any verse in the reader to bookmark it" else "اضغط على أي آية داخل المصحف لحفظها في العلامات المرجعية",
                                                fontSize = 12.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(quranBookmarks, key = { it.referenceId }) { bm ->
                                    val surahId = bm.referenceId.split(":").firstOrNull()?.toIntOrNull()
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (surahId != null) {
                                                    val s = QuranData.surahs.find { it.id == surahId }
                                                    if (s != null) selectedSurah = s
                                                }
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFFD4AF37).copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = bm.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                IconButton(
                                                    onClick = {
                                                        viewModel.toggleBookmark(
                                                            type = bm.type,
                                                            referenceId = bm.referenceId,
                                                            title = bm.title,
                                                            subtitle = bm.subtitle,
                                                            arabicText = bm.arabicText,
                                                            translationText = bm.translationText
                                                        )
                                                    }
                                                ) {
                                                    Icon(Icons.Filled.Bookmark, contentDescription = "حذف العلامة", tint = Color(0xFFD4AF37))
                                                }
                                            }
                                            if (bm.arabicText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = bm.arabicText,
                                                    fontSize = 17.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            if (bm.translationText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = bm.translationText,
                                                    fontSize = 12.sp,
                                                    color = Color.Gray,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (filteredSurahs.isEmpty()) {
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
                                    shape = RoundedCornerShape(11.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE2E2E2))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Star Number Badge
                                            IslamicStarBadge(number = surah.id)

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Text(
                                                    text = surah.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
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
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

private fun normalizeQuranSearch(text: String): String =
    text.replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]"), "")
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ٱ', 'ا')
        .replace('ى', 'ي')
        .replace(Regex("\\s+"), " ")
        .trim()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurahReader(
    viewModel: AppViewModel,
    surah: QuranData.Surah,
    lang: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = remember(context) { QuranPageMap.load(context) }
    val prefs = remember(context) {
        context.getSharedPreferences("quran_reader", android.content.Context.MODE_PRIVATE)
    }
    var currentPage by remember(surah.id) {
        mutableIntStateOf(
            prefs.getInt("saved_page", -1).takeIf { it in 1..604 }
                ?: QuranPageMap.pageFor(context, surah.id, 1).coerceIn(1, 604)
        )
    }
    var bookmarkPage by remember { mutableIntStateOf(prefs.getInt("bookmark_page", -1)) }
    var controlsVisible by remember { mutableStateOf(false) }
    var showIndex by remember { mutableStateOf(false) }
    var showJuzIndex by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Triple<Int, Int, String>>>(emptyList()) }
    var offlineSurahs by remember { mutableStateOf<Map<Int, List<QuranApiClient.ApiAyah>>>(emptyMap()) }
    val pagerState = rememberPagerState(
        initialPage = (currentPage - 1).coerceIn(0, 603),
        pageCount = { 604 }
    )

    LaunchedEffect(Unit) {
        offlineSurahs = withContext(Dispatchers.IO) {
            QuranData.surahs.mapNotNull { item ->
                QuranOfflineData.loadSurah(context, item.id)?.let { item.id to it }
            }.toMap()
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage + 1
        prefs.edit().putInt("saved_page", currentPage).apply()
        pages.getOrNull(pagerState.currentPage)?.firstOrNull()?.let { ref ->
            val pageSurah = QuranData.surahs.firstOrNull { it.id == ref.surah }
            if (pageSurah != null) viewModel.saveLastReadSurah(ref.surah, ref.ayah, pageSurah.name)
        }
    }
    LaunchedEffect(searchQuery, offlineSurahs) {
        if (searchQuery.isBlank() || offlineSurahs.isEmpty()) {
            searchResults = emptyList()
        } else {
            val needle = normalizeQuranSearch(searchQuery)
            searchResults = withContext(Dispatchers.Default) {
                QuranData.surahs.flatMap { item ->
                    offlineSurahs[item.id].orEmpty().mapNotNull { ayah ->
                        val text = ayah.arabicText
                        if (normalizeQuranSearch(text).contains(needle)) {
                            Triple(item.id, ayah.numberInSurah, text)
                        } else null
                    }
                }.take(100)
            }
        }
    }

    val pageRefs = pages.getOrNull(pagerState.currentPage).orEmpty()
    val firstRef = pageRefs.firstOrNull()
    val pageSurahName = firstRef?.let { ref ->
        QuranData.surahs.firstOrNull { it.id == ref.surah }?.name
    } ?: surah.name
    val juzNumber = firstRef?.let { getJuzNumber(it.surah, it.ayah) } ?: 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clickable { controlsVisible = !controlsVisible }
        ) { pageIndex ->
            val pageUrl = "https://raw.githubusercontent.com/akram-seid/quran-hd-images/main/images/${(pageIndex + 1).toString().padStart(3, '0')}.jpg"
            var pageScale by remember(pageIndex) { mutableFloatStateOf(1f) }
            var pageOffsetX by remember(pageIndex) { mutableFloatStateOf(0f) }
            var pageOffsetY by remember(pageIndex) { mutableFloatStateOf(0f) }
            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                val nextScale = (pageScale * zoomChange).coerceIn(1f, 4f)
                pageScale = nextScale
                if (nextScale > 1f) {
                    pageOffsetX += panChange.x
                    pageOffsetY += panChange.y
                } else {
                    pageOffsetX = 0f
                    pageOffsetY = 0f
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFDFBF7)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = pageUrl,
                    contentDescription = "صفحة المصحف الشريف رقم ${pageIndex + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = pageScale
                            scaleY = pageScale
                            translationX = pageOffsetX
                            translationY = pageOffsetY
                        }
                        .transformable(
                            state = transformState,
                            canPan = { pageScale > 1f }
                        ),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }

        if (controlsVisible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color(0xFF115E3F),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = "سورة $pageSurahName",
                            color = Color(0xFFD4AF37),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "صفحة ${pagerState.currentPage + 1}",
                            color = Color(0xFFD4AF37),
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "الجزء $juzNumber",
                            color = Color(0xFFD4AF37),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color(0xFF115E3F),
                shadowElevation = 5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MushafBottomControl(Icons.Filled.List, "الفهرس") { showIndex = true }
                    MushafBottomControl(Icons.Filled.Bookmarks, "الأجزاء") { showJuzIndex = true }
                    MushafBottomControl(Icons.Filled.BookmarkAdd, "حفظ علامة") {
                        bookmarkPage = pagerState.currentPage + 1
                        prefs.edit().putInt("bookmark_page", bookmarkPage).apply()
                        Toast.makeText(context, "تم حفظ العلامة عند الصفحة $bookmarkPage", Toast.LENGTH_SHORT).show()
                    }
                    MushafBottomControl(Icons.Filled.Bookmark, "الانتقال للعلامة") {
                        if (bookmarkPage in 1..604) {
                            scope.launch { pagerState.animateScrollToPage(bookmarkPage - 1) }
                        } else {
                            Toast.makeText(context, "لم تحفظ علامة بعد", Toast.LENGTH_SHORT).show()
                        }
                    }
                    MushafBottomControl(Icons.Filled.Search, "البحث") { showSearch = true }
                }
            }
        }

        // Persistent Mushaf identity badge: remains visible even when reader controls are hidden.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 10.dp,
                    bottom = if (controlsVisible) 76.dp else 12.dp
                ),
            shape = RoundedCornerShape(50),
            color = Color(0xE6115E3F),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, Color(0xFFD4AF37).copy(alpha = 0.75f)
            ),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = "المصحف الشريف",
                    tint = Color(0xFFE3C47D),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "مصحف المدينة النبوية",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }

        if (showIndex) {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF115E3F),
                    surface = Color(0xFFFAF8F5),
                    onSurface = Color(0xFF1A1A1A),
                    background = Color(0xFFFAF8F5)
                )
            ) {
                AlertDialog(
                onDismissRequest = { showIndex = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFFFAF8F5),
                titleContentColor = Color(0xFF115E3F),
                textContentColor = Color(0xFF2C1D11),
                iconContentColor = Color(0xFF115E3F),
                title = { Text("فهرس السور", color = Color(0xFF115E3F), fontFamily = ArabicFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 440.dp)) {
                        items(QuranData.surahs, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val target = QuranPageMap.pageFor(context, item.id, 1).coerceIn(1, 604)
                                        showIndex = false
                                        scope.launch { pagerState.animateScrollToPage(target - 1) }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.7f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF115E3F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.id.toString(),
                                            color = Color(0xFFD4AF37),
                                            fontFamily = ArabicFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "سورة ${item.name}",
                                            color = Color(0xFF115E3F),
                                            fontFamily = ArabicFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Text(
                                            text = "اضغط للانتقال إلى السورة",
                                            color = Color(0xFF82796C),
                                            fontFamily = ArabicFontFamily,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text("۞", color = Color(0xFFD4AF37), fontSize = 23.sp, fontFamily = ArabicFontFamily)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showIndex = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF115E3F))
                    ) { Text("إغلاق", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Color(0xFF115E3F)) }
                }
                )
            }
        }

        if (showJuzIndex) {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF115E3F),
                    surface = Color(0xFFFAF8F5),
                    onSurface = Color(0xFF1A1A1A),
                    background = Color(0xFFFAF8F5)
                )
            ) {
                AlertDialog(
                onDismissRequest = { showJuzIndex = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFFFAF8F5),
                titleContentColor = Color(0xFF115E3F),
                textContentColor = Color(0xFF2C1D11),
                iconContentColor = Color(0xFF115E3F),
                title = { Text("الانتقال إلى الجزء", color = Color(0xFF115E3F), fontFamily = ArabicFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 440.dp)) {
                        items((1..30).toList()) { juz ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val startSurah = JUZ_START_SURAHS[juz - 1]
                                        val target = QuranPageMap.pageFor(context, startSurah, 1).coerceIn(1, 604)
                                        showJuzIndex = false
                                        scope.launch { pagerState.animateScrollToPage(target - 1) }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.7f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF115E3F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = juz.toString(),
                                            color = Color(0xFFD4AF37),
                                            fontFamily = ArabicFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "الجزء $juz",
                                            color = Color(0xFF115E3F),
                                            fontFamily = ArabicFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Text(
                                            text = "اضغط للانتقال إلى الجزء",
                                            color = Color(0xFF82796C),
                                            fontFamily = ArabicFontFamily,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text("۞", color = Color(0xFFD4AF37), fontSize = 23.sp, fontFamily = ArabicFontFamily)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showJuzIndex = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF115E3F))
                    ) { Text("إغلاق", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Color(0xFF115E3F)) }
                }
                )
            }
        }

        if (showSearch) {
            AlertDialog(
                onDismissRequest = { showSearch = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFFFAF8F5),
                titleContentColor = Color(0xFF115E3F),
                textContentColor = Color(0xFF2C1D11),
                iconContentColor = Color(0xFF115E3F),
                title = { Text("البحث في القرآن", color = Color(0xFF115E3F), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("اكتب كلمة أو عبارة قرآنية") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        if (offlineSurahs.isEmpty()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
                            Text("لا توجد نتائج مطابقة")
                        }
                        LazyColumn(modifier = Modifier.heightIn(max = 330.dp)) {
                            items(searchResults) { result ->
                                val resultSurah = QuranData.surahs.firstOrNull { it.id == result.first }
                                TextButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF115E3F)),
                                    onClick = {
                                        val target = QuranPageMap.pageFor(context, result.first, result.second).coerceIn(1, 604)
                                        showSearch = false
                                        scope.launch { pagerState.animateScrollToPage(target - 1) }
                                    }
                                ) {
                                    Column(Modifier.fillMaxWidth()) {
                                        Text(
                                            "سورة ${resultSurah?.name.orEmpty()} — الآية ${result.second}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2C1D11)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showSearch = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF115E3F))
                    ) { Text("إغلاق", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Color(0xFF115E3F)) }
                }
            )
        }
    }
}


@Composable
private fun MushafBottomControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 3.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(21.dp)
            )
            Text(label, color = Color(0xFFD4AF37), fontFamily = ArabicFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
                color = MaterialTheme.colorScheme.primary
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
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    fontFamily: FontFamily = FontFamily.Serif,
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
                append(" ")
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
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = txtColor,
            lineHeight = (fontSize.value * 1.75f).sp,
            textAlign = TextAlign.Right,
            fontFamily = fontFamily
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
