package com.automotivecodelab.wbgoodstracker.data.sort

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface SortLocalDataSource {
    fun getSortingMode(): Flow<SortingMode>
    suspend fun setSortingMode(sortingMode: SortingMode)
}

class SortLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SortLocalDataSource {
    private val SAVED_SORTING_MODE = intPreferencesKey("savedSortingMode")

    override fun getSortingMode(): Flow<SortingMode> {
        return dataStore.data
            .map { prefs ->
                SortingMode.values()[
                    prefs[SAVED_SORTING_MODE]
                        ?: SortingMode.BY_LAST_CHANGES.ordinal
                ]
            }
            .distinctUntilChanged()
    }

    override suspend fun setSortingMode(sortingMode: SortingMode) {
        dataStore.edit { prefs ->
            prefs[SAVED_SORTING_MODE] = sortingMode.ordinal
        }
    }
}
