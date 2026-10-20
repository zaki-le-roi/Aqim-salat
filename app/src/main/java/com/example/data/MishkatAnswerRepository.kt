package com.example.data

import android.content.Context
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * مِشكاة يعتمد على المساعد الذكي عبر وسيط Cloudflare فقط.
 * لا توجد إجابات محلية أو قاعدة أسئلة بديلة في هذا المسار.
 * يبقى مفتاح Gemini على الخادم ولا يوضع داخل التطبيق.
 */
object MishkatAnswerRepository {

    suspend fun answer(context: Context, question: String): String = withContext(Dispatchers.IO) {
        val submitted = question.trim()
        if (submitted.isBlank()) {
            return@withContext "اكتب سؤالك أولًا."
        }

        askCloud(submitted) ?: "تعذّر على مساعد مِشكاة الإجابة الآن. تحقق من اتصال الإنترنت ثم أعد المحاولة. إذا استمر الخطأ، فقد تكون خدمة الذكاء الاصطناعي غير متاحة مؤقتًا."
    }

    private fun askCloud(question: String): String? {
        var connection: java.net.HttpURLConnection? = null
        return try {
            connection = (java.net.URL("https://mishkat-ai-api.zakidj181.workers.dev/ask")
                .openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 40000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().put("message", question).toString()
            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val result = runCatching { JSONObject(body) }.getOrNull()

            if (status !in 200..299) {
                val detail = result?.optString("error").orEmpty().ifBlank {
                    "رمز الاتصال $status"
                }
                return "تعذّر على مساعد مِشكاة الإجابة الآن: $detail (رمز الاتصال $status). تحقق من الخدمة ثم أعد المحاولة."
            }

            val answer = result?.optString("answer")?.trim().orEmpty()
            if (answer.isBlank()) {
                val detail = result?.optString("error").orEmpty()
                return if (detail.isNotBlank()) {
                    "لم تصل إجابة من مساعد مِشكاة: $detail"
                } else {
                    "وصل رد غير مكتمل من خدمة الذكاء الاصطناعي. أعد المحاولة."
                }
            }

            "$answer\n\nتنبيه: مِشكاة مساعد آلي للمعرفة الإسلامية؛ تحقّق من الأدلة والمراجع، وارجع إلى عالم مؤهل في المسائل الشرعية الخاصة."
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
