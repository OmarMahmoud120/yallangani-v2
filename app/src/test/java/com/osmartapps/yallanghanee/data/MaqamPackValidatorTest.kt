package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.FirstWin
import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.Onboarding
import com.osmartapps.yallanghanee.model.OnboardingOption
import com.osmartapps.yallanghanee.model.OnboardingStep
import com.osmartapps.yallanghanee.model.RecognitionItem
import com.osmartapps.yallanghanee.model.RecognitionLesson
import org.junit.Assert.assertEquals
import org.junit.Test

class MaqamPackValidatorTest {
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
        items = listOf(RecognitionItem(id = "1", answer = "hijaz")),
    )
    private val valid = MaqamPack(
        version = 2,
        maqams = listOf(hijaz, nahawand),
        onboarding = onboarding(),
        lessons = listOf(lesson),
    )
    private val allAudioPresent: (String) -> Boolean = { true }

    private fun step(index: Int) = OnboardingStep(
        id = "step_$index",
        promptAr = "سؤال $index",
        audio = "on-$index.mp3",
        options = listOf(
            OnboardingOption(id = "first", labelAr = "الأولى"),
            OnboardingOption(id = "second", labelAr = "الثانية"),
        ),
        answer = "second",
        explanationAr = "شرح $index",
    )

    private fun onboarding(steps: List<OnboardingStep> = (1..5).map(::step)) = Onboarding(
        steps = steps,
        firstWin = FirstWin(
            promptAr = "أي مقام تسمع؟",
            audio = "01-alhijaz.mp3",
            maqamIds = listOf("hijaz", "nahawand"),
            answer = "hijaz",
            explanationAr = "توقيع الحجاز",
        ),
    )

    @Test
    fun `valid pack has no errors`() {
        assertEquals(emptyList<String>(), MaqamPackValidator.validate(valid, allAudioPresent))
    }

    @Test
    fun `duplicate maqam ids are rejected`() {
        val pack = valid.copy(
            maqams = listOf(hijaz, hijaz, nahawand),
            lessons = emptyList(),
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
        assertEquals(listOf("unknown maqam in hijaz_nahawand: rast"), errors)
    }

    @Test
    fun `duplicate lesson options are rejected`() {
        val pack = valid.copy(
            lessons = listOf(lesson.copy(maqamIds = listOf("hijaz", "hijaz"))),
        )
        val errors = MaqamPackValidator.validate(pack, allAudioPresent)
        assertEquals(listOf("duplicate option in lesson: hijaz_nahawand"), errors)
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
