package com.automotivecodelab.wbgoodstracker

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.automotivecodelab.wbgoodstracker.di.AppComponent
import com.automotivecodelab.wbgoodstracker.di.DaggerAppComponent
import com.google.android.play.core.review.ReviewManager
import dagger.internal.DaggerGenerated
import timber.log.Timber

class MyApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
        appComponent = DaggerAppComponent
            .factory()
            .create(this)
    }
}

val Context.appComponent: AppComponent
    get() = (applicationContext as MyApplication).appComponent
