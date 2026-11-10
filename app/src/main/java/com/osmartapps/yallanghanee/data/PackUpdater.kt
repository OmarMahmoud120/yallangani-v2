package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.MaqamPack
import kotlinx.serialization.json.Json

class PackUpdater(
    private val cache: PackCache,
    private val remote: RemotePackSource,
    private val assetExists: (String) -> Boolean = { false },
) {

    fun loadCached(): MaqamPack? {
        val raw = cache.readPack() ?: return null
        return runCatching { decode(raw) }
            .getOrNull()
            ?.takeIf { it.version == SCHEMA_VERSION && errors(it).isEmpty() }
    }

    fun refresh(current: MaqamPack): MaqamPack? {
        val body = remote.get(REMOTE_PACK_PATH, PACK_MAX_BYTES)?.toString(Charsets.UTF_8) ?: return null
        val pack = runCatching { decode(body) }.getOrNull() ?: return null
        if (pack.version != SCHEMA_VERSION) return null
        if (pack.contentVersion == null || pack.contentVersion == current.contentVersion) return null
        if (!downloadClips(pack)) return null
        if (errors(pack).isNotEmpty()) return null
        cache.writePack(body)
        cache.pruneClips(pack.clipNames())
        return pack
    }

    fun errors(pack: MaqamPack): List<String> =
        MaqamPackValidator.validate(pack) { name ->
            assetExists(name) || cache.hasClip(name)
        }

    private fun downloadClips(pack: MaqamPack): Boolean {
        var total = 0L
        for (name in pack.clipNames()) {
            if (!isSafeClipName(name)) return false
            if (assetExists(name)) continue
            val expected = pack.audioSha256[name] ?: return false
            val file = cache.clipFile(name)
            if (file.isFile && sha256(file) == expected) {
                total += file.length()
                if (total > AUDIO_CACHE_MAX_BYTES) return false
                continue
            }
            val bytes = remote.get("$REMOTE_CLIP_DIR$name", CLIP_MAX_BYTES) ?: return false
            if (sha256(bytes) != expected) return false
            total += bytes.size
            if (total > AUDIO_CACHE_MAX_BYTES) return false
            cache.writeClip(name, bytes)
        }
        return true
    }

    private fun decode(raw: String): MaqamPack = JSON.decodeFromString<MaqamPack>(raw)

    companion object {
        const val SCHEMA_VERSION = 4
        const val PACK_MAX_BYTES = 512 * 1024
        const val CLIP_MAX_BYTES = 5 * 1024 * 1024
        const val AUDIO_CACHE_MAX_BYTES = 50L * 1024 * 1024
        const val REMOTE_PACK_PATH = "/data/pack.json"
        const val REMOTE_CLIP_DIR = "/data/app-audio/"
        val JSON = Json { ignoreUnknownKeys = true }
    }
}
