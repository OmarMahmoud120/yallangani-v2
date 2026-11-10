package com.osmartapps.yallanghanee.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPolicyTest {
    @Test
    fun `interstitial waits for the third completed lesson`() {
        var counters = AdCounters()
        repeat(2) {
            counters = AdPolicy.afterLessonCompleted(counters)
            assertFalse(AdPolicy.shouldShowInterstitial(counters))
        }
        counters = AdPolicy.afterLessonCompleted(counters)
        assertTrue(AdPolicy.shouldShowInterstitial(counters))
    }

    @Test
    fun `showing an interstitial restarts the count`() {
        val shown = AdPolicy.afterInterstitial(AdCounters(completedSinceAd = 3))
        assertEquals(0, shown.completedSinceAd)
        assertFalse(AdPolicy.shouldShowInterstitial(shown))
    }

    @Test
    fun `app open shows once per day and never during onboarding`() {
        val today = AdPolicy.epochDay(1_700_000_000_000)
        val fresh = AdCounters()
        assertFalse(AdPolicy.shouldShowAppOpen(fresh, today, onboardingComplete = false, canRequestAds = true))
        assertFalse(AdPolicy.shouldShowAppOpen(fresh, today, onboardingComplete = true, canRequestAds = false))
        assertTrue(AdPolicy.shouldShowAppOpen(fresh, today, onboardingComplete = true, canRequestAds = true))

        val shown = AdPolicy.afterAppOpen(fresh, today)
        assertFalse(AdPolicy.shouldShowAppOpen(shown, today, onboardingComplete = true, canRequestAds = true))
        assertTrue(
            AdPolicy.shouldShowAppOpen(
                shown,
                AdPolicy.epochDay(1_700_000_000_000 + 86_400_000L),
                onboardingComplete = true,
                canRequestAds = true,
            ),
        )
    }
}
