package com.example.data

import org.junit.Assert.assertTrue
import org.junit.Test

class MishkatKnowledgeBaseTest {
    @Test
    fun maghribAnswerIsDirectAndCitesRelevantSource() {
        val answer = MishkatKnowledgeBase.answer("كم ركعة صلاة المغرب") ?: error("No answer")
        assertTrue(answer.contains("ثلاث ركعات"))
        assertTrue(answer.contains("https://ibnbaz.org/fatwas/4754/%D8%B5%D9%81%D8%A9-%D8%B5%D9%84%D8%A7%D8%A9-%D8%A7%D9%84%D9%85%D8%BA%D8%B1%D8%A8-%D9%84%D9%85%D9%86-%D8%A3%D8%AF%D8%B1%D9%83-%D8%A7%D9%84%D8%A7%D9%85%D8%A7%D9%85-%D9%81%D9%8A-%D8%A7%D9%84%D8%AA%D8%B4%D9%87%D8%AF-%D8%A7%D9%84%D8%A7%D8%AE%D9%8A%D9%80%D8%B1"))
        assertTrue(answer.contains("صفة صلاة المغرب"))
        assertTrue(!answer.contains("حكم صلاة الفجر ووقتها"))
    }

    @Test
    fun prophetDeathAnswerAcknowledgesHistoricalDisagreement() {
        val answer = MishkatKnowledgeBase.answer("متى توفي النبي محمد صلى الله عليه وسلم") ?: error("No answer")
        assertTrue(answer.contains("فيه خلاف"))
        assertTrue(answer.contains("https://islamqa.info/ar/answers/147601"))
    }

    @Test
    fun hadithNarratorIsExtractedOnlyWhenArabicTextProvidesIt() {
        val narrator = extractArabicNarrator("عَنْ أَبِي هُرَيْرَةَ رَضِيَ اللهُ عَنْهُ قَالَ: قال رسول الله")
        assertTrue(narrator.contains("أبي هريرة"))

        val missing = extractArabicNarrator("قال رسول الله صلى الله عليه وسلم")
        assertTrue(missing.contains("لم يُفصل اسم الراوي"))
    }
}
