package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** مِشكاة يتصل بخدمة Cloudflare ويعرض أجزاء الإجابة فور وصولها. */
object MishkatAnswerRepository {

    suspend fun answer(
        context: Context,
        question: String,
        onPartialAnswer: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val submitted = question.trim()
        if (submitted.isBlank()) return@withContext "اكتب سؤالك أولًا."

        val answer = askCloud(submitted) { partial ->
            withContext(Dispatchers.Main) { onPartialAnswer(partial) }
        } ?: return@withContext "تعذّر على مساعد مِشكاة الإجابة الآن. تحقق من اتصال الإنترنت وخدمة الذكاء الاصطناعي ثم أعد المحاولة."

        if (answer.isBlank()) return@withContext "لم تصل إجابة مكتملة من مِشكاة. أعد المحاولة."
        "$answer\n\nتنبيه: مِشكاة مساعد آلي للمعرفة الإسلامية؛ تحقّق من الأدلة والمراجع، وارجع إلى عالم مؤهل في المسائل الشرعية الخاصة."
    }

    private suspend fun askCloud(
        question: String,
        onChunk: suspend (String) -> Unit
    ): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("https://mishkat-ai-api.zakidj181.workers.dev/ask")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 90000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "text/event-stream")
            }

            val payload = JSONObject().put("message", question).toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            if (status !in 200..299) {
                val error = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val detail = runCatching { JSONObject(error).optString("error") }.getOrNull().orEmpty()
                return "تعذّر على مِشكاة الاتصال بالخدمة (رمز الاتصال $status). $detail".trim()
            }

            val accumulated = StringBuilder()
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith("data:")) continue
                    val data = line.substringAfter("data:").trim()
                    if (data == "[DONE]") break
                    val chunk = runCatching { JSONObject(data).optString("text") }.getOrNull().orEmpty()
                    if (chunk.isNotEmpty()) {
                        accumulated.append(chunk)
                        onChunk(accumulated.toString())
                    }
                }
            }
            accumulated.toString().trim()
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
