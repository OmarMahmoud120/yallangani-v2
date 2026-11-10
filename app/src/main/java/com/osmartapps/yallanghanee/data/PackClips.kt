package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.MaqamPack
import java.io.File
import java.security.MessageDigest

private val SAFE_CLIP_NAME = Regex("[A-Za-z0-9][A-Za-z0-9._-]*")

fun MaqamPack.clipNames(): Set<String> {
    val names = mutableSetOf<String>()
    maqams.forEach { maqam ->
        names += maqam.audio
        maqam.drone?.let { names += it }
    }
    onboarding.steps.forEach { names += it.audio }
    names += onboarding.firstWin.audio
    lessons.forEach { lesson ->
        lesson.items.forEach { item -> item.audio?.let { names += it } }
    }
    return names
}

fun isSafeClipName(name: String): Boolean = SAFE_CLIP_NAME.matches(name)

fun sha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

fun sha256(file: File): String =
    MessageDigest.getInstance("SHA-256").run {
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                update(buffer, 0, read)
            }
        }
        digest().toHex()
    }

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
