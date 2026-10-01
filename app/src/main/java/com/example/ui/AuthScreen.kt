package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppMember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rewardPrefs = remember { context.getSharedPreferences("member_daily_rewards", android.content.Context.MODE_PRIVATE) }
    val rewardDay = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var prayerRewardClaimed by remember { mutableStateOf(rewardPrefs.getBoolean("prayer_$rewardDay", false)) }
    var adhkarRewardClaimed by remember { mutableStateOf(rewardPrefs.getBoolean("adhkar_$rewardDay", false)) }
    val loggedInMember by viewModel.loggedInMember.collectAsState()
    
    var isRegisterMode by remember { mutableStateOf(true) }
    
    // Form Inputs
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var countryInput by remember { mutableStateOf("مصر") }
    var cityInput by remember { mutableStateOf("القاهرة") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (lang == "ar") { if (loggedInMember != null) "بطاقة العضوية الرقمية" else "بوابة العضوية والإدارة" } else { if (loggedInMember != null) "Digital Membership Card" else "Membership & Administration" },
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = if (lang == "ar") "رجوع" else "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A1E33),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0A1E33), Color(0xFF152A42), Color(0xFF0D1B2A))
                        )
                    )
            ) {
                if (loggedInMember != null) {
                    // --- MEMBER DASHBOARD ---
                    val member = loggedInMember!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Golden Membership Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E354F)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "أقم صلاتك",
                                            color = Color(0xFFD4AF37),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (lang == "ar") "عضوية البركة الرقمية" else "Digital Barakah Membership",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = if (lang == "ar") "نجمة ذهبية" else "Gold Star",
                                        tint = Color(0xFFD4AF37),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = member.name,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = member.email,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Divider(color = Color(0xFFD4AF37).copy(alpha = 0.3f))

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (lang == "ar") "البلد والمدينة" else "Country & City",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${member.country} • ${member.city}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (lang == "ar") "مجموع نقاط البركة" else "Total Barakah Points",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.AutoAwesome,
                                                contentDescription = "points",
                                                tint = Color(0xFFD4AF37),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (lang == "ar") "${member.points} نقطة" else "${member.points} points",
                                                color = Color(0xFFD4AF37),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Welcome back messages and details
                        Text(
                            text = if (lang == "ar") "تقبل الله طاعاتكم وزادكم من فضله ونعيمه!" else "May Allah accept your deeds and increase you in His grace.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Interactive task to earn points
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3D30)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == "ar") "الورد اليومي للصلوات" else "Daily Prayer Log",
                                        color = Color(0xFF81C784),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (lang == "ar") "سجّل محافظتك على صلوات اليوم لنفسك، وتحصل على +50 نقطة داخل التطبيق مرة واحدة يوميًا." else "Log your daily prayers for your own tracking and earn +50 in-app points once per day.",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        if (!prayerRewardClaimed) {
                                            viewModel.updateMemberPoints(member.id, 50)
                                            rewardPrefs.edit().putBoolean("prayer_$rewardDay", true).apply()
                                            prayerRewardClaimed = true
                                            Toast.makeText(context, if (lang == "ar") "تم تسجيل الورد اليومي وإضافة 50 نقطة داخل التطبيق لهذا اليوم." else "Daily prayer log recorded and 50 in-app points added for today.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, if (lang == "ar") "تم تسجيل نقاط الصلوات داخل التطبيق لهذا اليوم بالفعل." else "Today's in-app prayer points have already been recorded.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (lang == "ar") "تسجيل" else "Record", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Daily Dhikr reward task
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == "ar") "تحدي الأذكار والسبحة" else "Adhkar & Tasbih Challenge",
                                        color = Color(0xFFFFB74D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (lang == "ar") "سجّل إكمالك لورد الأذكار أو التسبيح الصباحي والمسائي لتحصل على +30 نقطة داخل التطبيق مرة واحدة يوميًا." else "Log your completed morning/evening adhkar or tasbih to earn +30 in-app points once per day.",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        if (!adhkarRewardClaimed) {
                                            viewModel.updateMemberPoints(member.id, 30)
                                            rewardPrefs.edit().putBoolean("adhkar_$rewardDay", true).apply()
                                            adhkarRewardClaimed = true
                                            Toast.makeText(context, if (lang == "ar") "تم تسجيل ورد الأذكار وإضافة 30 نقطة داخل التطبيق لهذا اليوم." else "Daily adhkar log recorded and 30 in-app points added for today.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, if (lang == "ar") "تم تسجيل نقاط الأذكار داخل التطبيق لهذا اليوم بالفعل." else "Today's in-app adhkar points have already been recorded.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("تسجيل", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Logout Member
                        OutlinedButton(
                            onClick = {
                                viewModel.logoutMember()
                                Toast.makeText(context, if (lang == "ar") "تم تسجيل الخروج بنجاح." else "Signed out successfully.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373)),
                            border = BorderStroke(1.dp, Color(0xFFE57373)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (lang == "ar") "تسجيل الخروج من العضوية" else "Sign out of membership", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                } else {
                    // --- ANONYMOUS: SIGN UP / SIGN IN FORM ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4AF37).copy(alpha = 0.12f))
                                .border(1.5.dp, Color(0xFFD4AF37), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = if (lang == "ar") "عضوية" else "Membership",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (lang == "ar") { if (isRegisterMode) "عضوية البركة الرقمية" else "دخول الأعضاء للمنصة" } else { if (isRegisterMode) "Digital Barakah Membership" else "Member Sign In" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        Text(
                            text = if (lang == "ar") { if (isRegisterMode) "سجل مجاناً لتسجيل صلواتك ومتابعة تقدمك ونيل الجوائز!" else "أدخل بريدك الإلكتروني المعتمد للدخول الآمن لملفك الشخصي" } else { if (isRegisterMode) "Register free to track your prayers, progress and rewards!" else "Enter your verified email to securely access your profile." },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3147)),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (errorMessage != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = errorMessage!!,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (isRegisterMode) {
                                    // Name input
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it },
                                        label = { Text(if (lang == "ar") "الاسم الكريم" else "Full name") },
                                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFD4AF37)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFD4AF37),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFFD4AF37),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                // Email Input
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text(if (lang == "ar") "البريد الإلكتروني" else "Email") },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFFD4AF37)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFD4AF37),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedLabelColor = Color(0xFFD4AF37),
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text(if (lang == "ar") "كلمة المرور (8 أحرف على الأقل)" else "Password (at least 8 characters)") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFD4AF37)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFD4AF37),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedLabelColor = Color(0xFFD4AF37),
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                if (isRegisterMode) {
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Country
                                    OutlinedTextField(
                                        value = countryInput,
                                        onValueChange = { countryInput = it },
                                        label = { Text(if (lang == "ar") "الدولة" else "Country") },
                                        leadingIcon = { Icon(Icons.Filled.Public, contentDescription = null, tint = Color(0xFFD4AF37)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFD4AF37),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFFD4AF37),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // City
                                    OutlinedTextField(
                                        value = cityInput,
                                        onValueChange = { cityInput = it },
                                        label = { Text(if (lang == "ar") "المدينة (لتعديل المواقيت تلقائياً)" else "City (for prayer-time adjustment)") },
                                        leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFFD4AF37)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFD4AF37),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFFD4AF37),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Main Action Button
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        if (isRegisterMode) {
                                            viewModel.registerMember(
                                                name = nameInput,
                                                email = emailInput,
                                                password = passwordInput,
                                                country = countryInput,
                                                city = cityInput,
                                                onSuccess = {
                                                    Toast.makeText(context, if (lang == "ar") "أهلاً بك! تم إنشاء عضويتك، وأضيفت 150 نقطة داخل التطبيق." else "Welcome! Your membership was created, and 150 in-app points were added.", Toast.LENGTH_LONG).show()
                                                },
                                                onFailure = { err -> errorMessage = err }
                                            )
                                        } else {
                                            viewModel.loginMember(
                                                email = emailInput,
                                                password = passwordInput,
                                                onSuccess = {
                                                    Toast.makeText(context, if (lang == "ar") "تم تسجيل دخولك بنجاح. مرحباً بعودتك!" else "Signed in successfully. Welcome back!", Toast.LENGTH_SHORT).show()
                                                },
                                                onFailure = { err -> errorMessage = err }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E3A)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (lang == "ar") { if (isRegisterMode) "إنشاء عضوية جديدة بنيل 150 نقطة" else "دخول العضوية الآمن" } else { if (isRegisterMode) "Create membership & get 150 points" else "Secure member sign in" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Toggle sign-in / sign-up mode
                                TextButton(
                                    onClick = { isRegisterMode = !isRegisterMode }
                                ) {
                                    Text(
                                        text = if (lang == "ar") { if (isRegisterMode) "لديك عضوية بالفعل؟ سجل دخولك" else "ليس لديك عضوية؟ سجل عضواً جديداً مجاناً" } else { if (isRegisterMode) "Already a member? Sign in" else "No membership? Register for free" },
                                        color = Color(0xFFD4AF37),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Admin Portal Switch Button (as requested by user!)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAdmin() },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                            border = BorderStroke(1.dp, Color(0xFFCF6679).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = if (lang == "ar") "الإدارة" else "Admin",
                                    tint = Color(0xFFCF6679)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ar") "بوابة المشرفين والإدارة والتحكم (للمدراء فقط)" else "Admin & Management Portal (authorized users only)",
                                    color = Color(0xFFCF6679),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
