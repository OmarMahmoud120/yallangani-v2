package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.Level
import kotlin.random.Random

data class AnswerSlot(
    val letter: Char,
    val filled: Char? = null,
    val trayIndex: Int? = null,
)

data class AnswerWord(val slots: List<AnswerSlot>)

data class TrayLetter(val letter: Char, val used: Boolean = false)

data class GameState(
    val levelIndex: Int,
    val totalLevels: Int,
    val levelId: Int,
    val title: String,
    val audioFile: String,
    val words: List<AnswerWord>,
    val tray: List<TrayLetter>,
    val solved: Boolean = false,
    val wrong: Boolean = false,
    val allCompleted: Boolean = false,
) {
    val totalSlots: Int get() = words.sumOf { it.slots.size }
    val filledSlots: Int get() = words.sumOf { word -> word.slots.count { it.filled != null } }
}

class GameEngine(
    private val levels: List<Level>,
    startIndex: Int = 0,
    private val random: Random = Random.Default,
) {
    init {
        require(levels.isNotEmpty()) { "levels must not be empty" }
    }

    var state: GameState = buildState(startIndex.coerceIn(0, levels.lastIndex))
        private set

    fun onTrayLetter(trayIndex: Int) {
        if (state.solved || state.wrong) return
        val trayLetter = state.tray.getOrNull(trayIndex) ?: return
        if (trayLetter.used) return
        val (wordIndex, slotIndex) = firstEmptySlot() ?: return

        val words = state.words.mapIndexed { word, current ->
            if (word != wordIndex) {
                current
            } else {
                AnswerWord(
                    current.slots.mapIndexed { slot, item ->
                        if (slot != slotIndex) {
                            item
                        } else {
                            item.copy(filled = trayLetter.letter, trayIndex = trayIndex)
                        }
                    },
                )
            }
        }
        val tray = state.tray.mapIndexed { index, item ->
            if (index == trayIndex) item.copy(used = true) else item
        }
        state = state.copy(words = words, tray = tray)

        if (state.filledSlots == state.totalSlots) {
            checkAnswer()
        }
    }

    fun onSlotClick(wordIndex: Int, slotIndex: Int) {
        if (state.solved || state.wrong) return
        val slot = state.words.getOrNull(wordIndex)?.slots?.getOrNull(slotIndex) ?: return
        val trayIndex = slot.trayIndex ?: return

        val words = state.words.mapIndexed { word, current ->
            if (word != wordIndex) {
                current
            } else {
                AnswerWord(
                    current.slots.mapIndexed { index, item ->
                        if (index == slotIndex) item.copy(filled = null, trayIndex = null) else item
                    },
                )
            }
        }
        val tray = state.tray.mapIndexed { index, item ->
            if (index == trayIndex) item.copy(used = false) else item
        }
        state = state.copy(words = words, tray = tray)
    }

    fun clearWrong() {
        if (!state.wrong) return
        state = buildState(state.levelIndex)
    }

    fun revealHint(): Boolean {
        if (state.solved || state.wrong) return false
        val expected = answerLetters(levels[state.levelIndex].title)
        var flatIndex = 0
        state.words.forEachIndexed { wordIndex, word ->
            word.slots.forEachIndexed { slotIndex, slot ->
                if (slot.filled == null) {
                    val letter = expected.getOrNull(flatIndex) ?: return false
                    val trayIndex = state.tray.indexOfFirst { !it.used && it.letter == letter }
                    if (trayIndex < 0) return false
                    val words = state.words.mapIndexed { index, current ->
                        if (index != wordIndex) {
                            current
                        } else {
                            AnswerWord(
                                current.slots.mapIndexed { index, item ->
                                    if (index != slotIndex) item
                                    else item.copy(filled = letter, trayIndex = trayIndex)
                                },
                            )
                        }
                    }
                    val tray = state.tray.mapIndexed { index, item ->
                        if (index == trayIndex) item.copy(used = true) else item
                    }
                    state = state.copy(words = words, tray = tray)
                    if (state.filledSlots == state.totalSlots) checkAnswer()
                    return true
                }
                flatIndex++
            }
        }
        return false
    }

    fun next() {
        if (!state.solved || state.allCompleted) return
        state = buildState(state.levelIndex + 1)
    }

    fun restart() {
        state = buildState(0)
    }

    private fun checkAnswer() {
        val entered = state.words.joinToString("") { word ->
            word.slots.joinToString("") { slot -> slot.filled?.toString().orEmpty() }
        }
        val level = levels[state.levelIndex]
        state = if (isCorrectAnswer(entered, level.title)) {
            state.copy(solved = true, allCompleted = state.levelIndex == levels.lastIndex)
        } else {
            state.copy(wrong = true)
        }
    }

    private fun firstEmptySlot(): Pair<Int, Int>? {
        state.words.forEachIndexed { wordIndex, word ->
            word.slots.forEachIndexed { slotIndex, slot ->
                if (slot.filled == null) return wordIndex to slotIndex
            }
        }
        return null
    }

    private fun buildState(index: Int): GameState {
        val level = levels[index]
        val words = normalizeArabic(level.title)
            .split(' ')
            .filter { it.isNotBlank() }
            .map { word ->
                AnswerWord(word.filter { it.isYallaLetter() }.map { AnswerSlot(it) })
            }
        check(words.isNotEmpty() && words.all { it.slots.isNotEmpty() }) {
            "level ${level.id} has no usable letters"
        }
        val tray = buildTray(level.title, random = random).map { TrayLetter(it) }
        return GameState(
            levelIndex = index,
            totalLevels = levels.size,
            levelId = level.id,
            title = level.title,
            audioFile = level.audio,
            words = words,
            tray = tray,
        )
    }
}
