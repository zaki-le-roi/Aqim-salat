package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdhkarData
import com.example.data.HadithData
import com.example.data.QuranData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: AppViewModel,
    lang: String,
    onNavigateToQuran: ((Int) -> Unit)? = null,
    onNavigateToAdhkar: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("QURAN") }

    // Filtered lists
    val quranResults = remember(query) {
        if (query.isBlank()) emptyList()
        else {
            QuranData.surahs.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.englishName.contains(query, ignoreCase = true) ||
                        it.id.toString() == query
            }
        }
    }

    val hadithResults = remember(query) {
        if (query.isBlank()) emptyList()
        else {
            HadithData.hadiths.filter {
                it.arabic.contains(query) ||
                        it.english.contains(query, ignoreCase = true) ||
                        it.collection.contains(query, ignoreCase = true) ||
                        it.number == query
            }
        }
    }

    val adhkarResults = remember(query) {
        if (query.isBlank()) emptyList()
        else {
            AdhkarData.adhkar.filter {
                it.text.contains(query) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.translation.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar / Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("search_back_button")) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = if (lang == "ar") "رجوع" else "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (lang == "ar") "البحث الموحد" else if (lang == "tr") "Genel Arama" else "Unified Islamic Search",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Beautiful Filled Search Bar
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .testTag("global_search_input"),
            placeholder = { Text(if (lang == "ar") "ابحث عن سور، أحاديث، أدعية..." else "Search Surahs, Hadiths, Adhkar...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = if (lang == "ar") "بحث" else "Search") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = if (lang == "ar") "مسح" else "Clear")
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

        // Group filters Tab row
        TabRow(
            selectedTabIndex = when (selectedGroup) {
                "QURAN" -> 0
                "HADITH" -> 1
                else -> 2
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedGroup == "QURAN",
                onClick = { selectedGroup = "QURAN" },
                text = {
                    Text(
                        text = if (lang == "ar") "القرآن" else "Quran (${quranResults.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedGroup == "HADITH",
                onClick = { selectedGroup = "HADITH" },
                text = {
                    Text(
                        text = if (lang == "ar") "الحديث" else "Hadith (${hadithResults.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedGroup == "ADHKAR",
                onClick = { selectedGroup = "ADHKAR" },
                text = {
                    Text(
                        text = if (lang == "ar") "الأذكار" else "Adhkar (${adhkarResults.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (query.isBlank()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.YoutubeSearchedFor, contentDescription = if (lang == "ar") "النوع" else "Type", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (lang == "ar") "ابدأ بكتابة كلمة للبحث في القرآن والسنة والذكر" else "Enter a keyword above to lookup records from holy resources.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                when (selectedGroup) {
                    "QURAN" -> {
                        if (quranResults.isEmpty()) {
                            NoResultsFound()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(quranResults, key = { it.id }) { s ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToQuran?.invoke(s.id) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(s.englishName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(if (lang == "ar") "السورة ${s.id} • ${s.totalAyahs} آية" else "Surah ${s.id} • ${s.totalAyahs} Ayahs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            }
                                            Text(s.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "HADITH" -> {
                        if (hadithResults.isEmpty()) {
                            NoResultsFound()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(hadithResults, key = { it.id }) { h ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(h.collection, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD4AF37))
                                                Text(if (lang == "ar") "رقم ${h.number}" else "N° ${h.number}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = h.arabic,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                lineHeight = 22.sp,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = h.english,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "ADHKAR" -> {
                        if (adhkarResults.isEmpty()) {
                            NoResultsFound()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(adhkarResults, key = { it.id }) { a ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToAdhkar?.invoke() },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(a.category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD4AF37))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = a.text,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                lineHeight = 26.sp,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            if (a.translation.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(a.translation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
}

@Composable
fun NoResultsFound(lang: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.SearchOff, contentDescription = if (lang == "ar") "لا يوجد" else "None", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(if (lang == "ar") "لم يتم العثور على نتائج مطابقة." else "No matching results found.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 15.sp)
    }
}
