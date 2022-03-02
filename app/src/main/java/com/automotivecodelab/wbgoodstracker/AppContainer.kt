package com.automotivecodelab.wbgoodstracker

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.automotivecodelab.wbgoodstracker.data.NetworkStatusListener
import com.automotivecodelab.wbgoodstracker.data.ResourceManagerImpl
import com.automotivecodelab.wbgoodstracker.data.items.ItemsRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.items.local.AppDatabase
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.items.local.SAVED_SORTING_MODE
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.user.AuthenticationServiceImpl
import com.automotivecodelab.wbgoodstracker.data.user.UserRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.user.local.UserLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

const val PREFS_NAME = "prefs"
const val SAVED_UI_MODE = "savedUiMode"

class AppContainer(context: Context) {

    val itemsRepository: ItemsRepository
    val userRepository: UserRepository

    init {
        val db = AppDatabase(context)
        val itemDao = db.itemDao()
        val sizeDao = db.sizeDao()
        // encrypted SP is not necessary here
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val itemsLocalDataSource = ItemsLocalDataSourceImpl(itemDao, sizeDao, sharedPreferences)
        val networkStatusListener = NetworkStatusListener(context)
        val itemsRemoteDataSource = ItemsRemoteDataSourceImpl(networkStatusListener)
        val resourcesManager = ResourceManagerImpl(context)
        itemsRepository = ItemsRepositoryImpl(
            itemsLocalDataSource,
            itemsRemoteDataSource,
            resourcesManager
        )

        val userLocalDataSource = UserLocalDataSourceImpl(sharedPreferences)
        val authenticationService = AuthenticationServiceImpl(context)
        userRepository = UserRepositoryImpl(userLocalDataSource, authenticationService)

        if (!sharedPreferences.contains(SAVED_SORTING_MODE)) {
            sharedPreferences.edit()
                .putInt(SAVED_SORTING_MODE, SortingMode.BY_DATE_DESC.ordinal)
                .apply()
        }

        if (sharedPreferences.contains(SAVED_UI_MODE)) {
            val uiMode = sharedPreferences.getInt(
                SAVED_UI_MODE,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
            AppCompatDelegate.setDefaultNightMode(uiMode)
        }
    }
}
