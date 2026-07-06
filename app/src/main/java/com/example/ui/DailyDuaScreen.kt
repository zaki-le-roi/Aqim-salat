package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DailyDuaItem(
    val id: String,
    val category: String,
    val arabic: String,
    val translation: String,
    val transliteration: String,
    val source: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDuaScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val bookmarks by viewModel.bookmarks.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }

    val categories = if (lang == "ar") {
        listOf("All" to "الكل", "Morning" to "الصباح والمساء", "Protection" to "الحفظ والوقاية", "Forgiveness" to "الاستغفار", "Relief" to "الفرج واليسر")
    } else {
        listOf("All" to "All", "Morning" to "Morning & Evening", "Protection" to "Protection & Safety", "Forgiveness" to "Forgiveness", "Relief" to "Relief & Ease")
    }

    val duas = listOf(
        DailyDuaItem(
            id = "dua_1",
            category = "Morning",
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ.",
            translation = if (lang == "ar") "أصبحنا وأصبح الملك لله والحمد لله ولا إله إلا الله وحده لا شريك له" else "We have entered a new day and with it all dominion belongs to Allah, All praise is for Allah. None has the right to be worshipped except Allah alone, without partner.",
            transliteration = "Asbahna wa-asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lah.",
            source = "Sahih Muslim"
        ),
        DailyDuaItem(
            id = "dua_2",
            category = "Morning",
            arabic = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ.",
            translation = if (lang == "ar") "اللهم بك أصبحنا وبك أمسينا وبك نحيا وبك نموت وإليك النشور" else "O Allah, by Your leave we have entered the morning and by Your leave we have entered the evening, by Your leave we live and by Your leave we die, and unto You is the resurrection.",
            transliteration = "Allahumma bika asbahna, wa-bika amsayna, wa-bika nahya, wa-bika namutu, wa-ilaykan-nushur.",
            source = "Sunan Abi Dawud"
        ),
        DailyDuaItem(
            id = "dua_3",
            category = "Protection",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            translation = if (lang == "ar") "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم" else "In the Name of Allah, Who with His Name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
            transliteration = "Bismillahil-lazi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            source = "Sunan al-Tirmidhi"
        ),
        DailyDuaItem(
            id = "dua_4",
            category = "Protection",
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            translation = if (lang == "ar") "أعوذ بكلمات الله التامات من شر ما خلق" else "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
            transliteration = "A'uzu bi-kalimatillahit-tammati min sharri ma khalaq.",
            source = "Sahih Muslim"
        ),
        DailyDuaItem(
            id = "dua_5",
            category = "Forgiveness",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ.",
            translation = if (lang == "ar") "سيد الاستغفار: اللهم أنت ربي لا إله إلا أنت خلقتني وأنا عبدك وأنا على عهدك ووعدك ما استطعت" else "The Master Supplication of Forgiveness: O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant, and I am faithful to Your covenant and promise as much as I am able.",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mas-tata'tu.",
            source = "Sahih al-Bukhari"
        ),
        DailyDuaItem(
            id = "dua_6",
            category = "Relief",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ.",
            translation = if (lang == "ar") "اللهم إني أعوذ بك من الهم والحزن والعجز والكسل والبخل والجبن وضعل الدين وغلبة الرجال" else "O Allah, I seek refuge in You from anxiety and grief, weakness and laziness, miserliness and cowardice, the burden of debts and from being overpowered by men.",
            transliteration = "Allahumma inni a'uzu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal, wal-bukhli wal-jubn, wa dala'id-dayni wa ghalabatir-rijal.",
            source = "Sahih al-Bukhari"
        ),
        DailyDuaItem(
            id = "dua_7",
            category = "Relief",
            arabic = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ.",
            translation = if (lang == "ar") "لا إله إلا أنت سبحانك إني كنت من الظالمين" else "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin.",
            source = "Surah al-Anbya: 87"
        )
    )

    val filteredDuas = remember(selectedCategory) {
        if (selectedCategory == "All") duas else duas.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("dua_back_button")) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (lang == "ar") "الأدعية المأثورة" else if (lang == "tr") "Günlük Dualar" else "Daily Authentic Duas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Scrollable category tabs
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { (key, label) ->
                val isSelected = selectedCategory == key
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = key },
                    text = {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Duas Cards
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredDuas, key = { it.id }) { dua ->
                val isBookmarked = bookmarks.any { it.type == "DUA" && it.referenceId == dua.id }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD4AF37).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dua.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC59B27)
                                )
                            }

                            Text(
                                text = dua.source,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Arabic text (Centered and stylized)
                        Text(
                            text = dua.arabic,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Transliteration
                        Text(
                            text = dua.transliteration,
                            fontSize = 13.sp,
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Translation
                        Text(
                            text = dua.translation,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Row (Copy, Share, Favorite)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Dua Text", "${dua.arabic}\n\n${dua.translation}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, if (lang == "ar") "تم نسخ الدعاء!" else "Dua copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Dua Share")
                                        putExtra(Intent.EXTRA_TEXT, "${dua.arabic}\n\n${dua.translation}\n\nVia Aqim Salah App")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share via"))
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    viewModel.toggleBookmark(
                                        type = "DUA",
                                        referenceId = dua.id.toString(),
                                        title = "Dua - ${dua.source}",
                                        subtitle = dua.translation,
                                        arabicText = dua.arabic,
                                        translationText = dua.translation
                                    )
                                    if (isBookmarked) {
                                        Toast.makeText(context, if (lang == "ar") "تمت الإزالة من المفضلة" else "Removed from favorites", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, if (lang == "ar") "تم الحفظ في المفضلة!" else "Added to favorites!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Fav",
                                    tint = if (isBookmarked) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
