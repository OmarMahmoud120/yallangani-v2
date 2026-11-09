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

class ProgressStore(private val context: Context) {
    private val roundsCompletedKey = intPreferencesKey("rounds_completed")
    private val solvedLevelIdsKey = stringSetPreferencesKey("solved_level_ids")
    private val onboardingSeenKey = booleanPreferencesKey("onboarding_seen")

    val roundsCompleted: Flow<Int> =
        context.progressDataStore.data.map { it[roundsCompletedKey] ?: 0 }

    val solvedLevelIds: Flow<Set<Int>> = context.progressDataStore.data.map {
        it[solvedLevelIdsKey].orEmpty().mapNotNull { id -> id.toIntOrNull() }.toSet()
    }

    val onboardingSeen: Flow<Boolean> = context.progressDataStore.data.map {
        it[onboardingSeenKey] ?: false
    }

    suspend fun setRoundsCompleted(rounds: Int) {
        context.progressDataStore.edit { it[roundsCompletedKey] = rounds.coerceAtLeast(0) }
    }

    suspend fun markSolved(levelId: Int) {
        context.progressDataStore.edit {
            it[solvedLevelIdsKey] = it[solvedLevelIdsKey].orEmpty() + levelId.toString()
        }
    }

    suspend fun setOnboardingSeen() {
        context.progressDataStore.edit { it[onboardingSeenKey] = true }
    }

    suspend fun resetProgress() {
        context.progressDataStore.edit {
            it.remove(solvedLevelIdsKey)
            it.remove(roundsCompletedKey)
        }
    }
}
