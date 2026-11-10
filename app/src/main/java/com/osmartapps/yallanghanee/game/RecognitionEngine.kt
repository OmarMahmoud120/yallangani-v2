package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.RecognitionItem
import com.osmartapps.yallanghanee.model.RecognitionLesson
import kotlin.random.Random

data class RecognitionQuestion(
    val item: RecognitionItem,
    val answer: Maqam,
    val options: List<Maqam>,
) {
    val audio: String get() = item.audio?.let { "$AUDIO_DIR/$it" } ?: audioFor(answer)
    val droneAudio: String? get() = answer.drone?.let { "$AUDIO_DIR/$it" }
    val explanation: String get() = item.explanationAr.ifBlank { answer.signatureAr }
    val contrast: Maqam get() = options.first { it.id != answer.id }
    val contrastAudio: String get() = audioFor(contrast)

    fun audioFor(maqam: Maqam): String = "$AUDIO_DIR/${maqam.audio}"
}

data class RecognitionSession(
    val lesson: RecognitionLesson,
    val questions: List<RecognitionQuestion>,
    val index: Int = 0,
    val selectedId: String? = null,
    val correctCount: Int = 0,
    val finished: Boolean = false,
) {
    val question: RecognitionQuestion get() = questions[index]
    val answered: Boolean get() = selectedId != null
    val answeredCorrectly: Boolean get() = selectedId == question.answer.id
    val total: Int get() = questions.size
}

private const val AUDIO_DIR = "content/audio"

object RecognitionEngine {

    fun start(
        lesson: RecognitionLesson,
        pack: MaqamPack,
        random: Random = Random.Default,
    ): RecognitionSession {
        require(lesson.items.isNotEmpty()) { "lesson has no items: ${lesson.id}" }
        val options = lesson.maqamIds.map { id ->
            requireNotNull(pack.maqamById(id)) { "unknown maqam: $id" }
        }
        require(options.size >= 2) { "lesson needs two options: ${lesson.id}" }
        val questions = lesson.items.map { item ->
            RecognitionQuestion(
                item = item,
                answer = requireNotNull(pack.maqamById(item.answer)) { "unknown answer: ${item.answer}" },
                options = options.shuffled(random),
            )
        }
        return RecognitionSession(lesson = lesson, questions = questions)
    }

    fun answer(session: RecognitionSession, maqamId: String): RecognitionSession {
        require(!session.answered) { "item already answered" }
        require(session.question.options.any { it.id == maqamId }) { "option not offered: $maqamId" }
        val correct = maqamId == session.question.answer.id
        return session.copy(
            selectedId = maqamId,
            correctCount = session.correctCount + if (correct) 1 else 0,
        )
    }

    fun next(session: RecognitionSession): RecognitionSession {
        require(session.answered) { "answer the item first" }
        return advance(session)
    }

    fun skip(session: RecognitionSession): RecognitionSession {
        require(!session.answered) { "item already answered" }
        return advance(session)
    }

    private fun advance(session: RecognitionSession): RecognitionSession =
        if (session.index == session.questions.lastIndex) {
            session.copy(finished = true)
        } else {
            session.copy(index = session.index + 1, selectedId = null)
        }
}
