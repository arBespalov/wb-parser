package com.automotivecodelab.wbgoodstracker.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
class CoroutinesSupervisorScopesModule {

    @Provides
    @Singleton
    fun provideSupervisorCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}