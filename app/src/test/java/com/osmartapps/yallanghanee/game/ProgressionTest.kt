package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.Level
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionTest {
    private val levels = listOf(
        Level(id = 1, title = "لحن الحجاز", audio = "01.mp3"),
        Level(id = 2, title = "لحن الكرد", audio = "02.mp3"),
        Level(id = 3, title = "لحن الرست", audio = "03.mp3"),
    )

    @Test
    fun `nothing solved resumes at the first level`() {
        assertEquals(0, resumeIndex(levels, emptySet()))
    }

    @Test
    fun `first gap in the middle is resumed`() {
        assertEquals(1, resumeIndex(levels, setOf(1, 3)))
    }

    @Test
    fun `contiguous solved prefix resumes at the next level`() {
        assertEquals(2, resumeIndex(levels, setOf(1, 2)))
    }

    @Test
    fun `all solved restarts at the first level`() {
        assertEquals(0, resumeIndex(levels, setOf(1, 2, 3)))
    }

    @Test
    fun `empty level list resumes at zero`() {
        assertEquals(0, resumeIndex(emptyList(), setOf(1, 2)))
    }
}
