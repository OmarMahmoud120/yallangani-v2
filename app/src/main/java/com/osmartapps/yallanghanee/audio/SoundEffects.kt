package com.osmartapps.yallanghanee.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundEffects(context: Context) {
    private val descriptor = runCatching { context.assets.openFd("sounds/fireworks.mp3") }.getOrNull()

    private val pool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val fireworksId = descriptor?.let { pool.load(it, 1) } ?: 0

    fun playWin() {
        if (fireworksId != 0) {
            pool.play(fireworksId, 0.8f, 0.8f, 1, 0, 1f)
        }
    }

    fun release() {
        pool.release()
        runCatching { descriptor?.close() }
    }
}
