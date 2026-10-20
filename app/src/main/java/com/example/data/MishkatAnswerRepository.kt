package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * يرسل كل سؤال مباشرة إلى مساعد مِشكاة بالذكاء الاصطناعي.
 * لا يستخدم إجابات محلية أو قاعدة أسئلة أو بحثًا محليًا قبل الاتصال.
 */
object MishkatAnswerRepository {

    suspend fun answer(context: Context, question: String): String = withContext(Dispatchers.IO) {
        val submittedQuestion = question.trim()
        if (submittedQuestion.isBlank()) {
            return@withContext "اكتب سؤالك أولًا."
        }

        try {
            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 45000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().put("message", submittedQuestion).toString()
            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            connection.disconnect()

            if (status !in 200..299) {
                return@withContext "تعذّر على مساعد مِشكاة الإجابة الآن (رمز الاتصال $status). تحقق من الإنترنت ثم أعد المحاولة."
            }

            val generatedAnswer = runCatching {
                JSONObject(body).optString("answer").trim()
            }.getOrNull().orEmpty()

            if (generatedAnswer.isBlank()) {
                "وصل الرد من الخادم، لكنه لا يحتوي على إجابة مفهومة. أعد المحاولة بعد قليل."
            } else {
                "$generatedAnswer\n\nتنبيه: هذه إجابة مولّدة بالذكاء الاصطناعي، وليست فتوى ملزمة. تحقّق من الأدلة والمراجع، وارجع إلى عالم مؤهل في المسائل الخاصة."
            }
        } catch (_: Exception) {
            "تعذّر الاتصال بمساعد مِشكاة الذكي. تحقق من اتصال الإنترنت ثم أعد إرسال السؤال."
        }
    }

    private const val ENDPOINT = "https://mishkat-ai-api.zakidj181.workers.dev/ask"
}
