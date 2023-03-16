package com.automotivecodelab.wbgoodstracker.di

import android.content.Context
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
import com.automotivecodelab.wbgoodstracker.ui.quantitychartfrag.QuantityChartViewModel
import com.automotivecodelab.wbgoodstracker.ui.signinfrag.SignInViewModel
import dagger.*
import dagger.assisted.AssistedFactory
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        RoomModule::class,
        DataStoreModule::class,
        NetworkModule::class,
        CoroutinesSupervisorScopesModule::class
    ]
)
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

    @AssistedFactory
    interface QuantityChartViewModelAssistedFactory {
        fun create(itemId: String): QuantityChartViewModel
    }
    fun quantityChartViewModelFactory(): QuantityChartViewModelAssistedFactory
}
