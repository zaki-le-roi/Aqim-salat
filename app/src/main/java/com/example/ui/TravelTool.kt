package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerCalculator
import java.util.Date
import java.util.TimeZone

data class TravelDestination(
    val name: String,
    val lat: Double,
    val lng: Double,
    val timeZoneId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelTool(
    viewModel: AppViewModel,
    lang: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationName by viewModel.locationName.collectAsState()
    
    // Fiqh Concession Form State
    var distanceInput by remember { mutableStateOf("95") } // Default: 95 KM (exceeds travel minimum of ~83km)
    var durationDaysInput by remember { mutableStateOf("3") } // Default: 3 Days (within 4 days limit)
    
    // Dhikr Counter State
    var travelDhikrIndex by remember { mutableStateOf(0) }
    val travelAdhkars = listOf(
        Pair("دعاء الركوب", "سبحان الذي سخر لنا هذا وما كنا له مقرنين، وإنا إلى ربنا لمنقلبون."),
        Pair("دعاء السفر", "اللهم إنا نسألك في سفرنا هذا البر والتقوى، ومن العمل ما ترضى، اللهم هون علينا سفرنا هذا واطوِ عنا بعده."),
        Pair("التكبير والتسبيح", "الله أكبر (عند الصعود مرتفعاً)، سبحان الله (عند الهبوط وادياً)."),
        Pair("دعاء النزول في مكان", "أعوذ بكلمات الله التامات من شر ما خلق.")
    )
    var currentDhikrCount by remember { mutableStateOf(0) }

    val destinations = remember {
        listOf(
            TravelDestination("مكة المكرمة", 21.4225, 39.8262, "Asia/Riyadh"),
            TravelDestination("المدينة المنورة", 24.4672, 39.6024, "Asia/Riyadh"),
            TravelDestination("دبي", 25.2048, 55.2708, "Asia/Dubai"),
            TravelDestination("لندن", 51.5074, -0.1278, "Europe/London"),
            TravelDestination("كوالالمبور", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
            TravelDestination("باريس", 48.8566, 2.3522, "Europe/Paris")
        )
    }
    var selectedDestination by remember { mutableStateOf(destinations.first()) }
    val travelTimes = remember(selectedDestination) {
        val offsetHours = TimeZone.getTimeZone(selectedDestination.timeZoneId).getOffset(Date().time) / 3600000.0
        PrayerCalculator.calculateTimes(selectedDestination.lat, selectedDestination.lng, offsetHours, Date(), PrayerCalculator.CalculationMethod.MWL, PrayerCalculator.Madhab.STANDARD)
    }

    // Fiqh FAQ Accordion States
    var faq1Expanded by remember { mutableStateOf(false) }
    var faq2Expanded by remember { mutableStateOf(false) }
    var faq3Expanded by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF2F5F8))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. HERO BANNER CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2C4A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flight,
                            contentDescription = null,
                            tint = Color(0xFFFFCA28),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "حقيبة المسافر الإلكترونية",
                        color = Color(0xFFFFCA28),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "دليلك ورفيقك الشرعي المتكامل طيلة فترة السفر والترحال",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // --- 2. TRAVEL DESTINATION PREVIEW ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✈️ اختيار وجهة السفر ومعاينة المواقيت",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F2C4A)
                    )
                    Text(
                        text = "اختر وجهة السفر لمعاينة مواقيت الصلاة فيها دون تغيير موقع جهازك الحالي:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        destinations.take(3).forEach { destination ->
                            val isSelected = selectedDestination.name == destination.name
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedDestination = destination
                                        Toast
                                            .makeText(
                                                context,
                                                "تم اختيار وجهة DESTINATION_NAME لمعاينة المواقيت.".replace("DESTINATION_NAME", destination.name),
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = destination.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF1976D2) else Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        destinations.drop(3).forEach { destination ->
                            val isSelected = selectedDestination.name == destination.name
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedDestination = destination
                                        Toast
                                            .makeText(
                                                context,
                                                "تم اختيار وجهة DESTINATION_NAME لمعاينة المواقيت.".replace("DESTINATION_NAME", destination.name),
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = destination.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF1976D2) else Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "الموقع الحالي المسجل: $locationName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "مواقيت DESTINATION_NAME".replace("DESTINATION_NAME", selectedDestination.name),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F2C4A),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "الفجر" to travelTimes.fajr,
                            "الظهر" to travelTimes.dhuhr,
                            "العصر" to travelTimes.asr,
                            "المغرب" to travelTimes.maghrib,
                            "العشاء" to travelTimes.isha
                        ).forEach { (name, time) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(7.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(time, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. COMBINING & SHORTENING CONCESSIONS CALCULATOR ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🕌 حاسبة رخص السفر (القصر والجمع)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F2C4A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = distanceInput,
                            onValueChange = { distanceInput = it },
                            label = { Text("مسافة السفر (كم)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = durationDaysInput,
                            onValueChange = { durationDaysInput = it },
                            label = { Text("مدة الإقامة (أيام)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation Logic based on Islamic Fiqh
                    val distance = distanceInput.toIntOrNull() ?: 0
                    val days = durationDaysInput.toIntOrNull() ?: 0

                    val isDistanceValid = distance >= 83 // minimum distance of travel (~83km)
                    val isDurationValid = days <= 4 // Republic of scholars limit for guest is 4 days or less

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDistanceValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (isDistanceValid) {
                                Text(
                                    text = "✅ تشرع لك رخص السفر بالكامل (قصر وجمع الصلاة):",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• الظهر: ركعتان قصرًا (يجوز جمعه مع العصر تقديمًا أو تأخيرًا)\n" +
                                            "• العصر: ركعتان قصرًا\n" +
                                            "• المغرب: ثلاث ركعات (لا تقصر، يجوز جمعه مع العشاء)\n" +
                                            "• العشاء: ركعتان قصرًا",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 16.sp
                                )
                                if (!isDurationValid) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ انتبه: إقامتك تزيد عن 4 أيام، تنقطع رخصة القصر بمجرد الوصول للوجهة والاستقرار فيها عند جمهور الفقهاء.",
                                        color = Color(0xFFC62828),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 14.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "❌ مسافة سفرك أقل من حد السفر الشرعي (83 كم):",
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "لا تشرع لك رخص القصر والجمع في هذه المسافة، ويجب عليك أداء الصلوات في مواقيتها كاملة بغير قصر.",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. TRAVEL SUPPLICATIONS & DHIKR WITH INTERACTIVE COUNTER ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📿 حصن المسافر (أدعية وأذكار السفر)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F2C4A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            travelDhikrIndex = (travelDhikrIndex - 1 + travelAdhkars.size) % travelAdhkars.size
                            currentDhikrCount = 0
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Prev")
                        }

                        Text(
                            text = travelAdhkars[travelDhikrIndex].first,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2),
                            fontSize = 14.sp
                        )

                        IconButton(onClick = {
                            travelDhikrIndex = (travelDhikrIndex + 1) % travelAdhkars.size
                            currentDhikrCount = 0
                        }) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = travelAdhkars[travelDhikrIndex].second,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300).copy(alpha = 0.2f))
                                    .border(2.dp, Color(0xFFFFB300), CircleShape)
                                    .clickable { currentDhikrCount++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$currentDhikrCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Text(
                                text = "اضغط للتكرار",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // --- 5. INTERACTIVE FIQH TRAVEL FAQ ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📝 فقه وأحكام الصلاة في السفر",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F2C4A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // FAQ 1
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { faq1Expanded = !faq1Expanded }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "متى يبدأ المسافر بقصر الصلاة وجمعها؟",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (faq1Expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        if (faq1Expanded) {
                            Text(
                                text = "يبدأ رخص السفر من قصر وجمع بمجرد مغادرة المسافر لبيان بلده السكني ومبانيها العامرة، ولا يجوز قصر الصلاة وهو في منزله قبل بدء ترحاله.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp, end = 24.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    // FAQ 2
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { faq2Expanded = !faq2Expanded }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "كيف أصلي في الطائرة أو قطار متحرك؟",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (faq2Expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        if (faq2Expanded) {
                            Text(
                                text = "يجب استقبال القبلة والقيام مع الركوع والسجود إن تيسر، وإلا صلى بحسب استطاعته وإيماءً برأسه للركوع والسجود، جاعلاً سجوده أخفض من ركوعه، ولا يدع الصلاة تخرج عن وقتها.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp, end = 24.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    // FAQ 3
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { faq3Expanded = !faq3Expanded }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "هل يشرع للمسافر صلاة النوافل والرواتب؟",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (faq3Expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        if (faq3Expanded) {
                            Text(
                                text = "السنة للمسافر ترك الرواتب التابعة للصلوات (كالظهر والعصر والمغرب والعشاء)، لكن يشرع له ركعتا الفجر وصلاة الوتر وقيام الليل والنوافل المطلقة بلا كراهة.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp, end = 24.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
