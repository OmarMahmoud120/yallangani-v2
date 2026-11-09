package com.osmartapps.spike.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTimestamp
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import java.util.Locale

/**
 * Measures tap-to-first-frame latency of a preloaded static [AudioTrack].
 * The tap timestamp comes from the input event's uptime clock; presentation time comes from
 * [AudioTrack.getTimestamp], which reports when the first frame reached the output device.
 */
class AudioLatencyProbe(context: Context) {

    data class Trial(
        val index: Int,
        val tapNanos: Long,
        val latencyMs: Double?,
        val method: String,
    )

    private val audioManager = context.getSystemService(AudioManager::class.java)

    val sampleRate: Int = audioManager
        .getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        ?.toIntOrNull() ?: 48_000

    val framesPerBuffer: Int = audioManager
        .getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        ?.toIntOrNull() ?: 0

    private val minBufferBytes: Int = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT,
    ).coerceAtLeast(0)

    // The fast mixer only accepts stereo output on this platform, so duplicate the mono click.
    // The click sits at frame 0 and the rest is silence: the pad keeps the static track playing
    // long enough for getTimestamp to report a frame, without delaying the first frame.
    private val pcm: ShortArray = ClickSynth.render(sampleRate).let { mono ->
        val frames = sampleRate * PAD_MS / 1000
        val stereo = ShortArray(frames * 2)
        for (i in mono.indices) {
            stereo[i * 2] = mono[i]
            stereo[i * 2 + 1] = mono[i]
        }
        stereo
    }

    private val trackInfo: Pair<AudioTrack, String>? = createTrack()

    private val track: AudioTrack? = trackInfo?.first

    val performanceMode: String = trackInfo?.second ?: "unavailable"

    private fun createTrack(): Pair<AudioTrack, String>? {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()

        fun build(lowLatency: Boolean): AudioTrack {
            val builder = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.size * 2)
            if (lowLatency) {
                builder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            }
            return builder.build().also { it.write(pcm, 0, pcm.size) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { build(true) }.getOrNull()?.let { return it to "low_latency" }
        }
        runCatching { build(false) }
            .onFailure { Log.e(TAG, "audio track init failed", it) }
            .getOrNull()?.let { return it to "normal" }
        return null
    }

    init {
        Log.i(
            TAG,
            json(
                "audio_config",
                mapOf(
                    "model" to Build.MODEL,
                    "device" to Build.DEVICE,
                    "sdk" to Build.VERSION.SDK_INT,
                    "sampleRate" to sampleRate,
                    "framesPerBuffer" to framesPerBuffer,
                    "bufferMs" to if (framesPerBuffer > 0) framesPerBuffer * 1000.0 / sampleRate else null,
                    "performanceMode" to performanceMode,
                    "minBufferFrames" to (if (minBufferBytes > 0) minBufferBytes / 4 else 0),
                    "minBufferMs" to (if (minBufferBytes > 0) minBufferBytes / 4 * 1000.0 / sampleRate else null),
                    "trackAvailable" to (track != null),
                ),
            ),
        )
    }

    fun measure(tapNanos: Long, index: Int): Trial {
        val audioTrack = track ?: return Trial(index, tapNanos, null, "unavailable")
        // Rewind the static buffer; reloadStaticData() also stops any in-flight playback.
        runCatching { audioTrack.reloadStaticData() }

        val startedNanos = System.nanoTime()
        val played = runCatching { audioTrack.play() }.isSuccess
        if (!played) return Trial(index, tapNanos, null, "play_failed")

        val stamp = AudioTimestamp()
        var presentedNanos: Long? = null
        var stampFrame = -1L
        var headNanos: Long? = null
        var method = "timestamp"
        val deadline = startedNanos + TIMEOUT_NANOS

        while (System.nanoTime() < deadline) {
            if (headNanos == null && audioTrack.playbackHeadPosition > 0) {
                headNanos = System.nanoTime()
            }
            if (audioTrack.getTimestamp(stamp) && stamp.framePosition > 0) {
                presentedNanos = stamp.nanoTime
                stampFrame = stamp.framePosition
                break
            }
            Thread.sleep(1)
        }

        if (presentedNanos == null) {
            method = "head_fallback"
            presentedNanos = headNanos
        }
        runCatching { audioTrack.stop() }
        // getTimestamp reports the last frame played; back off that frame's offset to get
        // the presentation time of the first frame in the buffer.
        val firstFrameNanos = presentedNanos?.let { presented ->
            if (stampFrame > 0) presented - stampFrame * 1_000_000_000L / sampleRate else presented
        }
        val dispatchMs = (startedNanos - tapNanos) / 1_000_000.0
        val audioMs = firstFrameNanos?.let { (it - startedNanos) / 1_000_000.0 }
        val totalMs = firstFrameNanos?.let { (it - tapNanos) / 1_000_000.0 }
        Log.i(
            TAG,
            json(
                "audio_latency",
                mapOf(
                    "trial" to index,
                    "tapNanos" to tapNanos,
                    "dispatchMs" to String.format(Locale.US, "%.2f", dispatchMs),
                    "audioMs" to audioMs?.let { String.format(Locale.US, "%.2f", it) },
                    "headMs" to headNanos?.let { String.format(Locale.US, "%.2f", (it - startedNanos) / 1_000_000.0) },
                    "rawStampMs" to presentedNanos?.let { String.format(Locale.US, "%.2f", (it - startedNanos) / 1_000_000.0) },
                    "stampFrame" to stampFrame,
                    "totalMs" to totalMs?.let { String.format(Locale.US, "%.2f", it) },
                    "method" to method,
                ),
            ),
        )
        return Trial(index, tapNanos, totalMs, method)
    }

    fun release() {
        runCatching { track?.stop() }
        runCatching { track?.release() }
    }

    private fun json(event: String, fields: Map<String, Any?>): String = buildString {
        append("{\"e\":\"").append(event).append('"')
        fields.forEach { (key, value) ->
            append(",\"").append(key).append("\":")
            when (value) {
                null -> append("null")
                is Number, is Boolean -> append(value)
                else -> append('"').append(value.toString().replace("\"", "")).append('"')
            }
        }
        append('}')
    }

    companion object {
        const val TAG = "SpikeLatency"
        private const val TIMEOUT_NANOS = 300_000_000L
        private const val PAD_MS = 250
    }
}
