package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HadithData
import com.example.data.HadithRemoteRepository

private const val HADITH_PREFS = "hadith_reading_prefs"
private const val KEY_LAST_BOOK = "last_read_book_id"
private const val KEY_LAST_HADITH_ID = "last_read_hadith_id"
private const val KEY_LAST_HADITH_NUM = "last_read_hadith_num"
private const val KEY_LAST_BOOK_TITLE = "last_read_book_title"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookmarks by viewModel.bookmarks.collectAsState()

    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var activeHadithReading by remember { mutableStateOf<HadithData.BookHadith?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Last reading position state
    val prefs = remember { context.getSharedPreferences(HADITH_PREFS, Context.MODE_PRIVATE) }
    var lastBookId by remember { mutableStateOf(prefs.getString(KEY_LAST_BOOK, null)) }
    var lastHadithId by remember { mutableStateOf(prefs.getString(KEY_LAST_HADITH_ID, null)) }
    var lastHadithNum by remember { mutableStateOf(prefs.getString(KEY_LAST_HADITH_NUM, null)) }
    var lastBookTitle by remember { mutableStateOf(prefs.getString(KEY_LAST_BOOK_TITLE, null)) }

    fun saveLastRead(book: HadithData.HadithBook, hadith: HadithData.BookHadith) {
        prefs.edit()
            .putString(KEY_LAST_BOOK, book.id)
            .putString(KEY_LAST_HADITH_ID, hadith.id)
            .putString(KEY_LAST_HADITH_NUM, hadith.numberInBook)
            .putString(KEY_LAST_BOOK_TITLE, book.title)
            .apply()
        lastBookId = book.id
        lastHadithId = hadith.id
        lastHadithNum = hadith.numberInBook
        lastBookTitle = book.title
    }

    var downloadedBook by remember { mutableStateOf<HadithData.HadithBook?>(null) }
    var isLoadingFullBook by remember { mutableStateOf(false) }

    // Load the complete sourced corpus when a book is opened. Keep the bundled
    // excerpts visible as a safe fallback while downloading or when offline.
    LaunchedEffect(selectedBookId) {
        downloadedBook = null
        isLoadingFullBook = false
        val id = selectedBookId ?: return@LaunchedEffect
        val baseBook = HadithData.getBookById(id) ?: return@LaunchedEffect
        isLoadingFullBook = true
        try {
            downloadedBook = HadithRemoteRepository.loadFullBook(context, baseBook)
        } finally {
            isLoadingFullBook = false
        }
    }

    val selectedBook = remember(selectedBookId, downloadedBook) {
        val id = selectedBookId
        if (id == null) null
        else downloadedBook?.takeIf { it.id == id } ?: HadithData.getBookById(id)
    }

    BackHandler(enabled = selectedBook != null) {
        selectedBookId = null
    }

    if (selectedBook != null) {
        // --- Dedicated Book Screen ---
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            if (isLoadingFullBook) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "جارٍ تحميل النص الكامل من المصدر…",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (downloadedBook == null) {
                val message = if (HadithRemoteRepository.supportsFullText(selectedBook.id)) {
                    "تُعرض النصوص المتاحة دون اتصال. اتصل بالإنترنت لتحميل بقية الكتاب وحفظه للقراءة لاحقًا."
                } else {
                    "المتاح حاليًا لهذا الكتاب مقتطفات فقط؛ لم يُضف مصدر كامل له بعد."
                }
                Text(
                    text = message,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                HadithBookDetailScreen(
            book = selectedBook,
            viewModel = viewModel,
            lang = lang,
            onBack = { selectedBookId = null },
            onReadHadith = { hadith ->
                activeHadithReading = hadith
                saveLastRead(selectedBook, hadith)
            },
            onSaveLastRead = { hadith ->
                saveLastRead(selectedBook, hadith)
                Toast.makeText(context, "تم حفظ موضع القراءة في ${selectedBook.title} (حديث ${hadith.numberInBook})", Toast.LENGTH_SHORT).show()
            }
                )
            }
        }
    } else {
        // --- Library Grid Screen (All 10 Books) ---
        val categories = listOf(
            "ALL" to "جميع الكتب (10)",
            "الصحيحان" to "الصحيحان",
            "السنن الأربعة" to "السنن الأربعة",
            "المسانيد والموطآت" to "المسانيد والموطآت",
            "الجوامع والآداب" to "الجوامع والآداب"
        )

        val filteredBooks = remember(selectedCategory, searchQuery) {
            HadithData.books.filter { book ->
                val matchesCategory = selectedCategory == "ALL" || book.category == selectedCategory
                val matchesQuery = searchQuery.isBlank() ||
                        book.title.contains(searchQuery) ||
                        book.author.contains(searchQuery) ||
                        book.introduction.contains(searchQuery) ||
                        book.chapters.any { ch ->
                            ch.title.contains(searchQuery) ||
                                    ch.hadiths.any { h -> h.arabicText.contains(searchQuery) || h.numberInBook == searchQuery.trim() }
                        }
                matchesCategory && matchesQuery
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            // Refined library header — visual-only change; book data and navigation remain unchanged.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAF8),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("hadith_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مكتبة الحديث الشريف",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F4C3A)
                            )
                            Text(
                                text = "أمهات مصادر السنة النبوية",
                                fontSize = 12.sp,
                                color = Color(0xFF4B5B53)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFD4AF37).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFFB38A20),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Compact premium banner; it does not replace or modify any book content.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0B3D30), Color(0xFF176B52))
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFD4AF37).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFD4AF37).copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(15.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoStories,
                                    contentDescription = null,
                                    tint = Color(0xFFFFE082),
                                    modifier = Modifier.size(27.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "اقرأ وتصفّح براحة",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ابحث بين الكتب والأبواب ونصوص الأحاديث",
                                    color = Color(0xFFFFFFFF),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "10",
                                    color = Color(0xFFFFE082),
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "كتب",
                                    color = Color(0xFFFFFFFF),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                            .testTag("hadith_library_search"),
                        placeholder = {
                            Text("ابحث عن كتاب أو باب أو حديث...", fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "بحث", tint = Color(0xFFB38A20))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 1.dp)
                    ) {
                        items(categories) { (catKey, catLabel) ->
                            val isSelected = selectedCategory == catKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = catKey },
                                label = {
                                    Text(
                                        catLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0F4C3A),
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Main Content: Last Reading Resume Banner + 10 Books Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Last Reading Resume Banner
                if (lastBookId != null && lastBookTitle != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBookId = lastBookId
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F4C3A).copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD4AF37).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Color(0xFFD4AF37))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("متابعة القراءة من آخر موضع", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("$lastBookTitle • حديث $lastHadithNum", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { selectedBookId = lastBookId },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFD4AF37).copy(alpha = 0.25f))
                                ) {
                                    Text("فتح الكتاب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B6B15))
                                }
                            }
                        }
                    }
                }

                // Grid of 10 Books
                item {
                    Text(
                        text = "أمهات كتب الحديث الشريف (${filteredBooks.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // 2-Column Book Grid
                val bookRows = filteredBooks.chunked(2)
                items(bookRows) { rowBooks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowBooks.forEach { book ->
                            Box(modifier = Modifier.weight(1f)) {
                                BookCardItem(
                                    book = book,
                                    onClick = { selectedBookId = book.id }
                                )
                            }
                        }
                        if (rowBooks.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Reading a Single Hadith in Detail
    if (activeHadithReading != null && selectedBook != null) {
        HadithFullReadingDialog(
            book = selectedBook,
            hadith = activeHadithReading!!,
            isBookmarked = bookmarks.any { it.type == "HADITH" && it.referenceId == activeHadithReading!!.id },
            onToggleBookmark = {
                val h = activeHadithReading!!
                viewModel.toggleBookmark(
                    type = "HADITH",
                    referenceId = h.id,
                    title = "${selectedBook.title} • حديث ${h.numberInBook}",
                    subtitle = h.narrator + " — " + h.reference,
                    arabicText = h.arabicText,
                    translationText = h.explanation
                )
            },
            onDismiss = { activeHadithReading = null },
            onNavigateHadith = { nextHadith ->
                activeHadithReading = nextHadith
                saveLastRead(selectedBook, nextHadith)
            }
        )
    }
}

/**
 * Visual Book Card inside the Library Grid.
 * Displays custom-composed authentic book cover with title and author.
 */
@Composable
fun BookCardItem(
    book: HadithData.HadithBook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp, pressedElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(book.primaryColorHex).copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Existing individual cover artwork and book metadata are preserved.
            BookCoverCanvas(
                book = book,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(166.dp)
                    .clip(RoundedCornerShape(topStart = 21.dp, topEnd = 21.dp))
            )

            // High-contrast information footer; preserve original book details.
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(
                    text = book.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F4C3A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = book.author,
                    fontSize = 11.sp,
                    color = Color(0xFF343A36),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(book.primaryColorHex).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (book.isFullyBundled) "متن كامل" else "${book.chapters.size} أبواب متاحة في هذه النسخة",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(book.primaryColorHex)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "فتح الكتاب",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Authentic Islamic leather-bound book cover with gilding, title, author, and ornamental border.
 * Distinct colors and ornament styles for each of the 10 books.
 */
@Composable
fun BookCoverCanvas(
    book: HadithData.HadithBook,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val primaryColor = Color(book.primaryColorHex)
    val secondaryColor = Color(book.secondaryColorHex)
    val goldColor = Color(0xFFD4AF37)
    val lightGold = Color(0xFFFFE082)

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor),
                    start = Offset(0f, 0f),
                    end = Offset(300f, 400f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Draw Islamic decorative gilded frames and corner rosettes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val borderPadding = if (isLarge) 10.dp.toPx() else 6.dp.toPx()
            val innerPadding = if (isLarge) 16.dp.toPx() else 10.dp.toPx()

            // Outer Gilded Border
            drawRect(
                color = goldColor.copy(alpha = 0.65f),
                topLeft = Offset(borderPadding, borderPadding),
                size = Size(w - 2 * borderPadding, h - 2 * borderPadding),
                style = Stroke(width = if (isLarge) 2.dp.toPx() else 1.2.dp.toPx())
            )

            // Inner Filigree Border
            drawRect(
                color = goldColor.copy(alpha = 0.35f),
                topLeft = Offset(innerPadding, innerPadding),
                size = Size(w - 2 * innerPadding, h - 2 * innerPadding),
                style = Stroke(width = 0.8.dp.toPx())
            )

            // Corner Diamond Medallions
            val corners = listOf(
                Offset(innerPadding, innerPadding),
                Offset(w - innerPadding, innerPadding),
                Offset(innerPadding, h - innerPadding),
                Offset(w - innerPadding, h - innerPadding)
            )
            corners.forEach { c ->
                drawCircle(color = lightGold, radius = if (isLarge) 3.5.dp.toPx() else 2.2.dp.toPx(), center = c)
            }

            // Left spine simulated leather groove
            drawLine(
                color = Color.Black.copy(alpha = 0.35f),
                start = Offset(if (isLarge) 24.dp.toPx() else 16.dp.toPx(), 0f),
                end = Offset(if (isLarge) 24.dp.toPx() else 16.dp.toPx(), h),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = goldColor.copy(alpha = 0.3f),
                start = Offset(if (isLarge) 26.dp.toPx() else 18.dp.toPx(), 0f),
                end = Offset(if (isLarge) 26.dp.toPx() else 18.dp.toPx(), h),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Center typography on the cover
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isLarge) 28.dp else 18.dp, vertical = if (isLarge) 18.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Islamic category
            Text(
                text = "﴿ ${book.category} ﴾",
                fontSize = if (isLarge) 11.sp else 9.sp,
                color = lightGold.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Center: Golden Oval Badge with Book Title
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE6001712))
                    .border(1.dp, goldColor.copy(alpha = 0.95f), RoundedCornerShape(14.dp))
                    .padding(horizontal = if (isLarge) 16.dp else 10.dp, vertical = if (isLarge) 12.dp else 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = book.title,
                        fontSize = if (isLarge) 20.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = lightGold,
                        textAlign = TextAlign.Center,
                        lineHeight = if (isLarge) 26.sp else 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تأليف: " + book.author,
                        fontSize = if (isLarge) 11.sp else 9.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom authenticity stamp
            Text(
                text = if (book.isFullyBundled) "متن كامل" else "نصوص مختارة",
                fontSize = if (isLarge) 10.sp else 8.sp,
                color = goldColor.copy(alpha = 0.75f),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dedicated Screen for an individual Hadith Book.
 * Includes: Book Cover, Title, Author, Brief Authentic Intro, Scope,
 * Chapter List, Hadith List with numbers and narrators, search, and navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithBookDetailScreen(
    book: HadithData.HadithBook,
    viewModel: AppViewModel,
    lang: String,
    onBack: () -> Unit,
    onReadHadith: (HadithData.BookHadith) -> Unit,
    onSaveLastRead: (HadithData.BookHadith) -> Unit
) {
    val context = LocalContext.current
    val bookmarks by viewModel.bookmarks.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedChapterId by remember { mutableStateOf<String?>("ALL") }

    // Filter hadiths by selected chapter and search query
    val displayedHadiths = remember(book, selectedChapterId, searchQuery) {
        val baseList = if (selectedChapterId == "ALL" || selectedChapterId == null) {
            book.allHadiths
        } else {
            book.chapters.find { it.id == selectedChapterId }?.hadiths ?: emptyList()
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim()
            baseList.filter {
                it.arabicText.contains(q) ||
                        it.narrator.contains(q) ||
                        it.numberInBook == q ||
                        it.explanation.contains(q) ||
                        it.chapterTitle.contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(book.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(book.author, fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("book_detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- 1. HERO HEADER: BOOK COVER & METADATA ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Large Book Cover
                            Box(
                                modifier = Modifier
                                    .width(115.dp)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                            ) {
                                BookCoverCanvas(book = book, isLarge = true, modifier = Modifier.fillMaxSize())
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Metadata Column
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = book.authorFullName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "توفي رحمه الله: " + book.deathYear,
                                    fontSize = 11.sp,
                                    color = Color(0xFFD4AF37),
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Total hadiths pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(book.primaryColorHex).copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = book.totalHadithsDesc,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(book.primaryColorHex)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))

                        // --- 2. BRIEF AUTHENTIC INTRODUCTION ---
                        Text(
                            text = "مقدمة تعريفية عن الكتاب ومكانته:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.introduction,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Justify
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // --- 4. SEARCH INSIDE BOOK ---
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().testTag("book_search_inside"),
                        placeholder = { Text("بحث داخل ${book.title} (نص الحديث، الرقم، أو الراوي)...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "بحث", tint = Color(0xFFD4AF37)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // --- 5. CHAPTER SELECTOR TABS ---
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(
                        text = "أبواب الكتاب المرتبة (${book.chapters.size} أبواب):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedChapterId == "ALL",
                                onClick = { selectedChapterId = "ALL" },
                                label = { Text("جميع الأبواب (${book.allHadiths.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD4AF37),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(book.chapters) { ch ->
                            val isSelected = selectedChapterId == ch.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedChapterId = ch.id },
                                label = { Text("${ch.title} (${ch.hadiths.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // --- 6. HADITHS LIST ---
            if (displayedHadiths.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد أحاديث مطابقة للبحث داخل هذا الكتاب", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(displayedHadiths, key = { it.id }) { hadith ->
                    val isBookmarked = bookmarks.any { it.type == "HADITH" && it.referenceId == hadith.id }
                    HadithCardItem(
                        hadith = hadith,
                        book = book,
                        isBookmarked = isBookmarked,
                        onOpen = { onReadHadith(hadith) },
                        onBookmarkToggle = {
                            viewModel.toggleBookmark(
                                type = "HADITH",
                                referenceId = hadith.id,
                                title = "${book.title} • حديث ${hadith.numberInBook}",
                                subtitle = hadith.narrator + " — " + hadith.reference,
                                arabicText = hadith.arabicText,
                                translationText = hadith.explanation
                            )
                        },
                        onSavePosition = { onSaveLastRead(hadith) },
                        onCopy = {
                            val clip = ClipData.newPlainText("Hadith", "${hadith.arabicText}\n[${hadith.reference}]")
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الحديث الشريف", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual Hadith Card inside Book View.
 * Displays chapter badge, verified number, narrator, complete tashkeel Arabic text, explanation, and action buttons.
 */
@Composable
fun HadithCardItem(
    hadith: HadithData.BookHadith,
    book: HadithData.HadithBook,
    isBookmarked: Boolean,
    onOpen: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onSavePosition: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onOpen() }
            .testTag("hadith_item_${hadith.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFFE8E8E8))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header Row: Chapter Badge + Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(book.primaryColorHex).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = hadith.chapterTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(book.primaryColorHex)
                    )
                }

                Text(
                    text = "حديث رقم: ${hadith.numberInBook}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Narrator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "عن " + hadith.narrator,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Authentic Arabic Text with Complete Tashkeel
            Text(
                text = hadith.arabicText,
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Explanation / Meaning
            if (hadith.explanation.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "المعنى: " + hadith.explanation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Reference Source
            Text(
                text = "المرجع: " + hadith.reference,
                fontSize = 10.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(6.dp))

            // Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("قراءة كاملة", fontSize = 12.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bookmark / Favorite button
                    IconButton(onClick = onBookmarkToggle) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "حفظ في المفضلة",
                            tint = if (isBookmarked) Color(0xFFD4AF37) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Save last reading position
                    IconButton(onClick = onSavePosition) {
                        Icon(
                            imageVector = Icons.Filled.TurnedInNot,
                            contentDescription = "حفظ موضع القراءة",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Copy button
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "نسخ",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Hadith Reader Dialog with Next/Prev Hadith navigation, font sizing, and bookmarking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithFullReadingDialog(
    book: HadithData.HadithBook,
    hadith: HadithData.BookHadith,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateHadith: (HadithData.BookHadith) -> Unit
) {
    val context = LocalContext.current
    var fontSizeSp by remember { mutableStateOf(20) }

    val allBookHadiths = book.allHadiths
    val currentIndex = allBookHadiths.indexOfFirst { it.id == hadith.id }.coerceAtLeast(0)
    val hasPrev = currentIndex > 0
    val hasNext = currentIndex < allBookHadiths.size - 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(book.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${hadith.chapterTitle} • حديث ${hadith.numberInBook}", fontSize = 12.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font Size Adjuster
                    IconButton(onClick = { if (fontSizeSp > 16) fontSizeSp -= 2 }) {
                        Icon(Icons.Filled.TextDecrease, contentDescription = "تصغير الخط", tint = Color.Gray)
                    }
                    Text("${fontSizeSp}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (fontSizeSp < 32) fontSizeSp += 2 }) {
                        Icon(Icons.Filled.TextIncrease, contentDescription = "تكبير الخط", tint = Color.Gray)
                    }

                    // Bookmark
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "حفظ",
                            tint = Color(0xFFD4AF37)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(14.dp))

            // Narrator
            Text(
                text = "عن " + hadith.narrator,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(book.primaryColorHex)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Arabic Hadith Text
            Text(
                text = hadith.arabicText,
                fontSize = fontSizeSp.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = (fontSizeSp + 14).sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Card
            if (hadith.explanation.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("شرح ومعنى الحديث:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hadith.explanation,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reference
            Text(
                text = "تخريج الحديث: " + hadith.reference,
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Navigation Between Hadiths (Next / Previous)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (hasPrev) onNavigateHadith(allBookHadiths[currentIndex - 1]) },
                    enabled = hasPrev,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("الحديث السابق")
                }

                Button(
                    onClick = {
                        val clip = ClipData.newPlainText("Hadith", "${hadith.arabicText}\n[${hadith.reference}]")
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ الحديث الشريف", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ الحديث")
                }

                OutlinedButton(
                    onClick = { if (hasNext) onNavigateHadith(allBookHadiths[currentIndex + 1]) },
                    enabled = hasNext,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("الحديث التالي")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
