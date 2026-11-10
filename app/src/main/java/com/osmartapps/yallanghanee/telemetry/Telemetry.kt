package com.osmartapps.yallanghanee.telemetry

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * First-party event logging. Never pass free text, names, or audio data here; only ids,
 * counts, and booleans from the content pack.
 */
object Telemetry {
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    fun screen(name: String) = event(FirebaseAnalytics.Event.SCREEN_VIEW) {
        putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
    }

    fun lessonStarted(lessonId: String, maqamCount: Int) = event("lesson_started") {
        putString("lesson_id", lessonId)
        putInt("maqam_count", maqamCount)
    }

    fun answerSubmitted(
        lessonId: String,
        itemId: String,
        maqamId: String,
        correct: Boolean,
        position: Int,
    ) = event("answer_submitted") {
        putString("lesson_id", lessonId)
        putString("item_id", itemId)
        putString("maqam_id", maqamId)
        putBoolean("correct", correct)
        putInt("position", position)
    }

    fun lessonCompleted(lessonId: String, correct: Int, total: Int, passed: Boolean) =
        event("lesson_completed") {
            putString("lesson_id", lessonId)
            putInt("correct", correct)
            putInt("total", total)
            putBoolean("passed", passed)
        }

    fun onboardingCompleted() = event("onboarding_completed")

    fun settingChanged(key: String, value: String) = event("setting_changed") {
        putString("key", key)
        putString("value", value)
    }

    fun rewardedHintGranted() = event("rewarded_completed") {
        putString("placement", "hint")
        putString("reward_type", "fifty_fifty")
    }

    private inline fun event(name: String, params: Bundle.() -> Unit = {}) {
        analytics?.logEvent(name, Bundle().apply(params))
    }
}
