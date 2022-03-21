package com.automotivecodelab.wbgoodstracker

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.automotivecodelab.wbgoodstracker.data.NetworkStatusListener
import com.automotivecodelab.wbgoodstracker.data.items.ItemsRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.items.local.AppDatabase
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.sort.SortLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.sort.SortRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.user.AuthenticationServiceImpl
import com.automotivecodelab.wbgoodstracker.data.user.UserRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.user.local.UserLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

const val PREFS_NAME = "prefs"
const val SAVED_UI_MODE = "savedUiMode"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppContainer(context: Context) {

    val itemsRepository: ItemsRepository
    val userRepository: UserRepository
    val sortRepository: SortRepository

    init {
        val db = AppDatabase(context)
        val itemDao = db.itemDao()
        val sizeDao = db.sizeDao()
        // encrypted SP is not necessary here
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dataStore = context.dataStore
        val itemsLocalDataSource = ItemsLocalDataSourceImpl(
            itemDao,
            sizeDao,
            dataStore
        )
        val networkStatusListener = NetworkStatusListener(context)
        val itemsRemoteDataSource = ItemsRemoteDataSourceImpl(networkStatusListener)
        itemsRepository = ItemsRepositoryImpl(
            itemsLocalDataSource,
            itemsRemoteDataSource
        )

        val userLocalDataSource = UserLocalDataSourceImpl(sharedPreferences)
        val authenticationService = AuthenticationServiceImpl(context)
        userRepository = UserRepositoryImpl(userLocalDataSource, authenticationService)

        sortRepository = SortRepositoryImpl(SortLocalDataSourceImpl(dataStore))

        if (sharedPreferences.contains(SAVED_UI_MODE)) {
            val uiMode = sharedPreferences.getInt(
                SAVED_UI_MODE,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
            AppCompatDelegate.setDefaultNightMode(uiMode)
        }
    }
}
