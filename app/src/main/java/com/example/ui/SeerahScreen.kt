package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class SeerahEntry(
    val yearAr: String,
    val yearEn: String,
    val titleAr: String,
    val titleEn: String,
    val bodyAr: String,
    val bodyEn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeerahScreen(lang: String, onBack: () -> Unit) {
    val entries = listOf(
        SeerahEntry("قبل البعثة", "Before Prophethood", "الميلاد والنشأة", "Birth and upbringing",
            "وُلد محمد صلى الله عليه وسلم في مكة ونشأ فيها، وعُرف بين قومه بالصدق والأمانة.",
            "Muhammad, peace be upon him, was born and raised in Makkah and was known among his people for truthfulness and trustworthiness."),
        SeerahEntry("610م", "610 CE", "بدء الوحي", "Beginning of revelation",
            "بدأ نزول الوحي عليه صلى الله عليه وسلم في غار حراء، وكانت أول ما نزل من القرآن آيات من سورة العلق.",
            "Revelation began in the Cave of Hira, with the opening verses of Surah Al-Alaq."),
        SeerahEntry("مكة", "Makkah", "الدعوة في مكة", "The Makkah period",
            "دعا صلى الله عليه وسلم إلى توحيد الله، وصبر على أذى المشركين وثبت على دعوته.",
            "He called to the worship of Allah alone and remained steadfast despite persecution."),
        SeerahEntry("622م", "622 CE", "الهجرة إلى المدينة", "Migration to Madinah",
            "هاجر صلى الله عليه وسلم إلى المدينة، وبدأت مرحلة جديدة من بناء المجتمع المسلم.",
            "He migrated to Madinah, beginning a new period of building the Muslim community."),
        SeerahEntry("المدينة", "Madinah", "بناء المجتمع", "Building the community",
            "أسس صلى الله عليه وسلم مجتمعًا يقوم على الإيمان والعبادة والتكافل والعدل.",
            "He established a community founded on faith, worship, mutual support and justice."),
        SeerahEntry("632م", "632 CE", "وفاته صلى الله عليه وسلم", "His passing",
            "توفي محمد صلى الله عليه وسلم في المدينة بعد أن بلّغ الرسالة وأدى الأمانة ونصح الأمة.",
            "He passed away in Madinah after conveying the message and fulfilling his mission.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == "ar") "السيرة" else "Seerah", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = if (lang == "ar") "رجوع" else "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Icon(Icons.Filled.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (lang == "ar") entry.yearAr else entry.yearEn,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lang == "ar") entry.titleAr else entry.titleEn,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = if (lang == "ar") entry.bodyAr else entry.bodyEn,
                            fontSize = 15.sp,
                            lineHeight = 26.sp,
                            textAlign = if (lang == "ar") TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
