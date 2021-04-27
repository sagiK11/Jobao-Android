package com.sagikor.android.jobao.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesHandler"

enum class SortOrder { BY_COMPANY, BY_DATE }

data class FilteredPreferences(val sortOrder: SortOrder, val hideRejected: Boolean)

@Singleton
class PreferencesHandler @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore("user_preferences")

    val preferenceFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error: Reading preferences ", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }

        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_COMPANY.name
            )
            val hideRejected = preferences[PreferencesKeys.HIDE_REJECTED] ?: false
            FilteredPreferences(sortOrder, hideRejected)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideRejected(hideRejected: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_REJECTED] = hideRejected
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_REJECTED = preferencesKey<Boolean>("hide_rejected")
    }
}