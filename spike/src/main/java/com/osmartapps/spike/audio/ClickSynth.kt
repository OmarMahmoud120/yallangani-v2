package com.osmartapps.spike.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ClickSynth {
    private const val DURATION_MS = 8
    private const val FREQUENCY_HZ = 1_000.0
    private const val ATTACK_MS = 0.5
    private const val DECAY_MS = 3.0

    fun render(sampleRate: Int): ShortArray {
        val count = sampleRate * DURATION_MS / 1000
        val attackSamples = (sampleRate * ATTACK_MS / 1000).toInt().coerceAtLeast(1)
        val decaySamples = sampleRate * DECAY_MS / 1000
        return ShortArray(count) { index ->
            val attack = if (index < attackSamples) index.toDouble() / attackSamples else 1.0
            val decay =
                if (index < attackSamples) 1.0
                else exp(-(index - attackSamples).toDouble() / decaySamples)
            val sample = sin(2.0 * PI * FREQUENCY_HZ * index / sampleRate) * attack * decay * 0.6
            (sample * Short.MAX_VALUE).toInt().toShort()
        }
    }
}
