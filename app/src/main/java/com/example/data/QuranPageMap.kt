package com.example.data

import android.content.Context
import org.json.JSONObject

data class QuranAyahRef(val surah: Int, val ayah: Int)

object QuranPageMap {
    private const val ASSET_NAME = "quran_pages_604.json"
    @Volatile private var cachedPages: List<List<QuranAyahRef>>? = null

    fun load(context: Context): List<List<QuranAyahRef>> {
        cachedPages?.let { return it }
        synchronized(this) {
            cachedPages?.let { return it }
            val parsed = runCatching {
                val raw = context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val pages = JSONObject(raw).getJSONArray("pages")
                List(pages.length()) { pageIndex ->
                    val ayahs = pages.getJSONObject(pageIndex).getJSONArray("ayahs")
                    List(ayahs.length()) { ayahIndex ->
                        val item = ayahs.getJSONObject(ayahIndex)
                        QuranAyahRef(item.getInt("surah"), item.getInt("ayah"))
                    }
                }
            }.getOrDefault(emptyList())
            cachedPages = parsed
            return parsed
        }
    }

    fun pageFor(context: Context, surah: Int, ayah: Int): Int {
        return load(context).indexOfFirst { page -> page.any { it.surah == surah && it.ayah == ayah } }
            .let { if (it < 0) 1 else it + 1 }
    }
}
