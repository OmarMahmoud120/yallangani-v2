package com.osmartapps.yallanghanee.data

import android.content.res.AssetManager
import com.osmartapps.yallanghanee.model.MaqamPack

class MaqamRepository(
    private val assets: AssetManager,
    cache: PackCache? = null,
    remote: RemotePackSource? = null,
) {

    private val updater = if (cache != null && remote != null) {
        PackUpdater(cache, remote, ::assetExists)
    } else {
        null
    }

    fun load(): MaqamPack {
        val raw = assets.open(PACK_PATH).bufferedReader().use { it.readText() }
        return decode(raw).also { pack ->
            val errors = MaqamPackValidator.validate(pack, ::assetExists)
            check(errors.isEmpty()) { errors.joinToString("; ") }
        }
    }

    fun loadBaseline(): MaqamPack {
        val bundled = load()
        val cached = updater?.loadCached() ?: return bundled
        return if (cached.publishedAt.orEmpty() > bundled.publishedAt.orEmpty()) cached else bundled
    }

    fun refresh(current: MaqamPack): MaqamPack? = updater?.refresh(current)

    private fun assetExists(name: String): Boolean =
        runCatching { assets.open("$AUDIO_DIR/$name").close() }.isSuccess

    private fun decode(raw: String): MaqamPack = PackUpdater.JSON.decodeFromString<MaqamPack>(raw)

    private companion object {
        const val PACK_PATH = "content/maqams.json"
        const val AUDIO_DIR = "content/audio"
    }
}
