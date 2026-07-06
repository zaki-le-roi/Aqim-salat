package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicHistoryData
import com.example.data.HistoryEvent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnThisDayScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    // Find current date
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val currentMonth = calendar.get(Calendar.MONTH) + 1 // 0-indexed to 1-indexed

    // Format current date display
    val df = SimpleDateFormat("dd MMMM yyyy", if (lang == "ar") Locale("ar") else Locale.US)
    val formattedToday = df.format(Date())

    // Get today's events
    val todayEvents = remember(currentDay, currentMonth) {
        IslamicHistoryData.getEventsForDay(currentDay, currentMonth)
    }

    // Get all events
    val allEvents = remember {
        IslamicHistoryData.getAllEvents()
    }

    var showOnlyToday by remember { mutableStateOf(todayEvents.isNotEmpty()) }

    val displayedEvents = remember(showOnlyToday, todayEvents) {
        if (showOnlyToday) todayEvents else allEvents
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "ar") "حدث في مثل هذا اليوم" else "On This Day in History",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("on_this_day_back")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF004D40) // Beautiful Islamic deep emerald green
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF2F5F8))
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. DATE BANNER ---
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF00796B), Color(0xFF004D40))
                                )
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.History, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "التاريخ والذاكرة الإسلامية" else "Islamic History & Heritage",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formattedToday,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "ar") {
                                "مستوحى من أمجاد الأمة وتاريخ قوافل العظماء لحفظ الهوية والدين."
                            } else {
                                "Inspired by the triumphs and milestones of the Ummah to preserve our Islamic identity."
                            },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // --- 2. FILTER SWITCH SEGMENT ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Today's events Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showOnlyToday) Color(0xFF004D40) else Color.Transparent)
                            .clickable { showOnlyToday = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "ar") "أحداث اليوم (${todayEvents.size})" else "Today's Events (${todayEvents.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (showOnlyToday) Color.White else Color.Gray
                        )
                    }

                    // All events Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!showOnlyToday) Color(0xFF004D40) else Color.Transparent)
                            .clickable { showOnlyToday = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "ar") "جميع المحطات التاريخية" else "All Milestones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!showOnlyToday) Color.White else Color.Gray
                        )
                    }
                }
            }

            // No events fallback
            if (displayedEvents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.EventBusy, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (lang == "ar") "لا توجد أحداث مسجلة لهذا اليوم بالتحديد." else "No registered events for this specific day.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { showOnlyToday = false }) {
                                Text(
                                    text = if (lang == "ar") "تصفح جميع المحطات التاريخية" else "Browse all milestones",
                                    color = Color(0xFF004D40),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. EVENTS LIST ---
            items(displayedEvents, key = { it.id }) { event ->
                val refKey = "event_${event.id}"
                val isBookmarked = bookmarks.any { it.type == "ON_THIS_DAY" && it.referenceId == refKey }

                val displayTitle = if (lang == "ar") event.titleAr else event.titleEn
                val displayDesc = if (lang == "ar") event.descAr else event.descEn
                val displayCategory = if (lang == "ar") event.categoryAr else event.categoryEn

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF004D40).copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = displayCategory,
                                    color = Color(0xFF004D40),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Event Date indicators
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0F2F1))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${event.day}/${event.month}",
                                        color = Color(0xFF00796B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.toggleBookmark(
                                            type = "ON_THIS_DAY",
                                            referenceId = refKey,
                                            title = displayTitle,
                                            subtitle = "${event.yearG} م • ${event.yearH}",
                                            arabicText = event.descAr,
                                            translationText = event.descEn
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) Color(0xFFFFB300) else Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            text = displayTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Year Badge
                        Text(
                            text = if (lang == "ar") {
                                "سنة ${event.yearG} ميلادي • الموافق لـ ${event.yearH}"
                            } else {
                                "Year ${event.yearG} AD • Corresponding to ${event.yearH}"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFF1F3F5), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Description (Aligned Right if Arabic)
                        Text(
                            text = displayDesc,
                            fontSize = 13.5.sp,
                            color = Color(0xFF333333),
                            lineHeight = 22.sp,
                            textAlign = if (lang == "ar") TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
