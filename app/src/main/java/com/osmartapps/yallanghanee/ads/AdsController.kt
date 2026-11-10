package com.osmartapps.yallanghanee.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.osmartapps.yallanghanee.BuildConfig

class AdsController {
    private var initialized = false
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null
    private var appOpen: AppOpenAd? = null
    private var loadingInterstitial = false
    private var loadingRewarded = false
    private var loadingAppOpen = false

    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        MobileAds.initialize(context.applicationContext)
    }

    fun preload(context: Context) {
        loadInterstitial(context.applicationContext)
        loadRewarded(context.applicationContext)
        loadAppOpen(context.applicationContext)
    }

    fun hasInterstitial(): Boolean = interstitial != null

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        val ad = interstitial
        if (ad == null) {
            loadInterstitial(activity.applicationContext)
            onDismissed()
            return
        }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitial(activity.applicationContext)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadInterstitial(activity.applicationContext)
                onDismissed()
            }
        }
        ad.show(activity)
    }

    fun showRewarded(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewarded
        if (ad == null) {
            loadRewarded(activity.applicationContext)
            onUnavailable()
            return
        }
        rewarded = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadRewarded(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadRewarded(activity.applicationContext)
            }
        }
        ad.show(activity) { onReward() }
    }

    fun showAppOpen(activity: Activity, onDismissed: () -> Unit) {
        val ad = appOpen
        if (ad == null) {
            loadAppOpen(activity.applicationContext)
            onDismissed()
            return
        }
        appOpen = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadAppOpen(activity.applicationContext)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadAppOpen(activity.applicationContext)
                onDismissed()
            }
        }
        ad.show(activity)
    }

    private fun loadInterstitial(context: Context) {
        if (loadingInterstitial || interstitial != null) return
        loadingInterstitial = true
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadingInterstitial = false
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingInterstitial = false
                }
            },
        )
    }

    private fun loadRewarded(context: Context) {
        if (loadingRewarded || rewarded != null) return
        loadingRewarded = true
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadingRewarded = false
                    rewarded = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingRewarded = false
                }
            },
        )
    }

    private fun loadAppOpen(context: Context) {
        if (loadingAppOpen || appOpen != null) return
        loadingAppOpen = true
        AppOpenAd.load(
            context,
            BuildConfig.ADMOB_APP_OPEN_UNIT_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    loadingAppOpen = false
                    appOpen = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingAppOpen = false
                }
            },
        )
    }
}
