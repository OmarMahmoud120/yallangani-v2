package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.AppLanguage
import com.osmartapps.yallanghanee.model.FirstWin
import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.Onboarding
import com.osmartapps.yallanghanee.model.RecognitionItem
import com.osmartapps.yallanghanee.model.RecognitionLesson
import com.osmartapps.yallanghanee.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

class RecognitionEngineTest {
    private val hijaz = Maqam(
        id = "hijaz",
        nameAr = "حجاز",
        nameEn = "Hijaz",
        color = "#C97B5A",
        jinsAr = "جنس حجاز",
        jinsEn = "Jins Hijaz",
        signatureAr = "توقيع الحجاز",
        signatureEn = "Hijaz signature",
        audio = "01-alhijaz.mp3",
        drone = "drone-c4.mp3",
        notation = "media/maqam/hijaz/hijaz.png",
        tracks = listOf(hijazTrack()),
    )
    private val nahawand = Maqam(
        id = "nahawand",
        nameAr = "نهاوند",
        nameEn = "Nahawand",
        color = "#C77E9B",
        jinsAr = "جنس نهاوند",
        jinsEn = "Jins Nahawand",
        signatureAr = "توقيع النهاوند",
        signatureEn = "Nahawand signature",
        audio = "02-alnahawand.mp3",
        drone = "drone-c4.mp3",
        notation = "media/maqam/nahawand/nahawand.png",
        tracks = listOf(nahawandTrack()),
    )
    private val lesson = RecognitionLesson(
        id = "hijaz_nahawand",
        titleAr = "حجاز أم نهاوند؟",
        titleEn = "Hijaz or Nahawand?",
        maqamIds = listOf("hijaz", "nahawand"),
        items = listOf(
            RecognitionItem(id = "1", answer = "hijaz"),
            RecognitionItem(id = "2", answer = "nahawand"),
            RecognitionItem(id = "3", answer = "hijaz"),
        ),
    )
    private val pack = MaqamPack(
        version = 4,
        mediaBase = MEDIA_BASE,
        maqams = listOf(hijaz, nahawand),
        onboarding = Onboarding(
            steps = emptyList(),
            firstWin = FirstWin(
                promptAr = "أي مقام تسمع؟",
                promptEn = "Which maqam do you hear?",
                audio = "01-alhijaz.mp3",
                maqamIds = listOf("hijaz", "nahawand"),
                answer = "hijaz",
                explanationAr = "توقيع الحجاز",
                explanationEn = "Hijaz signature",
            ),
        ),
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
    fun `site track audio resolves against the media base`() {
        val question = started().question
        assertEquals(MEDIA_BASE + "media/maqam/hijaz/a.mp3", question.audio)
        assertEquals(MEDIA_BASE + "media/maqam/nahawand/a.mp3", question.contrastAudio)
    }

    @Test
    fun `notation resolves against the media base`() {
        assertEquals(MEDIA_BASE + "media/maqam/hijaz/hijaz.png", started().question.notationUrl)
    }

    @Test
    fun `each question picks a random track from the answer tracks`() {
        val tracks = listOf(
            Track("أ", "فنان", "media/maqam/hijaz/a.mp3"),
            Track("ب", "فنان", "media/maqam/hijaz/b.mp3"),
            Track("ج", "فنان", "media/maqam/hijaz/c.mp3"),
        )
        val pack = pack.copy(maqams = listOf(hijaz.copy(tracks = tracks), nahawand))
        val session = RecognitionEngine.start(pack.lessons.single(), pack, Random(11))
        val expected = tracks.map { MEDIA_BASE + it.audio }.toSet()
        assertTrue(session.question.audio in expected)

        val seen = session.questions.map { it.audio }.toSet()
        assertTrue("expected varied picks, got $seen", seen.size > 1)
    }

    @Test
    fun `maqam without site tracks falls back to the bundled clip`() {
        val pack = pack.copy(maqams = listOf(hijaz.copy(tracks = emptyList()), nahawand))
        val session = RecognitionEngine.start(
            pack.lessons.single().copy(items = listOf(RecognitionItem(id = "1", answer = "hijaz"))),
            pack,
            Random(1),
        )
        assertEquals("content/audio/01-alhijaz.mp3", session.question.audio)
    }

    @Test
    fun `missing track and bundled clip yields no source`() {
        val pack = pack.copy(maqams = listOf(hijaz.copy(tracks = emptyList(), audio = null), nahawand))
        val session = RecognitionEngine.start(
            pack.lessons.single().copy(items = listOf(RecognitionItem(id = "1", answer = "hijaz"))),
            pack,
            Random(1),
        )
        assertEquals(null, session.question.audio)
    }

    @Test
    fun `drone path resolves inside the content audio directory`() {
        assertEquals("content/audio/drone-c4.mp3", started().question.droneAudio)
    }

    @Test
    fun `maqam without a drone exposes no drone path`() {
        val pack = pack.copy(maqams = listOf(hijaz.copy(drone = null), nahawand))
        val session = RecognitionEngine.start(
            pack.lessons.single().copy(items = listOf(RecognitionItem(id = "1", answer = "hijaz"))),
            pack,
            Random(1),
        )
        assertEquals(null, session.question.droneAudio)
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
    fun `explanation follows the selected language`() {
        val question = started().question
        assertEquals("توقيع الحجاز", question.explanation(AppLanguage.ARABIC))
        assertEquals("Hijaz signature", question.explanation(AppLanguage.ENGLISH))
    }

    @Test
    fun `English item explanation overrides the maqam signature`() {
        val pack = pack.copy(
            lessons = listOf(
                lesson.copy(
                    items = listOf(
                        RecognitionItem(
                            id = "1",
                            answer = "hijaz",
                            explanationEn = "Item note",
                        ),
                    ),
                ),
            ),
        )
        val session = RecognitionEngine.start(pack.lessons.single(), pack, Random(1))
        assertEquals("Item note", session.question.explanation(AppLanguage.ENGLISH))
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

    @Test
    fun `hint is unavailable on a two option question`() {
        assertFalse(RecognitionEngine.hintAvailable(started()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requesting an unavailable hint is rejected`() {
        RecognitionEngine.eliminateWrongOption(started())
    }

    @Test
    fun `hint eliminates exactly one wrong option`() {
        val session = threeOptionSession()
        val hinted = RecognitionEngine.eliminateWrongOption(session)
        assertTrue(hinted.hintUsed)
        assertNotNull(hinted.eliminatedId)
        assertNotEquals(hinted.question.answer.id, hinted.eliminatedId)
        assertEquals(3, hinted.question.options.size)
        assertFalse(RecognitionEngine.hintAvailable(hinted))
    }

    @Test
    fun `an eliminated option cannot be answered`() {
        val hinted = RecognitionEngine.eliminateWrongOption(threeOptionSession())
        try {
            RecognitionEngine.answer(hinted, hinted.eliminatedId!!)
            fail("eliminated option was accepted")
        } catch (expected: IllegalArgumentException) {
            assertEquals("option was eliminated: ${hinted.eliminatedId}", expected.message)
        }
    }

    @Test
    fun `hint covers the whole lesson and clears its elimination`() {
        var session = RecognitionEngine.eliminateWrongOption(threeOptionSession())
        session = RecognitionEngine.answer(session, session.question.answer.id)
        session = RecognitionEngine.next(session)
        assertFalse(RecognitionEngine.hintAvailable(session))
        assertNull(session.eliminatedId)
    }

    private fun threeOptionSession(): RecognitionSession {
        val rast = Maqam(
            id = "rast",
            nameAr = "راست",
            nameEn = "Rast",
            color = "#C9A15A",
            jinsAr = "جنس راست",
            jinsEn = "Jins Rast",
            signatureAr = "توقيع الراست",
            signatureEn = "Rast signature",
            audio = "03-alrast.mp3",
            drone = "drone-c4.mp3",
            notation = "media/maqam/rast/rast.png",
            tracks = listOf(Track("لحن الراست", "فنان", "media/maqam/rast/a.mp3")),
        )
        val hintLesson = RecognitionLesson(
            id = "hijaz_nahawand_rast",
            titleAr = "أي مقام؟",
            titleEn = "Which maqam?",
            maqamIds = listOf("hijaz", "nahawand", "rast"),
            items = listOf(
                RecognitionItem(id = "1", answer = "hijaz"),
                RecognitionItem(id = "2", answer = "nahawand"),
            ),
        )
        val hintPack = MaqamPack(
            version = 4,
            mediaBase = MEDIA_BASE,
            maqams = listOf(hijaz, nahawand, rast),
            onboarding = pack.onboarding,
            lessons = listOf(hintLesson),
        )
        return RecognitionEngine.start(hintLesson, hintPack, Random(3))
    }

    private companion object {
        const val MEDIA_BASE = "https://yallanganee-73906702.web.app/data/"

        fun hijazTrack() = Track("لحن الحجاز", "فنان", "media/maqam/hijaz/a.mp3")
        fun nahawandTrack() = Track("لحن النهاوند", "فنان", "media/maqam/nahawand/a.mp3")
    }
}
