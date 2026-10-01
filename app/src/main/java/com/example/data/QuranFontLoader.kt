package com.example.data

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object QuranFontLoader {
    private const val FILE_NAME = "UthmanicHafs1Ver18.ttf"
    private const val FONT_URL =
        "https://verses.quran.foundation/fonts/quran/hafs/uthmanic_hafs/UthmanicHafs1Ver18.ttf"

    suspend fun load(context: Context): FontFamily? = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, FILE_NAME)
        try {
            if (!file.exists() || file.length() < 100_000L) {
                val connection = (URL(FONT_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 20000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Aqim-Salat/1.2 Android")
                }
                if (connection.responseCode !in 200..299) {
                    connection.disconnect()
                    return@withContext null
                }
                val temporary = File(context.cacheDir, "$FILE_NAME.tmp")
                connection.inputStream.use { input ->
                    temporary.outputStream().use { output -> input.copyTo(output) }
                }
                connection.disconnect()
                if (temporary.length() < 100_000L) {
                    temporary.delete()
                    return@withContext null
                }
                if (!temporary.renameTo(file)) {
                    temporary.copyTo(file, overwrite = true)
                    temporary.delete()
                }
            }
            FontFamily(Font(file, weight = FontWeight.Normal))
        } catch (_: Exception) {
            file.takeIf { it.exists() && it.length() >= 100_000L }
                ?.let { FontFamily(Font(it, weight = FontWeight.Normal)) }
        }
    }
}