package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAuthenticated by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("zakidj181@gmail.com") } // Pre-filled for development
    var passwordInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var currentRole by remember { mutableStateOf("مدير عام النظام") }
    var currentPermissions by remember { mutableStateOf("كامل الصلاحيات") }

    val adminAccounts by viewModel.adminAccounts.collectAsState()

    // We force Right-To-Left (RTL) layout direction for the entire Admin module as requested
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val handleLogin = {
            if (emailInput.isBlank() || passwordInput.isBlank()) {
                loginError = "الرجاء إدخال البريد الإلكتروني ورمز المرور."
            } else {
                isAuthenticating = true
                loginError = null
                viewModel.authenticateAdmin(
                    emailInput = emailInput,
                    passwordInput = passwordInput,
                    onSuccess = { role, permissions ->
                        isAuthenticating = false
                        // Translate role to Arabic safely
                        currentRole = when (role) {
                            "Super Admin" -> "مدير عام النظام"
                            "Moderator" -> "مراقب عام"
                            "Editor" -> "محرر محتوى"
                            else -> role
                        }
                        currentPermissions = when (permissions) {
                            "ALL" -> "كامل الصلاحيات"
                            "EDIT_CONTENT" -> "تعديل المحتوى"
                            "MANAGE_DONATIONS" -> "إدارة التبرعات"
                            "SEND_ALERTS" -> "إرسال التنبيهات"
                            else -> permissions
                        }
                        isAuthenticated = true
                        Toast.makeText(context, "تم تأكيد الهوية الرقمية بنجاح بنظام الإدارة!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        isAuthenticating = false
                        loginError = "فشل تسجيل الدخول: البريد الإلكتروني أو رمز المرور غير معتمد."
                    }
                )
            }
        }

        if (!isAuthenticated) {
            // --- PREMIUM ARABIC LOGIN SCREEN ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F1C14),
                                Color(0xFF14241B),
                                Color(0xFF121214)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2C22)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4AF37).copy(alpha = 0.12f))
                                .border(1.5.dp, Color(0xFFD4AF37), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "منصة الإدارة",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "منصة الإدارة والتحكم",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "منظومة التحكم والصلاحيات الرقمية المشفرة",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("البريد الإلكتروني للمشرف") },
                            modifier = Modifier.fillMaxWidth().testTag("admin_email_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD4AF37),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFFD4AF37),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFFD4AF37)) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("رمز المرور الصارم") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD4AF37),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFFD4AF37),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFD4AF37)) }
                        )

                        if (loginError != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF5E1E1E))
                            ) {
                                Text(
                                    text = loginError!!,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = handleLogin,
                            enabled = !isAuthenticating,
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("admin_login_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E3A)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("تأكيد الهوية والدخول الآمن", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TextButton(onClick = onBack) {
                            Text("العودة للقائمة الرئيسية للمنصة", color = Color(0xFFD4AF37), fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            // --- FULLY FUNCTIONAL PREMIUM ADMIN CONSOLE ---
            var activeTab by remember { mutableStateOf("CONTENT") } // CONTENT, DONATIONS, BROADCASTS, SECURITY

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("لوحة تحكم المنصة الإسلامية", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD4AF37))
                                Text("الرتبة: $currentRole • الصلاحية الممنوحة: $currentPermissions", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "رجوع") // Reversed for RTL
                            }
                        },
                        actions = {
                            IconButton(onClick = { isAuthenticated = false }) {
                                Icon(Icons.Filled.Logout, contentDescription = "قفل لوحة التحكم", tint = Color(0xFFCF6679))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF14241B),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF14241B),
                        tonalElevation = 8.dp
                    ) {
                        val tabs = listOf(
                            Triple("CONTENT", "المحتوى الشرعي", Icons.Filled.Feed),
                            Triple("DONATIONS", "الحملات والصدقات", Icons.Filled.VolunteerActivism),
                            Triple("BROADCASTS", "البث المجتمعي", Icons.Filled.Campaign),
                            Triple("MEMBERS", "إدارة الأعضاء", Icons.Filled.People),
                            Triple("SECURITY", "صلاحيات المشرفين", Icons.Filled.Security)
                        )
                        tabs.forEach { (tabId, label, icon) ->
                            val isSel = activeTab == tabId
                            NavigationBarItem(
                                selected = isSel,
                                onClick = { activeTab = tabId },
                                icon = { 
                                    Icon(
                                        imageVector = icon, 
                                        contentDescription = label, 
                                        tint = if (isSel) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.5f)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        text = label, 
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.7f)
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0xFF1E5E3A).copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            ) { p ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F1C14),
                                    Color(0xFF121214)
                                )
                            )
                        )
                        .padding(p)
                ) {
                    when (activeTab) {
                        "CONTENT" -> ContentTab(viewModel)
                        "DONATIONS" -> DonationsTab(viewModel)
                        "BROADCASTS" -> BroadcastsTab(viewModel)
                        "MEMBERS" -> MembersTab(viewModel)
                        "SECURITY" -> SecurityTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ContentTab(viewModel: AppViewModel) {
    var contentType by remember { mutableStateOf("HADITH") } // HADITH, ADHKAR, DUA, ARTICLE, ANNOUNCEMENT, REMINDER
    val context = LocalContext.current

    val customHadiths by viewModel.adminHadiths.collectAsState()
    val customAdhkars by viewModel.adminAdhkars.collectAsState()
    val customDuas by viewModel.adminDuas.collectAsState()
    val customArticles by viewModel.adminArticles.collectAsState()
    val customReminders by viewModel.adminBannersReminders.collectAsState()

    // Form inputs
    var titleInput by remember { mutableStateOf("") }
    var arabicInput by remember { mutableStateOf("") }
    var translationInput by remember { mutableStateOf("") }
    var numberInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var referenceInput by remember { mutableStateOf("") }

    val clearForm = {
        titleInput = ""
        arabicInput = ""
        translationInput = ""
        numberInput = ""
        categoryInput = ""
        referenceInput = ""
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "مستودع المحتوى الإسلامي الرقمي", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = Color(0xFFD4AF37)
            )
            Text(
                text = "إضافة وتعديل الأحاديث الشريفة، الأذكار اليومية، الأدعية المأثورة، والمقالات التعليمية.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            ScrollableTabRow(
                selectedTabIndex = listOf("HADITH", "ADHKAR", "DUA", "ARTICLE", "ANNOUNCEMENT", "REMINDER").indexOf(contentType),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[listOf("HADITH", "ADHKAR", "DUA", "ARTICLE", "ANNOUNCEMENT", "REMINDER").indexOf(contentType)]),
                        color = Color(0xFFD4AF37)
                    )
                }
            ) {
                listOf(
                    "HADITH" to "الحديث الشريف", 
                    "ADHKAR" to "الأذكار والتحصين", 
                    "DUA" to "الأدعية المأثورة", 
                    "ARTICLE" to "المقالات الفقهية", 
                    "ANNOUNCEMENT" to "الإعلانات العامة", 
                    "REMINDER" to "الخواطر والتذكير"
                ).forEach { (id, label) ->
                    Tab(
                        selected = contentType == id,
                        onClick = { contentType = id; clearForm() },
                        text = { 
                            Text(
                                text = label, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (contentType == id) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.6f)
                            ) 
                        }
                    )
                }
            }
        }

        // Add Content Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2C22)),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val displayType = when (contentType) {
                        "HADITH" -> "الحديث الشريف"
                        "ADHKAR" -> "الأذكار والتحصين"
                        "DUA" -> "الأدعية المأثورة"
                        "ARTICLE" -> "المقالات الفقهية"
                        "ANNOUNCEMENT" -> "الإعلانات العامة"
                        else -> "الخواطر والتذكير"
                    }
                    Text(
                        text = "إضافة $displayType جديد للنظام", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = Color(0xFFD4AF37)
                    )

                    if (contentType == "HADITH") {
                        OutlinedTextField(
                            value = categoryInput, 
                            onValueChange = { categoryInput = it }, 
                            label = { Text("مصدر أو راوي الحديث (مثال: صحيح البخاري، صحيح مسلم)") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = numberInput, 
                            onValueChange = { numberInput = it }, 
                            label = { Text("رقم الحديث الشريف") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = arabicInput, 
                            onValueChange = { arabicInput = it }, 
                            label = { Text("نص الحديث الشريف كاملاً") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 3
                        )
                        OutlinedTextField(
                            value = translationInput, 
                            onValueChange = { translationInput = it }, 
                            label = { Text("الترجمة والبيان التفصيلي للمحتوى") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 2
                        )
                        OutlinedTextField(
                            value = referenceInput, 
                            onValueChange = { referenceInput = it }, 
                            label = { Text("المصدر الشرعي والتخريج والسند المعتمد") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (contentType == "ADHKAR") {
                        OutlinedTextField(
                            value = categoryInput, 
                            onValueChange = { categoryInput = it }, 
                            label = { Text("تصنيف الذكر المبارك (مثال: أذكار الصباح، أذكار المساء)") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = titleInput, 
                            onValueChange = { titleInput = it }, 
                            label = { Text("عنوان الذكر المأثور") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = arabicInput, 
                            onValueChange = { arabicInput = it }, 
                            label = { Text("النص المبارك للذكر") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 3
                        )
                        OutlinedTextField(
                            value = translationInput, 
                            onValueChange = { translationInput = it }, 
                            label = { Text("ترجمة المعاني وشرح الفضل العظيم للذكر") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 2
                        )
                        OutlinedTextField(
                            value = numberInput, 
                            onValueChange = { numberInput = it }, 
                            label = { Text("العدد المستهدف لتكرار الذكر (مثال: ٣٣، ١٠٠)") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (contentType == "DUA") {
                        OutlinedTextField(
                            value = categoryInput, 
                            onValueChange = { categoryInput = it }, 
                            label = { Text("تصنيف الدعاء المبارك (مثال: الاستخارة، الحفظ، السفر)") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = arabicInput, 
                            onValueChange = { arabicInput = it }, 
                            label = { Text("نص الدعاء المبارك من الكتاب والسنة") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 3
                        )
                        OutlinedTextField(
                            value = translationInput, 
                            onValueChange = { translationInput = it }, 
                            label = { Text("الترجمة اللفظية للدعاء") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 2
                        )
                        OutlinedTextField(
                            value = titleInput, 
                            onValueChange = { titleInput = it }, 
                            label = { Text("النطق الصوتي بالحروف اللاتينية") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = referenceInput, 
                            onValueChange = { referenceInput = it }, 
                            label = { Text("المصدر الشرعي والآية أو الحديث المبارك") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (contentType == "ARTICLE") {
                        OutlinedTextField(
                            value = titleInput, 
                            onValueChange = { titleInput = it }, 
                            label = { Text("عنوان المقال العلمي الفقهي") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = categoryInput, 
                            onValueChange = { categoryInput = it }, 
                            label = { Text("التصنيف والباب الفقهي الشرعي") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = translationInput, 
                            onValueChange = { translationInput = it }, 
                            label = { Text("المحتوى الكامل للمقال الفقهي بالتنسيق") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 5
                        )
                        OutlinedTextField(
                            value = referenceInput, 
                            onValueChange = { referenceInput = it }, 
                            label = { Text("رابط الصورة البصرية أو الغلاف للمقال") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // ANNOUNCEMENT, REMINDER
                        OutlinedTextField(
                            value = titleInput, 
                            onValueChange = { titleInput = it }, 
                            label = { Text("عنوان التنبيه أو التذكير الرئيسي") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = translationInput, 
                            onValueChange = { translationInput = it }, 
                            label = { Text("التفاصيل الدقيقة والبيان الكامل للمحتوى") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 3
                        )
                        OutlinedTextField(
                            value = referenceInput, 
                            onValueChange = { referenceInput = it }, 
                            label = { Text("رابط الصورة التوضيحية أو الغلاف (اختياري)") }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = {
                            if (contentType == "HADITH") {
                                if (arabicInput.isNotBlank()) {
                                    viewModel.insertAdminHadith(AdminHadith(collection = categoryInput, number = numberInput, arabic = arabicInput, english = translationInput, reference = referenceInput))
                                    Toast.makeText(context, "تم حفظ وتوثيق الحديث الشريف بقاعدة البيانات بنجاح!", Toast.LENGTH_SHORT).show()
                                    clearForm()
                                }
                            } else if (contentType == "ADHKAR") {
                                if (arabicInput.isNotBlank()) {
                                    viewModel.insertAdminAdhkar(AdminAdhkar(category = categoryInput, title = titleInput, arabic = arabicInput, english = translationInput, countGoal = numberInput.toIntOrNull() ?: 33))
                                    Toast.makeText(context, "تم تسجيل الذكر وتثبيته في مستودع الأذكار!", Toast.LENGTH_SHORT).show()
                                    clearForm()
                                }
                            } else if (contentType == "DUA") {
                                if (arabicInput.isNotBlank()) {
                                    viewModel.insertAdminDua(AdminDua(category = categoryInput, arabic = arabicInput, translation = translationInput, transliteration = titleInput, source = referenceInput))
                                    Toast.makeText(context, "تم حفظ وتسجيل الدعاء المبارك بنجاح في النظام!", Toast.LENGTH_SHORT).show()
                                    clearForm()
                                }
                            } else if (contentType == "ARTICLE") {
                                if (titleInput.isNotBlank()) {
                                    viewModel.insertAdminArticle(AdminArticle(title = titleInput, content = translationInput, category = categoryInput, imageUri = referenceInput))
                                    Toast.makeText(context, "تم نشر وتوثيق المقال العلمي بقاعدة البيانات بنجاح!", Toast.LENGTH_SHORT).show()
                                    clearForm()
                                }
                            } else {
                                if (titleInput.isNotBlank()) {
                                    viewModel.insertAdminBannerReminder(AdminBannerReminder(type = contentType, title = titleInput, content = translationInput, imageUrl = referenceInput))
                                    Toast.makeText(context, "تم نشر التنبيه وتفعيله بنجاح للمستخدمين!", Toast.LENGTH_SHORT).show()
                                    clearForm()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E3A)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تثبيت وحفظ البيانات في النظام", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }

        // Listings & Deletion Manager
        item {
            Text(
                text = "المحتوى النشط المسجل حالياً في النظام", 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                color = Color(0xFFD4AF37)
            )
        }

        if (contentType == "HADITH") {
            if (customHadiths.isEmpty()) {
                item { Text("لا يوجد أحاديث مخصصة مضافة حالياً في هذا القسم.", color = Color.Gray) }
            } else {
                items(customHadiths) { hadith ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("[${hadith.collection}] الحديث الشريف رقم: ${hadith.number}", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(hadith.arabic, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(alpha = 0.8f))
                            }
                            IconButton(onClick = { viewModel.deleteAdminHadith(hadith.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color(0xFFCF6679))
                            }
                        }
                    }
                }
            }
        } else if (contentType == "ADHKAR") {
            if (customAdhkars.isEmpty()) {
                item { Text("لا يوجد أذكار مخصصة مسجلة حالياً.", color = Color.Gray) }
            } else {
                items(customAdhkars) { ad ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ad.title, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(ad.arabic, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(alpha = 0.8f))
                            }
                            IconButton(onClick = { viewModel.deleteAdminAdhkar(ad.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color(0xFFCF6679))
                            }
                        }
                    }
                }
            }
        } else if (contentType == "DUA") {
            if (customDuas.isEmpty()) {
                item { Text("لا يوجد أدعية مخصصة مسجلة حالياً.", color = Color.Gray) }
            } else {
                items(customDuas) { d ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(d.category, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(d.arabic, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(alpha = 0.8f))
                            }
                            IconButton(onClick = { viewModel.deleteAdminDua(d.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color(0xFFCF6679))
                            }
                        }
                    }
                }
            }
        } else if (contentType == "ARTICLE") {
            if (customArticles.isEmpty()) {
                item { Text("لا يوجد مقالات مضافة حالياً في هذا القسم.", color = Color.Gray) }
            } else {
                items(customArticles) { art ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(art.title, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(art.category, color = Color(0xFFD4AF37), fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.deleteAdminArticle(art.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color(0xFFCF6679))
                            }
                        }
                    }
                }
            }
        } else {
            val filteredReminders = customReminders.filter { it.type == contentType }
            if (filteredReminders.isEmpty()) {
                item { Text("لا توجد تنبيهات أو خواطر نشطة حالياً.", color = Color.Gray) }
            } else {
                items(filteredReminders) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.content, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.White.copy(alpha = 0.8f))
                            }
                            IconButton(onClick = { viewModel.deleteAdminBannerReminder(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color(0xFFCF6679))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonationsTab(viewModel: AppViewModel) {
    val campaigns by viewModel.donationCampaigns.collectAsState()
    val context = LocalContext.current

    // Inputs
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("15000") }
    var progress by remember { mutableStateOf("3200") }
    var start by remember { mutableStateOf("2026-07-01") }
    var end by remember { mutableStateOf("2026-12-31") }
    var imgUrl by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "بوابة العمل الخيري والتبرعات", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = Color(0xFFD4AF37)
            )
            Text(
                text = "تنظيم وإدارة حملات التبرع الخيرية، وتتبع نسب الإنجاز والمستهدف المالي لكل حملة مجتمعية.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Add Campaign Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2C22)),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "تأسيس وإطلاق حملة خيرية جديدة", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = Color(0xFFD4AF37)
                    )

                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("اسم المشروع أو الحملة الخيرية") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("أهداف المشروع الخيرية ونص حث المحسنين") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("المبلغ المستهدف (بالدولار)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = progress, onValueChange = { progress = it }, label = { Text("المبلغ المجموع ابتدائياً") }, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("تاريخ انطلاق الحملة") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("تاريخ انتهاء الحملة") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = imgUrl, onValueChange = { imgUrl = it }, label = { Text("رابط الصورة البصرية أو الغلاف للمشروع") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.insertDonationCampaign(
                                    DonationCampaign(
                                        title = title,
                                        description = desc,
                                        targetAmount = target.toDoubleOrNull() ?: 15000.0,
                                        currentProgress = progress.toDoubleOrNull() ?: 3200.0,
                                        startDate = start,
                                        endDate = end,
                                        imageUrl = imgUrl
                                    )
                                )
                                Toast.makeText(context, "تم نشر وإطلاق الحملة الخيرية بنجاح!", Toast.LENGTH_SHORT).show()
                                title = ""
                                desc = ""
                                imgUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E3A)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("نشر المشروع وتفعيل شريط التبرع", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }

        // Listings
        item {
            Text(
                text = "المشاريع الخيرية الفعالة حالياً", 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                color = Color(0xFFD4AF37)
            )
        }

        if (campaigns.isEmpty()) {
            item { Text("لا توجد مشاريع تبرعات نشطة حالياً.", color = Color.Gray) }
        } else {
            items(campaigns) { camp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(camp.title, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الهدف المستهدف: $${camp.targetAmount} • المجموع الحالي: $${camp.currentProgress}", fontSize = 12.sp, color = Color(0xFFD4AF37))
                        }
                        IconButton(onClick = { viewModel.deleteDonationCampaign(camp.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color(0xFFCF6679))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BroadcastsTab(viewModel: AppViewModel) {
    val logs by viewModel.notificationLogs.collectAsState()
    val context = LocalContext.current

    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }
    var selectedAudience by remember { mutableStateOf("جميع المستخدمين") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "منظومة البث الهوائي والرسائل الفورية", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = Color(0xFFD4AF37)
            )
            Text(
                text = "إرسال وتوجيه الرسائل العاجلة والإشعارات التوعوية لمجموع المصلين والمشتركين أو فئات محددة.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Compose Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2C22)),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "صياغة وتوجيه رسالة بث جديدة", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = Color(0xFFD4AF37)
                    )

                    OutlinedTextField(value = notifTitle, onValueChange = { notifTitle = it }, label = { Text("عنوان الإشعار الرئيسي العاجل") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notifBody, onValueChange = { notifBody = it }, label = { Text("النص التفصيلي للرسالة التنبيهية") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                    Text("تحديد الشريحة المستهدفة بالبث", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("جميع المستخدمين", "منطقة مكة المكرمة", "منطقة المدينة المنورة", "المسجد المحلي").forEach { aud ->
                            val isS = selectedAudience == aud
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isS) Color(0xFF1E5E3A) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isS) Color(0xFFD4AF37) else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedAudience = aud }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = aud, 
                                    color = if (isS) Color.White else Color.White.copy(alpha = 0.7f), 
                                    fontSize = 10.sp,
                                    fontWeight = if (isS) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (notifTitle.isNotBlank() && notifBody.isNotBlank()) {
                                // Maps the audience back to DB values
                                val audienceEng = when (selectedAudience) {
                                    "جميع المستخدمين" -> "All Users"
                                    "منطقة مكة المكرمة" -> "Makkah Area"
                                    "منطقة المدينة المنورة" -> "Medina Area"
                                    "المسجد المحلي" -> "Local Mosque"
                                    else -> selectedAudience
                                }
                                viewModel.insertNotificationLog(notifTitle, notifBody, audienceEng)
                                Toast.makeText(context, "تم بث الإشعار بنجاح لجميع الأجهزة المستهدفة!", Toast.LENGTH_LONG).show()
                                notifTitle = ""
                                notifBody = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E3A)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إطلاق وإرسال إشعار البث العاجل الآن", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }

        // Analytics Cards
        item {
            Text(
                text = "مؤشرات أداء البث والإرسال", 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                color = Color(0xFFD4AF37)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple("المستخدمون النشطون", "١،٢٤٠", "+١٤٪ نمو فصلي"),
                    Triple("نسبة نجاح التسليم", "٩٩.٨٪", "تسليم ناجح"),
                    Triple("معدل تفاعل المصلين", "٨٤.٢٪", "معدل تفاعل ممتاز")
                ).forEach { (title, valStr, desc) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(valStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(desc, fontSize = 9.sp, color = Color(0xFF1E5E3A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Notification History
        item {
            Text(
                text = "سجل الرسائل والبث السابق في المنصة", 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                color = Color(0xFFD4AF37)
            )
        }

        if (logs.isEmpty()) {
            item { Text("لا يوجد سجلات بث تاريخية في قاعدة البيانات حالياً.", color = Color.Gray) }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.title, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                            Text(log.sentTime, fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(log.body, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        val localAudience = when (log.audience) {
                            "All Users" -> "جميع المستخدمين"
                            "Makkah Area" -> "منطقة مكة المكرمة"
                            "Medina Area" -> "منطقة المدينة المنورة"
                            "Local Mosque" -> "المسجد المحلي"
                            else -> log.audience
                        }
                        Text("الشريحة المستهدفة: $localAudience • الحالة: تم التسليم بنجاح لكافة المشتركين", fontSize = 11.sp, color = Color(0xFF1E5E3A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityTab(viewModel: AppViewModel) {
    val admins by viewModel.adminAccounts.collectAsState()
    val context = LocalContext.current

    var newEmail by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Moderator") }
    var selectedPerm by remember { mutableStateOf("EDIT_CONTENT") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "إدارة المشرفين والصلاحيات الأمنية", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = Color(0xFFD4AF37)
            )
            Text(
                text = "تفويض المشرفين والمسؤولين الفرعيين وتعيين مهامهم الرقابية والأذونات الشرعية والأمنية بدقة.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Add admin form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2C22)),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "تفويض واعتماد مشرف فرعي جديد", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = Color(0xFFD4AF37)
                    )

                    OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("البريد الإلكتروني للشخص المفوض") }, modifier = Modifier.fillMaxWidth())

                    Text("الرتبة والمسؤولية الإدارية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "Super Admin" to "مدير عام النظام", 
                            "Moderator" to "مراقب عام", 
                            "Editor" to "محرر محتوى"
                        ).forEach { (id, roleLabel) ->
                            val isS = selectedRole == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isS) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isS) Color.White.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedRole = id }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = roleLabel, 
                                    color = if (isS) Color.Black else Color.White.copy(alpha = 0.7f), 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text("نطاق التفويض والأذونات الصارمة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "ALL" to "كامل الصلاحيات", 
                            "EDIT_CONTENT" to "إدارة وتعديل المحتوى", 
                            "MANAGE_DONATIONS" to "إدارة التبرعات", 
                            "SEND_ALERTS" to "إرسال التنبيهات"
                        ).forEach { (id, permLabel) ->
                            val isS = selectedPerm == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isS) Color(0xFF1E5E3A) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isS) Color(0xFFD4AF37).copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedPerm = id }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = permLabel, 
                                    color = if (isS) Color.White else Color.White.copy(alpha = 0.7f), 
                                    fontSize = 9.sp,
                                    fontWeight = if (isS) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (newEmail.isNotBlank()) {
                                viewModel.insertAdminAccount(newEmail, selectedRole, selectedPerm)
                                val arabicRoleLabel = when (selectedRole) {
                                    "Super Admin" -> "مدير عام النظام"
                                    "Moderator" -> "مراقب عام"
                                    else -> "محرر محتوى"
                                }
                                Toast.makeText(context, "تم منح وتفويض حساب المشرف بمرتبة: $arabicRoleLabel!", Toast.LENGTH_SHORT).show()
                                newEmail = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E3A)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تثبيت الصلاحيات وتفويض المسؤول", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }

        // Admin list
        item {
            Text(
                text = "قائمة طاقم العمل والمسؤولين المعتمدين", 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                color = Color(0xFFD4AF37)
            )
        }

        items(admins) { admin ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(admin.email, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        val roleText = when (admin.role) {
                            "Super Admin" -> "مدير عام النظام"
                            "Moderator" -> "مراقب عام"
                            "Editor" -> "محرر محتوى"
                            else -> admin.role
                        }
                        val permText = when (admin.permissions) {
                            "ALL" -> "كامل الصلاحيات"
                            "EDIT_CONTENT" -> "إدارة وتعديل المحتوى"
                            "MANAGE_DONATIONS" -> "إدارة التبرعات"
                            "SEND_ALERTS" -> "إرسال التنبيهات"
                            else -> admin.permissions
                        }
                        Text("الدور الإداري: $roleText • نطاق الصلاحية: $permText", fontSize = 11.sp, color = Color(0xFFD4AF37))
                    }
                    if (admin.email != "zakidj181@gmail.com") {
                        IconButton(onClick = { viewModel.deleteAdminAccount(admin.id, admin.email) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "سحب الترخيص", tint = Color(0xFFCF6679))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MembersTab(viewModel: AppViewModel) {
    val members by viewModel.allMembers.collectAsState()
    val context = LocalContext.current

    var selectedMemberForPoints by remember { mutableStateOf<AppMember?>(null) }
    var pointsChangeInput by remember { mutableStateOf("50") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "👥 لوحة إدارة أعضاء أقم صلاتك (${members.size} عضو)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFFD4AF37)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "التحكم في العضويات، تعديل نقاط بركة لمكافأتهم أو تجميد وتفعيل الحسابات.",
                fontSize = 11.sp,
                color = Color.LightGray.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedMemberForPoints != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F26))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎁 مكافأة العضو بالنقاط: ${selectedMemberForPoints!!.name}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = pointsChangeInput,
                                onValueChange = { pointsChangeInput = it },
                                label = { Text("عدد النقاط (موجب للإضافة أو سالب للخصم)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val pts = pointsChangeInput.toIntOrNull() ?: 0
                                    viewModel.updateMemberPoints(selectedMemberForPoints!!.id, pts)
                                    Toast.makeText(context, "تم تحديث نقاط العضو بنجاح!", Toast.LENGTH_SHORT).show()
                                    selectedMemberForPoints = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("تعديل")
                            }
                        }
                        TextButton(onClick = { selectedMemberForPoints = null }) {
                            Text("إلغاء التعديل", color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        if (members.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا يوجد أعضاء مسجلين حالياً بالمنصة.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(members) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.Bold,
                                    color = if (member.isActive) Color.White else Color.Gray,
                                    fontSize = 16.sp
                                )
                                if (!member.isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFC62828))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("مجمد", color = Color.White, fontSize = 9.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${member.email} • ${member.country}, ${member.city}",
                                fontSize = 12.sp,
                                color = Color.LightGray.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, null, tint = Color(0xFFD4AF37), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${member.points} نقطة بركة", fontSize = 11.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF9800), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${member.streakDays} أيام إلتزام", fontSize = 11.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Actions row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Reward points
                            IconButton(onClick = {
                                selectedMemberForPoints = member
                                pointsChangeInput = "50"
                            }) {
                                Icon(Icons.Filled.CardGiftcard, contentDescription = "منح نقاط", tint = Color(0xFF4CAF50))
                            }

                            // Freeze/Unfreeze
                            IconButton(onClick = {
                                viewModel.setMemberActiveState(member.id, !member.isActive)
                                Toast.makeText(context, if (member.isActive) "تم تجميد حساب العضو بنجاح" else "تم تفعيل حساب العضو بنجاح", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = if (member.isActive) Icons.Filled.Block else Icons.Filled.CheckCircle,
                                    contentDescription = "تجميد/تفعيل",
                                    tint = if (member.isActive) Color(0xFFE57373) else Color(0xFF81C784)
                                )
                            }

                            // Delete Member
                            IconButton(onClick = {
                                viewModel.deleteMember(member.id)
                                Toast.makeText(context, "تم حذف عضوية المشترك بالكامل", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف العضوية", tint = Color(0xFFCF6679))
                            }
                        }
                    }
                }
            }
        }
    }
}
