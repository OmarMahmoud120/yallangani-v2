package com.osmartapps.yallanghanee.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

// Notation PNGs are black on transparent, so they need a light plate to read.
private val NotationPlate = Color(0xFFF4EEDF)

// Fixed height keeps the feedback sheet from growing after the image arrives, which would
// otherwise leave the «التالي» button scrolled out of reach.
private val NotationHeight = 104.dp

private val notationCache = object : LruCache<String, Bitmap>(12) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
}

@Composable
fun NotationImage(
    url: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (url.isNullOrBlank()) return
    var bitmap by remember(url) { mutableStateOf(notationCache.get(url)) }
    LaunchedEffect(url) {
        if (bitmap != null) return@LaunchedEffect
        val loaded = withContext(Dispatchers.IO) { fetch(url) }
        if (loaded != null) {
            notationCache.put(url, loaded)
            bitmap = loaded
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NotationHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(NotationPlate)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap ?: return@Box
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun fetch(url: String): Bitmap? = runCatching {
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.instanceFollowRedirects = false
        connection.inputStream.use { BitmapFactory.decodeStream(it) }
    } finally {
        connection.disconnect()
    }
}.getOrNull()
