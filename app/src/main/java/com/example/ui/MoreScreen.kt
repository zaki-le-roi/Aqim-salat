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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection

data class ServicesGridItem(
    val key: String,
    val titleAr: String,
    val icon: ImageVector,
    val tint: Color
)

private fun ServicesGridItem.localizedTitle(lang: String): String {
    if (lang == "ar") return titleAr
    return when (titleAr) {
        "الرئيسية" -> "Home"
        "الإعدادات" -> "Settings"
        "القبلة" -> "Qibla"
        "حاسبة الزكاة" -> "Zakat Calculator"
        "عمل اليوم والليلة" -> "Daily Deeds"
        "بنك الصدقات" -> "Sadaqah Bank"
        "الفوائد" -> "Benefits"
        "ورد المحاسبة" -> "Daily Accountability"
        "التقويم" -> "Calendar"
        "وجهة المسافر" -> "Travel Companion"
        "الصلاة حول العالم" -> "Prayer Times Worldwide"
        "الاستطلاعات" -> "Polls"
        "أصدقائي" -> "My Friends"
        "مساجد" -> "Mosques"
        "المطاعم الحلال" -> "Halal Food"
        "صحيح أقم صلاتك" -> "Aqim Salat Community"
        "حدث في مثل هذا اليوم" -> "On This Day"
        "متابعة الفجر" -> "Fajr Tracker"
        "رمضان" -> "Ramadan"
        "المصحف" -> "Quran"
        "العلامات المحفوظة", "صندوق العلامات" -> "Saved Bookmarks"
        "الأذكار" -> "Adhkar"
        "الأدعية" -> "Duas"
        "أسماء الله الحسنى" -> "Names of Allah"
        "الحديث", "الفوائد والحديث" -> "Benefits & Hadith"
        "طاعاتك", "أعمال اليوم والليلة" -> "Your Daily Deeds"
        "الدعاء" -> "Dua"
        "السيرة" -> "Seerah"
        "حصن المسلم" -> "Hisn Al-Muslim"
        "الأسئلة الشائعة" -> "FAQ"
        "الدعم الفني" -> "Technical Support"
        "عن نحن" -> "About Us"
        "انشر التطبيق" -> "Share App"
        "برامجنا" -> "Our Apps"
        "انشر التفاصيل" -> "Share Details"
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
    // الخدمات المعروضة هنا هي المسارات الفعلية الموجودة في التطبيق.
    val generalServices = listOf(
        ServicesGridItem("HOME", "الرئيسية", Icons.Filled.Home, Color.Unspecified),
        ServicesGridItem("SETTINGS", "الإعدادات", Icons.Filled.Settings, Color.Unspecified),
        ServicesGridItem("QIBLA", "القبلة", Icons.Filled.Explore, Color.Unspecified),
        ServicesGridItem("ZAKAT", "حاسبة الزكاة", Icons.Filled.Percent, Color.Unspecified),
        ServicesGridItem("DAILY_DUA", "عمل اليوم والليلة", Icons.Filled.WbTwilight, Color.Unspecified),
        ServicesGridItem("DONATIONS", "بنك الصدقات", Icons.Filled.VolunteerActivism, Color.Unspecified),
        ServicesGridItem("HADITH", "الفوائد والحديث", Icons.Filled.Lightbulb, Color.Unspecified),
        ServicesGridItem("ACCOUNTABILITY", "ورد المحاسبة", Icons.Filled.FactCheck, Color.Unspecified),
        ServicesGridItem("DEEDS", "أعمال اليوم والليلة", Icons.Filled.TaskAlt, Color.Unspecified),
        ServicesGridItem("CALENDAR", "التقويم", Icons.Filled.CalendarMonth, Color.Unspecified),
        ServicesGridItem("TRAVEL", "وجهة المسافر", Icons.Filled.Flight, Color.Unspecified),
        ServicesGridItem("WORLD_PRAYER_TIMES", "الصلاة حول العالم", Icons.Filled.Public, Color.Unspecified),
        ServicesGridItem("POLLS", "الاستطلاعات", Icons.Filled.BarChart, Color.Unspecified),
        ServicesGridItem("COMPETITION", "أصدقائي", Icons.Filled.Group, Color.Unspecified),
        ServicesGridItem("MOSQUES", "مساجد", Icons.Filled.Place, Color.Unspecified),
        ServicesGridItem("HALAL_FOOD", "المطاعم الحلال", Icons.Filled.Restaurant, Color.Unspecified),
        ServicesGridItem("COMMUNITY", "صحيح أقم صلاتك", Icons.Filled.Forum, Color.Unspecified),
        ServicesGridItem("ON_THIS_DAY", "حدث في مثل هذا اليوم", Icons.Filled.History, Color.Unspecified),
        ServicesGridItem("FAJR_LIST", "متابعة الفجر", Icons.Filled.Cloud, Color.Unspecified),
        ServicesGridItem("RAMADAN", "رمضان", Icons.Filled.NightsStay, Color.Unspecified)
    )

    val quranDhikrServices = listOf(
        ServicesGridItem("QURAN", "المصحف", Icons.Filled.MenuBook, Color.Unspecified),
        ServicesGridItem("FAVORITES", "صندوق العلامات", Icons.Filled.Bookmark, Color.Unspecified),
        ServicesGridItem("ADHKAR", "الأذكار", Icons.Filled.SelfImprovement, Color.Unspecified),
        ServicesGridItem("DAILY_DUA", "الأدعية", Icons.Filled.AutoAwesome, Color.Unspecified),
        ServicesGridItem("NAMES", "أسماء الله الحسنى", Icons.Filled.Favorite, Color.Unspecified),
        ServicesGridItem("HADITH", "الحديث", Icons.Filled.MenuBook, Color.Unspecified),
        ServicesGridItem("DEEDS", "طاعاتك", Icons.Filled.VolunteerActivism, Color.Unspecified),
        ServicesGridItem("DAILY_DUA", "الدعاء", Icons.Filled.Signpost, Color.Unspecified),
        ServicesGridItem("SEERAH", "السيرة", Icons.Filled.Campaign, Color.Unspecified),
        ServicesGridItem("HISN_AL_MUSLIM", "حصن المسلم", Icons.Filled.Shield, Color.Unspecified)
    )

    val supportServices = listOf(
        ServicesGridItem("FAQ", "الأسئلة الشائعة", Icons.Filled.QuestionMark, Color.Unspecified),
        ServicesGridItem("SUPPORT", "الدعم الفني", Icons.Filled.ContactSupport, Color.Unspecified),
        ServicesGridItem("ABOUT_US", "عن نحن", Icons.Filled.Info, Color.Unspecified),
        ServicesGridItem("RATE_SHARE", "انشر التطبيق", Icons.Filled.Share, Color.Unspecified),
        ServicesGridItem("OUR_APPS", "برامجنا", Icons.Filled.Apps, Color.Unspecified),
        ServicesGridItem("RATE_SHARE", "انشر التفاصيل", Icons.Filled.Share, Color.Unspecified),
        ServicesGridItem("RATE_SHARE", "قيم التطبيق", Icons.Filled.ThumbUp, Color.Unspecified),
        ServicesGridItem("RATE_SHARE", "تابعنا", Icons.Filled.AlternateEmail, Color.Unspecified),
        ServicesGridItem("PARTNERS", "شركاؤنا", Icons.Filled.Handshake, Color.Unspecified)
    )

    val currentLanguage by viewModel.language.collectAsState()
    var showLanguageMenu by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (currentLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
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
                        contentDescription = if (currentLanguage == "ar") "الرجوع" else "Back",
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
                    text = if (currentLanguage == "ar") { loggedInMember?.let { "أهلاً، ${it.name}" } ?: "تسجيل الدخول / التسجيل" } else { loggedInMember?.let { "Welcome, ${it.name}" } ?: "Sign in / Register" },
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
                    title = if (currentLanguage == "ar") "خدمات التطبيق العامة" else "General Services",
                    items = generalServices,
                    lang = currentLanguage,
                    onItemClick = onNavigateToFeature
                )
            }

            // Section 2: Quran and Dhikr Card
            item {
                ServicesSectionCard(
                    title = if (currentLanguage == "ar") "القرآن والأذكار" else "Quran & Adhkar",
                    items = quranDhikrServices,
                    lang = currentLanguage,
                    onItemClick = onNavigateToFeature
                )
            }

            // Section 3: Support and Interaction Card
            item {
                ServicesSectionCard(
                    title = if (currentLanguage == "ar") "الدعم والتفاعل" else "Support & Community",
                    items = supportServices,
                    lang = currentLanguage,
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
                                contentDescription = if (currentLanguage == "ar") "مميز" else "Premium",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == "ar") "أقم صلاتك المميز" else "Aqim Salat Premium",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentLanguage == "ar") "ميزات حصرية" else "Exclusive features",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Right Card: Saved Bookmarks صندوق العلامات
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToFeature("FAVORITES") },
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
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = if (currentLanguage == "ar") "صندوق العلامات" else "Saved Bookmarks",
                                tint = Color(0xFF0D6B4B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == "ar") "صندوق العلامات" else "Saved Bookmarks",
                                    color = Color(0xFF333333),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentLanguage == "ar") "العلامات المحفوظة" else "Your saved items",
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
}

@Composable
fun ServicesSectionCard(
    title: String,
    items: List<ServicesGridItem>,
    lang: String,
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
                                        contentDescription = item.localizedTitle(lang),
                                        tint = Color(0xFF0D5E34),
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(
                                    text = item.localizedTitle(lang),
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
