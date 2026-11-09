package com.osmartapps.yallanghanee.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var queue: List<String> = emptyList()
    private var queueIndex = 0
    private var speed = 1f
    private var onFinished: (() -> Unit)? = null
    private var onInterrupted: (() -> Unit)? = null
    private var onError: (() -> Unit)? = null

    private val focus = AudioFocus(context, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
        interrupt()
    }
    private val noisy = NoisyAudioReceiver(context) { interrupt() }

    val isPlaying: Boolean get() = player?.isPlaying == true

    fun positionMs(): Int = runCatching { player?.currentPosition ?: 0 }.getOrDefault(0)

    fun durationMs(): Int = runCatching { player?.duration ?: 0 }.getOrDefault(0)

    fun play(
        assetPath: String,
        speed: Float = 1f,
        onFinished: () -> Unit = {},
        onInterrupted: () -> Unit = {},
        onError: () -> Unit = {},
    ) {
        playSequence(listOf(assetPath), speed, onFinished, onInterrupted, onError)
    }

    fun playSequence(
        assetPaths: List<String>,
        speed: Float = 1f,
        onFinished: () -> Unit = {},
        onInterrupted: () -> Unit = {},
        onError: () -> Unit = {},
    ) {
        stop()
        if (assetPaths.isEmpty()) {
            onError()
            return
        }
        queue = assetPaths
        queueIndex = 0
        this.speed = speed
        this.onFinished = onFinished
        this.onInterrupted = onInterrupted
        this.onError = onError
        playNext()
    }

    fun stop() {
        queue = emptyList()
        queueIndex = 0
        onFinished = null
        onInterrupted = null
        onError = null
        releasePlayer()
        focus.abandon()
        noisy.stop()
    }

    fun release() = stop()

    private fun playNext() {
        if (queueIndex >= queue.size) {
            finish()
            return
        }
        val path = queue[queueIndex++]
        val created = runCatching { create(path, speed) }.getOrNull()
        if (created == null) {
            fail()
            return
        }
        player = created
        created.setOnCompletionListener {
            releasePlayer()
            playNext()
        }
        created.setOnErrorListener { _, _, _ ->
            releasePlayer()
            fail()
            true
        }
        if (!focus.request()) {
            fail()
            return
        }
        val started = runCatching { created.start() }.isSuccess
        if (!started) {
            fail()
            return
        }
        noisy.start()
    }

    private fun create(assetPath: String, speed: Float): MediaPlayer {
        val created = MediaPlayer()
        context.assets.openFd(assetPath).use { descriptor ->
            created.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        }
        created.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        created.prepare()
        if (speed != 1f) {
            created.playbackParams = PlaybackParams().setSpeed(speed).setPitch(1f)
        }
        return created
    }

    private fun finish() {
        val callback = onFinished
        stop()
        callback?.invoke()
    }

    private fun fail() {
        val callback = onError
        stop()
        callback?.invoke()
    }

    private fun interrupt() {
        val callback = onInterrupted
        if (player?.isPlaying != true) return
        stop()
        callback?.invoke()
    }

    private fun releasePlayer() {
        player?.let { current ->
            runCatching { current.stop() }
            runCatching { current.release() }
        }
        player = null
    }
}
