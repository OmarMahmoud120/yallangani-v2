package com.osmartapps.yallanghanee.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.util.Log
import java.io.File

class AudioPlayer(
    private val context: Context,
    private val clipCacheDir: File = File(context.filesDir, "pack/audio"),
) {
    private var player: MediaPlayer? = null
    private var prepared = false
    private var queue: List<String> = emptyList()
    private var queueIndex = 0
    private var speed = 1f
    private var onStarted: (() -> Unit)? = null
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
    val isActive: Boolean get() = player != null
    val isDronePlaying: Boolean get() = dronePlayer?.isPlaying == true

    fun positionMs(): Int = if (!prepared) 0 else runCatching { player?.currentPosition ?: 0 }.getOrDefault(0)

    fun durationMs(): Int = if (!prepared) 0 else runCatching { player?.duration ?: 0 }.getOrDefault(0)

    fun play(
        source: String,
        speed: Float = 1f,
        onStarted: () -> Unit = {},
        onFinished: () -> Unit = {},
        onInterrupted: () -> Unit = {},
        onError: () -> Unit = {},
    ) {
        playSequence(listOf(source), speed, onStarted, onFinished, onInterrupted, onError)
    }

    fun playSequence(
        sources: List<String>,
        speed: Float = 1f,
        onStarted: () -> Unit = {},
        onFinished: () -> Unit = {},
        onInterrupted: () -> Unit = {},
        onError: () -> Unit = {},
    ) {
        stopPhrase()
        if (sources.isEmpty()) {
            onError()
            return
        }
        queue = sources
        queueIndex = 0
        this.speed = speed
        this.onStarted = onStarted
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
        onStarted = null
        onFinished = null
        onInterrupted = null
        onError = null
        releasePlayer()
        abandonFocusIfIdle()
    }

    fun setSpeed(newSpeed: Float) {
        speed = newSpeed
        if (!prepared) return
        runCatching {
            player?.playbackParams = PlaybackParams().setSpeed(newSpeed).setPitch(1f)
        }.onFailure { Log.w(TAG, "cannot change speed", it) }
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
        val source = queue[queueIndex++]
        val created = MediaPlayer()
        val configured = runCatching {
            setSource(created, source)
            created.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
        }
        if (configured.isFailure) {
            Log.w(TAG, "cannot set source: $source", configured.exceptionOrNull())
            runCatching { created.release() }
            fail()
            return
        }
        player = created
        created.setOnCompletionListener {
            releasePlayer()
            playNext()
        }
        created.setOnErrorListener { _, what, extra ->
            Log.w(TAG, "playback error what=$what extra=$extra source=$source")
            releasePlayer()
            fail()
            true
        }
        if (isRemote(source)) {
            created.setOnPreparedListener { prepared ->
                prepared.setOnPreparedListener(null)
                startPrepared(prepared, source)
            }
            if (runCatching { created.prepareAsync() }.isFailure) {
                Log.w(TAG, "prepareAsync failed: $source")
                fail()
            }
            return
        }
        if (runCatching { created.prepare() }.isFailure) {
            fail()
            return
        }
        startPrepared(created, source)
    }

    private fun startPrepared(created: MediaPlayer, source: String) {
        if (player !== created) return
        prepared = true
        if (speed != 1f) {
            runCatching { created.playbackParams = PlaybackParams().setSpeed(speed).setPitch(1f) }
        }
        if (!requestFocus()) {
            Log.w(TAG, "audio focus denied: $source")
            fail()
            return
        }
        if (runCatching { created.start() }.isFailure) {
            Log.w(TAG, "start failed: $source")
            fail()
            return
        }
        noisy.start()
        onStarted?.invoke()
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
        if (isRemote(assetPath)) {
            created.setDataSource(assetPath)
            return
        }
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

    private fun isRemote(source: String): Boolean = isRemoteSource(source)

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
        prepared = false
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

    private companion object {
        const val TAG = "AudioPlayer"
    }
}
