package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Loads the complete Arabic hadith corpus on demand from the public hadith-json
 * dataset. The app's curated bundled excerpts remain available offline; once a
 * full book is downloaded it is cached as a file for later offline reading.
 */
object HadithRemoteRepository {
    private const val BASE =
        "https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_book/"

    private val sourcePaths = mapOf(
        "bukhari" to "the_9_books/bukhari.json",
        "muslim" to "the_9_books/muslim.json",
        "abudawud" to "the_9_books/abudawud.json",
        "tirmidhi" to "the_9_books/tirmidhi.json",
        "nasai" to "the_9_books/nasai.json",
        "ibnmajah" to "the_9_books/ibnmajah.json",
        "malik" to "the_9_books/malik.json",
        "riyad" to "other_books/riyad_assalihin.json",
        "nawawi40" to "forties/nawawi40.json"
    )

    suspend fun loadFullBook(context: Context, book: HadithData.HadithBook): HadithData.HadithBook? =
        withContext(Dispatchers.IO) {
            val relativePath = sourcePaths[book.id] ?: return@withContext null
            val cacheDir = File(context.applicationContext.filesDir, "hadith_corpus").apply { mkdirs() }
            val cacheFile = File(cacheDir, "${book.id}_v1.2.0.json")
            val jsonText = try {
                if (cacheFile.isFile && cacheFile.length() > 100) {
                    cacheFile.readText(Charsets.UTF_8)
                } else {
                    val connection = (URL(BASE + relativePath).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 15000
                        readTimeout = 30000
                        setRequestProperty("User-Agent", "Aqim-Salat-Android")
                    }
                    try {
                        if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                        val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        if (body.length < 100 || !body.trimStart().startsWith("{")) return@withContext null
                        val parsed = JSONObject(body)
                        if (!parsed.has("hadiths") || parsed.getJSONArray("hadiths").length() == 0) {
                            return@withContext null
                        }
                        cacheFile.writeText(body, Charsets.UTF_8)
                        body
                    } finally {
                        connection.disconnect()
                    }
                }
            } catch (_: Exception) {
                // A failed network request never removes the bundled reading content.
                return@withContext null
            }

            try {
                val root = JSONObject(jsonText)
                val chaptersJson = root.optJSONArray("chapters") ?: return@withContext null
                val hadithsJson = root.optJSONArray("hadiths") ?: return@withContext null
                val chapterNames = HashMap<Int, String>()
                for (i in 0 until chaptersJson.length()) {
                    val chapter = chaptersJson.optJSONObject(i) ?: continue
                    val id = chapter.optInt("id", -1)
                    val title = chapter.optString("arabic").trim()
                    if (id >= 0 && title.isNotBlank()) chapterNames[id] = title
                }

                val grouped = linkedMapOf<Int, MutableList<HadithData.BookHadith>>()
                for (i in 0 until hadithsJson.length()) {
                    val item = hadithsJson.optJSONObject(i) ?: continue
                    val number = item.optInt("idInBook", i + 1)
                    val chapterId = item.optInt("chapterId", 0)
                    val chapterTitle = chapterNames[chapterId] ?: "أحاديث الكتاب"
                    val arabic = item.optString("arabic").trim()
                    if (arabic.isBlank()) continue
                    // The interface is Arabic-only; do not leak the dataset's English narrator field.
                    val narrator = "إسناد الحديث"
                    grouped.getOrPut(chapterId) { mutableListOf() }.add(
                        HadithData.BookHadith(
                            id = "${book.id}_source_${number}",
                            bookId = book.id,
                            chapterId = "${book.id}_source_ch_${chapterId}",
                            chapterTitle = chapterTitle,
                            numberInBook = number.toString(),
                            globalNumber = item.optInt("id", number).toString(),
                            narrator = narrator,
                            arabicText = arabic,
                            explanation = "",
                            reference = "${book.title}، رقم الحديث $number"
                        )
                    )
                }

                val chapters = grouped.entries.mapIndexed { index, entry ->
                    val title = chapterNames[entry.key] ?: "أحاديث الكتاب"
                    HadithData.HadithChapter(
                        id = "${book.id}_source_ch_${entry.key}",
                        number = index + 1,
                        title = title,
                        description = "النص العربي ورقم الحديث من قاعدة Hadith-json، الإصدار 1.2.0",
                        hadiths = entry.value
                    )
                }.filter { it.hadiths.isNotEmpty() }

                if (chapters.isEmpty()) null else book.copy(
                    chapters = chapters,
                    availableContentDesc = "النص الكامل المحمّل من قاعدة Hadith-json (1.2.0)",
                    isFullyBundled = false
                )
            } catch (_: Exception) {
                null
            }
        }
}
