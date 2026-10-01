package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ServicesGridItem(
    val key: String,
    val titleAr: String,
    val icon: ImageVector,
    val tint: Color
)

private fun ServicesGridItem.localizedTitle(lang: String): String {
    if (lang == "ar") return titleAr
    return when (titleAr) {
        "الرئيسة", "الرئيسية" -> "Home"
        "الإعدادات" -> "Settings"
        "القبلة" -> "Qibla"
        "حاسبة الزكاة" -> "Zakat Calculator"
        "عمل اليوم والليلة" -> "Daily Dua"
        "بنك الصدقات" -> "Sadaqah"
        "الفوائد" -> "Benefits"
        "ورد المحاسبة" -> "Daily Accountability"
        "التقويم" -> "Calendar"
        "حقيبة المسافر" -> "Travel Companion"
        "الصلاة حول العالم" -> "Prayer Times Worldwide"
        "استطلاعات" -> "Polls"
        "استباق" -> "Al-Istibaq"
        "مساجد" -> "Nearby Mosques"
        "المطاعم الحلال" -> "Halal Food"
        "مجتمع أقم صلاتك" -> "Community"
        "حدث في مثل هذا اليوم" -> "On This Day"
        "قائمة الفجر" -> "Fajr Tracker"
        "رمضان" -> "Ramadan"
        "المصحف" -> "Quran"
        "التحفيظ" -> "Memorization"
        "الأذكار" -> "Adhkar"
        "كنوز" -> "Hadith"
        "السبحة" -> "Tasbih"
        "طاعاتك" -> "Good Deeds"
        "الأجر بالنشر" -> "Share for Reward"
        "الدعاء" -> "Dua"
        "الختمة" -> "Quran Khatmah"
        "حصن المسلم" -> "Hisn Al-Muslim"
        "الأسئلة الشائعة" -> "FAQ"
        "الدعم الفني" -> "Support"
        "من نحن" -> "About Us"
        "اعلن معنا" -> "Advertise With Us"
        "برامجنا" -> "Our Apps"
        "انشر التطبيق" -> "Share App"
        "قيم التطبيق" -> "Rate App"
        "تابعنا" -> "Follow Us"
        "شركاؤنا" -> "Partners"
        else -> titleAr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: AppViewModel,
    lang: String,
    onNavigateToFeature: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. General Services List
    val generalServices = listOf(
        ServicesGridItem("HOME", "الرئيسية", Icons.Filled.Home, Color(0xFF1E88E5)),
        ServicesGridItem("SETTINGS", "الإعدادات", Icons.Filled.Settings, Color(0xFFFF9800)),
        ServicesGridItem("QIBLA", "القبلة", Icons.Filled.Explore, Color(0xFF009688)),
        ServicesGridItem("ZAKAT", "حاسبة الزكاة", Icons.Filled.Percent, Color(0xFF0D47A1)),
        ServicesGridItem("DAILY_DUA", "عمل اليوم والليلة", Icons.Filled.WbTwilight, Color(0xFF00ACC1)),
        ServicesGridItem("DONATIONS", "بنك الصدقات", Icons.Filled.VolunteerActivism, Color(0xFF29B6F6)),
        ServicesGridItem("HADITH", "الفوائد", Icons.Filled.Lightbulb, Color(0xFFFFB300)),
        ServicesGridItem("ACCOUNTABILITY", "ورد المحاسبة", Icons.Filled.FactCheck, Color(0xFF9C27B0)),
        ServicesGridItem("CALENDAR", "التقويم", Icons.Filled.CalendarMonth, Color(0xFFAB47BC)),
        ServicesGridItem("TRAVEL", "حقيبة المسافر", Icons.Filled.Flight, Color(0xFF42A5F5)),
        ServicesGridItem("WORLD_PRAYER_TIMES", "الصلاة حول العالم", Icons.Filled.Public, Color(0xFF26A69A)),
        ServicesGridItem("POLLS", "استطلاعات", Icons.Filled.BarChart, Color(0xFF5C6BC0)),
        ServicesGridItem("COMPETITION", "استباق", Icons.Filled.Group, Color(0xFF26C6DA)),
        ServicesGridItem("MOSQUES", "مساجد", Icons.Filled.Place, Color(0xFF66BB6A)),
        ServicesGridItem("HALAL_FOOD", "المطاعم الحلال", Icons.Filled.Restaurant, Color(0xFF9CCC65)),
        ServicesGridItem("COMMUNITY", "مجتمع أقم صلاتك", Icons.Filled.Forum, Color(0xFF4CAF50)),
        ServicesGridItem("ON_THIS_DAY", "حدث في مثل هذا اليوم", Icons.Filled.History, Color(0xFFFF7043)),
        ServicesGridItem("FAJR_LIST", "قائمة الفجر", Icons.Filled.Cloud, Color(0xFF5C6BC0)),
        ServicesGridItem("RAMADAN", "رمضان", Icons.Filled.NightsStay, Color(0xFFFF7043))
    )

    // 2. Quran and Dhikr List
    val quranDhikrServices = listOf(
        ServicesGridItem("QURAN", "المصحف", Icons.Filled.MenuBook, Color(0xFF4CAF50)),
        ServicesGridItem("QURAN", "التحفيظ", Icons.Filled.Bookmark, Color(0xFF3F51B5)),
        ServicesGridItem("ADHKAR", "الأذكار", Icons.Filled.SelfImprovement, Color(0xFF03A9F4)),
        ServicesGridItem("HADITH", "كنوز", Icons.Filled.AutoAwesome, Color(0xFFFF9800)),
        ServicesGridItem("TASBIH", "السبحة", Icons.Filled.FormatListNumbered, Color(0xFF1E3A5F)),
        ServicesGridItem("DEEDS", "طاعاتك", Icons.Filled.WorkspacePremium, Color(0xFF673AB7)),
        ServicesGridItem("RATE_SHARE", "الأجر بالنشر", Icons.Filled.Spa, Color(0xFFE91E63)),
        ServicesGridItem("DAILY_DUA", "الدعاء", Icons.Filled.Signpost, Color(0xFF00BCD4)),
        ServicesGridItem("KHATMAH", "الختمة", Icons.Filled.LibraryBooks, Color(0xFFE65100)),
        ServicesGridItem("HISN_AL_MUSLIM", "حصن المسلم", Icons.Filled.Shield, Color(0xFF4E342E))
    )

    // 3. Support and Interaction List
    val supportServices = listOf(
        ServicesGridItem("FAQ", "الأسئلة الشائعة", Icons.Filled.QuestionMark, Color(0xFF78909C)),
        ServicesGridItem("SUPPORT", "الدعم الفني", Icons.Filled.ContactSupport, Color(0xFF455A64)),
        ServicesGridItem("ABOUT_US", "من نحن", Icons.Filled.Info, Color(0xFF37474F)),
        ServicesGridItem("PARTNERS", "اعلن معنا", Icons.Filled.Campaign, Color(0xFF546E7A)),
        ServicesGridItem("OUR_APPS", "برامجنا", Icons.Filled.Apps, Color(0xFF5E35B1)),
        ServicesGridItem("RATE_SHARE", "انشر التطبيق", Icons.Filled.Share, Color(0xFF039BE5)),
        ServicesGridItem("RATE_SHARE", "قيم التطبيق", Icons.Filled.ThumbUp, Color(0xFFFFB300)),
        ServicesGridItem("RATE_SHARE", "تابعنا", Icons.Filled.AlternateEmail, Color(0xFF26A69A)),
        ServicesGridItem("PARTNERS", "شركاؤنا", Icons.Filled.Handshake, Color(0xFF8D6E63))
    )

    val currentLanguage by viewModel.language.collectAsState()
    var showLanguageMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8F6))
    ) {
        // --- IMMERSIVE SOLID DEEP BLUE HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4F3A))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back + language selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "الرجوع",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onNavigateToFeature("HOME") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        Surface(
                            onClick = { showLanguageMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (currentLanguage == "ar") "العربية" else "English",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("العربية") },
                                onClick = {
                                    viewModel.setAppLanguage("ar")
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    viewModel.setAppLanguage("en")
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }

                val loggedInMember by viewModel.loggedInMember.collectAsState()

                // Centered User greeting or Login
                Text(
                    text = loggedInMember?.let { "أهلاً، ${it.name}" } ?: "تسجيل الدخول / التسجيل",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onNavigateToFeature("AUTH") }
                )

                // Right: Profile Avatar circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFFD700), CircleShape)
                        .clickable { onNavigateToFeature("AUTH") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (loggedInMember != null) Icons.Filled.AccountCircle else Icons.Filled.Person,
                        contentDescription = "الملف الشخصي",
                        tint = if (loggedInMember != null) Color(0xFFFFCA28) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // --- GROUPED SERVICES CARDS SCROLLABLE LIST ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: General Services Card
            item {
                ServicesSectionCard(
                    title = "خدمات التطبيق العامة",
                    items = generalServices,
                    onItemClick = onNavigateToFeature
                )
            }

            // Section 2: Quran and Dhikr Card
            item {
                ServicesSectionCard(
                    title = "القرآن والذكر",
                    items = quranDhikrServices,
                    onItemClick = onNavigateToFeature
                )
            }

            // Section 3: Support and Interaction Card
            item {
                ServicesSectionCard(
                    title = "الدعم والتفاعل",
                    items = supportServices,
                    onItemClick = onNavigateToFeature
                )
            }

            // Bottom Dual Promo Column Items
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Card: Golden Al-Mosally (أقم صلاتك المميز)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToFeature("PREMIUM") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D253F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "مميز",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "أقم صلاتك المميز",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ميزات حصرية",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Right Card: Salah Challenge (تحدي إقامة الصلاة)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToFeature("DEEDS") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "تحدي",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تحدي الطاعات",
                                    color = Color(0xFF333333),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "حافظ على صلاتك",
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServicesSectionCard(
    title: String,
    items: List<ServicesGridItem>,
    onItemClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Category Title Header
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A5F),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Render items in a clean 3-column grid.
            val chunked = items.chunked(3)
            chunked.forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    for (i in 0 until 3) {
                        val item = rowItems.getOrNull(i)
                        if (item != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onItemClick(item.key) }
                                    .testTag("more_grid_item_" + item.key),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0D5E34).copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.titleAr,
                                        tint = Color(0xFF0D5E34),
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(
                                    text = item.localizedTitle(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF20352B),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    minLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
