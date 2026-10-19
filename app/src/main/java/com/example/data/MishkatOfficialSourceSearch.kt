package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

internal data class MishkatSourceResult(
    val title: String,
    val snippet: String,
    val url: String,
    val sourceName: String
)

/**
 * يبحث عن صفحات منشورة من نطاقات شرعية معروفة، ثم يعرض المقتطف والرابط الأصلي.
 * لا ينشئ إجابات ولا يستنتج فتوى من تلقاء نفسه.
 */
internal object MishkatOfficialSourceSearch {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val trustedDomains = listOf(
        "dar-alifta.org",
        "binbaz.org.sa",
        "islamweb.net",
        "islamqa.info",
        "dorar.net",
        "quran.com",
        "sunnah.com"
    )

    suspend fun search(question: String, rulingQuestion: Boolean = false): List<MishkatSourceResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val siteQuery = if (rulingQuestion) {
                    "site:dar-alifta.org/ar/fatawa OR site:binbaz.org.sa/fatwas OR site:islamweb.net/ar/fatwa OR site:islamqa.info/ar/answers"
                } else {
                    "site:dorar.net OR site:quran.com OR site:sunnah.com OR site:dar-alifta.org/ar/fatawa OR site:binbaz.org.sa/fatwas OR site:islamweb.net/ar/fatwa OR site:islamqa.info/ar/answers"
                }
                val query = URLEncoder.encode("$siteQuery $question", StandardCharsets.UTF_8.name())
                val request = Request.Builder()
                    .url("https://html.duckduckgo.com/html/?q=$query")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 Chrome/130.0 Mobile Safari/537.36")
                    .header("Accept-Language", "ar,en;q=0.7")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext emptyList()
                    val html = response.body?.string().orEmpty()
                    parseResults(html, rulingQuestion).take(4)
                }
            }.getOrDefault(emptyList())
        }

    private fun parseResults(html: String, rulingQuestion: Boolean): List<MishkatSourceResult> {
        val titlePattern = Regex(
            """(?is)<a\b(?=[^>]*class=["'][^"']*result__a[^"']*["'])(?=[^>]*href=["']([^"']+)["'])[^>]*>(.*?)</a>"""
        )
        val snippetPattern = Regex(
            """(?is)<a\b(?=[^>]*class=["'][^"']*result__snippet[^"']*["'])[^>]*>(.*?)</a>"""
        )
        val titles = titlePattern.findAll(html).toList()
        val snippets = snippetPattern.findAll(html).toList()
        val results = mutableListOf<MishkatSourceResult>()

        titles.forEachIndexed { index, match ->
            val url = normalizeResultUrl(match.groupValues[1]) ?: return@forEachIndexed
            val host = runCatching { URI(url).host?.lowercase().orEmpty() }.getOrDefault("")
            if (trustedDomains.none { host == it || host.endsWith(".$it") }) return@forEachIndexed
            if (rulingQuestion && !isFatwaPage(host, url)) return@forEachIndexed

            val title = cleanHtml(match.groupValues[2]).take(180)
            val snippet = snippets.getOrNull(index)?.groupValues?.get(1)?.let(::cleanHtml).orEmpty().take(420)
            if (title.isBlank() || results.any { it.url == url }) return@forEachIndexed
            results += MishkatSourceResult(title, snippet, url, sourceName(host))
        }
        return results
    }

    private fun isFatwaPage(host: String, url: String): Boolean {
        val lower = url.lowercase()
        return when {
            host.endsWith("dar-alifta.org") -> lower.contains("/fatawa/") || lower.contains("/fatwalist/")
            host.endsWith("binbaz.org.sa") -> lower.contains("/fatwas/")
            host.endsWith("islamweb.net") -> lower.contains("/fatwa/")
            host.endsWith("islamqa.info") -> lower.contains("/answers/")
            else -> false
        }
    }

    private fun normalizeResultUrl(raw: String): String? {
        val candidate = if (raw.startsWith("//")) "https:$raw" else raw
        val decoded = runCatching {
            val uri = URI(candidate)
            val target = uri.rawQuery.orEmpty().split("&")
                .firstOrNull { it.startsWith("uddg=") }?.substringAfter("uddg=")
            if (target != null) URLDecoder.decode(target, StandardCharsets.UTF_8.name()) else candidate
        }.getOrDefault(candidate)
        val uri = runCatching { URI(decoded) }.getOrNull() ?: return null
        if (uri.scheme != "https" || uri.host.isNullOrBlank()) return null
        return uri.toASCIIString()
    }

    private fun cleanHtml(value: String): String = value
        .replace(Regex("(?is)<script.*?</script>|<style.*?</style>"), " ")
        .replace(Regex("(?s)<[^>]+>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", " ")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun sourceName(host: String): String = when {
        host.endsWith("dar-alifta.org") -> "دار الإفتاء المصرية"
        host.endsWith("binbaz.org.sa") -> "موقع الشيخ ابن باز"
        host.endsWith("islamweb.net") -> "إسلام ويب"
        host.endsWith("islamqa.info") -> "الإسلام سؤال وجواب"
        host.endsWith("dorar.net") -> "الدرر السنية"
        host.endsWith("quran.com") -> "Quran.com"
        host.endsWith("sunnah.com") -> "Sunnah.com"
        else -> host
    }
}
