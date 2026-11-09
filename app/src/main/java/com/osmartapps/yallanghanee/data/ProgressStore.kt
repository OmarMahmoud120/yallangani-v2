package com.osmartapps.yallanghanee.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    val completedLessons: Flow<Set<String>> = context.progressDataStore.data.map {
        it[completedLessonsKey].orEmpty()
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
}
