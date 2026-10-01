package com.example.ui

import android.content.Intent
import com.example.BuildConfig
import android.net.Uri

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: AppViewModel,
    lang: String,
    initialTab: String = "FAQ", // "FAQ", "SUPPORT", "ABOUT_US", "PARTNERS", "OUR_APPS", "RATE_SHARE"
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(initialTab) }

    // State parameters
    var supportName by remember { mutableStateOf("") }
    var supportEmail by remember { mutableStateOf("") }
    var supportMsg by remember { mutableStateOf("") }
    var issueType by remember { mutableStateOf("عام / اقتراح") }
    
    // Rating star state
    var selectedStars by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            "FAQ" -> if (lang == "ar") "الأسئلة الشائعة" else "Frequently Asked Questions"
                            "SUPPORT" -> if (lang == "ar") "مركز الدعم الفني" else "Help & Technical Support"
                            "ABOUT_US" -> if (lang == "ar") "من نحن وقيمنا" else "About Us & Mission"
                            "PARTNERS" -> if (lang == "ar") "شركاؤنا والإعلان معنا" else "Partners & Advertisements"
                            "OUR_APPS" -> if (lang == "ar") "سلسلة تطبيقاتنا الدعوية" else "Our Islamic Sibling Apps"
                            "RATE_SHARE" -> if (lang == "ar") "التقييم ونشر التطبيق" else "Rate & Share App"
                            else -> if (lang == "ar") "مركز المساعدة" else "Support Center"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("support_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = if (lang == "ar") "رجوع" else "Back")
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
            // Tab row selector
            ScrollableTabRow(
                selectedTabIndex = when (selectedTab) {
                    "FAQ" -> 0
                    "SUPPORT" -> 1
                    "ABOUT_US" -> 2
                    "PARTNERS" -> 3
                    "OUR_APPS" -> 4
                    "RATE_SHARE" -> 5
                    else -> 0
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedTab == "FAQ",
                    onClick = { selectedTab = "FAQ" },
                    text = { Text(if (lang == "ar") "❓ الأسئلة الشائعة" else "FAQs") }
                )
                Tab(
                    selected = selectedTab == "SUPPORT",
                    onClick = { selectedTab = "SUPPORT" },
                    text = { Text(if (lang == "ar") "🛠️ الدعم الفني" else "Support") }
                )
                Tab(
                    selected = selectedTab == "ABOUT_US",
                    onClick = { selectedTab = "ABOUT_US" },
                    text = { Text(if (lang == "ar") "ℹ️ من نحن" else "About Us") }
                )
                Tab(
                    selected = selectedTab == "PARTNERS",
                    onClick = { selectedTab = "PARTNERS" },
                    text = { Text(if (lang == "ar") "🤝 الشركاء" else "Partners & Ads") }
                )
                Tab(
                    selected = selectedTab == "OUR_APPS",
                    onClick = { selectedTab = "OUR_APPS" },
                    text = { Text(if (lang == "ar") "📱 تطبيقاتنا" else "Sister Apps") }
                )
                Tab(
                    selected = selectedTab == "RATE_SHARE",
                    onClick = { selectedTab = "RATE_SHARE" },
                    text = { Text(if (lang == "ar") "🌟 النشر والتقييم" else "Rate & Share") }
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "SupportTabTransition",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    "FAQ" -> {
                        val faqs = listOf(
                            Pair(
                                if (lang == "ar") "كيف يقوم التطبيق بحساب مواقيت الصلاة؟" else "How does the app calculate prayer times?",
                                if (lang == "ar") "يستخدم التطبيق معادلات فلكية دقيقة بناءً على إحداثيات موقعك الجغرافي (خط الطول ودائرة العرض) وطريقة الحساب المختارة في الإعدادات مثل هيئة المساحة المصرية، رابطة العالم الإسلامي، أو أم القرى."
                                else "The app calculates exact prayer times dynamically based on your physical GPS coordinates and your preferred calculation method (e.g. MWL, Egypt Survey, Umm Al-Qura)."
                            ),
                            Pair(
                                if (lang == "ar") "هل يعمل تطبيق أقم صلاتك دون اتصال بالإنترنت؟" else "Does Aqim Salah work offline?",
                                if (lang == "ar") "نعم، كافة المميزات الأساسية كالأذان ومواقيت الصلاة والمصحف الشريف وقراءة الأذكار وحساب القبلة تعمل دون إنترنت بنسبة 100%. الإنترنت مطلوب فقط لتحديث موقعك لأول مرة وتصفح خرائط المساجد."
                                else "Yes, all core capabilities like prayer calculation, Athan, full Quran reading, Adhkar, and Qibla compass run 100% offline. Internet is only needed initially for location queries and loading live map."
                            ),
                            Pair(
                                if (lang == "ar") "كيف يمكنني تغيير صوت الأذان؟" else "How can I change the Athan voice?",
                                if (lang == "ar") "يمكنك الذهاب إلى شاشة الإعدادات، ثم اختيار 'حساب المواقيت والمذهب'. ستجد خيارات لتخصيص صوت أذان الفجر (مثل أذان المدينة المنورة) وصوت أذان الصلوات الأخرى (مثل أذان مكة الحرم المكي)."
                                else "Navigate to Settings, then click on 'Prayer Calculation' where you can fully customize the Athan voice for Fajr (e.g., Medina Athan) and standard prayers (e.g., Makkah Athan)."
                            ),
                            Pair(
                                if (lang == "ar") "ما هي نقاط البركة (Barakah Points) وكيف أستفيد منها؟" else "What are Barakah Points and how to use them?",
                                if (lang == "ar") "هي نظام تشجيعي يهدف لمساعدتك على الالتزام. تكسب النقاط عند توثيق صلواتك في وقتها، قراءة وردك من القرآن الكريم، مشاركة تذكيرات الخير في المجتمع، أو التصويت في الاستطلاعات وتظهر في قائمة المتصدرين."
                                else "It is an encouraging rewarding mechanism. You accumulate points by completing prayers, reading Quran, publishing reminders in the community forum, and voting on weekly polls."
                            )
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(faqs) { (q, a) ->
                                var expanded by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = q,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                contentDescription = if (lang == "ar") "توسيع" else "Expand",
                                                tint = Color(0xFFD4AF37)
                                            )
                                        }
                                        if (expanded) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = a,
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "SUPPORT" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                if (lang == "ar") "أرسل لنا استفسارك أو مشكلتك التقنية" else "Send your inquiry or technical bug report",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))

                                            OutlinedTextField(
                                                value = supportName,
                                                onValueChange = { supportName = it },
                                                label = { Text(if (lang == "ar") "الاسم الكريم" else "Your Name") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            OutlinedTextField(
                                                value = supportEmail,
                                                onValueChange = { supportEmail = it },
                                                label = { Text(if (lang == "ar") "البريد الإلكتروني" else "Email Address") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            OutlinedTextField(
                                                value = supportMsg,
                                                onValueChange = { supportMsg = it },
                                                label = { Text(if (lang == "ar") "وصف المشكلة أو الاقتراح" else "Detailed Description") },
                                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                     if (supportName.isBlank() || supportEmail.isBlank() || supportMsg.isBlank()) {
                                                         Toast.makeText(context, if (lang == "ar") "الرجاء ملء جميع الحقول المطلوبة" else "Please fill all required fields", Toast.LENGTH_SHORT).show()
                                                     } else {
                                                         val subject = "Aqim Salah Support - $issueType"
                                                         val body = "Name: $supportName\nEmail: $supportEmail\nIssue: $supportMsg"
                                                         val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"))
                                                         try {
                                                             context.startActivity(intent)
                                                         } catch (_: Exception) {
                                                             Toast.makeText(context, if (lang == "ar") "لا يوجد تطبيق بريد مثبت على الجهاز." else "No email application is installed.", Toast.LENGTH_LONG).show()
                                                         }
                                                     }
                                                 },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                                            ) {
                                                Icon(Icons.Filled.Send, contentDescription = if (lang == "ar") "فتح البريد" else "Open email")
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(if (lang == "ar") "فتح البريد لإرسال الطلب" else "Open Email to Send")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "ABOUT_US" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD4AF37).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Star, contentDescription = "Logo", tint = Color(0xFFD4AF37), modifier = Modifier.size(40.dp))
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            if (lang == "ar") "تطبيق أقم صلاتك الإسلامي" else "Aqim Salah Islamic Platform",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                             if (lang == "ar") "الإصدار ${BuildConfig.VERSION_NAME}" else "Version ${BuildConfig.VERSION_NAME}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = if (lang == "ar") "أقم صلاتك هي منصة دعوية متكاملة لا تهدف للربح المالي، بل لتسخير تكنولوجيا الهواتف لخدمة المسلمين في جميع أقطار الأرض، وتشجيعهم على الالتزام بالفرائض وسنن الهدى والتنافس في الطاعات، بعيداً عن التشويش والإعلانات المزعجة."
                                            else "Aqim Salah is a comprehensive, non-profit Islamic digital platform engineered to assist Muslims worldwide in adhering to daily prayers, reciting Holy Quran, maintaining Adhkar, and encouraging noble habits safely.",
                                            fontSize = 13.sp,
                                            lineHeight = 22.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (lang == "ar") "نسعى لتقديم أدوات إسلامية عملية وموثوقة دون ادعاءات غير موثقة."
                                            else "We aim to provide practical, trustworthy Islamic tools without unsupported claims.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD4AF37),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "PARTNERS" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    if (lang == "ar") "🤝 شركاء الخير والأوقاف" else "🤝 Waqf & Charity Partners",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                             item {
                                 Card(
                                     modifier = Modifier.fillMaxWidth(),
                                     shape = RoundedCornerShape(12.dp),
                                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                 ) {
                                     Column(modifier = Modifier.padding(16.dp)) {
                                         Text(if (lang == "ar") "لا توجد حالياً قائمة منشورة لشركاء موثقين داخل التطبيق." else "There is currently no published list of verified partners in the app.", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                         Spacer(modifier = Modifier.height(8.dp))
                                         Text(if (lang == "ar") "للاستفسار عن الشراكات أو الإعلان، استخدم عنوان التواصل الظاهر أدناه." else "For partnership or advertising inquiries, use the contact address shown below.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                     }
                                 }
                             }

                            items(partners) { (name, desc) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD4AF37).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Handshake, contentDescription = if (lang == "ar") "وقف" else "Waqf", tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            if (lang == "ar") "📢 الشراكات والإعلان" else "📢 Partnerships & Advertising",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFD4AF37)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            if (lang == "ar") "هل تملك مشروعاً تجارياً هادفاً أو مؤسسة إسلامية تود تعريف المجتمع بها؟ نوفر مساحات إعلانية نظيفة وموجهة خالية من المحتوى المخالف."
                                            else "Do you have a halal business or Islamic project? We offer clean, non-disruptive, highly targeted advertisement slots.",
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            if (lang == "ar") "للاستفسار تواصل معنا: ads@aqimsalah.org" else "For booking inquiries: ads@aqimsalah.org",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "OUR_APPS" -> {
                        val sisterApps = emptyList<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>>()

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (sisterApps.isEmpty()) {
                                item {
                                    Text(
                                        if (lang == "ar") "لا توجد برامج إضافية منشورة حالياً." else "No additional published apps are available yet.",
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            items(sisterApps) { (title, desc, icon) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFD4AF37).copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(icon, contentDescription = title, tint = Color(0xFFD4AF37), modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                        }

                                        Button(
                                            onClick = { Toast.makeText(context, if (lang == "ar") "سيتم توجيهك لمتجر التطبيقات لتنزيل التطبيق" else "Redirecting to Play Store...", Toast.LENGTH_SHORT).show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(if (lang == "ar") "تثبيت" else "Install", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "RATE_SHARE" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    if (false) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(Icons.Filled.Star, contentDescription = if (lang == "ar") "نجمة" else "Star", tint = Color(0xFFFFD700), modifier = Modifier.size(56.dp))
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                if (lang == "ar") "شكراً جزيلاً لتقييمك الطيب!" else "Thank you for your rating!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                if (lang == "ar") "تقييماتكم تساعدنا على البقاء وتطوير التطبيق وخدمة المزيد من المسلمين."
                                                else "Your kind feedback helps us improve and reach more Muslims worldwide.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                if (lang == "ar") "شاركنا تقييمك ودعمك للتطبيق" else "Rate and support Aqim Salah",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Rating Stars
                                            Row {
                                                for (i in 1..5) {
                                                    Icon(
                                                        imageVector = if (i <= selectedStars) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                        contentDescription = "Star $i",
                                                        tint = if (i <= selectedStars) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clickable { selectedStars = i }
                                                            .padding(4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = feedbackText,
                                                onValueChange = { feedbackText = it },
                                                placeholder = { Text(if (lang == "ar") "أضف كلمتك الطيبة أو ملاحظاتك هنا..." else "Add your feedback here...") },
                                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                     if (selectedStars > 0) {
                                                         val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                                                         val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                                         try { context.startActivity(marketIntent) } catch (_: Exception) { context.startActivity(webIntent) }
                                                     } else {
                                                         Toast.makeText(context, if (lang == "ar") "الرجاء تحديد النجوم أولاً" else "Please select a rating first", Toast.LENGTH_SHORT).show()
                                                     }
                                                 },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                                            ) {
                                                Text(if (lang == "ar") "إرسال التقييم" else "Submit Rating")
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.Share, contentDescription = if (lang == "ar") "مشاركة" else "Share", tint = Color(0xFFD4AF37), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            if (lang == "ar") "الأجر بالنشر والدعوة للخير" else "Share the App & Earn Rewards",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            if (lang == "ar") "قال محمد صلى الله عليه وسلم: 'من دلّ على خير فله مثل أجر فاعله'. انشر رابط تطبيق أقم صلاتك الإسلامي لأهلك وأصحابك واكسب مثل أجور صلاتهم وقراءتهم دون أن ينقص من أجورهم شيء."
                                            else "Muhammad, peace and blessings be upon him, said: 'Whoever guides to good has a reward like that of its doer.' Share this application with family and friends.",
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                val shareText = "تطبيق أقم صلاتك: مواقيت الصلاة والقرآن والأذكار وغيرها من الخدمات الإسلامية. https://play.google.com/store/apps/details?id=" + context.packageName
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, if (lang == "ar") "مشاركة التطبيق" else "Share app"))
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Filled.Share, contentDescription = if (lang == "ar") "مشاركة" else "Share")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (lang == "ar") "مشاركة التطبيق" else "Share App")
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
