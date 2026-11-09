package com.osmartapps.yallanghanee.data

import android.content.res.AssetManager
import com.osmartapps.yallanghanee.model.MaqamPack
import kotlinx.serialization.json.Json

class MaqamRepository(private val assets: AssetManager) {

    fun load(): MaqamPack {
        val raw = assets.open(PACK_PATH).bufferedReader().use { it.readText() }
        val pack = JSON.decodeFromString<MaqamPack>(raw)
        val errors = MaqamPackValidator.validate(pack) { name ->
            runCatching { assets.open("$AUDIO_DIR/$name").close() }.isSuccess
        }
        check(errors.isEmpty()) { errors.joinToString("; ") }
        return pack
    }

    private companion object {
        const val PACK_PATH = "content/maqams.json"
        const val AUDIO_DIR = "content/audio"
        val JSON = Json { ignoreUnknownKeys = true }
    }
}
