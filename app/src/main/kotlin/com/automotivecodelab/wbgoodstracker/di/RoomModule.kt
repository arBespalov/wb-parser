package com.automotivecodelab.wbgoodstracker.di

import android.content.Context
import com.automotivecodelab.wbgoodstracker.data.items.local.AppDatabase
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemDao
import com.automotivecodelab.wbgoodstracker.data.items.local.SizeDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {
    @Singleton
    @Provides
    fun provideRoomDb(context: Context): AppDatabase {
        return AppDatabase.invoke(context)
    }

    @Singleton
    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao {
        return db.itemDao()
    }

    @Singleton
    @Provides
    fun provideSizeDao(db: AppDatabase): SizeDao {
        return db.sizeDao()
    }
}