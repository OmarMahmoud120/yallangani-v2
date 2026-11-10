package com.osmartapps.yallanghanee.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class SigilLetterTest {
    @Test
    fun keepsPlainNames() {
        assertEquals("ح", sigilLetter("حجاز"))
        assertEquals("R", sigilLetter("Rast"))
    }

    @Test
    fun skipsLeadingApostrophes() {
        assertEquals("A", sigilLetter("'Ajam"))
        assertEquals("I", sigilLetter("’Iraq"))
    }

    @Test
    fun handlesNamesWithoutLetters() {
        assertEquals("'", sigilLetter("'"))
        assertEquals("", sigilLetter(""))
    }
}
