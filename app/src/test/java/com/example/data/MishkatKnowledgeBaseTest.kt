package com.example.data

import org.junit.Assert.assertTrue
import org.junit.Test

class MishkatKnowledgeBaseTest {
    @Test
    fun maghribAnswerIsDirectAndCitesSource() {
        val answer = MishkatKnowledgeBase.answer("كم ركعة صلاة المغرب") ?: error("No answer")
        assertTrue(answer.contains("ثلاث ركعات"))
        assertTrue(answer.contains("https://binbaz.org.sa/fatwas/17464/"))
        assertTrue(answer.contains("حكم صلاة الفجر ووقتها"))
    }

    @Test
    fun prophetDeathAnswerAcknowledgesHistoricalDisagreement() {
        val answer = MishkatKnowledgeBase.answer("متى توفي النبي محمد صلى الله عليه وسلم") ?: error("No answer")
        assertTrue(answer.contains("فيه خلاف"))
        assertTrue(answer.contains("https://islamqa.info/ar/answers/147601"))
    }
}
