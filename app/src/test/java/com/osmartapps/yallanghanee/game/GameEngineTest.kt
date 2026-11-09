package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.Level
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {
    private val levels = listOf(
        Level(id = 1, title = "لحن الحجاز", audio = "01.mp3"),
        Level(id = 2, title = "لحن الكرد", audio = "02.mp3"),
        Level(id = 3, title = "لحن الرست", audio = "03.mp3"),
    )

    private fun engine(startIndex: Int = 0) =
        GameEngine(levels, startIndex, Random(42))

    private fun enterAnswer(engine: GameEngine, value: String = answerLetters(engine.state.title)) {
        val used = mutableSetOf<Int>()
        value.forEach { letter ->
            val index = engine.state.tray.withIndex()
                .firstOrNull { (index, tray) -> index !in used && !tray.used && tray.letter == letter }
                ?.index
                ?: error("no available tray letter for $letter")
            used += index
            engine.onTrayLetter(index)
        }
    }

    @Test
    fun `starts at the requested level`() {
        assertEquals(1, engine(startIndex = 1).state.levelIndex)
        assertEquals(2, engine(startIndex = 99).state.levelIndex)
    }

    @Test
    fun `correct answer marks the level solved`() {
        val engine = engine()
        enterAnswer(engine)

        assertTrue(engine.state.solved)
        assertFalse(engine.state.wrong)
        assertEquals(engine.state.totalSlots, engine.state.filledSlots)
    }

    @Test
    fun `wrong answer sets wrong state and clear resets the board`() {
        val engine = engine()
        enterAnswer(engine, answerLetters(engine.state.title).reversed())

        assertTrue(engine.state.wrong)
        assertFalse(engine.state.solved)

        engine.clearWrong()

        assertFalse(engine.state.wrong)
        assertEquals(0, engine.state.filledSlots)
        assertEquals(0, engine.state.tray.count { it.used })
    }

    @Test
    fun `tapping a filled slot returns the letter to the tray`() {
        val engine = engine()
        val firstLetter = answerLetters(engine.state.title).first()
        val trayIndex = engine.state.tray.indexOfFirst { !it.used && it.letter == firstLetter }

        engine.onTrayLetter(trayIndex)
        assertEquals(1, engine.state.filledSlots)

        engine.onSlotClick(0, 0)

        assertEquals(0, engine.state.filledSlots)
        assertFalse(engine.state.tray[trayIndex].used)
    }

    @Test
    fun `next advances only after solving`() {
        val engine = engine()
        engine.next()
        assertEquals(0, engine.state.levelIndex)

        enterAnswer(engine)
        engine.next()

        assertEquals(1, engine.state.levelIndex)
        assertFalse(engine.state.solved)
    }

    @Test
    fun `solving the final level completes the pack`() {
        val engine = engine(startIndex = 2)
        enterAnswer(engine)

        assertTrue(engine.state.solved)
        assertTrue(engine.state.allCompleted)

        engine.next()
        assertEquals(2, engine.state.levelIndex)
    }

    @Test
    fun `restart returns to the first level`() {
        val engine = engine(startIndex = 2)
        enterAnswer(engine)
        engine.restart()

        assertEquals(0, engine.state.levelIndex)
        assertFalse(engine.state.solved)
        assertFalse(engine.state.allCompleted)
    }

    @Test
    fun `hint fills the next slot with the correct letter`() {
        val engine = engine()
        val expected = answerLetters(engine.state.title)

        assertTrue(engine.revealHint())

        assertEquals(1, engine.state.filledSlots)
        assertEquals(expected.first().toString(), engine.state.words[0].slots[0].filled.toString())
        assertEquals(1, engine.state.tray.count { it.used })
    }

    @Test
    fun `hints can solve the whole level`() {
        val engine = engine()
        val total = engine.state.totalSlots

        repeat(total) { assertTrue(engine.revealHint()) }

        assertTrue(engine.state.solved)
        assertEquals(total, engine.state.filledSlots)
    }

    @Test
    fun `hint is ignored after the level is solved`() {
        val engine = engine()
        enterAnswer(engine)

        assertFalse(engine.revealHint())
    }
}
