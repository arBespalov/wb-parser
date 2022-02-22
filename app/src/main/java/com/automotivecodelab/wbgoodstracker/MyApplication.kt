package com.automotivecodelab.wbgoodstracker

import android.app.Application

class MyApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}