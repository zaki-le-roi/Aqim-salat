package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.Locale

/**
 * يبحث في المصادر العربية المرفقة بالتطبيق قبل الإجابة.
 * لا يصدر فتوى من إنشائه، ولا يعرض نتيجة على أنها مصدر إن لم يوجد نص مرجعي.
 */
object MishkatAnswerRepository {

    private val stopWords = setOf(
        "ما", "ماذا", "هل", "كيف", "متى", "اين", "لماذا", "كم", "هو", "هي",
        "من", "عن", "على", "في", "الى", "إلى", "مع", "هذا", "هذه", "ذلك",
        "الذي", "التي", "لي", "لي؟", "اريد", "اعرف", "اخبرني", "قل", "ماهو",
        "ماهي", "فضلا", "من فضلك", "يا", "رب", "الله", "الاسلام", "الاسلامي"
    )

    suspend fun answer(context: Context, question: String): String = withContext(Dispatchers.IO) {
        val normalized = normalize(question)
        if (normalized.isBlank()) return@withContext "اكتب سؤالك لأبحث في المصادر المتاحة."

        if (normalized.contains("اية الكرسي") ||
            normalized.contains("ayat al kursi") ||
            normalized.contains("ayatul kursi")) {
            val verse = QuranOfflineData.loadSurah(context.applicationContext, 2)
                ?.firstOrNull { it.numberInSurah == 255 }
            if (verse != null) {
                return@withContext "آية الكرسي هي الآية 255 من سورة البقرة.\n\n" +
                    verse.arabicText + "\n\nالمصدر: القرآن الكريم، سورة البقرة، الآية 255 (2:255)."
            }
            return@withContext "تعذّر تحميل نص الآية من المصحف المحلي؛ لم أستبدل النص بتخمين.\n\nالمصدر المطلوب: القرآن الكريم، سورة البقرة، الآية 255 (2:255)."
        }

        if ((normalized.contains("عدد") || normalized.contains("كم")) &&
            normalized.contains("سورة الفاتحة") && normalized.contains("ايات")) {
            return@withContext "عدد آيات سورة الفاتحة سبع آيات.\n\nالمصدر: المصحف المرفق بالتطبيق، سورة الفاتحة (1:1–7)."
        }

        if ((normalized.contains("عدد") || normalized.contains("كم")) &&
            normalized.contains("سور القرآن")) {
            return@withContext "يضم المصحف 114 سورة.\n\nالمصدر: قائمة سور القرآن الكريم المعتمدة في المصحف المرفق بالتطبيق."
        }

        val terms = meaningfulTerms(normalized)
        if (terms.isNotEmpty()) {
            val hadithMatches = HadithData.books
                .flatMap { book -> book.allHadiths.map { hadith -> Triple(book, hadith, searchableText(hadith.arabicText + " " + hadith.explanation + " " + hadith.chapterTitle + " " + hadith.reference)) } }
                .map { (book, hadith, text) -> Triple(book, hadith, terms.count(text::contains)) }
                .filter { it.third >= minimumMatches(terms.size) }
                .sortedByDescending { it.third }
                .take(3)

            if (hadithMatches.isNotEmpty()) {
                val answer = buildString {
                    append("وجدت نصوصًا ذات صلة في مجموعة الأحاديث المرفقة. هذه مقتطفات للاستدلال، وليست فتوى مستقلة:\n")
                    hadithMatches.forEachIndexed { index, (book, hadith, _) ->
                        append("\n")
                        append(index + 1).append(") ")
                        append(hadith.arabicText)
                        if (hadith.explanation.isNotBlank()) append("\nالفائدة المسجلة: ").append(hadith.explanation)
                        append("\nالمصدر: ").append(hadith.reference)
                        append("\nالمجموعة: ").append(book.title)
                    }
                }
                return@withContext answer
            }

            val libraryMatches = ScholarsLibraryData.books
                .flatMap { book -> book.chapters.map { chapter -> Triple(book, chapter, searchableText(book.titleAr + " " + book.categoryAr + " " + chapter.titleAr + " " + chapter.contentAr)) } }
                .map { (book, chapter, text) -> Triple(book, chapter, terms.count(text::contains)) }
                .filter { it.third >= minimumMatches(terms.size) }
                .sortedByDescending { it.third }
                .take(2)

            if (libraryMatches.isNotEmpty()) {
                return@withContext buildString {
                    append("وجدت مقاطع من المكتبة العلمية المرفقة ذات صلة بسؤالك. أنقل النص مع مرجعه دون اعتباره فتوى لحالتك:\n")
                    libraryMatches.forEachIndexed { index, (book, chapter, _) ->
                        append("\n").append(index + 1).append(") ")
                        append(chapter.contentAr)
                        append("\nالمصدر: ").append(book.titleAr)
                        append(" — ").append(chapter.titleAr)
                        if (chapter.reference.isNotBlank()) append("، ").append(chapter.reference)
                        append("\nالمؤلف: ").append(book.authorAr)
                    }
                }
            }
        }

        if (isRulingQuestion(normalized)) {
            return@withContext "هذا سؤال عن حكم شرعي، ولم أعثر في المصادر المحلية المطابقة على نص موثق يكفي للإجابة بثقة. لن أصدر فتوى من عندي أو أنسب قولًا إلى عالم بلا مرجع. يلزم الرجوع إلى عالم مؤهل أو جهة إفتاء موثوقة، خصوصًا في مسائل الطلاق والميراث والحالات الشخصية."
        }

        "لم أعثر في المصادر المرفقة حاليًا على نص موثق يجيب عن السؤال مباشرة. لا أريد اختلاق آية أو حديث أو مرجع. البحث الحالي يعتمد على محتوى القرآن والحديث والمكتبة العلمية المتاح محليًا داخل التطبيق، ولا يتصل حتى الآن بمحرك ذكاء اصطناعي عام."
    }

    private fun isRulingQuestion(question: String): Boolean {
        val terms = listOf(
            "حلال", "حرام", "يجوز", "لا يجوز", "حكم", "فتوى", "طلاق",
            "ميراث", "زكاة", "كفارة", "يمين", "عدة", "رضاعة"
        )
        return terms.any(question::contains)
    }

    private fun meaningfulTerms(value: String): List<String> =
        normalize(value).split(' ')
            .filter { it.length >= 3 && it !in stopWords }
            .distinct()
            .take(12)

    private fun minimumMatches(termCount: Int): Int = if (termCount <= 2) 1 else 2

    private fun searchableText(value: String): String = " ${normalize(value)} "

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
