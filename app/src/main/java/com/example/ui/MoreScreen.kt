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
import androidx.compose.ui.graphics.Brush
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
    val titleEn: String,
    val icon: ImageVector,
    val tint: Color
)

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
        ServicesGridItem("HOME", "الرئيسة", "Home", Icons.Filled.Home, Color(0xFF1E88E5)),
        ServicesGridItem("SETTINGS", "الإعدادات", "Settings", Icons.Filled.Settings, Color(0xFFFF9800)),
        ServicesGridItem("QIBLA", "القبلة", "Qibla", Icons.Filled.Explore, Color(0xFF009688)),
        ServicesGridItem("DONATIONS", "حاسبة الزكاة", "Zakat Calc", Icons.Filled.Percent, Color(0xFF0D47A1)),
        ServicesGridItem("DAILY_DUA", "عمل اليوم والليلة", "Deed Day/Night", Icons.Filled.WbTwilight, Color(0xFF00ACC1)),
        ServicesGridItem("DONATIONS", "بنك الصدقات", "Charity Bank", Icons.Filled.VolunteerActivism, Color(0xFF29B6F6)),
        ServicesGridItem("HADITH", "الفوائد", "Benefits", Icons.Filled.Lightbulb, Color(0xFFFFB300)),
        ServicesGridItem("RAMADAN", "ورد المحاسبة", "Accountability", Icons.Filled.FactCheck, Color(0xFF9C27B0)),
        ServicesGridItem("CALENDAR", "التقويم", "Calendar", Icons.Filled.CalendarMonth, Color(0xFFAB47BC)),
        ServicesGridItem("TRAVEL", "حقيبة المسافر", "Travel Companion", Icons.Filled.Flight, Color(0xFF42A5F5)),
        ServicesGridItem("PRAYER_TIMES", "الصلاة حول العالم", "Global Prayer", Icons.Filled.Public, Color(0xFF26A69A)),
        ServicesGridItem("POLLS", "استطلاعات", "Polls", Icons.Filled.BarChart, Color(0xFF5C6BC0)),
        ServicesGridItem("COMPETITION", "استباق", "Competition", Icons.Filled.Group, Color(0xFF26C6DA)),
        ServicesGridItem("MOSQUES", "مساجد", "Mosques", Icons.Filled.Place, Color(0xFF66BB6A)),
        ServicesGridItem("HALAL_FOOD", "المطاعم الحلال", "Halal Food", Icons.Filled.Restaurant, Color(0xFF9CCC65)),
        ServicesGridItem("COMMUNITY", "مجتمع أقم صلاتك", "Community", Icons.Filled.Forum, Color(0xFF4CAF50)),
        ServicesGridItem("ON_THIS_DAY", "حدث في مثل هذا اليوم", "On This Day", Icons.Filled.History, Color(0xFFFF7043)),
        ServicesGridItem("FAJR_LIST", "قائمة الفجر", "Fajr List", Icons.Filled.Cloud, Color(0xFF5C6BC0)),
        ServicesGridItem("RAMADAN", "رمضان", "Ramadan", Icons.Filled.NightsStay, Color(0xFFFF7043))
    )

    // 2. Quran and Dhikr List
    val quranDhikrServices = listOf(
        ServicesGridItem("QURAN", "المصحف", "Mushaf", Icons.Filled.MenuBook, Color(0xFF4CAF50)),
        ServicesGridItem("QURAN", "التحفيظ", "Memorization", Icons.Filled.Bookmark, Color(0xFF3F51B5)),
        ServicesGridItem("ADHKAR", "الأذكار", "Adhkar", Icons.Filled.SelfImprovement, Color(0xFF03A9F4)),
        ServicesGridItem("HADITH", "كنوز", "Treasures", Icons.Filled.AutoAwesome, Color(0xFFFF9800)),
        ServicesGridItem("TASBIH", "السبحة", "Tasbih", Icons.Filled.FormatListNumbered, Color(0xFF1E3A5F)),
        ServicesGridItem("RAMADAN", "طاعاتك", "Obedience", Icons.Filled.WorkspacePremium, Color(0xFF673AB7)),
        ServicesGridItem("RATE_SHARE", "الأجر بالنشر", "Share Reward", Icons.Filled.Spa, Color(0xFFE91E63)),
        ServicesGridItem("DAILY_DUA", "الدعاء", "Supplications", Icons.Filled.Signpost, Color(0xFF00BCD4)),
        ServicesGridItem("KHATMAH", "الختمة", "Khatmah", Icons.Filled.LibraryBooks, Color(0xFFE65100)),
        ServicesGridItem("HISN_AL_MUSLIM", "حصن المسلم", "Hisn Al Muslim", Icons.Filled.Shield, Color(0xFF4E342E))
    )

    // 3. Support and Interaction List
    val supportServices = listOf(
        ServicesGridItem("FAQ", "الأسئلة الشائعة", "FAQ", Icons.Filled.QuestionMark, Color(0xFF78909C)),
        ServicesGridItem("SUPPORT", "الدعم الفني", "Tech Support", Icons.Filled.ContactSupport, Color(0xFF455A64)),
        ServicesGridItem("ABOUT_US", "من نحن", "About Us", Icons.Filled.Info, Color(0xFF37474F)),
        ServicesGridItem("PARTNERS", "اعلن معنا", "Advertise", Icons.Filled.Campaign, Color(0xFF546E7A)),
        ServicesGridItem("OUR_APPS", "برامجنا", "Our Apps", Icons.Filled.Apps, Color(0xFF5E35B1)),
        ServicesGridItem("RATE_SHARE", "انشر التطبيق", "Share App", Icons.Filled.Share, Color(0xFF039BE5)),
        ServicesGridItem("RATE_SHARE", "قيم التطبيق", "Rate App", Icons.Filled.ThumbUp, Color(0xFFFFB300)),
        ServicesGridItem("RATE_SHARE", "تابعنا", "Follow Us", Icons.Filled.AlternateEmail, Color(0xFF26A69A)),
        ServicesGridItem("PARTNERS", "شركاؤنا", "Partners", Icons.Filled.Handshake, Color(0xFF8D6E63))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5F8)) // Matching clean blue/grey body background
    ) {
        // --- IMMERSIVE SOLID DEEP BLUE HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A1E33)) // Solid matching deep blue
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left elements: Chevron + Mail
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onNavigateToFeature("HOME") }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Mail,
                        contentDescription = "Mail",
                        tint = Color(0xFFFFCA28), // Golden mail icon
                        modifier = Modifier.size(22.dp)
                    )
                }

                val loggedInMember by viewModel.loggedInMember.collectAsState()

                // Centered "Login" or "More Services" title in Arabic
                Text(
                    text = loggedInMember?.let { if (lang == "ar") "أهلاً، ${it.name}" else "Welcome, ${it.name}" } 
                        ?: (if (lang == "ar") "تسجيل الدخول" else "Login / Register"),
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
                        contentDescription = "User profile",
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
                    title = if (lang == "ar") "خدمات التطبيق العامة" else "General Services",
                    items = generalServices,
                    lang = lang,
                    onItemClick = onNavigateToFeature
                )
            }

            // Section 2: Quran and Dhikr Card
            item {
                ServicesSectionCard(
                    title = if (lang == "ar") "القرآن والذكر" else "Quran & Dhikr",
                    items = quranDhikrServices,
                    lang = lang,
                    onItemClick = onNavigateToFeature
                )
            }

            // Section 3: Support and Interaction Card
            item {
                ServicesSectionCard(
                    title = if (lang == "ar") "الدعم والتفاعل" else "Support & Feedback",
                    items = supportServices,
                    lang = lang,
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
                            .clickable { onNavigateToFeature("SETTINGS") },
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
                                contentDescription = "Shield",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (lang == "ar") "أقم صلاتك المميز" else "Aqim Salah Premium",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (lang == "ar") "ميزات حصرية" else "Premium Features",
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
                            .clickable { onNavigateToFeature("RAMADAN") },
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
                                contentDescription = "Trophy",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (lang == "ar") "تحدي الطاعات" else "Obedience Challenge",
                                    color = Color(0xFF333333),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (lang == "ar") "حافظ على صلاتك" else "Keep your prayers",
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
            horizontalAlignment = if (lang == "ar") Alignment.End else Alignment.Start
        ) {
            // Category Title Header
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A5F),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Render items in groups of 4 (4-column grid layout)
            val chunked = items.chunked(4)
            chunked.forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Loop over 4 items in a row
                    for (i in 0 until 4) {
                        val item = rowItems.getOrNull(i)
                        if (item != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onItemClick(item.key) }
                                    .testTag("more_grid_item_${item.key}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(item.tint.copy(alpha = 0.12f))
                                        .border(0.5.dp, item.tint.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.titleAr,
                                        tint = item.tint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (lang == "ar") item.titleAr else item.titleEn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF444444),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            // Dummy spacing cell for grid alignment
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
