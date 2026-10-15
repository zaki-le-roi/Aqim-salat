package com.example.data

import android.content.Context
import org.json.JSONObject
import java.util.Locale

/**
 * Offline Arabic Quran text in Uthmani script.
 * Text is kept verbatim from the attributed bundled source in app/src/main/assets.
 */
object QuranOfflineData {
    private const val PART_COUNT = 15

    private val surahCache = java.util.concurrent.ConcurrentHashMap<Int, List<QuranApiClient.ApiAyah>>()

    fun loadSurah(context: Context, surahId: Int): List<QuranApiClient.ApiAyah>? {
        if (surahId !in 1..114) return null
        surahCache[surahId]?.let { return it }

        val part = ((surahId - 1) / 8) + 1
        if (part in 1..PART_COUNT) {
            loadPart(context.applicationContext, part)
        }
        return surahCache[surahId]
    }

    @Synchronized
    private fun loadPart(context: Context, part: Int) {
        val fileName = "quran_uthmani_part_" + String.format(Locale.US, "%02d", part) + ".json"
        try {
            val raw = context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
            val surahs = JSONObject(raw).getJSONArray("surahs")
            for (i in 0 until surahs.length()) {
                val surah = surahs.getJSONObject(i)
                val sId = surah.getInt("id")
                val verses = surah.getJSONArray("verses")
                val audioPrefix = String.format(Locale.US, "%03d", sId)
                val ayahs = ArrayList<QuranApiClient.ApiAyah>(verses.length())
                for (j in 0 until verses.length()) {
                    val verse = verses.getJSONObject(j)
                    val ayahNumber = verse.getInt("id")
                    val audioUrl = "https://everyayah.com/data/Alafasy_128kbps/" +
                        audioPrefix + String.format(Locale.US, "%03d", ayahNumber) + ".mp3"
                    ayahs.add(
                        QuranApiClient.ApiAyah(
                            numberInSurah = ayahNumber,
                            arabicText = verse.getString("text"),
                            translationText = "",
                            audioUrl = audioUrl
                        )
                    )
                }
                surahCache[sId] = ayahs
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }
}
