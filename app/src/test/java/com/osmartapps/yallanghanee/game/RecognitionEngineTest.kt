package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.RecognitionItem
import com.osmartapps.yallanghanee.model.RecognitionLesson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RecognitionEngineTest {
    private val hijaz = Maqam(
        id = "hijaz",
        nameAr = "حجاز",
        color = "#C97B5A",
        jinsAr = "جنس حجاز",
        signatureAr = "توقيع الحجاز",
        audio = "01-alhijaz.mp3",
    )
    private val nahawand = Maqam(
        id = "nahawand",
        nameAr = "نهاوند",
        color = "#C77E9B",
        jinsAr = "جنس نهاوند",
        signatureAr = "توقيع النهاوند",
        audio = "02-alnahawand.mp3",
    )
    private val lesson = RecognitionLesson(
        id = "hijaz_nahawand",
        titleAr = "حجاز أم نهاوند؟",
        maqamIds = listOf("hijaz", "nahawand"),
        items = listOf(
            RecognitionItem(id = "1", answer = "hijaz"),
            RecognitionItem(id = "2", answer = "nahawand"),
            RecognitionItem(id = "3", answer = "hijaz"),
        ),
    )
    private val pack = MaqamPack(
        version = 1,
        maqams = listOf(hijaz, nahawand),
        lessons = listOf(lesson),
    )

    private fun started(): RecognitionSession =
        RecognitionEngine.start(lesson, pack, Random(7))

    @Test
    fun `start creates one question per item with both options`() {
        val session = started()
        assertEquals(lesson.items.size, session.questions.size)
        session.questions.forEach { question ->
            assertEquals(2, question.options.size)
            assertEquals(2, question.options.map { it.id }.distinct().size)
            assertTrue(question.options.any { it.id == question.answer.id })
        }
    }

    @Test
    fun `audio paths resolve inside the content audio directory`() {
        val question = started().question
        assertEquals("content/audio/01-alhijaz.mp3", question.audio)
        assertTrue(question.contrastAudio.startsWith("content/audio/"))
    }

    @Test
    fun `item audio override is used when present`() {
        val pack = pack.copy(
            lessons = listOf(
                lesson.copy(
                    items = listOf(
                        RecognitionItem(id = "1", answer = "hijaz", audio = "custom.mp3"),
                    ),
                ),
            ),
        )
        val session = RecognitionEngine.start(pack.lessons.single(), pack, Random(1))
        assertEquals("content/audio/custom.mp3", session.question.audio)
    }

    @Test
    fun `correct answer increments the score`() {
        val session = started()
        val answered = RecognitionEngine.answer(session, session.question.answer.id)
        assertEquals(1, answered.correctCount)
        assertTrue(answered.answered)
        assertTrue(answered.answeredCorrectly)
    }

    @Test
    fun `wrong answer keeps the score and exposes the contrast`() {
        val session = started()
        val wrongId = session.question.options.first { it.id != session.question.answer.id }.id
        val answered = RecognitionEngine.answer(session, wrongId)
        assertEquals(0, answered.correctCount)
        assertFalse(answered.answeredCorrectly)
        assertEquals(wrongId, session.question.contrast.id)
    }

    @Test
    fun `next clears the selection and finishes after the last item`() {
        var session = started()
        while (session.index < session.questions.lastIndex) {
            session = RecognitionEngine.answer(session, session.question.answer.id)
            session = RecognitionEngine.next(session)
            assertFalse(session.answered)
        }
        session = RecognitionEngine.answer(session, session.question.answer.id)
        session = RecognitionEngine.next(session)
        assertTrue(session.finished)
        assertEquals(session.total, session.correctCount)
    }

    @Test
    fun `skip advances without scoring or answering`() {
        val session = started()
        val skipped = RecognitionEngine.skip(session)
        assertEquals(1, skipped.index)
        assertEquals(0, skipped.correctCount)
        assertFalse(skipped.answered)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `answering twice is rejected`() {
        val session = started()
        val answered = RecognitionEngine.answer(session, session.question.answer.id)
        RecognitionEngine.answer(answered, session.question.answer.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `answering with an option outside the lesson is rejected`() {
        val session = started()
        RecognitionEngine.answer(session, "rast")
    }
}
