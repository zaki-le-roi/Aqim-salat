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
import androidx.compose.foundation.lazy.items
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
    val history by viewModel.quranHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<QuranData.Surah?>(null) }

    // Direct routing support from bookmarks/external triggers
    LaunchedEffect(initialSurahId) {
        if (initialSurahId != null) {
            val s = QuranData.surahs.find { it.id == initialSurahId }
            if (s != null) {
                selectedSurah = s
            }
        }
    }

    // Filter surahs based on query
    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            QuranData.surahs
        } else {
            QuranData.surahs.filter {
                it.englishName.contains(searchQuery, ignoreCase = true) ||
                        it.name.contains(searchQuery) ||
                        it.translation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (selectedSurah == null) {
        // --- Surah List Browsing Mode ---
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = Translations.get("quran", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Continue Reading Banner (Linked directly to Room History)
            val lastRead = history.firstOrNull()
            if (lastRead != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            val originalSurah = QuranData.surahs.find { it.id == lastRead.surahId }
                            if (originalSurah != null) {
                                selectedSurah = originalSurah
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = "Continue",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Translations.get("continue_reading", lang),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${lastRead.surahId}. ${lastRead.surahName}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Forward",
                            tint = Color(0xFFD4AF37)
                        )
                    }
                }
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("quran_search_bar"),
                placeholder = { Text(Translations.get("search_surah", lang)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Surah Scrollable List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSurahs, key = { it.id }) { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSurah = surah }
                            .testTag("surah_card_${surah.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = surah.id.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = surah.englishName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${surah.translation} • ${surah.totalAyahs} Ayahs",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Text(
                                text = surah.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37)
                            )
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

    // Configuration Settings (MUSHAF mode is default as requested!)
    var isMushafMode by remember { mutableStateOf(true) }
    var verticalScroll by remember { mutableStateOf(true) }
    var readingTheme by remember { mutableStateOf("WARM") } // WARM, NIGHT, GREEN
    var showReadingOptionsSheet by remember { mutableStateOf(false) }

    // Selected verse for Bottom Sheet (Tafsir / Word-by-word / Bookmarking / Play individual audio)
    var selectedAyahDetails by remember { mutableStateOf<QuranApiClient.ApiAyah?>(null) }

    // Loaded verses state
    var verses by remember { mutableStateOf<List<QuranApiClient.ApiAyah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Audio stream state
    var activeAudioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlayingIndex by remember { mutableStateOf(-1) }

    // Safe releases of Media Player
    fun releaseAudio() {
        activeAudioPlayer?.release()
        activeAudioPlayer = null
        currentlyPlayingIndex = -1
    }

    DisposableEffect(surah.id) {
        viewModel.saveLastReadSurah(surah.id, 1, surah.englishName)

        if (QuranData.localAyahs.containsKey(surah.id)) {
            val localList = QuranData.localAyahs[surah.id]!!.map {
                val paddedSurah = String.format("%03d", surah.id)
                val paddedAyah = String.format("%03d", it.number)
                QuranApiClient.ApiAyah(
                    numberInSurah = it.number,
                    arabicText = it.text,
                    translationText = it.translation,
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
                    val apiResult = QuranApiClient.fetchSurah(surah.id)
                    verses = apiResult
                } catch (e: Exception) {
                    errorMsg = "Ensure you are connected to the Internet to browse and cache all 114 Surahs."
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

    // Define Colors based on reading theme
    val bgColor = when (readingTheme) {
        "WARM" -> Color(0xFFFAF6E9)
        "GREEN" -> Color(0xFF0F2618)
        else -> Color(0xFF121212) // NIGHT
    }

    val txtColor = when (readingTheme) {
        "WARM" -> Color(0xFF1A1710)
        "GREEN" -> Color(0xFFF1ECE0)
        else -> Color(0xFFE8E5DC) // NIGHT
    }

    val goldAccent = when (readingTheme) {
        "WARM" -> Color(0xFF9E7E2C)
        "GREEN" -> Color(0xFFE0C06E)
        else -> Color(0xFFD4AF37) // NIGHT
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(surah.englishName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "${surah.translation} • ${surah.totalAyahs} Ayahs",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }, modifier = Modifier.testTag("surah_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Quick Action: Reciter play state toggle
                    if (currentlyPlayingIndex != -1) {
                        IconButton(onClick = { releaseAudio() }) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Reading Options Sheet toggle (Matches the settings panel in the 3rd screenshot)
                    IconButton(
                        onClick = { showReadingOptionsSheet = true },
                        modifier = Modifier.testTag("open_reading_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Reading Settings",
                            tint = goldAccent
                        )
                    }

                    // Toggle between Mushaf and Translation list modes
                    IconButton(
                        onClick = { isMushafMode = !isMushafMode },
                        modifier = Modifier.testTag("toggle_mushaf_button")
                    ) {
                        Icon(
                            imageVector = if (isMushafMode) Icons.Filled.ViewList else Icons.Filled.MenuBook,
                            contentDescription = "Toggle Read Mode",
                            tint = goldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(innerPadding)
        ) {
            // --- Custom Toolbar for Mushaf settings ---
            if (isMushafMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Theme togglers
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("WARM", "GREEN", "NIGHT").forEach { th ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (th) {
                                            "WARM" -> Color(0xFFFAF6E9)
                                            "GREEN" -> Color(0xFF0F2618)
                                            else -> Color(0xFF121212)
                                        }
                                    )
                                    .clickable { readingTheme = th }
                                    .border(
                                        width = if (readingTheme == th) 2.dp else 1.dp,
                                        color = if (readingTheme == th) goldAccent else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Scroll orientation togglers
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { verticalScroll = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapVert,
                                contentDescription = "Vertical",
                                tint = if (verticalScroll) goldAccent else Color.Gray
                            )
                        }

                        IconButton(
                            onClick = { verticalScroll = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Horizontal",
                                tint = if (!verticalScroll) goldAccent else Color.Gray
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
                        CircularProgressIndicator(color = goldAccent)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading verses...",
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
                        Icon(Icons.Filled.CloudOff, contentDescription = "Offline", modifier = Modifier.size(64.dp), tint = txtColor.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMsg!!,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            color = txtColor
                        )
                    }
                } else {
                    // --- MUSHAF MODE (AUTHENTIC printed continuous sheet) ---
                    if (isMushafMode) {
                        // Gather details
                        val startPage = SURAH_START_PAGES.getOrNull(surah.id - 1) ?: 1
                        val juzNum = getJuzNumber(surah.id, 1)

                        if (verticalScroll) {
                            // 1. Continuous Vertical Scroll Layout
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Surah header decorative block
                                item {
                                    SurahHeaderBlock(surah = surah, goldColor = goldAccent, txtColor = txtColor)
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                // Bismillah block
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

                                // Flow/Wrap all verses into a beautiful printed continuous text block
                                item {
                                    TextFlowContainer(
                                        verses = verses,
                                        txtColor = txtColor,
                                        goldColor = goldAccent,
                                        onVerseClick = { selectedAyahDetails = it }
                                    )
                                }

                                // Bottom Footer detailing Juz and Page range
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الجزء $juzNum",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                        Text(
                                            text = "صـ $startPage",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                        Text(
                                            text = "سورة ${surah.englishName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                    }
                                }
                            }
                        } else {
                            // 2. Horizontal Page Flipping Layout (Authentic Mushaf Experience with Smooth Page-Turning)
                            // Distribute verses in chunks of ~8 verses per page to simulate printed pages
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

                                // Calculate smooth realistic page-turning offset animations!
                                val pageOffset = (pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction
                                val scale = (1f - (Math.abs(pageOffset) * 0.12f)).coerceAtLeast(0.85f)
                                val alpha = (1f - (Math.abs(pageOffset) * 0.4f)).coerceAtLeast(0.6f)
                                val rotationY = pageOffset * -12f

                                MushafPageFrame(
                                    isNightMode = (readingTheme == "NIGHT"),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            this.scaleX = scale
                                            this.scaleY = scale
                                            this.alpha = alpha
                                            this.rotationY = rotationY
                                            this.cameraDistance = 8 * density
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Page Top Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "الجزء ${getJuzNumber(surah.id, startIdx + 1)}",
                                                fontSize = 11.sp,
                                                color = goldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "سورة ${surah.name}",
                                                fontSize = 12.sp,
                                                color = goldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Surah Header only on the first page
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (pageIdx == 0) {
                                                SurahHeaderBlock(surah = surah, goldColor = goldAccent, txtColor = txtColor)
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

                                        // Page Bottom Footer number
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
                        // --- STANDARD TRANSLATION VIEW (Separated Cards list) ---
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
                                        containerColor = if (isPlayingThisAyah) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                    )
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
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ayah.numberInSurah.toString(),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { playRecitation(verses.indexOf(ayah), ayah.audioUrl) }) {
                                                    Icon(
                                                        imageVector = if (isPlayingThisAyah) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                                        contentDescription = "Listen",
                                                        tint = goldAccent
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        viewModel.toggleBookmark(
                                                            type = "QURAN",
                                                            referenceId = refKey,
                                                            title = "${surah.englishName} • Ayah ${ayah.numberInSurah}",
                                                            subtitle = ayah.translationText.take(50) + "...",
                                                            arabicText = ayah.arabicText,
                                                            translationText = ayah.translationText
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                        contentDescription = "Bookmark",
                                                        tint = if (isBookmarked) goldAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = ayah.arabicText,
                                            fontSize = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth(),
                                            lineHeight = 36.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = ayah.translationText,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            lineHeight = 20.sp
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

    // --- Custom Reading Options Sheet (Matches third screenshot) ---
    if (showReadingOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReadingOptionsSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                // Header
                Text(
                    text = "طريقة عرض المصحف",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A5F),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Segmented control row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Vertical Mushaf Tab
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
                            text = "المصحف الرأسي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (verticalScroll) Color(0xFF6B3A9E) else Color.Gray
                        )
                    }

                    // Horizontal Mushaf Tab
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
                            text = "المصحف الأفقي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!verticalScroll) Color(0xFF6B3A9E) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options list
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 1. Mushaf Mode toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMushafMode = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isMushafMode,
                            onCheckedChange = { isMushafMode = it }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "عرض المصحف",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color(0xFF6B3A9E))
                        }
                    }

                    // 2. Auto scroll
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = Color.LightGray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "التصفح التلقائي",
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Filled.SwapVert, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    // 3. Memorization with "جديد" badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "جديد" Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE51C23))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "جديد",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "التحفيظ والمراجعة",
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Color(0xFFFFB300))
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                // Section: الخصائص
                Text(
                    text = "الخصائص",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val properties = listOf(
                        Triple("الترجمة", Icons.Filled.Language, "lang"),
                        Triple("الصوتيات", Icons.Filled.VolumeUp, "audio"),
                        Triple("المعاني", Icons.Filled.List, "meanings"),
                        Triple("التفسير", Icons.Filled.LibraryBooks, "tafsir")
                    )
                    properties.forEach { (name, icon, key) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = name, tint = Color(0xFF6B3A9E))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = name, fontSize = 12.sp, color = Color(0xFF333333))
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                // Section: تخصيص المصحف
                Text(
                    text = "تخصيص المصحف",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Night Mode Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = readingTheme == "NIGHT",
                        onCheckedChange = { checked ->
                            readingTheme = if (checked) "NIGHT" else "WARM"
                        }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "الوضع الليلي",
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Filled.NightsStay, contentDescription = null, tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Theme Color circle selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Pair("WARM", Color(0xFFFAF6E9)),
                            Pair("GREEN", Color(0xFF0F2618)),
                            Pair("NIGHT", Color(0xFF121212))
                        ).forEach { (thName, col) ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (readingTheme == thName) 2.dp else 1.dp,
                                        color = if (readingTheme == thName) Color(0xFF6B3A9E) else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { readingTheme = thName }
                            )
                        }
                    }
                    Text(
                        text = "لون مصحفك",
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- Interactive Verses Details Bottom Sheet ---
    // Shown whenever user clicks on a verse in continuous Mushaf Mode
    if (selectedAyahDetails != null) {
        val details = selectedAyahDetails!!
        val refKey = "${surah.id}:${details.numberInSurah}"
        val isBookmarked = bookmarks.any { it.type == "QURAN" && it.referenceId == refKey }
        val isPlayingThisAyah = currentlyPlayingIndex == verses.indexOf(details)

        ModalBottomSheet(
            onDismissRequest = { selectedAyahDetails = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                        text = "الآية ${details.numberInSurah} • سورة ${surah.englishName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = goldAccent
                    )

                    Row {
                        // Play Ayah Recitation
                        IconButton(onClick = { playRecitation(verses.indexOf(details), details.audioUrl) }) {
                            Icon(
                                imageVector = if (isPlayingThisAyah) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                contentDescription = "Play Audio",
                                tint = goldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Toggle Bookmark
                        IconButton(
                            onClick = {
                                viewModel.toggleBookmark(
                                    type = "QURAN",
                                    referenceId = refKey,
                                    title = "${surah.englishName} • Ayah ${details.numberInSurah}",
                                    subtitle = details.translationText.take(50) + "...",
                                    arabicText = details.arabicText,
                                    translationText = details.translationText
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Fav",
                                tint = goldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Original Arabic text
                Text(
                    text = details.arabicText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Word-by-word break down placeholder
                Text(
                    text = "WORD-BY-WORD MODE (LITERAL TRANSLATION)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Generate Word-by-Word
                val wordsAr = details.arabicText.split(" ")
                val wordsEn = details.translationText.split(" ")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    wordsAr.take(4).forEachIndexed { i, word ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                                .padding(8.dp)
                        ) {
                            Text(word, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text(wordsEn.getOrNull(i)?.take(6) ?: "...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tafsir translation
                Text(
                    text = "TAFSIR SUMMARY (COMMETARY)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldAccent
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = details.translationText + " This verse emphasizes the divine source of the Holy Quran, calling humanity to reflect upon the pristine guidance of Allah and adhere to spiritual uprightness.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { selectedAyahDetails = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close details")
                }
            }
        }
    }
}

@Composable
fun SurahHeaderBlock(surah: QuranData.Surah, goldColor: Color, txtColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(goldColor.copy(alpha = 0.1f))
            .border(2.dp, goldColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = surah.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = goldColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = surah.englishName.uppercase() + " • " + surah.revelationType.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = txtColor,
                letterSpacing = 1.sp
            )
            Text(
                text = "${surah.totalAyahs} VERSES",
                fontSize = 10.sp,
                color = txtColor.copy(alpha = 0.6f)
            )
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
                    append("﴿${ayah.numberInSurah}﴾")
                }
                append("   ")
                pop()
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
        // Draw double borders and intricate star corners
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
