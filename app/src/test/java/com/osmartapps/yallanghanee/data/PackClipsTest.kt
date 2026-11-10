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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PackClipsTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `clip names cover maqams, drones, onboarding, and item audio`() {
        val pack = MaqamPack(
            version = 2,
            maqams = listOf(
                maqam("hijaz", "01-alhijaz.mp3", drone = "drone-hijaz.mp3"),
                maqam("nahawand", "02-alnahawand.mp3"),
            ),
            onboarding = Onboarding(
                steps = listOf(step("on-1.mp3")),
                firstWin = FirstWin(
                    promptAr = "أي مقام؟",
                    audio = "on-win.mp3",
                    maqamIds = listOf("hijaz", "nahawand"),
                    answer = "hijaz",
                    explanationAr = "شرح",
                ),
            ),
            lessons = listOf(
                RecognitionLesson(
                    id = "l",
                    titleAr = "درس",
                    maqamIds = listOf("hijaz", "nahawand"),
                    items = listOf(RecognitionItem(id = "1", answer = "hijaz", audio = "item-1.mp3")),
                ),
            ),
        )

        assertEquals(
            setOf(
                "01-alhijaz.mp3",
                "02-alnahawand.mp3",
                "drone-hijaz.mp3",
                "on-1.mp3",
                "on-win.mp3",
                "item-1.mp3",
            ),
            pack.clipNames(),
        )
    }

    @Test
    fun `safe clip names are plain file names`() {
        listOf("01-alhijaz.mp3", "drone_C4-2.m4a", "a").forEach { name ->
            assertTrue("$name should be safe", isSafeClipName(name))
        }
    }

    @Test
    fun `path traversal and separators are unsafe`() {
        listOf("", "../evil.mp3", "dir/file.mp3", "dir\\file.mp3", ".hidden", "a b.mp3", "a\n.mp3")
            .forEach { name ->
                assertFalse("$name should be unsafe", isSafeClipName(name))
            }
    }

    @Test
    fun `sha256 matches the known digest of bytes and files`() {
        val bytes = "abc".toByteArray()
        val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertEquals(expected, sha256(bytes))

        val file = temp.newFile("abc.bin").apply { writeBytes(bytes) }
        assertEquals(expected, sha256(file))
    }

    private fun step(audio: String) = OnboardingStep(
        id = "step",
        promptAr = "سؤال",
        audio = audio,
        options = listOf(
            OnboardingOption(id = "first", labelAr = "الأولى"),
            OnboardingOption(id = "second", labelAr = "الثانية"),
        ),
        answer = "second",
        explanationAr = "شرح",
    )

    private fun maqam(id: String, audio: String, drone: String? = null) = Maqam(
        id = id,
        nameAr = "مقام",
        color = "#C97B5A",
        jinsAr = "جنس",
        signatureAr = "توقيع",
        audio = audio,
        drone = drone,
    )
}
