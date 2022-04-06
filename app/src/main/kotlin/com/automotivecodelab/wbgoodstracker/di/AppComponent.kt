package com.automotivecodelab.wbgoodstracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.data.NetworkStatusListener
import com.automotivecodelab.wbgoodstracker.data.NoInternetConnectionException
import com.automotivecodelab.wbgoodstracker.data.items.ItemsRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.items.local.*
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.items.remote.ServerApi
import com.automotivecodelab.wbgoodstracker.data.sort.SortLocalDataSource
import com.automotivecodelab.wbgoodstracker.data.sort.SortLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.sort.SortRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.user.AuthenticationService
import com.automotivecodelab.wbgoodstracker.data.user.AuthenticationServiceImpl
import com.automotivecodelab.wbgoodstracker.data.user.UserRepositoryImpl
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.automotivecodelab.wbgoodstracker.ui.AppThemeSource
import com.automotivecodelab.wbgoodstracker.ui.additemfrag.AddItemViewModel
import com.automotivecodelab.wbgoodstracker.ui.chartfragment.ChartViewModel
import com.automotivecodelab.wbgoodstracker.ui.confirmdeletegroupdialogfrag.ConfirmDeleteGroupDialogViewModel
import com.automotivecodelab.wbgoodstracker.ui.confirmremovedialogfrag.ConfirmRemoveDialogViewModel
import com.automotivecodelab.wbgoodstracker.ui.detailfrag.DetailViewModel
import com.automotivecodelab.wbgoodstracker.ui.edititemfrag.EditItemViewModel
import com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag.GroupPickerDialogViewModel
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.ItemsViewModel
import com.automotivecodelab.wbgoodstracker.ui.newgroupdialogfrag.NewGroupDialogViewModel
import com.automotivecodelab.wbgoodstracker.ui.signinfrag.SignInViewModel
import dagger.*
import dagger.assisted.AssistedFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    RoomModule::class,
    DataStoreModule::class,
    NetworkModule::class
])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun appThemeSource(): AppThemeSource
    fun addItemViewModel(): AddItemViewModel

    @AssistedFactory
    interface ChartViewModelAssistedFactory {
        fun create(itemId: String): ChartViewModel
    }
    fun chartViewModelFactory(): ChartViewModelAssistedFactory

    fun confirmDeleteGroupDialogViewModel(): ConfirmDeleteGroupDialogViewModel
    fun confirmRemoveDialogViewModel(): ConfirmRemoveDialogViewModel

    @AssistedFactory
    interface DetailViewModelAssistedFactory {
        fun create(itemId: String): DetailViewModel
    }
    fun detailViewModelFactory(): DetailViewModelAssistedFactory

    @AssistedFactory
    interface EditItemViewModelAssistedFactory {
        fun create(itemId: String): EditItemViewModel
    }
    fun editItemViewModelFactory(): EditItemViewModelAssistedFactory

    fun groupPickerDialogViewModel(): GroupPickerDialogViewModel
    fun itemsViewModel(): ItemsViewModel
    fun newGroupDialogViewModel(): NewGroupDialogViewModel
    fun signInViewModel(): SignInViewModel
}







