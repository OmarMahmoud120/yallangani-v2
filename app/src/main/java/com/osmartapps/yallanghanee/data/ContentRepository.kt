package com.osmartapps.yallanghanee.data

import android.content.Context
import com.osmartapps.yallanghanee.model.ContentPack
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

class ContentRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): ContentPack {
        val raw = context.assets.open(LEVELS_ASSET).bufferedReader().use { it.readText() }
        val pack = json.decodeFromString<ContentPack>(raw)
        val errors = pack.validationErrors()
        check(errors.isEmpty()) { "Invalid content pack: ${errors.joinToString("; ")}" }

        pack.levels.forEach { level ->
            val path = "$AUDIO_DIR/${level.audio}"
            try {
                context.assets.open(path).close()
            } catch (error: IOException) {
                throw IllegalStateException("Missing audio asset: $path", error)
            }
        }
        return pack
    }

    companion object {
        const val LEVELS_ASSET = "content/levels.json"
        const val AUDIO_DIR = "content/audio"
    }
}
