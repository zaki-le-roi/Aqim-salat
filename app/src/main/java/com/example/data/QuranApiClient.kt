package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object QuranApiClient {

    data class ApiAyah(
        val numberInSurah: Int,
        val arabicText: String,
        val translationText: String,
        val audioUrl: String
    )

    // Fetches Uthmani Arabic Quran text and the Arabic Tafsir al-Muyassar. Audio is streamed; no surah files are downloaded.
    suspend fun fetchSurah(surahId: Int, lang: String = "ar"): List<ApiAyah> = withContext(Dispatchers.IO) {
        val tafsirEdition = if (lang == "en") "en.asad" else "ar.muyassar"
        val urlString = "https://api.alquran.cloud/v1/surah/$surahId/editions/quran-uthmani,$tafsirEdition"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val dataArray = jsonResponse.getJSONArray("data")
                
                // Index 0: Arabic Text Edition
                // Index 1: English Translation Edition
                val arabicEdition = dataArray.getJSONObject(0)
                val translationEdition = dataArray.getJSONObject(1)

                val arabicAyahs = arabicEdition.getJSONArray("ayahs")
                val translationAyahs = translationEdition.getJSONArray("ayahs")

                val result = mutableListOf<ApiAyah>()
                for (i in 0 until arabicAyahs.length()) {
                    val aAyah = arabicAyahs.getJSONObject(i)
                    val tAyah = translationAyahs.getJSONObject(i)

                    val numInSurah = aAyah.getInt("numberInSurah")
                    val arabicText = aAyah.getString("text")
                    val translationText = tAyah.getString("text")

                    // Alquran.cloud has a standard audio stream URL pattern:
                    // https://cdn.alafasy.me/audios/128/<surahId_padded>/<ayahId_padded>.mp3
                    // For example: Surah 1 Ayah 1 -> https://everyayah.com/data/Alafasy_128kbps/001001.mp3
                    // Let's form a robust audio streaming URL using Alafasy's famous recitation:
                    val paddedSurah = String.format("%03d", surahId)
                    val paddedAyah = String.format("%03d", numInSurah)
                    val audioUrl = "https://everyayah.com/data/Alafasy_128kbps/$paddedSurah$paddedAyah.mp3"

                    result.add(
                        ApiAyah(
                            numberInSurah = numInSurah,
                            arabicText = arabicText,
                            translationText = translationText,
                            audioUrl = audioUrl
                        )
                    )
                }
                result
            } else {
                throw Exception("API returned response code ${connection.responseCode}")
            }
        } finally {
            connection.disconnect()
        }
    }
}
