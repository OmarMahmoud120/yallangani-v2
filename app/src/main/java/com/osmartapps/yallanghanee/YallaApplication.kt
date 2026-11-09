package com.osmartapps.yallanghanee

import android.app.Application
import com.google.android.gms.ads.MobileAds

class YallaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
