package com.example.ui

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPollsScreen(
    viewModel: AppViewModel,
    lang: String,
    initialTab: String = "COMMUNITY", // "COMMUNITY", "POLLS", "FAJR_LIST", "COMPETITION"
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) }

    // State collections
    val posts by viewModel.communityPosts.collectAsState()
    val polls by viewModel.communityPolls.collectAsState()
    val fajrRecords by viewModel.fajrRecords.collectAsState()
    val members by viewModel.allMembers.collectAsState()
    val loggedInMember by viewModel.loggedInMember.collectAsState()

    // New post input
    var postText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            "COMMUNITY" -> if (lang == "ar") "مجتمع أقم صلاتك التفاعلي" else "Aqim Salah Community"
                            "POLLS" -> if (lang == "ar") "الاستطلاعات واستطلاعات الرأي" else "Interactive Polls"
                            "FAJR_LIST" -> if (lang == "ar") "تحدي وقائمة صلاة الفجر" else "Fajr Prayer Challenge"
                            "COMPETITION" -> if (lang == "ar") "مسابقة استباق للخيرات" else "Al-Istibaq Leaderboard"
                            else -> if (lang == "ar") "المجتمع" else "Community"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("community_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Selector Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = when (selectedTab) {
                    "COMMUNITY" -> 0
                    "POLLS" -> 1
                    "FAJR_LIST" -> 2
                    "COMPETITION" -> 3
                    else -> 0
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedTab == "COMMUNITY",
                    onClick = { selectedTab = "COMMUNITY" },
                    text = { Text(if (lang == "ar") "💬 مجتمع الخير" else "Community") }
                )
                Tab(
                    selected = selectedTab == "POLLS",
                    onClick = { selectedTab = "POLLS" },
                    text = { Text(if (lang == "ar") "📊 الاستطلاعات" else "Polls") }
                )
                Tab(
                    selected = selectedTab == "FAJR_LIST",
                    onClick = { selectedTab = "FAJR_LIST" },
                    text = { Text(if (lang == "ar") "🌅 قائمة الفجر" else "Fajr Tracker") }
                )
                Tab(
                    selected = selectedTab == "COMPETITION",
                    onClick = { selectedTab = "COMPETITION" },
                    text = { Text(if (lang == "ar") "🏆 مسابقة استباق" else "Al-Istibaq") }
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "CommunityTabTransition",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    "COMMUNITY" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Write Post Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (lang == "ar") "انشر تذكيراً، آية أو حديثاً تشجع به إخوانك" else "Share a reminder or verse with brothers",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = postText,
                                            onValueChange = { postText = it },
                                            placeholder = { Text(if (lang == "ar") "اكتب تذكيرك الصالح هنا..." else "Write your reminder here...") },
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                if (postText.isNotBlank()) {
                                                    viewModel.addCommunityPost(postText)
                                                    postText = ""
                                                    Toast.makeText(context, if (lang == "ar") "تم النشر بنجاح وحصلت على +5 نقاط بركة!" else "Published successfully! +5 Barakah points", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, if (lang == "ar") "الرجاء كتابة شيء أولاً" else "Please write something first", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.End),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                                        ) {
                                            Icon(Icons.Filled.Send, contentDescription = "Send")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (lang == "ar") "انشر التذكير" else "Publish")
                                        }
                                    }
                                }
                            }

                            // Posts List
                            items(posts) { post ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.Person, contentDescription = "User", tint = Color(0xFFD4AF37))
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    Text(post.authorCountry, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                }
                                            }
                                            Text(
                                                text = if (lang == "ar") "منذ قليل" else "Recently",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            post.content,
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable { viewModel.likeCommunityPost(post.id) }
                                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                contentDescription = "Like",
                                                tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "${post.likesCount} ${if (lang == "ar") "تفاعل" else "reactions"}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "POLLS" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFD4AF37).copy(alpha = 0.08f))
                                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Leaderboard, contentDescription = "Stats", tint = Color(0xFFD4AF37), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                if (lang == "ar") "ساهم برأيك في توجيه التطبيق" else "Shape the app with your opinion",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFFD4AF37)
                                            )
                                            Text(
                                                if (lang == "ar") "استطلاعات تفاعلية حية، نتائج فورية بعد التصويت." else "Live interactive polls, results update instantly.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            items(polls) { poll ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (lang == "ar") poll.questionAr else poll.questionEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))

                                        val hasVoted = poll.votedOptionIndex != null
                                        val options = if (lang == "ar") poll.optionsAr else poll.optionsEn

                                        options.forEachIndexed { index, option ->
                                            val optionVotes = poll.votes[index]
                                            val percentage = if (poll.totalVotes > 0) (optionVotes * 100) / poll.totalVotes else 0
                                            val isSelected = poll.votedOptionIndex == index

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (hasVoted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    )
                                                    .clickable(enabled = !hasVoted) {
                                                        viewModel.voteInPoll(poll.id, index)
                                                        Toast.makeText(context, if (lang == "ar") "شكراً لمشاركتك! +10 نقاط بركة" else "Thank you for voting! +10 points", Toast.LENGTH_SHORT).show()
                                                    }
                                            ) {
                                                // Progress bar overlay when voted
                                                if (hasVoted) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(fraction = percentage.toFloat() / 100f)
                                                            .background(
                                                                if (isSelected) Color(0xFFD4AF37).copy(alpha = 0.18f)
                                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                            )
                                                            .align(Alignment.CenterStart)
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        if (isSelected) {
                                                            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = Color(0xFFD4AF37), modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                        }
                                                        Text(
                                                            option,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }

                                                    if (hasVoted) {
                                                        Text(
                                                            "$percentage% ($optionVotes)",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "${if (lang == "ar") "إجمالي الأصوات: " else "Total Votes: "}${poll.totalVotes}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "FAJR_LIST" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Fajr Header Status Card
                            item {
                                val completedCount = fajrRecords.count { it.status == "CONGREGATION" || it.status == "INDIVIDUAL" }
                                val streak = loggedInMember?.streakDays ?: 3

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD4AF37).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.WbTwilight, contentDescription = "Fajr", tint = Color(0xFFD4AF37), modifier = Modifier.size(36.dp))
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            if (lang == "ar") "برنامج ومتابعة صلاة الفجر" else "Fajr Prayer Tracker",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            if (lang == "ar") "المحافظة على الفجر في وقتها يفتح أبواب البركة والتوفيق لليوم كاملاً."
                                            else "Maintaining Fajr on time opens the doors of blessings for your entire day.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    "$completedCount/7",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 20.sp,
                                                    color = Color(0xFF4CAF50)
                                                )
                                                Text(if (lang == "ar") "صليت هذا الأسبوع" else "Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    "$streak ${if (lang == "ar") "أيام" else "days"}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 20.sp,
                                                    color = Color(0xFFD4AF37)
                                                )
                                                Text(if (lang == "ar") "الالتزام المتتالي" else "Streak Days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = if (lang == "ar") "سجل التزامك للأيام السبعة الماضية:" else "Log your commitment for the last 7 days:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                                )
                            }

                            // Days Tracker Cards
                            items(fajrRecords) { record ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                if (lang == "ar") record.dayNameAr else record.dayNameEn,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(record.dateString, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Status button: In Congregation
                                            IconButton(
                                                onClick = { viewModel.updateFajrRecord(record.dateString, "CONGREGATION") },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (record.status == "CONGREGATION") Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .border(
                                                        width = if (record.status == "CONGREGATION") 1.5.dp else 0.dp,
                                                        color = if (record.status == "CONGREGATION") Color(0xFF4CAF50) else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                            ) {
                                                Icon(Icons.Filled.Group, contentDescription = "Congregation", tint = if (record.status == "CONGREGATION") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                            }

                                            // Status button: Individual
                                            IconButton(
                                                onClick = { viewModel.updateFajrRecord(record.dateString, "INDIVIDUAL") },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (record.status == "INDIVIDUAL") Color(0xFF2196F3).copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .border(
                                                        width = if (record.status == "INDIVIDUAL") 1.5.dp else 0.dp,
                                                        color = if (record.status == "INDIVIDUAL") Color(0xFF2196F3) else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                            ) {
                                                Icon(Icons.Filled.Person, contentDescription = "Individual", tint = if (record.status == "INDIVIDUAL") Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                            }

                                            // Status button: Missed
                                            IconButton(
                                                onClick = { viewModel.updateFajrRecord(record.dateString, "MISSED") },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (record.status == "MISSED") Color(0xFFF44336).copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .border(
                                                        width = if (record.status == "MISSED") 1.5.dp else 0.dp,
                                                        color = if (record.status == "MISSED") Color(0xFFF44336) else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = "Missed", tint = if (record.status == "MISSED") Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "COMPETITION" -> {
                        val sortedMembers = members.sortedByDescending { it.points }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Info Banner
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            if (lang == "ar") "🏆 مسابقة استباق الأسبوعية" else "🏆 Weekly Al-Istibaq Race",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFFD4AF37)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (lang == "ar") "قال تعالى: {وَفِي ذَٰلِكَ فَلْيَتَنَافَسِ الْمُتَنَافِسُونَ}. تزداد بركتك ونقاطك مع كل عمل صالح أو قراءة قرآن أو تذكير تنشره في التطبيق."
                                            else "Gain barakah points by committing to prayers, reciting Quran, and sharing encouragement reminders.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            // Members List
                            items(sortedMembers.take(15)) { member ->
                                val index = sortedMembers.indexOf(member)
                                val rank = index + 1
                                val isCurrent = loggedInMember?.id == member.id

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrent) Color(0xFFD4AF37).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = if (isCurrent) borderStrokeSelected() else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Rank visual badge
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (rank) {
                                                            1 -> Color(0xFFFFD700) // Gold
                                                            2 -> Color(0xFFC0C0C0) // Silver
                                                            3 -> Color(0xFFCD7F32) // Bronze
                                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$rank",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (rank in 1..3) Color.Black else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = member.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "${member.city}, ${member.country}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${member.points}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = Color(0xFFD4AF37)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (lang == "ar") "نقطة" else "pts",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
}

@Composable
fun borderStrokeSelected() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = Color(0xFFD4AF37)
)
