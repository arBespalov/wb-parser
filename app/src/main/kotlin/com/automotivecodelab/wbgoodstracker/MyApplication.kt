package com.automotivecodelab.wbgoodstracker

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.automotivecodelab.wbgoodstracker.di.AppComponent
import com.automotivecodelab.wbgoodstracker.di.DaggerAppComponent
import dagger.internal.DaggerGenerated
import timber.log.Timber

class MyApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        appComponent = DaggerAppComponent
            .factory()
            .create(this)
    }
}

val Context.appComponent: AppComponent
    get() = (applicationContext as MyApplication).appComponent
