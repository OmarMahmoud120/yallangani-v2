package com.osmartapps.yallanghanee.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.osmartapps.yallanghanee.model.MotionPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val motionKey = intPreferencesKey("motion_preference")

    val motion: Flow<MotionPreference> = context.settingsDataStore.data.map { preferences ->
        MotionPreference.entries.getOrElse(preferences[motionKey] ?: 0) {
            MotionPreference.System
        }
    }

    suspend fun setMotion(preference: MotionPreference) {
        context.settingsDataStore.edit { it[motionKey] = preference.ordinal }
    }
}
