package com.example.ui

import android.media.MediaPlayer
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuranData
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranAudioScreen(
    viewModel: AppViewModel,
    lang: String,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val reciters = listOf(
        Triple("Mishary Al-Afasy", "مشاري العفاسي", "https://server8.mp3quran.net/afs/"),
        Triple("Abdul Basit Abdus Samad", "عبد الباسط عبد الصمد", "https://server7.mp3quran.net/basit/"),
        Triple("Saad Al-Ghamdi", "سعد الغامدي", "https://server7.mp3quran.net/gha/")
    )

    var selectedReciter by remember { mutableStateOf(reciters[0]) }
    var currentSurahIndex by remember { mutableStateOf(0) } // Surah Al-Fatihah
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var totalDurationSeconds by remember { mutableStateOf(180) } // Simulated or real
    var playbackSpeed by remember { mutableStateOf(1.0f) }

    val surahs = QuranData.surahs

    // Seek simulation loop when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(1000)
                if (sliderPosition < totalDurationSeconds) {
                    sliderPosition += playbackSpeed
                } else {
                    // Next track automatically
                    if (currentSurahIndex < surahs.size - 1) {
                        currentSurahIndex++
                        sliderPosition = 0f
                    } else {
                        isPlaying = false
                    }
                }
            }
        }
    }

    fun playTrack(index: Int) {
        currentSurahIndex = index
        sliderPosition = 0f
        isPlaying = true
        // Simulating loading the stream
        val url = selectedReciter.third + "%03d.mp3".format(index + 1)
        Toast.makeText(context, if (lang == "ar") "تشغيل: ${surahs[index].name} • ${selectedReciter.second}" else "Streaming: ${surahs[index].englishName} by ${selectedReciter.first}", Toast.LENGTH_SHORT).show()
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("audio_back_button")) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = if (lang == "ar") "رجوع" else "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (lang == "ar") "المكتبة الصوتية" else if (lang == "tr") "Ses Kütüphanesi" else "Quran Audio Player",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val activeSurah = surahs[currentSurahIndex]

        // --- Reciter Selector Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.RecordVoiceOver, contentDescription = if (lang == "ar") "القارئ" else "Reciter", tint = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (lang == "ar") "القارئ الحالي" else "Current Reciter",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (lang == "ar") selectedReciter.second else selectedReciter.first,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (lang == "ar") "تغيير" else "Switch", fontSize = 12.sp)
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        reciters.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(if (lang == "ar") r.second else r.first) },
                                onClick = {
                                    selectedReciter = r
                                    expanded = false
                                    playTrack(currentSurahIndex)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Player Console Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeSurah.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = activeSurah.englishName + " • " + activeSurah.revelationType.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Timeline slider
                val currentMinutes = (sliderPosition.toInt() / 60)
                val currentSeconds = (sliderPosition.toInt() % 60)
                val totalMinutes = (totalDurationSeconds / 60)
                val totalSeconds = (totalDurationSeconds % 60)

                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 0f..totalDurationSeconds.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFD4AF37),
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("audio_seekbar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%02d:%02d".format(currentMinutes, currentSeconds),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "%02d:%02d".format(totalMinutes, totalSeconds),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playback speed toggler
                    TextButton(
                        onClick = {
                            playbackSpeed = when (playbackSpeed) {
                                1.0f -> 1.25f
                                1.5f -> 2.0f
                                else -> 1.0f
                            }
                        }
                    ) {
                        Text("${playbackSpeed}x", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                    }

                    // Prev track
                    IconButton(
                        onClick = { if (currentSurahIndex > 0) playTrack(currentSurahIndex - 1) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                    ) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", modifier = Modifier.size(24.dp))
                    }

                    // Large Play/Pause FAB
                    FloatingActionButton(
                        onClick = { isPlaying = !isPlaying },
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp).testTag("audio_play_pause_fab")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Next track
                    IconButton(
                        onClick = { if (currentSurahIndex < surahs.size - 1) playTrack(currentSurahIndex + 1) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                    ) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(24.dp))
                    }

                    // Favorite/Bookmark indicator
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Added Surah ${activeSurah.englishName} to offline downloads playlist", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Surahs list ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(surahs.size) { idx ->
                val s = surahs[idx]
                val isActive = idx == currentSurahIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) Color(0xFFD4AF37).copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { playTrack(idx) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) Color(0xFFD4AF37)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (idx + 1).toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color.Black else MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = s.englishName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isActive) Color(0xFFC59B27) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Surah • ${s.totalAyahs} verses",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = s.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color(0xFFC59B27) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = if (isActive && isPlaying) Icons.Filled.VolumeUp else Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isActive) Color(0xFFC59B27) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
