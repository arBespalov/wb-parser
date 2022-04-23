package com.automotivecodelab.wbgoodstracker.data.usagestatistics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.automotivecodelab.wbgoodstracker.domain.repositories.UsageStatisticsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UsageStatisticsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UsageStatisticsRepository {

    val updateAllItemsActionCountKey = intPreferencesKey("updateAllItemsActionCountKey")

    override suspend fun incrementUpdateAllItemsAction() {
        dataStore.edit { prefs ->
            val currentValue = prefs[updateAllItemsActionCountKey] ?: 0
            prefs[updateAllItemsActionCountKey] = currentValue + 1
        }
    }

    override suspend fun getCountOfUpdateAllItemsAction(): Int {
        return dataStore.data
            .map { prefs -> prefs[updateAllItemsActionCountKey] ?: 0 }
            .first()
    }
}
