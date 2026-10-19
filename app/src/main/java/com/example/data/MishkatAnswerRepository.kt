package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.Locale

/**
 * Source-first answers for Mishkat.
 * This initial version only answers items grounded in the Quran corpus already
 * bundled with the app. It does not generate rulings or invent hadith references.
 */
object MishkatAnswerRepository {

    suspend fun answer(context: Context, question: String): String = withContext(Dispatchers.IO) {
        val normalized = normalize(question)

        if (normalized.contains("اية الكرسي") ||
            normalized.contains("ayat al kursi") ||
            normalized.contains("ayatul kursi")) {
            val verse = QuranOfflineData.loadSurah(context.applicationContext, 2)
                ?.firstOrNull { it.numberInSurah == 255 }
            if (verse != null) {
                return@withContext buildString {
                    append("آية الكرسي هي الآية 255 من سورة البقرة.\n\n")
                    append(verse.arabicText)
                    append("\n\nالمصدر: القرآن الكريم، سورة البقرة، الآية 255 (2:255).")
                }
            }
            return@withContext "تعذّر تحميل نص الآية من المصحف المحلي. لم أستبدل النص بتخمين.\n\nالمصدر المطلوب: القرآن الكريم، سورة البقرة، الآية 255 (2:255)."
        }

        if ((normalized.contains("عدد") || normalized.contains("كم")) &&
            normalized.contains("سورة الفاتحة") && normalized.contains("ايات")) {
            return@withContext "عدد آيات سورة الفاتحة سبع آيات.\n\nالمصدر: بيانات المصحف المرفق بالتطبيق، سورة الفاتحة (1:1–7)."
        }

        if ((normalized.contains("عدد") || normalized.contains("كم")) &&
            normalized.contains("سور القرآن")) {
            return@withContext "يضم المصحف 114 سورة.\n\nالمصدر: قائمة سور القرآن الكريم المرفقة بالتطبيق."
        }

        if (isRulingQuestion(normalized)) {
            return@withContext "هذا سؤال عن حكم شرعي، ولن أصدر فيه فتوى من عندي. لم أجد بعدُ في قاعدة الفتاوى الموثقة داخل هذه النسخة مرجعًا يمكنني نقله بثقة. لذلك لن أنسب حكمًا إلى عالم أو مصدر دون تحقق. يُرجى الرجوع إلى عالم مؤهل أو جهة إفتاء موثوقة، خصوصًا إذا كان الحكم متعلقًا بحالة شخصية."
        }

        "لم أعثر بعدُ في المصادر الموثقة المتاحة داخل هذه النسخة على جواب يمكن إسناده إلى مرجع محدد. لن أخمّن أو أختلق آية أو حديثًا أو اسم عالم. قاعدة المصادر الموسّعة ما زالت تحتاج إلى استكمال والتحقق قبل أن أجيب عن هذا النوع من الأسئلة."
    }

    private fun isRulingQuestion(question: String): Boolean {
        val terms = listOf(
            "حلال", "حرام", "يجوز", "لا يجوز", "حكم", "فتوى", "طلاق",
            "ميراث", "زكاة", "كفارة", "يمين", "عدة", "رضاعة"
        )
        return terms.any(question::contains)
    }

    private fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        return decomposed
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]"), "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ى', 'ي')
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
