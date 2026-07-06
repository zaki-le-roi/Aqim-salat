package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.example.data.HadithData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    var selectedTab by remember { mutableStateOf("Sahih al-Bukhari") }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("Sahih al-Bukhari", "Sahih Muslim", "Riyad as-Salihin", "40 Nawawi")

    // Filter hadiths by selected collection and search query
    val filteredHadiths = remember(selectedTab, searchQuery) {
        HadithData.hadiths.filter {
            it.collection == selectedTab && (
                    searchQuery.isBlank() ||
                            it.english.contains(searchQuery, ignoreCase = true) ||
                            it.arabic.contains(searchQuery) ||
                            it.number.contains(searchQuery)
                    )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("hadith_back_button")) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = Translations.get("hadith", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Scrollable Tab Row for collections
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth().testTag("hadith_tab_row")
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = if (tab == "Sahih al-Bukhari") "Bukhari"
                            else if (tab == "Sahih Muslim") "Muslim"
                            else if (tab == "Riyad as-Salihin") "Riyad"
                            else "Nawawi",
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("hadith_search_bar"),
            placeholder = { Text(Translations.get("search_hadith", lang)) },
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

        // Hadith Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredHadiths, key = { it.id }) { hadith ->
                val refKey = hadith.id
                val isBookmarked = bookmarks.any { it.type == "HADITH" && it.referenceId == refKey }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hadith_card_$refKey"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Card Header Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge with Hadith Number
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Hadith ${hadith.number}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Bookmark button
                            IconButton(
                                onClick = {
                                    viewModel.toggleBookmark(
                                        type = "HADITH",
                                        referenceId = refKey,
                                        title = "${hadith.collection} • Hadith ${hadith.number}",
                                        subtitle = hadith.english.take(50) + "...",
                                        arabicText = hadith.arabic,
                                        translationText = hadith.english
                                    )
                                },
                                modifier = Modifier.testTag("hadith_bookmark_button_$refKey")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Arabic Script
                        Text(
                            text = hadith.arabic,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // English Translation
                        Text(
                            text = hadith.english,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Citation Reference Footer
                        Text(
                            text = hadith.reference,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
