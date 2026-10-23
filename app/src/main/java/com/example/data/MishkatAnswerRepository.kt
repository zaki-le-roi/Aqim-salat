package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** يتصل مِشكاة بخدمة Cloudflare التي تُرجع JSON نهائياً. */
object MishkatAnswerRepository {

    suspend fun answer(
        context: Context,
        question: String,
        onPartialAnswer: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val submitted = question.trim()
        if (submitted.isBlank()) return@withContext "اكتب سؤالك أولاً."

        val answer = askCloud(submitted) { text ->
            withContext(Dispatchers.Main) { onPartialAnswer(text) }
        } ?: return@withContext "تعذّر على مِشكاة قراءة ردّ الخدمة. تحقق من سجلّ Worker ثم أعد المحاولة."

        if (answer.isBlank()) {
            return@withContext "لم تصل إجابة مكتملة من مِشكاة. أعد المحاولة."
        }

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
                // Worker returns one final JSON object, not server-sent events.
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().put("question", question).toString()
            connection.outputStream.use {
                it.write(payload.toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = stream
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()

            val json = runCatching { JSONObject(responseText) }.getOrNull()

            if (status !in 200..299) {
                val detail = json?.optString("error")
                    ?.takeIf { it.isNotBlank() }
                    ?: "رمز HTTP $status"
                return "تعذّر على مِشكاة الاتصال بالخدمة: $detail"
            }

            if (json == null) {
                return "وصل ردّ من مِشكاة لكنه ليس JSON صالحاً."
            }

            if (!json.optBoolean("success", false)) {
                val detail = json.optString("error")
                    .ifBlank { "لم تُرجع الخدمة إجابة." }
                return "تعذّر على مِشكاة الإجابة: $detail"
            }

            val answer = json.optString("answer").trim()
            if (answer.isBlank()) {
                return "أعادت الخدمة استجابة ناجحة دون نص للإجابة."
            }

            // The endpoint returns a completed answer in one response, so
            // publish it once to update the existing chat UI.
            onChunk(answer)
            answer
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
