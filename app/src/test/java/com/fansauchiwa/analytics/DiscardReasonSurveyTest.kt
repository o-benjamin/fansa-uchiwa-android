package com.fansauchiwa.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 編集を破棄した理由の調査（#265・一時的）のテスト */
class DiscardReasonSurveyTest {

    @Test
    fun shouldAsk_FirstDiscard_ReturnsTrue() {
        val survey = DiscardReasonSurvey()

        assertTrue(survey.shouldAsk())
    }

    @Test
    fun shouldAsk_SecondAndLaterDiscards_ReturnsFalse() {
        val survey = DiscardReasonSurvey()
        survey.shouldAsk()

        assertFalse(survey.shouldAsk())
        assertFalse(survey.shouldAsk())
    }

    @Test
    fun answerEvent_ReasonSelected_SendsReasonParamValue() {
        val survey = DiscardReasonSurvey()

        val event = survey.answerEvent(DiscardReason.NOT_ENOUGH_MATERIALS)

        assertEquals("answer_discard_reason", event.name)
        assertEquals(mapOf("reason" to "not_enough_materials"), event.params)
    }

    @Test
    fun answerEvent_NoAnswer_SendsNoAnswer() {
        val event = DiscardReasonSurvey().answerEvent(DiscardReason.NO_ANSWER)

        assertEquals(mapOf("reason" to "no_answer"), event.params)
    }

    @Test
    fun choices_Always_ExcludesNoAnswerAndKeepsOtherReasons() {
        assertFalse(DiscardReason.NO_ANSWER in DiscardReason.choices)
        assertEquals(DiscardReason.entries.size - 1, DiscardReason.choices.size)
    }

    @Test
    fun paramValue_AllReasons_AreUniqueAndWithinGa4Limit() {
        val values = DiscardReason.entries.map { it.paramValue }

        assertEquals(values.size, values.toSet().size)
        // GA4 のイベントパラメータ値は100文字まで
        assertTrue(values.all { it.isNotEmpty() && it.length <= 100 })
    }
}
