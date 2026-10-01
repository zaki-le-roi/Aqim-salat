package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: AppViewModel,
    lang: String,
    onNavigateToQuran: ((Int) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val bookmarks by viewModel.bookmarks.collectAsState()
    val favoriteMosques by viewModel.favoriteMosques.collectAsState()

    var selectedTab by remember { mutableStateOf("QURAN") }

    val quranBookmarks = remember(bookmarks) { bookmarks.filter { it.type == "QURAN" } }
    val hadithBookmarks = remember(bookmarks) { bookmarks.filter { it.type == "HADITH" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("favorites_back_button")) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (lang == "ar") "المفضلة والمحفوظات" else if (lang == "tr") "Favorilerim" else "Favorites & Bookmarks",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Bookmark types selection tab
        TabRow(
            selectedTabIndex = when (selectedTab) {
                "QURAN" -> 0
                "HADITH" -> 1
                else -> 2
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTab == "QURAN",
                onClick = { selectedTab = "QURAN" },
                text = {
                    Text(
                        text = if (lang == "ar") "القرآن" else "Quran (${quranBookmarks.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == "HADITH",
                onClick = { selectedTab = "HADITH" },
                text = {
                    Text(
                        text = if (lang == "ar") "الحديث" else "Hadith (${hadithBookmarks.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == "MOSQUES",
                onClick = { selectedTab = "MOSQUES" },
                text = {
                    Text(
                        text = if (lang == "ar") "المساجد" else "Mosques (${favoriteMosques.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                "QURAN" -> {
                    if (quranBookmarks.isEmpty()) {
                        EmptyStatePlaceholder(
                            icon = Icons.Filled.BookmarkBorder,
                            text = if (lang == "ar") "لا توجد آيات محفوظة في المفضلة بعد." else "No bookmarked verses found yet."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(quranBookmarks, key = { it.id }) { b ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Extract surahId from referenceId if possible (e.g. "surah_1_ayah_2")
                                            val parts = b.referenceId.split("_")
                                            if (parts.size >= 2) {
                                                val surahId = parts[1].toIntOrNull() ?: 1
                                                onNavigateToQuran?.invoke(surahId)
                                            }
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = b.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFFD4AF37)
                                            )
                                            IconButton(
                                                onClick = { viewModel.toggleBookmark(b.type, b.referenceId, b.title, b.subtitle ?: "") },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = b.arabicText ?: "",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            lineHeight = 28.sp,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "HADITH" -> {
                    if (hadithBookmarks.isEmpty()) {
                        EmptyStatePlaceholder(
                            icon = Icons.Filled.BookmarkBorder,
                            text = if (lang == "ar") "لا توجد أحاديث محفوظة في المفضلة بعد." else "No bookmarked Hadiths found yet."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(hadithBookmarks, key = { it.id }) { b ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = b.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFFD4AF37)
                                            )
                                            IconButton(
                                                onClick = { viewModel.toggleBookmark(b.type, b.referenceId, b.title, b.subtitle ?: "") },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = b.arabicText ?: "",
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
                "MOSQUES" -> {
                    if (favoriteMosques.isEmpty()) {
                        EmptyStatePlaceholder(
                            icon = Icons.Filled.StarBorder,
                            text = if (lang == "ar") "لا توجد مساجد محفوظة في المفضلة بعد." else "No favorited mosques found yet."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(favoriteMosques, key = { it.id }) { m ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = m.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = { viewModel.deleteFavoriteMosque(m.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                            }
                                        }

                                        Text(
                                            text = m.address,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Button(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${m.latitude},${m.longitude}&mode=d"))
                                                    try { context.startActivity(intent) } catch (e: Exception) {}
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            ) {
                                                Icon(Icons.Filled.DirectionsCar, contentDescription = "Drive", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (lang == "ar") "سيارة" else "Drive", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${m.latitude},${m.longitude}&mode=w"))
                                                    try { context.startActivity(intent) } catch (e: Exception) {}
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            ) {
                                                Icon(Icons.Filled.DirectionsWalk, contentDescription = "Walk", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (lang == "ar") "مشياً" else "Walk", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
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
fun EmptyStatePlaceholder(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = if (lang == "ar") "لا توجد محفوظات" else "Empty", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )
    }
}
