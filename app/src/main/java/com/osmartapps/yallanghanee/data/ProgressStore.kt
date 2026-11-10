package com.osmartapps.yallanghanee.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.osmartapps.yallanghanee.ads.AdCounters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

data class OnboardingProgress(
    val step: Int = 0,
    val complete: Boolean = false,
)

class ProgressStore(private val context: Context) {
    private val completedLessonsKey = stringSetPreferencesKey("completed_lessons")
    private val onboardingStepKey = intPreferencesKey("onboarding_step")
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
    private val lessonsSinceAdKey = intPreferencesKey("lessons_since_ad")
    private val lastAppOpenDayKey = longPreferencesKey("last_app_open_day")

    val completedLessons: Flow<Set<String>> = context.progressDataStore.data.map {
        it[completedLessonsKey].orEmpty()
    }

    val adCounters: Flow<AdCounters> = context.progressDataStore.data.map {
        AdCounters(
            completedSinceAd = it[lessonsSinceAdKey] ?: 0,
            lastAppOpenEpochDay = it[lastAppOpenDayKey] ?: AdCounters.NEVER,
        )
    }

    val onboarding: Flow<OnboardingProgress> = context.progressDataStore.data.map {
        OnboardingProgress(
            step = it[onboardingStepKey] ?: 0,
            complete = it[onboardingCompleteKey] ?: false,
        )
    }

    suspend fun markLessonCompleted(lessonId: String) {
        context.progressDataStore.edit {
            it[completedLessonsKey] = it[completedLessonsKey].orEmpty() + lessonId
        }
    }

    suspend fun setOnboardingStep(step: Int) {
        context.progressDataStore.edit { it[onboardingStepKey] = step }
    }

    suspend fun completeOnboarding() {
        context.progressDataStore.edit {
            it[onboardingCompleteKey] = true
            it.remove(onboardingStepKey)
        }
    }

    suspend fun resetProgress() {
        context.progressDataStore.edit { it.remove(completedLessonsKey) }
    }

    suspend fun setAdCounters(counters: AdCounters) {
        context.progressDataStore.edit {
            it[lessonsSinceAdKey] = counters.completedSinceAd
            it[lastAppOpenDayKey] = counters.lastAppOpenEpochDay
        }
    }
}
