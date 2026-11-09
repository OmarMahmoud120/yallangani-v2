package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.game.answerLetters
import com.osmartapps.yallanghanee.game.disallowedCharacters
import com.osmartapps.yallanghanee.model.ContentPack

fun ContentPack.validationErrors(): List<String> = buildList {
    if (version < 1) add("version must be >= 1")
    if (question.isBlank()) add("question must not be blank")
    if (levels.isEmpty()) add("levels must not be empty")

    val ids = mutableSetOf<Int>()
    levels.forEach { level ->
        if (level.id <= 0) add("level id must be positive: ${level.id}")
        if (!ids.add(level.id)) add("duplicate level id: ${level.id}")
        if (level.title.isBlank()) add("level ${level.id}: title is blank")
        if (answerLetters(level.title).isEmpty()) {
            add("level ${level.id}: title has no usable Arabic letters")
        }
        val invalid = disallowedCharacters(level.title)
        if (invalid.isNotEmpty()) {
            add("level ${level.id}: unsupported characters ${invalid.distinct()}")
        }
        if (level.audio.isBlank()) add("level ${level.id}: audio is blank")
        if (level.audio.contains("..") || level.audio.contains('/') || level.audio.contains('\\')) {
            add("level ${level.id}: audio must be a plain file name")
        }
    }
}
