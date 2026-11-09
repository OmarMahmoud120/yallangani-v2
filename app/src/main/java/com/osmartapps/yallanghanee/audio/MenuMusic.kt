package com.osmartapps.yallanghanee.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer

class MenuMusic(context: Context) {
    private val player = MediaPlayer().apply {
        context.assets.openFd("sounds/main.mp3").use { descriptor ->
            setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        }
        setVolume(0.5f, 0.5f)
        isLooping = true
        prepare()
    }
    private val focus = AudioFocus(context, AudioManager.AUDIOFOCUS_GAIN) { pause() }
    private val noisy = NoisyAudioReceiver(context) { pause() }

    fun start() {
        if (player.isPlaying) return
        if (focus.request() && runCatching { player.start() }.isSuccess) {
            noisy.start()
        }
    }

    fun pause() {
        focus.abandon()
        noisy.stop()
        if (player.isPlaying) {
            runCatching { player.pause() }
        }
    }

    fun release() {
        pause()
        runCatching { player.release() }
    }
}
