package com.osmartapps.spike

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.osmartapps.spike.audio.AudioLatencyProbe
import com.osmartapps.spike.ui.AudioLatencyScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            MaterialTheme {
                Surface {
                    val probe = remember { AudioLatencyProbe(this) }
                    DisposableEffect(probe) {
                        onDispose { probe.release() }
                    }
                    AudioLatencyScreen(probe)
                }
            }
        }
    }
}
