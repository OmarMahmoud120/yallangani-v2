package com.osmartapps.yallanghanee.ads

data class AdCounters(
    val completedSinceAd: Int = 0,
    val lastAppOpenEpochDay: Long = NEVER,
) {
    companion object {
        const val NEVER = -1L
    }
}

object AdPolicy {
    const val LESSONS_PER_INTERSTITIAL = 3
    private const val MILLIS_PER_DAY = 86_400_000L

    fun epochDay(nowMillis: Long): Long = nowMillis / MILLIS_PER_DAY

    fun afterLessonCompleted(counters: AdCounters): AdCounters =
        counters.copy(completedSinceAd = counters.completedSinceAd + 1)

    fun shouldShowInterstitial(counters: AdCounters): Boolean =
        counters.completedSinceAd >= LESSONS_PER_INTERSTITIAL

    fun afterInterstitial(counters: AdCounters): AdCounters =
        counters.copy(completedSinceAd = 0)

    fun shouldShowAppOpen(
        counters: AdCounters,
        todayEpochDay: Long,
        onboardingComplete: Boolean,
        canRequestAds: Boolean,
    ): Boolean = onboardingComplete &&
        canRequestAds &&
        counters.lastAppOpenEpochDay != todayEpochDay

    fun afterAppOpen(counters: AdCounters, todayEpochDay: Long): AdCounters =
        counters.copy(lastAppOpenEpochDay = todayEpochDay)
}
