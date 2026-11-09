package com.osmartapps.spike.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osmartapps.spike.audio.AudioLatencyProbe
import java.util.Locale
import kotlin.math.ceil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AudioLatencyScreen(probe: AudioLatencyProbe, modifier: Modifier = Modifier) {
    val trials = remember { mutableStateListOf<AudioLatencyProbe.Trial>() }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun tap(tapNanos: Long) {
        if (busy) return
        busy = true
        val index = trials.size + 1
        scope.launch {
            val trial = withContext(Dispatchers.Default) { probe.measure(tapNanos, index) }
            trials.add(trial)
            busy = false
        }
    }

    val completed = trials.mapNotNull { it.latencyMs }.sorted()
    val median = completed.median()
    val p95 = completed.percentile(0.95)
    val bufferMs = if (probe.framesPerBuffer > 0) {
        String.format(Locale.US, "%.1f", probe.framesPerBuffer * 1000.0 / probe.sampleRate)
    } else {
        "?"
    }

    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("AudioLatency", fontSize = 22.sp, fontFamily = FontFamily.Monospace)
        Text(
            "${Build.MODEL} · API ${Build.VERSION.SDK_INT} · ${probe.sampleRate} Hz · " +
                "frames/buffer ${probe.framesPerBuffer} (${bufferMs} ms)",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
        )
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (busy) Color(0xFF444444) else Color(0xFF1F6FEB))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        tap(down.uptimeMillis * 1_000_000L)
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (busy) "…" else "TAP",
                fontSize = 40.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "n=${completed.size}  median=${median ?: "-"}  p95=${p95 ?: "-"} ms\n" +
                "min=${completed.firstOrNull()?.let { format(it) } ?: "-"}  " +
                "max=${completed.lastOrNull()?.let { format(it) } ?: "-"} ms",
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            trials.takeLast(5).joinToString("\n") { trial ->
                "#${trial.index}  ${trial.latencyMs?.let { format(it) } ?: "n/a"} ms " +
                    "(${trial.method})"
            },
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { trials.clear() }, enabled = !busy) {
            Text("Clear")
        }
    }
}

private fun format(value: Double): String = String.format(Locale.US, "%.1f", value)

private fun List<Double>.median(): String? {
    if (isEmpty()) return null
    val middle = size / 2
    val value = if (size % 2 == 0) (this[middle - 1] + this[middle]) / 2 else this[middle]
    return format(value)
}

private fun List<Double>.percentile(fraction: Double): String? {
    if (isEmpty()) return null
    val index = (ceil(fraction * size).toInt() - 1).coerceIn(0, lastIndex)
    return format(this[index])
}
