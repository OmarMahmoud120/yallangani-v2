package com.osmartapps.yallanghanee.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import java.io.File

class AudioPlayer(
    private val context: Context,
    private val clipCacheDir: File = File(context.filesDir, "pack/audio"),
) {
    private var player: MediaPlayer? = null
    private var queue: List<String> = emptyList()
    private var queueIndex = 0
    private var speed = 1f
    private var onFinished: (() -> Unit)? = null
    private var onInterrupted: (() -> Unit)? = null
    private var onError: (() -> Unit)? = null

    private var dronePlayer: MediaPlayer? = null
    private var dronePath: String? = null
    private var onDroneStopped: (() -> Unit)? = null

    private var focusHeld = false

    private val focus = AudioFocus(context, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
        interrupt()
    }
    private val noisy = NoisyAudioReceiver(context) { interrupt() }

    val isPlaying: Boolean get() = player?.isPlaying == true
    val isDronePlaying: Boolean get() = dronePlayer?.isPlaying == true

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
        stopPhrase()
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

    fun startDrone(assetPath: String, volume: Float, onStopped: () -> Unit = {}) {
        if (dronePath == assetPath && dronePlayer?.isPlaying == true) {
            onDroneStopped = onStopped
            return
        }
        onDroneStopped = null
        releaseDrone()
        val created = runCatching { createDrone(assetPath, volume) }.getOrNull()
        if (created == null) {
            abandonFocusIfIdle()
            onStopped()
            return
        }
        dronePlayer = created
        dronePath = assetPath
        onDroneStopped = onStopped
        created.setOnErrorListener { _, _, _ ->
            failDrone()
            true
        }
        if (!requestFocus()) {
            onDroneStopped = null
            releaseDrone()
            dronePath = null
            abandonFocusIfIdle()
            onStopped()
            return
        }
        if (runCatching { created.start() }.isFailure) {
            onDroneStopped = null
            releaseDrone()
            dronePath = null
            abandonFocusIfIdle()
            onStopped()
            return
        }
        noisy.start()
    }

    fun stopDrone() {
        onDroneStopped = null
        releaseDrone()
        dronePath = null
        abandonFocusIfIdle()
    }

    fun stopPhrase() {
        queue = emptyList()
        queueIndex = 0
        onFinished = null
        onInterrupted = null
        onError = null
        releasePlayer()
        abandonFocusIfIdle()
    }

    fun stop() {
        stopPhrase()
        stopDrone()
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
        if (!requestFocus()) {
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
        setSource(created, assetPath)
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

    private fun createDrone(assetPath: String, volume: Float): MediaPlayer {
        val created = MediaPlayer()
        setSource(created, assetPath)
        created.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        created.prepare()
        created.isLooping = true
        created.setVolume(volume, volume)
        return created
    }

    private fun setSource(created: MediaPlayer, assetPath: String) {
        val fromAssets = runCatching {
            context.assets.openFd(assetPath).use { descriptor ->
                created.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            }
        }
        if (fromAssets.isSuccess) return
        val cached = File(clipCacheDir, assetPath.substringAfterLast('/'))
        check(cached.isFile) { "missing clip: $assetPath" }
        created.setDataSource(cached.absolutePath)
    }

    private fun requestFocus(): Boolean {
        if (focusHeld) return true
        focusHeld = focus.request()
        return focusHeld
    }

    private fun abandonFocusIfIdle() {
        if (player == null && dronePlayer == null && focusHeld) {
            focus.abandon()
            focusHeld = false
            noisy.stop()
        }
    }

    private fun finish() {
        val callback = onFinished
        stopPhrase()
        callback?.invoke()
    }

    private fun fail() {
        val callback = onError
        stopPhrase()
        callback?.invoke()
    }

    private fun failDrone() {
        val callback = onDroneStopped
        onDroneStopped = null
        releaseDrone()
        dronePath = null
        abandonFocusIfIdle()
        callback?.invoke()
    }

    private fun interrupt() {
        val phraseCallback = onInterrupted
        val droneCallback = onDroneStopped
        val phraseWasPlaying = player?.isPlaying == true
        val droneWasPlaying = dronePlayer?.isPlaying == true
        if (!phraseWasPlaying && !droneWasPlaying) return
        stopPhrase()
        onDroneStopped = null
        releaseDrone()
        dronePath = null
        abandonFocusIfIdle()
        if (phraseWasPlaying) phraseCallback?.invoke()
        if (droneWasPlaying) droneCallback?.invoke()
    }

    private fun releasePlayer() {
        player?.let { current ->
            runCatching { current.stop() }
            runCatching { current.release() }
        }
        player = null
    }

    private fun releaseDrone() {
        dronePlayer?.let { current ->
            runCatching { current.stop() }
            runCatching { current.release() }
        }
        dronePlayer = null
    }
}
