package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CurrentGroupLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
): CurrentGroupLocalDataSource {

    private val CURRENT_GROUP = stringPreferencesKey("current_group")

    override fun observeCurrentGroup(): Flow<String?> {
        return dataStore.data
            .map { prefs ->
                prefs[CURRENT_GROUP]
            }
            .distinctUntilChanged()
    }

    override suspend fun setCurrentGroup(groupName: String?) {
        dataStore.edit { prefs ->
            if (groupName == null) {
                prefs.remove(CURRENT_GROUP)
            } else {
                prefs[CURRENT_GROUP] = groupName
            }
        }
    }
}