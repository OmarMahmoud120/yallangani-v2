package com.osmartapps.yallanghanee.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var onInterrupted: (() -> Unit)? = null
    private val focus = AudioFocus(context, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
        pauseForInterruption()
    }
    private val noisy = NoisyAudioReceiver(context) { pauseForInterruption() }

    /** Returns true when playback is running after the call. */
    fun toggle(
        assetPath: String,
        onFinished: () -> Unit,
        onInterrupted: () -> Unit,
    ): Boolean {
        this.onInterrupted = onInterrupted
        val current = player
        if (current != null) {
            if (current.isPlaying) {
                pause()
                return false
            }
            return start(current)
        }

        val created = runCatching { create(assetPath) }.getOrNull() ?: return false
        created.setOnCompletionListener {
            focus.abandon()
            noisy.stop()
            it.release()
            player = null
            onFinished()
        }
        player = created
        return start(created)
    }

    fun stop() {
        focus.abandon()
        noisy.stop()
        onInterrupted = null
        player?.let { current ->
            runCatching { current.stop() }
            current.release()
        }
        player = null
    }

    fun release() = stop()

    private fun start(target: MediaPlayer): Boolean {
        if (!focus.request()) return false
        return runCatching {
            target.start()
            noisy.start()
            true
        }.getOrDefault(false)
    }

    private fun pause() {
        player?.let { runCatching { it.pause() } }
        focus.abandon()
        noisy.stop()
    }

    private fun pauseForInterruption() {
        if (player?.isPlaying != true) return
        pause()
        onInterrupted?.invoke()
    }

    private fun create(assetPath: String): MediaPlayer {
        val player = MediaPlayer()
        context.assets.openFd(assetPath).use { descriptor ->
            player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        }
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        player.prepare()
        return player
    }
}
