package com.osmartapps.yallanghanee.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ArabicTextTest {
    @Test
    fun `normalizes hamza variants`() {
        assertEquals("احمد", normalizeArabic("أحمد"))
        assertEquals("ايمان", normalizeArabic("إيمان"))
        assertEquals("امال", normalizeArabic("آمال"))
    }

    @Test
    fun `normalizes ta marbuta and alef maqsura`() {
        assertEquals("مدرسه", normalizeArabic("مدرسة"))
        assertEquals("ليلي", normalizeArabic("ليلى"))
    }

    @Test
    fun `strips diacritics and tatweel`() {
        assertEquals("قمر", normalizeArabic("قَمَر"))
        assertEquals("فاطمه", normalizeArabic("فاطـــمة"))
    }

    @Test
    fun `builds a tray containing every answer letter`() {
        val title = "لحن الحجاز"
        val tray = buildTray(title, random = Random(7))
        val actual = tray.groupingBy { it }.eachCount()
        val expected = answerLetters(title).groupingBy { it }.eachCount()

        assertEquals(16, tray.size)
        expected.forEach { (letter, count) ->
            assertTrue("missing $letter", (actual[letter] ?: 0) >= count)
        }
    }

    @Test
    fun `accepts answers while ignoring spaces and diacritics`() {
        assertTrue(isCorrectAnswer("لحنالحجاز", "لحن الحجاز"))
        assertTrue(isCorrectAnswer("لَحْنُ الحِجَاز", "لحن الحجاز"))
        assertFalse(isCorrectAnswer("لحن الكرد", "لحن الحجاز"))
    }

    @Test
    fun `long titles still produce full rows`() {
        val tray = buildTray("لحن البنجكاه", random = Random(1))
        assertEquals(0, tray.size % TRAY_ROW_SIZE)
        assertTrue(tray.size >= TRAY_SIZE)
    }

    @Test
    fun `flags unsupported characters`() {
        assertEquals(emptyList<Char>(), disallowedCharacters("لحن الحجاز"))
        assertTrue(disallowedCharacters("Song 1").isNotEmpty())
    }
}
