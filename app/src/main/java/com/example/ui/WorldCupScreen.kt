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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class WorldCupMatch(
    val id: String,
    val homeTeamAr: String,
    val homeTeamEn: String,
    val awayTeamAr: String,
    val awayTeamEn: String,
    val homeFlag: String,
    val awayFlag: String,
    val matchTimeUtc: String, // e.g. "15:00" or "19:00"
    val stadiumAr: String,
    val stadiumEn: String,
    val stageAr: String,
    val stageEn: String,
    val dateStr: String // e.g. "2026-07-04"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldCupScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: () -> Unit
) {
    val times by viewModel.prayerTimes.collectAsState()
    val locationName by viewModel.locationName.collectAsState()

    // Predictions states
    var predictions by remember { mutableStateOf(mapOf<String, String>()) }

    val matches = listOf(
        WorldCupMatch(
            id = "wc_1",
            homeTeamAr = "البرازيل", homeTeamEn = "Brazil",
            awayTeamAr = "ألمانيا", awayTeamEn = "Germany",
            homeFlag = "🇧🇷", awayFlag = "🇩🇪",
            matchTimeUtc = "15:00",
            stadiumAr = "ملعب ميتلايف، نيوجيرسي", stadiumEn = "MetLife Stadium, New Jersey",
            stageAr = "ربع النهائي", stageEn = "Quarter-Final",
            dateStr = "2026-07-04"
        ),
        WorldCupMatch(
            id = "wc_2",
            homeTeamAr = "الأرجنتين", homeTeamEn = "Argentina",
            awayTeamAr = "إسبانيا", awayTeamEn = "Spain",
            homeFlag = "🇦🇷", awayFlag = "🇪🇸",
            matchTimeUtc = "19:00",
            stadiumAr = "ملعب أروهيد، كانساس سيتي", stadiumEn = "Arrowhead Stadium, Kansas City",
            stageAr = "ربع النهائي", stageEn = "Quarter-Final",
            dateStr = "2026-07-04"
        ),
        WorldCupMatch(
            id = "wc_3",
            homeTeamAr = "فرنسا", homeTeamEn = "France",
            awayTeamAr = "إنجلترا", awayTeamEn = "England",
            homeFlag = "🇫🇷", awayFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
            matchTimeUtc = "15:00",
            stadiumAr = "ملعب هارد روك، ميامي", stadiumEn = "Hard Rock Stadium, Miami",
            stageAr = "ربع النهائي", stageEn = "Quarter-Final",
            dateStr = "2026-07-05"
        ),
        WorldCupMatch(
            id = "wc_4",
            homeTeamAr = "المغرب", homeTeamEn = "Morocco",
            awayTeamAr = "الجزائر", awayTeamEn = "Algeria",
            homeFlag = "🇲🇦", awayFlag = "🇩🇿",
            matchTimeUtc = "19:00",
            stadiumAr = "ملعب روز بول، لوس أنجلوس", stadiumEn = "Rose Bowl, Los Angeles",
            stageAr = "ربع النهائي", stageEn = "Quarter-Final",
            dateStr = "2026-07-05"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "ar") "تغطية كأس العالم 2026" else "World Cup 2026 Coverage",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A121A) // Maroon World Cup color
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
            // --- 1. HERO BANNER ---
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A121A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "البطولة جارية الآن!" else "The Tournament is LIVE!",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "ar") "تغطية حية وتنسيق أوقات المباريات مع مواقيت الصلاة في بلدك لمنع أي تضارب." else "Live coverage and soccer matches schedule synchronized with your local prayer times.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // --- 2. INNOVATIVE PRAYER-MATCH CONFLICT PLANNER ---
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Timer, contentDescription = null, tint = Color(0xFF4A121A), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "منسق الصلوات والمباريات 🕌⚽" else "Prayer-Match Planner 🕌⚽",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E3A5F)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (lang == "ar") {
                                "موقعك الحالي: $locationName. نقوم بمطابقة مواعيد ربع النهائي مع أوقات صلواتك لمنع فوات الجماعة."
                            } else {
                                "Your Location: $locationName. We calculate overlaps between matches and your local prayers."
                            },
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Conflict checks
                        if (times != null) {
                            // Simple helper to check and render conflict
                            val conflicts = mutableListOf<String>()
                            
                            // Let's analyze 15:00 UTC (which is e.g. 16:00 in Algeria, 18:00 in Saudi Arabia)
                            // Standard timezone calculations for simplicity:
                            val cal = Calendar.getInstance()
                            val zoneOffsetHours = cal.timeZone.getOffset(Date().time) / 3600000

                            val m1HourLocal = (15 + zoneOffsetHours) % 24
                            val m2HourLocal = (19 + zoneOffsetHours) % 24

                            // Compare with Dhuhr, Asr, Maghrib, Isha
                            fun parseTimeToMins(t: String): Int {
                                val parts = t.split(":")
                                if (parts.size < 2) return 0
                                return parts[0].toInt() * 60 + parts[1].toInt()
                            }

                            val dhuhrMins = parseTimeToMins(times!!.dhuhr)
                            val asrMins = parseTimeToMins(times!!.asr)
                            val maghribMins = parseTimeToMins(times!!.maghrib)
                            val ishaMins = parseTimeToMins(times!!.isha)

                            val match1StartMins = m1HourLocal * 60
                            val match1EndMins = match1StartMins + 120

                            val match2StartMins = m2HourLocal * 60
                            val match2EndMins = match2StartMins + 120

                            // Check Match 1
                            if (match1StartMins <= asrMins && match1EndMins >= asrMins - 15) {
                                conflicts.add(
                                    if (lang == "ar") 
                                        "⚠️ تضارب: مباراة الساعة ${String.format("%02d:00", m1HourLocal)} تتزامن مع وقت صلاة العصر (${times!!.asr}). يفضل الصلاة جماعة قبل المتابعة!" 
                                    else 
                                        "⚠️ Conflict: Match at ${String.format("%02d:00", m1HourLocal)} overlaps with Asr Prayer (${times!!.asr}). Pray in congregation first!"
                                )
                            }
                            // Check Match 2
                            if (match2StartMins <= maghribMins && match2EndMins >= maghribMins - 15) {
                                conflicts.add(
                                    if (lang == "ar") 
                                        "⚠️ تضارب: مباراة الساعة ${String.format("%02d:00", m2HourLocal)} تتزامن مع وقت صلاة المغرب (${times!!.maghrib}). صلِّ في المسجد ثم تابع الشوط الثاني!" 
                                    else 
                                        "⚠️ Conflict: Match at ${String.format("%02d:00", m2HourLocal)} overlaps with Maghrib Prayer (${times!!.maghrib}). Pray at the mosque then catch the second half!"
                                )
                            } else if (match2StartMins <= ishaMins && match2EndMins >= ishaMins - 15) {
                                conflicts.add(
                                    if (lang == "ar") 
                                        "⚠️ تضارب: مباراة الساعة ${String.format("%02d:00", m2HourLocal)} تقترب من وقت صلاة العشاء (${times!!.isha})." 
                                    else 
                                        "⚠️ Conflict: Match at ${String.format("%02d:00", m2HourLocal)} is close to Isha Prayer (${times!!.isha})."
                                )
                            }

                            if (conflicts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (lang == "ar") "✅ رائع! لا تضارب مباشر اليوم بين مواعيد المباريات ومواعيد الصلاة الكبرى جماعة." else "✅ Awesome! No direct conflicts between major prayer congregation times and matches today.",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    conflicts.forEach { conflictText ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFFF3E0))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = conflictText,
                                                color = Color(0xFFE65100),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (lang == "ar") "جاري تحميل أوقات الصلاة للتحقق من التضارب..." else "Loading prayer times to calculate conflicts...",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // --- 3. MATCHES LIST AND PREDICTIONS ---
            item {
                Text(
                    text = if (lang == "ar") "مباريات ربع النهائي الجارية ومواعيدها" else "Quarter-Final Matches & Prediction Game",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A5F)
                )
            }

            items(matches) { match ->
                val cal = Calendar.getInstance()
                val zoneOffsetHours = cal.timeZone.getOffset(Date().time) / 3600000
                val originalHour = match.matchTimeUtc.split(":")[0].toInt()
                val localHour = (originalHour + zoneOffsetHours) % 24
                val localTimeStr = String.format("%02d:00", localHour)

                val displayStage = if (lang == "ar") match.stageAr else match.stageEn
                val homeTeam = if (lang == "ar") match.homeTeamAr else match.homeTeamEn
                val awayTeam = if (lang == "ar") match.awayTeamAr else match.awayTeamEn
                val displayStadium = if (lang == "ar") match.stadiumAr else match.stadiumEn

                val predictedWinner = predictions[match.id]

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
                            Text(
                                text = "$displayStage • ${match.dateStr}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4A121A).copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = localTimeStr,
                                    color = Color(0xFF4A121A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Teams duel visualizer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Home Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(match.homeFlag, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = homeTeam,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E3A5F),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // VS Node
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F3F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }

                            // Away Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(match.awayFlag, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = awayTeam,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E3A5F),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = displayStadium,
                            fontSize = 10.5.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Prediction interaction (completely real client state)
                        Column {
                            Text(
                                text = if (lang == "ar") "توقع الفائز بمكافأة الأجر:" else "Predict the winner:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val optionHomeSelected = predictedWinner == "HOME"
                                val optionAwaySelected = predictedWinner == "AWAY"

                                // Predict Home
                                Button(
                                    onClick = {
                                        predictions = predictions + (match.id to "HOME")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (optionHomeSelected) Color(0xFF4A121A) else Color(0xFFF1F3F5)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = homeTeam,
                                        color = if (optionHomeSelected) Color.White else Color(0xFF333333),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Predict Away
                                Button(
                                    onClick = {
                                        predictions = predictions + (match.id to "AWAY")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (optionAwaySelected) Color(0xFF4A121A) else Color(0xFFF1F3F5)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = awayTeam,
                                        color = if (optionAwaySelected) Color.White else Color(0xFF333333),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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
