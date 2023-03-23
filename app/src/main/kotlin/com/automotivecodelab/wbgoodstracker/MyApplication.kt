package com.automotivecodelab.wbgoodstracker

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.automotivecodelab.wbgoodstracker.di.AppComponent
import com.automotivecodelab.wbgoodstracker.di.DaggerAppComponent
import com.squareup.picasso.Picasso
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
            Picasso.get().setIndicatorsEnabled(true)
        }
        appComponent = DaggerAppComponent
            .factory()
            .create(this)
    }
}

val Context.appComponent: AppComponent
    get() = (applicationContext as MyApplication).appComponent
