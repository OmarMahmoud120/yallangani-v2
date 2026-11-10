package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.FirstWin
import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.Onboarding
import com.osmartapps.yallanghanee.model.OnboardingOption
import com.osmartapps.yallanghanee.model.OnboardingStep
import com.osmartapps.yallanghanee.model.RecognitionItem
import com.osmartapps.yallanghanee.model.RecognitionLesson
import com.osmartapps.yallanghanee.model.Track
import org.junit.Assert.assertEquals
import org.junit.Test

class MaqamPackValidatorTest {
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
        notation = "media/maqam/hijaz/hijaz.png",
        tracks = listOf(track("hijaz")),
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
        notation = "media/maqam/nahawand/nahawand.png",
        tracks = listOf(track("nahawand")),
    )
    private val lesson = RecognitionLesson(
        id = "hijaz_nahawand",
        titleAr = "حجاز أم نهاوند؟",
        titleEn = "Hijaz or Nahawand?",
        maqamIds = listOf("hijaz", "nahawand"),
        items = listOf(RecognitionItem(id = "1", answer = "hijaz")),
    )
    private val valid = MaqamPack(
        version = 4,
        mediaBase = MEDIA_BASE,
        maqams = listOf(hijaz, nahawand),
        onboarding = onboarding(),
        lessons = listOf(lesson),
    )
    private val allAudioPresent: (String) -> Boolean = { true }

    private companion object {
        const val MEDIA_BASE = "https://yallanganee-73906702.web.app/data/"
    }

    private fun track(id: String) =
        Track(title = "لحن", artist = "فنان", audio = "media/maqam/$id/track.mp3")

    private fun step(index: Int) = OnboardingStep(
        id = "step_$index",
        promptAr = "سؤال $index",
        promptEn = "Question $index",
        audio = "on-$index.mp3",
        options = listOf(
            OnboardingOption(id = "first", labelAr = "الأولى", labelEn = "First"),
            OnboardingOption(id = "second", labelAr = "الثانية", labelEn = "Second"),
        ),
        answer = "second",
        explanationAr = "شرح $index",
        explanationEn = "Explanation $index",
    )

    private fun onboarding(steps: List<OnboardingStep> = (1..5).map(::step)) = Onboarding(
        steps = steps,
        firstWin = FirstWin(
            promptAr = "أي مقام تسمع؟",
            promptEn = "Which maqam do you hear?",
            audio = "01-alhijaz.mp3",
            maqamIds = listOf("hijaz", "nahawand"),
            answer = "hijaz",
            explanationAr = "توقيع الحجاز",
            explanationEn = "Hijaz signature",
        ),
    )

    @Test
    fun `missing media base is rejected`() {
        val errors = MaqamPackValidator.validate(valid.copy(mediaBase = null), allAudioPresent)
        assertEquals(listOf("missing media base"), errors)
    }

    @Test
    fun `maqam without notation is rejected`() {
        val pack = valid.copy(maqams = listOf(hijaz.copy(notation = null), nahawand))
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("missing notation: hijaz"), errors)
    }

    @Test
    fun `maqam without tracks is rejected`() {
        val pack = valid.copy(maqams = listOf(hijaz.copy(tracks = emptyList()), nahawand))
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("no tracks for hijaz"), errors)
    }

    @Test
    fun `maqam without a lesson is rejected`() {
        val pack = valid.copy(maqams = listOf(hijaz, nahawand, hijaz.copy(id = "rast")))
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("no lesson covers: rast"), errors)
    }

    @Test
    fun `valid pack has no errors`() {
        assertEquals(emptyList<String>(), MaqamPackValidator.validate(valid, allAudioPresent))
    }

    @Test
    fun `missing English maqam copy is rejected`() {
        val pack = valid.copy(
            maqams = listOf(hijaz.copy(signatureEn = ""), nahawand),
        )
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("blank English signature for hijaz"), errors)
    }

    @Test
    fun `missing English lesson title is rejected`() {
        val pack = valid.copy(lessons = listOf(lesson.copy(titleEn = "")))
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("blank English lesson title: hijaz_nahawand"), errors)
    }

    @Test
    fun `duplicate maqam ids are rejected`() {
        val pack = valid.copy(
            maqams = listOf(hijaz, hijaz, nahawand),
        )
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("duplicate maqam id: hijaz"), errors)
    }

    @Test
    fun `missing audio is rejected`() {
        val errors = MaqamPackValidator.validate(valid) { it != "02-alnahawand.mp3" }
        assertEquals(listOf("missing audio: 02-alnahawand.mp3"), errors)
    }

    @Test
    fun `missing drone audio is rejected`() {
        val pack = valid.copy(
            maqams = listOf(hijaz.copy(drone = "drone-c4.mp3"), nahawand),
        )
        val errors = MaqamPackValidator.validate(pack) { it != "drone-c4.mp3" }
        assertEquals(listOf("missing drone audio: drone-c4.mp3"), errors)
    }

    @Test
    fun `unapproved maqam is rejected`() {
        val pack = valid.copy(maqams = listOf(hijaz.copy(status = "draft"), nahawand))
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("maqam not approved: hijaz"), errors)
    }

    @Test
    fun `answer outside the lesson options is rejected`() {
        val pack = valid.copy(
            lessons = listOf(
                lesson.copy(items = listOf(RecognitionItem(id = "1", answer = "rast"))),
            ),
        )
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("answer outside options in hijaz_nahawand/1"), errors)
    }

    @Test
    fun `unknown maqam in lesson is rejected`() {
        val pack = valid.copy(
            lessons = listOf(lesson.copy(maqamIds = listOf("hijaz", "rast"))),
        )
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(
            listOf("unknown maqam in hijaz_nahawand: rast", "no lesson covers: nahawand"),
            errors,
        )
    }

    @Test
    fun `duplicate lesson options are rejected`() {
        val pack = valid.copy(
            lessons = listOf(lesson.copy(maqamIds = listOf("hijaz", "hijaz"))),
        )
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(
            listOf("duplicate option in lesson: hijaz_nahawand", "no lesson covers: nahawand"),
            errors,
        )
    }

    @Test
    fun `onboarding needs five steps`() {
        val pack = valid.copy(onboarding = onboarding((1..4).map(::step)))
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("onboarding needs 5 steps: 4"), errors)
    }

    @Test
    fun `onboarding answer outside options is rejected`() {
        val steps = (1..5).map(::step).toMutableList()
        steps[0] = steps[0].copy(answer = "third")
        val pack = valid.copy(onboarding = onboarding(steps))
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("answer outside options in onboarding step: step_1"), errors)
    }

    @Test
    fun `onboarding missing audio is rejected`() {
        val errors = MaqamPackValidator.validate(valid) { it != "on-3.mp3" }
        assertEquals(listOf("missing audio: on-3.mp3"), errors)
    }

    @Test
    fun `first win with an unknown maqam is rejected`() {
        val pack = valid.copy(
            onboarding = onboarding().copy(
                firstWin = onboarding().firstWin.copy(maqamIds = listOf("hijaz", "rast")),
            ),
        )
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("unknown maqam in first win: rast"), errors)
    }
}
