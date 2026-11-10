package com.osmartapps.yallanghanee.data

import java.io.File

class PackCache(root: File) {
    private val packFile = File(root, "pack.json")
    private val audioDir = File(root, "audio")

    fun readPack(): String? =
        runCatching { packFile.takeIf { it.isFile }?.readText() }.getOrNull()

    fun writePack(raw: String) {
        writeAtomically(packFile, raw.toByteArray(), "$PACK_TMP")
    }

    fun clipFile(name: String): File = File(audioDir, name)

    fun hasClip(name: String): Boolean = clipFile(name).isFile

    fun writeClip(name: String, bytes: ByteArray) {
        writeAtomically(clipFile(name), bytes, "$name.tmp")
    }

    fun pruneClips(keep: Set<String>) {
        audioDir.listFiles()?.forEach { file ->
            if (file.name !in keep && !file.name.endsWith(".tmp")) file.delete()
        }
    }

    private fun writeAtomically(target: File, bytes: ByteArray, tempName: String) {
        val parent = target.parentFile ?: return
        parent.mkdirs()
        val temp = File(parent, tempName)
        temp.writeBytes(bytes)
        if (!temp.renameTo(target)) {
            target.delete()
            temp.renameTo(target)
        }
    }

    private companion object {
        const val PACK_TMP = "pack.json.tmp"
    }
}
