package com.osmartapps.yallanghanee.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionPreferenceTest {
    @Test
    fun systemFollowsTheAnimatorScale() {
        assertTrue(MotionPreference.System.enabled(systemAnimationsEnabled = true))
        assertFalse(MotionPreference.System.enabled(systemAnimationsEnabled = false))
    }

    @Test
    fun reducedAlwaysDisablesMotion() {
        assertFalse(MotionPreference.Reduced.enabled(systemAnimationsEnabled = true))
        assertFalse(MotionPreference.Reduced.enabled(systemAnimationsEnabled = false))
    }

    @Test
    fun fullAlwaysEnablesMotion() {
        assertTrue(MotionPreference.Full.enabled(systemAnimationsEnabled = true))
        assertTrue(MotionPreference.Full.enabled(systemAnimationsEnabled = false))
    }
}
