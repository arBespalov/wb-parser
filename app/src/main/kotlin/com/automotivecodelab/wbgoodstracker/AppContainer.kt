package com.automotivecodelab.wbgoodstracker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.automotivecodelab.wbgoodstracker.data.NetworkStatusListener
import com.automotivecodelab.wbgoodstracker.data.items.ItemsRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.items.local.AppDatabase
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.items.remote.ServerApi
import com.automotivecodelab.wbgoodstracker.data.sort.SortLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.sort.SortRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.user.AuthenticationServiceImpl
import com.automotivecodelab.wbgoodstracker.data.user.UserRepositoryImpl
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.automotivecodelab.wbgoodstracker.ui.AppThemeSource
import kotlinx.coroutines.runBlocking

const val PREFS_NAME = "prefs"
const val SAVED_UI_MODE = "savedUiMode"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppContainer(context: Context) {

    val itemsRepository: ItemsRepository
    val userRepository: UserRepository
    val sortRepository: SortRepository
    val appThemeSource: AppThemeSource

    init {
        val db = AppDatabase(context)
        val itemDao = db.itemDao()
        val sizeDao = db.sizeDao()
        val dataStore = context.dataStore
        val itemsLocalDataSource = ItemsLocalDataSourceImpl(
            itemDao,
            sizeDao,
            dataStore
        )
        val networkStatusListener = NetworkStatusListener(context)
        val serverApi = ServerApi(networkStatusListener)
        val itemsRemoteDataSource = ItemsRemoteDataSourceImpl(serverApi)
        itemsRepository = ItemsRepositoryImpl(
            itemsLocalDataSource,
            itemsRemoteDataSource
        )
        val authenticationService = AuthenticationServiceImpl(context)
        userRepository = UserRepositoryImpl(dataStore, authenticationService)
        sortRepository = SortRepositoryImpl(SortLocalDataSourceImpl(dataStore))
        appThemeSource = AppThemeSource(dataStore)
    }
}
