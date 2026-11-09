package com.osmartapps.yallanghanee.game

import kotlin.random.Random

const val TRAY_ROW_SIZE = 8
const val TRAY_SIZE = 16

const val ARABIC_ALPHABET = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي"

private val ARABIC_LETTERS = ARABIC_ALPHABET.toSet()

private val DIACRITICS = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")
private val WHITESPACE = Regex("\\s+")

fun Char.isYallaLetter(): Boolean = this in ARABIC_LETTERS

fun normalizeArabic(input: String): String =
    input.trim()
        .replace(DIACRITICS, "")
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ٱ', 'ا')
        .replace('ى', 'ي')
        .replace('ة', 'ه')
        .replace('ؤ', 'و')
        .replace('ئ', 'ي')
        .replace(WHITESPACE, " ")

fun answerLetters(title: String): String =
    normalizeArabic(title).filter { it.isYallaLetter() }

fun disallowedCharacters(title: String): List<Char> =
    normalizeArabic(title).filter { !it.isWhitespace() && !it.isYallaLetter() }.toList()

fun buildTray(
    title: String,
    size: Int = TRAY_SIZE,
    random: Random = Random.Default,
): List<Char> {
    val letters = answerLetters(title).toMutableList()
    val target = maxOf(size, ((letters.size + TRAY_ROW_SIZE - 1) / TRAY_ROW_SIZE) * TRAY_ROW_SIZE)
    while (letters.size < target) {
        letters += ARABIC_ALPHABET[random.nextInt(ARABIC_ALPHABET.length)]
    }
    letters.shuffle(random)
    return letters
}

fun isCorrectAnswer(entered: String, title: String): Boolean {
    val expected = answerLetters(title)
    val actual = normalizeArabic(entered).filter { it.isYallaLetter() }
    return expected.isNotEmpty() && expected == actual
}
