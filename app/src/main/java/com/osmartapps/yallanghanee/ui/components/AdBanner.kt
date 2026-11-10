package com.osmartapps.yallanghanee.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.osmartapps.yallanghanee.BuildConfig

@Composable
fun AdBanner(enabled: Boolean, modifier: Modifier = Modifier) {
    if (!enabled) return
    val context = LocalContext.current
    val adView = remember(context) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
        }
    }
    LaunchedEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
    }
    AndroidView(
        factory = { adView },
        onRelease = { it.destroy() },
        modifier = modifier,
    )
}
