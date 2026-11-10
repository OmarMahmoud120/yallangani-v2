package com.osmartapps.yallanghanee.data

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

fun interface RemotePackSource {
    fun get(path: String, maxBytes: Int): ByteArray?
}

class HttpRemotePackSource(
    private val baseUrl: String,
    private val connectTimeoutMs: Int = 5_000,
    private val readTimeoutMs: Int = 5_000,
) : RemotePackSource {

    override fun get(path: String, maxBytes: Int): ByteArray? = runCatching {
        val url = URL(baseUrl + path)
        if (url.protocol != "https") return null
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept-Encoding", "identity")
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val type = connection.contentType.orEmpty()
            if (!type.startsWith("application/json") && !type.startsWith("audio/")) return null
            connection.inputStream.use { readCapped(it, maxBytes) }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()

    private fun readCapped(input: java.io.InputStream, maxBytes: Int): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) return null
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }
}
