package com.example.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DonationCampaign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationsScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val campaigns by viewModel.donationCampaigns.collectAsState()

    var showContributionSheet by remember { mutableStateOf<DonationCampaign?>(null) }
    var contributionAmountInput by remember { mutableStateOf("50") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "ar") "تبرعات وصدقات" else "Philanthropy & Donations",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("donations_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = if (lang == "ar") "رجوع" else "Back")
                    }
                }
            )
        }
    ) { p ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(p)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Hero Card introducing donations
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E5E3A))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.VolunteerActivism,
                                    contentDescription = if (lang == "ar") "صدقة" else "Donation",
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (lang == "ar") "الصدقة تطفئ الخطيئة" else "Charity Extinguishes Sin",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (lang == "ar")
                                    "قال النبي ﷺ: «الصدقة تطفئ الخطيئة كما يطفئ الماء النار». ساهم في دعم بيوت الله ومراكز المجتمع الإسلامي."
                                else
                                    "The Prophet ﷺ said: 'Charity extinguishes sin as water extinguishes fire.' Support construction, food aid, and community services.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = if (lang == "ar") "الحملات النشطة" else "Active Philanthropy Campaigns",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (campaigns.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (lang == "ar") "لا توجد حملات صدقة نشطة حاليًا." else "No donation campaigns are active at the moment.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(campaigns) { campaign ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = campaign.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = campaign.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    lineHeight = 16.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Progress Info
                                val progressPct = (campaign.currentProgress / campaign.targetAmount).toFloat().coerceIn(0f, 1f)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (lang == "ar") "المجموع: ${String.format("%.2f", campaign.currentProgress)}" else "Raised: ${String.format("%.2f", campaign.currentProgress)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E5E3A)
                                    )
                                    Text(
                                        text = if (lang == "ar") "الهدف: ${String.format("%.2f", campaign.targetAmount)} (${String.format("%.0f", progressPct * 100)}%)" else "Goal: ${String.format("%.2f", campaign.targetAmount)} (${String.format("%.0f", progressPct * 100)}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD4AF37)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = progressPct,
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF1E5E3A),
                                    trackColor = Color.Gray.copy(alpha = 0.15f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lang == "ar") "تنتهي: ${campaign.endDate}" else "Ends: ${campaign.endDate}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    Button(
                                        onClick = { showContributionSheet = campaign },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (lang == "ar") "ساهم" else "Contribute", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contribution modal
            if (showContributionSheet != null) {
                val campaign = showContributionSheet!!
                AlertDialog(
                    onDismissRequest = { showContributionSheet = null },
                    confirmButton = {
                        Button(
                            onClick = {
                                Toast.makeText(context, if (lang == "ar") "الدفع الإلكتروني للصدقات غير مفعّل حالياً. لم يتم تسجيل أي تبرع أو زيادة في الحملة." else "Online donation payment is not enabled. No donation or campaign progress was recorded.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E3A))
                        ) {
                            Text(if (lang == "ar") "الدفع غير متاح حالياً" else "Payment Unavailable", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showContributionSheet = null }) {
                            Text(if (lang == "ar") "إلغاء" else "Cancel", color = Color.Gray)
                        }
                    },
                    title = { Text(if (lang == "ar") "المساهمة في الحملة" else "Contribute to Campaign", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(campaign.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (lang == "ar") "أدخل مبلغ الصدقة بالدولار ($):" else "Enter donation amount in USD ($):", fontSize = 12.sp)
                            OutlinedTextField(
                                value = contributionAmountInput,
                                onValueChange = { contributionAmountInput = it },
                                label = { Text(if (lang == "ar") "المبلغ ($)" else "Amount ($)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                )
            }
        }
    }
}
