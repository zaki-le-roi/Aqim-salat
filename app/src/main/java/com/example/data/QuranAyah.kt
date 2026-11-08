package com.example.data

/** Arabic Quran verse data loaded from the bundled offline Uthmani text. */
data class QuranAyah(
    val numberInSurah: Int,
    val arabicText: String,
    val audioUrl: String
)
