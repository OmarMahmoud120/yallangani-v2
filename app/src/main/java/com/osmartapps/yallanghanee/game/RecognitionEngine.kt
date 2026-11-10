package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.AppLanguage
import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.RecognitionItem
import com.osmartapps.yallanghanee.model.RecognitionLesson
import com.osmartapps.yallanghanee.model.Track
import com.osmartapps.yallanghanee.model.explanation
import com.osmartapps.yallanghanee.model.signature
import kotlin.random.Random

data class RecognitionQuestion(
    val item: RecognitionItem,
    val answer: Maqam,
    val options: List<Maqam>,
    val track: Track?,
    val contrastTrack: Track?,
    val audio: String?,
    val contrastAudio: String?,
    val notationUrl: String?,
) {
    val droneAudio: String? get() = answer.drone?.let { "$AUDIO_DIR/$it" }
    val contrast: Maqam get() = options.first { it.id != answer.id }

    fun explanation(language: AppLanguage): String =
        item.explanation(language).ifBlank { answer.signature(language) }
}

data class RecognitionSession(
    val lesson: RecognitionLesson,
    val questions: List<RecognitionQuestion>,
    val index: Int = 0,
    val selectedId: String? = null,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val hintUsed: Boolean = false,
    val eliminatedId: String? = null,
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
            val answer = requireNotNull(pack.maqamById(item.answer)) { "unknown answer: ${item.answer}" }
            val contrast = options.first { it.id != answer.id }
            val track = answer.tracks.randomOrNull(random)
            val contrastTrack = contrast.tracks.randomOrNull(random)
            RecognitionQuestion(
                item = item,
                answer = answer,
                options = options.shuffled(random),
                track = track,
                contrastTrack = contrastTrack,
                audio = source(pack, item.audio, track, answer),
                contrastAudio = source(pack, null, contrastTrack, contrast),
                notationUrl = pack.mediaUrl(answer.notation),
            )
        }
        return RecognitionSession(lesson = lesson, questions = questions)
    }

    private fun source(pack: MaqamPack, override: String?, track: Track?, maqam: Maqam): String? =
        override?.let { "$AUDIO_DIR/$it" }
            ?: pack.mediaUrl(track?.audio)
            ?: maqam.audio?.let { "$AUDIO_DIR/$it" }

    fun answer(session: RecognitionSession, maqamId: String): RecognitionSession {
        require(!session.answered) { "item already answered" }
        require(session.question.options.any { it.id == maqamId }) { "option not offered: $maqamId" }
        require(maqamId != session.eliminatedId) { "option was eliminated: $maqamId" }
        val correct = maqamId == session.question.answer.id
        return session.copy(
            selectedId = maqamId,
            correctCount = session.correctCount + if (correct) 1 else 0,
        )
    }

    fun hintAvailable(session: RecognitionSession): Boolean =
        !session.answered && !session.hintUsed && session.question.options.size >= 3

    fun eliminateWrongOption(session: RecognitionSession): RecognitionSession {
        require(hintAvailable(session)) { "hint unavailable" }
        val wrong = session.question.options.first { it.id != session.question.answer.id }
        return session.copy(eliminatedId = wrong.id, hintUsed = true)
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
            session.copy(index = session.index + 1, selectedId = null, eliminatedId = null)
        }
}
