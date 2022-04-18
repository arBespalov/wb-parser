package com.automotivecodelab.wbgoodstracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = {
        listOf(SharedPreferencesMigration(it, "prefs", setOf("isUserSignedIn")))
    })
@Module
class DataStoreModule {
    @Provides
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}