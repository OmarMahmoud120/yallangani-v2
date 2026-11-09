package com.osmartapps.yallanghanee.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

class ProgressStore(private val context: Context) {
    private val completedLessonsKey = stringSetPreferencesKey("completed_lessons")

    val completedLessons: Flow<Set<String>> = context.progressDataStore.data.map {
        it[completedLessonsKey].orEmpty()
    }

    suspend fun markLessonCompleted(lessonId: String) {
        context.progressDataStore.edit {
            it[completedLessonsKey] = it[completedLessonsKey].orEmpty() + lessonId
        }
    }

    suspend fun resetProgress() {
        context.progressDataStore.edit { it.remove(completedLessonsKey) }
    }
}
